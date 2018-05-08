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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.action.mondrian.catalog;

import java.io.IOException;
import java.io.StringBufferInputStream;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.pentaho.platform.api.cache.IPlatformCache;
import org.pentaho.platform.api.cache.IPlatformCache.CacheScope;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import mockit.Mock;
import mockit.MockUp;
import mockit.NonStrictExpectations;
import mondrian.xmla.DataSourcesConfig;
import mondrian.xmla.DataSourcesConfig.Catalog;
import mondrian.xmla.DataSourcesConfig.Catalogs;
import mondrian.xmla.DataSourcesConfig.DataSource;
import org.pentaho.platform.util.XmlTestConstants;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class MondrianCatalogHelperTest {

  private static final String DEFINITION = "mondrian:";

  private MondrianCatalogHelper mch = new MondrianCatalogHelper();

  @Test
  public void testLoadCatalogsIntoCache() {
    DataSourcesConfig.DataSources dsList = new DataSourcesConfig.DataSources();

    dsList.dataSources = new DataSource[1];
    DataSource ds = new DataSource();
    dsList.dataSources[0] = ds;

    ds.catalogs = new Catalogs();
    ds.catalogs.catalogs = new Catalog[1];

    Catalog ct = new Catalog();
    ct.definition = DEFINITION;

    ds.catalogs.catalogs[0] = ct;

    MockUp<IPlatformCache> pcMock = new MockUp<IPlatformCache>() {

      Object cacheValue;

      @Mock
      public Object get( CacheScope scope, Object key ) {
        return cacheValue;
      }

      @Mock
      public void put( CacheScope scope, Object key, Object value ) {
        cacheValue = value;
      }
    };
    final IPlatformCache pc = pcMock.getMockInstance();

    new NonStrictExpectations( PentahoSystem.class ) {
      {
        PentahoSystem.get( IPlatformCache.class );
        result = pc;
      }
    };

    mch.loadCatalogsIntoCache( dsList, null );

    Object cacheValue = pc.get( null, null );
    Assert.assertTrue( Map.class.isInstance( cacheValue ) );
    Map<?, ?> map = (Map<?, ?>) cacheValue;

    for ( Object item : map.values() ) {
      Assert.assertTrue( MondrianCatalog.class.isInstance( item ) );
      MondrianCatalog catalog = (MondrianCatalog) item;
      Assert.assertEquals( DEFINITION, catalog.getDefinition() );
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
}
