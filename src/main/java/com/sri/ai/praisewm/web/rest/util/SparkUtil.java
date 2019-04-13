package com.sri.ai.praisewm.web.rest.util;

import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

import com.google.common.net.MediaType;
import com.sri.ai.praisewm.event.notification.DataRefreshEvent;
import com.sri.ai.praisewm.event.notification.DataRefreshEvent.RefreshType;
import com.sri.ai.praisewm.event.notification.NotificationEvent.Broadcast;
import com.sri.ai.praisewm.event.notification.NotificationEvent.Level;
import com.sri.ai.praisewm.event.notification.NotificationTextMessage;
import com.sri.ai.praisewm.service.SecurityServiceImpl;
import com.sri.ai.praisewm.util.JsonConverter;
import com.sri.ai.praisewm.web.error.BadRequestException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import org.apache.commons.io.FilenameUtils;
import spark.Request;
import spark.Response;

/**
 * Contains utility methods for JavaSpark route handling.
 *
 * <p>These utility methods provide functionality and consistency in the following areas
 *
 * <ul>
 *   <li>REST parameter access, type conversion, validation, and exception messages.
 *   <li>Java Object to JSON conversion
 *   <li>Updating the HTTP Response to include the correct MIME type, HTTP status, and headers.
 *   <li>File downloads and streaming I/O.
 * </ul>
 */
public final class SparkUtil {
  private SparkUtil() {}

  /**
   * Converts a Java Object to a JSON string and sets the response type.
   *
   * @param o Java Object to convert
   * @param response response to update
   * @return the JSON string
   */
  public static String toJson(Object o, Response response) {
    response.type("application/json");
    return JsonConverter.to(o);
  }

  /**
   * Converts a JSON string to a Java Object
   *
   * @param request the request containing the JSON to be converted
   * @param clazz the class type of target Java Object
   * @param <T> the return type
   * @return the Java Object
   */
  public static <T> T fromJson(Request request, Class<T> clazz) {
    return JsonConverter.from(request.body(), clazz);
  }

  /**
   * Use when the REST call does not expect a payload in the response.
   *
   * <p>This method should be called and returned as the last line in the REST method handler.
   *
   * @param response the response Object to update
   * @return the value to be returned from the REST method call
   */
  public static String respondNoContent(Response response) {
    return respondNoContentOrNotFound(response, true);
  }

  /**
   * Use when the REST call does not expect a payload in the response but the client may want to
   * know if the target of the request was found.
   *
   * <p>This method should be called and returned as the last line in the REST method handler.
   *
   * @param response the response Object to update
   * @param isFound true if the target resource of the REST call was found. For example, if a delete
   *     or update request succeeded.
   * @return the value to be returned from the REST method call
   */
  public static String respondNoContentOrNotFound(Response response, boolean isFound) {
    if (isFound) {
      response.status(HttpStatus.NO_CONTENT);
    } else {
      response.status(HttpStatus.NOT_FOUND);
    }
    return "";
  }

  private static String toContentTypeFromFileExt(String filename) {
    String ext = FilenameUtils.getExtension(filename).toLowerCase();
    switch (ext) {
      case "csv":
      case "txt":
      case "json":
        return MediaType.PLAIN_TEXT_UTF_8.toString();
      case "pdf":
        return MediaType.PDF.toString();
      case "zip":
        return MediaType.ZIP.toString();
      default:
        return MediaType.OCTET_STREAM.toString();
    }
  }

  /**
   * Use as a response to download request where the file data is a byte array.
   *
   * <p>This method should be called and returned as the last line in the REST method handler.
   *
   * @param response the response Object to update
   * @param body the file data - set to null to return a {@link HttpStatus#NOT_FOUND} status
   * @param filename the name of the file
   * @return the file data passed to this method (unmodified) to be returned from the REST method
   *     call
   */
  public static byte[] respondFileOrNotFound(Response response, byte[] body, String filename) {
    response
        .raw()
        .setHeader("Content-Disposition", String.format("attachment;filename=%s", filename));

    response.type(toContentTypeFromFileExt(filename));
    response.status(HttpStatus.OK);
    if (body == null) {
      response.status(HttpStatus.NOT_FOUND);
    } else {
      response.status(HttpStatus.OK);
    }
    return body;
  }

  /**
   * Use when the REST call expects a payload in the response
   *
   * <p>This method should be called and returned as the last line in the REST method handler.
   *
   * @param response the response Object to update
   * @param body the Java Object to be converted to JSON and returned to the client - set to null if
   *     the Object to return was not found
   * @return the value to be returned from the REST method call
   */
  public static String respondObjectOrNotFound(Response response, Object body) {
    String retValue;
    if (body == null) {
      response.status(HttpStatus.NOT_FOUND);
      retValue = "";
    } else {
      response.status(HttpStatus.OK);
      retValue = toJson(body, response);
    }
    return retValue;
  }

