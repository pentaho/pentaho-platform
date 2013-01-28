package org.pentaho.platform.engine.services.actionsequence;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.pentaho.commons.connection.IPentahoStreamSource;
import org.pentaho.platform.api.engine.IActionSequenceResource;

public final class ActionSequenceResourceWrapper implements IPentahoStreamSource {
  private InputStream inputStream;

  private IActionSequenceResource resource;

  public ActionSequenceResourceWrapper(IActionSequenceResource actionResource, InputStream iStream) {
    super();
    assert (actionResource != null);
    assert (iStream != null);
    this.resource = actionResource;
    this.inputStream = iStream;
  }

  public String getContentType() {
    return resource.getMimeType();
  }

  public InputStream getInputStream() throws IOException {
    if (inputStream != null){
      if(inputStream.markSupported()) {
        try {
          inputStream.reset();
        }
        catch(IOException e) {
          //do nothing: a mark has not been set on this stream. This is not an error.
          return inputStream;
        }
      }
    }
    return inputStream;
  }

  public String getName() {
    return resource.getName();
  }

  public OutputStream getOutputStream() throws IOException {
    return new ByteArrayOutputStream();
  }
}
