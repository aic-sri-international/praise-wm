package com.sri.ai.praisewm.service.praise_service.remote;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestClient {
  private static final Logger LOG = LoggerFactory.getLogger(RestClient.class);
  private final HttpClient httpclient;

  public RestClient() {
    httpclient =
        HttpClient.newBuilder()
            .followRedirects(Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(20))
            .build();
  }

  public static void main(String[] args) {
    RestClient restClient = new RestClient();
    String q =
        "https://edcintl.cr.usgs.gov/fews_geoengine4/rest/dataset/fewschirps?listCoverages=true";

    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(q)).build();
    HttpResponse<String> response = restClient.send(request, BodyHandlers.ofString());
    if (response != null) {
      System.out.println(response.statusCode());
      System.out.println(response.body());
    }
  }

  public ParamsBuilder newParamsBuilder() {
    return new ParamsBuilder();
  }

  public <T> HttpResponse<T> send(
      HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
    try {
      return httpclient.send(request, responseBodyHandler);
    } catch (Exception e) {
      LOG.error("HTTP send error on " + request, e);
      return null;
    }
  }

  public class ParamsBuilder {
    private StringBuilder sb = new StringBuilder();

    public ParamsBuilder add(String name, String value) {
      if (sb.length() > 0) {
        sb.append('&');
      }
      sb.append(name).append('=').append(URLEncoder.encode(value, StandardCharsets.UTF_8));
      return this;
    }

    public String build() {
      return sb.toString();
    }
  }
}
