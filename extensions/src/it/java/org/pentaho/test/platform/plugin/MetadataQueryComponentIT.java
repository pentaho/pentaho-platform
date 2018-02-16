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

package org.pentaho.test.platform.plugin;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.metadata.model.Category;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.SqlDataSource;
import org.pentaho.metadata.model.SqlDataSource.DataSourceType;
import org.pentaho.metadata.model.SqlPhysicalColumn;
import org.pentaho.metadata.model.SqlPhysicalModel;
import org.pentaho.metadata.model.SqlPhysicalTable;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.metadata.model.concept.types.TargetTableType;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.metadata.repository.InMemoryMetadataDomainRepository;
import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory.Scope;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.connection.datasource.dbcp.JndiDatasourceService;
import org.pentaho.platform.engine.services.solution.SolutionEngine;
import org.pentaho.platform.plugin.action.pentahometadata.MetadataQueryComponent;
import org.pentaho.platform.plugin.services.connections.sql.SQLConnection;
import org.pentaho.platform.repository2.unified.fs.FileSystemBackedUnifiedRepository;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.pentaho.test.platform.utils.TestResourceLocation;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings( "nls" )
public class MetadataQueryComponentIT {

  private MicroPlatform microPlatform;

  private IPentahoResultSet resultSet;

