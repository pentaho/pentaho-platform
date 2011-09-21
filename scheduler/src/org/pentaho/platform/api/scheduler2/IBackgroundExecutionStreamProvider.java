package org.pentaho.platform.api.scheduler2;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.pentaho.platform.api.action.IStreamingAction;

/**
 * An object used by the scheduler to provide actions that expect to write to an output stream
 * with the stream to be written to.
 * @author arodriguez
 */

public interface IBackgroundExecutionStreamProvider extends Serializable {
  OutputStream getOutputStream() throws Exception;
  InputStream getInputStream() throws Exception;
  void setStreamingAction(IStreamingAction streamingAction);
}
