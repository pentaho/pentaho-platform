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
