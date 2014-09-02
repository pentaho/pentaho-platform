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

import java.util.HashMap;
import java.util.Map;

import org.pentaho.platform.plugin.services.importer.mimeType.MimeType;

/**
 * Resolves mime-types by extension.
 * <p/>
 * User: nbaker Date: 6/18/12
 */
public class NameBaseMimeResolver implements IPlatformImportMimeResolver {

  private Map<String, MimeType> extensionToMimeMap = new HashMap<String, MimeType>();

  public NameBaseMimeResolver() {
  }

  @Override
  public String resolveMimeForFileName( String fileName ) {
    MimeType mimeType = extensionToMimeMap.get( extractExtension( fileName ) );
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
      MimeType mimeType = extensionToMimeMap.get( extractExtension( bundle.getName() ) );
      if ( mimeType == null ) {
        return null;
      }
      return mimeType.getName();
    }
  }

  @Override
  public MimeType resolveMimeTypeForFileName( String fileName ) {
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
  public void addMimeType( MimeType mimeType ) {
    for ( String extension : mimeType.getExtensions() ) {
      extensionToMimeMap.put( extension, mimeType );
    }
  }
}
