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

import junit.framework.Assert;
import junit.framework.TestCase;
import org.pentaho.platform.util.xml.XmlHelper;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

import java.io.FileInputStream;
import java.io.InputStream;

public class CleanXmlHelperTest extends TestCase {

  public void testGetEncoding() {
    // these should succeed, and cause the specified (windows-1252) encoding to
    // be returned
    String[] winXmls = { "<?xml version=\"1.0\" encoding=\"windows-1252\"?><root></root>", //$NON-NLS-1$
      "<?xml version=\"1.0\" encoding=\"windows-1252\"?><root></root>", //$NON-NLS-1$
      "<?xml version='1.0' encoding=\"windows-1252\"?><root></root>", //$NON-NLS-1$
      "<?xml version=\"1.0\" encoding='windows-1252'?><root></root>", //$NON-NLS-1$
      "<?xml version='1.0' encoding='windows-1252'?><root></root>" //$NON-NLS-1$
    };

    // these should fail, and cause the default system encoding to be returned
    String[] defaultXmls = { "<?xml version='1.0'><root></root>", //$NON-NLS-1$
      "<?xml version='1.0' encoding='windows-1252\"?><root></root>", //$NON-NLS-1$
      "<?xml version='1.0' encoding=\"windows-1252'?><root></root>", //$NON-NLS-1$
      "<?xml version=\"1.0\"?><root></root>", //$NON-NLS-1$
      "bart simpson was here", //$NON-NLS-1$
      "<root>encoding=bad</root>" }; //$NON-NLS-1$

    try {

      InputStream in = new FileInputStream( "test-res/solution/test/xml/query_without_connection.xaction" ); //$NON-NLS-1$
      XmlDom4JHelper.getDocFromStream( in );
      Assert.assertTrue( true );
    } catch ( Exception e ) {
      e.printStackTrace();
      Assert.assertTrue( "Shouldn't have thrown exception here", false ); //$NON-NLS-1$      
    }

    for ( String element : winXmls ) {
      String enc = XmlHelper.getEncoding( element );
      System.out.println( "xml: " + element + " enc: " + enc ); //$NON-NLS-1$ //$NON-NLS-2$
      Assert.assertTrue( enc.equals( "windows-1252" ) ); //$NON-NLS-1$
    }

    for ( String element : winXmls ) {
      try {
        XmlDom4JHelper.getDocFromString( element, null );
      } catch ( Exception ex ) {
        Assert.assertTrue( ex.getMessage(), false );
      }
    }

    for ( String element : defaultXmls ) {
      String enc = XmlHelper.getEncoding( element );
      System.out.println( "xml encoding: " + element + " enc: " + enc ); //$NON-NLS-1$ //$NON-NLS-2$
      Assert.assertTrue( enc == null );
    }

  }
}
