package com.sri.ai.praisewm.event;

/** SessionStartedEvent */
public class SessionStartedEvent {
  private String sessionId;

  public SessionStartedEvent(String sessionId) {
    this.sessionId = sessionId;
  }

  public String getSessionId() {
    return sessionId;
  }

  @Override
  public String toString() {
    return "SessionStartedEvent{" + "sessionId='" + sessionId + '\'' + '}';
  }
}
