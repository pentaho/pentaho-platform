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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import junit.framework.TestCase;

import org.pentaho.platform.util.xml.w3c.XmlW3CHelper;
import org.w3c.dom.Document;

public class XmlW3CHelperTest extends TestCase {

  public void testValidXmlW3C() throws FileNotFoundException, IOException {
    String domString = "<root><subroot>this is sub root</subroot></root>";//$NON-NLS-1$

    Document doc = XmlW3CHelper.getDomFromString( domString );
    assertNotNull( doc );
  }

  public void testNotValidXmlW3C() throws FileNotFoundException, IOException {
    String domString = "<root>this is sub root</subroot></root>";//$NON-NLS-1$

    Document doc = XmlW3CHelper.getDomFromString( domString );
    assertNull( doc );
  }

  public void testXmlW3C() throws FileNotFoundException, IOException {
    String path = "test-res/solution/test/xml/query_without_connection.xaction"; //$NON-NLS-1$

    byte[] encoded = Files.readAllBytes( Paths.get( path ) );
    String sourceXml = new String( encoded );

    Document doc = XmlW3CHelper.getDomFromString( sourceXml );
    assertNotNull( doc );
  }

  public void testXmlW3CError() {
    try {
      XmlW3CHelper.getDomFromString( null );
      fail( "This should have thrown an exception" );
    } catch ( IllegalArgumentException e ) {
      assertEquals( "The source string can not be null", e.getMessage() );
    }
  }

}
