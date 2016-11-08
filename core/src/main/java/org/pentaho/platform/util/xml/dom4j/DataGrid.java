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

package org.pentaho.platform.util.xml.dom4j;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.pentaho.commons.connection.IPentahoMetaData;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.util.messages.LocaleHelper;

import java.util.HashMap;

public class DataGrid {

  public static final int STYLE_ROWS = 1;

  public static final int STYLE_TREE = 2;

  Document gridDocument;

  int documentStyle;

  public DataGrid( final int documentStyle ) {
    this.documentStyle = documentStyle;
  }

  public Document getDataDocument() {
    return gridDocument;
  }

  public int getDocumentStyle() {
    return documentStyle;
  }

  public void populate( final IPentahoResultSet results ) {

    switch ( documentStyle ) {
      case STYLE_ROWS:
        populateRowStyle( results );
    }

  }

  protected void populateRowStyle( final IPentahoResultSet results ) {
    gridDocument = DocumentHelper.createDocument();
    gridDocument.setXMLEncoding( LocaleHelper.getSystemEncoding() );
    Element root = gridDocument.addElement( "datagrid" ); //$NON-NLS-1$

    // add metadata about the headers
    Element metadataNode = root.addElement( "metadata" ); //$NON-NLS-1$
    HashMap<String, String> headerMap = new HashMap<String, String>();
    IPentahoMetaData metadata = results.getMetaData();
    // first process the column headers
    Object[][] headers = metadata.getColumnHeaders();
    addHeaderMetadata( headers, metadataNode, headerMap );

    // now process the rows in the data set
    Element rowsNode = root.addElement( "data" ); //$NON-NLS-1$
    Object[] row = results.next();

    while ( row != null ) {
      // create a new node tree for every row
      Element rowNode = rowsNode.addElement( "row" ); //$NON-NLS-1$
      Element currentNode = rowNode;
      String headerId;
      for ( int columnNo = 0; columnNo < row.length; columnNo++ ) {
        // TODO make sure the nodes are encoded well
        Object columnHeader;
        for ( int headerNo = 0; headerNo < headers.length; headerNo++ ) {
          columnHeader = headers[headerNo][columnNo];
          headerId = headerMap.get( columnHeader );
          currentNode = currentNode.addElement( headerId );
          if ( headerNo < row.length ) {
            currentNode.addElement( "value" ).setText( row[headerNo].toString() ); //$NON-NLS-1$
          }
          // TODO support formatters
          // currentNode.addElement( "formatted" ).setText( row[
          // headerNo ].toString() );
        }
      }
      row = results.next();
    }
    // System .out.println(gridDocument.asXML());
  }

  protected void addHeaderMetadata( final Object[][] headers, final Element metadataNode,
      final HashMap<String, String> headerMap ) {
    if ( headers == null ) {
      return;
    }
    // use a map to ensure we only add this information once
    HashMap<String, String> metadataMap = new HashMap<String, String>();
    for ( Object[] element : headers ) {
      for ( int y = 0; y < element.length; y++ ) {
        Object header = element[y];
        if ( header instanceof String ) {
          String headerStr = (String) header;
          metadataMap.put( headerStr, headerStr );
          createMetadata( headerStr, metadataNode, headerMap );
        } else {
          // TODO suppport heavier metadata objects
          String headerStr = header.toString();
          metadataMap.put( headerStr, headerStr );
          createMetadata( headerStr, metadataNode, headerMap );
        }

      }
    }
  }

  protected void createMetadata( final String header, final Element metadataNode,
      final HashMap<String, String> headerMap ) {
    Element node = metadataNode.addElement( "header" ); //$NON-NLS-1$
    String id = "header" + headerMap.keySet().size(); //$NON-NLS-1$
    node.addAttribute( "id", id ); //$NON-NLS-1$
    node.addElement( "name" ).setText( header ); //$NON-NLS-1$
    node.addAttribute( "format", "" ); //$NON-NLS-1$ //$NON-NLS-2$
    Element title = node.addElement( "title" ); //$NON-NLS-1$
    title.setText( header );
    headerMap.put( header, id );
  }

  // TODO support creating metadata nodes for other metadata types
}
