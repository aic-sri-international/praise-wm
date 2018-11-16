package com.sri.ai.praisewm.service;

import com.sri.ai.praisewm.service.praise_service.PraiseServiceImpl;
import java.util.LinkedHashSet;
import java.util.Set;

/** ServiceRegistry. */
public final class ServiceRegistry {
  private static final Set<Class<? extends Service>> SERVICES = new LinkedHashSet<>();

  static {
    // Add services in the order that they are to be initialized
    // The security service must be first
    SERVICES.add(SecurityServiceImpl.class);
    SERVICES.add(NotificationService.class);
    SERVICES.add(AdminService.class);
    SERVICES.add(UserService.class);
    SERVICES.add(NotificationInputService.class);
    SERVICES.add(FileTransferService.class);
    SERVICES.add(SystemStatusService.class);
    SERVICES.add(PraiseServiceImpl.class);
  }

  public static Set<Class<? extends Service>> get() {
    return SERVICES;
  }
}
