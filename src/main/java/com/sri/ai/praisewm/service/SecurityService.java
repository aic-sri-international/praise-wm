package com.sri.ai.praisewm.service;

import com.sri.ai.praisewm.db.jooq.tables.pojos.User;
import com.sri.ai.praisewm.service.dto.LoginResponseDto;
import java.util.Map;
import spark.Request;
import spark.Response;

/** SecurityService. */
public interface SecurityService {
  void authenticateAndAuthorize(Request request);

  LoginResponseDto login(User user, Request request, Response response);

  boolean logout(Request request);

  boolean updateAccess(String sessionId);

  Map<String, SessionInfo> getSessionMap();
}
