/*!
 *
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
 *
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

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