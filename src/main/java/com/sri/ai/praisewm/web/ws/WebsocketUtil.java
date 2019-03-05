package com.sri.ai.praisewm.web.ws;

import com.sri.ai.praisewm.service.SecurityServiceImpl;
import java.util.List;
import org.eclipse.jetty.websocket.api.Session;

class WebsocketUtil {
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
    if (keyValues != null && keyValues.size() == 1) {
      return keyValues.get(0);
    }

    return null;
  }
}
