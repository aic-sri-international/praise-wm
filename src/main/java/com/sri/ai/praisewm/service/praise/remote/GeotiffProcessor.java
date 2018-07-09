package com.sri.ai.praisewm.service.praise.remote;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeotiffProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(RestClient.class);

  public static void main(String[] args) {
    final Path testFile = Paths.get("fewschirps.tiff");

    if (!Files.exists(testFile)) {
      throw new RuntimeException("file does not exist");
    }

    byte[] imageBytes;
    try {
      imageBytes = Files.readAllBytes(testFile);
    } catch (IOException e) {
      throw new RuntimeException("Cannot read file " + testFile);
    }

    LOG.info("mean = {}", computeMean(imageBytes));
  }

  static double computeMean(byte[] imageBytes) {
    ByteArrayInputStream bStream = new ByteArrayInputStream(imageBytes);
    ImageInputStream imgStream;
    try {
      imgStream = ImageIO.createImageInputStream(bStream);
    } catch (IOException e) {
      throw new RuntimeException("Cannot convert imageBytes array to an ImageInputStream", e);
    }

    BufferedImage img;
    try {
      img = ImageIO.read(imgStream);
    } catch (IOException e) {
      throw new RuntimeException("Error reading image stream for GeoTIFF", e);
    }
    WritableRaster raster = img.getRaster();
    int numBands = raster.getNumBands();

    double total = 0.;
    int count = 0;

    int w = img.getWidth();
    int h = img.getHeight();

    for (int i = 0; i < w; i++) {
      for (int j = 0; j < h; j++) {
        Double s = 0.;

        for (int k = 0; k < numBands; k++) {
          double d = raster.getSampleDouble(i, j, k);
          s += d;
        }

        if (s >= 0) {
          total += s;
          ++count;
        }
      }
    }

    return count > 0 ? total / count : 0.;
  }
}
