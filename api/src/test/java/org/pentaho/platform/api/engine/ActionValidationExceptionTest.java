/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.api.engine;

import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;

import org.junit.Test;
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
