package com.sri.ai.praisewm.service.dto;

import java.time.Instant;

/** UserDto provides information about a user. */
public class UserDto {
  private Long userId;
  private String name;
  private String fullname;
  private Instant createDate;
  private Instant lastLoginDate;
  private Integer loggedInCount;

  // no-arg constructor for JSON conversion
  public UserDto() {}

  public Long getUserId() {
    return userId;
  }

  public UserDto setUserId(Long userId) {
    this.userId = userId;
    return this;
  }

  public String getName() {
    return name;
  }

  public UserDto setName(String name) {
    this.name = name;
    return this;
  }

  public String getFullname() {
    return fullname;
  }

  public UserDto setFullname(String fullname) {
    this.fullname = fullname;
    return this;
  }

  public Instant getCreateDate() {
    return createDate;
  }

  public UserDto setCreateDate(Instant createDate) {
    this.createDate = createDate;
    return this;
  }

  public Instant getLastLoginDate() {
    return lastLoginDate;
  }

  public UserDto setLastLoginDate(Instant lastLoginDate) {
    this.lastLoginDate = lastLoginDate;
    return this;
  }

  public Integer getLoggedInCount() {
    return loggedInCount;
  }

  public UserDto setLoggedInCount(Integer loggedInCount) {
    this.loggedInCount = loggedInCount;
    return this;
  }
}
