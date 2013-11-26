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

package org.pentaho.platform.uifoundation.component.xml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.ui.IXMLComponent;
import org.pentaho.platform.engine.services.SolutionURIResolver;
import org.pentaho.platform.uifoundation.component.BaseUIComponent;
import org.pentaho.platform.uifoundation.messages.Messages;
import org.pentaho.platform.util.xml.XmlHelper;

import javax.xml.transform.TransformerException;
import java.util.List;

public abstract class XmlComponent extends BaseUIComponent implements IXMLComponent {
  /**
   * 
   */
  private static final long serialVersionUID = 7269657964926730872L;
  private static final Log log = LogFactory.getLog( XmlComponent.class );

  public XmlComponent( final IPentahoUrlFactory urlFactory, final List messages, final String sourcePath ) {
    super( urlFactory, messages, sourcePath );
  }

  public abstract Document getXmlContent();

  @Override
  public String getContent( final String mimeType ) {

    if ( "text/xml".equalsIgnoreCase( mimeType ) ) { //$NON-NLS-1$
      Document content = getXmlContent();
      return content.asXML();
    } else {
      Document document = getXmlContent();
      if ( document != null ) {
        String xslName = (String) contentTypes.get( mimeType );
        if ( xslName == null ) {
          error( Messages.getInstance().getString( "BaseUI.ERROR_0002_XSL_NOT_FOUND" ) + mimeType ); //$NON-NLS-1$
          return null;
        }
        StringBuffer sb = null;
        try {
          sb =
              XmlHelper.transformXml( xslName, getSourcePath(), document.asXML(), getXslProperties(),
                  new SolutionURIResolver() );
        } catch ( TransformerException e ) {
          XmlComponent.log.error( Messages.getInstance().getString( "XmlComponent.ERROR_0000_XML_XFORM_FAILED" ), e ); //$NON-NLS-1$
          return null;
        }
        if ( sb == null ) {
          XmlComponent.log.error( Messages.getInstance().getString( "XmlComponent.ERROR_0000_XML_XFORM_FAILED" ) ); //$NON-NLS-1$
          return null;
        }
        if ( BaseUIComponent.debug ) {
          debug( sb.toString() );
        }
        return sb.toString();
      } else {
        return null;
      }
    }
  }
}
