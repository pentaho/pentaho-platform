/*
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
 * Copyright 2007 - 2008 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.test.platform.util;

import java.io.InputStream;

import junit.framework.Assert;
import junit.framework.TestCase;

//import org.dom4j.Document;
import org.pentaho.platform.util.xml.XmlHelper;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

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

    Class resourceClass = this.getClass();

    try {

      InputStream in = resourceClass.getResourceAsStream("/test/xml/query_without_connection.xaction"); //$NON-NLS-1$
      XmlDom4JHelper.getDocFromStream(in);
      Assert.assertTrue(true);
    } catch (Exception e) {
      e.printStackTrace();
      Assert.assertTrue("Shouldn't have thrown exception here", false); //$NON-NLS-1$      
    }

    try {
      // JD - what is this testing?
      /*
      InputStream fis = resourceClass.getResourceAsStream("/test/xml/query_without_connection.xaction"); //$NON-NLS-1$
      Document doc = XmlDom4JHelper.getDocFromStream(fis);
       XmlDom4JHelper.saveDomToFile(doc, new File(PentahoSystem.getApplicationContext().getSolutionPath("test/analysis/copy_query_without_connection.xaction")), null); //$NON-NLS-1$
       XmlDom4JHelper.saveDomToFile(doc, PentahoSystem.getApplicationContext().getSolutionPath("test/analysis/copy1_query_without_connection.xaction"), null);//$NON-NLS-1$
       FileWriter writer = new FileWriter(PentahoSystem.getApplicationContext().getSolutionPath("test/analysis/copy2_query_without_connection.xaction")); //$NON-NLS-1$
       XmlDom4JHelper.saveDomToWriter(doc, writer);
       assertTrue("DOM is saved to a file", true); //$NON-NLS-1$
       */
    } catch (Exception ex) {
      // should throw exception
      System.out.println("should throw exception. " + ex.getMessage()); //$NON-NLS-1$
    }

    for (String element : winXmls) {
      String enc = XmlHelper.getEncoding(element);
      System.out.println("xml: " + element + " enc: " + enc); //$NON-NLS-1$ //$NON-NLS-2$
      Assert.assertTrue(enc.equals("windows-1252")); //$NON-NLS-1$
    }

    for (String element : winXmls) {
      try {
        XmlDom4JHelper.getDocFromString(element, null);
      } catch (Exception ex) {
        Assert.assertTrue(ex.getMessage(), false);
      }
    }

    for (String element : defaultXmls) {
      String enc = XmlHelper.getEncoding(element);
      System.out.println("xml encoding: " + element + " enc: " + enc); //$NON-NLS-1$ //$NON-NLS-2$
      Assert.assertTrue(enc == null);
    }

  }

  public static void main(final String[] args) {
    CleanXmlHelperTest test = new CleanXmlHelperTest();
    try {
      test.testGetEncoding();
    } finally {
    }
  }
}
