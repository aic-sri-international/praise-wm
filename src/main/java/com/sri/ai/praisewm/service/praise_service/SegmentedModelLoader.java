package com.sri.ai.praisewm.service.praise_service;

import static com.sri.ai.praisewm.util.FilesUtil.loadFiles;
import static com.sri.ai.praisewm.util.FilesUtil.readFileFully;

import com.google.common.eventbus.EventBus;
import com.sri.ai.praisewm.event.notification.NotificationEvent;
import com.sri.ai.praisewm.event.notification.NotificationEvent.Broadcast;
import com.sri.ai.praisewm.event.notification.NotificationEvent.Level;
import com.sri.ai.praisewm.event.notification.NotificationTextMessage;
import com.sri.ai.praisewm.service.dto.SegmentedModelDto;
import com.sri.ai.praisewm.util.DirectoryWatcher;
import com.sri.ai.praisewm.util.DirectoryWatcher.DirChangeEntry;
import com.sri.ai.praisewm.util.JsonConverter;
import com.sri.ai.praisewm.util.PropertiesWrapper;
import com.sri.ai.praisewm.util.ResourceFileDirectoryCopy;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The SegmentedModelLoader loads and caches the models, making them accessible to clients.
 *
 * <p>Model files are loaded at startup from the {@link #EXAMPLES_RESOURCE_DIR} resources path and
 * copied into the directory referenced by the <code>application.properties</code> property {@link
 * #MODEL_FILES_DIR_PROPERTY} if they do not already exist within that directory.
 *
 * <p>Files within the model directory are cached in memory as instances of {@link
 * SegmentedModelDto} and kept current by using the services of the {@link DirectoryWatcher}.
 */
public class SegmentedModelLoader {
  public static final String EXAMPLES_RESOURCE_DIR = "model_examples";
  public static final String MODEL_FILES_DIR_PROPERTY = "server.modelsFolder";
  private static final Logger LOG = LoggerFactory.getLogger(SegmentedModelLoader.class);
  private static final Predicate<Path> jsonFilter = o -> o.toString().endsWith(".json");
  private final EventBus eventBus;
  private final Path segmentedModelDir;
  private DirectoryWatcher directoryWatcher;
  private Map<String, SegmentedModelDto> segmentedModelMap;
  private boolean refresh;

  /**
   * The Segmented Model Loader
   *
   * @param propertiesWrapper config properties wrapper
   * @param eventBus event bus
   */
  public SegmentedModelLoader(PropertiesWrapper propertiesWrapper, EventBus eventBus) {
    this.eventBus = eventBus;
    this.segmentedModelDir = propertiesWrapper.getPathToRelativeDirectory(MODEL_FILES_DIR_PROPERTY);
    ResourceFileDirectoryCopy resourceFileDirectoryCopy =
        new ResourceFileDirectoryCopy(
            EXAMPLES_RESOURCE_DIR, e -> e.endsWith(".json"), segmentedModelDir);
    resourceFileDirectoryCopy.apply();
    segmentedModelMap = loadSegmentedModels(segmentedModelDir);
    directoryWatcher =
        new DirectoryWatcher(segmentedModelDir, true, this::processedDirectoryChange);

    LOG.info(
        "Loaded {} model file{} from {}",
        segmentedModelMap.size(),
        segmentedModelMap.size() == 1 ? "" : "s",
        segmentedModelDir);
  }

  private static SegmentedModelDto loadSegmentedModelFileDto(Path filePath) {
    try {
      String fileContents = readFileFully(filePath, "\n");
      return toSegmentedModelDto(filePath, fileContents);
    } catch (Exception e) {
      LOG.error("Error reading model file contents: File={}, file skipped", filePath, e);
      return null;
    }
  }

  private static SegmentedModelDto toSegmentedModelDto(Path filePath, String fileContents) {
    SegmentedModelDto modelDto;
    try {
      modelDto = JsonConverter.from(fileContents, SegmentedModelDto.class);
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

  private static Map<String, SegmentedModelDto> loadSegmentedModels(Path modelDir) {
    Map<Path, String> fileContentsMap = loadFiles(modelDir, jsonFilter, "\n");
    Map<String, SegmentedModelDto> modelMap = new HashMap<>();
    fileContentsMap.forEach(
        (k, v) -> {
          SegmentedModelDto modelDto = toSegmentedModelDto(k, v);
          if (modelDto != null) {
            // The model's name needs to be unique, if it is not unique, make it unique:
            int uniqueId = 0;
            String origName = modelDto.getName();
            while (modelMap.containsKey(modelDto.getName())) {
              ++uniqueId;
              modelDto.setName(String.format("%s (%d)", origName, uniqueId));
            }

            modelMap.put(modelDto.getName(), modelDto);
          }
        });
    return modelMap;
  }

  private void processedDirectoryChange(DirChangeEntry dirChangeEntry) {
    this.refresh = true;

    String msg;

    boolean appendModelName = false;

    switch (dirChangeEntry.getType()) {
      case CREATE:
        msg = "New model available: ";
        appendModelName = true;
        break;
      case DELETE:
        msg = "Deleted model file: " + dirChangeEntry.getFile();
        break;
      case MODIFY:
        msg = "Modified model available: ";
        appendModelName = true;
        break;
      case OVERFLOW:
        msg = "Models added, deleted, and/or modified";
        break;
      default:
        LOG.warn("Unknown event type: {}", dirChangeEntry.getType());
        return;
    }

    if (appendModelName) {
      Path filePath = segmentedModelDir.resolve(dirChangeEntry.getFile());
      SegmentedModelDto smd = loadSegmentedModelFileDto(filePath);
      if (smd == null) {
        return;
      }

      msg += smd.getName();
    }
    NotificationEvent notificationEvent =
        new NotificationTextMessage()
            .setText(msg)
            .setLevel(Level.INFO)
            .setBroadcast(Broadcast.EXCLUSIVE);
    eventBus.post(notificationEvent);
  }

  public void stop() {
    if (directoryWatcher != null) {
      directoryWatcher.stop();
    }
  }

  List<SegmentedModelDto> getSegmentedModels() {
    if (refresh) {
      segmentedModelMap = loadSegmentedModels(segmentedModelDir);
      refresh = false;
    }

    List<SegmentedModelDto> list = new ArrayList<>(segmentedModelMap.values());
    list.sort((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
    return list;
  }
}
