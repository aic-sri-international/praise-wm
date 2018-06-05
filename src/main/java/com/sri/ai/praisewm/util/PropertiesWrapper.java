package com.sri.ai.praisewm.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.commons.lang3.Validate;

/** PropertiesWrapper. */
public class PropertiesWrapper {
  private final Map<Class, Function> converters = new HashMap<>();
  private final String resourceName;
  private final Properties properties;

  private PropertiesWrapper(String resourceName, Properties properties) {
    this.resourceName = resourceName;
    this.properties = properties;

    converters.put(String.class, (Function<String, String>) this::asString);
    converters.put(Integer.class, (Function<String, Integer>) this::asInt);
    converters.put(Long.class, (Function<String, Long>) this::asLong);
    converters.put(Boolean.class, (Function<String, Boolean>) this::asBool);
  }

  public static PropertiesWrapper fromClasspath(String resourceName) {
    return new PropertiesWrapper(
        resourceName, PropertiesUtil.getPropertiesFromClasspath(resourceName));
  }

  public Properties getProperties() {
    return properties;
  }

  @SuppressWarnings("unchecked")
  public <T> void ifExists(String name, Class<T> type, Consumer<T> consumer) {
    if (properties.containsKey(name)) {
      if (!converters.containsKey(type)) {
        throw new IllegalArgumentException(
            err(
                "Configuration property '%s' cannot be converted to a "
                    + type.getName()
                    + " because the type is not supported",
                name));
      }

      consumer.accept((T) converters.get(type).apply(name));
    }
  }

  public String asString(String name) {
    return Validate.notBlank(
        properties.getProperty(name), "Configuration property '%s' is not set", name);
  }

  public Integer asInt(String name) {
    try {
      return Integer.parseInt(asString(name));
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
          err("Configuration property '%s' " + "cannot be converted to an integer", name), e);
    }
  }

  public Long asLong(String name) {
    try {
      return Long.parseLong(asString(name));
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
          err("Configuration property '%s' " + "cannot be converted to a long", name), e);
    }
  }

  public Boolean asBool(String name) {
    String value = asString(name).toLowerCase();
    if (value.equals("true")) {
      return Boolean.TRUE;
    } else if (value.equals("false")) {
      return Boolean.FALSE;
    }

    throw new IllegalArgumentException(
        err("Configuration property '%s' " + "must be either 'true' or 'false'", name));
  }

  public String format() {
    return PropertiesUtil.format(properties, resourceName);
  }

  public String toString() {
    return properties.entrySet().toString();
  }

  private String err(String msg, String property) {
    return String.format(msg, property) + " : " + resourceName;
  }
}
