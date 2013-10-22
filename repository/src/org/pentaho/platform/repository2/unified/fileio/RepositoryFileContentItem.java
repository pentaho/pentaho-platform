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

package org.pentaho.platform.repository2.unified.fileio;

import org.pentaho.commons.connection.IPentahoStreamSource;
import org.pentaho.platform.api.repository.ContentException;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.util.web.MimeHelper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RepositoryFileContentItem implements IContentItem {

  RepositoryFileInputStream inputStream;
  RepositoryFileOutputStream outputStream;

  public RepositoryFileContentItem( String filePath ) {
    outputStream = new RepositoryFileOutputStream( filePath, true, true, null, false );
  }

  RepositoryFileContentItem( RepositoryFileOutputStream outputStream ) {
    this.outputStream = outputStream;
  }

  public void closeOutputStream() {
    if ( outputStream != null ) {
      try {
        outputStream.close();
      } catch ( IOException e ) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  public IPentahoStreamSource getDataSource() {
    return new IPentahoStreamSource() {

      public OutputStream getOutputStream() throws IOException {
        return outputStream;
      }

      public String getName() {
        return RepositoryFilenameUtils.getName( getPath() );
      }

      public InputStream getInputStream() throws IOException {
        return RepositoryFileContentItem.this.getInputStream();
      }

      public String getContentType() {
        return getMimeType();
      }
    };
  }

  public InputStream getInputStream() throws ContentException {
    if ( inputStream == null ) {
      try {
        RepositoryFileOutputStream outputStream = (RepositoryFileOutputStream) getOutputStream( null );
        if ( ( outputStream.autoCreateUniqueFileName ) && !( outputStream.flushed ) ) {
          throw new FileNotFoundException( "File not yet versioned." );
        }
        if ( inputStream == null ) {
          IUnifiedRepository repository = PentahoSystem.get( IUnifiedRepository.class );
          RepositoryFile repositoryFile = repository.getFile( outputStream.path );
          if ( ( repositoryFile == null ) || repositoryFile.isFolder() ) {
            throw new FileNotFoundException();
          }
          return new RepositoryFileInputStream( repositoryFile );
        }
      } catch ( FileNotFoundException e ) {
        throw new ContentException( e );
      } catch ( IOException e ) {
        throw new ContentException( e );
      }
    }
    return inputStream;
  }

  public String getMimeType() {
    return MimeHelper.getMimeTypeFromExtension( "." + RepositoryFilenameUtils.getExtension( getPath() ) );
  }

  public OutputStream getOutputStream( String arg0 ) throws IOException {
    return outputStream;
  }

  public String getPath() {
    return outputStream.getFilePath();
  }

  public void setMimeType( String mimeType ) {
    String fileExtension = MimeHelper.getExtension( mimeType );
    if ( fileExtension == null ) {
      throw new IllegalArgumentException( "Unknown mime type" );
    }
    String requestedFileExtension = MimeHelper.getExtension( mimeType );
    String currentExtension = RepositoryFilenameUtils.getExtension( outputStream.getFilePath() );
    if ( requestedFileExtension == null ) {
      if ( currentExtension != null ) {
        String tempFilePath =
            RepositoryFilenameUtils.getFullPathNoEndSeparator( outputStream.getFilePath() ) + "/"
                + RepositoryFilenameUtils.getBaseName( outputStream.getFilePath() );
        outputStream.setFilePath( tempFilePath );
      }
    } else if ( !requestedFileExtension.substring( 1 ).equals( currentExtension.toLowerCase() ) ) {
      String tempFilePath =
          RepositoryFilenameUtils.getFullPathNoEndSeparator( outputStream.getFilePath() ) + "/"
              + RepositoryFilenameUtils.getBaseName( outputStream.getFilePath() ) + requestedFileExtension;
      outputStream.setFilePath( tempFilePath );
    }
  }

  public void setName( String arg0 ) {
    // TODO Auto-generated method stub
  }

}
