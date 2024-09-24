/*!
 *
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
 *
 * Copyright (c) 2002-2024 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.api.util;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class PentahoChainedExceptionTest {
  private static final String CAUSE_MSG = "Root cause";
  private static final Exception CAUSE = new Exception( CAUSE_MSG );

  @Test
  public void testGetRootCause() {
    PentahoChainedException exception = new PentahoChainedException( CAUSE );
    Throwable rootCause = exception.getRootCause();
    assertEquals( CAUSE, rootCause );
  }

  @Test
  public void testPrintStackTrace() {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    PrintWriter writer = new PrintWriter( stream, true );
    PrintStream pStream = new PrintStream( stream, true );

    PentahoChainedException exception = new PentahoChainedException( CAUSE );
    exception.printStackTrace( writer );
    assertNotEquals( "", stream.toString() );

    exception.printStackTrace( pStream );
    assertNotEquals( "", pStream.toString() );
  }
}
