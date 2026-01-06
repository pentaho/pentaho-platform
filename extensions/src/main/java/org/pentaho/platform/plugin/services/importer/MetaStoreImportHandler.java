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


package org.pentaho.platform.plugin.services.importer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.stores.xml.XmlMetaStore;
import org.pentaho.metastore.util.MetaStoreUtil;
import org.pentaho.platform.api.mimetype.IMimeType;
import org.pentaho.platform.api.repository2.unified.IPlatformImportBundle;
import org.pentaho.platform.plugin.services.exporter.MetaStoreExportUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.nio.file.Files;

public class MetaStoreImportHandler implements IPlatformImportHandler {
  private static final Log log = LogFactory.getLog( MetaStoreImportHandler.class );

  private static final String METASTORE = "metastore";
  private List<IMimeType> mimeTypes;
  private IMetaStore metastore;

  public MetaStoreImportHandler() {
  }

  public MetaStoreImportHandler( List<IMimeType> mimeTypes ) {
    this.mimeTypes = mimeTypes;
  }

  @Override
  public void importFile( IPlatformImportBundle bundle ) throws IOException {
    Path tempMetastorePath = Files.createTempDirectory( METASTORE );
    try {
      extractBundleToDir( bundle, tempMetastorePath );

      // get a hold of the metastore to import into
      IMetaStore metastore = getRepoMetaStore();
      if ( metastore != null ) {
        // copy the exported metastore to where it needs to go
        importToMetaStore( bundle, tempMetastorePath, metastore );
      }
    } finally {
      FileUtils.deleteDirectory( tempMetastorePath.toFile() );
    }
  }

  private static void importToMetaStore( IPlatformImportBundle bundle, Path tempMetastorePath, IMetaStore metastore ) {
    try {
      XmlMetaStore tmpXmlMetaStore = new XmlMetaStore( tempMetastorePath.toString() );
      tmpXmlMetaStore.setName( bundle.getName() );

      var desc = bundle.getProperty( "description" );
      tmpXmlMetaStore.setDescription( desc == null ? null : desc.toString() );

      MetaStoreUtil.copy( tmpXmlMetaStore, metastore, bundle.overwriteInRepository() );

    } catch ( MetaStoreException e ) {
      log.error( "Could not restore the MetaStore" );
      log.debug( "Error restoring the MetaStore", e );
    }
  }

  private static void extractBundleToDir( IPlatformImportBundle bundle, Path tempMetastorePath ) throws IOException {
    try ( InputStream inputStream = bundle.getInputStream();
          ZipInputStream zis = new ZipInputStream( inputStream ) ) {
      // get the zipped metastore from the export bundle
      ZipEntry entry;
      while ( ( entry = zis.getNextEntry() ) != null ) {
        try {
          String filePath = tempMetastorePath.toString() + File.separator + entry.getName();
          if ( entry.isDirectory() ) {
            File dir = new File( filePath );
            dir.mkdir();
          } else {
            File file = new File( filePath );
            file.getParentFile().mkdirs();
            FileOutputStream fos = new FileOutputStream( filePath );
            IOUtils.copy( zis, fos );
            IOUtils.closeQuietly( fos );
          }
        } finally {
          zis.closeEntry();
        }
      }
    }
  }

  public void setMimeTypes( List<IMimeType> mimeTypes ) {
    this.mimeTypes = mimeTypes;
  }

  @Override
  public List<IMimeType> getMimeTypes() {
    return mimeTypes;
  }

  protected IMetaStore getRepoMetaStore() {
    if ( metastore == null ) {
      try {
        metastore = MetaStoreExportUtil.connectToRepository( null ).getRepositoryMetaStore();
      } catch ( KettleException e ) {
        log.debug( "Can't get the metastore to import into" );
      }
    }
    return metastore;
  }

  protected void setRepoMetaStore( IMetaStore metastore ) {
    this.metastore = metastore;
  }

}
