package com.sri.ai.praisewm.event.notification;

/** SessionTimeoutEvent */
public class SessionTimeoutEvent extends SessionCloseEvent {
  @Override
  public String toString() {
    return "SessionTimeoutEvent{} " + super.toString();
  }
}
