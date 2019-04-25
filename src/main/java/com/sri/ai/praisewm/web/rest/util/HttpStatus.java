package com.sri.ai.praisewm.web.rest.util;

/** HTTP Status codes */
public final class HttpStatus {
  /**
   * The request was successful and there is content in the response body unless the status is sent
   * in response to a HEAD request, in which case, there will not be a message body.
   */
  public static final int OK = 200;

  /**
   * Resource was created and the response contains a Location header that the client can use to get
   * the newly created resource.
   */
  public static final int CREATED = 201;

  /** The request completed successfully and the response body does not contain a message */
  public static final int NO_CONTENT = 204;

  public static final int BAD_REQUEST = 400;
  public static final int UNAUTHORIZED = 401;
  public static final int NOT_FOUND = 404;
  public static final int REQUEST_TIMEOUT = 408;
  public static final int PERMANENT_REDIRECT = 308;

  /**
   * Resource conflict such as trying to create two users with the same information, deleting root
   * objects (device) when cascade-delete is not supported.
   */
  public static final int CONFLICT = 409;

  public static final int INTERNAL_SERVER_ERROR = 500;

  private HttpStatus() {}
}
