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


package org.pentaho.platform.api.engine;

import org.junit.jupiter.api.Test;
import org.pentaho.platform.api.repository.ContentException;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.fail;

public class InvalidParameterExceptionTest {

  @Test
  public void testInvalidParameterException() {
    try {
      Constructor<InvalidParameterException> constructor = InvalidParameterException.class.getDeclaredConstructor();
      constructor.setAccessible( true );
      constructor.newInstance();
    } catch ( Exception e ) {
      fail( InvalidParameterException.class.getSimpleName() + " Does not have a no args constructor" );
    }

    try {
      Constructor<InvalidParameterException> constructor = InvalidParameterException.class.getDeclaredConstructor( String.class );
      constructor.setAccessible( true );
      constructor.newInstance( "testMsg" );
    } catch ( Exception e ) {
      fail( ContentException.class.getSimpleName() + " Does not have a constructor with String parameter" );
    }
  }
}
