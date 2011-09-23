package org.pentaho.test.platform.engine.services.actions;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Date;

import org.pentaho.platform.api.action.IStreamingAction;

@SuppressWarnings("nls")
public class TestStreamingAction implements IStreamingAction {

  private OutputStream myContentOutputStream;

  private String message;

  private boolean executeWasCalled = false;

  public void setOutputStream(OutputStream outputStream) {
    setMyContentOutputStream(outputStream);
  }

  public void setMyContentOutputStream(OutputStream myContentOutput) {
    this.myContentOutputStream = myContentOutput;
  }

  public OutputStream getMyContentOutputStream() {
    return myContentOutputStream;
  }

  public ByteArrayOutputStream getMyContentOutput() {
    throw new IllegalStateException(
        "this method should never be called. It indicates that the Action framework is treating a content"
            + "type output as a normal output and not as a stream");
  }

  public String getMimeType(String streamPropertyName) {
    return "text/html";
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public boolean isExecuteWasCalled() {
    return executeWasCalled;
  }

  public void execute() throws Exception {
    StringBuilder html = new StringBuilder("<html><h1>TestStreamingAction was here @ " + new Date().toString()
        + "!  Your message is \"" + message + "\"<h1>");
    html.append("</html>");
    if (myContentOutputStream != null) {
      myContentOutputStream.write(html.toString().getBytes());
    }
  }

}
