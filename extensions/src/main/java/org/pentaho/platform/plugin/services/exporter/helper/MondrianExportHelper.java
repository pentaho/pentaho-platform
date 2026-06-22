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
import org.apache.commons.lang.StringEscapeUtils;
import org.castor.core.util.Assert;
import org.pentaho.platform.api.importexport.ExportException;
import org.pentaho.platform.api.importexport.IExportHelper;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.services.exporter.PentahoPlatformExporter;
import org.pentaho.platform.plugin.services.importexport.ComponentConfig;
import org.pentaho.platform.plugin.services.importexport.ImportExportMetrics;
import org.pentaho.platform.plugin.services.importexport.ExportFileNameEncoder;
import org.pentaho.platform.plugin.services.importexport.exportManifest.Parameters;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.ExportManifestMondrian;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.repository.solution.filebased.MondrianVfs;
import org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;

/**
 * Export helper for Mondrian OLAP schemas.
 */
public class MondrianExportHelper implements IExportHelper {
  private IMondrianCatalogService mondrianCatalogService;
  private MondrianCatalogRepositoryHelper mondrianCatalogRepositoryHelper;

  @Override
  public String getName() {
    return "MondrianExporter";
  }

  public boolean shouldExecute( Object config ) {
    if ( config instanceof ComponentConfig ) {
      return ( ( ComponentConfig ) config ).isIncludeMondrian();
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
      exportMondrianSchemas( exporter );
      if ( exporter.getExportMetrics() != null ) {
        exporter.getExportMetrics().recordSuccess( ImportExportMetrics.Category.MONDRIAN );
      }
    } catch ( Exception e ) {
      if ( exporter.getExportMetrics() != null ) {
        exporter.getExportMetrics().recordFailure( ImportExportMetrics.Category.MONDRIAN, "schemas", e );
      }
      throw new ExportException( "Failed to export Mondrian schemas: " + e.getMessage(), e );
    }
  }

