package com.sri.ai.praisewm.service.praise_service;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.sri.ai.praisewm.event.notification.SessionCloseEvent;
import com.sri.ai.praisewm.web.error.ProcessingException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.Validate;

public class QueryFunctionCache {
  private final Map<String, QueryFunctionCacheEntry> sessionIdToEntry = new HashMap<>();
  private final EventBus eventBus;

  public QueryFunctionCache(EventBus eventBus) {
    this.eventBus = eventBus;
    eventBus.register(new SessionCloseEventListener());
  }

  void addEntry(String sessionId, QueryFunctionCacheEntry entry) {
    synchronized (sessionIdToEntry) {
      sessionIdToEntry.put(Validate.notEmpty(sessionId), entry);
    }
  }

  QueryFunctionCacheEntry getEntry(String sessionId) {
    QueryFunctionCacheEntry entry = sessionIdToEntry.get(Validate.notEmpty(sessionId));
    if (entry == null) {
      throw new ProcessingException(
          "Query Result Function is not available, please re-submit the query",
          "QueryFunctionCacheEntry not in map for SessionId: " + sessionId);
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
