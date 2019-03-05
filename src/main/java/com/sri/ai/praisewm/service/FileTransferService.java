package com.sri.ai.praisewm.service;

import com.sri.ai.praisewm.util.FilesUtil;
import com.sri.ai.praisewm.web.error.ProcessingException;
import com.sri.ai.praisewm.web.rest.route.FileTransferRoutes;
import com.sri.ai.praisewm.web.rest.util.RouteScope;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;

/** FileTransferService * */
public class FileTransferService implements Service {
  private static final Logger LOG = LoggerFactory.getLogger(FileTransferService.class);
  private Path uploadDir;

  @Override
  public void start(ServiceManager serviceManager) {
    init(serviceManager.getConfiguration().asString("server.fileUploadFolder"));
    new FileTransferRoutes(
        serviceManager.getSparkService(),
        RouteScope.API,
        (request) -> new UploadFileProcessor(request, uploadDir).upload());
  }

  private void init(String fileUploadFolderSubPath) {
    uploadDir = Paths.get("." + fileUploadFolderSubPath).toAbsolutePath().normalize();
    FilesUtil.createDirectories(uploadDir, "file upload");
  }

  static class UploadFileProcessor {
    private static final int MEMORY_THRESHOLD = 1024 * 1024 * 3; // 3MB
    private static final long MAX_FILE_SIZE = 1024L * 1024 * 1024 * 10; // 10GB
    private static final long MAX_REQUEST_SIZE = 1024L * 1024 * 1024 * 10; // 10GB

    private Request req;
    private Path uploadDir;
    private boolean isUploaded;

    UploadFileProcessor(Request req, Path uploadDir) {
      this.req = req;
      this.uploadDir = uploadDir;
    }

    String upload() {
      LOG.debug("Started processing file upload request");
      String result = "";
      String currentFilename = null;
      Path currentFilePath = null;
      boolean hasFormField = false;

      // checks if the request actually contains upload file
      if (!ServletFileUpload.isMultipartContent(req.raw())) {
        throw new ProcessingException("Upload Error: Form must have enctype=multipart/form-data.");
      }

      // configures upload settings
      DiskFileItemFactory factory = new DiskFileItemFactory();
      // sets memory threshold - beyond which files are stored in disk
      factory.setSizeThreshold(MEMORY_THRESHOLD);
      // sets temporary location to store files
      factory.setRepository(new File(System.getProperty("java.io.tmpdir")));

      ServletFileUpload upload = new ServletFileUpload(factory);

      // sets maximum size of upload file
      upload.setFileSizeMax(MAX_FILE_SIZE);

      // sets maximum size of request (include file + form data)
      upload.setSizeMax(MAX_REQUEST_SIZE);

      try {
        List<FileItem> formItems = upload.parseRequest(req.raw());

        if (formItems != null && formItems.size() > 0) {
          for (FileItem item : formItems) {
            if (!item.isFormField()) {
              hasFormField = true;
              currentFilename = new File(item.getName()).getName();

              if (isUploaded) {
                LOG.error(
                    "Only one file per upload request is expected, file discarded: {}",
                    currentFilename);
              } else {
                currentFilePath = uploadDir.resolve(currentFilename);
                item.write(currentFilePath.toFile());
                isUploaded = true;
                result = currentFilename;
              }

              // release resources
              item.delete();
            }
          }
        }
      } catch (Exception ex) {
        String msg = "";
        if (currentFilename != null) {
          msg = String.format("Error uploading file %s", currentFilename);

          if (currentFilePath != null) {
            msg = String.format("%s to %s", msg, currentFilePath);
          }
        } else {
          msg = "Cannot upload file";
        }
        msg = String.format("%s: %s", msg, ex.getMessage());
        throw new ProcessingException(msg, ex);
      }

      if (!hasFormField) {
        throw new ProcessingException(
            "Received an upload request that did not include a form field");
      } else {
        LOG.debug("Uploaded file: '{}'", currentFilename);
      }
      return result;
    }
  }
}
