package com.sri.ai.praisewm.service.praise_service;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.sri.ai.praisewm.event.notification.SessionCloseEvent;
import com.sri.ai.praisewm.web.error.ProcessingException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.Validate;

public class GraphCache {
  private final Map<String, GraphCacheEntry> sessionIdToEntry = new HashMap<>();
  private final EventBus eventBus;

  public GraphCache(EventBus eventBus) {
    this.eventBus = eventBus;
    eventBus.register(new SessionCloseEventListener());
  }

  void addEntry(String sessionId, GraphCacheEntry entry) {
    synchronized (sessionIdToEntry) {
      sessionIdToEntry.put(Validate.notEmpty(sessionId), entry);
    }
  }

  GraphCacheEntry getEntry(String sessionId) {
    GraphCacheEntry entry = sessionIdToEntry.get(Validate.notEmpty(sessionId));
    if (entry == null) {
      throw new ProcessingException(
          "Graph is not available, please re-submit the query",
          "GraphCacheEntry not in map for SessionId: " + sessionId);
    }
    return entry;
  }

  private class SessionCloseEventListener {
    @Subscribe
    public void eventHandler(SessionCloseEvent sessionCloseEvent) {
      synchronized (sessionIdToEntry) {
        sessionIdToEntry.remove(sessionCloseEvent.getSessionId());
      }
    }
  }
}
