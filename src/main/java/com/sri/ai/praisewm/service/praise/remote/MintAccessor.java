package com.sri.ai.praisewm.service.praise.remote;

import com.google.common.io.Files;
import com.jayway.jsonpath.JsonPath;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MintAccessor {
  private static final Logger LOG = LoggerFactory.getLogger(MintAccessor.class);

  private RestClient restClient;

  public MintAccessor() {
    this.restClient = new RestClient();
  }

  public static void main(String[] args) {
    MintAccessor ma = new MintAccessor();
    System.out.println("Mean = " + ma.precipitationQuery(new MintQueryParameters()));
  }

  double precipitationQuery(MintQueryParameters params) {
    try {
      return precipitationQuery_internal(params);
    } catch (Exception e) {
      LOG.error("Error running precipitationQuery {}", e.toString());
      return 82.013789;
    }
  }

  private double precipitationQuery_internal(MintQueryParameters params) {
    List<String> dataSetLocations = lookupDataSetLocations(params);
    List<String> geotiffUrls = lookupGeotiffLocations(dataSetLocations.get(0));

    byte[] geotiff = downloadGeotiff(geotiffUrls.get(0));
    return GeotiffProcessor.computeMean(geotiff);
  }

  private byte[] downloadGeotiff(String locationUrl) {
    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(locationUrl)).build();
    HttpResponse<byte[]> response = restClient.send(request, BodyHandlers.ofByteArray());
   return response != null ? response.body() : new byte[0];
  }

  private void writeGeotiff(byte[] geotiff) {
    try {
      Files.write(geotiff, new File("./geotiff.tml.tiff"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private List<String> lookupDataSetLocations(MintQueryParameters params) {
    URI uri;
    try {
      uri = new URI("http://mint-demo.westus2.cloudapp.azure.com/data_sets?"
           +  restClient.newParamsBuilder()
              .add("standard_name", params.getStandardName())
              .add("start_time", params.getStartTime())
              .add("end_time", params.getEndTime())
              .add("location", params.getLocation())
              .build());
    } catch (URISyntaxException e) {
      throw new RuntimeException("Error building the query URI", e);
    }

    HttpRequest request = HttpRequest.newBuilder().uri(uri).build();
    HttpResponse<String> response = restClient.send(request, BodyHandlers.ofString());

    String jsonPath = "$.results.bindings[0:].variable_id.value";
    List<String> locationUrls;
    try {
      locationUrls = JsonPath.read(response.body(), jsonPath);
    } catch (com.jayway.jsonpath.PathNotFoundException ex) {
      throw new RuntimeException("Expected field not found in precipitationQuery result", ex);
    }
    if (locationUrls.isEmpty()) {
      throw new RuntimeException(
          "Expected field not found in precipitationQuery result" + " using JSONPATH " + jsonPath);
    }
    String variableId = locationUrls.get(0);
    if (locationUrls.size() > 1) {
      LOG.warn("Found multiple variable_id.value={}, only the first will be used", locationUrls);
    } else {
      LOG.info("Using variable_id.value={}", variableId);
    }
    return locationUrls;
  }

  private List<String> lookupGeotiffLocations(String variableId) {
    String jsonOut = "{ \"variable_id\":\"" + variableId + "\"}";
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("http://mint-demo.westus2.cloudapp.azure.com/data_sets/get_location_url"))
        .POST(BodyPublishers.ofString(jsonOut))
        .build();
    HttpResponse<String> response = restClient.send(request, BodyHandlers.ofString());

    String jsonPath = "$.results.bindings[0:].storage_path.value";
    List<String> locationUrls;
    try {
      locationUrls = JsonPath.read(response.body(), jsonPath);
    } catch (com.jayway.jsonpath.PathNotFoundException ex) {
      throw new RuntimeException("Expected field not found in query result", ex);
    }
    if (locationUrls.isEmpty()) {
      throw new RuntimeException(
          "Expected field not found in query result using JSONPATH " + jsonPath);
    }
    String url = locationUrls.get(0);
    if (locationUrls.size() > 1) {
      LOG.warn("Found multiple variable_id.value={}, only the first will be used", locationUrls);
    } else {
      LOG.info("Using variable_id.value={}", url);
    }

    return locationUrls;
  }
}
