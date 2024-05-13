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

package org.pentaho.platform.web.servlet;

import mondrian.olap.DriverManager;
import mondrian.olap.MondrianException;
import mondrian.olap.Util;
import mondrian.rolap.RolapConnection;
import mondrian.xmla.XmlaException;
import mondrian.xmla.XmlaHandler;
import org.dom4j.Document;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.olap4j.OlapConnection;
import org.pentaho.platform.api.engine.ISecurityHelper;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.engine.services.solution.PentahoEntityResolver;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogHelper;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

import javax.servlet.ServletConfig;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.Callable;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class PentahoXmlaServletTest {
  private static final String DATASOURCE_XML =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
      + "<DataSources>\n"
      + "<DataSource>\n"
      + "<DataSourceName>Pentaho</DataSourceName>\n"
      + "<DataSourceDescription>Pentaho BI Platform Datasources</DataSourceDescription>\n"
      + "<URL>http://localhost:8080/pentaho/Xmla</URL>\n"
      + "<DataSourceInfo>Provider=mondrian</DataSourceInfo>\n"
      + "<ProviderName>PentahoXMLA</ProviderName>\n"
      + "<ProviderType>MDP</ProviderType>\n"
      + "<AuthenticationMode>Unauthenticated</AuthenticationMode>\n"
      + "<Catalogs>\n"
      + "<Catalog name=\"SampleData\">\n"
      + "<DataSourceInfo>DataSource=SampleData;Provider=mondrian;EnableXmla=False</DataSourceInfo>\n"
      + "<Definition>mondrian:/SampleData</Definition>\n"
      + "</Catalog>\n"
      + "<Catalog name=\"SteelWheels\">\n"
      + "<DataSourceInfo>DataSource=SampleData;Provider=mondrian;EnableXmla=\"false\"</DataSourceInfo>\n"
      + "<Definition>mondrian:/SteelWheels</Definition>\n"
      + "</Catalog>\n"
      + "<Catalog name=\"SteelWheels\">\n"
      + "<DataSourceInfo>DataSource=SampleData;Provider=mondrian;EnableXmla='false'</DataSourceInfo>\n"
      + "<Definition>mondrian:/SteelWheels</Definition>\n"
      + "</Catalog>\n"
      + "<Catalog name=\"FoodMart\">\n"
      + "<DataSourceInfo>DataSource=foodmart;EnableXmla=true;Provider=mondrian;Datasource=\"foodmart\";"
      + "overwrite=\"false\"</DataSourceInfo>\n"
      + "<Definition>mondrian:/FoodMart</Definition>\n"
      + "</Catalog>\n"
      + "<Catalog name=\"EnabledCatalog\">\n"
      + "<DataSourceInfo>DataSource=SampleData;Provider=mondrian;EnableXmla=True</DataSourceInfo>\n"
      + "<Definition>mondrian:/SampleData</Definition>\n"
      + "</Catalog>\n"
      + "</Catalogs>\n"
      + "</DataSource>\n"
      + "</DataSources>\n";

  @After
  public void tearDown() throws Exception {
    SecurityHelper.setMockInstance( null );
  }

  @Test
  public void testMakeContentFinderHandlesXmlaEnablement() throws Exception {
    ISecurityHelper securityHelper = mock( ISecurityHelper.class );
    SecurityHelper.setMockInstance( securityHelper );
    when( securityHelper.runAsSystem( any( ( Callable.class ) ) ) ).thenReturn( DATASOURCE_XML );

    Document content =
      XmlDom4JHelper.getDocFromString( new PentahoXmlaServlet().makeContentFinder( "fakeurl" ).getContent(),
        new PentahoEntityResolver() );

    assertEquals( 2, content.selectNodes( "/DataSources/DataSource/Catalogs/Catalog" ).size() );
    assertNotNull( content.selectNodes( "/DataSources/DataSource/Catalogs/Catalog[@name='EnabledCatalog']" ) );
    assertNotNull( content.selectNodes( "/DataSources/DataSource/Catalogs/Catalog[@name='FoodMart']" ) );
  }

  @Test
  public void testInvalidDataSourceInfo() throws Exception {
    ISecurityHelper securityHelper = mock( ISecurityHelper.class );
    SecurityHelper.setMockInstance( securityHelper );
    when( securityHelper.runAsSystem( any( ( Callable.class ) ) ) ).thenReturn(
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<DataSources>\n"
        + "<DataSource>\n"
        + "<DataSourceName>Pentaho</DataSourceName>\n"
        + "<DataSourceDescription>Pentaho BI Platform Datasources</DataSourceDescription>\n"
        + "<URL>http://localhost:8080/pentaho/Xmla</URL>\n"
        + "<DataSourceInfo>Provider=mondrian</DataSourceInfo>\n"
        + "<ProviderName>PentahoXMLA</ProviderName>\n"
        + "<ProviderType>MDP</ProviderType>\n"
        + "<AuthenticationMode>Unauthenticated</AuthenticationMode>\n"
        + "<Catalogs>\n"
        + "<Catalog name=\"SampleData\">\n"
        + "<DataSourceInfo></DataSourceInfo>\n"
        + "<Definition>mondrian:/SampleData</Definition>\n"
        + "</Catalog>\n"
        + "</Catalogs>\n"
        + "</DataSource>\n"
        + "</DataSources>\n"
    );

    try {
      // should throw
      new PentahoXmlaServlet().makeContentFinder( "fakeurl" ).getContent();
    } catch ( MondrianException e ) {
      assertTrue( e.getCause().getCause().getMessage().contains( "DataSourceInfo not defined for SampleData" ) );
      return;
    }

    fail( "Did not throw expected exception." );
  }

  @Test
  public void createConnectionFactory() throws Exception {
    ISecurityHelper securityHelper = mock( ISecurityHelper.class );
    SecurityHelper.setMockInstance( securityHelper );
    when( securityHelper.runAsSystem( any( ( Callable.class ) ) ) ).thenReturn( DATASOURCE_XML );

    IMondrianCatalogService catalogService = mock( MondrianCatalogHelper.class );
    MondrianCatalog mondrianCatalog = mock( MondrianCatalog.class );
    when( mondrianCatalog.getDataSourceInfo() ).thenReturn( "DataSource=foo" );

    doReturn( mondrianCatalog ).when( catalogService ).getCatalog( nullable( String.class ), any() );

    try ( MockedStatic<DriverManager> driverManagerMock = Mockito.mockStatic( DriverManager.class ) ) {
      driverManagerMock.when( () -> DriverManager.getConnection( nullable( String.class ), any() ) )
        .thenReturn( mock( RolapConnection.class ) );

      PentahoSystem.registerObject( catalogService );

      PentahoXmlaServlet xmlaServlet = spy( new PentahoXmlaServlet() );

      XmlaHandler.ConnectionFactory connectionFactory =
        xmlaServlet.createConnectionFactory( mock( ServletConfig.class ) );

      Properties properties = new Properties();
      properties.put( "DataSource", "bogus" );

      try {
        connectionFactory.getConnection( "SampleData", "SampleData", "baz", properties );
      } catch ( MondrianException exception ) {
        //ignored
      }

      try {
        connectionFactory.getConnection( "SampleData", "SampleData", "baz", properties );
      } catch ( MondrianException exception ) {
        //ignored
      }

      // We verify that only one Catalog Locator is created for multiple requests
      verify( xmlaServlet, times( 1 ) ).makeCatalogLocator( any() );
    }
  }

  @Test
  public void testIfConnectionIsXMLANotEnabled() throws Exception {
    OlapConnection connection = mock( OlapConnection.class );
    RolapConnection rc = mock( RolapConnection.class );
    doReturn( rc ).when( connection ).unwrap( RolapConnection.class );

    Util.PropertyList propertyList = new Util.PropertyList();
    propertyList.put( "EnableXmla", "false" );
    when( rc.getConnectInfo() ).thenReturn( propertyList );
    when( rc.getCatalogName() ).thenReturn( "SteelWheels" );

    PentahoXmlaServlet xmlaServlet = spy( new PentahoXmlaServlet() );

    try {
      xmlaServlet.checkIfXMLAEnabled( connection );
      Assert.fail();
    } catch ( Exception e ) {
      assertTrue( e instanceof XmlaException );
    }

    verify( connection, times( 1 ) ).unwrap( RolapConnection.class );
    verify( rc, times( 1 ) ).getConnectInfo();
    verify( rc, times( 1 ) ).getCatalogName();
  }

  @Test
  public void testIfConnectionIsXMLANotEnabledButDontExistProperty() throws Exception {
    OlapConnection connection = mock( OlapConnection.class );
    RolapConnection rc = mock( RolapConnection.class );
    doReturn( rc ).when( connection ).unwrap( RolapConnection.class );

    when( rc.getConnectInfo() ).thenReturn( new Util.PropertyList() );
    when( rc.getCatalogName() ).thenReturn( "SteelWheels" );

    PentahoXmlaServlet xmlaServlet = spy( new PentahoXmlaServlet() );

    try {
      xmlaServlet.checkIfXMLAEnabled( connection );
      Assert.fail();
    } catch ( Exception e ) {
      assertTrue( e instanceof XmlaException );
    }

    verify( connection, times( 1 ) ).unwrap( RolapConnection.class );
    verify( rc, times( 1 ) ).getConnectInfo();
    verify( rc, times( 1 ) ).getCatalogName();
  }

  @Test
  public void testIfConnectionCheckIfXMLAEnabled() throws Exception {
    OlapConnection connection = mock( OlapConnection.class );
    RolapConnection rc = mock( RolapConnection.class );
    doReturn( rc ).when( connection ).unwrap( RolapConnection.class );

    Util.PropertyList propertyList = new Util.PropertyList();
    propertyList.put( "EnableXmla", "true" );
    when( rc.getConnectInfo() ).thenReturn( propertyList );

    PentahoXmlaServlet xmlaServlet = spy( new PentahoXmlaServlet() );
    xmlaServlet.checkIfXMLAEnabled( connection );

    verify( connection, times( 1 ) ).unwrap( RolapConnection.class );
    verify( rc, times( 1 ) ).getConnectInfo();
    verify( rc, times( 0 ) ).getCatalogName();
  }

  @Test
  public void testIfConnectionCheckIfXMLAEnabledException() throws Exception {
    OlapConnection connection = mock( OlapConnection.class );
    RolapConnection rc = mock( RolapConnection.class );
    doThrow( SQLException.class ).when( connection ).unwrap( RolapConnection.class );

    PentahoXmlaServlet xmlaServlet = spy( new PentahoXmlaServlet() );
    xmlaServlet.checkIfXMLAEnabled( connection );

    verify( connection, times( 1 ) ).unwrap( RolapConnection.class );
    verify( rc, times( 0 ) ).getConnectInfo();
    verify( rc, times( 0 ) ).getCatalogName();
  }
}
