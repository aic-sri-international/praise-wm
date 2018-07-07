package com.sri.ai.praisewm.service.praise.remote;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.MeanDescriptor;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.gce.geotiff.GeoTiffFormat;
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
    GridCoverage2DReader reader = new GeoTiffFormat().getReader(imgStream);
    try {
      GridCoverage2D coverage = reader.read(null);
      return computeMean(coverage);
    } catch (IOException e) {
      throw new RuntimeException("Cannot compute mean for GeoTIFF", e);
    }
  }

  private static double computeMean(GridCoverage2D coverage) {
    RenderedOp rop =
        MeanDescriptor.create(
            coverage.getRenderedImage(),
            null, // entire image is scanned
            1,
            1, // every pixel in the ROI is processed
            null);
    double[] bands = (double[]) rop.getProperty("mean");
    LOG.info("computeMean: bands: {}", bands);
    return bands != null && bands.length > 0 ? bands[0] : 0;
  }

  //  private static double selectCells(GridCoverage2D cov, int value) {
  //    GridGeometry2D geom = cov.getGridGeometry();
  //    // cov.show();
  //    final OperationJAI op = new OperationJAI("Histogram");
  //    ParameterValueGroup params = op.getParameters();
  //    GridCoverage2D coverage;
  //    params.parameter("Source").setValue(cov);
  //    coverage = (GridCoverage2D) op.doOperation(params, null);
  //    javax.media.jai.Histogram hist = (Histogram) coverage
  //        .getProperty("histogram");
  //    int total = hist.getSubTotal(0, value, value);
  //    double area = calcAreaOfCell(geom);
  //    Unit<?> unit = cov.getCoordinateReferenceSystem().getCoordinateSystem()
  //        .getAxis(0).getUnit();
  //    System.out.println("which gives " + (area * total) + " " + unit
  //        + "^2 area with value " + value);
  //    return area * total;
  //  }
  //
  //  private static double calcAreaOfCell(GridGeometry2D geom) {
  //    GridEnvelope gridRange = geom.getGridRange();
  //    int width = gridRange.getHigh(0) - gridRange.getLow(0) + 1; // allow for the
  //    int height = gridRange.getHigh(1) - gridRange.getLow(1) + 1;// 0th row/col
  //    Envelope envelope = geom.getEnvelope();
  //    double dWidth = envelope.getMaximum(0) - envelope.getMinimum(0);
  //    double dHeight = envelope.getMaximum(1) - envelope.getMinimum(1);
  //    double cellWidth = dWidth / width;
  //    double cellHeight = dHeight / height;
  //
  //    return cellWidth * cellHeight;
  //  }

  //    private static void writeAsJpeg(GridCoverage2D coverage) {
  //      File out = new File("test.jpg");
  //      Iterator<ImageWriter> iter = ImageIO.getImageWritersBySuffix("jpg");
  //      ImageWriter writer = null;
  //      while (iter.hasNext()) {
  //        writer = iter.next();
  //        break;
  //      }
  //      try {
  //        writer.setOutput(new ImageOutputStreamAdapter(new FileOutputStream(out)));
  //      } catch (FileNotFoundException e) {
  //        e.printStackTrace();
  //      }
  //      try {
  //        writer.write(coverage.getRenderedImage());
  //      } catch (IOException e) {
  //        e.printStackTrace();
  //      }
  //    }
}
