package com.sri.ai.praisewm.event.notification;

import java.time.Instant;

/** Base class for all NotificationEvent types. */
public abstract class NotificationEvent {
  private Instant time = Instant.now();
  private String sessionId;
  private String eventType = this.getClass().getSimpleName();
  private Level level;
  private Broadcast broadcast;

  public Instant getTime() {
    return time;
  }

  public NotificationEvent setTime(Instant time) {
    this.time = time;
    return this;
  }

  public String getSessionId() {
    return sessionId;
  }

  public NotificationEvent setSessionId(String sessionId) {
    this.sessionId = sessionId;
    return this;
  }

  public String getEventType() {
    return eventType;
  }

  public Level getLevel() {
    return level;
  }

  public NotificationEvent setLevel(Level level) {
    this.level = level;
    return this;
  }

  public Broadcast getBroadcast() {
    return broadcast;
  }

  public NotificationEvent setBroadcast(Broadcast broadcast) {
    this.broadcast = broadcast;
    return this;
  }

  @Override
  public String toString() {
    return "NotificationEvent{"
        + "time="
        + time
        + ", sessionId='"
        + sessionId
        + '\''
        + ", eventType='"
        + eventType
        + '\''
        + ", level="
        + level
        + ", broadcast="
        + broadcast
        + '}';
  }

  public enum Level {
    INFO,
    WARN,
    ERROR
  }

  public enum Broadcast {
    INCLUSIVE,
    EXCLUSIVE
  }
}
