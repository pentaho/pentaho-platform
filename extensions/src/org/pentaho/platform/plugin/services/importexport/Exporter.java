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

package org.pentaho.platform.plugin.services.importexport;

import org.apache.commons.io.IOUtils;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.repository2.unified.webservices.DefaultUnifiedRepositoryWebService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author wseyler
 * 
 */
public class Exporter {
  IUnifiedRepository unifiedRepository;
  private String repoPath;
  private String filePath;

  private File exportDir;

  protected DefaultUnifiedRepositoryWebService repoWs;

  /**
   * @param unifiedRepository
   */
  public Exporter( IUnifiedRepository unifiedRepository ) {
    this( unifiedRepository, null, null );
  }

  /**
   * @param unifiedRepository
   */
  public Exporter( IUnifiedRepository unifiedRepository, String path ) {
    this( unifiedRepository, path, null );
  }

  /**
   * @param unifiedRepository
   * @param path
   *          (repo)
   * @param filePath
   */
  public Exporter( IUnifiedRepository unifiedRepository, String path, String filePath ) {
    this.unifiedRepository = unifiedRepository;
    this.repoPath = path;
    this.filePath = filePath;
  }

  /**
   * 
   * @throws java.io.IOException
   */
  public void doExport() throws IOException {
    exportDir = new File( filePath );
    RepositoryFile exportRepositoryFile = unifiedRepository.getFile( repoPath );

    if ( exportRepositoryFile == null ) {
      throw new FileNotFoundException( Messages.getInstance().getErrorString(
          "Exporter.ERROR_0001_INVALID_SOURCE_DIRECTORY", repoPath ) );
    }

    if ( exportRepositoryFile.isFolder() ) { // Handle recursive export
      exportDirectory( exportRepositoryFile, exportDir );
    } else { // Handle a single file export
      exportFile( exportRepositoryFile, exportDir );
    }
  }

  /**
   * 
   * @return
   * @throws java.io.IOException
   */
  public File doExportAsZip() throws IOException {
    RepositoryFile exportRepositoryFile = unifiedRepository.getFile( repoPath );
    return doExportAsZip( exportRepositoryFile );
  }

  /**
   * 
   * @param exportRepositoryFile
   * @return
   * @throws java.io.IOException
   */
  public File doExportAsZip( RepositoryFile exportRepositoryFile ) throws IOException {
    File zipFile = File.createTempFile( "repoExport", ".zip" );
    zipFile.deleteOnExit();

    filePath = new File( repoPath ).getParent();
    if ( exportRepositoryFile == null ) {
      throw new FileNotFoundException( Messages.getInstance().getErrorString(
          "Exporter.ERROR_0001_INVALID_SOURCE_DIRECTORY", repoPath ) );
    }

    ZipOutputStream zos = new ZipOutputStream( new FileOutputStream( zipFile ) );
    if ( exportRepositoryFile.isFolder() ) { // Handle recursive export
      ZipEntry entry = new ZipEntry( exportRepositoryFile.getPath().substring( filePath.length() + 1 ) + "/" );
      zos.putNextEntry( entry );
      exportDirectoryAsZip( exportRepositoryFile, zos );
    } else {
      exportFileAsZip( exportRepositoryFile, zos );
    }

    zos.close();
    return zipFile;
  }

  /**
   * @param repositoryDir
   * @param zos
   */
  private void exportDirectoryAsZip( RepositoryFile repositoryDir, ZipOutputStream zos ) throws IOException {
    List<RepositoryFile> children = unifiedRepository.getChildren( repositoryDir.getId() );
    for ( RepositoryFile repoFile : children ) {
      if ( repoFile.isFolder() ) {
        ZipEntry entry = new ZipEntry( repoFile.getPath().substring( filePath.length() + 1 ) + "/" );
        zos.putNextEntry( entry );
        exportDirectoryAsZip( repoFile, zos );
      } else {
        exportFileAsZip( repoFile, zos );
      }
    }
  }