  /**
   * Creates a {@link DataRefreshEvent} to be broadcast to all clients except the client that is
   * handling the REST call.
   *
   * @param request the request for the REST method
   * @param type the type of refresh
   * @return a refresh event containing information about the REST method that is sending the event.
   */
  public static DataRefreshEvent newRefreshEvent(Request request, RefreshType type) {
    DataRefreshEvent dre = new DataRefreshEvent();
    dre.setRefreshType(type);
    dre.setSessionId(SecurityServiceImpl.getSessionId(request));
    dre.setBroadcast(Broadcast.EXCLUSIVE);
    dre.setLevel(Level.INFO);
    dre.setText(
        String.format(
            "Data refresh. Type=%s, Reason: %s %s",
            type.toString().toLowerCase(),
            request.requestMethod().toLowerCase(),
            request.pathInfo()));
    return dre;
  }

  /**
   * Creates an error {@link NotificationTextMessage} to be broadcast to all clients except the
   * client that is handling the REST call.
   *
   * @param request the request for the REST method
   * @param message the message to broadcast
   * @return a notification event containing information about the REST method that is sending the
   *     event.
   */
  public static NotificationTextMessage newErrorMessageEvent(Request request, String message) {
    NotificationTextMessage ntm = new NotificationTextMessage();
    ntm.setSessionId(SecurityServiceImpl.getSessionId(request));
    ntm.setBroadcast(Broadcast.EXCLUSIVE);
    ntm.setLevel(Level.ERROR);
    ntm.setText(
        String.format(
            "Error: Message=%s, Reason: %s %s",
            message, request.requestMethod().toLowerCase(), request.pathInfo()));
    return ntm;
  }

  /**
   * Use when the REST call creates an object on the server and can return an identifier that the
   * client can use to obtain the created object.
   *
   * <p>The Location header in the response will be updated to contain the request path and <code>
   * pkId</code>.
   *
   * <p>This method should be used by all applicable REST calls since it complies with REST
   * standards/conventions. However, using this method does not mean that a corresponding REST call
   * currently exists to retrieve the created object. The client/server code required to do so
   * should only be created if that specific functionality is needed.
   *
   * <p>This method should be called and returned as the last line in the REST method handler.
   *
   * @param request the request Object
   * @param response the response Object to update
   * @param pkId the primary key of the created object that can be used by the client to retrieve
   *     the object.
   * @return the value to be returned from the REST method call
   */
  public static String respondCreated(Request request, Response response, Object pkId) {
    response.header("Location", request.pathInfo() + "/" + pkId);
    response.status(HttpStatus.CREATED);
    return "";
  }

  /**
   * Get the value of a String parameter from the request.
   *
   * <p>A {@link BadRequestException} will be thrown if the request does not contain the param.
   *
   * @param request the request
   * @param param the name of the parameter
   * @return the parameter value
   */
  public static String getParam(Request request, String param) {
    return new ParamConverter(request, param, String.class).get();
  }

  /**
   * Get the value of a String parameter from the request or use a default if it does not exist.
   *
   * @param request the request
   * @param param the name of the parameter
   * @param defaultValue value to use if the param is not found in the request
   * @return the parameter value
   */
  public static String getParam(Request request, String param, String defaultValue) {
    return new ParamConverter(request, param, String.class, defaultValue).get();
  }

  /**
   * Get the value of a Integer parameter from the request.
   *
   * <p>A {@link BadRequestException} will be thrown if the request does not contain the param or if
   * it cannot be converted into an Integer.
   *
   * @param request the request
   * @param param the name of the parameter
   * @return the parameter value
   */
  public static Integer getParamInt(Request request, String param) {
    return new ParamConverter(request, param, Integer.class).get();
  }

  /**
   * Get the value of a Integer parameter from the request or use a default if it does not exist.
   *
   * <p>A {@link BadRequestException} will be thrown if the param value exists but or cannot be
   * converted into an Integer.
   *
   * @param request the request
   * @param param the name of the parameter
   * @param defaultValue value to use if the param is not found in the request
   * @return the parameter value
   */
  public static Integer getParamInt(Request request, String param, Integer defaultValue) {
    return new ParamConverter(request, param, Integer.class, defaultValue).get();
  }

