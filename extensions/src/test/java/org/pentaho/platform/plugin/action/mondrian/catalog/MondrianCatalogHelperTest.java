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
 * Copyright (c) 2002-2020 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.action.mondrian.catalog;

import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mondrian.xmla.DataSourcesConfig;
import mondrian.xmla.DataSourcesConfig.Catalog;
import mondrian.xmla.DataSourcesConfig.Catalogs;
import mondrian.xmla.DataSourcesConfig.DataSource;
import org.junit.Assert;
import org.junit.Test;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.DataProperty;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper;
import org.pentaho.platform.util.XmlTestConstants;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringBufferInputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class MondrianCatalogHelperTest {

  private static final String DEFINITION = "mondrian:";

  private MondrianCatalogHelper mch = new MondrianCatalogHelper() {
    protected boolean hasAccess( MondrianCatalog cat, RepositoryFilePermission permission ) {
      return true;
    }
  };

  ICacheManager cm;

  DataSourcesConfig.DataSources dsList;

  @Mocked RepositoryFile mockRepositoryFolder;
  @Mocked RepositoryFile mockRepositoryFile;
  @Mocked RepositoryFile mockMetatdataFolder;
  @Mocked DataNode metadataNode;
  @Mocked NodeRepositoryFileData mockIRepositoryFileData;
  @Mocked MondrianCatalogRepositoryHelper mockMondrianCatalogRepositoryHelper;

  @Test
  public void testLoadCatalogsIntoCache() {
    setupDsObjects();
    setupCommonObjects();

    mch.loadCatalogsIntoCache( dsList, null );

    Object cacheValue = cm.getFromRegionCache( null, null );

    Assert.assertTrue( MondrianCatalogCache.class.isInstance( cacheValue ) );
    Map<?, ?> map = ( (MondrianCatalogCache) cacheValue ).getCatalogs();

    for ( Object item : map.values() ) {
      Assert.assertTrue( MondrianCatalog.class.isInstance( item ) );
      MondrianCatalog catalog = (MondrianCatalog) item;
      assertEquals( DEFINITION, catalog.getDefinition() );
    }
  }

  @Test( timeout = 2000, expected = SAXException.class )
  public void shouldNotFailAndReturnNullWhenMaliciousXmlIsGiven() throws IOException, ParserConfigurationException, SAXException {
    mch.getMondrianXmlDocument( new StringBufferInputStream( XmlTestConstants.MALICIOUS_XML ) );
    fail();
  }

  @Test
  public void shouldNotFailAndReturnNotNullWhenLegalXmlIsGiven() throws Exception {
    String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
      + "<slave_config>"
      + "</slave_config>";

    assertNotNull( mch.getMondrianXmlDocument( new StringBufferInputStream( xml ) ) );
  }

  // PPP-4192
  @Test
  public void testInconsistentSynchronizationOfDataSourcesConfig() throws Exception {

    MondrianCatalogHelperTestable mondrianCatalogHelper = new MondrianCatalogHelperTestable();

    AtomicReference<Boolean> failed = new AtomicReference<>();
    failed.set( Boolean.FALSE );

    ExecutorService executorService = Executors.newFixedThreadPool( 2 );

    executorService.execute( () -> {
      System.out.println( "validateSynchronizedDataSourcesConfig()" );
      try {
        mondrianCatalogHelper.validateSynchronizedDataSourcesConfig();
      } catch ( InterruptedException e ) {
        e.printStackTrace();
        failed.set( Boolean.TRUE );
      }
      catch ( Exception e ) {
        e.printStackTrace();
        failed.set( Boolean.TRUE );
      }
    });

    executorService.execute( () -> {
      for( int i = 0 ; i < 25 ; i++ ) {
        try {
          Thread.sleep( 10 );
        } catch ( InterruptedException e ) {
          e.printStackTrace();
          failed.set( Boolean.TRUE );
        }
        mondrianCatalogHelper.setDataSourcesConfig( "b" );
      }
      System.out.println( "setDataSourcesConfig() - Finished" );
    });

    executorService.shutdown();

    if( executorService.awaitTermination( 5000, TimeUnit.MILLISECONDS) ) {
      if( failed.get() ) {
        System.out.println( "Failed multi-thread execution" );
        Assert.fail();
      }
      return;
    }
    System.out.println( "Failed by timeout");
    Assert.fail();
  }

  @Test
  public void testGetCatalog() {
    setupCommonObjects();
    setupRepositoryObjects();

    MondrianCatalog cat = mch.getCatalog( "name", null );

    assertNotNull( cat );
    assertEquals( "name", cat.getName() );
    assertEquals( "mondrian:/definition", cat.getDefinition() );
  }

  private void setupDsObjects() {
    dsList = new DataSourcesConfig.DataSources();

    dsList.dataSources = new DataSource[1];
    DataSource ds = new DataSource();
    dsList.dataSources[0] = ds;

    ds.catalogs = new Catalogs();
    ds.catalogs.catalogs = new Catalog[1];

    Catalog ct = new Catalog();
    ct.definition = DEFINITION;

    ds.catalogs.catalogs[0] = ct;
  }

  private void setupCommonObjects() {


    MockUp<ICacheManager> cmMock = new MockUp<ICacheManager>() {

      Object cacheValue;

      @Mock
      public boolean cacheEnabled( String s ) {
        return true;
      }

      @Mock
      public Object getFromRegionCache( String s, Object obj ) {
        return cacheValue;
      }

      @Mock
      public void putInRegionCache( String s, Object obj, Object obj1 ) {
        cacheValue = obj1;
      }
    };

    cm = cmMock.getMockInstance();

    new NonStrictExpectations( PentahoSystem.class ) {
      {
        PentahoSystem.getCacheManager( null );
        result = cm;
      }
    };
  }

  private void setupRepositoryObjects() {
    MockUp<IUnifiedRepository> iUnifiedRepositoryMock = new MockUp<IUnifiedRepository>() {
      @Mock
      public RepositoryFile getFile( String s ) {
        if ( s.equals( "/etc/mondrian" ) ) {
          return mockRepositoryFolder;
        } else {
          if ( s.equals( "/etc/mondrian/name/metadata" ) ) {
            return mockMetatdataFolder;
          }
        }
        return null;
      }

      @Mock
      public <T extends IRepositoryFileData> T getDataForRead( final Serializable fileId, final Class<T> dataClass ) {
        return (T) mockIRepositoryFileData;
      }

      @Mock
      public List<RepositoryFile> getChildren( Serializable s ) {
        return Collections.singletonList( mockRepositoryFile );
      }
    };
    IUnifiedRepository mockIUnifiedRepository = iUnifiedRepositoryMock.getMockInstance();

    new NonStrictExpectations( PentahoSystem.class ) {
      {
        PentahoSystem.get( IUnifiedRepository.class, null );
        result = mockIUnifiedRepository;
      }
    };

    new Expectations() {{
      mockRepositoryFolder.getId();
      result = "1";
      mockRepositoryFile.getName();
      maxTimes = 2;
      result = "name";
      mockMetatdataFolder.getId();
      result = "2";
      mockIRepositoryFileData.getNode();
      result = metadataNode;
      metadataNode.getProperty( "datasourceInfo" );
      result = new DataProperty( "datasourceInfo", "datasourceInfo", DataNode.DataPropertyType.STRING );
      metadataNode.getProperty( "definition" );
      result = new DataProperty( "definition", "mondrian:/definition", DataNode.DataPropertyType.STRING );
    }};

    mch.catalogRepositoryHelper = mockMondrianCatalogRepositoryHelper;
  }

  class MondrianCatalogHelperTestable extends MondrianCatalogHelper {

    public synchronized void validateSynchronizedDataSourcesConfig() throws Exception {
      setDataSourcesConfig( "a" );
      for( int i = 0 ; i < 25 ; i++) {
        Thread.sleep( 20 );
        if ( !"a".equals( getDataSourcesConfig() ) ) {
          throw new Exception("Another thread changed dataSourcesConfig value");
        }
      }
      System.out.println( "validateSynchronizedDataSourcesConfig() - Finished" );
    }
  }

}
