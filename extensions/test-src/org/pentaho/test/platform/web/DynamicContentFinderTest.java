/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.test.platform.web;

import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;
import com.mockrunner.mock.web.MockHttpSession;
import com.mockrunner.mock.web.MockServletConfig;
import com.mockrunner.mock.web.MockServletContext;
import junit.framework.Assert;
import mondrian.olap.MondrianProperties;
import mondrian.server.RepositoryContentFinder;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.service.DatabaseDialectService;
import org.pentaho.database.service.IDatabaseDialectService;
import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory.Scope;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.util.IPasswordService;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.SystemSettings;
import org.pentaho.platform.engine.services.connection.datasource.dbcp.JndiDatasourceService;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogHelper;
import org.pentaho.platform.plugin.services.cache.CacheManager;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.util.Base64PasswordService;
import org.pentaho.platform.web.servlet.PentahoXmlaServlet;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.pentaho.test.platform.plugin.UserRoleMapperTest.TestUserDetailsService;
import org.pentaho.test.platform.plugin.UserRoleMapperTest.TestUserRoleListService;
import org.springframework.security.userdetails.UserDetailsService;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.*;

public class DynamicContentFinderTest {

  private MicroPlatform booter;
  private ICacheManager cacheMgr;
  private IUnifiedRepository repo;

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
    repo = mock( IUnifiedRepository.class );
    booter = new MicroPlatform( "test-src/solution" );
    booter.define( IPasswordService.class, Base64PasswordService.class, Scope.GLOBAL );
    booter.define( IDatabaseConnection.class, DatabaseConnection.class, Scope.GLOBAL );
    booter.define( IDatabaseDialectService.class, DatabaseDialectService.class, Scope.GLOBAL );
    booter.define( IMondrianCatalogService.class, MondrianCatalogHelper.class, Scope.GLOBAL );
    booter.define( ICacheManager.class, CacheManager.class, Scope.GLOBAL );
    booter.define( IUserRoleListService.class, TestUserRoleListService.class, Scope.GLOBAL );
    booter.define( UserDetailsService.class, TestUserDetailsService.class, Scope.GLOBAL );
    booter.define( IDBDatasourceService.class, JndiDatasourceService.class, Scope.GLOBAL );
    booter.defineInstance( IUnifiedRepository.class, repo );
    booter.setSettingsProvider( new SystemSettings() );
    booter.start();

    // Clear up the cache
    cacheMgr = PentahoSystem.getCacheManager( null );
    cacheMgr.clearRegionCache( MondrianCatalogHelper.MONDRIAN_CATALOG_CACHE_REGION );

    // Setup the datasources.
    File file1 = new File( "test-src/solution/test/charts/steelwheels.mondrian.xml" );
    String mondrianSchema1 = IOUtils.toString( new FileInputStream( file1 ) );
    File file2 = new File( "test-src/solution/samples/reporting/SampleData.mondrian.xml" );
    String mondrianSchema2 = IOUtils.toString( new FileInputStream( file2 ) );

    final String mondrianFolderPath = ClientRepositoryPaths.getEtcFolderPath() + RepositoryFile.SEPARATOR + "mondrian";
    stubGetFolder( repo, mondrianFolderPath );
    stubGetChildren( repo, mondrianFolderPath, "SampleData/", "SteelWheels/" ); // return two child folders

    final String sampleDataFolderPath = mondrianFolderPath + RepositoryFile.SEPARATOR + "SampleData";
    final String sampleDataMetadataPath = sampleDataFolderPath + RepositoryFile.SEPARATOR + "metadata";
    final String sampleDataSchemaPath = sampleDataFolderPath + RepositoryFile.SEPARATOR + "schema.xml";
    stubGetFile( repo, sampleDataMetadataPath );
    stubGetData( repo, sampleDataMetadataPath, "catalog", pathPropertyPair( "/catalog/definition",
        "mondrian:/SampleData" ), pathPropertyPair( "/catalog/datasourceInfo",
        "Provider=mondrian;DataSource=SampleData" ) );
    stubGetFile( repo, sampleDataSchemaPath );
    stubGetData( repo, sampleDataSchemaPath, mondrianSchema2 );

