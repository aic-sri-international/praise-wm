package com.sri.ai.praisewm.web.rest.route;

import com.sri.ai.praisewm.db.JooqTxProcessor;
import com.sri.ai.praisewm.db.jooq.tables.pojos.User;
import com.sri.ai.praisewm.repository.UserRepository;
import com.sri.ai.praisewm.service.SecurityService;
import com.sri.ai.praisewm.service.SessionInfo;
import com.sri.ai.praisewm.service.dto.LoginResponseDto;
import com.sri.ai.praisewm.service.dto.UserDto;
import com.sri.ai.praisewm.service.mapper.UserMapper;
import com.sri.ai.praisewm.web.rest.util.SparkUtil;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** UserRoutes. */
public class UserRoutes extends AbstractRouteGroup {
  private JooqTxProcessor txp;
  private SecurityService securityService;

  public UserRoutes(
      spark.Service sparkService,
      String scope,
      SecurityService securityService,
      JooqTxProcessor txp) {
    super(sparkService, scope);
    this.securityService = securityService;
    this.txp = txp;
  }

  @Override
  public void addRoutes() {
    // Get list of users
    get(
        "/users",
        (req, res) ->
            txp.query(
                jc -> {
                  final Map<Integer, List<SessionInfo>> uToS =
                      securityService.getSessionMap().values().stream()
                          .filter(SessionInfo::isWsOpen)
                          .collect(Collectors.groupingBy(SessionInfo::getUserId));

                  List<UserDto> dtos =
                      UserRepository.getUsers(jc).stream()
                          .map(
                              u ->
                                  UserMapper.INSTANCE
                                      .userToUserDto(u)
                                      .setLoggedInCount(
                                          uToS.getOrDefault(u.getUserId(), Collections.emptyList())
                                              .size()))
                          .collect(Collectors.toList());
                  return SparkUtil.toJson(dtos, res);
                }));

    get(
        "/users/:id",
        (req, res) ->
            txp.query(
                jc -> {
                  User user = UserRepository.getUser(jc, SparkUtil.getParamInt(req, ":id"));
                  return SparkUtil.respondObjectOrNotFound(res, user);
                }));

    post(
        "/login",
        (req, res) ->
            txp.query(
                jc -> {
                  User user = SparkUtil.fromJson(req, User.class);
                  LoginResponseDto loginResponse = securityService.login(user, req, res);
                  return SparkUtil.respondObjectOrNotFound(res, loginResponse);
                }));

    post(
        "/logout",
        (req, res) ->
            txp.query(jc -> SparkUtil.respondObjectOrNotFound(res, securityService.logout(req))));
  }
}
