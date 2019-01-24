package com.sri.ai.praisewm.web;

/** Error Message Response. Should only be created by the WebController. */
class ErrorResponseMessage {
  private String userMessage;
  private String techMessage;

  // The tech message will always be logged on the client, but never displayed to the user
  // unless displayTechMessage to set to true.
  private boolean displayTechMessage;

  // The HTTP status is always logged on the client, set to true to
  // include the status in the message displayed to the user.
  private boolean displayHttpStatus = true;

  // We need a no-args ctor for JSON conversion
  public ErrorResponseMessage() {}

  public ErrorResponseMessage(String userMessage) {
    this.userMessage = userMessage;
  }

  public String getUserMessage() {
    return userMessage;
  }

  public ErrorResponseMessage setUserMessage(String userMessage) {
    this.userMessage = userMessage;
    return this;
  }

  public String getTechMessage() {
    return techMessage;
  }

  public ErrorResponseMessage setTechMessage(String techMessage) {
    this.techMessage = techMessage;
    return this;
  }

  public boolean isDisplayTechMessage() {
    return displayTechMessage;
  }

  public ErrorResponseMessage setDisplayTechMessage(boolean displayTechMessage) {
    this.displayTechMessage = displayTechMessage;
    return this;
  }

  public boolean isDisplayHttpStatus() {
    return displayHttpStatus;
  }

  public ErrorResponseMessage setDisplayHttpStatus(boolean displayHttpStatus) {
    this.displayHttpStatus = displayHttpStatus;
    return this;
  }

  @Override
  public String toString() {
    return "ErrorResponseMessage{"
        + "userMessage='"
        + userMessage
        + '\''
        + ", techMessage='"
        + techMessage
        + '\''
        + ", displayTechMessage="
        + displayTechMessage
        + ", displayHttpStatus="
        + displayHttpStatus
        + '}';
  }
}
