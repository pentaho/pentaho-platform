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
import org.pentaho.metastore.stores.xml.XmlMetaStore;
import org.pentaho.platform.api.repository2.unified.IPlatformImportBundle;

import java.io.InputStream;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MetaStoreImportHandlerTest {

  MetaStoreImportHandler handler;
  IMetaStore metastore;
  XmlMetaStore fromMetaStore;

  @Before
  public void setUp() throws Exception {
    handler = new MetaStoreImportHandler();
    metastore = mock( IMetaStore.class );
    handler.setRepoMetaStore( metastore );
    String[] namespaces = new String[] { "pentaho", "hitachi" };
    fromMetaStore = mock( XmlMetaStore.class );
    when( fromMetaStore.getNamespaces() ).thenReturn( Arrays.asList( namespaces ) );
  }

  @Test
  public void testImportFile() throws Exception {

    IPlatformImportBundle bundle = mock( IPlatformImportBundle.class );
    InputStream ios = mock( InputStream.class );
    byte[] bytes = new byte[]{};
    when( ios.read( any( bytes.getClass() ), anyInt(), anyInt() ) ).thenReturn( -1 );
    when( bundle.getInputStream() ).thenReturn( ios );
    when( bundle.getName() ).thenReturn( "metastore" );
    when( bundle.getProperty( "description" ) ).thenReturn( "bundle description" );
    handler.tmpXmlMetaStore = fromMetaStore;

    handler.importFile( bundle );

    // not going to test all of the internals of the MetaStoreUtil.copy, just enough to make sure it was called.
    verify( metastore ).createNamespace( "pentaho" );
    verify( metastore ).createNamespace( "hitachi" );
    verify( fromMetaStore ).setRootFolder( nullable( String.class ) );
  }
}