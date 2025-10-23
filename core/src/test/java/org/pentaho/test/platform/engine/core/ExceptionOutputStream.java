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


package org.pentaho.test.platform.engine.core;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An output stream that throws an IOException on every operation. Used for testing purposes.
 * 
 * @author James Dixon
 * 
 */
public class ExceptionOutputStream extends OutputStream {

  @Override
  public void write( final int b ) throws IOException {

    throw new IOException( "Test Exception" ); //$NON-NLS-1$

  }

  @Override
  public void write( final byte[] b ) throws IOException {

    throw new IOException( "Test Exception" ); //$NON-NLS-1$

  }

  @Override
  public void write( final byte[] b, final int off, final int len ) throws IOException {

    throw new IOException( "Test Exception" ); //$NON-NLS-1$

  }

  @Override
  public void close() throws IOException {
    throw new IOException( "Test Exception" ); //$NON-NLS-1$
  }
}
