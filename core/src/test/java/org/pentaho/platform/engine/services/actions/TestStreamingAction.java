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


package org.pentaho.platform.engine.services.actions;

import org.pentaho.platform.api.action.IStreamingAction;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Date;

public class TestStreamingAction implements IStreamingAction {

  private OutputStream myContentOutputStream;

  private String message;

  private boolean executeWasCalled = false;

  public void setOutputStream( OutputStream outputStream ) {
    setMyContentOutputStream( outputStream );
  }

  public void setMyContentOutputStream( OutputStream myContentOutput ) {
    this.myContentOutputStream = myContentOutput;
  }

  public OutputStream getMyContentOutputStream() {
    return myContentOutputStream;
  }

  public ByteArrayOutputStream getMyContentOutput() {
    throw new IllegalStateException(
        "this method should never be called. It indicates that the Action framework is treating a content"
            + "type output as a normal output and not as a stream" );
  }

  public String getMimeType( String streamPropertyName ) {
    return "text/html";
  }

  public String getMessage() {
    return message;
  }

  public void setMessage( String message ) {
    this.message = message;
  }

  public boolean isExecuteWasCalled() {
    return executeWasCalled;
  }

  public void execute() throws Exception {
    StringBuilder html =
        new StringBuilder( "<html><h1>TestStreamingAction was here @ " + new Date().toString()
            + "!  Your message is \"" + message + "\"<h1>" );
    html.append( "</html>" );
    if ( myContentOutputStream != null ) {
      myContentOutputStream.write( html.toString().getBytes() );
    }
  }

}
