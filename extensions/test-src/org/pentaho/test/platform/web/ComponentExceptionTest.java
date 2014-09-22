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

package org.pentaho.test.platform.web;

import org.pentaho.platform.api.engine.ComponentException;
import org.pentaho.test.platform.engine.core.BaseTest;

import java.io.File;

@SuppressWarnings( "nls" )
public class ComponentExceptionTest extends BaseTest {

  private static final String SOLUTION_PATH = "test-src/web-solution";

  private static final String ALT_SOLUTION_PATH = "test-src/web-solution";

  private static final String PENTAHO_XML_PATH = "/system/pentaho.xml";

  public String getSolutionPath() {
    File file = new File( SOLUTION_PATH + PENTAHO_XML_PATH );
    if ( file.exists() ) {
      return SOLUTION_PATH;
    } else {
      return ALT_SOLUTION_PATH;
    }
  }

  public void testComponentException1() {
    startTest();
    info( "Expected: Exception will be caught and thrown as a Component Exception" ); //$NON-NLS-1$
    ComponentException ce = new ComponentException();
    System.out.println( "ComponentException :" + ce ); //$NON-NLS-1$
    assertTrue( true );
    finishTest();

  }

  public void testComponentException2() {
    startTest();
    info( "Expected: Exception will be caught and thrown as a Component Exception" ); //$NON-NLS-1$
    ComponentException ce1 = new ComponentException( "A test Component Exception has been thrown" ); //$NON-NLS-1$
    System.out.println( "ComponentException :" + ce1 ); //$NON-NLS-1$    
    assertTrue( true );
    finishTest();
  }

  public void testComponentException3() {
    startTest();
    info( "Expected: A Component Exception will be created with Throwable as a parameter" ); //$NON-NLS-1$
    ComponentException ce2 = new ComponentException( new Throwable( "This is a throwable exception" ) ); //$NON-NLS-1$
    System.out.println( "ComponentException" + ce2 ); //$NON-NLS-1$    
    assertTrue( true );
    finishTest();

  }

  public void testComponentException4() {
    startTest();
    info( "Expected: Exception will be caught and thrown as a Component Exception" ); //$NON-NLS-1$
    ComponentException ce3 = new ComponentException( "A test UI Exception has been thrown", new Throwable() ); //$NON-NLS-1$
    System.out.println( "ComponentException :" + ce3 ); //$NON-NLS-1$    
    assertTrue( true );
    finishTest();

  }

  public static void main( String[] args ) {
    ComponentExceptionTest test = new ComponentExceptionTest();
    try {
      test.setUp();
      test.testComponentException1();
      test.testComponentException2();
      test.testComponentException3();
      test.testComponentException4();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }
}
