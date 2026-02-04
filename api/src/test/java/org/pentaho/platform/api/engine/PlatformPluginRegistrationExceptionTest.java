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

public class PlatformPluginRegistrationExceptionTest {

  @Test
  public void hasValidExceptionConstructors() {

    try {
      Constructor<PlatformPluginRegistrationException> constructor = PlatformPluginRegistrationException.class.getDeclaredConstructor( String.class, Throwable.class );
      constructor.setAccessible( true );
      constructor.newInstance( "testMsg", new Exception( "cause" ) );
    } catch ( Exception e ) {
      fail( ContentException.class.getSimpleName() + " Does not have a constructor with String and Throwable parameter" );
    }

    try {
      Constructor<PlatformPluginRegistrationException> constructor = PlatformPluginRegistrationException.class.getDeclaredConstructor( String.class );
      constructor.setAccessible( true );
      constructor.newInstance( "testMsg" );
    } catch ( Exception e ) {
      fail( PlatformPluginRegistrationException.class.getSimpleName() + " Does not have a constructor with String parameter" );
    }

  }
}
