package com.sri.ai.praisewm.util;

import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FilesUtil {
  private static final Logger LOG = LoggerFactory.getLogger(FilesUtil.class);

  public static void createDirectories(Path dirPath, String description) {
    String msg = String.format("%s directory '%s'", description, dirPath);
    if (Files.notExists(dirPath)) {
      try {
        Files.createDirectories(dirPath);
        LOG.info("Created " + msg);
      } catch (Exception ex) {
        throw new RuntimeException("Cannot create " + msg, ex);
      }
    } else {
      LOG.info("Using " + msg);
    }
  }
}
