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

package org.pentaho.platform.util.xml.w3c;

import org.pentaho.platform.util.logging.Logger;
import org.pentaho.platform.util.messages.Messages;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class XmlW3CHelper {

  public static final Document getDomFromString( final String str ) {
    DocumentBuilderFactory dbf;
    DocumentBuilder db;
    Document doc;

    if ( str == null ) {
      throw new IllegalArgumentException( "The source string can not be null" ); //$NON-NLS-1$
    }

    try {
      // Check and open XML document
      dbf = DocumentBuilderFactory.newInstance();
      db = dbf.newDocumentBuilder();
      doc = db.parse( new InputSource( new java.io.StringReader( str ) ) );

      return doc;
    } catch ( Exception e ) {
      Logger.error( XmlW3CHelper.class.getName(), Messages.getInstance().getErrorString(
          "XmlHelper.ERROR_0008_GET_DOM_FROM_STRING_ERROR", e.getMessage() ), e ); //$NON-NLS-1$
    }
    return null;
  }
}
