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
 * Copyright 2006 - 2017 Hitachi Vantara.  All rights reserved.
 */

package org.pentaho.platform.repository2.unified.fileio;

import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.repository2.unified.Converter;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.ISourcesStreamEvents;
import org.pentaho.platform.api.repository2.unified.IStreamListener;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.util.web.MimeHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RepositoryFileOutputStream extends ByteArrayOutputStream implements ISourcesStreamEvents {

  private static final String TRANS_EXT = "ktr";
  private static final String JOB_EXT = "kjb";

  protected boolean hidden = false;
  protected String path = null;
  protected IUnifiedRepository repository;
  protected String charsetName = null;
  protected boolean autoCreateUniqueFileName = false;
  protected boolean autoCreateDirStructure = false;
  protected boolean closed = false;
  protected boolean flushed = false;
  protected ArrayList<IStreamListener> listeners = new ArrayList<>();

  public RepositoryFileOutputStream( final String path, final boolean autoCreateUniqueFileName,
      final boolean autoCreateDirStructure, final IUnifiedRepository repository, final boolean hidden ) {
    setRepository( repository );
    this.path = path;
    this.hidden = hidden;
    this.autoCreateDirStructure = autoCreateDirStructure;
    this.autoCreateUniqueFileName = autoCreateUniqueFileName;
  }

  public RepositoryFileOutputStream( final Serializable id, final boolean autoCreateUniqueFileName,
      final boolean autoCreateDirStructure, final IUnifiedRepository repository, final boolean hidden )
    throws FileNotFoundException {
    setRepository( repository );
    RepositoryFile file = this.repository.getFileById( id );
    if ( file == null ) {
      throw new FileNotFoundException( MessageFormat.format(
          "Repository file with id {0} not readable or does not exist", id ) );
    }
    this.hidden = hidden;
    this.path = file.getPath();
    this.autoCreateDirStructure = autoCreateDirStructure;
    this.autoCreateUniqueFileName = autoCreateUniqueFileName;
  }

  public RepositoryFileOutputStream( final RepositoryFile file, final boolean autoCreateUniqueFileName,
      final boolean autoCreateDirStructure, final IUnifiedRepository repository ) {
    this( file.getPath(), autoCreateUniqueFileName, autoCreateDirStructure, repository, false );
  }

  public RepositoryFileOutputStream( final String path, final boolean autoCreateUniqueFileName,
      final boolean autoCreateDirStructure ) {
    this( path, autoCreateUniqueFileName, autoCreateDirStructure, null, false );
  }

  public RepositoryFileOutputStream( final Serializable id, final boolean autoCreateUniqueFileName,
      final boolean autoCreateDirStructure ) throws FileNotFoundException {
    this( id, autoCreateUniqueFileName, autoCreateDirStructure, null, false );
  }

  public RepositoryFileOutputStream( final RepositoryFile file, final boolean autoCreateUniqueFileName,
      final boolean autoCreateDirStructure ) {
    this( file, autoCreateUniqueFileName, autoCreateDirStructure, null );
  }

  public RepositoryFileOutputStream( final String path ) {
    this( path, false, false, null, false );
  }

  public RepositoryFileOutputStream( final String path, final boolean hidden ) {
    this( path, false, false, null, hidden );
  }

  public RepositoryFileOutputStream( final RepositoryFile file ) {
    this( file.getPath(), false, false, null, file.isHidden() );
  }

  public RepositoryFileOutputStream( final Serializable id ) throws FileNotFoundException {
    this( id, false, false, null, false );
  }

  // //
  // charsetName is required as metadata so the JCR can be provided the correct encoding, provided
  // we are storing text. These are package private methosd only and should only be called by
  // RepositoryFileWriter, because if you are wanting to write characters to a repository file,
  // you need to use RepositoryFileWriter, not this class.
  //
  protected RepositoryFileOutputStream( String path, String charsetName ) throws FileNotFoundException {
    this( path );
    this.charsetName = charsetName;
  }

  protected RepositoryFileOutputStream( RepositoryFile file, String charsetName ) throws FileNotFoundException {
    this( file );
    this.charsetName = charsetName;
  }

  protected RepositoryFileOutputStream( Serializable id, String charsetName ) throws FileNotFoundException {
    this( id );
    this.charsetName = charsetName;
  }

  //
  // //

  protected RepositoryFile getParent( String path ) {
    String newFilePath = StringUtils.removeEnd( path, "/" ); //$NON-NLS-1$
    String parentPath = StringUtils.substringBeforeLast( newFilePath, "/" ); //$NON-NLS-1$
    if ( parentPath.isEmpty() ) {
      parentPath = "/";
    }
    return repository.getFile( parentPath );
  }

  @Override
  public void close() throws IOException {
    if ( !closed ) {
      flush();
      closed = true;
      reset();
    }
  }

  @Override
  public void flush() throws IOException {
    if ( closed ) {
      return;
    }
    super.flush();

    ByteArrayInputStream bis = new ByteArrayInputStream( toByteArray() );

    // make an effort to determine the correct mime type, default to application/octet-stream
    String extension = RepositoryFilenameUtils.getExtension( path );
    String mimeType = "application/octet-stream"; //$NON-NLS-1$
    if ( extension != null ) {
      String tempMimeType = MimeHelper.getMimeTypeFromExtension( "." + extension ); //$NON-NLS-1$
      if ( tempMimeType != null ) {
        mimeType = tempMimeType;
      }
    }

    if ( charsetName == null ) {
      charsetName = MimeHelper.getDefaultCharset( mimeType );
    }

    // FIXME: not a good idea that we assume we are dealing with text. Best if this is somehow moved to the
    // RepositoryFileWriter
    // but I couldn't figure out a clean way to do that. For now, charsetName is passed in here and we use it if
    // available.
    final IRepositoryFileData payload;
    Converter converter;

    if ( TRANS_EXT.equalsIgnoreCase( extension ) ) {
      converter = PentahoSystem.get( Converter.class, null, Collections.singletonMap( "name", "PDITransformationStreamConverter" ) );
      mimeType = "application/vnd.pentaho.transformation";
    } else if ( JOB_EXT.equalsIgnoreCase( extension ) ) {
      converter = PentahoSystem.get( Converter.class, null, Collections.singletonMap( "name", "PDIJobStreamConverter" ) );
      mimeType = "application/vnd.pentaho.job";
    } else {
      converter = null;
    }
    payload = convert( converter, bis, mimeType );
    if ( !flushed ) {
      RepositoryFile file = repository.getFile( path );
      RepositoryFile parentFolder = getParent( path );
      String baseFileName = RepositoryFilenameUtils.getBaseName( path );
      if ( file == null ) {
        if ( autoCreateDirStructure ) {
          ArrayList<String> foldersToCreate = new ArrayList<>();
          String parentPath = RepositoryFilenameUtils.getFullPathNoEndSeparator( path );
          // Make sure the parent path isn't the root
          while ( ( parentPath != null ) && ( parentPath.length() > 0 && !path.equals( parentPath ) )
              && ( repository.getFile( parentPath ) == null ) ) {
            foldersToCreate.add( RepositoryFilenameUtils.getName( parentPath ) );
            parentPath = RepositoryFilenameUtils.getFullPathNoEndSeparator( parentPath );
          }
          Collections.reverse( foldersToCreate );
          parentFolder =
              ( ( parentPath != null ) && ( parentPath.length() > 0 ) ) ? repository.getFile( parentPath ) : repository
                  .getFile( "/" );
          if ( !parentFolder.isFolder() ) {
            throw new FileNotFoundException();
          }
          for ( String folderName : foldersToCreate ) {
            parentFolder =
                repository.createFolder( parentFolder.getId(), new RepositoryFile.Builder( folderName ).folder( true )
                    .build(), null );
          }
        } else {
          if ( parentFolder == null ) {
            throw new FileNotFoundException();
          }
        }
        file = buildRepositoryFile( RepositoryFilenameUtils.getName( path ), extension, baseFileName );

        repository.createFile( parentFolder.getId(), file, payload,
          "commit from " + RepositoryFileOutputStream.class.getName() ); //$NON-NLS-1$
        for ( IStreamListener listener : listeners ) {
          listener.fileCreated( path );
        }
      } else if ( file.isFolder() ) {
        throw new FileNotFoundException( MessageFormat.format( "Repository file {0} is a directory", file.getPath() ) );
      } else {
        if ( autoCreateUniqueFileName ) {
          int nameCount = 1;
          String newFileName = null;
          String newBaseFileName = null;

          List<RepositoryFile> children = repository.getChildren( parentFolder.getId() );

          boolean hasFile = true;
          while ( hasFile ) {
            hasFile = false;
            nameCount++;
            newBaseFileName = baseFileName + "(" + nameCount + ")";
            if ( ( extension != null ) && ( extension.length() > 0 ) ) {
              newFileName = newBaseFileName + "." + extension; //$NON-NLS-1$ //$NON-NLS-2$
            } else {
              newFileName = newBaseFileName; //$NON-NLS-1$ //$NON-NLS-2$
            }
            for ( RepositoryFile child : children ) {
              if ( child.getPath().equals( parentFolder.getPath() + "/" + newFileName ) ) {
                hasFile = true;
                break;
              }
            }
          }

          file = buildRepositoryFile( newFileName, extension, newBaseFileName );

          file = repository.createFile( parentFolder.getId(), file, payload, "New File" ); //$NON-NLS-1$
          path = file.getPath();
          for ( IStreamListener listener : listeners ) {
            listener.fileCreated( path );
          }
        } else {
          repository.updateFile( file, payload, "New File" ); //$NON-NLS-1$
          path = file.getPath();
          for ( IStreamListener listener : listeners ) {
            listener.fileCreated( path );
          }
        }
      }
    } else {
      RepositoryFile file = repository.getFile( path );
      repository.updateFile( file, payload, "New File" ); //$NON-NLS-1$
    }
    flushed = true;
  }

  IRepositoryFileData convert( Converter converter, ByteArrayInputStream bis, String mimeType ) {
    final IRepositoryFileData payload;
    if ( converter != null ) {
      payload = converter.convert( bis, charsetName, mimeType );
    } else {
      payload = new SimpleRepositoryFileData( bis, charsetName, mimeType );
    }
    return payload;
  }

  public String getFilePath() {
    return path;
  }

  public void setFilePath( String path ) {
    if ( !path.equals( this.path ) ) {
      this.path = path;
      reset();
      flushed = false;
      closed = false;
    }
  }

  public boolean getAutoCreateUniqueFileName() {
    return autoCreateUniqueFileName;
  }

  public boolean getAutoCreateDirStructure() {
    return autoCreateDirStructure;
  }

  public void setAutoCreateDirStructure( boolean autoCreateDirStructure ) {
    this.autoCreateDirStructure = autoCreateDirStructure;
  }

  public void addListener( IStreamListener listener ) {
    listeners.add( listener );
  }

  public void setRepository( final IUnifiedRepository repository ) {
    this.repository = ( repository != null ? repository : PentahoSystem.get( IUnifiedRepository.class ) );
  }

  public IUnifiedRepository getRepository() {
    return this.repository;
  }

  public String getCharsetName() {
    return charsetName;
  }

  public void setCharsetName( final String charsetName ) {
    this.charsetName = charsetName;
  }

  public boolean isFlushed() {
    return flushed;
  }

  private RepositoryFile buildRepositoryFile( String fileName, String extension, String baseFileName ) {
    RepositoryFile.Builder fileBuilder = new RepositoryFile.Builder( fileName )
      .hidden( hidden )
      .versioned( true ); // Default versioned to true so that we're keeping history

    if ( isKettleExtension( extension ) ) {
      fileBuilder.title( RepositoryFile.DEFAULT_LOCALE, baseFileName );
    }

    return fileBuilder.build();
  }

  private static boolean isKettleExtension( String extension ) {
    return TRANS_EXT.equalsIgnoreCase( extension ) || JOB_EXT.equalsIgnoreCase( extension );
  }
}
