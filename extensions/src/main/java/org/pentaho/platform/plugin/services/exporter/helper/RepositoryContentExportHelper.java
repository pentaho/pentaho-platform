/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.plugin.services.exporter.helper;

import org.castor.core.util.Assert;
import org.pentaho.platform.api.importexport.ExportException;
import org.pentaho.platform.api.importexport.IExportHelper;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.plugin.services.exporter.PentahoPlatformExporter;
import org.pentaho.platform.plugin.services.importexport.ComponentConfig;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.repository2.ClientRepositoryPaths;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.ZipEntry;

/**
 * Export helper for repository content (files and folders).
 * Handles conditional export of repository content based on backup profile settings.
 */
public class RepositoryContentExportHelper implements IExportHelper {

  @Override
  public String getName() {
    return "RepositoryContentExporter";
  }

  /**
   * Determine if repository content export should be performed.
   * Content is only exported if explicitly requested in the backup profile configuration.
   */
  public boolean shouldExecute( Object config ) {
    if ( config instanceof ComponentConfig ) {
      return ( ( ComponentConfig ) config ).isIncludeContent();
    }
    return false;
  }

  @Override
  public void doExport( Object exportArg ) throws ExportException {
    Assert.notNull( exportArg, "PentahoPlatformExporter is expected to be not null");
    PentahoPlatformExporter exporter = (PentahoPlatformExporter) exportArg;

    Object config = exporter.getComponentConfig();
    if ( !shouldExecute( config ) ) {
      return;
    }

    try {
      RepositoryFile rootFolder = exporter.getUnifiedRepository().getFile( "/" );
      exportFileContent( rootFolder, exporter );
    } catch ( Exception e ) {
      throw new ExportException( "Failed to export repository content: " + e.getMessage(), e );
    }
  }

  /**
   * Export file/folder content from the repository.
   * 
   * @param exportRepositoryFile the file or folder to export
   * @throws IOException if I/O error occurs
   * @throws ExportException if export error occurs
   */
  protected void exportFileContent( RepositoryFile exportRepositoryFile, PentahoPlatformExporter exporter ) throws IOException, ExportException {
    exporter.getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_START_EXPORT_REPOSITORY_OBJECT" ) );
    // get the file path
    String filePath = new File( exporter.getPath() ).getParent();
    if ( filePath == null ) {
      filePath = "/";
    }

    // send a response right away if not found
    if ( exportRepositoryFile == null ) {
      throw new FileNotFoundException( "JCR file not found: " + exporter.getPath() );
    }

    if ( exportRepositoryFile.isFolder() ) {
      exporter.getRepositoryExportLogger().trace( "Repository object [ " + exportRepositoryFile.getName() + "] is a folder" );
      exporter.getExportManifest().getManifestInformation().setRootFolder( exporter.getPath().substring( 0, exporter.getPath().lastIndexOf( "/" ) + 1 ) );

      // don't zip root folder without name
      if ( !ClientRepositoryPaths.getRootFolderPath().equals( exportRepositoryFile.getPath() ) ) {
        exporter.getRepositoryExportLogger().trace( "Adding a name to the root folder" );
        String zipEntryName = exporter.getFixedZipEntryName( exportRepositoryFile, filePath );
        exporter.getZipStream().putNextEntry( new ZipEntry( zipEntryName ) );
      }

      exporter.getRepositoryExportLogger().debug( "Starting recursive backup of a folder [ " + exportRepositoryFile.getName() + " ]" );
      exporter.exportDirectory( exportRepositoryFile, exporter.getZipStream(), filePath );

    } else {
      exporter.getRepositoryExportLogger().trace( "Repository object [ " + exportRepositoryFile.getName() + "] is a file" );
      exporter.getExportManifest().getManifestInformation().setRootFolder( exporter.getPath().substring( 0, exporter.getPath().lastIndexOf( "/" ) + 1 ) );

      try {
        exporter.getRepositoryExportLogger().debug( "Starting backup of a file [ " + exportRepositoryFile.getName() + " ]" );
        exporter.exportFile( exportRepositoryFile, exporter.getZipStream(), filePath );
      } catch ( ExportException | IOException exception ) {
        exporter.getRepositoryExportLogger().error( Messages.getInstance().getString( "PentahoPlatformExporter.ERROR_EXPORT_REPOSITORY_OBJECT", exportRepositoryFile.getName() ) );
      } finally {
        exporter.getRepositoryExportLogger().debug( "Finished the backup of a file [ " + exportRepositoryFile.getName() + " ]" );
      }
    }
    exporter.getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_END_EXPORT_REPOSITORY_OBJECT" ) );
  }
}
