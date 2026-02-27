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


package org.pentaho.commons.util.repository.exception;

import static org.junit.Assert.*;
import org.junit.Test;

public class CmisFaultTest {
  class TestClass extends CmisFault {
    private static final long serialVersionUID = 1369015378927004363L;

    TestClass() {
      super();
    }
  }

  @Test
  public void test() {
    CmisFault fault = new TestClass();
    fault.setErrorCode( 0 );
    assertEquals( 0, fault.getErrorCode() );

    fault.setErrorMessage( "Some Error Message" );
    assertNotEquals( "", fault.getErrorMessage() );
  }
}
