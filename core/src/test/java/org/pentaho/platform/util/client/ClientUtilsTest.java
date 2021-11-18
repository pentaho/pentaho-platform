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
* Copyright (c) 2002-2021 Hitachi Vantara..  All rights reserved.
*/

package org.pentaho.platform.util.client;

import org.apache.commons.io.input.ReaderInputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.dom4j.Document;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClientUtilsTest {

  private static final String XML_TEXT =
    "<Level1>\n" + " <Level2>\n" + "   <Props>\n" + "    <ObjectID>AAAAA</ObjectID>\n"
      + "    <SAPIDENT>31-8200</SAPIDENT>\n" + "    <Quantity>1</Quantity>\n"
      + "    <Merkmalname>TX_B</Merkmalname>\n" + "    <Merkmalswert> 600</Merkmalswert>\n" + "   </Props>\n"
      + "   <Props>\n" + "    <ObjectID>AAAAA</ObjectID>\n" + "    <SAPIDENT>31-8200</SAPIDENT>\n"
      + "    <Quantity>3</Quantity>\n" + "    <Merkmalname>TX_B</Merkmalname>\n"
      + "    <Merkmalswert> 900</Merkmalswert>\n" + "   </Props>\n" + " </Level2></Level1>";

  private static final String XML_BROKEN =
    "<Level1>\n" + " <Level2>\n" + "   <Props>\n" + "    <ObjectID>AAAAA</ObjectID>\n"
      + "    <SAPIDENT>31-8200</SAPIDENT>\n";

  private static final int OK_CODE = 200;

  @Test
  public void getHttpClientTest() {
    HttpResponse httpResponseMock = mock( HttpResponse.class );
    HttpEntity httpEntityMock = mock( HttpEntity.class );
    StatusLine statusLineMock = mock( StatusLine.class );
    HttpClient httpClientMock = mock( HttpClient.class );
    HttpGet method = mock( HttpGet.class );

    try {
      when( httpResponseMock.getStatusLine() ).thenReturn( statusLineMock );
      when( httpClientMock.execute( any( HttpUriRequest.class ) ) ).thenReturn( httpResponseMock );
      when( statusLineMock.getStatusCode() ).thenReturn( OK_CODE );
      when( httpResponseMock.getStatusLine() ).thenReturn( statusLineMock );

      InputStream inputStream = new ReaderInputStream( new StringReader( XML_TEXT ) );
      when( httpResponseMock.getEntity() ).thenReturn( httpEntityMock );
      when( httpEntityMock.getContent() ).thenReturn( inputStream );
      Document document = ClientUtil.getResultDom4jDocument( httpClientMock, method );
      assertTrue( document.getRootElement().getName().equals( "Level1" ) );
    } catch ( IOException | ServiceException e ) {
      assertTrue( "Shouldn't have thrown exception here", false );
    }
  }

  @Test( expected = ServiceException.class )
  public void testgetResultDom4jDocumentException() throws Exception {
    HttpResponse httpResponseMock = mock( HttpResponse.class );
    StatusLine statusLineMock = mock( StatusLine.class );
    HttpClient httpClientMock = mock( HttpClient.class );
    HttpGet method = mock( HttpGet.class );

    when( httpResponseMock.getStatusLine() ).thenReturn( statusLineMock );
    when( httpClientMock.execute( any( HttpUriRequest.class ) ) ).thenReturn( httpResponseMock );
    when( statusLineMock.getStatusCode() ).thenReturn( 500 );

    Document document;
    document = ClientUtil.getResultDom4jDocument( httpClientMock, method );
    assertNull( document );
  }

  @Test( expected = ServiceException.class )
  public void testgetResultDom4jDocumentException2() throws Exception {
    HttpResponse httpResponseMock = mock( HttpResponse.class );
    HttpEntity httpEntityMock = mock( HttpEntity.class );
    StatusLine statusLineMock = mock( StatusLine.class );
    HttpClient httpClientMock = mock( HttpClient.class );
    HttpGet method = mock( HttpGet.class );

    when( httpResponseMock.getStatusLine() ).thenReturn( statusLineMock );
    when( httpClientMock.execute( any( HttpUriRequest.class ) ) ).thenReturn( httpResponseMock );
    when( statusLineMock.getStatusCode() ).thenReturn( OK_CODE );

    Document document;
    when( httpResponseMock.getEntity() ).thenReturn( httpEntityMock );
    when( httpEntityMock.getContent() ).thenThrow( new IOException() );

    document = ClientUtil.getResultDom4jDocument( httpClientMock, method );
    assertNull( document );
  }

  @Test( expected = ServiceException.class )
  public void testgetResultDom4jDocumentException3() throws Exception {
    HttpResponse httpResponseMock = mock( HttpResponse.class );
    HttpEntity httpEntityMock = mock( HttpEntity.class );
    StatusLine statusLineMock = mock( StatusLine.class );
    HttpClient httpClientMock = mock( HttpClient.class );
    HttpGet method = mock( HttpGet.class );

    when( httpResponseMock.getStatusLine() ).thenReturn( statusLineMock );
    when( httpClientMock.execute( any( HttpUriRequest.class ) ) ).thenReturn( httpResponseMock );
    when( statusLineMock.getStatusCode() ).thenReturn( OK_CODE );

    InputStream inputStream = new ReaderInputStream( new StringReader( XML_BROKEN ) );
    when( httpResponseMock.getEntity() ).thenReturn( httpEntityMock );
    when( httpEntityMock.getContent() ).thenReturn( inputStream );
    Document document;
    document = ClientUtil.getResultDom4jDocument( httpClientMock, method );
    assertNull( document );
  }

  @Test
  public void testGetClient() {
    ClientUtil util = new ClientUtil();
    assertNotNull( util );

    HttpClient client = ClientUtil.getClient( null, null );
    assertNotNull( client );

    client = ClientUtil.getClient( "", "" );
    assertNotNull( client );

    client = ClientUtil.getClient( null, "" );
    assertNotNull( client );

    client = ClientUtil.getClient( "", null );
    assertNotNull( client );

    client = ClientUtil.getClient( "username", null );
    assertNotNull( client );

    client = ClientUtil.getClient( "username", "" );
    assertNotNull( client );

    client = ClientUtil.getClient( null, "password" );
    assertNotNull( client );

    client = ClientUtil.getClient( "", "password" );
    assertNotNull( client );

    client = ClientUtil.getClient( "username", "password" );
    assertNotNull( client );
  }
}
