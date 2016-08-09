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
package org.pentaho.platform.util.client;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.input.ReaderInputStream;
import org.dom4j.Document;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;


import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

public class ClientUtilsTest {

  private static final String XML_TEXT = "<Level1>\n"
    + " <Level2>\n"
    + "   <Props>\n"
    + "    <ObjectID>AAAAA</ObjectID>\n"
    + "    <SAPIDENT>31-8200</SAPIDENT>\n"
    + "    <Quantity>1</Quantity>\n"
    + "    <Merkmalname>TX_B</Merkmalname>\n"
    + "    <Merkmalswert> 600</Merkmalswert>\n"
    + "   </Props>\n"
    + "   <Props>\n"
    + "    <ObjectID>AAAAA</ObjectID>\n"
    + "    <SAPIDENT>31-8200</SAPIDENT>\n"
    + "    <Quantity>3</Quantity>\n"
    + "    <Merkmalname>TX_B</Merkmalname>\n"
    + "    <Merkmalswert> 900</Merkmalswert>\n"
    + "   </Props>\n"
    + " </Level2></Level1>";

  @Test
  public void getHttpClientTest() {

    HttpClient clientSpy = spy( ClientUtil.getClient( "admin", "password" ) );
    GetMethod method = mock( GetMethod.class );

    try {
      doReturn( 200 ).when( clientSpy ).executeMethod( method );
      InputStream inputStream = new ReaderInputStream( new StringReader( XML_TEXT ) );
      when( method.getResponseBodyAsStream() ).thenReturn( inputStream );
      Document document = ClientUtil.getResultDom4jDocument( clientSpy, method );
      assertTrue( document.getRootElement().getName().equals( "Level1" ) );
    } catch ( IOException | ServiceException e ) {
      assertTrue( "Shouldn't have thrown exception here", false );
    }
  }
}
