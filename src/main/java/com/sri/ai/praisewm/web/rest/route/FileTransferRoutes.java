package com.sri.ai.praisewm.web.rest.route;

import com.sri.ai.praisewm.web.rest.util.SparkUtil;
import java.util.function.Function;
import spark.Request;

/** FileUploadRoutes. */
public class FileTransferRoutes extends AbstractRouteGroup {
  private final Function<Request, Object> fileUploader;

  public FileTransferRoutes(
      spark.Service sparkService, String scope, Function<Request, Object> fileUploader) {
    super(sparkService, scope);
    this.fileUploader = fileUploader;
  }

  @Override
  public void addRoutes() {
    post("/upload", (req, res) -> SparkUtil.respondCreated(req, res, fileUploader.apply(req)));
  }
}
