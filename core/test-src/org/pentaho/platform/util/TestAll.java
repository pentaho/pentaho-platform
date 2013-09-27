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

package org.pentaho.platform.util;

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
    TestSuite suite = new TestSuite( "Platform Util" ); //$NON-NLS-1$

    suite.addTestSuite( CleanXmlHelperTest.class );
    suite.addTestSuite( ColorHelperTest.class );
    suite.addTestSuite( DateMathTest.class );
    suite.addTestSuite( InputStreamDataSourceTest.class );
    suite.addTestSuite( LocaleHelperTest.class );
    suite.addTestSuite( LogTest.class );
    suite.addTestSuite( MessageUtilTest.class );
    suite.addTestSuite( ParameterHelperTest.class );
    suite.addTestSuite( PasswordServiceTest.class );
    suite.addTestSuite( PentahoSystemExceptionTest.class );
    suite.addTestSuite( StringUtilTest.class );
    suite.addTestSuite( VersionHelperTest.class );
    suite.addTestSuite( XmlHelperTest.class );
    suite.addTestSuite( XmlW3CHelperTest.class );

    return suite;
  }

  public static void main( final String[] args ) {
    TestRunner.run( TestAll.testAll() );
  }
}