  /**
   * Get the value of a Long parameter from the request.
   *
   * <p>A {@link BadRequestException} will be thrown if the request does not contain the param or if
   * it cannot be converted into an Long.
   *
   * @param request the request
   * @param param the name of the parameter
   * @return the parameter value
   */
  public static Long getParamLong(Request request, String param) {
    return new ParamConverter(request, param, Long.class).get();
  }

  /**
   * Get the value of a Long parameter from the request or use a default if it does not exist.
   *
   * <p>A {@link BadRequestException} will be thrown if the param value exists but or cannot be
   * converted into an Long.
   *
   * @param request the request
   * @param param the name of the parameter
   * @param defaultValue value to use if the param is not found in the request
   * @return the parameter value
   */
  public static Long getParamLong(Request request, String param, Long defaultValue) {
    return new ParamConverter(request, param, Long.class, defaultValue).get();
  }

  /**
   * Get the value of a Boolean parameter from the request.
   *
   * <p>A {@link BadRequestException} will be thrown if the request does not contain the param or if
   * it cannot be converted into an Boolean.
   *
   * @param request the request
   * @param param the name of the parameter
   * @return the parameter value
   */
  public static Boolean getParamBool(Request request, String param) {
    return new ParamConverter(request, param, Boolean.class).get();
  }

  /**
   * Get the value of a Boolean parameter from the request or use a default if it does not exist.
   *
   * <p>A {@link BadRequestException} will be thrown if the param value exists but or cannot be
   * converted into an Boolean.
   *
   * @param request the request
   * @param param the name of the parameter
   * @param defaultValue value to use if the param is not found in the request
   * @return the parameter value
   */
  public static Boolean getParamBool(Request request, String param, Boolean defaultValue) {
    return new ParamConverter(request, param, Boolean.class, defaultValue).get();
  }

  /**
   * Get the value of a Instant parameter from the request.
   *
   * <p>A {@link BadRequestException} will be thrown if the request does not contain the param or if
   * it cannot be converted into an Instant.
   *
   * @param request the request
   * @param param the name of the parameter
   * @return the parameter value
   */
  public static Instant getParamInstant(Request request, String param) {
    return new ParamConverter(request, param, Instant.class).get();
  }

  /** ParamConverter. */
  private static final class ParamConverter {
    private Request request;
    private String param;
    private Class type;
    private boolean isRequired;
    private Object defaultValue;

    ParamConverter(Request request, String param, Class type) {
      this.request = notNull(request, "request cannot be null");
      this.param = notEmpty(param, "param cannot be empty");
      this.type = notNull(type, "type cannot be null");
    }

    ParamConverter(Request request, String param, Class type, Object defaultValue) {
      this.request = notNull(request, "request cannot be null");
      this.param = notEmpty(param, "param cannot be empty");
      this.type = notNull(type, "type cannot be null");
      this.defaultValue = defaultValue;
      isRequired = false;
    }

    @SuppressWarnings("unchecked")
    public <T> T get() {
      String value = request.params(param);

      if (value == null) {
        if (isRequired) {
          throw new BadRequestException(request, param, "Required param not found");
        }

        return (T) defaultValue;
      }

      if (type.equals(String.class)) {
        return (T) value;
      }

      if (type.equals(Integer.class)) {
        return (T) toInt(value);
      }

      if (type.equals(Long.class)) {
        return (T) toLong(value);
      }

      if (type.equals(Boolean.class)) {
        return (T) toBool(value);
      }

      if (type.equals(Instant.class)) {
        return (T) toInstant(value);
      }

      throw new UnsupportedOperationException("Class type is not supported: " + type.getName());
    }

    private Integer toInt(String value) {
      try {
        return Integer.parseInt(value);
      } catch (NumberFormatException e) {
        throw new BadRequestException(
            request, param, formatErrMsg(value, "cannot be converted into an integer"), e);
      }
    }

    private Long toLong(String value) {
      try {
        return Long.parseLong(value);
      } catch (NumberFormatException e) {
        throw new BadRequestException(
            request, param, formatErrMsg(value, "cannot be converted into a long"), e);
      }
    }

    private Boolean toBool(String value) {
      switch (value) {
        case "true":
          return Boolean.TRUE;
        case "false":
          return Boolean.FALSE;
        default:
          throw new BadRequestException(
              request, param, formatErrMsg(value, "must be either 'true' or 'false'"));
      }
    }

    private Instant toInstant(String value) {
      try {
        return Instant.parse(value);
      } catch (DateTimeParseException e) {
        throw new BadRequestException(
            request, param, formatErrMsg(value, "cannot be converted into an Instant object"), e);
      }
    }

    private String formatErrMsg(String value, String msg) {
      return String.format(
          "Path=%s '%s' %s : RestParamName=%s", request.pathInfo(), value, msg, param);
    }
  }
}
