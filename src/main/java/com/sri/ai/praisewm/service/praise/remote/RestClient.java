package com.sri.ai.praisewm.service.praise.remote;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestClient {
  private static final Logger LOG = LoggerFactory.getLogger(RestClient.class);
  private final CloseableHttpClient httpclient;

  public RestClient() {
    httpclient = HttpClients.createDefault();
  }

  public static void main(String[] args) {
    RestClient restClient = new RestClient();
    String q =
        "https://edcintl.cr.usgs.gov/fews_geoengine4/rest/dataset/fewschirps?listCoverages=true";
    HttpGet httpget = new HttpGet(q);
    String json = restClient.execute(httpget, String.class);
    if (json == null) {
      return;
    }

    File file = new File("data/fewschirps.json");
    try {
      Files.write(json, file, Charset.defaultCharset());
    } catch (IOException e) {
      LOG.error("Error writing JSON to {}", file, e);
    }
    LOG.info("JSON response for Query: {}\nWritten to {}", q, file);
  }

  public <T> T execute(HttpRequestBase request, Class<T> returnType) {
    LOG.info("HTTP Request: {}", request);
    CloseableHttpResponse response;
    try {
      response = httpclient.execute(request);
    } catch (IOException e) {
      LOG.error("Cannot execute {}", request, e);
      return null;
    }

    // The underlying HTTP connection is still held by the response object
    // to allow the response content to be streamed directly from the network socket.
    // In order to ensure correct deallocation of system resources
    // the user MUST call CloseableHttpResponse#close() from a finally clause.
    // Please note that if response content is not fully consumed the underlying
    // connection cannot be safely re-used and will be shut down and discarded
    // by the connection manager.

    String json = null;
    byte[] blob = null;
    try {
      LOG.info("HTTP status for: {}\n{}", request, response.getStatusLine());
      HttpEntity entity = response.getEntity();
      // do something useful with the response body
      // and ensure it is fully consumed
      try {
        if (returnType == String.class) {
          json = EntityUtils.toString(entity);
        } else {
          blob = EntityUtils.toByteArray(entity);
        }
        EntityUtils.consume(entity);
      } catch (IOException e) {
        LOG.error("Error consuming entity", e);
      }
    } finally {
      try {
        response.close();
      } catch (IOException e) {
        LOG.error("Error closing response", e);
      }
    }

    if (returnType == String.class) {
      return returnType.cast(json);
    } else {
      return returnType.cast(blob);
    }
  }

  public void close() {
    if (httpclient != null) {
      try {
        httpclient.close();
      } catch (IOException e) {
        LOG.error("Error closing httpclient", e);
      }
    }
  }
}
