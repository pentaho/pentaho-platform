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


package org.pentaho.platform.plugin.services.importexport.legacy;

import org.apache.commons.io.IOUtils;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.repository.messages.Messages;
import org.springframework.util.Assert;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author wseyler
 */
public class ZipSolutionRepositoryImportSource extends AbstractImportSource {
  private String charSet;
  private ZipInputStream zipInputStream;
  private List<IRepositoryFileBundle> files;

  /**
   * Creates an Import Source based on a Zip file
   * 
   * @param zipInputStream
   * @param charSet
   */
  public ZipSolutionRepositoryImportSource( final ZipInputStream zipInputStream, final String charSet )
    throws org.pentaho.platform.plugin.services.importexport.InitializationException {
    Assert.notNull( zipInputStream, "ZipInputStream must not be null" );
    Assert.hasText( charSet, "Character set must not be null or empty" );
    this.zipInputStream = zipInputStream;
    this.charSet = charSet;
    this.files = new ArrayList<IRepositoryFileBundle>();

    initialize();
  }

  /**
   * Initializes the ImportSource - it will read the zip input stream and create the list of files
   */
  protected void initialize() throws org.pentaho.platform.plugin.services.importexport.InitializationException {
    try {
      ZipEntry entry = zipInputStream.getNextEntry();
      while ( entry != null ) {
        final String entryName = RepositoryFilenameUtils.separatorsToRepository( entry.getName() );
        final String extension = RepositoryFilenameUtils.getExtension( entryName );
        File tempFile = null;
        boolean isDir = entry.getSize() == 0;
        if ( !isDir ) {
          tempFile = File.createTempFile( "zip", null );
          tempFile.deleteOnExit();
          FileOutputStream fos = new FileOutputStream( tempFile );
          IOUtils.copy( zipInputStream, fos );
          fos.close();
        }
        File file = new File( entryName );
        RepositoryFile repoFile = new RepositoryFile.Builder( file.getName() ).folder( isDir ).hidden( false ).build();
        String parentDir = new File( entryName ).getParent() == null ? "/" : new File( entryName ).getParent() + "/";
        org.pentaho.platform.plugin.services.importexport.RepositoryFileBundle repoFileBundle =
            new org.pentaho.platform.plugin.services.importexport.RepositoryFileBundle( repoFile, null, parentDir,
                tempFile, charSet, getMimeType( extension.toLowerCase() ) );
        files.add( repoFileBundle );
        zipInputStream.closeEntry();
        entry = zipInputStream.getNextEntry();
      }
      zipInputStream.close();
    } catch ( IOException exception ) {
      // TODO I18N
      final String errorMessage = Messages.getInstance().getErrorString( "", exception.getLocalizedMessage() );
      throw new org.pentaho.platform.plugin.services.importexport.InitializationException( errorMessage, exception );
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see ImportSource#getFiles()
   */
  @Override
  public Iterable<IRepositoryFileBundle> getFiles() throws IOException {
    return files;
  }

  /**
   * Returns the number of files to process (or -1 if that is not known)
   */
  @Override
  public int getCount() {
    return files.size();
  }
}
