package com.sri.ai.praisewm.web.rest;

/** Error Message Response. */
public class ErrorResponseMessage {
  private String message;
  private String techMessage;

  // We need a no-args ctor for JSON conversion
  public ErrorResponseMessage() {}

  public ErrorResponseMessage(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  public ErrorResponseMessage setMessage(String message) {
    this.message = message;
    return this;
  }

  public String getTechMessage() {
    return techMessage;
  }

  public ErrorResponseMessage setTechMessage(String techMessage) {
    this.techMessage = techMessage;
    return this;
  }

  @Override
  public String toString() {
    return "ErrorResponseMessage{"
        + "message='"
        + message
        + '\''
        + ", techMessage='"
        + techMessage
        + '\''
        + '}';
  }
}
