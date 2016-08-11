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
* Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
*/
package org.pentaho.platform.util.versionchecker;


import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.logging.Log;
import org.junit.Test;
import org.mockito.Mockito;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class PentahoVersionCheckReflectHelperTest {

  private static final String XML_TEXT = "<Level1>\n"
    + " <Level2>\n"
    + "   <update>\n"
    + "    <done/>\n"
    + "   </update>\n"
    + "   <error>\n"
    + "    <bad/>\n"
    + "   </error>\n"
    + " </Level2></Level1>";
  @Test
  public void performVersionCheckTest() {
    List results = PentahoVersionCheckReflectHelper.performVersionCheck( false, -1 );
    assertNotNull( results );
    assertTrue( results.size() > 0 );
    validateResult( results.get( 0 ).toString() );
  }

  @Test
  public void logVersionCheckTest() {
    Log mockLog = Mockito.mock( Log.class );

    List<String> results = new ArrayList<>();
    results.add( XML_TEXT );
    PentahoVersionCheckReflectHelper.logVersionCheck( results, mockLog );
    verify( mockLog, times( 2 ) ).info( anyObject() );
  }

  private void validateResult( String result ) {

    try {
      DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
      StringReader stringReader = new StringReader( result  );
      documentBuilder.parse( new ReaderInputStream( stringReader ) );
    } catch ( ParserConfigurationException | SAXException | IOException e ) {
      assertTrue( "unexpected exception", false );
    }
  }
}
