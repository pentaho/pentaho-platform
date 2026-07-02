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


package org.pentaho.platform.config;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class DtdEntityResolver implements EntityResolver {

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String, java.lang.String)
   */
  public InputSource resolveEntity( final String publicId, final String systemId ) throws SAXException, IOException {
    int idx = systemId.lastIndexOf( '/' );
    String dtdName = systemId.substring( idx + 1 );
    try {
      File f = new File( AppConfigProperties.getInstance().getSolutionPath() + "/system/dtd/" + dtdName ); //$NON-NLS-1$
      if ( f.exists() && f.isFile() ) {
        return new InputSource( new FileInputStream( f ) );
      }
    } catch ( IOException e ) {
      //ignore
    }
    return null;
  }

}
