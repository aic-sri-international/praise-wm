package com.sri.ai.praisewm.service.praise_service;

import static com.sri.ai.praisewm.util.FilesUtil.loadFiles;

import com.google.common.eventbus.EventBus;
import com.sri.ai.praisewm.event.notification.NotificationEvent;
import com.sri.ai.praisewm.event.notification.NotificationEvent.Broadcast;
import com.sri.ai.praisewm.event.notification.NotificationEvent.Level;
import com.sri.ai.praisewm.event.notification.NotificationTextMessage;
import com.sri.ai.praisewm.service.dto.SegmentedModelDto;
import com.sri.ai.praisewm.util.DirectoryWatcher;
import com.sri.ai.praisewm.util.DirectoryWatcher.DirChangeEntry;
import com.sri.ai.praisewm.util.PropertiesWrapper;
import com.sri.ai.praisewm.util.ResourceFileDirectoryCopy;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Feature Collections Loader loads and caches GeoJson feature collections, making them
 * accessible to clients.
 *
 * <p>GeoJSON Feature Collections files are loaded at startup from the {@link
 * #FEATURE_COLLECTIONS_RESOURCE_DIR} resources path and copied into the directory referenced by the
 * <code>application.properties</code> property {@link #FEATURE_COLLECTIONS_DIR_PROPERTY} if they do
 * not already exist within that directory.
 *
 * <p>Files within the model directory are cached in memory as instances of {@link
 * SegmentedModelDto} and kept current by using the services of the {@link DirectoryWatcher}.
 */
public class FeatureCollectionsLoader {
  public static final String FEATURE_COLLECTIONS_RESOURCE_DIR = "feature_collections";
  public static final String FEATURE_COLLECTIONS_DIR_PROPERTY =
      "server.geojsonFeatureCollectionsFolder";
  private static final Logger LOG = LoggerFactory.getLogger(FeatureCollectionsLoader.class);
  private static final String FILE_EXT = ".json";
  private static final Predicate<Path> jsonFilter = o -> o.toString().endsWith(FILE_EXT);
  private final EventBus eventBus;
  private final Path featureCollectionsDir;
  private DirectoryWatcher directoryWatcher;
  private Map<String, String> nameToFeatureCollectionMap;
  private boolean refresh;

  /**
   * The GeoJSON Feature Collections Loader
   *
   * @param propertiesWrapper config properties wrapper
   * @param eventBus event bus
   */
  public FeatureCollectionsLoader(PropertiesWrapper propertiesWrapper, EventBus eventBus) {
    this.eventBus = eventBus;
    this.featureCollectionsDir =
        propertiesWrapper.getPathToRelativeDirectory(FEATURE_COLLECTIONS_DIR_PROPERTY);
    ResourceFileDirectoryCopy resourceFileDirectoryCopy =
        new ResourceFileDirectoryCopy(
            FEATURE_COLLECTIONS_RESOURCE_DIR, e -> e.endsWith(FILE_EXT), featureCollectionsDir);
    resourceFileDirectoryCopy.apply();
    nameToFeatureCollectionMap = loadFeatureCollections(featureCollectionsDir);
    directoryWatcher =
        new DirectoryWatcher(featureCollectionsDir, true, this::processedDirectoryChange);

    LOG.info(
        "Loaded {} feature collection file{} from {}",
        nameToFeatureCollectionMap.size(),
        nameToFeatureCollectionMap.size() == 1 ? "" : "s",
        featureCollectionsDir);
  }

  private static Map<String, String> loadFeatureCollections(Path modelDir) {
    Map<Path, String> fileContentsMap = loadFiles(modelDir, jsonFilter, "\n");
    Map<String, String> featureCollectionMap = new HashMap<>();
    fileContentsMap.forEach(
        (k, v) -> {
          String basename = k.getFileName().toString();
          basename = basename.substring(0, basename.length() - FILE_EXT.length());
          featureCollectionMap.put(basename, v);
        });
    return featureCollectionMap;
  }

  private void processedDirectoryChange(DirChangeEntry dirChangeEntry) {
    this.refresh = true;

    String msg;

    switch (dirChangeEntry.getType()) {
      case CREATE:
        msg = "New feature collection available: " + dirChangeEntry.getFile();
        break;
      case DELETE:
        msg = "Deleted feature collection file: " + dirChangeEntry.getFile();
        break;
      case MODIFY:
        msg = "Modified feature collection available: " + dirChangeEntry.getFile();
        break;
      case OVERFLOW:
        msg = "Models added, deleted, and/or modified";
        break;
      default:
        LOG.warn("Unknown event type: {}", dirChangeEntry.getType());
        return;
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

  public List<String> getFeatureCollectionNames() {
    if (refresh) {
      nameToFeatureCollectionMap = loadFeatureCollections(featureCollectionsDir);
      refresh = false;
    }

    return nameToFeatureCollectionMap.keySet().stream().sorted().collect(Collectors.toList());
  }

  public String getFeatureCollection(String collectionName) {
    if (refresh) {
      nameToFeatureCollectionMap = loadFeatureCollections(featureCollectionsDir);
      refresh = false;
    }

    String collection = nameToFeatureCollectionMap.get(collectionName);
    return collection == null ? "" : collection;
  }
}