  /**
   * @param exportRepositoryFile
   * @param zos
   */
  private void exportFileAsZip( RepositoryFile exportRepositoryFile, ZipOutputStream zos ) throws IOException {
    ZipEntry entry = new ZipEntry( exportRepositoryFile.getPath().substring( filePath.length() + 1 ) );
    zos.putNextEntry( entry );
    SimpleRepositoryFileData repoFileData =
        unifiedRepository.getDataForRead( exportRepositoryFile.getId(), SimpleRepositoryFileData.class );
    InputStream is = repoFileData.getStream();
    IOUtils.copy( is, zos );
    zos.closeEntry();
    is.close();
  }

  /**
   * @param repositoryDir
   * @param parentDir
   * @throws java.io.IOException
   */
  public void exportDirectory( RepositoryFile repositoryDir, File parentDir ) throws IOException {
    if ( repositoryDir == null || !repositoryDir.isFolder() ) {
      throw new IllegalArgumentException( Messages.getInstance().getErrorString(
          "Exporter.ERROR_0001_INVALID_SOURCE_DIRECTORY", repositoryDir == null ? "Null" : repositoryDir.getPath() ) );
    }
    if ( parentDir == null ) {
      throw new FileNotFoundException( Messages.getInstance()
          .getErrorString( "Exporter.ERROR_0002_MISSING_DESTINATION" ) );
    }
    parentDir = new File( parentDir, repositoryDir.getName() );

    if ( !parentDir.mkdirs() ) {
      throw ( new IOException() );
    }

    List<RepositoryFile> children = unifiedRepository.getChildren( repositoryDir.getId() );
    for ( RepositoryFile repoFile : children ) {
      if ( repoFile.isFolder() ) {
        exportDirectory( repoFile, parentDir );
      } else {
        exportFile( repoFile, parentDir );
      }
    }
  }

  /**
   * 
   * @param exportRepositoryFile
   * @param exportDirectory
   * @throws java.io.IOException
   */
  public void exportFile( RepositoryFile exportRepositoryFile, File exportDirectory ) throws IOException {
    if ( exportDirectory.exists() ) {
      if ( !exportDirectory.isDirectory() ) {
        throw new IllegalArgumentException( Messages.getInstance().getErrorString(
            "Exporter.ERROR_0004_INVALID_DESTINATION_DIRECTORY", exportDirectory.getAbsolutePath() ) );
      }
    } else { // Directory doesn't exist so create it
      if ( !exportDirectory.mkdirs() ) {
        throw ( new IOException() );
      }
    }

    if ( exportRepositoryFile == null ) {
      throw new FileNotFoundException( Messages.getInstance().getErrorString(
          "Exporter.ERROR_0001_INVALID_SOURCE_DIRECTORY", repoPath ) );
    }

    SimpleRepositoryFileData repoFileData =
        unifiedRepository.getDataForRead( exportRepositoryFile.getId(), SimpleRepositoryFileData.class );
    InputStream is = repoFileData.getStream();
    File exportFile = new File( exportDirectory.getAbsolutePath() + File.separator + exportRepositoryFile.getName() );
    OutputStream os = new FileOutputStream( exportFile );
    IOUtils.copy( is, os );
    os.close();
    is.close();
  }

  /**
   * 
   * @return
   */
  public IUnifiedRepository getUnifiedRepository() {
    return unifiedRepository;
  }

  /**
   * 
   * @param unifiedRepository
   */
  public void setUnifiedRepository( IUnifiedRepository unifiedRepository ) {
    this.unifiedRepository = unifiedRepository;
  }

  /**
   * 
   * @return
   */
  public String getRepoPath() {
    return repoPath;
  }

  /**
   * 
   * @param repoPath
   */
  public void setRepoPath( String repoPath ) {
    this.repoPath = repoPath;
  }

  /**
   * 
   * @return
   */
  public String getFilePath() {
    return filePath;
  }

  /**
   * 
   * @param filePath
   */
  public void setFilePath( String filePath ) {
    this.filePath = filePath;
  }

  /**
   * 
   * @return
   */
  public DefaultUnifiedRepositoryWebService getRepoWs() {
    return repoWs;
  }

  /**
   * 
   * @param repoWs
   */
  public void setRepoWs( DefaultUnifiedRepositoryWebService repoWs ) {
    this.repoWs = repoWs;
  }
}
