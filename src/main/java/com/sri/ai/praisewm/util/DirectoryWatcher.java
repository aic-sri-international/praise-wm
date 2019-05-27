package com.sri.ai.praisewm.util;

import com.sri.ai.praisewm.util.DirectoryWatcher.DirChangeEntry.DirChangeType;
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
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectoryWatcher {
  private static final Logger LOG = LoggerFactory.getLogger(DirectoryWatcher.class);
  private static final AtomicInteger uniquifier = new AtomicInteger();
  private final List<DirChangeEntry> dirChangeQueue = new ArrayList<>();
  private final ExecutorService dirChangeQueueExecutor = Executors.newSingleThreadExecutor();
  private final Runnable dirChangeQueueRunnable;
  private final Path watchDir;
  private WatchService watchService;

  public DirectoryWatcher(
      final Path watchDir,
      final boolean flushQueueOnOverflow,
      final DirectoryChanged directoryChanged) {
    this.watchDir = watchDir;

    dirChangeQueueRunnable =
        () -> {
          try {
            TimeUnit.MILLISECONDS.sleep(2000);

            List<DirChangeEntry> prod;
            List<DirChangeEntry> tmp;

            synchronized (dirChangeQueue) {
              if (dirChangeQueue.isEmpty()) {
                LOG.debug("dirChangeQueue is empty");
                return;
              }

              if (dirChangeQueue.size() == 1) {
                prod = new ArrayList<>(dirChangeQueue);
                tmp = Collections.emptyList();
              } else {
                tmp = new ArrayList<>(dirChangeQueue);
                prod = new ArrayList<>();

                if (flushQueueOnOverflow) {
                  for (DirChangeEntry dirChangeEntry : tmp) {
                    if (dirChangeEntry.getType() == DirChangeType.OVERFLOW) {
                      prod.add(dirChangeEntry);
                      tmp = Collections.emptyList();
                      break;
                    }
                  }
                }
              }
              dirChangeQueue.clear();
            }

            LOG.debug("dirChangeQueue length: {}", Math.max(tmp.size(), prod.size()));
            if (prod.isEmpty()) {
              // remove duplicates and if a modify is preceded by a create, only keep the create.
              for (int i = 0; i < tmp.size(); ++i) {
                DirChangeEntry cur = tmp.get(i);
                DirChangeEntry next = i < tmp.size() - 1 ? tmp.get(i + 1) : null;
                if (next == null || !cur.getFile().equals(next.getFile())) {
                  prod.add(cur);
                } else if (cur.getType() == DirChangeEntry.DirChangeType.CREATE
                    && next.getType() == DirChangeEntry.DirChangeType.MODIFY) {
                  tmp.set(i + 1, cur);
                } else if (cur.getType() != next.getType()) {
                  prod.add(cur);
                }
              }
            }

            prod.forEach(
                e -> {
                  switch (e.getType()) {
                    case CREATE:
                    case DELETE:
                    case MODIFY:
                    case OVERFLOW:
                      break;
                    default:
                      LOG.warn("Unknown event type: {}", e.getType());
                      return;
                  }

                  directoryChanged.fileEntryChange(e);
                });
          } catch (Exception e) {
            if (!(e instanceof InterruptedException)) {
              LOG.warn("Error from dirChangeQueueRunnable: {}", e.toString());
            }
          }
        };

    try {
      watchService = FileSystems.getDefault().newWatchService();
      LOG.debug("newWatchService completed");
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
                LOG.info("WatchService thread for {} terminated, {}", watchDir, e.toString());
              }
            });
    thread.setName("DirectoryWatcher-" + uniquifier.getAndIncrement());
    thread.start();
  }

  private void loop() throws InterruptedException {
    WatchKey key;
    List<DirChangeEntry> localEntries = new ArrayList<>();

    while ((key = watchService.take()) != null) {
      for (WatchEvent<?> event : key.pollEvents()) {
        DirChangeEntry dirChangeEntry = new DirChangeEntry();
        if (event.kind() != StandardWatchEventKinds.OVERFLOW) {
          dirChangeEntry.setFile(event.context().toString());
        }

        if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
          dirChangeEntry.setType(DirChangeEntry.DirChangeType.CREATE);
        } else if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
          dirChangeEntry.setType(DirChangeEntry.DirChangeType.MODIFY);
        } else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
          dirChangeEntry.setType(DirChangeEntry.DirChangeType.DELETE);
        } else if (event.kind() == StandardWatchEventKinds.OVERFLOW) {
          dirChangeEntry.setType(DirChangeEntry.DirChangeType.OVERFLOW);
        }

        if (dirChangeEntry.getType() != null) {
          localEntries.add(dirChangeEntry);
        }
      }
      key.reset();

      if (!localEntries.isEmpty()) {
        boolean isFirst;

        synchronized (dirChangeQueue) {
          isFirst = dirChangeQueue.isEmpty();
          dirChangeQueue.addAll(localEntries);
        }
        localEntries.clear();
        if (isFirst) {
          dirChangeQueueExecutor.submit(dirChangeQueueRunnable);
        }
      }
    }
  }

  public void stop() {
    if (watchService != null) {
      try {
        watchService.close();
      } catch (IOException e) {
        LOG.info("Error attempting to close WatchService for {}", watchDir, e);
      }
      dirChangeQueueExecutor.shutdownNow();
    }
  }

  public interface DirectoryChanged {
    void fileEntryChange(DirChangeEntry dirChangeEntry);
  }

  public static class DirChangeEntry {
    private DirChangeType type;
    private String file;

    public DirChangeType getType() {
      return type;
    }

    public DirChangeEntry setType(DirChangeType type) {
      this.type = type;
      return this;
    }

    /**
     * Get the file related to the event.
     *
     * @return the file or null if the event type is OVERFLOW
     */
    public String getFile() {
      return file;
    }

    public DirChangeEntry setFile(String file) {
      this.file = file;
      return this;
    }

    @Override
    public String toString() {
      return "DirChangeEntry{" + "type=" + type + ", file='" + file + '\'' + '}';
    }

    public enum DirChangeType {
      CREATE,
      DELETE,
      MODIFY,
      /** Events may have been lost or discarded. */
      OVERFLOW,
    }
  }
}
