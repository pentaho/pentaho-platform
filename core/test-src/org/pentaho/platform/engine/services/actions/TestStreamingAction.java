/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.services.actions;

import org.pentaho.platform.api.action.IStreamingAction;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Date;

@SuppressWarnings( "nls" )
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