  /**
   * Export Mondrian schemas to the export bundle.
   * This method contains the full export logic for Mondrian catalogs.
   * 
   * @throws IOException if I/O error occurs
   */
  protected void exportMondrianSchemas(PentahoPlatformExporter exporter ) throws IOException {
    exporter.getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_START_EXPORT_MONDRIAN_DATASOURCE" ) );
    // Get the mondrian catalogs available in the repo
    int successfulExportMondrianDSCount = 0;
    int mondrianDSSize = 0;
    List<MondrianCatalog> catalogs = getMondrianCatalogService().listCatalogs( exporter.getPublicSession(), false );
    if ( catalogs != null ) {
      mondrianDSSize = catalogs.size();
      exporter.getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_COUNT_MONDRIAN_DATASOURCE_TO_EXPORT", mondrianDSSize ) );
    }
    for ( MondrianCatalog catalog : catalogs ) {
      exporter.getRepositoryExportLogger().debug( "Starting to perform backup mondrian datasource [ " + catalog.getName() + " ]" );
      // get the files for this catalog
      Map<String, InputStream> files = getMondrianCatalogRepositoryHelper( exporter ).getModrianSchemaFiles( catalog.getName() );

      ExportManifestMondrian mondrian = new ExportManifestMondrian();
      for ( String fileName : files.keySet() ) {
        exporter.getRepositoryExportLogger().trace( "Starting to add filename [ " + fileName + " ] with datasource [" + catalog.getName() + " ] to the bundle" );

        // write the file to the zip
        String path = PentahoPlatformExporter.ANALYSIS_PATH_IN_ZIP + catalog.getName() + "/" + fileName;
        ZipEntry zipEntry = new ZipEntry( new ZipEntry( ExportFileNameEncoder.encodeZipPathName( path ) ) );
        InputStream inputStream = files.get( fileName );

        // ignore *.annotated.xml files, they are not needed
        if ( fileName.equals( "schema.annotated.xml" ) ) {
          // these types of files only exist for contextual export of a data source (from the UI) to later be imported in.
          // However, in the case of backup/restore we don't need these since we'll be using the annotations.xml file along
          // with the original schema xml file to re-generate the model properly
          continue;
        } else if ( MondrianVfs.ANNOTATIONS_XML.equals( fileName ) ) {
          // annotations.xml should be written to the zip file and referenced in the export manifest entry for the
          // related mondrian model
          mondrian.setAnnotationsFile( path );
        } else {
          // must be a true mondrian model
          mondrian.setCatalogName( catalog.getName() );
          boolean xmlaEnabled = exporter.parseXmlaEnabled( catalog.getDataSourceInfo() );
          mondrian.setXmlaEnabled( xmlaEnabled );
          mondrian.setFile( path );
          Parameters mondrianParameters = new Parameters();
          mondrianParameters.put( "Provider", "mondrian" );
          //DataSource can be escaped
          mondrianParameters.put( "DataSource", StringEscapeUtils.unescapeXml( catalog.getJndi() ) );
          mondrianParameters.put( "EnableXmla", Boolean.toString( xmlaEnabled ) );

          StreamSupport.stream( catalog.getConnectProperties().spliterator(), false )
              .filter( p -> !mondrianParameters.containsKey( p.getKey() ) )
              //if value is escaped it should be unescaped to avoid double escape after export in xml file, because
              //marshaller executes escaping as well
              .forEach( p -> mondrianParameters.put( p.getKey(), StringEscapeUtils.unescapeXml( p.getValue() ) ) );

          mondrian.setParameters( mondrianParameters );
        }

        try {
          exporter.getZipOutputStream().putNextEntry( zipEntry );
          exporter.trackFileAdded( path );
          IOUtils.copy( inputStream, exporter.getZipOutputStream() );
        } catch ( IOException e ) {
          exporter.getRepositoryExportLogger().error( Messages.getInstance().getString( "PentahoPlatformExporter.ERROR_MONDRIAN_DATASOURCE_EXPORT" ) );
        } finally {
          IOUtils.closeQuietly( inputStream );
          try {
            exporter.getZipOutputStream().closeEntry();
          } catch ( IOException e ) {
            // can't close the entry of input stream
          }
        }
      }
      if ( mondrian.getCatalogName() != null && mondrian.getFile() != null ) {
        exporter.getExportManifest().addMondrian( mondrian );
        exporter.getRepositoryExportLogger().debug( "Successfully added filename [ " + mondrian.getFile() + " ] with catalog [" + mondrian.getCatalogName() + " ] to the bundle" );
        successfulExportMondrianDSCount++;
      }
    }
    exporter.getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_SUCCESSFUL_MONDRIAN_DATASOURCE_EXPORT_COUNT", successfulExportMondrianDSCount, mondrianDSSize ) );

    exporter.getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_END_EXPORT_MONDRIAN_DATASOURCE" ) );
  }

  public IMondrianCatalogService getMondrianCatalogService() {
    if ( mondrianCatalogService == null ) {
      mondrianCatalogService = PentahoSystem.get( IMondrianCatalogService.class, PentahoSessionHolder.getSession() );
    }
    return mondrianCatalogService;
  }

  public void setMondrianCatalogService( IMondrianCatalogService mondrianCatalogService ) {
    this.mondrianCatalogService = mondrianCatalogService;
  }

  public MondrianCatalogRepositoryHelper getMondrianCatalogRepositoryHelper( PentahoPlatformExporter exporter ) {
    if ( this.mondrianCatalogRepositoryHelper == null ) {
      mondrianCatalogRepositoryHelper = new MondrianCatalogRepositoryHelper( exporter.getUnifiedRepository() );
    }
    return mondrianCatalogRepositoryHelper;
  }

  public void setMondrianCatalogRepositoryHelper( MondrianCatalogRepositoryHelper mondrianCatalogRepositoryHelper ) {
    this.mondrianCatalogRepositoryHelper = mondrianCatalogRepositoryHelper;
  }
}
