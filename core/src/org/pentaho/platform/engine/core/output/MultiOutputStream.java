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

package org.pentaho.platform.engine.core.output;

import java.io.IOException;
import java.io.OutputStream;

/*
 * A subclass of java.io.OutputStream. Forks an output stream to multiple 
 * output streams. Takes an array of output streams on construction and copies 
 * all output sent to it to each of the streams.
 */

public class MultiOutputStream extends OutputStream {

  OutputStream[] outs;

  public MultiOutputStream( OutputStream[] outs ) {
    this.outs = outs;
  }

  @Override
  public void write( int b ) throws IOException {
    IOException ioEx = null;
    for ( int idx = 0; idx < outs.length; idx++ ) {
      try {
        outs[idx].write( b );
      } catch ( IOException e ) {
        ioEx = e;
      }
    }
    if ( ioEx != null ) {
      throw ioEx;
    }
  }

  public void write( byte[] b ) throws IOException {
    IOException ioEx = null;
    for ( int idx = 0; idx < outs.length; idx++ ) {
      try {
        outs[idx].write( b );
      } catch ( IOException e ) {
        ioEx = e;
      }
    }
    if ( ioEx != null ) {
      throw ioEx;
    }
  }

  public void write( byte[] b, int off, int len ) throws IOException {
    IOException ioEx = null;
    for ( int idx = 0; idx < outs.length; idx++ ) {
      try {
        outs[idx].write( b, off, len );
      } catch ( IOException e ) {
        ioEx = e;
      }
    }
    if ( ioEx != null ) {
      throw ioEx;
    }
  }

  public void close() throws IOException {
    IOException ioEx = null;
    for ( int idx = 0; idx < outs.length; idx++ ) {
      try {
        outs[idx].close();
      } catch ( IOException e ) {
        ioEx = e;
      }
    }
    if ( ioEx != null ) {
      throw ioEx;
    }
  }

}
