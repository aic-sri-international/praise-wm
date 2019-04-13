package com.sri.ai.praisewm.service;

import com.sri.ai.praisewm.db.jooq.tables.pojos.User;
import com.sri.ai.praisewm.service.dto.LoginResponseDto;
import com.sri.ai.praisewm.event.notification.SessionLogoutEvent;
import com.sri.ai.praisewm.web.error.LoginException;
import java.util.Map;
import spark.Request;
import spark.Response;

/** The SecurityService provide UI client authentication and session management. */
public interface SecurityService {
  void authenticateAndAuthorize(Request request);

  /**
   * Handles a user's login request from the REST path {@link SecurityServiceImpl#LOGIN_PATH}.
   *
   * <p>After the user is authenticated, {@link SecurityServiceImpl#SECURITY_HEADER_KEY} is set in the
   * REST response header that is returned to the UI client. The client sends
   * {@link SecurityServiceImpl#SECURITY_HEADER_KEY} back to the server will each subsequent REST call.
   *
   * @param user login request from client that contains a name and password field.
   * @param request the REST request object
   * @param response the REST response object
   * @return the User request updated with the userId database PK and other user information.
   * @throws LoginException if the user cannot be authenticated
   */
  LoginResponseDto login(User user, Request request, Response response);

  /**
   * Called when a UI client logs out.
   *
   * <p>The client's session information will be removed and a {@link SessionLogoutEvent} will be
   * posted to the event bus so that other services can cleanup any resources (such as the client's
   * WebSocket) and send other client's an event notification that the client has logged out.
   *
   * @param request the REST request
   * @return true if the client's session was found
   */
  boolean logout(Request request);

  /**
   * Determine if a security session identifier maps to an authenticated login session and update
   * its last accessed time if valid.
   *
   * @param sessionId the identifier that was established by the login(User, Request, Response)
   *     method.
   * @return true if the session id is valid.
   */
  boolean updateAccess(String sessionId);

  /**
   * Get session id to {@link SessionInfo} map.
   *
   * @return the session info map
   */
  Map<String, SessionInfo> getSessionMap();
}
