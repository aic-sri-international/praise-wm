package com.sri.ai.praisewm.event.notification;

import java.time.Instant;

/** Base class for all NotificationEvent types. */
public abstract class NotificationEvent {
  private Instant time = Instant.now();
  private String sessionId;
  private String eventType = this.getClass().getSimpleName();
  private Level level;
  private Broadcast broadcast;

  /**
   * Get the time the event occurred.
   *
   * @return event time
   */
  public Instant getTime() {
    return time;
  }

  /**
   * Set the time of the event.
   *
   * <p>The default is the event creation time, so this method does not typically need to be called.
   *
   * @param time the event time
   * @return this instance
   */
  public NotificationEvent setTime(Instant time) {
    this.time = time;
    return this;
  }

  /**
   * Get the session id of the UI client that should receive this event.
   *
   * <p>If the event is not being sent to a specific client this value can be any value, such as the
   * service that created the event, so that it can be used for debugging purposes.
   *
   * @return the session id or non-client name
   */
  public String getSessionId() {
    return sessionId;
  }

  /**
   * Set the session id of the client that should receive the event or be excluded from receiving
   * the event if {@link Broadcast#EXCLUSIVE} is set.
   *
   * <p>If the event is not being sent to a specific client this value can be set to any value, such
   * as the service that created the event, so that it can be used for debugging purposes.
   *
   * @return this instance
   */
  public NotificationEvent setSessionId(String sessionId) {
    this.sessionId = sessionId;
    return this;
  }

  /**
   * Get the simple name of the event class.
   *
   * @return the name of the event
   */
  public String getEventType() {
    return eventType;
  }

  /**
   * Get the level of the event.
   *
   * @return the event level
   */
  public Level getLevel() {
    return level;
  }

  /**
   * Set the level of the event.
   *
   * @return this instance
   */
  public NotificationEvent setLevel(Level level) {
    this.level = level;
    return this;
  }
  /**
   * Get the {@link Broadcast} value.
   *
   * @return the {@link Broadcast} value
   */
  public Broadcast getBroadcast() {
    return broadcast;
  }

  /**
   * Set the {@link Broadcast} value.
   *
   * <p>Do not call this method if the event should only be sent to {@code sessionId}.
   *
   * @return this instance
   */
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

  /**
   * The level of the event.
   *
   * <p>The client can use this value to effect the display of the event message.
   */
  public enum Level {
    INFO,
    WARN,
    ERROR
  }

  public enum Broadcast {
    /** The event will be sent to all clients */
    INCLUSIVE,
    /** The event will be sent to all clients, except for the sessionId client */
    EXCLUSIVE
  }
}
