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

package org.pentaho.platform.engine.services;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.util.messages.LocaleHelper;

import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class SoapHelper {

  public static String getSoapHeader() {
    return "<SOAP-ENV:Envelope " + //$NON-NLS-1$
        "xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" " + //$NON-NLS-1$
        "SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n " + //$NON-NLS-1$
        "<SOAP-ENV:Body>\n"; //$NON-NLS-1$

  }

  public static String getSoapFooter() {
    return "</SOAP-ENV:Body>\n</SOAP-ENV:Envelope>"; //$NON-NLS-1$

  }

  public static String openSoapResponse() {
    return "<ExecuteActivityResponse xmlns:m=\"http://pentaho.org\">\n"; //$NON-NLS-1$
  }

  public static String closeSoapResponse() {
    return "</ExecuteActivityResponse>\n"; //$NON-NLS-1$
  }

  private static Element createActivityResponseElement() {
    Element element = new DefaultElement( "ExecuteActivityResponse" );
    element.addAttribute( "xmlns:m", "http://pentaho.org" );
    return element;
  }

  private static Element createSoapElement( String name, Object value ) {
    if ( value instanceof String ) {
      return createSoapElement( name, (String) value );
    } else if ( value instanceof List ) {
      return createSoapElement( name, (List) value );
    } else if ( value instanceof IPentahoResultSet ) {
      return createSoapElement( name, (IPentahoResultSet) value );
    } else if ( value instanceof IContentItem ) {
      return createSoapElement( name, ( (IContentItem) value ).getPath() );
    }
    return null;
  }

  private static Element createSoapElement( String name, String value ) {
    Element element = new DefaultElement( name );
    element.addCDATA( value );
    return element;
  }

  private static Element createSoapElement( String name, List value ) {
    Element element = new DefaultElement( name );
    element.addCDATA( value.toString() );
    return element;
  }

  @SuppressWarnings( "null" )
  private static Element createSoapElement( String name, IPentahoResultSet resultSet ) {

    Element resultSetElement = new DefaultElement( name );
    Object[][] columnHeaders = resultSet.getMetaData().getColumnHeaders();
    Object[][] rowHeaders = resultSet.getMetaData().getRowHeaders();
    boolean hasColumnHeaders = columnHeaders != null;
    boolean hasRowHeaders = rowHeaders != null;

    if ( hasColumnHeaders ) {
      for ( Object[] element : columnHeaders ) {
        Element columnHeaderRowElement = resultSetElement.addElement( "COLUMN-HDR-ROW" ); //$NON-NLS-1$
        for ( int column = 0; column < element.length; column++ ) {
          columnHeaderRowElement.addElement( "COLUMN-HDR-ITEM" ).addCDATA( element[column].toString() ); //$NON-NLS-1$
        }
      }
    }

    if ( hasRowHeaders ) {
      for ( Object[] element : rowHeaders ) {
        Element rowHeaderRowElement = resultSetElement.addElement( "ROW-HDR-ROW" ); //$NON-NLS-1$
        for ( int column = 0; column < element.length; column++ ) {
          rowHeaderRowElement.addElement( "ROW-HDR-ITEM" ).addCDATA( element[column].toString() ); //$NON-NLS-1$
        }
      }
    }

    Object[] dataRow = resultSet.next();
    while ( dataRow != null ) {
      Element dataRowElement = resultSetElement.addElement( "DATA-ROW" ); //$NON-NLS-1$
      for ( Object element : dataRow ) {
        dataRowElement.addElement( "DATA-ITEM" ).addCDATA( element.toString() ); //$NON-NLS-1$
      }
      dataRow = resultSet.next();
    }

    return resultSetElement;
  }

  private static Element createSoapEnvelope() {
    Element envelope = new DefaultElement( "SOAP-ENV:Envelope" );
    envelope.addAttribute( "xmlns:SOAP-ENV", "http://schemas.xmlsoap.org/soap/envelope/" );
    envelope.addAttribute( "SOAP-ENV:encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/" );
    return envelope;
  }

  private static Element createSoapBody() {
    return new DefaultElement( "SOAP-ENV:Body" );
  }

  public static Document createSoapDocument() {
    Document document = DocumentHelper.createDocument();
    Element envelope = createSoapEnvelope();
    document.setRootElement( envelope );
    envelope.add( createSoapBody() );
    return document;
  }

  public static Document createSoapResponseDocument( Document[] documents ) {
    return createSoapResponseDocument( documents, "content" );
  }

  public static Document createSoapResponseDocument( Document responseXml ) {
    return createSoapResponseDocument( responseXml, "content" );
  }

  private static Element createSoapFaultElement( List messages ) {
    Element faultElement = new DefaultElement( "SOAP-ENV:Fault" );

    // TODO mlowery begin hack: copied in getFirstError code from MessageFormatter
    // to avoid needing an IPentahoSession
    String message = null;
    String errorStart = PentahoMessenger.getUserString( "ERROR" ); //$NON-NLS-1$
    int pos = errorStart.indexOf( '{' );
    if ( pos != -1 ) {
      errorStart = errorStart.substring( 0, pos );
    }
    Iterator msgIterator = messages.iterator();
    while ( msgIterator.hasNext() ) {
      String msg = (String) msgIterator.next();
      if ( msg.indexOf( errorStart ) == 0 ) {
        message = msg;
      }
    }
    // TODO mlowery end hack

    if ( message == null ) {
      message = Messages.getInstance().getErrorString( "SoapHelper.ERROR_0001_UNKNOWN_ERROR" ); //$NON-NLS-1$
    }

    // Envelope envelope = new Envelope();
    // Fault fault = new Fault( );
    // TODO: Generate the following message using the envelope and fault objects

    // TODO determine if this is a reciever or a sender problem by examining
    // the error code
    boolean senderFault = ( message.indexOf( "SolutionEngine.ERROR_0002" ) != -1 ) || //$NON-NLS-1$ // solution not specifed
        ( message.indexOf( "SolutionEngine.ERROR_0003" ) != -1 ) || //$NON-NLS-1$ // Path not specifeid
        ( message.indexOf( "SolutionEngine.ERROR_0004" ) != -1 ) || //$NON-NLS-1$ // Action not specified
        ( message.indexOf( "SolutionEngine.ERROR_0005" ) != -1 ); //$NON-NLS-1$ // Action not found
    // send the error code
    // TODO parse out the error code
    faultElement
        .addElement( "SOAP-ENV:Fault" ).addElement( "SOAP-ENV:Subcode" ).addElement( "SOAP-ENV:Value" ).addCDATA( message ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    if ( senderFault ) {
      faultElement.addElement( "SOAP-ENV:faultactor" ).setText( "SOAP-ENV:Client" ); //$NON-NLS-1$ //$NON-NLS-2$ 
    } else {
      faultElement.addElement( "SOAP-ENV:faultactor" ).setText( "SOAP-ENV:Server" ); //$NON-NLS-1$ //$NON-NLS-2$ 
    }

    Element faultTextElement = faultElement.addElement( "SOAP-ENV:faultstring" ).addElement( "SOAP-ENV:Text" );
    faultTextElement.addAttribute( "xml:lang", LocaleHelper.getDefaultLocale().toString() );
    faultTextElement.addCDATA( message );

    Element detailElement = faultElement.addElement( "SOAP-ENV:Detail" );
    Iterator messageIterator = messages.iterator();
    while ( messageIterator.hasNext() ) {
      detailElement.addElement( "message" ).addAttribute( "name", "trace" ).addCDATA( (String) messageIterator.next() );
    }
    return faultElement;
  }

  public static Document createSoapResponseDocument( IRuntimeContext context, IOutputHandler outputHandler,
      OutputStream contentStream, List messages ) {
    Document document = createSoapDocument();
    if ( ( context == null ) || ( context.getStatus() != IRuntimeContext.RUNTIME_STATUS_SUCCESS ) ) {
      document.getRootElement().element( "SOAP-ENV:Body" ).add( createSoapFaultElement( messages ) ); //$NON-NLS-1$
    } else {
      Element activityResponse = createActivityResponseElement();
      document.getRootElement().element( "SOAP-ENV:Body" ).add( activityResponse ); //$NON-NLS-1$

      IContentItem contentItem = outputHandler.getFeedbackContentItem();

      // hmm do we need this to be ordered?
      Set outputNames = context.getOutputNames();

      Iterator outputNameIterator = outputNames.iterator();
      while ( outputNameIterator.hasNext() ) {
        String outputName = (String) outputNameIterator.next();
        contentItem =
            outputHandler.getOutputContentItem( IOutputHandler.RESPONSE, IOutputHandler.CONTENT, context
                .getInstanceId(), "text/xml" ); //$NON-NLS-1$
        if ( ( outputNames.size() == 1 ) && ( contentItem != null ) ) {
          String mimeType = contentItem.getMimeType();
          if ( ( mimeType != null ) && mimeType.startsWith( "text/" ) ) { //$NON-NLS-1$
            if ( mimeType.equals( "text/xml" ) ) { //$NON-NLS-1$
              activityResponse.addElement( outputName ).setText( contentStream.toString() );
            } else if ( mimeType.startsWith( "text/" ) ) { //$NON-NLS-1$
              activityResponse.addElement( outputName ).addCDATA( contentStream.toString() );
            }
          } else {
            Object value = context.getOutputParameter( outputName ).getValue();
            if ( value == null ) {
              value = ""; //$NON-NLS-1$
            }
            activityResponse.add( createSoapElement( outputName, value ) );
          }
        } else {
          Object value = context.getOutputParameter( outputName ).getValue();
          if ( value == null ) {
            value = ""; //$NON-NLS-1$
          }
          activityResponse.add( createSoapElement( outputName, value ) );
        }
      }
    }
    return document;
  }

  public static Document createSoapResponseDocument( Document[] documents, String contentNodeName ) {
    Document document = createSoapDocument();
    Element activityResponse = createActivityResponseElement();
    document.getRootElement().element( "SOAP-ENV:Body" ).add( activityResponse );
    Element contentElement = activityResponse.addElement( contentNodeName );
    for ( Document contentDocument : documents ) {
      if ( ( contentDocument != null ) && ( contentDocument.getRootElement() != null ) ) {
        contentElement.add( contentDocument.getRootElement() );
      }
    }
    return document;
  }

  public static Document createSoapResponseDocument( Document contentDocument, String contentNodeName ) {
    Document document = createSoapDocument();
    Element activityResponse = createActivityResponseElement();
    document.getRootElement().element( "SOAP-ENV:Body" ).add( activityResponse );
    Element contentElement = activityResponse.addElement( contentNodeName );
    if ( ( contentDocument != null ) && ( contentDocument.getRootElement() != null ) ) {
      contentElement.add( contentDocument.getRootElement() );
    }
    return document;
  }

  public static Document createSoapResponseDocument( String responseString ) {
    return createSoapResponseDocument( responseString, "content" );
  }

  public static Document createSoapResponseDocument( String responseString, String contentNodeName ) {
    Document document = createSoapDocument();
    Element activityResponse =
        document.getRootElement().element( "SOAP-ENV:Body" ).addElement( "ExecuteActivityResponse" ).addAttribute(
            "xmlns:m", "\"http://pentaho.org\"" );
    Element contentElement = activityResponse.addElement( contentNodeName );
    contentElement.setText( responseString );
    return document;
  }
}
