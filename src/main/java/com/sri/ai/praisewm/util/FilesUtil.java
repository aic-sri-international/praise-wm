package com.sri.ai.praisewm.util;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FilesUtil {
  private static final Logger LOG = LoggerFactory.getLogger(FilesUtil.class);
  private static final Set<String> SUPPORTED_IMAGE_TYPES = ImmutableSet.of("png", "jpeg", "jpg");

  /**
   * Wraps {@link Files#readAllLines} to throw a {@link RuntimeException}
   *
   * @param filepath the file's path
   * @return file's contents as a list of text lines
   */
  public static List<String> readFile(Path filepath) {
    try {
      return Files.readAllLines(filepath);
    } catch (Exception e) {
      throw new RuntimeException(String.format("Cannot read file '%s'", filepath), e);
    }
  }

  /**
   * Wraps {@link Files#write} to throw a {@link RuntimeException}
   *
   * @param lines lines of text for the file
   * @param targetFilepath path to file to be created or recreated
   */
  public static void writeFile(List<String> lines, Path targetFilepath) {
    try {
      Files.write(targetFilepath, lines);
    } catch (Exception e) {
      throw new RuntimeException(String.format("Cannot write to file '%s'", targetFilepath), e);
    }
  }

  public static void createDirectories(Path dirPath, String description) {
    String msg = String.format("%s directory '%s'", description, dirPath);
    if (Files.notExists(dirPath)) {
      try {
        Files.createDirectories(dirPath);
        LOG.info("Created " + msg);
      } catch (Exception ex) {
        throw new RuntimeException("Cannot create " + msg, ex);
      }
    }
  }

  public static String readFileFully(Path filePath, String lineDelimiter) throws IOException {
    StringBuilder data = new StringBuilder();
    try (Stream<String> lines = Files.lines(filePath); ) {
      lines.forEach(line -> data.append(line).append(lineDelimiter));
    }
    return data.toString();
  }

  public static Map<Path, String> loadFiles(
      Path directory, Predicate<Path> filePathFilter, String lineDelimiter) {
    Map<Path, String> fileContentsMap = new HashMap<>();

    try {
      Files.list(directory)
          .filter(filePathFilter)
          .forEach(
              filePath -> {
                try {
                  String contents = readFileFully(filePath, lineDelimiter);
                  fileContentsMap.put(filePath, contents);
                } catch (Exception e) {
                  LOG.error("Error reading file contents: File={}, file skipped", filePath, e);
                }
              });
    } catch (IOException e) {
      throw new RuntimeException(
          String.format("Error accessing files in directory: %s", directory), e);
    }

    return fileContentsMap;
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
