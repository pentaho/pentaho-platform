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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.security.userrole.ldap.transform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedList;

import org.junit.Assert;
import org.junit.Test;

public class StringToGrantedAuthorityTest {

  @Test
  public void test() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

    Field rolePrefix = StringToGrantedAuthority.class.getDeclaredField( "rolePrefix" );
    rolePrefix.setAccessible( true );

    Field convertToUpperCase = StringToGrantedAuthority.class.getDeclaredField( "convertToUpperCase" );
    convertToUpperCase.setAccessible( true );

    StringToGrantedAuthority test = new StringToGrantedAuthority();

    assertEquals( "ROLE_", rolePrefix.get( test ) );
    test.setRolePrefix( "TESTROLE_" );
    assertNotEquals( "ROLE_", rolePrefix.get( test ) );

    assertEquals( true, convertToUpperCase.get( test ) );
    test.setConvertToUpperCase( false );
    assertEquals( false, convertToUpperCase.get( test ) );
  }

  @Test
  public void testTransform() {
    StringToGrantedAuthority test = new StringToGrantedAuthority();
    test.setConvertToUpperCase( false );
    String admin = "admin";
    Object transformed = test.transform( admin );
    assertEquals( "ROLE_admin", transformed.toString() );

    test.setConvertToUpperCase( true );
    transformed = test.transform( admin );
    assertEquals( "ROLE_ADMIN", transformed.toString() );

    Integer zero = new Integer( 0 );
    transformed = test.transform( zero );
    assertEquals( zero, transformed );

    Object[] input = new Object[] { new Integer( 0 ), new Long( 1 ), new Character( 'a' ) };
    Object output = test.transform( input );
    Assert.assertArrayEquals( (Object[]) output, input );

    Collection<Object> inputCollection = new LinkedList<>();
    output = test.transform( inputCollection );
    assertTrue( ( (Collection) output ).isEmpty() );

    inputCollection.add( new String( "admin" ) );
    output = test.transform( inputCollection );

    assertTrue( ( (Collection) output ).size() == 1 );

    Object element = ( (Collection) output ).toArray()[0];
    assertEquals( "ROLE_ADMIN", element.toString() );
  }
}
