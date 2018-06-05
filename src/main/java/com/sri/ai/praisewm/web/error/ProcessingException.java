package com.sri.ai.praisewm.web.error;

import org.apache.commons.lang3.StringUtils;

/** ProcessingException - an exception that does not fall under other web/error exceptions. */
public class ProcessingException extends RuntimeException {
  private String displayMessage;
  private String techMessage;

  public ProcessingException(String displayMessage) {
    this(displayMessage, null, null);
  }

  public ProcessingException(String displayMessage, Exception cause) {
    this(displayMessage, null, cause);
  }

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
  public ProcessingException(String displayMessage, String techMessage, Exception cause) {
    super(
        StringUtils.trimToEmpty(displayMessage)
            + ", TechMessage:"
            + StringUtils.trimToEmpty(techMessage),
        cause);
    this.displayMessage = displayMessage;
    this.techMessage = techMessage;

    if (StringUtils.trimToNull(displayMessage) == null) {
      throw new IllegalArgumentException("ProcessingException must contain a displayMessage");
    }
  }

  public String getDisplayMessage() {
    return displayMessage;
  }

  public String getTechMessage() {
    return techMessage;
  }
}
