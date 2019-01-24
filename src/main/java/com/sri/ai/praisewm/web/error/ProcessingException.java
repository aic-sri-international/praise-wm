package com.sri.ai.praisewm.web.error;

import org.apache.commons.lang3.StringUtils;

/** An exception that does not fall under other web/error exceptions. */
public class ProcessingException extends RuntimeException {
  private String displayMessage;
  private String techMessage;
  private boolean logStackTrace = true;

  public ProcessingException(String displayMessage) {
    this(displayMessage, null, null);
  }

  public ProcessingException(String displayMessage, Throwable cause) {
    this(displayMessage, null, cause);
  }

  /**
   * ProcessingException - construct a ProcessingException
   *
   * @param displayMessage required message for the UI to display to the user
   * @param techMessage optional message with technical information that can be used for analysis
   */
  public ProcessingException(String displayMessage, String techMessage) {
    this(displayMessage, techMessage, null);
  }

  /**
   * ProcessingException - construct a ProcessingException
   *
   * @param displayMessage required message for the UI to display to the user
   * @param techMessage optional message with technical information that can be used for analysis
   * @param cause optional cause of the processing exception
   */
  public ProcessingException(String displayMessage, String techMessage, Throwable cause) {
    super(toExceptionMessage(displayMessage, techMessage), cause);
    this.displayMessage = displayMessage;
    this.techMessage = techMessage;

    if (StringUtils.trimToNull(displayMessage) == null) {
      throw new IllegalArgumentException("ProcessingException must contain a displayMessage");
    }
  }

  private static String toExceptionMessage(String displayMessage, String techMessage) {
    String msg = StringUtils.trimToEmpty(displayMessage);
    if (msg.isEmpty()) {
      throw new IllegalArgumentException("ProcessingException must contain a displayMessage");
    }

    String techMsg = StringUtils.trimToEmpty(techMessage);
    if (!techMsg.isEmpty()) {
      msg += ", TechMessage: " + techMsg;
    }

    return msg;
  }

  /**
   * Get the message to display to the UI client.
   *
   * @return the display message
   */
  public String getDisplayMessage() {
    return displayMessage;
  }

  /**
   * Get the technical message to log but not display to the UI client.
   *
   * @return the technical message
   */
  public String getTechMessage() {
    return techMessage;
  }

  /**
   * Find out if the stack trace should be logged.
   *
   * @return true is the stack trace should be logged
   */
  public boolean isLogStackTrace() {
    return logStackTrace;
  }

  /**
   * Determine if the stack track should be included in the logfile.
   *
   * <p>Unless including the stack trace will help in analysing the cause of the exception, call
   * this method passing <code>true</code> so that it does not clog up the logfile.
   *
   * @param logStackTrace false to not log the stack track
   * @return this ProcessingException
   */
  public ProcessingException setLogStackTrace(boolean logStackTrace) {
    this.logStackTrace = logStackTrace;
    return this;
  }
}
