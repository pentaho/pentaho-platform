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

package org.pentaho.platform.plugin.services.importexport.legacy;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.springframework.util.Assert;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author wseyler
 */
public class FileSolutionRepositoryImportSource extends AbstractImportSource {
  private static final Log log = LogFactory.getLog( FileSolutionRepositoryImportSource.class );

  private String charSet;
  private String filename;
  private final String sourceParentFilePath;
  private final List<IRepositoryFileBundle> files = new ArrayList<IRepositoryFileBundle>();
  private boolean recursive;

  public FileSolutionRepositoryImportSource( final File sourceFile, final String charSet ) {
    this( sourceFile, sourceFile.getName(), charSet );
  }

  public FileSolutionRepositoryImportSource( final File sourceFile, final String filename, final String charSet ) {
    Assert.notNull( sourceFile );
    Assert.hasText( filename );
    Assert.hasText( charSet );
    this.filename = filename;
    this.charSet = charSet;
    this.recursive = sourceFile.isDirectory();
    this.sourceParentFilePath = sourceFile.getAbsoluteFile().getPath();

    addFileToList( sourceFile, false );
    log.debug( "File list built - size=" + files.size() );
  }

  /*
   * (non-Javadoc)
   * 
   * @see ImportSource#getFiles()
   */
  public Iterable<IRepositoryFileBundle> getFiles() {
    return files;
  }

  /**
   * Returns the number of files to process (or -1 if that is not known)
   */
  @Override
  public int getCount() {
    return files.size();
  }

  protected void addFileToList( final File currentFile, final boolean extractFilename ) {
    // Weed out .svn folders
    if ( currentFile == null || !currentFile.exists()
        || ( currentFile.isDirectory() && currentFile.getName().equals( ".svn" ) ) ) {
      return;
    }

    // Extract the filename from the file object
    final String filename = ( extractFilename ? currentFile.getName() : this.filename );

    // Add the file to the list (and get more if it is a folder)
    files.add( getFile( currentFile, filename ) );
    if ( currentFile.isDirectory() ) {
      for ( File child : currentFile.listFiles() ) {
        addFileToList( child, true );
      }
    }
  }

  protected IRepositoryFileBundle getFile( final File currentFile, final String filename ) {
    final String name = filename;
    final boolean directory = currentFile.isDirectory();
    final boolean hidden = false;
    final Date lastModifiedDate = new Date( currentFile.lastModified() );
    final RepositoryFile repoFile =
        new RepositoryFile.Builder( name ).folder( directory ).hidden( hidden ).lastModificationDate( lastModifiedDate )
            .build();

    final String repoPath = getRepositoryPath( currentFile );
    final String extension = RepositoryFilenameUtils.getExtension( filename );
    return new org.pentaho.platform.plugin.services.importexport.RepositoryFileBundle( repoFile, null, repoPath,
        currentFile, charSet, getMimeType( extension.toLowerCase() ) );
  }

  protected String getRepositoryPath( final File currentFile ) {
    String repositoryPath = "";
    if ( recursive ) {
      final String parentFilePath = currentFile.getAbsoluteFile().getParent();
      repositoryPath = StringUtils.substring( parentFilePath, sourceParentFilePath.length() );
    }
    return repositoryPath;
  }
}
