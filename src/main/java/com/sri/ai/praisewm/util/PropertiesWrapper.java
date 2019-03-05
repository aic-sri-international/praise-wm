package com.sri.ai.praisewm.util;

import static com.sri.ai.praisewm.util.FilesUtil.readFile;
import static com.sri.ai.praisewm.util.PropertiesUtil.getPropertiesFromClasspath;
import static com.sri.ai.praisewm.util.PropertiesUtil.toProperties;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.commons.lang3.Validate;

/**
 * Loads and wraps a properties file to provide type conversion and additional functionality.
 *
 * <p>Supports properties files with end of line comments
 */
public class PropertiesWrapper {
  private final Map<Class, Function> converters = new HashMap<>();
  private final String resourceName;
  private final Properties properties;

  PropertiesWrapper(String resourceName, Properties properties) {
    this.resourceName = resourceName;
    this.properties = properties;

    converters.put(String.class, (Function<String, String>) this::asString);
    converters.put(Integer.class, (Function<String, Integer>) this::asInt);
    converters.put(Long.class, (Function<String, Long>) this::asLong);
    converters.put(Boolean.class, (Function<String, Boolean>) this::asBool);
  }

  /**
   * Construct a PropertiesWrapper from a properties file located in the classpath
   *
   * @param resourceName the properties file
   * @return the PropertiesWrapper
   */
  public static PropertiesWrapper fromClasspath(String resourceName) {
    return new PropertiesWrapper(resourceName, getPropertiesFromClasspath(resourceName));
  }

  /**
   * Construct a PropertiesWrapper from a properties file path
   *
   * @param configFilepath path to the properties file
   * @return the PropertiesWrapper
   */
  public static PropertiesWrapper fromFile(Path configFilepath) {
    return new PropertiesWrapper(
        configFilepath.getFileName().toString(), toProperties(readFile(configFilepath)));
  }

  /**
   * Get the name of the properties file
   *
   * @return the file name
   */
  public String getName() {
    return resourceName;
  }

  /**
   * Get the contained Properties object.
   *
   * @return the Properties object
   */
  public Properties getProperties() {
    return properties;
  }

  /**
   * Merge the properties from another PropertiesWrapper
   *
   * <p>Any duplicates will be replaced.
   *
   * @param pw the other PropertiesWrapper
   */
  public void merge(PropertiesWrapper pw) {
    this.properties.putAll(pw.getProperties());
  }

  /**
   * Passes the converted value of a property to a consumer, if the property exists.
   *
   * @param name the property key
   * @param type the target class type to convert the property value (if it exists)
   * @param consumer the consumer to receive the converted value
   * @param <T> the type of the converted property value
   */
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

  /**
   * Get the directory path for a property whose value is a relative directory path.
   *
   * @param property the property key
   * @param createIfNotExist true if the directories should be created if they do not already exist
   * @return the absolute, normalized directory path
   * @throws RuntimeException if createIfNotExist is false and the directory does not exist, or
   *     createIfNotExist is true and the dir does not exist but cannot be created.
   */
  public Path getPathToRelativeDirectory(String property, boolean createIfNotExist) {
    Path path = Paths.get(".", asString(property)).toAbsolutePath().normalize();
    if (createIfNotExist) {
      try {
        Files.createDirectories(path);
      } catch (Exception e) {
        throw new RuntimeException(
            String.format("%s directory '%s' cannot be created", property, path), e);
      }
    } else {
      if (!Files.isDirectory(path)) {
        throw new RuntimeException(
            String.format("%s directory does not exist '%s'", property, path));
      }
    }
    return path;
  }

  /**
   * Get the Path for a relative directory property.
   *
   * <p>The directory and any intermediate directories will be created if they do not already exist.
   *
   * @param property relative directory property name
   * @return absolute directory Path
   */
  public Path getPathToRelativeDirectory(String property) {
    return getPathToRelativeDirectory(property, true);
  }

  /**
   * Get a property as a string.
   *
   * @param name the property key
   * @return the string value of the property
   * @throws RuntimeException if the property does not exist
   */
  public String asString(String name) {
    return Validate.notBlank(
        properties.getProperty(name), "Configuration property '%s' is not set", name);
  }

  /**
   * Get a property as a integer.
   *
   * @param name the property key
   * @return the integer value of the property
   * @throws RuntimeException if the property does not exist or cannot be converted into an integer
   */
  public Integer asInt(String name) {
    try {
      return Integer.parseInt(asString(name));
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
          err("Configuration property '%s' " + "cannot be converted to an integer", name), e);
    }
  }

  /**
   * Get a property as a long.
   *
   * @param name the property key
   * @return the long value of the property
   * @throws RuntimeException if the property does not exist or cannot be converted into a long
   */
  public Long asLong(String name) {
    try {
      return Long.parseLong(asString(name));
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
          err("Configuration property '%s' " + "cannot be converted to a long", name), e);
    }
  }

  /**
   * Get a property as a boolean.
   *
   * @param name the property key
   * @return the boolean value of the property
   * @throws RuntimeException if the property does not exist or cannot be converted into a boolean
   */
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

  /**
   * Get a formatted display of all of the key/value pairs.
   *
   * <p>If a key contains 'password' (in any case) its entry will not be included in the display.
   *
   * @return the formatted string
   */
  public String format() {
    return PropertiesUtil.format(properties, resourceName);
  }

  /**
   * Get a complete unformatted display of the set of key/value pairs.
   *
   * @return the string
   */
  public String toString() {
    return properties.entrySet().toString();
  }

  private String err(String msg, String property) {
    return String.format(msg, property) + " : " + resourceName;
  }
}
