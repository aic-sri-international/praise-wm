package com.sri.ai.praisewm.web.error;

/**
 * Exception throw if the path portion of a URL request received by the webserver does not start
 * with any of the following:
 *
 * <ul>
 *   <li>{@link com.sri.ai.praisewm.web.WebController#API_ENDPOINT}
 *   <li>{@link com.sri.ai.praisewm.web.WebController#ADMIN_ENDPOINT}
 *   <li>{@link com.sri.ai.praisewm.web.websocket.WebSocketConstants#ENDPOINT_PREFIX}
 * </ul>
 */
public class PathNotDefinedException extends RuntimeException {

  public PathNotDefinedException() {
    super("Path not defined");
  }

  public PathNotDefinedException(String message) {
    super(message);
  }
}
