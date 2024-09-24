/*!
 *
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
 *
 * Copyright (c) 2002-2024 Hitachi Vantara. All rights reserved.
 *
 */

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
