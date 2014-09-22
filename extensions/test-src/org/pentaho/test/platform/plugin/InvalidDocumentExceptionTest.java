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

import org.pentaho.platform.plugin.action.mondrian.InvalidDocumentException;
import org.pentaho.test.platform.engine.core.BaseTest;

@SuppressWarnings( "nls" )
public class InvalidDocumentExceptionTest extends BaseTest {

  private static final String SOLUTION_PATH = "test-src/solution";

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
    InvalidDocumentExceptionTest test = new InvalidDocumentExceptionTest();
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
