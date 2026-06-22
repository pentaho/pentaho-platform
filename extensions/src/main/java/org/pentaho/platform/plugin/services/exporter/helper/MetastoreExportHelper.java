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

import org.apache.commons.io.IOUtils;
import org.castor.core.util.Assert;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.stores.xml.XmlMetaStore;
import org.pentaho.metastore.util.MetaStoreUtil;
import org.pentaho.platform.api.importexport.ExportException;
import org.pentaho.platform.api.importexport.IExportHelper;
import org.pentaho.platform.plugin.services.exporter.PentahoPlatformExporter;
import org.pentaho.platform.plugin.services.exporter.MetaStoreExportUtil;
import org.pentaho.platform.plugin.services.importexport.ComponentConfig;
import org.pentaho.platform.plugin.services.importexport.ImportExportMetrics;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.ExportManifestMetaStore;
import org.pentaho.platform.plugin.services.messages.Messages;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Export helper for metastore configuration.
 * Responsible for:
 * - Lazy-loading and caching the repository metastore
 * - Exporting metastore configuration to the export bundle
 * - Handling errors during metastore initialization and export
 * 
 * The metastore initialization logic was moved here from PentahoPlatformExporter
 * to consolidate metastore handling within the helper itself.
 */
public class MetastoreExportHelper implements IExportHelper {
  private IMetaStore cachedMetastore;

  @Override
  public String getName() {
    return "MetastoreExporter";
  }

  public boolean shouldExecute( Object config ) {
    if ( config instanceof ComponentConfig ) {
      return ( ( ComponentConfig ) config ).isIncludeMetastore();
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
      exportMetastore( exporter );
      if ( exporter.getExportMetrics() != null ) {
        exporter.getExportMetrics().recordSuccess( ImportExportMetrics.Category.METASTORE );
      }
    } catch ( Exception e ) {
      if ( exporter.getExportMetrics() != null ) {
        exporter.getExportMetrics().recordFailure( ImportExportMetrics.Category.METASTORE, "metastore", e );
      }
      throw new ExportException( "Failed to export metastore: " + e.getMessage(), e );
    }
  }

  /**
   * Export metastore configuration to the export bundle.
   * This method contains the full export logic for metastore.
   * 
   * @throws IOException if I/O error occurs
   */
  protected void exportMetastore( PentahoPlatformExporter exporter ) throws IOException {
    exporter.getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_START_EXPORT_METASTORE" ) );
    try {
      exporter.getRepositoryExportLogger().debug( "Starting to copy metastore to a temp location" );
      Path tempDirectory = Files.createTempDirectory( "metastore" );
      IMetaStore xmlMetaStore = new XmlMetaStore( tempDirectory.toString() );
      MetaStoreUtil.copy( getRepoMetaStore( exporter ), xmlMetaStore );
      exporter.getRepositoryExportLogger().debug( "Finished to copying metastore to a temp location" );
      exporter.getRepositoryExportLogger().debug( "Starting to zip the metastore" );
      File zippedMetastore = Files.createTempFile( "metastore", ".zip" ).toFile();
      ZipOutputStream zipOutputStream = new ZipOutputStream( new FileOutputStream( zippedMetastore ) );
      zipFolder( tempDirectory.toFile(), zipOutputStream, tempDirectory.toString(), exporter );
      zipOutputStream.close();
      exporter.getRepositoryExportLogger().debug( "Finished zipping the metastore" );
      // now that we have the zipped content of an xml metastore, we need to write that to the export bundle
      FileInputStream zis = new FileInputStream( zippedMetastore );
      String zipFileLocation = "metastore" + ".mzip";
      ZipEntry metastoreZipFileZipEntry = new ZipEntry( zipFileLocation );
      exporter.getRepositoryExportLogger().debug( "Starting to add the metastore zip to the bundle" );
      exporter.getZipOutputStream().putNextEntry( metastoreZipFileZipEntry );
      exporter.trackFileAdded( zipFileLocation );
      try {
        IOUtils.copy( zis, exporter.getZipOutputStream() );
        exporter.getRepositoryExportLogger().debug( "Finished adding the metastore zip to the bundle" );
      } catch ( IOException e ) {
        throw e;
      } finally {
        zis.close();
        exporter.getZipOutputStream().closeEntry();
      }
      exporter.getRepositoryExportLogger().debug( "Starting to add the metastore to the manifest" );
      // add an ExportManifest entry for the metastore.
      ExportManifestMetaStore exportManifestMetaStore = new ExportManifestMetaStore( zipFileLocation,
          getRepoMetaStore( exporter ).getName(),
          getRepoMetaStore( exporter ).getDescription() );

      exporter.getExportManifest().setMetaStore( exportManifestMetaStore );

      zippedMetastore.deleteOnExit();
      tempDirectory.toFile().deleteOnExit();
      exporter.getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_SUCCESSFUL_EXPORT_METASTORE" ) );
    } catch ( Exception e ) {
      exporter.getRepositoryExportLogger().error( Messages.getInstance().getString( "PentahoPlatformExporter.ERROR.ExportingMetaStore" ) );
      exporter.getRepositoryExportLogger().debug( Messages.getInstance().getString( "PentahoPlatformExporter.ERROR.ExportingMetaStore" ), e );
    }
    exporter.getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_END_EXPORT_METASTORE" ) );
  }

  /**
   * Get or lazy-load the repository metastore.
   * Caches the metastore locally to ensure consistent instance throughout export.
   * 
   * @return IMetaStore instance, or null if unable to initialize
   */
  protected IMetaStore getRepoMetaStore(PentahoPlatformExporter exporter ) {
    if ( cachedMetastore == null ) {
      try {
        cachedMetastore = MetaStoreExportUtil.connectToRepository( null ).getRepositoryMetaStore();
        exporter.getRepositoryExportLogger().debug( "Initialized repository metastore" );
      } catch ( KettleException e ) {
        exporter.getRepositoryExportLogger().debug( "Unable to initialize metastore: " + e.getMessage() );
      }
    }
    return cachedMetastore;
  }

  protected void zipFolder( File file, ZipOutputStream zos, String pathPrefixToRemove, PentahoPlatformExporter exporter ) {
    if ( file.isDirectory() ) {
      File[] listFiles = file.listFiles();
      for ( File listFile : listFiles ) {
        if ( listFile.isDirectory() ) {
          zipFolder( listFile, zos, pathPrefixToRemove, exporter );
        } else {
          if ( !pathPrefixToRemove.endsWith( File.separator ) ) {
            pathPrefixToRemove += File.separator;
          }
          String path = listFile.getPath().replace( pathPrefixToRemove, "" );
          ZipEntry entry = new ZipEntry( path );
          FileInputStream fis = null;
          try {
            zos.putNextEntry( entry );
            exporter.trackFileAdded( path );
            fis = new FileInputStream( listFile );
            IOUtils.copy( fis, zos );
          } catch ( IOException e ) {
            e.printStackTrace();
          } finally {
            try {
              zos.closeEntry();
            } catch ( IOException e ) {
              e.printStackTrace();
            }
            IOUtils.closeQuietly( fis );
          }
        }
      }
    }
  }
}
