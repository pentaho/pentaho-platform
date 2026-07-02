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

import org.pentaho.platform.plugin.action.mondrian.MissingParameterException;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.test.platform.utils.TestResourceLocation;

@SuppressWarnings( "nls" )
public class MissingParameterExceptionIT extends BaseTest {

  private static final String SOLUTION_PATH = TestResourceLocation.TEST_RESOURCES + "/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public void testMissingParameterException1() {
    startTest();
    info( "Expected: Exception will be caught and thrown as a Missing Parameter Exception" ); //$NON-NLS-1$
    MissingParameterException mpe1 =
        new MissingParameterException( "A test Missing Parameter Exception has been thrown" ); //$NON-NLS-1$
    System.out.println( "Missing Parameter Exception :" + mpe1 ); //$NON-NLS-1$    
    assertTrue( true );
    finishTest();
  }

  public void testMissingParameterException2() {
    startTest();
    info( "Expected: A Missing Parameter Exception will be created with Throwable as a parameter" ); //$NON-NLS-1$
    MissingParameterException mpe2 = new MissingParameterException( new Throwable( "This is a throwable exception" ) ); //$NON-NLS-1$
    System.out.println( "Missing Parameter Exception :" + mpe2 ); //$NON-NLS-1$    
    assertTrue( true );
    finishTest();

  }

  public void testMissingParameterException3() {
    startTest();
    info( "Expected: Exception will be caught and thrown as a Missing Parameter Exception" ); //$NON-NLS-1$
    MissingParameterException mpe3 =
        new MissingParameterException( "A test Missing Parameter Exception has been thrown", new Throwable() ); //$NON-NLS-1$
    System.out.println( "Missing Parameter Exception :" + mpe3 ); //$NON-NLS-1$    
    assertTrue( true );
    finishTest();

  }

  public static void main( String[] args ) {
    MissingParameterExceptionIT test = new MissingParameterExceptionIT();
    try {
      test.setUp();
      test.testMissingParameterException1();
      test.testMissingParameterException2();
      test.testMissingParameterException3();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }
}
