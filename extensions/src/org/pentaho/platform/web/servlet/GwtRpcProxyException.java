package org.pentaho.platform.web.servlet;

import java.io.Serializable;

public class GwtRpcProxyException extends RuntimeException implements Serializable {

  private static final long serialVersionUID = -5090524647540284482L;

  public GwtRpcProxyException() {
    // TODO Auto-generated constructor stub
  }

  public GwtRpcProxyException(String message) {
    super(message);
    // TODO Auto-generated constructor stub
  }

  public GwtRpcProxyException(Throwable cause) {
    super(cause);
    // TODO Auto-generated constructor stub
  }

  public GwtRpcProxyException(String message, Throwable cause) {
    super(message, cause);
    // TODO Auto-generated constructor stub
  }

}
