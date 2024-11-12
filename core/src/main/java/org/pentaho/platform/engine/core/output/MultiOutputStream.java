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
