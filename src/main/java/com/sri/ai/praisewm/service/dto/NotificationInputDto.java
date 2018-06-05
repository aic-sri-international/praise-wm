package com.sri.ai.praisewm.service.dto;

import com.sri.ai.praisewm.event.notification.DataRefreshEvent.RefreshType;
import com.sri.ai.praisewm.event.notification.NotificationEvent;
import com.sri.ai.praisewm.event.notification.NotificationEvent.Broadcast;
import com.sri.ai.praisewm.event.notification.NotificationEvent.Level;
import com.sri.ai.praisewm.event.notification.SystemStatusEvent.SystemStatusType;

/** NotificationInputDto. */
public class NotificationInputDto {
  private NotificationEvent.Broadcast broadcast;
  private NotificationEvent.Level level;
  private RefreshType dataRefreshType;
  private SystemStatusType systemStatusType;
  private String text;

  public Broadcast getBroadcast() {
    return broadcast;
  }

  public NotificationInputDto setBroadcast(Broadcast broadcast) {
    this.broadcast = broadcast;
    return this;
  }

  public Level getLevel() {
    return level;
  }

  public NotificationInputDto setLevel(Level level) {
    this.level = level;
    return this;
  }

  public RefreshType getDataRefreshType() {
    return dataRefreshType;
  }

  public NotificationInputDto setDataRefreshType(RefreshType dataRefreshType) {
    this.dataRefreshType = dataRefreshType;
    return this;
  }

  public SystemStatusType getSystemStatusType() {
    return systemStatusType;
  }

  public NotificationInputDto setSystemStatusType(SystemStatusType systemStatusType) {
    this.systemStatusType = systemStatusType;
    return this;
  }

  public String getText() {
    return text;
  }

  public NotificationInputDto setText(String text) {
    this.text = text;
    return this;
  }

  @Override
  public String toString() {
    return "NotificationInputDto{"
        + "broadcast="
        + broadcast
        + ", level="
        + level
        + ", dataRefreshType="
        + dataRefreshType
        + ", systemStatusType="
        + systemStatusType
        + ", text='"
        + text
        + '\''
        + '}';
  }
}
