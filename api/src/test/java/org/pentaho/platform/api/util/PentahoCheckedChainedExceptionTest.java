/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.api.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

import org.junit.Test;

/**
 * Created by bgroves on 11/6/15.
 */
public class PentahoCheckedChainedExceptionTest {
  private static final String MSG = "Break in the chain";
  private static final String CAUSE_MSG = "Root cause";
  private static final Exception CAUSE = new Exception( CAUSE_MSG );

  @Test
  public void testContructors() {
    PentahoCheckedChainedException exception = new CheckedException();
    assertNull( exception.getMessage() );
    assertEquals( exception, exception.getRootCause() );

    exception = new CheckedException( MSG );
    assertEquals( MSG, exception.getMessage() );
    assertEquals( exception, exception.getRootCause() );

    exception = new CheckedException( CAUSE );
    assertNotNull( exception.getMessage() );
    assertTrue( exception.getMessage().contains( CAUSE_MSG ) );
    assertEquals( CAUSE, exception.getRootCause() );

    exception = new CheckedException( MSG, CAUSE );
    assertEquals( MSG, exception.getMessage() );
    assertEquals( CAUSE, exception.getRootCause() );
  }

  @Test
  public void testGetRootCause() {
    PentahoCheckedChainedException exception = new CheckedException( CAUSE );
    Throwable rootCause = exception.getRootCause();
    assertEquals( CAUSE, rootCause );
  }

  @Test
  public void testPrintStackTrace() {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    PrintWriter writer = new PrintWriter( stream, true );
    PrintStream pStream = new PrintStream( stream, true );

    PentahoCheckedChainedException exception = new CheckedException( CAUSE );
    exception.printStackTrace( writer );
    assertNotEquals( "", stream.toString() );

    exception.printStackTrace( pStream );
    assertNotEquals( "", pStream.toString() );
  }

  private class CheckedException extends PentahoCheckedChainedException {
    private static final long serialVersionUID = -7614124426358360606L;

    public CheckedException() {
      super();
    }

    public CheckedException( String message ) {
      super( message );
    }

    public CheckedException( String message, Throwable cause ) {
      super( message, cause );
    }

    public CheckedException( Throwable cause ) {
      super( cause );
    }
  }
}
