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

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
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
  protected XmlMetaStore tmpXmlMetaStore;

  public MetaStoreImportHandler() {
  }

  public MetaStoreImportHandler( List<IMimeType> mimeTypes ) {
    this.mimeTypes = mimeTypes;
  }

  @Override
  public void importFile( IPlatformImportBundle bundle )
    throws PlatformImportException, DomainIdNullException, DomainAlreadyExistsException, DomainStorageException,
    IOException {

    InputStream inputStream = bundle.getInputStream();
    Path path = Files.createTempDirectory( METASTORE );
    path.toFile().deleteOnExit();

    // get the zipped metastore from the export bundle
    ZipInputStream zis = new ZipInputStream( inputStream );
    ZipEntry entry;
    while ( ( entry = zis.getNextEntry() ) != null ) {
      try {
        String filePath = path.toString() + File.separator + entry.getName();
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
    IOUtils.closeQuietly( zis );

    // get a hold of the metastore to import into
    IMetaStore metastore = getRepoMetaStore();
    if ( metastore != null ) {
      // copy the exported metastore to where it needs to go
      try {
        if ( tmpXmlMetaStore == null ) {
          tmpXmlMetaStore = new XmlMetaStore( path.toString() );
        } else {
          // we are re-using an existing object, make sure the root folder is pointed at the new location on disk
          tmpXmlMetaStore.setRootFolder( path.toString() + File.separator + METASTORE );
        }
        tmpXmlMetaStore.setName( bundle.getName() );

        String desc = bundle.getProperty( "description" ) == null ? null : bundle.getProperty( "description" ).toString();

        tmpXmlMetaStore.setDescription( desc );

        MetaStoreUtil.copy( tmpXmlMetaStore, metastore, bundle.overwriteInRepository() );

      } catch ( MetaStoreException e ) {
        log.error( "Could not restore the MetaStore" );
        log.debug( "Error restoring the MetaStore", e );
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
        // can't get the metastore to import into
        log.debug( "Can't get the metastore to import into" );
      }
    }
    return metastore;
  }

  protected void setRepoMetaStore( IMetaStore metastore ) {
    this.metastore = metastore;
  }

}
