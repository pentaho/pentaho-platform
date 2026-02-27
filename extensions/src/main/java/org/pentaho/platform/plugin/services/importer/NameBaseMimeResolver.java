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


package org.pentaho.platform.plugin.services.importer;

import org.pentaho.platform.api.mimetype.IMimeType;
import org.pentaho.platform.api.mimetype.IPlatformMimeResolver;
import org.pentaho.platform.api.repository2.unified.IPlatformImportBundle;

import java.util.HashMap;
import java.util.Map;

/**
 * Resolves mime-types by extension.
 * <p/>
 * User: nbaker Date: 6/18/12
 */
public class NameBaseMimeResolver implements IPlatformMimeResolver {

  private Map<String, IMimeType> extensionToMimeMap = new HashMap<String, IMimeType>();

  public NameBaseMimeResolver() {
  }

  @Override
  public String resolveMimeForFileName( String fileName ) {
    IMimeType mimeType = extensionToMimeMap.get( extractExtension( fileName ) );
    if ( mimeType == null ) {
      return null;
    }

    return mimeType.getName();
  }

  @Override
  public String resolveMimeForBundle( IPlatformImportBundle bundle ) {
    if ( bundle.getMimeType() != null ) {
      return bundle.getMimeType();
    } else {
      IMimeType mimeType = extensionToMimeMap.get( extractExtension( bundle.getName() ) );
      if ( mimeType == null ) {
        return null;
      }
      return mimeType.getName();
    }
  }

  @Override
  public IMimeType resolveMimeTypeForFileName( String fileName ) {
    return extensionToMimeMap.get( extractExtension( fileName ) );
  }

  private String extractExtension( String name ) {
    if ( name == null ) {
      return null;
    }
    int idx = name.lastIndexOf( "." );
    if ( idx == -1 || idx == name.length() ) {
      return name;
    }
    return name.substring( idx + 1 );
  }

  @Override
  public void addMimeType( IMimeType mimeType ) {
    for ( String extension : mimeType.getExtensions() ) {
      extensionToMimeMap.put( extension, mimeType );
    }
  }
}
