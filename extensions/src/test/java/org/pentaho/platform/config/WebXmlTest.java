/*!
 *
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
 *
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.config;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by rfellows on 10/22/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class WebXmlTest {

  WebXml webXml;
  WebXml webXmlSpy;

  String value = "test value";

  @Mock Document document;

  @Before
  public void setUp() throws Exception {
    webXml = new WebXml();
    webXmlSpy = spy( webXml );
  }

  @Test
  public void testConstructor_doc() throws Exception {
    webXml = new WebXml( document );
    assertEquals( document, webXml.getDocument() );
  }

  @Test
  public void testConstructor_xmlString() throws Exception {
    webXml = new WebXml( "<web-app></web-app>" );
    assertNull( webXml.getEncoding() );
  }

  @Test( expected = DocumentException.class )
  public void testConstructor_file() throws Exception {
    webXml = new WebXml( File.createTempFile( "web", ".xml" ) );
  }

  @Test
  public void testSetFullyQualifiedServerUrl() throws Exception {
    webXmlSpy.setFullyQualifiedServerUrl( value );
    verify( webXmlSpy ).setContextParamValue( WebXml.FULLY_QUALIFIED_SERVER_URL_CONTEXT_PARAM_NAME, value );
  }

  @Test
  public void testGetFullyQualifiedServerUrl() throws Exception {
    webXmlSpy.getFullyQualifiedServerUrl();
    verify( webXmlSpy ).getContextParamValue( WebXml.FULLY_QUALIFIED_SERVER_URL_CONTEXT_PARAM_NAME );
  }

  @Test
  public void testSetContextConfigFileName() throws Exception {
    webXmlSpy.setContextConfigFileName( value );
    verify( webXmlSpy ).setContextParamValue( WebXml.CONTEXT_CONFIG_CONTEXT_PARAM_NAME, value );
  }

  @Test
  public void testGetContextConfigFileName() throws Exception {
    webXmlSpy.getContextConfigFileName();
    verify( webXmlSpy ).getContextParamValue( WebXml.CONTEXT_CONFIG_CONTEXT_PARAM_NAME );
  }

  @Test
  public void testGetBaseUrl() throws Exception {
    webXmlSpy.getBaseUrl();
    // make sure this call forwards to the method that replaced this deprecated one
    verify( webXmlSpy ).getFullyQualifiedServerUrl();
  }

  @Test
  public void testSetSolutionPath() throws Exception {
    webXmlSpy.setSolutionPath( value );
    verify( webXmlSpy ).setContextParamValue( WebXml.SOLUTION_PATH_CONTEXT_PARAM_NAME, value );
  }

  @Test
  public void testGetSolutionPath() throws Exception {
    webXmlSpy.getSolutionPath();
    verify( webXmlSpy ).getContextParamValue( WebXml.SOLUTION_PATH_CONTEXT_PARAM_NAME );
  }

  @Test
  public void testSetLocaleLanguage() throws Exception {
    webXmlSpy.setLocaleLanguage( value );
    verify( webXmlSpy ).setContextParamValue( WebXml.LOCALE_LANGUAGE_CONTEXT_PARAM_NAME, value );
  }

  @Test
  public void testGetLocaleLanguage() throws Exception {
    webXmlSpy.getLocaleLanguage();
    verify( webXmlSpy ).getContextParamValue( WebXml.LOCALE_LANGUAGE_CONTEXT_PARAM_NAME );
  }

  @Test
  public void testSetLocaleCountry() throws Exception {
    webXmlSpy.setLocaleCountry( value );
    verify( webXmlSpy ).setContextParamValue( WebXml.LOCALE_COUNTRY_CONTEXT_PARAM_NAME, value );
  }

  @Test
  public void testGetLocaleCountry() throws Exception {
    webXmlSpy.getLocaleCountry();
    verify( webXmlSpy ).getContextParamValue( WebXml.LOCALE_COUNTRY_CONTEXT_PARAM_NAME );
  }

  @Test
  public void testSetEncoding() throws Exception {
    webXmlSpy.setEncoding( value );
    verify( webXmlSpy ).setContextParamValue( WebXml.ENCODING_CONTEXT_PARAM_NAME, value );
  }

  @Test
  public void testGetEncoding() throws Exception {
    webXmlSpy.getEncoding();
    verify( webXmlSpy ).getContextParamValue( WebXml.ENCODING_CONTEXT_PARAM_NAME );
  }

  @Test
  public void testSetHomePage() throws Exception {
    webXmlSpy.setHomePage( value );
    verify( webXmlSpy ).setServletMapping( WebXml.HOME_SERVLET_NAME, value );
  }

  @Test
  public void testGet() throws Exception {
    webXmlSpy.getHomePage();
    verify( webXmlSpy ).getServletMapping( WebXml.HOME_SERVLET_NAME );
  }

  @Test
  public void testGetContextParamValue() throws Exception {
    webXml = new WebXml( document );

    Node node = mock( Node.class );
    Node subNode = mock( Node.class );
    when( subNode.getText() ).thenReturn( "test node content" );
    when( node.selectSingleNode( "../param-value" ) ).thenReturn( subNode );
    when( document.selectSingleNode( nullable( String.class ) ) ).thenReturn( node );

    String test = webXml.getContextParamValue( "test" );
    assertEquals( "test node content", test );
    verify( node ).selectSingleNode( "../param-value" );
    verify( subNode ).getText();
  }

  @Test
  public void testGetContextParamValue_nullSubNode() throws Exception {
    webXml = new WebXml( document );

    Node node = mock( Node.class );
    when( node.selectSingleNode( "../param-value" ) ).thenReturn( null );
    when( document.selectSingleNode( nullable( String.class ) ) ).thenReturn( node );

    String test = webXml.getContextParamValue( "test" );
    assertNull( test );
    verify( node ).selectSingleNode( "../param-value" );
  }

  @Test
  public void testGetContextParamValue_nullNode() throws Exception {
    webXml = new WebXml( document );

    when( document.selectSingleNode( nullable( String.class ) ) ).thenReturn( null );

    String test = webXml.getContextParamValue( "test" );
    assertNull( test );
  }

  @Test
  public void testGetServletMapping() throws Exception {
    webXml = new WebXml( document );

    Node node = mock( Node.class );
    Node subNode = mock( Node.class );
    when( subNode.getText() ).thenReturn( "test node content" );
    when( node.selectSingleNode( "../jsp-file" ) ).thenReturn( subNode );
    when( document.selectSingleNode( nullable( String.class ) ) ).thenReturn( node );

    String test = webXml.getServletMapping( "test" );
    assertEquals( "test node content", test );
    verify( node ).selectSingleNode( "../jsp-file" );
    verify( subNode ).getText();
  }

  @Test
  public void testGetServletMapping_nullSubNode() throws Exception {
    webXml = new WebXml( document );

    Node node = mock( Node.class );
    when( node.selectSingleNode( "../jsp-file" ) ).thenReturn( null );
    when( document.selectSingleNode( nullable( String.class ) ) ).thenReturn( node );

    String test = webXml.getServletMapping( "test" );
    assertNull( test );
    verify( node ).selectSingleNode( "../jsp-file" );
  }

  @Test
  public void testGetServletMapping_nullNode() throws Exception {
    webXml = new WebXml( document );

    when( document.selectSingleNode( nullable( String.class ) ) ).thenReturn( null );

    String test = webXml.getServletMapping( "test" );
    assertNull( test );
  }

  @Test
  public void testSetContextParamValue_nullValue() throws Exception {
    webXml = new WebXml( document );

    Element node = mock( Element.class );
    Element parent = mock( Element.class ); ;
    when( node.getParent() ).thenReturn( parent );

    when( document.selectSingleNode( nullable( String.class ) ) ).thenReturn( node );

    webXml.setContextParamValue( "key", null );
    verify( parent ).detach();
  }

  @Test
  public void testSetContextParamValue() throws Exception {
    webXml = new WebXml( document );
    Element root = mock( Element.class );
    when( document.getRootElement() ).thenReturn( root );
    Element node = mock( Element.class );
    Element paramNode = mock( Element.class );
    when( root.addElement( nullable( String.class ) ) ).thenReturn( node );
    when( node.getParent() ).thenReturn( root );
    when( document.selectSingleNode( nullable( String.class ) ) ).thenReturn( null );
    when( node.addElement( nullable( String.class ) ) ).thenReturn( paramNode );

    webXml.setContextParamValue( "key", value );
    verify( root ).addElement( WebXml.CONTEXT_PARAM_ELEMENT );
    verify( node ).addElement( WebXml.PARAM_NAME_ELEMENT );
    verify( paramNode ).setText( "key" );
  }

  @Test
  public void testSetServletMapping_noMatch() throws Exception {
    webXml = new WebXml( document );
    when( document.selectSingleNode( nullable( String.class ) ) ).thenReturn( null );
    assertFalse( webXml.setServletMapping( "key", value ) );
  }

  @Test
  public void testSetServletMapping_noJspFileNode() throws Exception {
    webXml = new WebXml( document );
    Element node = mock( Element.class );
    Element jspNode = mock( Element.class );
    when( document.selectSingleNode( nullable( String.class ) ) ).thenReturn( node );
    when( node.selectSingleNode( "../jsp-file" ) ).thenReturn( jspNode );
    assertTrue( webXml.setServletMapping( "key", value ) );
    verify( jspNode ).setText( value );
  }
}
