package com.sri.ai.praisewm.util;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Set;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FilesUtil {
  private static final Logger LOG = LoggerFactory.getLogger(FilesUtil.class);
  private static final Set<String> SUPPORTED_IMAGE_TYPES = ImmutableSet.of("png", "jpeg", "jpg");

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

  public static String imageFileToBase64DataImage(Path filepath) {
    if (filepath == null) {
      throw new IllegalArgumentException("filepath cannot be null");
    }

    String ext = FilenameUtils.getExtension(filepath.getFileName().toString().toLowerCase());
    if (!SUPPORTED_IMAGE_TYPES.contains(ext)) {
      throw new IllegalArgumentException(
          String.format(
              "%s must have one of the following extensions: %s",
              filepath.toString(), SUPPORTED_IMAGE_TYPES));
    }

    if (ext.equals("jpg")) {
      // https://www.w3.org/Graphics/JPEG/
      // Use the correct MIME type
      ext = "jpeg";
    }

    byte[] bytes;
    try {
      bytes = Files.readAllBytes(filepath);
    } catch (IOException e) {
      throw new RuntimeException("Cannot read file: " + filepath, e);
    }
    String header = String.format("data:image/%s;base64,", ext);
    return header + Base64.getEncoder().encodeToString(bytes);
  }
}
