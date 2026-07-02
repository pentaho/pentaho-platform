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

import org.pentaho.platform.uifoundation.component.xml.FilterPanelException;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.test.platform.utils.TestResourceLocation;

public class FilterPanelExceptionIT extends BaseTest {

  public String getSolutionPath() {
    return TestResourceLocation.TEST_RESOURCES + "/solution";
  }

  public void testFilterPanelException1() {
    startTest();
    info( "Expected: Exception will be caught and thrown as a FilterPanel Exception" ); //$NON-NLS-1$
    FilterPanelException fpe = new FilterPanelException();
    System.out.println( "FilterPanelException :" + fpe ); //$NON-NLS-1$
    assertTrue( true );
    finishTest();
  }

  public void testFilterPanelException2() {
    startTest();
    info( "Expected: Exception will be caught and thrown as a FilterPanel Exception" ); //$NON-NLS-1$
    FilterPanelException fpe1 = new FilterPanelException( "A test FilterPanel Exception has been thrown" ); //$NON-NLS-1$
    System.out.println( "FilterPanelException :" + fpe1 ); //$NON-NLS-1$    
    assertTrue( true );
    finishTest();
  }

  public void testFilterPanelException3() {
    startTest();
    info( "Expected: A FilterPanel Exception will be created with Throwable as a parameter" ); //$NON-NLS-1$
    FilterPanelException fpe2 = new FilterPanelException( new Throwable( "This is a throwable exception" ) ); //$NON-NLS-1$
    System.out.println( "FilterPanelException :" + fpe2 ); //$NON-NLS-1$    
    assertTrue( true );
    finishTest();

  }

  public void testFilterPanelException4() {
    startTest();
    info( "Expected: Exception will be caught and thrown as a FilterPanel Exception" ); //$NON-NLS-1$
    FilterPanelException fpe3 =
        new FilterPanelException( "A test FilterPanel Exception has been thrown", new Throwable() ); //$NON-NLS-1$
    System.out.println( "FilterPanelException :" + fpe3 ); //$NON-NLS-1$    
    assertTrue( true );
    finishTest();

  }

  public static void main( String[] args ) {
    FilterPanelExceptionIT test = new FilterPanelExceptionIT();
    try {
      test.setUp();
      test.testFilterPanelException1();
      test.testFilterPanelException2();
      test.testFilterPanelException3();
      test.testFilterPanelException4();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }
}
