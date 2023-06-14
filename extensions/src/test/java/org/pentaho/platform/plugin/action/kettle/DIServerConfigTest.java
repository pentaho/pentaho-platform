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

package org.pentaho.platform.plugin.action.kettle;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.metastore.MetaStoreConst;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.www.SlaveServerConfig;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.stores.delegate.DelegatingMetaStore;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class DIServerConfigTest {

  public static final String SERVER_URL = "http://mockurl:9080/pentaho-di";
  public static final String MOCK_USER = "mockUser";
  private final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

  private SlaveServerConfig baseConfig;
  private PluginRegistry pluginRegistry;
  private Repository purRepository;
  private RepositoryMeta purRepositoryMeta;
  private IMetaStore purMetaStore;
  private LogChannel logChannel;

  @Before
  public void setup() throws Exception {
    IApplicationContext mockAppContext = mock( IApplicationContext.class );
    when( mockAppContext.getFullyQualifiedServerURL() ).thenReturn( SERVER_URL );
    PentahoSystem.setApplicationContext( mockAppContext );

    IPentahoSession pentahoSession = mock( IPentahoSession.class );
    when( pentahoSession.getName() ).thenReturn( MOCK_USER );
    PentahoSessionHolder.setSession( pentahoSession );

    baseConfig = new SlaveServerConfig();

    pluginRegistry = mock( PluginRegistry.class );
    purRepositoryMeta = mock( RepositoryMeta.class );
    purRepository = mock( Repository.class );
    purMetaStore = mock( IMetaStore.class );

    when( pluginRegistry
      .loadClass( RepositoryPluginType.class, DIServerConfig.PUR_REPOSITORY_PLUGIN_ID, Repository.class ) )
      .thenReturn( purRepository );
    when( pluginRegistry
      .loadClass( RepositoryPluginType.class, DIServerConfig.PUR_REPOSITORY_PLUGIN_ID, RepositoryMeta.class ) )
      .thenReturn( purRepositoryMeta );

    when( purRepository.getRepositoryMetaStore() ).thenReturn( purMetaStore );
    when( purMetaStore.getName() ).thenReturn( "Mock MetaStore" );

    logChannel = mock( LogChannel.class );
  }

  private Node getConfigNode() throws Exception {
    InputSource xmlSource = new InputSource( new StringReader( baseConfig.getXML() ) );
    Document document = documentBuilderFactory.newDocumentBuilder().parse( xmlSource );
    return XMLHandler.getSubNode( document, SlaveServerConfig.XML_TAG );
  }

  @Test
  public void testGetRepositoryWithDefault() throws Exception {
    DIServerConfig diConfig = new DIServerConfig( logChannel, getConfigNode(), pluginRegistry );
    assertEquals( purRepository, diConfig.getRepository() );

    verifyConnection();
  }

  @Test
  public void testGetRepositoryWithConfig() throws Exception {
    Repository repo = mock( Repository.class );

    DIServerConfig diConfig = new DIServerConfig( logChannel, getConfigNode(), pluginRegistry );
    diConfig.setRepository( repo );
    assertEquals( repo, diConfig.getRepository() );

    verify( purRepository, never() ).init( any( RepositoryMeta.class ) );
    verify( purRepository, never() ).connect( nullable( String.class ), nullable( String.class ) );
  }

  @Test
  public void testGetMetaStoreWithDefault() throws Exception {
    MetaStoreConst.enableDefaultToLocalXml();
    DIServerConfig diConfig = new DIServerConfig( logChannel, getConfigNode(), pluginRegistry );
    IMetaStore delegatingMetaStore = diConfig.getMetaStore();
    assertEquals( purMetaStore, delegatingMetaStore );

    verifyConnection();
  }

  @Test
  public void testGetMetaStoreWithConfig() throws Exception {
    Repository repo = mock( Repository.class );
    DelegatingMetaStore delegatingMetaStore = mock( DelegatingMetaStore.class );
    DIServerConfig diConfig = new DIServerConfig( logChannel, getConfigNode(), pluginRegistry );

    diConfig.setRepository( repo );
    diConfig.setMetastoreSupplier( () -> delegatingMetaStore );

    assertEquals( delegatingMetaStore, diConfig.getMetaStore() );

    verifyNoMoreInteractions( delegatingMetaStore );
    verify( purRepository, never() ).init( any( RepositoryMeta.class ) );
    verify( purRepository, never() ).connect( nullable( String.class ), nullable( String.class ) );
  }

  private void verifyConnection() throws KettleException {
    InOrder connectionProcess = inOrder( purRepositoryMeta, purRepository );

    connectionProcess.verify( purRepositoryMeta ).loadXML( any( Node.class ), any( List.class ) );
    connectionProcess.verify( purRepository ).init( purRepositoryMeta );
    connectionProcess.verify( purRepository ).connect( eq( MOCK_USER ), nullable( String.class ) );
  }

}
