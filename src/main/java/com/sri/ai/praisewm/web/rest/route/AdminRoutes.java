package com.sri.ai.praisewm.web.rest.route;

import com.google.common.eventbus.EventBus;
import com.sri.ai.praisewm.db.JooqTxProcessor;
import com.sri.ai.praisewm.db.jooq.tables.pojos.User;
import com.sri.ai.praisewm.event.notification.DataRefreshEvent.RefreshType;
import com.sri.ai.praisewm.repository.UserRepository;
import com.sri.ai.praisewm.service.SecurityServiceImpl;
import com.sri.ai.praisewm.web.rest.util.SparkUtil;

/** AdminRoutes. */
public class AdminRoutes extends AbstractRouteGroup {
  private JooqTxProcessor txp;
  private EventBus eventBus;

  public AdminRoutes(
      spark.Service sparkService, String scope, JooqTxProcessor txp, EventBus eventBus) {
    super(sparkService, scope);
    this.txp = txp;
    this.eventBus = eventBus;
  }

  @Override
  public void addRoutes() {
    // Create Users
    post(
        "/users",
        (req, res) ->
            txp.query(
                jc -> {
                  Number result =
                      UserRepository.createUser(jc, SparkUtil.fromJson(req, User.class));
                  eventBus.post(SparkUtil.newRefreshEvent(req, RefreshType.USER));
                  return SparkUtil.respondCreated(req, res, result);
                }));

    // Update Users
    put(
        "/users",
        (req, res) ->
            txp.query(
                jc -> {
                  User user = SparkUtil.fromJson(req, User.class);
                  // Do not allow a rest client to update the admin user
                  boolean updated =
                      !user.getName().equals(SecurityServiceImpl.ADMIN_NAME)
                          && UserRepository.updateUser(jc, user);
                  if (updated) {
                    eventBus.post(SparkUtil.newRefreshEvent(req, RefreshType.USER));
                  }
                  return SparkUtil.respondNoContentOrNotFound(res, updated);
                }));

    // Delete Users
    delete(
        "/users/:id",
        (req, res) ->
            txp.query(
                jc -> {
                  boolean deleted = UserRepository.deleteUser(jc, req.params(":id"));
                  if (deleted) {
                    eventBus.post(SparkUtil.newRefreshEvent(req, RefreshType.USER));
                  }
                  return SparkUtil.respondNoContentOrNotFound(res, deleted);
                }));
  }
}
