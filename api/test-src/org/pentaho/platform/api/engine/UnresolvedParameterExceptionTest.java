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

import static org.junit.Assert.*;

import java.lang.reflect.Constructor;

import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.actionsequence.dom.IActionDefinition;

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
