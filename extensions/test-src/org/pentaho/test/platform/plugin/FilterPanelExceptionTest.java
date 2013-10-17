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

package org.pentaho.test.platform.plugin;

import org.pentaho.platform.uifoundation.component.xml.FilterPanelException;
import org.pentaho.test.platform.engine.core.BaseTest;

public class FilterPanelExceptionTest extends BaseTest {

  public String getSolutionPath() {
    return "test-src/solution";
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
    FilterPanelExceptionTest test = new FilterPanelExceptionTest();
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
