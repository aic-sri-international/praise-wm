package com.sri.ai.praisewm.util;

import static java.util.stream.Collectors.toMap;

import com.google.common.io.Resources;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/** Utility methods for Properties */
final class PropertiesUtil {

  private PropertiesUtil() {}

  static String format(Properties properties, String title) {
    String props =
        properties.entrySet().stream()
            .filter((e) -> !e.getKey().toString().toLowerCase().contains("password"))
            .collect(toMap((e) -> e.getKey().toString(), Map.Entry::getValue))
            .entrySet()
            .stream()
            .sorted(Map.Entry.comparingByKey())
            .map((e) -> "  " + e.getKey() + "=" + e.getValue())
            .collect(Collectors.joining("\n"));

    return title != null ? (title + '\n' + props) : props;
  }

  static Properties getPropertiesFromClasspath(String resourceName) {
    try {
      return toProperties(readResourceFromClasspath(resourceName));
    } catch (Exception e) {
      throw new RuntimeException("Error getting resource: " + resourceName, e);
    }
  }

  static Properties toProperties(List<String> lines) {
    // Add support for end-of-line comments
    String propsBuffer =
        lines.stream()
            .map(
                (l) -> {
                  int ix = l.indexOf("#");
                  String value;
                  if (ix > 0) {
                    value = l.substring(0, ix);
                  } else {
                    value = l;
                  }
                  return value.trim();
                })
            .collect(Collectors.joining("\n"));

    Properties properties;

    try (Reader r = new StringReader(propsBuffer)) {
      properties = new Properties();
      properties.load(r);
    } catch (IOException e) {
      throw new RuntimeException("Error converting text lines to properties", e);
    }

    return properties;
  }

  static List<String> readResourceFromClasspath(String resourceName) {
    URL url = Resources.getResource(resourceName);

    try {
      return Resources.readLines(url, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException("Cannot load resource: " + resourceName, e);
    }
  }
}
