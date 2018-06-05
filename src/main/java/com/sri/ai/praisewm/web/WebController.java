package com.sri.ai.praisewm.web;

import com.sri.ai.praisewm.service.SecurityService;
import com.sri.ai.praisewm.util.JsonConverter;
import com.sri.ai.praisewm.util.PropertiesWrapper;
import com.sri.ai.praisewm.web.error.AuthenticationException;
import com.sri.ai.praisewm.web.error.AuthorizationException;
import com.sri.ai.praisewm.web.error.LoginException;
import com.sri.ai.praisewm.web.error.PathNotDefinedException;
import com.sri.ai.praisewm.web.error.ProcessingException;
import com.sri.ai.praisewm.web.rest.ErrorResponseMessage;
import com.sri.ai.praisewm.web.rest.filter.CorsFilter;
import com.sri.ai.praisewm.web.rest.util.HttpStatus;
import com.sri.ai.praisewm.web.websocket.WebSocketConstants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Supplier;
import org.jooq.exception.DataAccessException;
import org.jooq.exception.SQLStateClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Service;

/**
 * WebController establishes the web resources, starts the web service, sets initial filters and
 * routes, and stops the web service.
 */
public class WebController {
  private static final Logger LOG = LoggerFactory.getLogger(WebController.class);
  private static final String[] DEFINED_PATHS = {
    "/api/", "/admin/", WebSocketConstants.ENDPOINT_PREFIX
  };
  // Header sent to client to signify that only the message fields should be display,
  // and not any HTTP status info, or, optional techMessage
  private static final String USER_MESSAGE_EXCEPTION = "USER_MESSAGE_EXCEPTION";
  private final PropertiesWrapper pw;
  private final Supplier<SecurityService> securityServiceSupplier;
  private final int portIncrement;
  private boolean sparkIsInitialized;
  private String staticContentFolder;
  private Service sparkService;
  private CorsFilter corsFilter;

  public WebController(
      PropertiesWrapper propertiesWrapper,
      Supplier<SecurityService> securityServiceSupplier,
      int portIncrement) {
    this.pw = propertiesWrapper;
    this.securityServiceSupplier = securityServiceSupplier;
    this.portIncrement = portIncrement;
    sparkService = spark.Service.ignite();
    corsFilter = new CorsFilter(sparkService);
  }

  private static Response setMessage(ErrorResponseMessage message, Response res) {
    res.type("application/json");
    res.body(JsonConverter.to(message));
    return res;
  }

  public WebController init() {
    ArrayList<Exception> exceptions = new ArrayList<>();

    initInternal();

    // Capture initialization exception, if any
    sparkService.initExceptionHandler(exceptions::add);

    sparkService.init();

    sparkService.awaitInitialization();

    if (!exceptions.isEmpty()) {
      throw new RuntimeException(exceptions.get(0));
    }

    sparkIsInitialized = true;

    setFilters();

    return this;
  }

  public Service getSparkService() {
    return sparkService;
  }

