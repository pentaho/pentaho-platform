/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.core;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class TestAll {

  /**
   * 
   */
  public static Test suite() {
    TestSuite suite = new TestSuite();
    return suite;
  }

  public static TestSuite testAll() {
    TestSuite suite = new TestSuite( "BI Engine Test suite" ); //$NON-NLS-1$

    // suite.addTestSuite(SettingsParameterProviderTest.class);
    // suite.addTestSuite(SimpleParameterProviderTest.class);
    suite.addTestSuite( SimpleUrlTest.class );

    return suite;
  }

  public static void main( final String[] args ) {
    TestRunner.run( TestAll.testAll() );
  }
}
