package org.pentaho.platform.engine.services;

import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.pentaho.platform.engine.services.solution.SimpleContentGenerator;

public class MockContentGenerator extends SimpleContentGenerator {

  @Override
  public void createContent(OutputStream out) throws Exception {

    out.write( "MockContentGenerator content".getBytes() );
  
  }

  @Override
  public String getMimeType() {
    return "text/test";
  }

  @Override
  public Log getLogger() {
    // TODO Auto-generated method stub
    return null;
  }

}
