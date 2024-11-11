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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

import org.junit.jupiter.api.Test;

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
