package com.sri.ai.praisewm.util;

import static com.sri.ai.praisewm.util.ResourceUtil.getResourceNames;

import com.google.common.io.Resources;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceFileDirectoryCopy {
  private static final Logger LOG = LoggerFactory.getLogger(ResourceFileDirectoryCopy.class);
  private final String CLASSPATH_MODEL_EXAMPLE_FILES_REGEX;
  private String resourceDirName;
  private Predicate<String> fileFilter;
  private Path targetDir;

  public ResourceFileDirectoryCopy(
      String resourceDirName, Predicate<String> fileFilter, Path targetDir) {
    this.resourceDirName = resourceDirName;
    this.fileFilter = fileFilter;
    this.targetDir = targetDir;

    // Pattern to match files contained in the EXAMPLES_RESOURCE_DIR in the classpath
    // for development and production.
    CLASSPATH_MODEL_EXAMPLE_FILES_REGEX = "(^|.*/)" + resourceDirName + "/[^/]+";
  }

  private static List<String> getResourceContents(String resourceName) {
    List<String> lines = Collections.emptyList();
    URL url;
    try {
      url = Resources.getResource(resourceName);
    } catch (Exception e) {
      LOG.error("Cannot get resource for {}", resourceName, e);
      return lines;
    }

    try {
      lines = Resources.readLines(url, StandardCharsets.UTF_8);
    } catch (Exception e) {
      LOG.error("Cannot read contents of resource {}", url, e);
      return lines;
    }

    return lines;
  }

  public void apply() {
    List<String> files = getResourceNames(CLASSPATH_MODEL_EXAMPLE_FILES_REGEX);
    LOG.info("Files matched: {}", files);
    List<String> filesCopied = new ArrayList<>();
    List<String> filesAlreadyExisted = new ArrayList<>();

    files.stream()
        .filter(fileFilter)
        .forEach(
            filename -> {
              final String fileResourcePath = resourceDirName + '/' + filename;
              final Path fileDiskPath = targetDir.resolve(filename);

              if (Files.exists(fileDiskPath)) {
                filesAlreadyExisted.add(filename);
              } else {
                try {
                  List<String> arr = getResourceContents(fileResourcePath);
                  if (!arr.isEmpty()) {
                    byte[] bytes = String.join("\n", arr).getBytes();
                    Files.write(fileDiskPath, bytes);
                    filesCopied.add(filename);
                  }
                } catch (Exception e) {
                  LOG.error("Error processing resource files in {}", resourceDirName, e);
                }
              }
            });
    LOG.info("Resource files copied to {}: {}", targetDir, filesCopied);
    LOG.info(
        "Resource files that already existed in and not copied to {}: {}",
        targetDir,
        filesAlreadyExisted);
  }
}
