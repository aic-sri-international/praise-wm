package com.sri.ai.praisewm.service;

import com.sri.ai.praisewm.db.jooq.tables.pojos.User;
import java.time.Instant;
import java.util.Objects;

/** SessionInfo. */
// Security Session Information
public class SessionInfo {

  private final String sessionId;
  private final User user;
  private final String remoteIp;
  private Instant wsLastOpenedTime;
  private Instant wsLastClosedTime;
  private long lastAccessInMillis = System.currentTimeMillis();

  public SessionInfo(String sessionId, User user, String remoteIp) {
    this.sessionId = sessionId;
    this.user = user;
    this.remoteIp = remoteIp;
  }

  public long getLastAccessInMillis() {
    return lastAccessInMillis;
  }

  public String getSessionId() {
    return sessionId;
  }

  public String getUserName() {
    return user.getName();
  }

  public Integer getUserId() {
    return user.getUserId();
  }

  public String getRemoteIp() {
    return remoteIp;
  }

  public void setWsLastOpenedTime(Instant wsLastOpenedTime) {
    this.wsLastOpenedTime = wsLastOpenedTime;
  }

  public void setWsLastClosedTime(Instant wsLastClosedTime) {
    this.wsLastClosedTime = wsLastClosedTime;
  }

  public boolean isWsOpen() {
    return wsLastOpenedTime != null
        && (wsLastClosedTime == null || wsLastOpenedTime.isAfter(wsLastClosedTime));
  }

  public void updateSessionAccessedTime() {
    lastAccessInMillis = System.currentTimeMillis();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SessionInfo that = (SessionInfo) o;
    return Objects.equals(sessionId, that.sessionId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sessionId);
  }
}
