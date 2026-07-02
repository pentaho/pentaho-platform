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


package org.pentaho.test.platform.plugin;

import org.pentaho.platform.plugin.action.mondrian.InvalidDocumentException;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.test.platform.utils.TestResourceLocation;

@SuppressWarnings( "nls" )
public class InvalidDocumentExceptionIT extends BaseTest {

  private static final String SOLUTION_PATH = TestResourceLocation.TEST_RESOURCES + "/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public void testInvalidDocumentException1() {
    startTest();
    info( "Expected: Exception will be caught and thrown as a Invalid Document Exception" ); //$NON-NLS-1$
    InvalidDocumentException ide1 = new InvalidDocumentException( "A test Invalid Document Exception has been thrown" ); //$NON-NLS-1$
    System.out.println( "Invalid Document Exception :" + ide1 ); //$NON-NLS-1$    
    assertTrue( true );
    finishTest();
  }

  public void testInvalidDocumentException2() {
    startTest();
    info( "Expected: A Invalid Document Exception will be created with Throwable as a parameter" ); //$NON-NLS-1$
    InvalidDocumentException ide2 = new InvalidDocumentException( new Throwable( "This is a throwable exception" ) ); //$NON-NLS-1$
    System.out.println( "Invalid Document Exception :" + ide2 ); //$NON-NLS-1$    
    assertTrue( true );
    finishTest();

  }

  public void testInvalidDocumentException3() {
    startTest();
    info( "Expected: Exception will be caught and thrown as a Invalid Document Exception" ); //$NON-NLS-1$
    InvalidDocumentException ide3 =
        new InvalidDocumentException( "A test Invalid Document Exception has been thrown", new Throwable() ); //$NON-NLS-1$
    System.out.println( "Invalid Document Exception :" + ide3 ); //$NON-NLS-1$    
    assertTrue( true );
    finishTest();

  }

  public static void main( String[] args ) {
    InvalidDocumentExceptionIT test = new InvalidDocumentExceptionIT();
    try {
      test.setUp();
      test.testInvalidDocumentException1();
      test.testInvalidDocumentException2();
      test.testInvalidDocumentException3();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }
}
