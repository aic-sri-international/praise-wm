package com.sri.ai.praisewm.service.praise.remote;

import com.google.common.io.Files;
import com.jayway.jsonpath.JsonPath;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.List;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MintAccessor {
  private static final Logger LOG = LoggerFactory.getLogger(MintAccessor.class);
  private static final Path GEO_TIFF_OUTPUT_DIR = Paths.get("data/tmp/geotiffs");
  private static DecimalFormat PRECIPITATION_FORMAT = new DecimalFormat(".0#####");

  private RestClient restClient;

  public MintAccessor() {
    this.restClient = new RestClient();
  }

  public static void main(String[] args) {
    MintAccessor ma = new MintAccessor();
    ma.precipitationQuery(new MintQueryParameters());
  }

  String precipitationQuery(MintQueryParameters params) {
    try {
      return precipitationQuery_internal(params);
    } catch (Exception e) {
      LOG.error("Error running precipitationQuery {}", e.toString());
      return "254.235";
    }
  }

  private String precipitationQuery_internal(MintQueryParameters params) {
    List<String> dataSetLocations = lookupDataSetLocations(params);
    List<String> geotiffUrls = lookupGeotiffLocations(dataSetLocations.get(0));

    byte[] geotiff = downloadGeotiff(geotiffUrls.get(0));
    Double mean = GeotiffProcessor.computeMean(geotiff);
    return PRECIPITATION_FORMAT.format(mean);
  }

  private byte[] downloadGeotiff(String locationUrl) {
    return restClient.execute(new HttpGet(locationUrl), byte[].class);
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
      uri =
          new URIBuilder()
              .setScheme("http")
              .setHost("mint-demo.westus2.cloudapp.azure.com")
              .setPath("/data_sets")
              .setParameter("standard_name", params.getStandardName())
              .setParameter("start_time", params.getStartTime())
              .setParameter("end_time", params.getEndTime())
              .setParameter("location", params.getLocation())
              .build();
    } catch (URISyntaxException e) {
      throw new RuntimeException("Error building the query URI", e);
    }

    String json = restClient.execute(new HttpGet(uri), String.class);

    String jsonPath = "$.results.bindings[0:].variable_id.value";
    List<String> locationUrls;
    try {
      locationUrls = JsonPath.read(json, jsonPath);
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
    StringEntity entity = new StringEntity(jsonOut, ContentType.APPLICATION_JSON);
    HttpPost httppost =
        new HttpPost("http://mint-demo.westus2.cloudapp.azure.com/data_sets/get_location_url");
    httppost.setEntity(entity);

    String jsonIn = restClient.execute(httppost, String.class);

    String jsonPath = "$.results.bindings[0:].storage_path.value";
    List<String> locationUrls;
    try {
      locationUrls = JsonPath.read(jsonIn, jsonPath);
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
