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
 * Copyright (c) 2002-2014 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.web.servlet;

import junit.framework.TestCase;
import mondrian.olap.Connection;
import mondrian.olap.DriverManager;
import mondrian.olap.MondrianException;
import mondrian.rolap.RolapConnection;
import mondrian.xmla.XmlaHandler;
import org.dom4j.Document;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.platform.api.engine.ISecurityHelper;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.engine.services.solution.PentahoEntityResolver;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogHelper;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.servlet.ServletConfig;
import java.util.Properties;
import java.util.concurrent.Callable;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DriverManager.class)
@PowerMockIgnore("javax.management.*")
public class PentahoXmlaServletTest  {

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
          + "<DataSourceInfo>DataSource=foodmart;EnableXmla=true;Provider=mondrian;Datasource=\"foodmart\";overwrite=\"false\"</DataSourceInfo>\n"
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
        XmlDom4JHelper.getDocFromString(
            new PentahoXmlaServlet().makeContentFinder( "fakeurl" ).getContent(),
            new PentahoEntityResolver() );

    assertEquals( 2,
        content.selectNodes( "/DataSources/DataSource/Catalogs/Catalog" ).size() );
    assertNotNull( content.selectNodes(
        "/DataSources/DataSource/Catalogs/Catalog[@name='EnabledCatalog']" ) );
    assertNotNull( content.selectNodes(
        "/DataSources/DataSource/Catalogs/Catalog[@name='FoodMart']" ) );

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
      assertTrue( e.getCause().getCause().getMessage().contains(
          "DataSourceInfo not defined for SampleData" ) );
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


    doReturn( mondrianCatalog ).when( catalogService ).getCatalog(  anyString(), anyObject() );
    PowerMockito.mockStatic(DriverManager.class);

    when(DriverManager.getConnection(anyString(), anyObject())).thenReturn( mock( RolapConnection.class ));


    PentahoSystem.registerObject( catalogService );

    PentahoXmlaServlet xmlaServlet = spy( new PentahoXmlaServlet() );

    XmlaHandler.ConnectionFactory connectionFactory =
        xmlaServlet.createConnectionFactory( mock( ServletConfig.class ) );

    Properties properties = new Properties();
    properties.put("DataSource", "bogus");
    try {
      connectionFactory.getConnection( "SampleData", "SampleData", "baz", properties );
    } catch( MondrianException exception ){
      //ignored
    }

    try {
      connectionFactory.getConnection( "SampleData", "SampleData", "baz", properties );
    } catch( MondrianException exception ){
      //ignored
    }

    // We verify that only one Catalog Locator is created for multiple requests
    verify( xmlaServlet, times(1) ).makeCatalogLocator( anyObject() );

  }
}
