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

package org.pentaho.commons.util.repository.exception;

import static org.junit.Assert.*;
import org.junit.Test;

public class CmisFaultTest {
  class TestClass extends CmisFault {
    private static final long serialVersionUID = 1369015378927004363L;

    TestClass() {
      super();
    }
  }

  @Test
  public void test() {
    CmisFault fault = new TestClass();
    fault.setErrorCode( 0 );
    assertEquals( 0, fault.getErrorCode() );

    fault.setErrorMessage( "Some Error Message" );
    assertNotEquals( "", fault.getErrorMessage() );
  }
}
