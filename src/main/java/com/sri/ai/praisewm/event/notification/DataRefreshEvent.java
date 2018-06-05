package com.sri.ai.praisewm.event.notification;

/** RefreshDataEvent. */
public class DataRefreshEvent extends NotificationTextMessage {
  private RefreshType refreshType;

  public RefreshType getRefreshType() {
    return refreshType;
  }

  public DataRefreshEvent setRefreshType(RefreshType refreshType) {
    this.refreshType = refreshType;
    return this;
  }

  @Override
  public String toString() {
    return "RefreshDataEvent{" + "refreshType=" + refreshType + "} " + super.toString();
  }

  public enum RefreshType {
    USER,
  }
}
