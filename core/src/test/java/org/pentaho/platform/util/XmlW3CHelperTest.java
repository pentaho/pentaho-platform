/*!
 *
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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.pentaho.platform.util.xml.w3c.XmlW3CHelper;
import org.w3c.dom.Document;

public class XmlW3CHelperTest {

  @Test
  public void testXmlW3CDTDsDisabling() throws IOException {
    String domString =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
            + "<!DOCTYPE foo [<!ENTITY xxe SYSTEM \"file:///etc/passwd\" >]>\n"
            + "<root xmlns=\"http://www.pentaho.com\">\n" + "</root>";
    Document doc = XmlW3CHelper.getDomFromString( domString );
    // should prevent XXE attack
    // factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
    // should be set
    assertNull( doc );
  }

  /**
   * attack example is from https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Processing
   *
   * @throws IOException
   */
  @Test
  public void testXmlW3CLocalResourceAccessing() throws IOException {
    String domString =
        "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" + " <!DOCTYPE foo [  \n" + "  <!ELEMENT foo ANY >\n"
            + "  <!ENTITY xxe SYSTEM \"file:///dev/random\" >]><foo>&xxe;</foo>";
    Document doc = XmlW3CHelper.getDomFromString( domString );
    // should prevent XXE attack
    // factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
    // should be set
    assertNull( doc );
  }

  /**
   * attack example is from https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Processing
   *
   * @throws IOException
   */
  @Test
  public void testXmlW3CDisclosingEtcPass() throws IOException {
    String domString =
        "  <?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" + " <!DOCTYPE foo [  \n" + "   <!ELEMENT foo ANY >\n"
            + "   <!ENTITY xxe SYSTEM \"file:///etc/passwd\" >]><foo>&xxe;</foo>";
    Document doc = XmlW3CHelper.getDomFromString( domString );
    assertNull( doc );
  }

  /**
   * attack example is from https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Processing
   *
   * @throws IOException
   */
  @Test
  public void testXmlW3CDisclosingEtcShadow() throws IOException {
    String domString =
        " <?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" + " <!DOCTYPE foo [  \n" + "   <!ELEMENT foo ANY >\n"
            + "   <!ENTITY xxe SYSTEM \"file:///etc/shadow\" >]><foo>&xxe;</foo>";
    Document doc = XmlW3CHelper.getDomFromString( domString );
    assertNull( doc );
  }

  /**
   * attack example is from https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Processing
   *
   * @throws IOException
   */
  @Test
  public void testXmlW3CDisclosingCIni() throws IOException {
    String domString =
        " <?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" + " <!DOCTYPE foo [  \n" + "   <!ELEMENT foo ANY >\n"
            + "   <!ENTITY xxe SYSTEM \"file:///c:/boot.ini\" >]><foo>&xxe;</foo>";
    Document doc = XmlW3CHelper.getDomFromString( domString );
    assertNull( doc );
  }

  /**
   * attack example is from https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Processing
   *
   * @throws IOException
   */
  @Test
  public void testXmlW3CDisclosingHttp() throws IOException {
    String domString =
        " <?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" + " <!DOCTYPE foo [  \n" + "   <!ELEMENT foo ANY >\n"
            + "   <!ENTITY xxe SYSTEM \"http://www.attacker.com/text.txt\" >]><foo>&xxe;</foo>";
    Document doc = XmlW3CHelper.getDomFromString( domString );
    assertNull( doc );
  }

  /**
   * attack example is from https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Processing
   *
   * @throws IOException
   */
  @Test
  public void testXmlW3CDisclosingEtcPasswd() throws IOException {
    String domString =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
            + "<!DOCTYPE foo [<!ENTITY xxe SYSTEM \"file:///etc/passwd\" >]>\n"
            + "<root xmlns=\"http://www.pentaho.com\">\n" + "</root>";
    Document doc = XmlW3CHelper.getDomFromString( domString );
    assertNull( doc );
  }

  @Test
  public void testValidXmlW3C() throws IOException {
    String domString = "<root><subroot>this is sub root</subroot></root>";
    Document doc = XmlW3CHelper.getDomFromString( domString );
    assertNotNull( doc );
  }

  @Test
  public void testNotValidXmlW3C() throws IOException {
    String domString = "<root>this is sub root</subroot></root>";

    Document doc = XmlW3CHelper.getDomFromString( domString );
    assertNull( doc );
  }

  @Test
  public void testXmlW3C() throws IOException {
    String path = "src/test/resources/solution/test/xml/query_without_connection.xaction";

    byte[] encoded = IOUtils.toByteArray( new FileInputStream( path ) );
    String sourceXml = new String( encoded );

    Document doc = XmlW3CHelper.getDomFromString( sourceXml );
    assertNotNull( doc );
  }

  @Test
  public void testXmlW3CError() {
    try {
      XmlW3CHelper.getDomFromString( null );
      fail( "This should have thrown an exception" );
    } catch ( IllegalArgumentException e ) {
      assertEquals( "The source string can not be null", e.getMessage() );
    }
  }

  @Test( timeout = 2000 )
  public void shouldNotFailAndReturnNullWhenMaliciousXmlIsGiven() throws Exception {
    assertNull( XmlW3CHelper.getDomFromString( XmlTestConstants.MALICIOUS_XML ) );
  }

  @Test
  public void shouldNotFailAndReturnNotNullWhenLegalXmlIsGiven() throws Exception {
    String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<element>" + "</element>";

    assertNotNull( XmlW3CHelper.getDomFromString( xml ) );
  }
}
