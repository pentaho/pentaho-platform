package org.pentaho.test.platform.web.http.api;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.api.action.IStreamProcessingAction;
import org.pentaho.platform.api.action.IStreamingAction;

public class SchedulerTestAction implements IAction, IStreamingAction, IStreamProcessingAction {

  private InputStream inputStream;
  private OutputStream outputStream;
  
  public void execute() throws Exception {
    IOUtils.copy(inputStream, outputStream);
    outputStream.close();
  }

  public String getMimeType(String reportOutput) {
    return null;
  }

  public void setInputStream(InputStream inputStream) {
    this.inputStream = inputStream;
  }

  public void setOutputStream(OutputStream outputStream) {
    this.outputStream = outputStream;
  }

}
