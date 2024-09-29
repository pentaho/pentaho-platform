/*!
 *
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
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.api.resources.utils;

import org.pentaho.platform.api.mimetype.IPlatformMimeResolver;
import org.pentaho.platform.api.repository2.unified.Converter;
import org.pentaho.platform.api.repository2.unified.IRepositoryContentConverterHandler;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository.RepositoryFilenameUtils;

import java.io.InputStream;
import java.util.Map;


public class RepositoryFileHelper {

  public static IRepositoryFileData getFileData( RepositoryFile repositoryFile ) {
    IRepositoryContentConverterHandler converterHandler;
    Map<String, Converter> converters;
    IPlatformMimeResolver mimeResolver;

    IRepositoryFileData repositoryFileData = null;

    if ( !repositoryFile.isFolder() ) {
      // Get the extension
      final String ext = RepositoryFilenameUtils.getExtension( repositoryFile.getName() );
      if ( ( ext == null ) || ( ext.isEmpty() ) ) {
        return null;
      }

      // Find the converter

      // If we have not been given a handler, try PentahoSystem
      converterHandler = PentahoSystem.get( IRepositoryContentConverterHandler.class );

      // fail if we have no converter handler
      if ( converterHandler == null ) {
        return null;
      }

      converters = converterHandler.getConverters();

      final Converter converter = converters.get( ext );
      if ( converter == null ) {
        return null;
      }

      // Check the mime type
      mimeResolver = PentahoSystem.get( IPlatformMimeResolver.class );

      // fail if we have no mime resolver
      if ( mimeResolver == null ) {
        return null;
      }

      final String mimeType = mimeResolver.resolveMimeTypeForFileName( repositoryFile.getName() ).getName();
      if ( ( mimeType == null ) || ( mimeType.isEmpty() ) ) {
        return null;
      }

      // Get the input stream
      InputStream inputStream = converter.convert( repositoryFile.getId() );
      if ( inputStream == null ) {
        return null;
      }

      // Get the file data
      repositoryFileData = converter.convert( inputStream, "UTF-8", mimeType );
      if ( repositoryFileData == null ) {
        return null;
      }
    }

    return repositoryFileData;
  }
}
