package com.sri.ai.praisewm.event.notification;

/** A text message to send to UI clients. */
public class NotificationTextMessage extends NotificationEvent {
  private String text;

  public String getText() {
    return text;
  }

  public NotificationTextMessage setText(String text) {
    this.text = text;
    return this;
  }

  @Override
  public String toString() {
    return "NotificationTextMessage{" + "text='" + text + '\'' + "} " + super.toString();
  }
}
