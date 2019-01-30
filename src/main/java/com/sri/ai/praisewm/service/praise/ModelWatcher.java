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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelWatcher {
  private static final Logger LOG = LoggerFactory.getLogger(ModelWatcher.class);
  private final List<QueueEntry> msgQueue = new ArrayList<>();
  private final ExecutorService msgQueueExecutor = Executors.newSingleThreadExecutor();
  private final Runnable msgQueueRunnableTask;
  private final Path watchDir;
  private WatchService watchService;

  public ModelWatcher(
      EventBus eventBus,
      Path watchDir,
      DirectoryChanged directoryChanged,
      ModelNameAccessor modelNameAccessor) {
    this.watchDir = watchDir;

    msgQueueRunnableTask =
        () -> {
          try {
            TimeUnit.MILLISECONDS.sleep(2000);

            List<QueueEntry> prod;
            List<QueueEntry> tmp;

            synchronized (msgQueue) {
              if (msgQueue.isEmpty()) {
                LOG.info("msgQueue is empty");
                return;
              }

              if (msgQueue.size() == 1) {
                prod = new ArrayList<>(msgQueue);
                tmp = Collections.emptyList();
              } else {
                tmp = new ArrayList<>(msgQueue);
                prod = new ArrayList<>();
              }
              msgQueue.clear();
            }

            LOG.info("msgQueue length: {}", Math.max(tmp.size(), prod.size()));
            if (prod.isEmpty()) {
              // remove duplicates and if a modify is preceded by a create, only keep the create.
              for (int i = 0; i < tmp.size(); ++i) {
                QueueEntry cur = tmp.get(i);
                QueueEntry next = i < tmp.size() - 1 ? tmp.get(i + 1) : null;
                if (next == null || !cur.getFile().equals(next.getFile())) {
                  prod.add(cur);
                } else if (cur.getType() == QueueEntry.TYPE.CREATE
                    && next.getType() == QueueEntry.TYPE.MODIFY) {
                  tmp.set(i + 1, cur);
                } else if (cur.getType() != next.getType()) {
                  prod.add(cur);
                }
              }
            }

            prod.forEach(
                e -> {
                  String msg;

                  switch (e.getType()) {
                    case CREATE:
                      msg = "New model available.";
                      break;
                    case DELETE:
                      msg = "Deleted model file.";
                      break;
                    case MODIFY:
                      msg = "Modified model available.";
                      break;
                    default:
                      LOG.warn("Unknown event type: {}", e.getType());
                      return;
                  }

                  NotificationTextMessage ntm = e.getNotification();

                  if (e.getType() == QueueEntry.TYPE.DELETE) {
                    ntm.setText(String.format(msg + "<br /><b>File</b>: %s", e.getFile()));
                  } else {
                    String modelName =
                        modelNameAccessor.getModelName(watchDir.resolve(e.getFile()));
                    if (modelName == null) {
                      // a model name is required - error already logged getModelName method
                      return;
                    }
                    ntm.setText(
                        String.format(
                            msg + "<br /><b>Name</b>: %s<br /><b>File</b>: %s",
                            modelName,
                            e.getFile()));
                  }
                  eventBus.post(ntm);
                });
            directoryChanged.filesChanged();
          } catch (Exception e) {
            if (!(e instanceof InterruptedException)) {
              LOG.warn("Error from msgQueueRunnableTask: {}", e.toString());
            }
          }
        };

    try {
      watchService = FileSystems.getDefault().newWatchService();
      LOG.info("newWatchService completed");
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
    List<QueueEntry> localEntries = new ArrayList<>();

    while ((key = watchService.take()) != null) {
      for (WatchEvent<?> event : key.pollEvents()) {
        QueueEntry queueEntry = new QueueEntry();
        NotificationTextMessage ntm = new NotificationTextMessage();
        ntm.setLevel(Level.INFO).setBroadcast(Broadcast.EXCLUSIVE);

        queueEntry.setFile(event.context().toString());

        if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
          queueEntry.setType(QueueEntry.TYPE.CREATE);
        } else if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
          queueEntry.setType(QueueEntry.TYPE.MODIFY);
        } else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
          queueEntry.setType(QueueEntry.TYPE.DELETE);
        }
        if (queueEntry.getType() != null) {
          localEntries.add(queueEntry.setNotification(ntm));
        }
      }
      key.reset();

      if (!localEntries.isEmpty()) {
        boolean isFirst;

        synchronized (msgQueue) {
          isFirst = msgQueue.isEmpty();
          msgQueue.addAll(localEntries);
        }
        localEntries.clear();
        if (isFirst) {
          msgQueueExecutor.submit(msgQueueRunnableTask);
        }
      }
    }
  }

  void stop() {
    if (watchService != null) {
      try {
        watchService.close();
      } catch (IOException e) {
        LOG.info("Error attempting to close WatchService for {}", watchDir, e);
      }
      msgQueueExecutor.shutdownNow();
    }
  }

  public interface DirectoryChanged {
    void filesChanged();
  }

  public interface ModelNameAccessor {
    String getModelName(Path file);
  }

  private static class QueueEntry {
    private TYPE type;;
    private String file;
    private NotificationTextMessage notification;

    public TYPE getType() {
      return type;
    }

    public QueueEntry setType(TYPE type) {
      this.type = type;
      return this;
    }

    public String getFile() {
      return file;
    }

    public QueueEntry setFile(String file) {
      this.file = file;
      return this;
    }

    public NotificationTextMessage getNotification() {
      return notification;
    }

    public QueueEntry setNotification(NotificationTextMessage notification) {
      this.notification = notification;
      return this;
    }

    enum TYPE {
      CREATE,
      DELETE,
      MODIFY
    }
  }
}
