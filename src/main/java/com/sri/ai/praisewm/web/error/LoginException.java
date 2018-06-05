package com.sri.ai.praisewm.web.error;

/** LoginException. */
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
