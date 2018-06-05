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

/** SparkUtil. */
public final class SparkUtil {
  private SparkUtil() {}

  public static String toJson(Object o, Response response) {
    response.type("application/json");
    return JsonConverter.to(o);
  }

  public static <T> T fromJson(Request request, Class<T> clazz) {
    return JsonConverter.from(request.body(), clazz);
  }

  public static String respondNoContent(Response response) {
    return respondNoContentOrNotFound(response, true);
  }

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

  public static String respondCreated(Request request, Response response, Object pkId) {
    response.header("Location", request.pathInfo() + "/" + pkId);
    response.status(HttpStatus.CREATED);
    return "";
  }

  public static String getParam(Request request, String param) {
    return new ParamConverter(request, param, String.class).get();
  }

  public static String getParam(Request request, String param, String defaultValue) {
    return new ParamConverter(request, param, String.class, defaultValue).get();
  }

  public static Integer getParamInt(Request request, String param) {
    return new ParamConverter(request, param, Integer.class).get();
  }

  public static Integer getParamInt(Request request, String param, Integer defaultValue) {
    return new ParamConverter(request, param, Integer.class, defaultValue).get();
  }

  public static Long getParamLong(Request request, String param) {
    return new ParamConverter(request, param, Long.class).get();
  }

  public static Long getParamLong(Request request, String param, Long defaultValue) {
    return new ParamConverter(request, param, Long.class, defaultValue).get();
  }

  public static Boolean getParamBool(Request request, String param) {
    return new ParamConverter(request, param, Boolean.class).get();
  }

  public static Boolean getParamBool(Request request, String param, Boolean defaultValue) {
    return new ParamConverter(request, param, Boolean.class, defaultValue).get();
  }

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
