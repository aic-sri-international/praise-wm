package com.sri.ai.praisewm.event.notification;

import java.util.Map;

/** SystemStatusEvent. */
public class SystemStatusEvent extends NotificationTextMessage {
  private Map<SystemStatusType, Level> systemStatuses;

  public Map<SystemStatusType, Level> getSystemStatuses() {
    return systemStatuses;
  }

  public SystemStatusEvent setSystemStatuses(Map<SystemStatusType, Level> systemStatuses) {
    this.systemStatuses = systemStatuses;
    return this;
  }

  @Override
  public String toString() {
    return "SystemStatusEvent{" + "systemStatuses=" + systemStatuses + "} " + super.toString();
  }

  public enum SystemStatusType {
    DATABASE,
  }
}
