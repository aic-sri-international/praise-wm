package com.sri.ai.praisewm.web.ws;

import org.eclipse.jetty.websocket.api.StatusCode;

public enum WsStatusCode {
  NORMAL(StatusCode.NORMAL),
  TIMEOUT(StatusCode.TRY_AGAIN_LATER),

  // WebSocket OnError condition occurred for the Session's connection.
  // The connection is now suspect, and the connection is likely being shut down.
  ON_ERROR(StatusCode.SERVER_ERROR);

  private final int code;

  WsStatusCode(int code) {
    this.code = code;
  }

  int code() {
    return code;
  }
}
