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


package org.pentaho.platform.api.repository;

import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Constructor;

import org.junit.jupiter.api.Test;

public class ContentExceptionTest {

  @Test
  public void hasValidExceptionConstructors() {

    try {
      Constructor<ContentException> constructor = ContentException.class.getDeclaredConstructor( String.class );
      constructor.setAccessible( true );
      constructor.newInstance( "testMsg" );
    } catch ( Exception e ) {
      fail( ContentException.class.getSimpleName() + " Does not have a constructor with String parameter" );
    }

    try {
      Constructor<ContentException> constructor = ContentException.class.getDeclaredConstructor( Throwable.class );
      constructor.setAccessible( true );
      constructor.newInstance( new Exception( "cause" ) );
    } catch ( Exception e ) {
      fail( ContentException.class.getSimpleName() + " Does not have a constructor with Throwable parameter" );
    }

    try {
      Constructor<ContentException> constructor = ContentException.class.getDeclaredConstructor( String.class, Throwable.class );
      constructor.setAccessible( true );
      constructor.newInstance( "testMsg", new Exception( "cause" ) );
    } catch ( Exception e ) {
      fail( ContentException.class.getSimpleName() + " Does not have a constructor with String and Throwable parameter" );
    }
  }
}
