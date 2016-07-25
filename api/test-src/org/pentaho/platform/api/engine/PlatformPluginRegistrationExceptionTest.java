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
import org.pentaho.platform.api.repository.ContentException;

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
