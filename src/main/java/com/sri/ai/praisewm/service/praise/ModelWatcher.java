package com.sri.ai.praisewm.service.praise;

import com.google.common.eventbus.EventBus;
import com.sri.ai.praisewm.event.notification.NotificationEvent.Broadcast;
import com.sri.ai.praisewm.event.notification.NotificationEvent.Level;
import com.sri.ai.praisewm.event.notification.NotificationTextMessage;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelWatcher {
  private static final Logger LOG = LoggerFactory.getLogger(ModelWatcher.class);
  private EventBus eventBus;

  private WatchService watchService;
  private Path watchDir;

  public ModelWatcher(EventBus eventBus, Path watchDir) {
    this.eventBus = eventBus;
    this.watchDir = watchDir;

    try {
      watchService = FileSystems.getDefault().newWatchService();
    } catch (Exception e) {
      LOG.error("Cannot create WatchService", e);
      return;
    }

    try {
      watchDir.register(
          watchService,
          StandardWatchEventKinds.ENTRY_CREATE,
          StandardWatchEventKinds.ENTRY_DELETE,
          StandardWatchEventKinds.ENTRY_MODIFY);
    } catch (Exception e) {
      LOG.error("Error attempting to register WatchService on {}", watchDir, e);
      return;
    }

    Thread thread =
        new Thread(
            () -> {
              try {
                LOG.info("WatchService thread for {} started", watchDir);
                loop();
              } catch (Exception e) {
                LOG.info("WatchService thread for {} terminated", watchDir, e.toString());
              }
            });
    thread.setName("ModelWatcher");
    thread.start();
  }

  private void loop() throws InterruptedException {
    WatchKey key;
    while ((key = watchService.take()) != null) {
      for (WatchEvent<?> event : key.pollEvents()) {
        NotificationTextMessage ntm = new NotificationTextMessage();
        ntm.setLevel(Level.INFO).setBroadcast(Broadcast.EXCLUSIVE);
        String file = event.context().toString();

        if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
          ntm.setText("New model file available: " + file);
        } else if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
          ntm.setText("Modified model file available: " + file);
        } else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
          ntm.setText("Removed model file: " + file);
        }
        if (ntm.getText() != null) {
          eventBus.post(ntm);
        }
      }
      key.reset();
    }
  }

  void stop() {
    if (watchService != null) {
      try {
        watchService.close();
      } catch (IOException e) {
        LOG.info("Error attempting to close WatchService for {}", watchDir, e);
      }
    }
  }
}
