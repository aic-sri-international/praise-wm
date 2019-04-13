package com.sri.ai.praisewm.web.error;

/**
 * Exception thrown if either the user is not found in the database, or, the password for the user
 * does not match.
 */
public class LoginException extends RuntimeException {
  private String user;
  private String password;

  public LoginException(String user, String password, String message) {
    super(message);
    this.user = user;
    this.password = password;
  }

  public String getUser() {
    return user;
  }

  public String getPassword() {
    return password;
  }
}
