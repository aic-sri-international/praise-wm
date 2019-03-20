package com.sri.ai.praisewm.service.praise;

import com.google.common.eventbus.EventBus;
import com.google.common.io.Resources;
import com.sri.ai.praisewm.service.dto.SegmentedModelDto;
import com.sri.ai.praisewm.util.FilesUtil;
import com.sri.ai.praisewm.util.JsonConverter;
import com.sri.ai.praisewm.util.PropertiesWrapper;
import com.sri.ai.praisewm.util.ResourceUtil;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SegmentedModelLoader {
  private static final Logger LOG = LoggerFactory.getLogger(SegmentedModelLoader.class);
  private static final String EXAMPLES_RESOURCE_DIR = "model_examples";
  private static final Predicate<Object> jsonFilter = o -> o.toString().endsWith(".json");

  private PropertiesWrapper propertiesWrapper;
  private ModelWatcher modelWatcher;
  private Map<String, SegmentedModelDto> segmentedModelMap = Collections.emptyMap();
  private Path segmentedModelDir;
  private boolean refresh;

  public SegmentedModelLoader(PropertiesWrapper propertiesWrapper, EventBus eventBus) {
    this.propertiesWrapper = propertiesWrapper;
    initSegmentedModelFolder();
    copyExamplesIntoSegmentedModelsDir();
    loadSegmentedModels();
    modelWatcher =
        new ModelWatcher(
            eventBus,
            segmentedModelDir,
            jsonFilter::test,
            () -> refresh = true,
            filepath -> {
              SegmentedModelDto smd = loadSegmentedModelFileDto(filepath);
              return smd == null ? null : smd.getName();
            });

    LOG.info(
        "Loaded {} model file{} from {}",
        segmentedModelMap.size(),
        segmentedModelMap.size() == 1 ? "" : "s",
        segmentedModelDir);
  }

  private static void loadSegmentedModelFileDto(
      Path filePath, Map<String, SegmentedModelDto> segmentedModelMap) {

    SegmentedModelDto modelDto = loadSegmentedModelFileDto(filePath);
    if (modelDto != null) {
      // The model's name needs to be unique, if it is not unique, make it unique:
      int uniqueId = 0;
      String origName = modelDto.getName();
      while (segmentedModelMap.containsKey(modelDto.getName())) {
        ++uniqueId;
        modelDto.setName(String.format("%s (%d)", origName, uniqueId));
      }

      segmentedModelMap.put(modelDto.getName(), modelDto);
    }
  }

  private static SegmentedModelDto loadSegmentedModelFileDto(Path filePath) {
    StringBuilder data = new StringBuilder();
    try (Stream<String> lines = Files.lines(filePath); ) {
      lines.forEach(line -> data.append(line).append("\n"));
    } catch (Exception e) {
      LOG.error("Error reading model file contents: File={}, file skipped", filePath, e);
      return null;
    }

    SegmentedModelDto modelDto;
    try {
      modelDto = JsonConverter.from(data.toString(), SegmentedModelDto.class);
    } catch (Exception e) {
      LOG.error(
          "Error converting model file from text to JSON contents: File={}, file skipped",
          filePath,
          e);
      return null;
    }

    if (StringUtils.trimToNull(modelDto.getName()) == null) {
      LOG.error(
          "Error converted model file does not contain a non-empty name field: File={}, file skipped",
          filePath);
      return null;
    }

    return modelDto;
  }

  private static List<String> getResourceFiles(String regex) {
    Collection<String> files = ResourceUtil.getResources(Pattern.compile(regex));
    LOG.info("Files matched: {}", files);
    List<String> list = new ArrayList<>();
    files.forEach(
        file -> {
          int ix = file.lastIndexOf('/');
          if (ix == -1) {
            list.add(file);
          } else {
            list.add(file.substring(ix + 1));
          }
        });
    return list;
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

  public void stop() {
    if (modelWatcher != null) {
      modelWatcher.stop();
    }
  }

  private void initSegmentedModelFolder() {
    String segmentedModelFolder = propertiesWrapper.asString("server.segmentedModelFolder");
    segmentedModelDir = Paths.get("." + segmentedModelFolder).toAbsolutePath().normalize();
    FilesUtil.createDirectories(segmentedModelDir, "segmented models");
  }

  private void copyExamplesIntoSegmentedModelsDir() {
    // Pattern to match files contained in the EXAMPLES_RESOURCE_DIR in the classpath
    // for development and production.
    final String CLASSPATH_MODEL_EXAMPLE_FILES_REGEX =
        "(^|.*/)" + EXAMPLES_RESOURCE_DIR + "/[^/]+\\.json";
    List<String> files = getResourceFiles(CLASSPATH_MODEL_EXAMPLE_FILES_REGEX);
    List<String> filesCopied = new ArrayList<>();
    List<String> filesAlreadyExisted = new ArrayList<>();

    files.stream()
        .filter(jsonFilter)
        .forEach(
            filename -> {
              final String fileResourcePath = EXAMPLES_RESOURCE_DIR + '/' + filename;
              final Path fileDiskPath = segmentedModelDir.resolve(filename);

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
                  LOG.error(
                      "Error processing model resource files in {}", EXAMPLES_RESOURCE_DIR, e);
                }
              }
            });
    LOG.info("Example resource model files copied to {}: {}", segmentedModelDir, filesCopied);
    LOG.info(
        "Example resource model files that already existed in and not copied to {}: {}",
        segmentedModelDir,
        filesAlreadyExisted);
  }

  public List<SegmentedModelDto> getSegmentedModels() {
    if (refresh) {
      loadSegmentedModels();
      refresh = false;
    }

    List<SegmentedModelDto> list = new ArrayList<>(segmentedModelMap.values());
    list.sort((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
    return list;
  }

  private void loadSegmentedModels() {
    segmentedModelMap = new HashMap<>();

    try {
      Files.list(segmentedModelDir)
          .filter(jsonFilter)
          .forEach(
              filePath -> {
                loadSegmentedModelFileDto(filePath, segmentedModelMap);
              });
    } catch (IOException e) {
      throw new RuntimeException(
          String.format("Error accessing model files in directory: %s", segmentedModelDir), e);
    }
  }
}
