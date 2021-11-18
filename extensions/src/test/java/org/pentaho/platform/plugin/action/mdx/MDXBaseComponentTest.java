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

package org.pentaho.platform.plugin.action.mdx;

import org.apache.commons.logging.Log;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.actionsequence.dom.ActionInputConstant;
import org.pentaho.actionsequence.dom.IActionInput;
import org.pentaho.actionsequence.dom.IActionOutput;
import org.pentaho.actionsequence.dom.IActionResource;
import org.pentaho.actionsequence.dom.actions.MdxConnectionAction;
import org.pentaho.actionsequence.dom.actions.MdxQueryAction;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.data.DBDatasourceServiceException;
import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.api.data.IPreparedComponent;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.engine.IParameterManager;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryException;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.services.connections.mondrian.MDXConnection;
import org.pentaho.platform.plugin.services.connections.mondrian.MDXResultSet;
import org.pentaho.test.platform.utils.TestResourceLocation;

import javax.sql.DataSource;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by rfellows on 11/2/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class MDXBaseComponentTest {

  MDXBaseComponent mdxBaseComponent;
  MDXBaseComponent baseMdxBaseComponent;

  @Mock MdxQueryAction queryAction;
  @Mock MdxConnectionAction connAction;
  @Mock IActionOutput outputResultSet;
  @Mock IPreparedComponent preparedComponent;
  @Mock IActionInput actionInput;
  @Mock IPentahoConnection conn;
  @Mock IActionInput queryActionInput;
  @Mock IPentahoResultSet resultSet;
  @Mock IRuntimeContext runtimeContext;
  @Mock IParameterManager paramManager;
  @Mock MDXResultSet mdxResultSet;
  @Mock IActionInput catalog;
  @Mock IMondrianCatalogService mondrianCatalogService;
  @Mock MondrianCatalog mondrianCatalog;
  @Mock IPentahoObjectFactory objFactory;
  @Mock IPentahoSession session;
  @Mock MDXConnection mdxConnection;
  @Mock IActionInput connectionStringAction;
  @Mock IActionInput jdbcAction;
  @Mock IActionInput jndiAction;
  @Mock IActionInput locationAction;
  @Mock IActionInput roleAction;
  @Mock IActionResource catalogResource;
  @Mock IActionSequenceResource catalogActionSeqRes;
  @Mock IActionInput userAction;

  @Before
  public void setUp() throws Exception {
    baseMdxBaseComponent = new MDXBaseComponent() {
      @Override public boolean validateSystemSettings() {
        return false;
      }

      @Override public Log getLogger() {
        return null;
      }
    };
    mdxBaseComponent = spy( baseMdxBaseComponent );

    mdxBaseComponent.setActionName( "action name" );
    doNothing().when( mdxBaseComponent ).error( nullable( String.class ), nullable( Throwable.class ) );
    doNothing().when( mdxBaseComponent ).error( nullable( String.class ) );
    doReturn( false ).when( mdxBaseComponent ).fileExistsInRepository( nullable( String.class ) );
  }

  @Test
  public void testValidateAction_noActionDefinition() {
    boolean action = mdxBaseComponent.validateAction();
    assertTrue( action );
  }

  @Test
  public void testValidateAction_queryAction_nullInput() {
    mdxBaseComponent.setActionDefinition( queryAction );

    when( queryAction.getQuery() ).thenReturn( ActionInputConstant.NULL_INPUT );

    boolean action = mdxBaseComponent.validateAction();
    assertFalse( action );
    verify( mdxBaseComponent ).error( nullable( String.class ) );
  }

  @Test
  public void testValidateAction_queryAction_nullResult() {
    mdxBaseComponent.setActionDefinition( queryAction );

    when( queryAction.getOutputResultSet() ).thenReturn( null );
    when( queryAction.getOutputPreparedStatement() ).thenReturn( null );

    boolean action = mdxBaseComponent.validateAction();
    assertFalse( action );
    verify( mdxBaseComponent ).error( nullable( String.class ) );
  }

  @Test
  public void testValidateAction_queryAction() {
    mdxBaseComponent.setActionDefinition( queryAction );

    when( queryAction.getOutputResultSet() ).thenReturn( outputResultSet );

    boolean action = mdxBaseComponent.validateAction();
    assertTrue( action );
    verify( mdxBaseComponent, never() ).error( nullable( String.class ), any( Throwable.class ) );
  }

  @Test
  public void testValidateAction_connectionAction_noConnection() {
    mdxBaseComponent.setActionDefinition( connAction );

    boolean action = mdxBaseComponent.validateAction();
    assertFalse( action );
    verify( mdxBaseComponent ).error( nullable( String.class ) );
  }

  @Test
  public void testValidateAction_connectionAction() throws Exception {
    mdxBaseComponent.setActionDefinition( connAction );
    when( connAction.getOutputConnection() ).thenReturn( outputResultSet );

    boolean action = mdxBaseComponent.validateAction();
    assertTrue( action );
    verify( mdxBaseComponent, never() ).error( nullable( String.class ), any( Throwable.class ) );
  }

  @Test
  public void testDone() throws Exception {
    // no op, calling for code coverage
    mdxBaseComponent.done();
  }

  @Test
  public void testInit() throws Exception {
    assertTrue( mdxBaseComponent.init() );
  }

  @Test
  public void testExecuteAction_queryAction_nullInput() throws Exception {
    mdxBaseComponent.setActionDefinition( queryAction );
    doReturn( conn ).when( mdxBaseComponent ).getDatasourceConnection();
    when( queryAction.getMdxConnection() ).thenReturn( ActionInputConstant.NULL_INPUT );
    assertFalse( mdxBaseComponent.executeAction() );
  }

  @Test
  public void testExecuteAction_queryAction() {
    mdxBaseComponent.setActionDefinition( queryAction );

    when( queryAction.getMdxConnection() ).thenReturn( actionInput );
    when( actionInput.getValue() ).thenReturn( preparedComponent );
    when( preparedComponent.shareConnection() ).thenReturn( conn );
    when( conn.getDatasourceType() ).thenReturn( IPentahoConnection.MDX_DATASOURCE );

    when( queryAction.getQuery() ).thenReturn( queryActionInput );
    when( queryActionInput.getStringValue() ).thenReturn( "select * from table" );
    when( queryAction.getOutputPreparedStatement() ).thenReturn( outputResultSet );

    doReturn( true ).when( mdxBaseComponent ).prepareQuery( nullable( String.class ) );
    doNothing().when( mdxBaseComponent ).setOutputValue( nullable( String.class ), any( MDXBaseComponent.class ) );

    assertTrue( mdxBaseComponent.executeAction() );
    verify( mdxBaseComponent ).prepareQuery( "select * from table" );
    verify( mdxBaseComponent ).setOutputValue( nullable( String.class ), any( MDXBaseComponent.class ) );

  }

  @Test
  public void testExecuteAction_mdxConnectionAction() {
    mdxBaseComponent.setActionDefinition( connAction );
    doReturn( conn ).when( mdxBaseComponent ).getDatasourceConnection();
    doNothing().when( mdxBaseComponent ).setOutputValue( nullable( String.class ), any( MDXBaseComponent.class ) );

    boolean b = mdxBaseComponent.executeAction();
    assertTrue( b );
  }

  @Test
  public void testPrepareQuery() throws Exception {
    mdxBaseComponent.setConnection( conn );
    when( conn.initialized() ).thenReturn( true );
    doReturn( "yes" ).when( mdxBaseComponent ).applyInputsToFormat( nullable( String.class ) );
    assertTrue( mdxBaseComponent.prepareQuery( "select * from table" ) );
  }

  @Test
  public void testPrepareQuery_nullConn() throws Exception {
    mdxBaseComponent.setConnection( null );
    assertFalse( mdxBaseComponent.prepareQuery( "select * from table" ) );
  }

  @Test
  public void testPrepareQuery_notInitialized() throws Exception {
    mdxBaseComponent.setConnection( conn );
    when( conn.initialized() ).thenReturn( false );
    assertFalse( mdxBaseComponent.prepareQuery( "select * from table" ) );
  }

  @Test
  public void testShareConnection() throws Exception {
    mdxBaseComponent.setConnection( conn );
    assertEquals( conn, mdxBaseComponent.shareConnection() );
  }

  @Test
  public void testExecutePrepared_nullConnection() throws Exception {
    Map params = new HashMap();
    assertNull( mdxBaseComponent.executePrepared( params ) );
  }

  @Test
  public void testExecutePrepared_connectionNoInit() throws Exception {
    Map params = new HashMap();
    when( conn.initialized() ).thenReturn( false );
    mdxBaseComponent.setConnection( conn );

    assertNull( mdxBaseComponent.executePrepared( params ) );
  }

  @Test
  public void testExecutePrepared() throws Exception {
    Map params = new HashMap();
    when( conn.initialized() ).thenReturn( true );
    mdxBaseComponent.setConnection( conn );
    mdxBaseComponent.preparedQuery = "select * from table where x = ?";

    when( conn.executeQuery( nullable( String.class ) ) ).thenReturn( resultSet );
    doReturn( runtimeContext ).when( mdxBaseComponent ).getRuntimeContext();
    Set inputs = new HashSet();
    when( runtimeContext.getInputNames() ).thenReturn( inputs );
    when( runtimeContext.getParameterManager() ).thenReturn( paramManager );
    when( paramManager.getCurrentInputNames() ).thenReturn( inputs );

    IPentahoResultSet result = mdxBaseComponent.executePrepared( params );
    assertNotNull( result );
    assertEquals( resultSet, result );
    assertEquals( resultSet, mdxBaseComponent.getResultSet() );
  }

  @Test
  public void testRunQuery_nullConnection() throws Exception {
    assertFalse( mdxBaseComponent.runQuery( null, "select * from table" ) );
  }

  @Test
  public void testRunQuery_connectionNotInit() throws Exception {
    assertFalse( mdxBaseComponent.runQuery( conn, "select * from table" ) );
  }

  @Test
  public void testRunQuery_nullQuery() throws Exception {
    when( conn.initialized() ).thenReturn( true );
    assertFalse( mdxBaseComponent.runQuery( conn, null ) );
  }

  @Test
  public void testRunQuery() throws Exception {
    String query = "select * from table";
    when( conn.executeQuery( query ) ).thenReturn( resultSet );
    when( conn.initialized() ).thenReturn( true );

    mdxBaseComponent.setActionDefinition( queryAction );
    when( queryAction.getOutputResultSet() ).thenReturn( outputResultSet );

    assertTrue( mdxBaseComponent.runQuery( conn, query ) );
    verify( outputResultSet ).setValue( resultSet );
  }

  @Test
  public void testRunQuery_nullResultSet() throws Exception {
    String query = "select * from table";
    when( conn.executeQuery( query ) ).thenReturn( null );
    when( conn.initialized() ).thenReturn( true );

    assertFalse( mdxBaseComponent.runQuery( conn, query ) );
    verify( conn ).close();
  }

  @Test
  public void testRunQuery_mdxResultSet() throws Exception {
    String query = "select * from table";
    when( conn.executeQuery( query ) ).thenReturn( mdxResultSet );
    when( conn.initialized() ).thenReturn( true );

    mdxBaseComponent.setRuntimeContext( runtimeContext );
    HashSet<String> inputs = new HashSet<>();
    inputs.add( MDXBaseComponent.FORMATTED_CELL_VALUES );
    when( runtimeContext.getInputNames() ).thenReturn( inputs );
    mdxBaseComponent.setActionDefinition( queryAction );
    when( queryAction.getOutputResultSet() ).thenReturn( outputResultSet );

    assertTrue( mdxBaseComponent.runQuery( conn, query ) );
    verify( outputResultSet ).setValue( mdxResultSet );
  }

  @Test
  public void testGetDatasourceConnection() throws Exception {
    doReturn( conn ).when( mdxBaseComponent ).getConnection();
    IPentahoConnection datasourceConnection = mdxBaseComponent.getDatasourceConnection();
    assertEquals( conn, datasourceConnection );
    verify( conn ).clearWarnings();
  }

  @Test
  public void testGetDatasourceConnection_waitForTimeout() throws Exception {
    doThrow( new RuntimeException() )      // first try
      .doThrow( new RuntimeException() )   // second try
      .doThrow( new RuntimeException() )   // third try
      .doReturn( conn ).when( mdxBaseComponent ).getConnection();  // last attempt, lets return something
    doNothing().when( mdxBaseComponent ).waitFor( anyInt() );
    IPentahoConnection datasourceConnection = mdxBaseComponent.getDatasourceConnection();
    assertEquals( conn, datasourceConnection );
    verify( conn ).clearWarnings();
    verify( mdxBaseComponent ).waitFor( 200 );
    verify( mdxBaseComponent ).waitFor( 500 );
    verify( mdxBaseComponent ).waitFor( 2000 );
  }

  @Test
  public void testWaitFor() throws Exception {
    // code coverage, it doesn't nothing testable but sleep the thread
    mdxBaseComponent.waitFor( 0 );

    // make sure no exception is thrown for invalid sleep interval
    mdxBaseComponent.waitFor( -1 );
  }

  @Test
  public void testGetConnection() throws Exception {
    doReturn( connAction ).when( mdxBaseComponent ).getActionDefinition();
    when( connAction.getCatalog() ).thenReturn( catalog );
    when( catalog.getStringValue() ).thenReturn( "my catalog" );

    PentahoSystem.registerObject( mondrianCatalogService );
    PentahoSystem.registerObject( mdxConnection );
    PentahoSystem.registerPrimaryObjectFactory( objFactory );
    PentahoSessionHolder.setSession( session );

    when( mondrianCatalogService.getCatalog( nullable( String.class ), any( IPentahoSession.class ) ) ).thenReturn( mondrianCatalog );

    when( mondrianCatalog.getDataSourceInfo() ).thenReturn( "connection info" );
    when( mondrianCatalog.getDefinition() ).thenReturn( "<catalog></catalog>" );
    when( connAction.getExtendedColumnNames() ).thenReturn( ActionInputConstant.NULL_INPUT );

    IPentahoConnection connection = mdxBaseComponent.getConnection();
    assertNotNull( connection );
    assertEquals( mdxConnection, connection );
  }

  @Test
  public void testGetConnectionOrig_null() throws Exception {
    IPentahoConnection connectionOrig = mdxBaseComponent.getConnectionOrig();
    assertNull( connectionOrig );
  }

  @Test
  public void testGetConnectionOrig() throws Exception {
    doReturn( connAction ).when( mdxBaseComponent ).getActionDefinition();
    when( connAction.getMdxConnectionString() ).thenReturn( connectionStringAction );
    when( connectionStringAction.getStringValue() ).thenReturn( "mdx:localhost:8080" );
    when( connAction.getConnectionProps() ).thenReturn( actionInput );
    when( actionInput.getValue() ).thenReturn( new Properties() );

    when( connAction.getConnection() ).thenReturn( jdbcAction );
    when( jdbcAction.getStringValue() ).thenReturn( "psql://localhost:5432" );

    when( connAction.getJndi() ).thenReturn( jndiAction );
    when( jndiAction.getStringValue() ).thenReturn( "jndi string" );

    when( connAction.getLocation() ).thenReturn( locationAction );
    when( locationAction.getStringValue() ).thenReturn( "location string" );

    when( connAction.getRole() ).thenReturn( roleAction );
    when( roleAction.getStringValue() ).thenReturn( "role string" );

    when( connAction.getCatalog() ).thenReturn( catalog );
//    when( catalog.getStringValue() ).thenReturn( "<catalog></catalog>" );
    when( catalog.getStringValue() ).thenReturn( null );

    when( connAction.getCatalogResource() ).thenReturn( catalogResource );
    when( catalogResource.getName() ).thenReturn( "catalog name" );

    when( connAction.getUserId() ).thenReturn( userAction );
    when( connAction.getPassword() ).thenReturn( userAction );
    when( userAction.getStringValue() ).thenReturn( "user/pass" );

    PentahoSystem.registerObject( mdxConnection );
    PentahoSystem.registerPrimaryObjectFactory( objFactory );
    PentahoSessionHolder.setSession( session );

    mdxBaseComponent.setRuntimeContext( runtimeContext );
    when( runtimeContext.getResourceDefintion( "catalog name" ) ).thenReturn( catalogActionSeqRes );

    when( catalogActionSeqRes.getSourceType() ).thenReturn( IActionSequenceResource.URL_RESOURCE );
    when( catalogActionSeqRes.getAddress() ).thenReturn( "sampledata" );

    when( connAction.getExtendedColumnNames() ).thenReturn( ActionInputConstant.NULL_INPUT );

    IPentahoConnection connectionOrig = mdxBaseComponent.getConnectionOrig();
    assertNotNull( connectionOrig );
    assertEquals( mdxConnection, connectionOrig );
  }

  @Test
  public void testGetConnectionOrig_nullConnectionProps() throws Exception {
    doReturn( connAction ).when( mdxBaseComponent ).getActionDefinition();
    when( connAction.getMdxConnectionString() ).thenReturn( connectionStringAction );
    when( connectionStringAction.getStringValue() ).thenReturn( "mdx:localhost:8080" );
    when( connAction.getConnectionProps() ).thenReturn( actionInput );
    when( actionInput.getValue() ).thenReturn( null );

    when( connAction.getConnection() ).thenReturn( jdbcAction );
    when( jdbcAction.getStringValue() ).thenReturn( "psql://localhost:5432" );

    when( connAction.getJndi() ).thenReturn( jndiAction );
    when( jndiAction.getStringValue() ).thenReturn( "jndi string" );

    when( connAction.getLocation() ).thenReturn( locationAction );
    when( locationAction.getStringValue() ).thenReturn( "location string" );

    when( connAction.getRole() ).thenReturn( roleAction );
    when( roleAction.getStringValue() ).thenReturn( "role string" );

    when( connAction.getCatalog() ).thenReturn( catalog );
    //    when( catalog.getStringValue() ).thenReturn( "<catalog></catalog>" );
    when( catalog.getStringValue() ).thenReturn( null );

    when( connAction.getCatalogResource() ).thenReturn( catalogResource );
    when( catalogResource.getName() ).thenReturn( "catalog name" );

    when( connAction.getUserId() ).thenReturn( userAction );
    when( connAction.getPassword() ).thenReturn( userAction );
    when( userAction.getStringValue() ).thenReturn( "user/pass" );

    PentahoSystem.registerObject( mdxConnection );
    PentahoSystem.registerPrimaryObjectFactory( objFactory );
    PentahoSessionHolder.setSession( session );

    mdxBaseComponent.setRuntimeContext( runtimeContext );
    when( runtimeContext.getResourceDefintion( "catalog name" ) ).thenReturn( catalogActionSeqRes );

    when( catalogActionSeqRes.getSourceType() ).thenReturn( IActionSequenceResource.URL_RESOURCE );
    when( catalogActionSeqRes.getAddress() ).thenReturn( "sampledata" );

    when( connAction.getExtendedColumnNames() ).thenReturn( ActionInputConstant.NULL_INPUT );

    IPentahoConnection connectionOrig = mdxBaseComponent.getConnectionOrig();
    assertNotNull( connectionOrig );
    assertEquals( mdxConnection, connectionOrig );
  }

  @Test
  public void testGetConnectionOrig_nullConnectionProps_noConnString() throws Exception {
    doReturn( connAction ).when( mdxBaseComponent ).getActionDefinition();
    when( connAction.getMdxConnectionString() ).thenReturn( connectionStringAction );
    when( connectionStringAction.getStringValue() ).thenReturn( null );
    when( connAction.getConnectionProps() ).thenReturn( actionInput );
    when( actionInput.getValue() ).thenReturn( null );

    when( connAction.getConnection() ).thenReturn( jdbcAction );
    when( jdbcAction.getStringValue() ).thenReturn( "psql://localhost:5432" );

    when( connAction.getJndi() ).thenReturn( jndiAction );
    when( jndiAction.getStringValue() ).thenReturn( "jndi string" );

    when( connAction.getLocation() ).thenReturn( locationAction );
    when( locationAction.getStringValue() ).thenReturn( "location string" );

    when( connAction.getRole() ).thenReturn( roleAction );
    when( roleAction.getStringValue() ).thenReturn( "role string" );

    when( connAction.getCatalog() ).thenReturn( catalog );
    //    when( catalog.getStringValue() ).thenReturn( "<catalog></catalog>" );
    when( catalog.getStringValue() ).thenReturn( null );

    when( connAction.getCatalogResource() ).thenReturn( catalogResource );
    when( catalogResource.getName() ).thenReturn( "catalog name" );

    when( connAction.getUserId() ).thenReturn( userAction );
    when( connAction.getPassword() ).thenReturn( userAction );
    when( userAction.getStringValue() ).thenReturn( "user/pass" );

    PentahoSystem.registerObject( mdxConnection );
    PentahoSystem.registerPrimaryObjectFactory( objFactory );
    PentahoSessionHolder.setSession( session );

    mdxBaseComponent.setRuntimeContext( runtimeContext );
    when( runtimeContext.getResourceDefintion( "catalog name" ) ).thenReturn( catalogActionSeqRes );

    when( catalogActionSeqRes.getSourceType() ).thenReturn( IActionSequenceResource.URL_RESOURCE );
    when( catalogActionSeqRes.getAddress() ).thenReturn( "sampledata" );

    when( connAction.getExtendedColumnNames() ).thenReturn( ActionInputConstant.NULL_INPUT );

    IPentahoConnection connectionOrig = mdxBaseComponent.getConnectionOrig();
    assertNotNull( connectionOrig );
    assertEquals( mdxConnection, connectionOrig );
  }

  @Test
  public void testGetConnectionOrig_nullConnectionProps_noConnString2() throws Exception {
    doReturn( connAction ).when( mdxBaseComponent ).getActionDefinition();
    when( connAction.getMdxConnectionString() ).thenReturn( connectionStringAction );
    when( connectionStringAction.getStringValue() ).thenReturn( null );
    when( connAction.getConnectionProps() ).thenReturn( actionInput );
    when( actionInput.getValue() ).thenReturn( null );

    when( connAction.getConnection() ).thenReturn( jdbcAction );
    when( jdbcAction.getStringValue() ).thenReturn( "psql://localhost:5432" );

    when( connAction.getJndi() ).thenReturn( jndiAction );
    when( jndiAction.getStringValue() ).thenReturn( "jndi string" );

    when( connAction.getLocation() ).thenReturn( locationAction );
    when( locationAction.getStringValue() ).thenReturn( "location string" );

    when( connAction.getRole() ).thenReturn( roleAction );
    when( roleAction.getStringValue() ).thenReturn( "role string" );

    when( connAction.getCatalog() ).thenReturn( catalog );
    //    when( catalog.getStringValue() ).thenReturn( "<catalog></catalog>" );
    when( catalog.getStringValue() ).thenReturn( null );

    when( connAction.getCatalogResource() ).thenReturn( catalogResource );
    when( catalogResource.getName() ).thenReturn( "catalog name" );

    when( connAction.getUserId() ).thenReturn( userAction );
    when( connAction.getPassword() ).thenReturn( userAction );
    when( userAction.getStringValue() ).thenReturn( "user/pass" );

    PentahoSystem.registerObject( mdxConnection );
    PentahoSystem.registerPrimaryObjectFactory( objFactory );
    PentahoSessionHolder.setSession( session );

    mdxBaseComponent.setRuntimeContext( runtimeContext );
    when( runtimeContext.getResourceDefintion( "catalog name" ) ).thenReturn( catalogActionSeqRes );

    when( catalogActionSeqRes.getSourceType() ).thenReturn( IActionSequenceResource.URL_RESOURCE );

    final String urlFileExists = ( new File( TestResourceLocation.TEST_RESOURCES + "/MDXBaseComponentTest/SampleData.mondrian.xml" ) ).toURL().toString();
    when( catalogActionSeqRes.getAddress() ).thenReturn( urlFileExists );

    when( connAction.getExtendedColumnNames() ).thenReturn( ActionInputConstant.NULL_INPUT );

    IPentahoConnection connectionOrig = mdxBaseComponent.getConnectionOrig();
    assertNotNull( connectionOrig );
    assertEquals( mdxConnection, connectionOrig );
  }

  @Test
  @SuppressWarnings( "unchecked" )
  public void shouldAddSolutionPrefixIfFileExistsInRepository()
    throws ObjectFactoryException, DBDatasourceServiceException {
    mdxBaseComponent.setActionDefinition( connAction );
    when( connAction.getMdxConnectionString() ).thenReturn( IActionInput.NULL_INPUT );
    when( connAction.getConnectionProps() ).thenReturn( IActionInput.NULL_INPUT );
    when( connAction.getConnection() ).thenReturn( IActionInput.NULL_INPUT );
    when( connAction.getJndi() ).thenReturn( new ActionInputConstant( "", null ) );
    when( connAction.getLocation() ).thenReturn( new ActionInputConstant( "", null ) );
    when( connAction.getRole() ).thenReturn( IActionInput.NULL_INPUT );
    when( connAction.getCatalog() ).thenReturn( IActionInput.NULL_INPUT );
    when( connAction.getUserId() ).thenReturn( IActionInput.NULL_INPUT );
    when( connAction.getPassword() ).thenReturn( IActionInput.NULL_INPUT );
    when( connAction.getCatalogResource() ).thenReturn( catalogResource );
    when( catalogResource.getName() ).thenReturn( "catalog name" );

    PentahoSystem.registerObject( mdxConnection );
    PentahoSystem.registerPrimaryObjectFactory( objFactory );
    PentahoSessionHolder.setSession( session );

    IDBDatasourceService datasourceService = mock( IDBDatasourceService.class );
    when( objFactory.objectDefined( "IDBDatasourceService" ) ).thenReturn( true );
    when( objFactory.get( any( Class.class ), eq( "IDBDatasourceService" ), nullable( IPentahoSession.class ) ) )
      .thenReturn( datasourceService );
    DataSource dataSource = mock( DataSource.class );
    when( datasourceService.getDataSource( nullable( String.class ) ) ).thenReturn( dataSource );

    mdxBaseComponent.setRuntimeContext( runtimeContext );
    when( runtimeContext.getResourceDefintion( "catalog name" ) ).thenReturn( catalogActionSeqRes );

    when( catalogActionSeqRes.getSourceType() ).thenReturn( IActionSequenceResource.URL_RESOURCE );

    MondrianCatalog mc = mock( MondrianCatalog.class );
    doReturn( mc ).when( mdxBaseComponent ).getMondrianCatalog( nullable( String.class ) );

    // using the same prepared environment for several checks...

    // if file exists in repository, then the "solution:" prefix should be added
    when( catalogActionSeqRes.getAddress() ).thenReturn( "fileName" );
    doReturn( true ).when( mdxBaseComponent ).fileExistsInRepository( "fileName" );
    mdxBaseComponent.getConnectionOrig();
    verify( mdxBaseComponent ).getMondrianCatalog( "solution:fileName" );
    verify( mdxBaseComponent, times( 1 ) ).fileExistsInRepository( nullable( String.class ) );

    // if file exists in repository, then the "solution:" prefix should NOT be added,
    // and the file therefore should be read from file system further
    when( catalogActionSeqRes.getAddress() ).thenReturn( "fileName" );
    doReturn( false ).when( mdxBaseComponent ).fileExistsInRepository( "fileName" );
    mdxBaseComponent.getConnectionOrig();
    verify( mdxBaseComponent ).getMondrianCatalog( "fileName" );
    verify( mdxBaseComponent, times( 2 ) ).fileExistsInRepository( nullable( String.class ) );

    // if filename already starts from "solution:" prefix,
    // then no need to check file existence in repository
    when( catalogActionSeqRes.getAddress() ).thenReturn( "solution:fileName" );
    mdxBaseComponent.getConnectionOrig();
    verify( mdxBaseComponent, times( 2 ) ).fileExistsInRepository( nullable( String.class ) );

    // same here: if the resource is http link, then no need to check it in repository
    when( catalogActionSeqRes.getAddress() ).thenReturn( "http:fileName" );
    mdxBaseComponent.getConnectionOrig();
    verify( mdxBaseComponent, times( 2 ) ).fileExistsInRepository( nullable( String.class ) );
  }

  @Test
  @SuppressWarnings( "unchecked" )
  public void shouldUseUnifiedRepositoryWhenCheckingFileExistence() throws ObjectFactoryException {
    PentahoSystem.registerPrimaryObjectFactory( objFactory );
    PentahoSessionHolder.setSession( session );

    IUnifiedRepository repository = mock( IUnifiedRepository.class );
    when( objFactory.objectDefined( "IUnifiedRepository" ) ).thenReturn( true );
    when( objFactory.get( any( Class.class ), eq( "IUnifiedRepository" ), any( IPentahoSession.class ) ) )
      .thenReturn( repository );
    doCallRealMethod().when( mdxBaseComponent ).fileExistsInRepository( nullable( String.class ) );
    RepositoryFile file = mock( RepositoryFile.class );
    doReturn( file ).when( repository ).getFile( "fileNameExisting" );
    doReturn( null ).when( repository ).getFile( "fileNameNonExisting" );
    // checking bad file name for pentaho repository, e.g. windows os file names
    doThrow( new UnifiedRepositoryException() ).when( repository ).getFile( "fileNameBad" );

    boolean fileExists = mdxBaseComponent.fileExistsInRepository( "fileNameExisting" );
    assertTrue( fileExists );

    boolean fileDoesNotExist = mdxBaseComponent.fileExistsInRepository( "fileNameNonExisting" );
    assertFalse( fileDoesNotExist );

    boolean fileBad = mdxBaseComponent.fileExistsInRepository( "fileNameBad" );
    assertFalse( fileBad );
  }

  @After
  public void tearDown() throws Exception {
    PentahoSystem.clearObjectFactory();
  }
}
