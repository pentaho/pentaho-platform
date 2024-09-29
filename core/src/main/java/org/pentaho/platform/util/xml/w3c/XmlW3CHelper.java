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
