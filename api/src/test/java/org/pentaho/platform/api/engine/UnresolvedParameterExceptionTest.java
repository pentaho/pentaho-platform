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
import org.mockito.Mockito;
import org.pentaho.actionsequence.dom.IActionDefinition;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class UnresolvedParameterExceptionTest {
  @Test
  public void testUnresolvedParameterException() {
    try {
      Constructor<UnresolvedParameterException> constructor = UnresolvedParameterException.class.getDeclaredConstructor();
      constructor.setAccessible( true );
      constructor.newInstance();
    } catch ( Exception e ) {
      fail( UnresolvedParameterException.class.getSimpleName() + " Does not have a no args constructor" );
    }
  }

  @Test
  public void testUnresolvedParameterExceptionString() {
    try {
      Constructor<UnresolvedParameterException> constructor = UnresolvedParameterException.class.getDeclaredConstructor( String.class );
      constructor.setAccessible( true );
      constructor.newInstance( "testMsg" );
    } catch ( Exception e ) {
      fail( UnresolvedParameterException.class.getSimpleName() + " Does not have a constructor with String parameter" );
    }
  }

  @Test
  public void testUnresolvedParameterExceptionStringString() {
    try {
      Constructor<UnresolvedParameterException> constructor = UnresolvedParameterException.class.getDeclaredConstructor( String.class, String.class );
      constructor.setAccessible( true );
      constructor.newInstance( "testMsg", "parameterName" );
    } catch ( Exception e ) {
      fail( UnresolvedParameterException.class.getSimpleName() + " Does not have a constructor with String, String parameter" );
    }
  }

  @Test
  public void testUnresolvedParameterExceptionStringThrowableStringStringStringIActionDefinition() {
    IActionDefinition actionDef = Mockito.mock( IActionDefinition.class );

    try {
      Constructor<UnresolvedParameterException> constructor = UnresolvedParameterException.class.getDeclaredConstructor( String.class, String.class, String.class,
              String.class, IActionDefinition.class );
      constructor.setAccessible( true );
      constructor.newInstance( "msg", "sessionName", "instanceId", "actionSequenceName", actionDef );
    } catch ( Exception e ) {
      fail( UnresolvedParameterException.class.getSimpleName() + " Does not have a constructor with String, String, String, String, IActionDefinition " );
    }

  }

  @Test
  public void testUnresolvedParameterExceptionStringStringStringStringIActionDefinition() {
    IActionDefinition actionDef = Mockito.mock( IActionDefinition.class );

    try {
      Constructor<UnresolvedParameterException> constructor = UnresolvedParameterException.class.getDeclaredConstructor( String.class, Throwable.class, String.class, String.class,
              String.class, IActionDefinition.class );
      constructor.setAccessible( true );
      constructor.newInstance( "msg", new Exception( "cause" ), "sessionName", "instanceId", "actionSequenceName", actionDef );
    } catch ( Exception e ) {
      fail( UnresolvedParameterException.class.getSimpleName() + " Does not have a constructor with String, Throwable, String, String, String, IActionDefinition " );
    }

  }

  @Test
  public void testGetParameterName() {
    UnresolvedParameterException ex = new UnresolvedParameterException( "testMsg", "testParameterName" );
    assertEquals( "testParameterName", ex.getParameterName() );

  }

  @Test
  public void testSetParameterName() {
    UnresolvedParameterException ex = new UnresolvedParameterException();
    ex.setParameterName( "testParameterName" );
    assertEquals( "testParameterName", ex.getParameterName() );
  }

}
