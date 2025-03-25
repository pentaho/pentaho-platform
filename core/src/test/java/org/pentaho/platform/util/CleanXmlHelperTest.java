/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.util;

import java.io.FileInputStream;
import java.io.InputStream;

import org.pentaho.platform.util.xml.XmlHelper;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

import junit.framework.Assert;
import junit.framework.TestCase;

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

      InputStream in = new FileInputStream( "src/test/resources/solution/test/xml/query_without_connection.xaction" ); //$NON-NLS-1$
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
