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
import mondrian.olap.MondrianException;
import org.dom4j.Document;
import org.pentaho.platform.api.engine.ISecurityHelper;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.engine.services.solution.PentahoEntityResolver;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

import java.util.concurrent.Callable;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class PentahoXmlaServletTest extends TestCase {

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


  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    SecurityHelper.setMockInstance( null );
  }

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

}
