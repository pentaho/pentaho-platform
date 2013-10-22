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

package org.pentaho.platform.util;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.pentaho.commons.connection.InputStreamDataSource;

import java.io.InputStream;

public class InputStreamDataSourceTest extends TestCase {

  public void testInputStreamDataSource() {
    try {
      InputStream inputStream = getClass().getResourceAsStream( "/test/xml/query_without_connection.xaction" ); //$NON-NLS-1$"test/xml/departments.rule.xaction")); 
      String name = "mystream"; //$NON-NLS-1$
      InputStreamDataSource is = new InputStreamDataSource( name, inputStream );

      Assert.assertEquals( inputStream, is.getInputStream() );
      Assert.assertEquals( name, is.getName() );
    } catch ( Exception e ) {
      e.printStackTrace();
    }

  }
  /*
   * public static void main(String[] args) { InputStreamDataSourceTest test = new InputStreamDataSourceTest();
   * test.testInputStreamDataSource(); try {
   * 
   * } finally { } }
   */
}
