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
import org.pentaho.platform.util.xml.w3c.XmlW3CHelper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class XmlW3CHelperTest extends TestCase {

  public void testXmlW3C() throws FileNotFoundException, IOException {
    String path = "test-res/solution/test/xml/query_without_connection.xaction"; //$NON-NLS-1$
    //    String domString = "<root><subroot>this is sub root</subroot></root>";//$NON-NLS-1$

    InputStream in = null;

    try {
      in = new FileInputStream( path );

      byte[] bytes = new byte[10000];
      in.read( bytes );
    } catch ( Exception e ) {
      // should not get here
      Assert.assertTrue( e.getMessage(), false );
    } finally {
      if ( in != null ) {
        in.close();
      }
    }

    // This test doesn't work - needs to be fixed (but it makes no sense as is)
    // Document doc = XmlW3CHelper.getDomFromString(domString);
    // System.out.println(doc.toString());
    // Document doc2 = XmlW3CHelper.getDomFromString(domString);
    // String str = doc2.toString();
    // Assert.assertEquals(str, domString);
  }

  public void testXmlW3CError() {
    try {
      XmlW3CHelper.getDomFromString( null );
      Assert.assertTrue( true );
    } catch ( Exception expected ) {
      //ignored
    }
  }
}
