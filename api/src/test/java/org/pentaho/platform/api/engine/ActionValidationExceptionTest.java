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

import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Constructor;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pentaho.actionsequence.dom.IActionDefinition;

public class ActionValidationExceptionTest {
  @Test
  public void testExceptionConstructors() {
    try {
      Constructor<ActionValidationException> constructor = ActionValidationException.class.getDeclaredConstructor( String.class, String.class );
      constructor.setAccessible( true );
      constructor.newInstance( "testMsg", "componentName" );
    } catch ( Exception e ) {
      fail( ActionValidationException.class.getSimpleName() + " Does not have a constructor with String, String parameters" );
    }

    try {
      Constructor<ActionValidationException> constructor = ActionValidationException.class.getDeclaredConstructor( String.class, Throwable.class, String.class );
      constructor.setAccessible( true );
      constructor.newInstance( "testMsg", new Exception( "cause" ), "componentName" );
    } catch ( Exception e ) {
      fail( ActionValidationException.class.getSimpleName() + " Does not have a constructor with String, Throwable, String parameters" );
    }

    try {
      Constructor<ActionValidationException> constructor = ActionValidationException.class.getDeclaredConstructor( String.class, Throwable.class, String.class, String.class, String.class, String.class, String.class );
      constructor.setAccessible( true );
      constructor.newInstance( "testMsg", new Exception( "cause" ), "sessionName", "instanceId", "actionSequenceName", "actionDescription", "componentName" );
    } catch ( Exception e ) {
      fail( ActionValidationException.class.getSimpleName() + " Does not have a constructor with String, Throwable, String, String, String, String, String parameters" );
    }

    try {
      Constructor<ActionValidationException> constructor = ActionValidationException.class.getDeclaredConstructor( String.class );
      constructor.setAccessible( true );
      constructor.newInstance( "testMsg" );
    } catch ( Exception e ) {
      fail( ActionValidationException.class.getSimpleName() + " Does not have a constructor with String parameter" );
    }

    try {
      Constructor<ActionValidationException> constructor = ActionValidationException.class.getDeclaredConstructor( Throwable.class );
      constructor.setAccessible( true );
      constructor.newInstance( new Exception( "cause" ) );
    } catch ( Exception e ) {
      fail( ActionValidationException.class.getSimpleName() + " Does not have a constructor with Throwable parameter" );
    }

    try {
      Constructor<ActionValidationException> constructor = ActionValidationException.class.getDeclaredConstructor( String.class, Throwable.class );
      constructor.setAccessible( true );
      constructor.newInstance( "testMsg", new Exception( "cause" ) );
    } catch ( Exception e ) {
      fail( ActionValidationException.class.getSimpleName() + " Does not have a constructor with String and Throwable parameter" );
    }

    IActionDefinition actionDef = Mockito.mock( IActionDefinition.class );

    try {
      Constructor<ActionValidationException> constructor = ActionValidationException.class
            .getDeclaredConstructor( String.class, Throwable.class, String.class, String.class, String.class,
                IActionDefinition.class );
      constructor.setAccessible( true );
      constructor.newInstance( "testMsg", new Exception( "cause" ), "sessionName", "instanceId", "actionSequenceName",
            actionDef );
    } catch ( Exception e ) {
      fail( ActionValidationException.class.getSimpleName()
            + " Does not have a constructor with String, Throwable, String, String, String, IActionDefinition " );
    }

    try {
      Constructor<ActionValidationException> constructor = ActionValidationException.class
            .getDeclaredConstructor( String.class, String.class, String.class, String.class,
                IActionDefinition.class );
      constructor.setAccessible( true );
      constructor.newInstance( "testMsg", "sessionName", "instanceId", "actionSequenceName", actionDef );
    } catch ( Exception e ) {
      fail( ActionValidationException.class.getSimpleName()
            + " Does not have a constructor with String, String, String, String, IActionDefinition " );
    }
  }
}
