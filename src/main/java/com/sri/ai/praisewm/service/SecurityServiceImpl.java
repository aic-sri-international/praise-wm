package com.sri.ai.praisewm.service;

import static org.apache.commons.lang3.StringUtils.trimToNull;

import com.google.common.eventbus.EventBus;
import com.google.common.hash.Hashing;
import com.sri.ai.praisewm.db.JooqTxProcessor;
import com.sri.ai.praisewm.db.jooq.tables.pojos.User;
import com.sri.ai.praisewm.event.notification.NotificationEvent;
import com.sri.ai.praisewm.event.notification.SessionCloseEvent;
import com.sri.ai.praisewm.event.notification.SessionLogoutEvent;
import com.sri.ai.praisewm.event.notification.SessionTimeoutEvent;
import com.sri.ai.praisewm.repository.UserRepository;
import com.sri.ai.praisewm.service.dto.LoginResponseDto;
import com.sri.ai.praisewm.util.PropertiesWrapper;
import com.sri.ai.praisewm.web.error.AuthenticationException;
import com.sri.ai.praisewm.web.error.AuthorizationException;
import com.sri.ai.praisewm.web.error.LoginException;
import com.sri.ai.praisewm.web.websocket.WebSocketConstants;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

/** SecurityServiceImpl. */
public class SecurityServiceImpl implements Service, SecurityService {
  public static final String SECURITY_HEADER_KEY = "SEC_SESSION_ID";
  public static final String LOGIN_PATH = "/api/login";
  public static final String ADMIN_PATH_PREFIX = "/admin/";
  public static final String ADMIN_NAME = "admin";
  private static final Logger LOG = LoggerFactory.getLogger(SecurityServiceImpl.class);
  private JooqTxProcessor txp;
  private EventBus eventBus;
  private Map<String, SessionInfo> sessionMap;
  private ScheduledExecutorService sessionTimeoutScheduler =
      Executors.newSingleThreadScheduledExecutor();
  private Long sessionTimeoutInMillis;
  private Integer localPort;
  private Integer wsReconnectInterval;
  private Integer maxReconnectAttempts;
  private Integer wsClientInactivityTimeout;

  public static String getSessionId(Request request) {
    return request.headers(SECURITY_HEADER_KEY);
  }

  @Override
  public void start(ServiceManager serviceManager) {
    txp = serviceManager.getJooqTxProcessor();
    eventBus = serviceManager.getEventBus();
    sessionMap = new ConcurrentHashMap<>();
    initTimeoutScheduler(serviceManager);

    PropertiesWrapper config = serviceManager.getConfiguration();
    localPort = config.asInt("server.port");
    wsReconnectInterval = config.asInt("client.ws.reconnectIntervalInMillis");
    maxReconnectAttempts = config.asInt("client.ws.maxReconnectAttempts");
    wsClientInactivityTimeout = config.asInt("client.ws.inactivityTimeoutInMillis");
  }

  private void initTimeoutScheduler(ServiceManager serviceManager) {
    PropertiesWrapper config = serviceManager.getConfiguration();

    sessionTimeoutInMillis = config.asLong("server.session.idleTimeoutInSeconds") * 1000;
    if (sessionTimeoutInMillis <= 0) {
      sessionTimeoutInMillis = Long.MAX_VALUE;
    }

    long timeInterval = config.asLong("server.session.timeoutIntervalInSeconds") * 1000;
    if (sessionTimeoutInMillis < timeInterval) {
      timeInterval = sessionTimeoutInMillis;
    }

    sessionTimeoutScheduler.scheduleWithFixedDelay(
        () -> {
          long timeout = System.currentTimeMillis() - sessionTimeoutInMillis;

          Arrays.stream(sessionMap.values().toArray(new SessionInfo[0]))
              .filter(s -> s.getLastAccessInMillis() < timeout)
              .forEach(
                  s -> {
                    NotificationEvent event =
                        new SessionTimeoutEvent().setText("TIMEOUT").setSessionId(s.getSessionId());
                    removeSession(s, (SessionTimeoutEvent) event);
                  });
        },
        timeInterval,
        timeInterval,
        TimeUnit.MILLISECONDS);
  }

  @Override
  public void stop() {
    sessionTimeoutScheduler.shutdownNow();
  }

  @Override
  public void authenticateAndAuthorize(Request request) {
    final String requestPath = request.pathInfo();

    if (requestPath.equals(LOGIN_PATH)) {
      // User authentication will be handled by the login method
      return;
    }

    if (requestPath.startsWith(WebSocketConstants.ENDPOINT_PREFIX)) {
      String sessionId = request.queryParams(SECURITY_HEADER_KEY);
      if (sessionId == null) {
        throw new AuthenticationException(
            request, SECURITY_HEADER_KEY + " query parameter not found");
      }

      if (!updateAccess(sessionId)) {
        throw new AuthenticationException(request, "Session not found");
      }
    } else {
      String sessionId = getSessionId(request);
      if (sessionId == null) {
        throw new AuthenticationException(request, SECURITY_HEADER_KEY + " not found in header");
      }

      authorizedRoute(sessionId, requestPath);

      if (!updateAccess(sessionId)) {
        throw new AuthenticationException(request, "Session not found");
      }
    }
  }

