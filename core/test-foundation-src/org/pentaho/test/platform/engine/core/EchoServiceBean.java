package org.pentaho.test.platform.engine.core;

import java.util.Date;

public class EchoServiceBean {
  
  public String echo(String message) {
    return new Date().toString()+":"+message; //$NON-NLS-1$
  }

}
