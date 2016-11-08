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
