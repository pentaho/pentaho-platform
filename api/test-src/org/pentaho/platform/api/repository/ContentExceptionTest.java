/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2016 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.api.repository;

import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;

import org.junit.Test;

public class ContentExceptionTest {

  @Test
  public void hasValidExceptionConstructors() {

    try {
      Constructor<ContentException> constructor = ContentException.class.getDeclaredConstructor( String.class );
      constructor.setAccessible( true );
      constructor.newInstance( "testMsg" );
    } catch ( Exception e ) {
      fail( ContentException.class.getSimpleName() + " Does not have a constructor with String parameter" );
    }

    try {
      Constructor<ContentException> constructor = ContentException.class.getDeclaredConstructor( Throwable.class );
      constructor.setAccessible( true );
      constructor.newInstance( new Exception( "cause" ) );
    } catch ( Exception e ) {
      fail( ContentException.class.getSimpleName() + " Does not have a constructor with Throwable parameter" );
    }

    try {
      Constructor<ContentException> constructor = ContentException.class.getDeclaredConstructor( String.class, Throwable.class );
      constructor.setAccessible( true );
      constructor.newInstance( "testMsg", new Exception( "cause" ) );
    } catch ( Exception e ) {
      fail( ContentException.class.getSimpleName() + " Does not have a constructor with String and Throwable parameter" );
    }
  }
}
