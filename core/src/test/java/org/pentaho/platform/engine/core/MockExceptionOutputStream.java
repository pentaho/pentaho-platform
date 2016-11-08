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

package org.pentaho.platform.engine.core;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An output stream that throws an IOException on every operation. Used for testing purposes.
 * 
 * @author James Dixon
 * 
 */
public class MockExceptionOutputStream extends OutputStream {

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
