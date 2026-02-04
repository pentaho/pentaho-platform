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

import org.junit.Before;
import org.junit.Test;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.stores.xml.XmlUtil;
import org.pentaho.platform.api.repository2.unified.IPlatformImportBundle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MetaStoreImportHandlerTest {

  MetaStoreImportHandler handler;
  IMetaStore metastore;

  @Before
  public void setUp() throws Exception {
    handler = new MetaStoreImportHandler();
    metastore = mock( IMetaStore.class );
    handler.setRepoMetaStore( metastore );
  }

  private InputStream createMetaStoreZip( String... namespaces ) throws IOException {
    try ( var baos = new ByteArrayOutputStream();
          var zipOut = new ZipOutputStream( baos ) ) {
      var root = new ZipEntry( XmlUtil.META_FOLDER_NAME + "/" );
      zipOut.putNextEntry( root );
      zipOut.closeEntry();
      for ( String folder : namespaces ) {
        var entry = new ZipEntry( XmlUtil.META_FOLDER_NAME + "/" + folder + "/" );
        zipOut.putNextEntry( entry );
        zipOut.closeEntry();
      }
      return new ByteArrayInputStream( baos.toByteArray() );
    }
  }

  @Test
  public void testImportFile() throws Exception {

    IPlatformImportBundle bundle = mock( IPlatformImportBundle.class );
    var ios = createMetaStoreZip( "pentaho", "hitachi" );
    when( bundle.getInputStream() ).thenReturn( ios );
    when( bundle.getName() ).thenReturn( "metastore" );
    when( bundle.getProperty( "description" ) ).thenReturn( "bundle description" );

    handler.importFile( bundle );

    // not going to test all of the internals of the MetaStoreUtil.copy, just enough to make sure it was called.
    verify( metastore ).createNamespace( "pentaho" );
    verify( metastore ).createNamespace( "hitachi" );
  }
}