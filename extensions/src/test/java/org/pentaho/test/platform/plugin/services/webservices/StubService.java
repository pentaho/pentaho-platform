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
