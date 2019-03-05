package com.sri.ai.praisewm;

import ch.qos.logback.classic.util.ContextInitializer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** The starting point for the application. */
public class ApplicationBootstrap {
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
