/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.engine.services;

import org.apache.commons.logging.Log;
import org.pentaho.platform.engine.services.solution.SimpleContentGenerator;

import java.io.OutputStream;

public class MockContentGenerator extends SimpleContentGenerator {

  /**
   * 
   */
  private static final long serialVersionUID = -2265799756443767907L;

  @Override
  public void createContent( OutputStream out ) throws Exception {

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
