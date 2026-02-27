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


package org.pentaho.platform.engine.services;

import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.engine.IDocumentResourceLoader;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.actionsequence.ActionSequenceResource;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.xml.sax.InputSource;

import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SolutionURIResolver implements URIResolver, IDocumentResourceLoader {

  public InputSource resolveEntity( final String publicId, final String systemId ) {
    InputStream xslIS = null;
    try {
      if ( systemId.toLowerCase().indexOf( ".dtd" ) >= 0 ) { //$NON-NLS-1$
        return resolveDTDEntity( publicId, systemId );
      }
      IActionSequenceResource resource =
          new ActionSequenceResource( "", IActionSequenceResource.SOLUTION_FILE_RESOURCE, "text/xml", //$NON-NLS-1$ //$NON-NLS-2$
              systemId );
      xslIS = resource.getInputStream( RepositoryFilePermission.READ, LocaleHelper.getLocale() );
      return new InputSource( xslIS );
    } catch ( IOException e ) {
      Logger.error( this, e.getLocalizedMessage() );
    }
    return null;

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String, java.lang.String)
   */
  public InputSource resolveDTDEntity( final String publicId, final String systemId ) throws IOException {

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

  /*
   * (non-Javadoc)
   * 
   * @see javax.xml.transform.URIResolver#resolve(java.lang.String, java.lang.String)
   */
  public Source resolve( final String href, final String base ) {
    StreamSource xslSrc = null;
    InputStream xslIS = null;
    IActionSequenceResource resource =
        new ActionSequenceResource( "", IActionSequenceResource.SOLUTION_FILE_RESOURCE, "text/xml", //$NON-NLS-1$ //$NON-NLS-2$
            href );
    xslIS = resource.getInputStream( RepositoryFilePermission.READ, LocaleHelper.getLocale() );
    xslSrc = new StreamSource( xslIS );

    return xslSrc;
  }

  public InputStream loadXsl( final String name ) {
    InputStream xslIS = null;
    IActionSequenceResource resource =
        new ActionSequenceResource( "", IActionSequenceResource.SOLUTION_FILE_RESOURCE, "text/xml", //$NON-NLS-1$ //$NON-NLS-2$
            name );
    xslIS = resource.getInputStream( RepositoryFilePermission.READ, LocaleHelper.getLocale() );

    return xslIS;
  }

}
