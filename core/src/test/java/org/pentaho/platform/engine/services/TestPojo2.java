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

import org.pentaho.platform.api.engine.IActionSequenceResource;

import java.io.InputStream;
import java.io.OutputStream;

@SuppressWarnings( { "all" } )
public class TestPojo2 {

  protected OutputStream outputStream;
  protected String input1;

  public boolean execute() throws Exception {

    // this will generate a null pointer if input1 is null
    String output = input1 + input1;

    // this will generate an exception is outputStream is null
    outputStream.write( output.getBytes() );
    outputStream.close();

    return true;
  }

  public void setInput1( String input1 ) {
    this.input1 = input1;
  }

  public void setOutputStream( OutputStream outputStream ) {
    this.outputStream = outputStream;
  }

  public String getMimeType() {
    return "text/text";
  }

  public boolean validate() throws Exception {
    return true;
  }

  public void setResource1( InputStream stream ) {
    PojoComponentTest.setResourceInputStreamCalled = true;
  }

  public void setResource2( IActionSequenceResource resource ) {
    PojoComponentTest.setActionSequenceResourceCalled = true;
  }

}
