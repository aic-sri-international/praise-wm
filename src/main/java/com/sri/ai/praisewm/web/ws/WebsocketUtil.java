package com.sri.ai.praisewm.web.ws;

import com.sri.ai.praisewm.service.SecurityServiceImpl;
import java.util.List;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class WebsocketUtil {
  private static final Logger LOG = LoggerFactory.getLogger(WebsocketUtil.class);
  static String getFormattedWsSessionInfo(Session session) {
    return String.format(
        "Endpoint=%s, RemoteAddress=%s, ParameterMap=%s",
        session.getUpgradeRequest().getRequestURI().getPath(),
        session.getRemoteAddress(),
        session.getUpgradeRequest().getParameterMap());
  }

  static String getSessionId(Session session) {
    List<String> keyValues =
        session.getUpgradeRequest().getParameterMap().get(SecurityServiceImpl.SECURITY_HEADER_KEY);
    if (keyValues == null ) {
      LOG.error("{} key not found in WebSocket Session UpgradeRequest parameter map",
          SecurityServiceImpl.SECURITY_HEADER_KEY);
    } else if (keyValues.size() != 1) {
      LOG.error("{} key in WebSocket Session UpgradeRequest parameter map must contain 1 value, "
              + "but found {}: {}",
          SecurityServiceImpl.SECURITY_HEADER_KEY, keyValues.size(), keyValues);
    } else {
      String sessionId = keyValues.get(0).trim();
      if (sessionId.isEmpty()) {
        LOG.error("{} key in WebSocket Session UpgradeRequest parameter map contained an empty value",
            SecurityServiceImpl.SECURITY_HEADER_KEY);
      } else {
        return sessionId;
      }
    }

    return null;
  }
}
