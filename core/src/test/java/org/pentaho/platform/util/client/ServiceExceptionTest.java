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


package org.pentaho.platform.util.client;

import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;

import org.junit.Test;

public class ServiceExceptionTest {

  @Test
  public void test() {
    try {
      Constructor<ServiceException> constructor = ServiceException.class.getDeclaredConstructor( Exception.class );
      constructor.setAccessible( true );
      constructor.newInstance( new Exception( "cause" ) );
    } catch ( Exception e ) {
      fail( ServiceException.class.getSimpleName() + " Does not have a constructor with Exception " );
    }

    try {
      Constructor<ServiceException> constructor = ServiceException.class.getDeclaredConstructor( String.class );
      constructor.setAccessible( true );
      constructor.newInstance( "msg" );
    } catch ( Exception e ) {
      fail( ServiceException.class.getSimpleName() + " Does not have a constructor with String " );
    }
  }
}
