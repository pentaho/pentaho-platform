/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.api.engine;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pentaho.actionsequence.dom.IActionDefinition;

import static org.junit.jupiter.api.Assertions.fail;
import static org.pentaho.platform.api.test.ExceptionTester.hasValidExceptionConstructors;

import java.lang.reflect.Constructor;

/**
 * Created by bgroves on 11/9/15.
 */
public class ActionInitializationExceptionTest {

  @Test
  public void testExceptionClasses() {
    hasValidExceptionConstructors( ActionInitializationException.class );
  }

  @Test
  public void testCustomizedConstructors() {
    IActionDefinition actionDef = Mockito.mock( IActionDefinition.class );

    try {
      Constructor<ActionInitializationException> constructor = ActionInitializationException.class.getDeclaredConstructor( String.class, Throwable.class, String.class, String.class,
              String.class, IActionDefinition.class );
      constructor.setAccessible( true );
      constructor.newInstance( "msg", new Exception( "cause" ), "sessionName", "instanceId", "actionSequenceName", actionDef );
    } catch ( Exception e ) {
      fail( ActionInitializationException.class.getSimpleName() + " Does not have a constructor with String, Throwable, String, String, String, IActionDefinition " );
    }

    try {
      Constructor<ActionInitializationException> constructor = ActionInitializationException.class.getDeclaredConstructor( String.class, String.class, String.class,
              String.class, IActionDefinition.class );
      constructor.setAccessible( true );
      constructor.newInstance( "msg", "sessionName", "instanceId", "actionSequenceName", actionDef  );
    } catch ( Exception e ) {
      fail( ActionInitializationException.class.getSimpleName() + " Does not have a constructor with String, String, String, String, IActionDefinition " );
    }
  }

}
