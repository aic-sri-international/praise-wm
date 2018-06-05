package com.sri.ai.praisewm.web.rest.route;

import com.google.common.eventbus.EventBus;
import com.sri.ai.praisewm.event.notification.DataRefreshEvent;
import com.sri.ai.praisewm.event.notification.NotificationEvent;
import com.sri.ai.praisewm.event.notification.NotificationEvent.Level;
import com.sri.ai.praisewm.event.notification.NotificationTextMessage;
import com.sri.ai.praisewm.event.notification.SystemStatusEvent;
import com.sri.ai.praisewm.service.SecurityServiceImpl;
import com.sri.ai.praisewm.service.dto.NotificationInputDto;
import com.sri.ai.praisewm.web.rest.util.SparkUtil;
import java.util.Collections;

/** NotificationInputRoutes. */
public class NotificationInputRoutes extends AbstractRouteGroup {
  private EventBus eventBus;

  public NotificationInputRoutes(spark.Service sparkService, String scope, EventBus eventBus) {
    super(sparkService, scope);
    this.eventBus = eventBus;
  }

  @Override
  public void addRoutes() {
    put(
        "/notification",
        (req, res) -> {
          NotificationInputDto nim = SparkUtil.fromJson(req, NotificationInputDto.class);
          String sessionId = SecurityServiceImpl.getSessionId(req);
          NotificationEvent event;
          if (nim.getDataRefreshType() != null) {
            event = prepDataRefresh(nim);
          } else if (nim.getSystemStatusType() != null) {
            event = prepSystemStatus(nim);
          } else {
            event = prepTextNotification(nim);
          }
          event.setSessionId(sessionId);
          eventBus.post(event);
          return SparkUtil.respondNoContent(res);
        });
  }

  private DataRefreshEvent prepDataRefresh(NotificationInputDto nim) {
    DataRefreshEvent dre = new DataRefreshEvent();
    updateBaseData(nim, dre);
    dre.setRefreshType(nim.getDataRefreshType());

    if (dre.getText() == null) {
      dre.setText("Data refresh event received for " + dre.getRefreshType());
    }

    dre.setLevel(Level.INFO);

    return dre;
  }

  private SystemStatusEvent prepSystemStatus(NotificationInputDto nim) {
    SystemStatusEvent sse = new SystemStatusEvent();
    updateBaseData(nim, sse);
    sse.setSystemStatuses(Collections.singletonMap(nim.getSystemStatusType(), nim.getLevel()));
    sse.setLevel(nim.getLevel());
    if (sse.getText() == null) {
      sse.setText("System Status event received for " + nim.getSystemStatusType());
    }

    return sse;
  }

  private NotificationTextMessage prepTextNotification(NotificationInputDto nim) {
    if (nim.getText() == null) {
      throw new IllegalArgumentException(
          "NotificationInputDto must contain a text message " + "if it is not a DataRefreshType");
    }

    NotificationTextMessage ntm = new NotificationTextMessage();
    updateBaseData(nim, ntm);
    ntm.setLevel(nim.getLevel() == null ? Level.INFO : nim.getLevel());
    return ntm;
  }

  private void updateBaseData(NotificationInputDto nim, NotificationTextMessage ntm) {
    ntm.setBroadcast(nim.getBroadcast());
    ntm.setText(nim.getText());
  }
}
