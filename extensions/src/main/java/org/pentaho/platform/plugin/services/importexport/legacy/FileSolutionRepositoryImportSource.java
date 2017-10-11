/*
 * Copyright 2002 - 2017 Hitachi Vantara.  All rights reserved.
 * 
 * This software was developed by Hitachi Vantara and is provided under the terms
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. TThe Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
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
