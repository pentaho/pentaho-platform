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


package org.pentaho.platform.util.xml.w3c;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.pentaho.platform.util.logging.Logger;
import org.pentaho.platform.util.messages.Messages;
import org.pentaho.platform.util.xml.XMLParserFactoryProducer;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class XmlW3CHelper {

  private XmlW3CHelper() {
  }

  public static final Document getDomFromString( final String str ) {
    if ( str == null ) {
      throw new IllegalArgumentException( "The source string can not be null" ); //$NON-NLS-1$
    }

    try {
      // Check and open XML document
      DocumentBuilderFactory dbf = XMLParserFactoryProducer.createSecureDocBuilderFactory();
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse( new InputSource( new java.io.StringReader( str ) ) );

      return doc;
    } catch ( Exception e ) {
      Logger.error( XmlW3CHelper.class.getName(), Messages.getInstance().getErrorString(
        "XmlHelper.ERROR_0008_GET_DOM_FROM_STRING_ERROR", e.getMessage() ), e ); //$NON-NLS-1$
    }
    return null;
  }
}
