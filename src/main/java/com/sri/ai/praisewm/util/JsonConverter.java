package com.sri.ai.praisewm.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import java.io.IOException;
import java.util.Objects;

/** JsonConverter */
public final class JsonConverter {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  static {
    MAPPER.configure(SerializationFeature.INDENT_OUTPUT, true);
    MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    MAPPER.setSerializationInclusion(JsonInclude.Include.NON_ABSENT);
    MAPPER.registerModule(new ParameterNamesModule()).registerModule(new JavaTimeModule());
  }

  private JsonConverter() {}

  public static String to(Object object) {
    Objects.requireNonNull(object, "object cannot be null");

    try {
      return MAPPER.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new JsonConversionException(
          String.format(
              "Cannot convert object of type %s a JSON string", object.getClass().getName()),
          e);
    }
  }

  /**
   * Convert a JSON string into a Java Object.
   *
   * @param json the JSON to deserialize
   * @param expectedClassType the expected class type
   * @param <T> the class type
   * @return the Java object
   * @throws NullPointerException if required arguments are null
   * @throws IllegalArgumentException if json will not convert into expectedClassType
   */
  public static <T> T from(String json, Class<T> expectedClassType) {
    Objects.requireNonNull(json, "json cannot be null");
    Objects.requireNonNull(expectedClassType, "expectedClassType cannot be null");

    try {
      return MAPPER.readValue(json, expectedClassType);
    } catch (IOException e) {
      throw new JsonConversionException(
          String.format(
              "Cannot convert JSON string to an instanceof class %s: %s",
              expectedClassType.getName(), json),
          e);
    }
  }
}
