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
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.importexport.ExportException;
import org.pentaho.platform.api.importexport.IExportHelper;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.exporter.PentahoPlatformExporter;
import org.pentaho.platform.plugin.services.importexport.ComponentConfig;
import org.pentaho.platform.plugin.services.importexport.ExportFileNameEncoder;
import org.pentaho.platform.plugin.services.importexport.ImportExportMetrics;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.ExportManifestMetadata;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.plugin.services.metadata.IPentahoMetadataDomainRepositoryExporter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;

/**
 * Export helper for metadata models.
 */
public class MetadataExportHelper implements IExportHelper {
  private IMetadataDomainRepository metadataDomainRepository;


  @Override
  public String getName() {
    return "MetadataExporter";
  }

  public boolean shouldExecute( Object config ) {
    if ( config instanceof ComponentConfig ) {
      return ( ( ComponentConfig ) config ).isIncludeDatasources();
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
      exportMetadataModels( exporter );
      if ( exporter.getExportMetrics() != null ) {
        exporter.getExportMetrics().recordSuccess( ImportExportMetrics.Category.METADATA );
      }
    } catch ( Exception e ) {
      if ( exporter.getExportMetrics() != null ) {
        exporter.getExportMetrics().recordFailure( ImportExportMetrics.Category.METADATA, "models", e );
      }
      throw new ExportException( "Failed to export metadata models: " + e.getMessage(), e );
    }
  }

  /**
   * Export metadata models to the export bundle.
   * This method contains the full export logic for metadata models.
   * 
   * @throws IOException if I/O error occurs
   */
  protected void exportMetadataModels( PentahoPlatformExporter exporter ) throws IOException {
    exporter.getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_START_EXPORT_METADATA" ) );
    int successfulExportMetadataDSCount = 0;
    int metadataDSSize = 0;
    // get all of the metadata models
    Set<String> domainIds = getMetadataDomainRepository().getDomainIds();
    if ( domainIds != null ) {
      metadataDSSize = domainIds.size();
      exporter.getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_COUNT_METADATA_DATASOURCE_TO_EXPORT", metadataDSSize ) );
    }

    for ( String domainId : domainIds ) {
      // get all of the files for this model
      Map<String, InputStream> domainFilesData = getDomainFilesData( domainId );
      exporter.getRepositoryExportLogger().debug( "Starting to backup metadata datasource [ " + domainId + " ]" );
      for ( String fileName : domainFilesData.keySet() ) {
        exporter.getRepositoryExportLogger().trace( "Adding metadata file [ " + fileName + " ]" );
        // write the file to the zip
        String metadataFilePath = PentahoPlatformExporter.METADATA_PATH_IN_ZIP + fileName;
        if ( !metadataFilePath.endsWith( ".xmi" ) ) {
          metadataFilePath += ".xmi";
        }
        String metadataZipEntryName = metadataFilePath;
        if ( exporter.isWithManifest() ) {
          metadataZipEntryName = ExportFileNameEncoder.encodeZipPathName( metadataZipEntryName );
        }
        ZipEntry zipEntry = new ZipEntry( metadataZipEntryName );
        InputStream inputStream = domainFilesData.get( fileName );

        try {
          exporter.getZipOutputStream().putNextEntry( zipEntry );
          exporter.trackFileAdded( metadataZipEntryName );
          IOUtils.copy( inputStream, exporter.getZipOutputStream() );

          // add the info to the exportManifest
          ExportManifestMetadata metadata = new ExportManifestMetadata();
          metadata.setDomainId( domainId );
          metadata.setFile( metadataFilePath );
          exporter.getExportManifest().addMetadata( metadata );
          successfulExportMetadataDSCount++;
        } catch ( IOException e ) {
          exporter.getRepositoryExportLogger().warn( Messages.getInstance().getString( "PentahoPlatformExporter.ERROR_METADATA_DATASOURCE_EXPORT", e.getMessage() ), e );
        } finally {
          IOUtils.closeQuietly( inputStream );
          try {
            exporter.getZipOutputStream().closeEntry();
          } catch ( IOException e ) {
            // can't close the entry of input stream
          }
        }
        exporter.getRepositoryExportLogger().trace( "Successfully added metadata file [ " + fileName + " ] to the manifest" );
      }
      exporter.getRepositoryExportLogger().debug( "Successfully perform backup of metadata datasource [ " + domainId + " ]" );
    }
    exporter.getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_SUCCESSFUL_METADATA_DATASOURCE_EXPORT_COUNT", successfulExportMetadataDSCount, metadataDSSize ) );

    exporter.getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_END_EXPORT_METADATA" ) );
  }

  public IMetadataDomainRepository getMetadataDomainRepository() {
    if ( metadataDomainRepository == null ) {
      metadataDomainRepository = PentahoSystem.get( IMetadataDomainRepository.class, PentahoSessionHolder.getSession() );
    }
    return metadataDomainRepository;
  }

  public void setMetadataDomainRepository( IMetadataDomainRepository metadataDomainRepository ) {
    this.metadataDomainRepository = metadataDomainRepository;
  }

  public Map<String, InputStream> getDomainFilesData( String domainId ) {
    return ( (IPentahoMetadataDomainRepositoryExporter) getMetadataDomainRepository() ).getDomainFilesData( domainId );
  }
}
