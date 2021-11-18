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
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.engine.services;

import junit.framework.TestCase;
import org.apache.commons.lang.ArrayUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.repository.IContentItem;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link org.pentaho.platform.engine.services.SoapHelper} class.
 *
 * @author whartman
 */
@SuppressWarnings( "nls" )
public class SoapHelperTest extends TestCase {

  private Document buildNonEmptyDoc() {
    Document d = DocumentHelper.createDocument();
    d.setRootElement( new DefaultElement( "testRootElement" ) );
    return d;
  }

  /**
   * Tests for SoapHelper.createSoapDocument()
   */
  public void testCreateSoapDocument() {
    Document d = SoapHelper.createSoapDocument();
    Element envelope = d.getRootElement();
    assertEquals( envelope.getName(), "SOAP-ENV:Envelope" );
    assertEquals( envelope.attribute( "xmlns:SOAP-ENV" ).getValue(), "http://schemas.xmlsoap.org/soap/envelope/" );
    assertEquals( envelope.attribute( "SOAP-ENV:encodingStyle" ).getValue(), "http://schemas.xmlsoap.org/soap/encoding/" );
  }

  /**
   * Tests for:
   * SoapHelper.createSoapResponseDocument(Document[], String)
   * SoapHelper.createSoapResponseDocument(Document, String)
   * SoapHelper.createSoapResponseDocument(String, String)
   * SoapHelper.createSoapResponseDocument(String)
   */
  public void testCreateSoapResponseDocument() {
    //Set up test data
    Document[] nullCheckDocs = {
      SoapHelper.createSoapResponseDocument( new Document[]{} ),
      SoapHelper.createSoapResponseDocument( new Document[]{null} ),
      SoapHelper.createSoapResponseDocument( new Document[]{DocumentHelper.createDocument()} ),
      SoapHelper.createSoapResponseDocument( new Document[]{DocumentHelper.createDocument(), null} ),
      SoapHelper.createSoapResponseDocument( (Document) null ),
      SoapHelper.createSoapResponseDocument( DocumentHelper.createDocument() ), //Has a null root element
    };
    Document[] nonNullDocs = {
      SoapHelper.createSoapResponseDocument( new Document[]{buildNonEmptyDoc()} ),
      SoapHelper.createSoapResponseDocument( new Document[]{buildNonEmptyDoc(), null} ),
      SoapHelper.createSoapResponseDocument( new Document[]{buildNonEmptyDoc(), DocumentHelper.createDocument()} ),
      SoapHelper.createSoapResponseDocument( new Document[]{buildNonEmptyDoc(), buildNonEmptyDoc()} ),
      SoapHelper.createSoapResponseDocument( buildNonEmptyDoc() ),
      SoapHelper.createSoapResponseDocument( "test", "content" ),
      SoapHelper.createSoapResponseDocument( "test" ),
    };
    Object[] allDocs = ArrayUtils.addAll( nullCheckDocs, nonNullDocs );

    //Test for proper content nodes across documents with null and documents without null
    for ( Object d : allDocs ) {
      Element activityResponse = ((Document) d).getRootElement().element( "SOAP-ENV:Body" ).element( "ExecuteActivityResponse" );
      Element contentNode = activityResponse.element( "content" );

      assertNotNull( activityResponse.attribute( "xmlns:m" ) );
      assertEquals( contentNode.getName(), "content" );
      // Some methods add "\"http://pentaho.org\"" and some add "http://pentaho.org" -- Is this intended?
    }

    //Test that items with null documents do not add anything to the contentElement
    for ( Document d : nullCheckDocs ) {
      Element contentNode = d.getRootElement().element( "SOAP-ENV:Body" ).element( "ExecuteActivityResponse" ).element( "content" );
      assertEquals( contentNode.elements().size(), 0 );
    }

    //Test that non-empty root elements are added
    for ( Document d : nonNullDocs ) {
      Element contentNode = d.getRootElement().element( "SOAP-ENV:Body" ).element( "ExecuteActivityResponse" ).element( "content" );

      for ( Object e : contentNode.elements( "testRootElement" ) ) {
        assertEquals( ((Element) e).getName(), buildNonEmptyDoc().getRootElement().getName() );
      }
    }
  }

  /**
   * Tests for SoapHelper.createSoapResponseDocument(IRuntimeContext, IOutputHandler, OutputStream, List)
   */
  public void testCreateSoapResponseDocumentFromContext() {
    //Set up test data
    Set<Object> outputNames = new HashSet<>(  );

    IActionParameter actionParameter = mock( IActionParameter.class );
    when( actionParameter.getValue() ).thenReturn( "testValue" );

    IRuntimeContext context = mock( IRuntimeContext.class );
    when( context.getOutputNames() ).thenReturn( outputNames );
    when( context.getStatus() ).thenReturn( IRuntimeContext.RUNTIME_STATUS_SUCCESS );
    when( context.getOutputParameter( nullable( String.class ) ) ).thenReturn( actionParameter );
    List messages = new ArrayList();

    IOutputHandler outputHandler = mock( IOutputHandler.class );
    IContentItem contentItem = mock( IContentItem.class );
    when( outputHandler.getOutputContentItem( nullable( String.class ), nullable( String.class ), nullable( String.class ), nullable( String.class ) ) ).thenReturn( contentItem );
    when( contentItem.getMimeType() ).thenReturn( "text/xml" );

    OutputStream contentStream = mock( OutputStream.class );
    when( contentStream.toString() ).thenReturn( "contentStreamTestString" );

    //Tests for document with two output names
    outputNames.add( "outputName1" );
    outputNames.add( "outputName2" );
    Document d1 = SoapHelper.createSoapResponseDocument( context, outputHandler, contentStream, messages );
    Element activityResponse1 = d1.getRootElement().element( "SOAP-ENV:Body" ).element( "ExecuteActivityResponse" );
    assertEquals( activityResponse1.elements().size(), 2 );
    assertNotNull( activityResponse1.element( "outputName1" ) );
    assertNotNull( activityResponse1.element( "outputName2" ) );

    //Test for document with one output name (different branch in createSoapResponseDocument())
    outputNames.clear();
    outputNames.add( "outputName1" );
    Document d2 = SoapHelper.createSoapResponseDocument( context, outputHandler, contentStream, messages );
    Element activityResponse2 = d2.getRootElement().element( "SOAP-ENV:Body" ).element( "ExecuteActivityResponse" );
    assertEquals( activityResponse2.elements().size(), 1 );
    assertNotNull( activityResponse2.element( "outputName1" ) );

    //Tests for document with no output names
    outputNames.clear();
    Document d3 = SoapHelper.createSoapResponseDocument( context, outputHandler, contentStream, messages );
    Element activityResponse3 = d3.getRootElement().element( "SOAP-ENV:Body" ).element( "ExecuteActivityResponse" );
    assertTrue( activityResponse3.elements().isEmpty() );
  }
}
