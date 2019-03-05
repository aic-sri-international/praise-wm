package com.sri.ai.praisewm.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sri.ai.praisewm.db.jooq.tables.pojos.User;
import java.time.Instant;

/** LoginResponseDto. */
public class LoginResponseDto {
  private User user;

  @JsonProperty("isAdminRole")
  private Boolean isAdminRole;

  private Instant serverTime;
  private Integer wsReconnectInterval;
  private Integer wsMaxReconnectAttempts;
  private Integer wsClientInactivityTimeout;

  public User getUser() {
    return user;
  }

  public LoginResponseDto setUser(User user) {
    this.user = user;
    return this;
  }

  public boolean isAdminRole() {
    return isAdminRole;
  }

  public LoginResponseDto setAdminRole(boolean adminRole) {
    isAdminRole = adminRole;
    return this;
  }

  public Instant getServerTime() {
    return serverTime;
  }

  public LoginResponseDto setServerTime(Instant serverTime) {
    this.serverTime = serverTime;
    return this;
  }

  public int getWsReconnectInterval() {
    return wsReconnectInterval;
  }

  public LoginResponseDto setWsReconnectInterval(int wsReconnectInterval) {
    this.wsReconnectInterval = wsReconnectInterval;
    return this;
  }

  public int getWsMaxReconnectAttempts() {
    return wsMaxReconnectAttempts;
  }

  public LoginResponseDto setWsMaxReconnectAttempts(int wsMaxReconnectAttempts) {
    this.wsMaxReconnectAttempts = wsMaxReconnectAttempts;
    return this;
  }

  public int getWsClientInactivityTimeout() {
    return wsClientInactivityTimeout;
  }

  public LoginResponseDto setWsClientInactivityTimeout(int wsClientInactivityTimeout) {
    this.wsClientInactivityTimeout = wsClientInactivityTimeout;
    return this;
  }

  @Override
  public String toString() {
    return "LoginResponseDto{"
        + "user="
        + user
        + ", isAdminRole="
        + isAdminRole
        + ", serverTime="
        + serverTime
        + ", wsReconnectInterval="
        + wsReconnectInterval
        + ", wsMaxReconnectAttempts="
        + wsMaxReconnectAttempts
        + ", wsClientInactivityTimeout="
        + wsClientInactivityTimeout
        + '}';
  }
}
