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

import org.pentaho.platform.plugin.action.mondrian.MissingParameterException;
import org.pentaho.test.platform.engine.core.BaseTest;

@SuppressWarnings( "nls" )
public class MissingParameterExceptionTest extends BaseTest {

  private static final String SOLUTION_PATH = "test-src/solution";

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
    MissingParameterExceptionTest test = new MissingParameterExceptionTest();
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
