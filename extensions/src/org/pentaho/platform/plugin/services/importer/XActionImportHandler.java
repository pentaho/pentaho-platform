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

package org.pentaho.platform.plugin.services.importer;

import org.apache.commons.io.IOUtils;
import org.pentaho.platform.plugin.services.importer.mimeType.MimeType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.ByteArrayInputStream;
import java.util.List;

public class XActionImportHandler extends RepositoryFileImportFileHandler implements IPlatformImportHandler {

  public XActionImportHandler( List<MimeType> mimeTypes ) {
    super( mimeTypes );
  }

  /**
   * This import hander was created with the sole purpose to honor the hidden flag for action-sequences which is
   * controlled by the "/action-sequence/documentation/result-type" inside of the xaction definition.
   * 
   * Refer to BISERVER-9861, BISERVER-1950, BISERVER-2001.
   */
  @Override
  public void importFile( IPlatformImportBundle bundle ) throws PlatformImportException {
    try {
      RepositoryFileImportBundle importBundle = (RepositoryFileImportBundle) bundle;
      byte[] bytes = IOUtils.toByteArray( bundle.getInputStream() );
      importBundle.setInputStream( new ByteArrayInputStream( bytes ) );

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse( new ByteArrayInputStream( bytes ) );

      NodeList resultTypes = document.getElementsByTagName( "result-type" );
      if ( resultTypes.getLength() > 0 ) {
        Node resultType = resultTypes.item( 0 );
        boolean isHidden = "none".equals( resultType.getTextContent() );
        importBundle.setHidden( isHidden );
      }
      super.importFile( importBundle );
    } catch ( Exception e ) {
      throw new PlatformImportException( e.getMessage(), e );
    }
  }
}