  @Before
  public void setUp() throws Exception {
    microPlatform = new MicroPlatform( TestResourceLocation.TEST_RESOURCES + "/solution" );
    microPlatform.define( ISolutionEngine.class, SolutionEngine.class );
    microPlatform.define( IMetadataDomainRepository.class, InMemoryMetadataDomainRepository.class, Scope.GLOBAL );
    microPlatform.define( "connection-SQL", SQLConnection.class );
    microPlatform.define( IUnifiedRepository.class, FileSystemBackedUnifiedRepository.class, Scope.GLOBAL );
    FileSystemBackedUnifiedRepository repos = (FileSystemBackedUnifiedRepository) PentahoSystem.get( IUnifiedRepository.class );
    repos.setRootDir( new File( TestResourceLocation.TEST_RESOURCES + "/solution" ) );

    microPlatform.define( IDBDatasourceService.class, JndiDatasourceService.class, Scope.GLOBAL );
    KettleEnvironment.init( false );
    IMetadataDomainRepository repo = PentahoSystem.get( IMetadataDomainRepository.class, null );
    Domain domain = getBasicDomain();
    Domain domain2 = getJdbcDomain();

    Domain domain3 = getJdbcDomain();
    domain3.setId( "JDBCDOMAIN2" );
    domain3.getLogicalModels().get( 0 ).setProperty( "max_rows", new BigDecimal( 10 ) );

    Domain domain4 = getBasicDomain();
    ( (SqlPhysicalModel) domain4.getPhysicalModels().get( 0 ) ).getDatasource().setDialectType( "MYSQL" );
    Map<String, String> attributes = new HashMap<String, String>();
    attributes.put( "QUOTE_ALL_FIELDS", "Y" );
    ( (SqlPhysicalModel) domain4.getPhysicalModels().get( 0 ) ).getDatasource().setAttributes( attributes );

    domain4.setId( "MYSQL_DOMAIN" );
    repo.storeDomain( domain, true );
    repo.storeDomain( domain2, true );
    repo.storeDomain( domain3, true );
    repo.storeDomain( domain4, true );

    // JNDI
    System.setProperty( "java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory" ); //$NON-NLS-1$ //$NON-NLS-2$
    System.setProperty( "org.osjava.sj.root", TestResourceLocation.TEST_RESOURCES + "/solution/system/simple-jndi" ); //$NON-NLS-1$ //$NON-NLS-2$
    System.setProperty( "org.osjava.sj.delimiter", "/" ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  @After
  public void tearDown() {
    if ( resultSet != null ) {
      resultSet.close();
      resultSet.closeConnection();
      resultSet = null;
    }
  }

  @Test
  public void testParameters() {
    String mql =
        "<mql><domain_id>DOMAIN</domain_id><model_id>MODEL</model_id>"
            + "<parameters><parameter name=\"param1\" type=\"STRING\" defaultValue=\"A%\"/></parameters>"
            + "<selections><selection>"
            + "<view>CATEGORY</view>"
            + "<column>LC_CUSTOMERNAME</column>"
            + "</selection>"
            + "</selections>"
            + "<constraints>"
            + "<constraint><operator>AND</operator><condition>LIKE([CATEGORY.LC_CUSTOMERNAME];"
            + "[param:param1])</condition></constraint>"
            + "</constraints>" + "</mql>";

    exucuteComponent( mql, null );

    Assert.assertNotNull( resultSet );
    Assert.assertEquals( 16, resultSet.getRowCount() );

    Map<String, Object> inputs = new HashMap<String, Object>();
    inputs.put( "param1", "B%" );

    exucuteComponent( mql, inputs );

    Assert.assertNotNull( resultSet );
    Assert.assertEquals( 5, resultSet.getRowCount() );
  }

  @Test
  public void testMultiValuedParameters() {
    String mql =
        "<mql><domain_id>DOMAIN</domain_id><model_id>MODEL</model_id>"
            + "<parameters><parameter name=\"param1\" type=\"STRING\" defaultValue=\"Alpha Cognac|"
            + "ANG Resellers|&quot;American Souvenirs Inc|test|quoted&quot;\"/></parameters>"
            + "<selections><selection>"
            + "<view>CATEGORY</view>"
            + "<column>LC_CUSTOMERNAME</column>"
            + "</selection>"
            + "</selections>"
            + "<constraints>"
            + "<constraint><operator>AND</operator><condition>EQUALS([CATEGORY.LC_CUSTOMERNAME];"
            + "[param:param1])</condition></constraint>"
            + "</constraints>" + "</mql>";

    exucuteComponent( mql, null );

    Assert.assertNotNull( resultSet );
    Assert.assertEquals( 2, resultSet.getRowCount() );

    Map<String, Object> inputs = new HashMap<String, Object>();
    inputs.put( "param1", new String[] {
      "BG&E Collectables",
      "Baane Mini Imports",
      "Bavarian Collectables Imports, Co.",
      "Boards & Toys Co." } );

    exucuteComponent( mql, inputs );

    Assert.assertNotNull( resultSet );
    Assert.assertEquals( 4, resultSet.getRowCount() );

    mql =
        "<mql><domain_id>DOMAIN</domain_id><model_id>MODEL</model_id>"
            + "<parameters><parameter name=\"param1\" type=\"NUMERIC\" defaultValue=\"1504|1337\"/></parameters>"
            + "<selections><selection>"
            + "<view>CATEGORY</view>"
            + "<column>LC_CUSTOMERNAME</column>"
            + "</selection><selection>"
            + "<view>CATEGORY</view>"
            + "<column>LC_SALESREP</column>"
            + "</selection>"
            + "</selections>"
            + "<constraints>"
            + "<constraint><operator>AND</operator><condition>EQUALS([CATEGORY.LC_SALESREP];"
            + "[param:param1])</condition></constraint>"
            + "</constraints>" + "</mql>";

    exucuteComponent( mql, null );

    Assert.assertNotNull( resultSet );
    Assert.assertEquals( 15, resultSet.getRowCount() );
  }

  @Test
  public void testPreProcessor() {
    microPlatform.define( "sqlGenerator", TestPreSqlGenerator.class );
    String mql =
        "<mql><domain_id>DOMAIN</domain_id><model_id>MODEL</model_id>"
            + "<parameters><parameter name=\"param1\" type=\"STRING\" defaultValue=\"A%\"/></parameters>"
            + "<selections><selection>"
            + "<view>CATEGORY</view>"
            + "<column>LC_CUSTOMERNAME</column>"
            + "</selection>"
            + "</selections>"
            + "<constraints>"
            + "<constraint><operator>AND</operator><condition>LIKE([CATEGORY.LC_CUSTOMERNAME];"
            + "[param:param1])</condition></constraint>"
            + "</constraints>" + "</mql>";

    // Preprocessor test code will add condition narrowing
    // resultset to 6 rows, all customers starting with 'Au'
    exucuteComponent( mql, null );

    Assert.assertNotNull( resultSet );
    Assert.assertEquals( 6, resultSet.getRowCount() );
  }

  @Test
  public void testPostProcessor() {
    microPlatform.define( "sqlGenerator", TestPostSqlGenerator.class );

    String mql =
        "<mql><domain_id>DOMAIN</domain_id><model_id>MODEL</model_id>"
            + "<parameters><parameter name=\"param1\" type=\"STRING\" defaultValue=\"A%\"/></parameters>"
            + "<selections><selection>"
            + "<view>CATEGORY</view>"
            + "<column>LC_CUSTOMERNAME</column>"
            + "</selection>"
            + "</selections>"
            + "<constraints>"
            + "<constraint><operator>AND</operator><condition>LIKE([CATEGORY.LC_CUSTOMERNAME];"
            + "[param:param1])</condition></constraint>"
            + "</constraints>" + "</mql>";

    // Postprocessor test code will add condition changing
    // resultset to 10 rows, all contactfirstnames starting with 'A'

    exucuteComponent( mql, null );

    Assert.assertNotNull( resultSet );
    Assert.assertEquals( 10, resultSet.getRowCount() );
  }

  @Test
  public void testComponent() {
    String mql =
        "<mql><domain_id>DOMAIN</domain_id><model_id>MODEL</model_id>" + "<selections><selection>"
            + "<view>CATEGORY</view>" + "<column>LC_CUSTOMERNAME</column>" + "</selection>" + "</selections></mql>";

    exucuteComponent( mql, null );

    Assert.assertNotNull( resultSet );
    Assert.assertEquals( 1, resultSet.getColumnCount() );
    Assert.assertEquals( 122, resultSet.getRowCount() );
  }

  @Test
  public void testMysqlComponent() {

    // first, test default behavior of forceDb = false
    String mql =
        "<mql><domain_id>MYSQL_DOMAIN</domain_id><model_id>MODEL</model_id>" + "<selections><selection>"
            + "<view>CATEGORY</view>" + "<column>LC_CUSTOMERNAME</column>" + "</selection>" + "</selections></mql>";

    exucuteComponent( mql, null );

    Assert.assertNotNull( resultSet );
    Assert.assertEquals( 1, resultSet.getColumnCount() );
    Assert.assertEquals( 122, resultSet.getRowCount() );

    // second, test with forceDb = true
    Map<String, Object> inputs = new HashMap<String, Object>();
    inputs.put( "forcedbdialect", "true" );

    exucuteComponent( mql, inputs );

    Assert.assertNull( resultSet );
  }

  @Test
  public void testJdbcComponent() {
    String mql =
        "<mql><domain_id>JDBCDOMAIN</domain_id><model_id>MODEL</model_id>" + "<selections><selection>"
            + "<view>CATEGORY</view>" + "<column>LC_CUSTOMERNAME</column>" + "</selection>" + "</selections></mql>";

    exucuteComponent( mql, null );

    Assert.assertNotNull( resultSet );
    Assert.assertEquals( 1, resultSet.getColumnCount() );
    Assert.assertEquals( 122, resultSet.getRowCount() );
  }

  @Test
  public void testApplyTemplates() {
    String mql =
        "<mql><domain_id>{domain}</domain_id><model_id>MODEL</model_id>" + "<selections><selection>"
            + "<view>CATEGORY</view>" + "<column>LC_CUSTOMERNAME</column>" + "</selection>" + "</selections></mql>";

    Map<String, Object> inputs = new HashMap<String, Object>();
    inputs.put( "domain", "JDBCDOMAIN" );

    exucuteComponent( mql, inputs );

    Assert.assertNotNull( resultSet );
    Assert.assertEquals( 1, resultSet.getColumnCount() );
    Assert.assertEquals( 122, resultSet.getRowCount() );
  }

  @Test
  public void testJdbcComponentMaxRows() {
    String mql =
        "<mql><domain_id>JDBCDOMAIN2</domain_id><model_id>MODEL</model_id>" + "<selections><selection>"
            + "<view>CATEGORY</view>" + "<column>LC_CUSTOMERNAME</column>" + "</selection>" + "</selections></mql>";

    exucuteComponent( mql, null );

    Assert.assertNotNull( resultSet );
    Assert.assertEquals( 1, resultSet.getColumnCount() );
    Assert.assertEquals( 10, resultSet.getRowCount() );

    MetadataQueryComponent component = new MetadataQueryComponent();
    component.setQuery( mql );
    component.setMaxRows( 100 );
    component.execute();

    resultSet = component.getResultSet();
    Assert.assertNotNull( resultSet );
    Assert.assertEquals( 1, resultSet.getColumnCount() );
    Assert.assertEquals( 100, resultSet.getRowCount() );
  }

  @Test
  public void testEmptyInputs() {
    String mql =
        "<mql><domain_id>DOMAIN</domain_id><model_id>MODEL</model_id>"
            + "<parameters><parameter name=\"param1\" type=\"STRING\" defaultValue=\"Alpha Cognac|"
            + "ANG Resellers|&quot;American Souvenirs Inc|test|quoted&quot;\"/></parameters>"
            + "<selections><selection>"
            + "<view>CATEGORY</view>"
            + "<column>LC_CUSTOMERNAME</column>"
            + "</selection>"
            + "</selections>"
            + "<constraints>"
            + "<constraint><operator>AND</operator><condition>EQUALS([CATEGORY.LC_CUSTOMERNAME];"
            + "[param:param1])</condition></constraint>"
            + "</constraints>" + "</mql>";
    Map<String, Object> inputs = new HashMap<String, Object>();
    inputs.put( "param1", new String[] { } );

    exucuteComponent( mql, inputs );

    Assert.assertNotNull( resultSet );
    Assert.assertEquals( 0, resultSet.getRowCount() );
  }

  @Test
  public void testEmptyXMLHelper() {
    MetadataQueryComponent component = new MetadataQueryComponent();
    component.setQueryModelXmlHelper( "NonExistClassName" );
    Assert.assertFalse( component.execute() );
  }

  @Test
  public void testEmptyQuery() {
    MetadataQueryComponent component = new MetadataQueryComponent();
    Assert.assertFalse( component.execute() );
  }

  @Test
  public void testValidate() {
    MetadataQueryComponent component = new MetadataQueryComponent();
    Assert.assertFalse( component.validate() );

    component.setQuery( "MQL" );
    Assert.assertTrue( component.validate() );
  }

  private void exucuteComponent( String mql, Map<String, Object> inputs ) {
    MetadataQueryComponent component = new MetadataQueryComponent();
    component.setInputs( inputs );
    component.setQuery( mql );
    component.execute();
    //result set will be closed after tear down method
    resultSet = component.getResultSet();
  }

  public Domain getJdbcDomain() {
    Domain domain = getBasicDomain();
    SqlDataSource dataSource = ( (SqlPhysicalModel) domain.getPhysicalModels().get( 0 ) ).getDatasource();
    dataSource.setType( DataSourceType.NATIVE );
    dataSource.setDatabaseName( "file:src/test/resources/solution/system/data/sampledata" );
    dataSource.setUsername( "pentaho_user" );
    dataSource.setPort( "-1" );
    dataSource.setPassword( "password" );
    domain.setId( "JDBCDOMAIN" );
    return domain;
  }

  public Domain getBasicDomain() {
    SqlPhysicalModel model = new SqlPhysicalModel();
    SqlDataSource dataSource = new SqlDataSource();
    dataSource.setDatabaseName( "SampleData" );
    dataSource.setDialectType( "HYPERSONIC" );
    dataSource.setType( DataSourceType.JNDI );

    model.setDatasource( dataSource );
    SqlPhysicalTable table = new SqlPhysicalTable( model );
    table.setId( "PT1" );
    model.getPhysicalTables().add( table );
    table.setTargetTableType( TargetTableType.INLINE_SQL );
    table.setTargetTable( "select distinct customername, salesrepemployeenumber from customers" );

    SqlPhysicalColumn column = new SqlPhysicalColumn( table );
    column.setId( "PC1" );
    column.setTargetColumn( "CUSTOMERNAME" );
    column.setName( new LocalizedString( "en_US", "Customer Name" ) );
    column.setDescription( new LocalizedString( "en_US", "Customer Name Desc" ) );
    column.setDataType( DataType.STRING );
    table.getPhysicalColumns().add( column );

    SqlPhysicalColumn column2 = new SqlPhysicalColumn( table );
    column2.setId( "PC2" );
    column2.setTargetColumn( "SALESREPEMPLOYEENUMBER" );
    column2.setName( new LocalizedString( "en_US", "Sales Rep" ) );
    column2.setDescription( new LocalizedString( "en_US", "Sales Rep Employee Number" ) );
    column2.setDataType( DataType.NUMERIC );
    table.getPhysicalColumns().add( column2 );

    LogicalModel logicalModel = new LogicalModel();
    logicalModel.setPhysicalModel( model );
    logicalModel.setId( "MODEL" );
    logicalModel.setName( new LocalizedString( "en_US", "My Model" ) );
    logicalModel.setDescription( new LocalizedString( "en_US", "A Description of the Model" ) );

    LogicalTable logicalTable = new LogicalTable();
    logicalTable.setId( "LT" );
    logicalTable.setPhysicalTable( table );

    logicalModel.getLogicalTables().add( logicalTable );

    LogicalColumn logicalColumn = new LogicalColumn();
    logicalColumn.setId( "LC_CUSTOMERNAME" );
    logicalColumn.setPhysicalColumn( column );
    logicalColumn.setLogicalTable( logicalTable );
    logicalTable.addLogicalColumn( logicalColumn );

    LogicalColumn logicalColumn2 = new LogicalColumn();
    logicalColumn2.setId( "LC_SALESREP" );
    logicalColumn2.setPhysicalColumn( column2 );
    logicalColumn2.setLogicalTable( logicalTable );
    logicalTable.addLogicalColumn( logicalColumn2 );

    Category mainCategory = new Category();
    mainCategory.setId( "CATEGORY" );
    mainCategory.setName( new LocalizedString( "en_US", "Category" ) );
    mainCategory.addLogicalColumn( logicalColumn );
    mainCategory.addLogicalColumn( logicalColumn2 );

    logicalModel.getCategories().add( mainCategory );

    Domain domain = new Domain();
    domain.setId( "DOMAIN" );
    domain.addPhysicalModel( model );
    domain.addLogicalModel( logicalModel );

    return domain;
  }

}
