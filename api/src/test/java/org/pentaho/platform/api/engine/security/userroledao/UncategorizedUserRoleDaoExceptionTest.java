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

import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.fail;

public class UncategorizedUserRoleDaoExceptionTest {

  @Test
  public void test() {
    try {
      Constructor<UncategorizedUserRoleDaoException> constructor = UncategorizedUserRoleDaoException.class.getDeclaredConstructor( String.class, Throwable.class );
      constructor.setAccessible( true );
      constructor.newInstance( "msg", new Exception( "cause" ) );
    } catch ( Exception e ) {
      fail( UncategorizedUserRoleDaoException.class.getSimpleName() + " Does not have a constructor with String, Throwable " );
    }

    try {
      Constructor<UncategorizedUserRoleDaoException> constructor = UncategorizedUserRoleDaoException.class.getDeclaredConstructor( String.class );
      constructor.setAccessible( true );
      constructor.newInstance( "msg" );
    } catch ( Exception e ) {
      fail( UncategorizedUserRoleDaoException.class.getSimpleName() + " Does not have a constructor with String " );
    }
  }
}
