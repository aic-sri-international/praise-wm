package com.sri.ai.praisewm.service;

import com.sri.ai.praisewm.util.PropertiesWrapper;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** ServiceRegistry. */
public final class ServiceRegistry {
  private static final Logger LOG = LoggerFactory.getLogger(ServiceRegistry.class);
  private final Set<Class<? extends Service>> services = new LinkedHashSet<>();
  private PropertiesWrapper pw;

  public ServiceRegistry(PropertiesWrapper pw) {
    this.pw = pw;
    registerServices();
  }

  private void registerServices() {
    TreeMap<Object, Object> sortedMap = new TreeMap<>(pw.getProperties());

    final String serviceKeyPattern = "^service\\.implementation\\.\\d+\\..*";

    sortedMap.entrySet().stream()
        .filter(e -> e.getKey().toString().matches(serviceKeyPattern))
        .forEach(
            e -> {
              Class<? extends Service> serviceClass;
              try {
                serviceClass = Class.forName(e.getValue().toString()).asSubclass(Service.class);
                services.add(serviceClass);
              } catch (ClassNotFoundException ex) {
                LOG.error(
                    "Skipping service class {} from '{}'. Class not found",
                    e.getValue().toString(),
                    pw.getName(),
                    ex);
              }
            });

    if (services.isEmpty()) {
      throw new IllegalArgumentException(
          String.format("You need to add at least one service to the '%s'", pw.getName()));
    }
  }

  public Set<Class<? extends Service>> get() {
    return Collections.unmodifiableSet(services);
  }
}