    final String steelWheelsFolderPath = mondrianFolderPath + RepositoryFile.SEPARATOR + "SteelWheels";
    final String steelWheelsMetadataPath = steelWheelsFolderPath + RepositoryFile.SEPARATOR + "metadata";
    final String steelWheelsSchemaPath = steelWheelsFolderPath + RepositoryFile.SEPARATOR + "schema.xml";
    stubGetFile( repo, steelWheelsMetadataPath );
    stubGetData( repo, steelWheelsMetadataPath, "catalog", pathPropertyPair( "/catalog/definition",
        "mondrian:/SteelWheels" ), pathPropertyPair( "/catalog/datasourceInfo",
        "Provider=mondrian;DataSource=SampleData" ) );
    stubGetFile( repo, steelWheelsSchemaPath );
    stubGetData( repo, steelWheelsSchemaPath, mondrianSchema1 );

    // Setup JNDI
    System.setProperty( "java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory" ); //$NON-NLS-1$ //$NON-NLS-2$
    System.setProperty( "org.osjava.sj.root", "test-src/solution/system/simple-jndi" ); //$NON-NLS-1$ //$NON-NLS-2$
    System.setProperty( "org.osjava.sj.delimiter", "/" ); //$NON-NLS-1$ //$NON-NLS-2$

    MondrianProperties.instance().DataSourceResolverClass
        .set( "org.pentaho.platform.web.servlet.PentahoDataSourceResolver" );
  }

  @After
  public void tearDown() throws Exception {
    cacheMgr.cacheStop();
  }

  @Test
  public void testSingleInstanceForTwoRequests() throws Exception {

    final String response = "<DataSourceName>Pentaho</DataSourceName>";

    MockHttpServletRequest request = new MockHttpServletRequest();

    MockHttpSession session = new MockHttpSession();
    request.setSession( session );
    request.setMethod( "POST" );
    request.setContentType( "text/xml" );
    request.setBodyContent( "<SOAP-ENV:Envelope\n"
        + "    xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n"
        + "    SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" + "  <SOAP-ENV:Body>\n"
        + "    <Discover xmlns=\"urn:schemas-microsoft-com:xml-analysis\"\n"
        + "        SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n"
        + "    <RequestType>DISCOVER_DATASOURCES</RequestType>\n" + "    <Restrictions>\n"
        + "      <RestrictionList>\n" + "      </RestrictionList>\n" + "    </Restrictions>\n" + "    <Properties>\n"
        + "      <PropertyList>\n" + "        <Content>Tabular</Content>\n" + "      </PropertyList>\n"
        + "    </Properties>\n" + "    </Discover>\n" + "</SOAP-ENV:Body>\n" + "</SOAP-ENV:Envelope>\n" );

    MockHttpServletResponse response1 = new MockHttpServletResponse();
    MockHttpServletResponse response2 = new MockHttpServletResponse();

    MockServletContext context = new MockServletContext();
    context.setServletContextName( "pentaho" );

    MockServletConfig config = new MockServletConfig();
    config.setServletContext( context );
    config.setServletName( "Xmla" );
    request.setContextPath( "pentaho" );

    final List<RepositoryContentFinder> finders = new ArrayList<RepositoryContentFinder>();

    final PentahoXmlaServlet servlet = new PentahoXmlaServlet() {
      private static final long serialVersionUID = 1L;

      protected RepositoryContentFinder makeContentFinder( String dataSources ) {
        RepositoryContentFinder finder = super.makeContentFinder( dataSources );
        if ( finders.size() > 0 ) {
          Assert.assertTrue( finders.get( 0 ) == finder );
        }
        finders.add( finder );
        return finders.get( 0 );
      }
    };

    try {
      servlet.init( config );
      servlet.service( request, response1 );
      servlet.service( request, response2 );
      Assert.assertEquals( 1, finders.size() );
      Assert.assertTrue( response1.getOutputStreamContent().contains( response ) );
      Assert.assertTrue( response2.getOutputStreamContent().contains( response ) );
    } finally {
      servlet.destroy();
    }
  }
}
