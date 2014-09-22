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

import org.pentaho.platform.api.ui.UIException;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.test.platform.engine.core.BaseTestCase;

public class UIExceptionTest extends BaseTestCase {
  private static final String SOLUTION_PATH = "test-src/web-servlet-solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public void testUIException1() {
    UIException uie = new UIException();
    System.out.println( "UIException :" + uie ); //$NON-NLS-1$
    assertTrue( true );
  }

  public void testUIException2() {
    UIException uie1 = new UIException( "A test UI Exception has been thrown" ); //$NON-NLS-1$
    System.out.println( "UIException :" + uie1 ); //$NON-NLS-1$    
    assertTrue( true );
  }

  public void testUIException3() {
    UIException uie2 = new UIException( new Throwable( "This is a throwable exception" ) ); //$NON-NLS-1$
    System.out.println( "UIException :" + uie2 ); //$NON-NLS-1$    
    assertTrue( true );
  }

  public void testUIException4() {
    UIException uie3 = new UIException( "A test UI Exception has been thrown", new Throwable() ); //$NON-NLS-1$
    System.out.println( "UIException :" + uie3 ); //$NON-NLS-1$    
    assertTrue( true );
  }

  public static void main( String[] args ) {
    UIExceptionTest test = new UIExceptionTest();
    try {
      test.testUIException1();
      test.testUIException2();
      test.testUIException3();
      test.testUIException4();
    } finally {
      BaseTest.shutdown();
    }
  }
}