  private void authorizedRoute(String sessionId, String requestPath) {
    if (requestPath.startsWith(ADMIN_PATH_PREFIX)) {
      SessionInfo sessionInfo = sessionMap.get(sessionId);
      Validate.notNull(sessionInfo, "Entry not found in sessionMap for %s", sessionId);

      if (!sessionInfo.getUserName().equals(ADMIN_NAME)) {
        throw new AuthorizationException(
            String.format(
                "User '%s' is not authorized to access requested path: '%s'",
                sessionInfo.getUserName(), requestPath));
      }
    }
  }

  @Override
  public LoginResponseDto login(User user, Request request, Response response) {
    LOG.info("Login attempt for User:{} IP:{}", user.getName(), request.ip());
    if (trimToNull(user.getName()) == null || trimToNull(user.getPassword()) == null) {
      throw new LoginException(
          user.getName(), user.getPassword(), "User and password must both be set");
    }

    User userResult =
        txp.query(
            jc -> {
              User u = UserRepository.getUserAndPasswordByName(jc, user.getName());
              if (u == null) {
                throw new LoginException(user.getName(), user.getPassword(), "User not found");
              }

              String hash =
                  Hashing.sha256()
                      .hashString(user.getPassword(), StandardCharsets.UTF_8)
                      .toString();

              if (!hash.equals(u.getPassword())) {
                throw new LoginException(user.getName(), user.getPassword(), "Invalid password");
              }

              u.setPassword(user.getPassword());
              u.setLastLoginDate(Instant.now());
              UserRepository.updateUser(jc, u);

              // Neither of the following are needed in the reply.
              u.setPassword(null);
              u.setLastLoginDate(null);
              return u;
            });

    String oldSessionId = getSessionId(request);

    if (oldSessionId != null) {
      SessionInfo oldSession = sessionMap.get(oldSessionId);
      if (oldSession != null) {
        NotificationEvent event =
            new SessionLogoutEvent().setText("RE-LOGIN").setSessionId(oldSessionId);
        removeSession(oldSession, (SessionLogoutEvent) event);
      }
    }

    SessionInfo sessionInfo =
        new SessionInfo(UUID.randomUUID().toString(), userResult, request.ip());
    sessionMap.put(sessionInfo.getSessionId(), sessionInfo);

    response.header(SECURITY_HEADER_KEY, sessionInfo.getSessionId());

    LoginResponseDto loginResponse =
        new LoginResponseDto()
            .setUser(userResult)
            .setAdminRole(user.getName().equals(ADMIN_NAME))
            .setLocalPort(getLocalPort())
            .setServerTime(Instant.now())
            .setWsReconnectInterval(wsReconnectInterval)
            .setWsMaxReconnectAttempts(maxReconnectAttempts)
            .setWsClientInactivityTimeout(wsClientInactivityTimeout);

    LOG.info("Login succeeded: {}", formatSessionInfo(sessionInfo));

    return loginResponse;
  }

  public boolean logout(Request request) {
    String sessionId = getSessionId(request);
    SessionInfo sessionInfo = sessionMap.get(sessionId);
    if (sessionInfo == null) {
      LOG.warn(String.format("Session to be removed on LOGOUT not found: SessionId=%s", sessionId));
      return false;
    }

    NotificationEvent event = new SessionLogoutEvent().setText("LOGOUT").setSessionId(sessionId);
    removeSession(sessionInfo, (SessionLogoutEvent) event);
    return true;
  }

  private int getLocalPort() {
    // This is a bit of a kluge to get websockets working when using the Webpack dev server.
    // Since we use different ports for REST and Websockets, the client determines the WS port
    // by bumping the port it gets from window.location port by 1. However, the Webpack dev server
    // expects WS traffic to use the same port as HTTP, so, we pass the HTTP port in the header
    // and the client compares it to the window.location port (which will be the port of the
    // proxy server rather than the actual server port). If they differ, it knows not to increment
    // the port to get the WS port. This should also work in production when using a proxy server
    // as long as the ports differ.

    return localPort;
  }

  private void removeSession(SessionInfo sessionInfo, SessionCloseEvent closeEvent) {
    eventBus.post(closeEvent);
    sessionMap.remove(sessionInfo.getSessionId());

    LOG.info("Session removed due to {}: {}", closeEvent.getText(), formatSessionInfo(sessionInfo));
  }

  private String formatSessionInfo(SessionInfo sessionInfo) {
    return String.format(
        "User: %s, UserId: %d, IP: %s, SessionId: %s",
        sessionInfo.getUserName(),
        sessionInfo.getUserId(),
        sessionInfo.getRemoteIp(),
        sessionInfo.getSessionId());
  }

  public Map<String, SessionInfo> getSessionMap() {
    return sessionMap;
  }

  /**
   * Determine if a security session identifier maps to an authenticated login session and update
   * its last accessed time if valid.
   *
   * @param sessionId the identifier that was established by the {@link #login(User, Request,
   *     Response)} method.
   * @return true if the session id is valid.
   */
  @Override
  public boolean updateAccess(String sessionId) {
    SessionInfo sessionInfo = sessionMap.get(sessionId);
    if (sessionInfo != null) {
      sessionInfo.updateSessionAccessedTime();
    }

    return sessionInfo != null;
  }
}