  private void initInternal() {
    // root is 'src/main/resources', so put files in 'src/main/resources/public'
    // Note that the public directory name is not included in the URL.
    // A file /public/css/style.css is made available as http://{host}:{port}/css/style.css

    staticContentFolder = pw.asString("server.staticFolder");
    sparkService.staticFiles.location(staticContentFolder);

    sparkService.ipAddress(pw.asString("server.host"));

    sparkService.port(portIncrement + pw.asInt("server.port"));

    pw.ifExists(
        "server.staticFiles.expireTime",
        Long.class,
        (expireTime) -> sparkService.staticFiles.expireTime(expireTime));

    sparkService.threadPool(
        pw.asInt("server.threads.max"),
        pw.asInt("server.threads.min"),
        pw.asInt("server.threads.idleTimeoutInMillis"));

    pw.ifExists(
        "server.ssl.keystoreFile",
        String.class,
        (keystoreFile) ->
            sparkService.secure(
                keystoreFile,
                pw.asString("server.ssl.keystorePassword"),
                null,
                null,
                pw.asBool("server.ssl.needsClientCert")));

    sparkService.internalServerError(
        (req, res) -> {
          String msg =
              String.format("Internal Server Error while processing route: %s", req.pathInfo());
          setMessage(new ErrorResponseMessage(msg), res).status(HttpStatus.INTERNAL_SERVER_ERROR);
          corsFilter.apply(res);
          logError(req, res, new IllegalArgumentException(msg));
          return res.body();
        });

    // This probably won't ever get called, since we check in the before filter for
    // defined routes, but, keep it here for now. Things might change in a subsequent Spark
    // release, and this could help track down related issues.
    sparkService.notFound(
        (req, res) -> {
          String msg = String.format("Not defined: %s %s", req.requestMethod(), req.pathInfo());
          setMessage(new ErrorResponseMessage(msg), res).status(HttpStatus.NOT_FOUND);
          corsFilter.apply(res);
          logError(req, res, new IllegalArgumentException(msg));
          return res.body();
        });

    sparkService.exception(
        PathNotDefinedException.class,
        (exception, req, res) -> {
          String msg = String.format("Not defined: %s %s", req.requestMethod(), req.pathInfo());
          setMessage(new ErrorResponseMessage(msg), res).status(HttpStatus.NOT_FOUND);
          corsFilter.apply(res);
          logError(req, res, exception);
        });

    sparkService.exception(
        LoginException.class,
        (exception, req, res) -> {
          // For some reason, if we don't return something in the body the client will get a
          // CORS error.
          setMessage(new ErrorResponseMessage("Invalid login"), res)
              .status(HttpStatus.UNAUTHORIZED);
          corsFilter.apply(res);
          logError(req, res, exception);
        });

    sparkService.exception(
        AuthenticationException.class,
        (exception, req, res) -> {
          // For some reason, if we don't return something in the body the client will get a
          // CORS error.
          setMessage(new ErrorResponseMessage("Not logged in"), res)
              .status(HttpStatus.UNAUTHORIZED);
          corsFilter.apply(res);
          logError(req, res, exception);
        });

    sparkService.exception(
        AuthorizationException.class,
        (exception, req, res) -> {
          // For some reason, if we don't return something in the body the client will get a
          // CORS error.
          setMessage(new ErrorResponseMessage("Not authorized"), res)
              .status(HttpStatus.UNAUTHORIZED);
          corsFilter.apply(res);
          logError(req, res, exception);
        });

    sparkService.exception(
        DataAccessException.class,
        (exception, req, res) -> {
          String userMsg;
          String techMessage = null;

          if (exception.sqlStateClass() == SQLStateClass.C23_INTEGRITY_CONSTRAINT_VIOLATION) {
            userMsg = "Database Constraint Violation";
            techMessage = exception.getMessage();
            res.header(USER_MESSAGE_EXCEPTION, "true");
          } else {
            userMsg = exception.getMessage();
          }

          setMessage(new ErrorResponseMessage(userMsg).setTechMessage(techMessage), res)
              .status(HttpStatus.BAD_REQUEST);
          corsFilter.apply(res);
          logError(req, res, exception);
        });

    sparkService.exception(
        ProcessingException.class,
        (exception, req, res) -> {
          res.header(USER_MESSAGE_EXCEPTION, "true");
          ErrorResponseMessage erm =
              new ErrorResponseMessage(exception.getDisplayMessage())
                  .setTechMessage(exception.getTechMessage());
          setMessage(erm, res).status(HttpStatus.INTERNAL_SERVER_ERROR);
          corsFilter.apply(res);
          logError(req, res, exception);
        });
  }

  private void logError(Request req, Response res, Exception ex) {
    LOG.error(
        "HttpStatus={}, RemoteIp={}, Path={}, Message={}",
        res.status(),
        req.ip(),
        req.pathInfo(),
        ex.getMessage());
  }

  private void setFilters() {
    // Checking for the defined paths in this filter is used to filter-out requests that
    // would otherwise be passed to the security authentication service.
    sparkService.before(
        (req, res) -> {
          boolean isAllowed =
              Arrays.stream(DEFINED_PATHS).anyMatch((p) -> req.pathInfo().startsWith(p));
          if (isAllowed) {
            securityServiceSupplier.get().authenticateAndAuthorize(req);
          } else {
            throw new PathNotDefinedException();
          }
        });

    corsFilter.apply();
    sparkService.after((req, res) -> res.header("Content-Encoding", "gzip"));
  }

  public void stop() {
    if (sparkIsInitialized) {
      sparkService.stop();
    }
  }
}
