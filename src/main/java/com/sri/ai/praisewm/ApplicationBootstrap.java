package com.sri.ai.praisewm;

import ch.qos.logback.classic.util.ContextInitializer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** The starting point for the application. */
public class ApplicationBootstrap {
  /**
   * Starts the web server.
   *
   * <p>If the system property <code>logback.configurationFile</code> is already set, it will be
   * used to find the logback configuration file. If it is not set and the file <code>logback.xml
   * </code> exists in the current working directory, it will get set as the logback configuration
   * file.
   *
   * <p>Since logback will determine its configuration by reading the system property <code>
   * logback.configurationFile</code> when <code>LoggerFactory.getLogger()</code> is first called,
   * all other initialization starts from {@link com.sri.ai.praisewm.Application}.
   *
   * @param args unused
   */
  public static void main(String[] args) {
    initLogback();
    new Application();
  }

  private static void initLogback() {
    // Allow user to override by defining property on command line
    if (System.getProperty(ContextInitializer.CONFIG_FILE_PROPERTY) == null) {
      Path file = Paths.get(".", "logback.xml").toAbsolutePath();
      if (Files.exists(file)) {
        // must be set before the first call to LoggerFactory.getLogger();
        System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, file.toString());
      }
    }
  }
}
