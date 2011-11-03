package org.pentaho.platform.repository2.unified.fileio;

import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.pentaho.platform.api.engine.IContentOutputHandler;
import org.pentaho.platform.api.engine.IMimeTypeListener;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.web.MimeHelper;

public class RepositoryFileOutputHandler implements IOutputHandler {

  private RepositoryFileOutputStream outputStream;
  private boolean contentGenerated;
  private boolean responseExpected;
  private IMimeTypeListener mimeTypeListener;
  private int outputType = IOutputHandler.OUTPUT_TYPE_DEFAULT;
  private IPentahoSession session;
  
  public RepositoryFileOutputHandler(RepositoryFileOutputStream outputStream) {
    this.outputStream = outputStream;
    contentGenerated = false;
  }
  
  public boolean allowFeedback() {
    return false;
  }

  public boolean contentDone() {
    return contentGenerated;
  }

  public IContentItem getFeedbackContentItem() {
    return null;
  }

  public IMimeTypeListener getMimeTypeListener() {
    return mimeTypeListener;
  }

  public IContentItem getOutputContentItem(final String outputName, final String contentName, final String instanceId, final String localMimeType) {
    IContentItem outputContentItem = null;
    if(outputName.equals(IOutputHandler.RESPONSE) && contentName.equals(IOutputHandler.CONTENT)) {
      String requestedFileExtension = MimeHelper.getExtension(localMimeType);
      String currentExtension = FilenameUtils.getExtension(outputStream.getFilePath());
      if (requestedFileExtension == null) {
        if (currentExtension != null) {
          String tempFilePath = FilenameUtils.getFullPathNoEndSeparator(outputStream.getFilePath()) + "/" + FilenameUtils.getBaseName(outputStream.getFilePath());
          outputContentItem = new RepositoryFileContentItem(tempFilePath);
        } else {
          outputContentItem = new RepositoryFileContentItem(outputStream);
        }
      } else if (!requestedFileExtension.substring(1).equals(currentExtension.toLowerCase())){
        String tempFilePath = FilenameUtils.getFullPathNoEndSeparator(outputStream.getFilePath()) + "/" + FilenameUtils.getBaseName(outputStream.getFilePath()) + requestedFileExtension;
        outputContentItem = new RepositoryFileContentItem(tempFilePath);
      } else {
        outputContentItem = new RepositoryFileContentItem(outputStream);
      }
      responseExpected = true;
    } else {
      IContentOutputHandler output = PentahoSystem.getOutputDestinationFromContentRef(contentName, session);
      // If the output handler wasn't found with just the content name. Try to look it up with the output name as well.
      // (This mirrors HttpOutputHandler's lookup logic)
      if (output == null) {
        output = PentahoSystem.getOutputDestinationFromContentRef(outputName + ":" + contentName, session); //$NON-NLS-1$
      }
      if (output != null) {
        output.setInstanceId(instanceId);
        output.setMimeType(localMimeType);
        outputContentItem = output.getFileOutputContentItem();
      }
    }
    return outputContentItem;
  }

  public int getOutputPreference() {
    return outputType;
  }

  public IPentahoSession getSession() {
    return session;
  }

  public boolean isResponseExpected() {
    return responseExpected;
  }

  public void setMimeTypeListener(IMimeTypeListener mimeTypeListener) {
    this.mimeTypeListener = mimeTypeListener;
  }

  public void setOutput(final String name, final Object value) throws IOException {
    if (value == null) {
      return;
    }

    if (IOutputHandler.CONTENT.equalsIgnoreCase(name)) {
      IContentItem response = getOutputContentItem("response", IOutputHandler.CONTENT, null, null); //$NON-NLS-1$
      if (response != null) {
        if (!(value instanceof IContentItem)) {
          if (response.getMimeType() == null) {
            response.setMimeType("text/html"); //$NON-NLS-1$
          }
          response.getOutputStream(null).write(value.toString().getBytes());
          contentGenerated = true;
        }
      }
    }
  }

  public void setOutputPreference(int outputType) {
    this.outputType = outputType;
  }

  public void setSession(IPentahoSession session) {
    this.session = session;
  }

}
