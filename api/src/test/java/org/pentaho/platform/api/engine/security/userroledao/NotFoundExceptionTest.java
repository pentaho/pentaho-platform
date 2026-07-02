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


package org.pentaho.platform.api.engine.security.userroledao;

import static org.junit.jupiter.api.Assertions.fail;
import java.lang.reflect.Constructor;
import org.junit.jupiter.api.Test;

public class NotFoundExceptionTest {

  @Test
  public void test() {
    try {
      Constructor<NotFoundException> constructor = NotFoundException.class.getDeclaredConstructor( String.class, Throwable.class );
      constructor.setAccessible( true );
      constructor.newInstance( "msg", new Exception( "cause" ) );
    } catch ( Exception e ) {
      fail( NotFoundException.class.getSimpleName() + " Does not have a constructor with String, Throwable " );
    }

    try {
      Constructor<NotFoundException> constructor = NotFoundException.class.getDeclaredConstructor( String.class );
      constructor.setAccessible( true );
      constructor.newInstance( "msg" );
    } catch ( Exception e ) {
      fail( NotFoundException.class.getSimpleName() + " Does not have a constructor with String " );
    }
  }
}
