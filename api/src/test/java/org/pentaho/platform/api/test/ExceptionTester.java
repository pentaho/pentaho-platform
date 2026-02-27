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


package org.pentaho.platform.api.test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Created by bgroves on 11/6/15.
 */
public class ExceptionTester {

  public static void hasValidExceptionConstructors( Class exceptionClass ) {
    try {
      Constructor constructor = exceptionClass.getDeclaredConstructor();
      constructor.setAccessible( true );
      constructor.newInstance();
    } catch ( Exception e ) {
      fail( exceptionClass.getSimpleName() + " Does not have a no args constructor" );
    }

    try {
      Constructor constructor = exceptionClass.getDeclaredConstructor( String.class );
      constructor.setAccessible( true );
      constructor.newInstance( "testMsg" );
    } catch ( Exception e ) {
      fail( exceptionClass.getSimpleName() + " Does not have a constructor with String parameter" );
    }

    try {
      Constructor constructor = exceptionClass.getDeclaredConstructor( Throwable.class );
      constructor.setAccessible( true );
      constructor.newInstance( new Exception( "cause" ) );
    } catch ( Exception e ) {
      fail( exceptionClass.getSimpleName() + " Does not have a constructor with Throwable parameter" );
    }

    try {
      Constructor constructor = exceptionClass.getDeclaredConstructor( String.class, Throwable.class );
      constructor.setAccessible( true );
      constructor.newInstance( "testMsg", new Exception( "cause" ) );
    } catch ( Exception e ) {
      fail( exceptionClass.getSimpleName() + " Does not have a constructor with String and Throwable parameter" );
    }
  }
}
