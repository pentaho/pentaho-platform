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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.test.platform.plugin.services.webservices;

public class StubService {

  public static String str;
  public static boolean getStringCalled = false;
  public static boolean setStringCalled = false;
  public static boolean throwsError1Called = false;
  public static boolean throwsError2Called = false;

  public void setString( String str ) {
    setStringCalled = true;
    StubService.str = str;
  }

  public ComplexType getDetails( ComplexType object ) {
    object.setAddress( "test address" ); //$NON-NLS-1$
    object.setAge( 44 );
    return object;
  }

  public String getString() {
    getStringCalled = true;
    return "test result"; //$NON-NLS-1$
  }

  public void throwsError1() {
    throwsError1Called = true;
    throw new RuntimeException( "test error 1" ); //$NON-NLS-1$
  }

  public String throwsError2() {
    throwsError2Called = true;
    throw new RuntimeException( "test error 2" ); //$NON-NLS-1$
  }

}
