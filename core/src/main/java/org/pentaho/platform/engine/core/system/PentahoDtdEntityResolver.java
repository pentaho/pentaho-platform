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


package org.pentaho.platform.engine.core.system;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class PentahoDtdEntityResolver implements EntityResolver {

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String, java.lang.String)
   */
  public InputSource resolveEntity( final String publicId, final String systemId ) throws SAXException, IOException {
    int idx = systemId.lastIndexOf( '/' );
    String dtdName = systemId.substring( idx + 1 );
    String fullPath = PentahoSystem.getApplicationContext().getSolutionPath( "system/dtd/" + dtdName ); //$NON-NLS-1$
    File theFile = new File( fullPath );
    if ( theFile.canRead() ) {
      InputStream xslIS = new BufferedInputStream( new FileInputStream( theFile ) );
      InputSource source = new InputSource( xslIS );
      return source;
    }
    return null;
  }

}
