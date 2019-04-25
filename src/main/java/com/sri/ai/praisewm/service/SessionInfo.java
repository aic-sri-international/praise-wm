package com.sri.ai.praisewm.service;

import com.sri.ai.praisewm.db.jooq.tables.pojos.User;
import java.time.Instant;
import java.util.Objects;

/**
 * SessionInfo contains information about a UI client's Session.
 *
 * <p>This information is stored in-memory at the point a UI client is authorized, prior to
 * returning from {@code com.sri.ai.praisewm.service.SecurityService#login(User, Request,
 * Response)}.
 */
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

  /**
   * Get the last time this session was accessed.
   *
   * @return last access time
   */
  public long getLastAccessInMillis() {
    return lastAccessInMillis;
  }

  /**
   * Get the sessions unique ID.
   *
   * @return the session id
   */
  public String getSessionId() {
    return sessionId;
  }

  /**
   * Get the user name for associated with this session.
   *
   * @return the user name
   */
  public String getUserName() {
    return user.getName();
  }

  /**
   * Get the database user id associated with this session.
   *
   * @return the user id
   */
  public Integer getUserId() {
    return user.getUserId();
  }

  /**
   * Get the remote IP associated with this session.
   *
   * @return the client's remote IP
   */
  public String getRemoteIp() {
    return remoteIp;
  }

  /**
   * Set the last time this session's associated WebSocket was opened.
   *
   * @param wsLastOpenedTime the time the client's WebSocket was last opened
   */
  public void setWsLastOpenedTime(Instant wsLastOpenedTime) {
    this.wsLastOpenedTime = wsLastOpenedTime;
  }

  /**
   * Set the last time this session's associated WebSocket was closed.
   *
   * @param wsLastClosedTime the time the associated WebSocket for this session was last closed
   */
  public void setWsLastClosedTime(Instant wsLastClosedTime) {
    this.wsLastClosedTime = wsLastClosedTime;
  }

  /**
   * Determine if the session should currently have an open corresponding WebSocket connection.
   *
   * @return true if the UI client's WebSocket connection is open
   */
  public boolean isWsOpen() {
    return wsLastOpenedTime != null
        && (wsLastClosedTime == null || wsLastOpenedTime.isAfter(wsLastClosedTime));
  }

  /**
   * Update the session's accessed time.
   *
   * <p>Call this method to updated the associated UI client's last server access.
   */
  public void updateSessionAccessedTime() {
    lastAccessInMillis = System.currentTimeMillis();
  }

  /**
   * Equality is determined by the session id.
   *
   * @param o the other instance
   * @return true if the session id's are identical
   */
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

  /**
   * The hashcode is determined by the session id.
   *
   * @return the hashcode for the session id
   */
  @Override
  public int hashCode() {
    return Objects.hash(sessionId);
  }
}
