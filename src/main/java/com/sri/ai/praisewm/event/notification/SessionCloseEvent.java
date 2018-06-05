package com.sri.ai.praisewm.event.notification;

/** SessionCloseEvent */
public abstract class SessionCloseEvent extends NotificationTextMessage {
  @Override
  public String toString() {
    return "SessionCloseEvent{} " + super.toString();
  }
}
