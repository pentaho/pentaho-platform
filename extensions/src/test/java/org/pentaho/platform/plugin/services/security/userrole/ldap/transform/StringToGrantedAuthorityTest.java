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
