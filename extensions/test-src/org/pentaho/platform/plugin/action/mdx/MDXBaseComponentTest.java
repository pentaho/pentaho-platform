/*
 * ******************************************************************************
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.pentaho.platform.plugin.action.mdx;

import org.apache.commons.logging.Log;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.actionsequence.dom.ActionInputConstant;
import org.pentaho.actionsequence.dom.IActionInput;
import org.pentaho.actionsequence.dom.IActionOutput;
import org.pentaho.actionsequence.dom.IActionResource;
import org.pentaho.actionsequence.dom.actions.MdxConnectionAction;
import org.pentaho.actionsequence.dom.actions.MdxQueryAction;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.data.IPreparedComponent;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.engine.IParameterManager;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.services.connections.mondrian.MDXConnection;
import org.pentaho.platform.plugin.services.connections.mondrian.MDXResultSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

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
    doNothing().when( mdxBaseComponent ).error( anyString(), any( Throwable.class ) );
    doNothing().when( mdxBaseComponent ).error( anyString() );
  }

  @Test
  public void testValidateAction_noActionDefinition() throws Exception {
    boolean action = mdxBaseComponent.validateAction();
    assertTrue( action );
  }

  @Test
  public void testValidateAction_queryAction_nullInput() throws Exception {
    mdxBaseComponent.setActionDefinition( queryAction );

    when( queryAction.getQuery() ).thenReturn( ActionInputConstant.NULL_INPUT );

    boolean action = mdxBaseComponent.validateAction();
    assertFalse( action );
    verify( mdxBaseComponent ).error( anyString() );
  }

  @Test
  public void testValidateAction_queryAction_nullResult() throws Exception {
    mdxBaseComponent.setActionDefinition( queryAction );

    when( queryAction.getOutputResultSet() ).thenReturn( null );
    when( queryAction.getOutputPreparedStatement() ).thenReturn( null );

    boolean action = mdxBaseComponent.validateAction();
    assertFalse( action );
    verify( mdxBaseComponent ).error( anyString() );
  }

  @Test
  public void testValidateAction_queryAction() throws Exception {
    mdxBaseComponent.setActionDefinition( queryAction );

    when( queryAction.getOutputResultSet() ).thenReturn( outputResultSet );
    when( queryAction.getOutputPreparedStatement() ).thenReturn( null );

    boolean action = mdxBaseComponent.validateAction();
    assertTrue( action );
    verify( mdxBaseComponent, never() ).error( anyString(), any( Throwable.class ) );
  }

  @Test
  public void testValidateAction_connectionAction_noConnection() throws Exception {
    mdxBaseComponent.setActionDefinition( connAction );

    boolean action = mdxBaseComponent.validateAction();
    assertFalse( action );
    verify( mdxBaseComponent ).error( anyString() );
  }

  @Test
  public void testValidateAction_connectionAction() throws Exception {
    mdxBaseComponent.setActionDefinition( connAction );
    when( connAction.getOutputConnection() ).thenReturn( outputResultSet );

    boolean action = mdxBaseComponent.validateAction();
    assertTrue( action );
    verify( mdxBaseComponent, never() ).error( anyString(), any( Throwable.class ) );
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
  public void testExecuteAction_queryAction() throws Exception {
    mdxBaseComponent.setActionDefinition( queryAction );

    when( queryAction.getMdxConnection() ).thenReturn( actionInput );
    when( actionInput.getValue() ).thenReturn( preparedComponent );
    when( preparedComponent.shareConnection() ).thenReturn( conn );
    when( conn.getDatasourceType() ).thenReturn( IPentahoConnection.MDX_DATASOURCE );

    when( queryAction.getQuery() ).thenReturn( queryActionInput );
    when( queryActionInput.getStringValue() ).thenReturn( "select * from table" );
    when( queryAction.getOutputPreparedStatement() ).thenReturn( outputResultSet );

    doReturn( true ).when( mdxBaseComponent ).prepareQuery( anyString() );
    doNothing().when( mdxBaseComponent ).setOutputValue( anyString(), anyString() );

    assertTrue( mdxBaseComponent.executeAction() );
    verify( mdxBaseComponent ).prepareQuery( "select * from table" );
    verify( mdxBaseComponent ).setOutputValue( anyString(), anyString() );

  }

  @Test
  public void testExecuteAction_mdxConnectionAction() throws Exception {
    mdxBaseComponent.setActionDefinition( connAction );
    doReturn( conn ).when( mdxBaseComponent ).getDatasourceConnection();
    doNothing().when( mdxBaseComponent ).setOutputValue( anyString(), anyString() );

    assertTrue( mdxBaseComponent.executeAction() );
  }

  @Test
  public void testPrepareQuery() throws Exception {
    mdxBaseComponent.setConnection( conn );
    when( conn.initialized() ).thenReturn( true );
    doReturn( "yes" ).when( mdxBaseComponent ).applyInputsToFormat( anyString() );
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

    when( conn.executeQuery( anyString() ) ).thenReturn( resultSet );
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

    when( objFactory.get( any( Class.class ), eq( "connection-MDX" ), any( IPentahoSession.class ) ) ).thenReturn( mdxConnection );

    when( mondrianCatalogService.getCatalog( anyString(), any( IPentahoSession.class ) ) ).thenReturn( mondrianCatalog );

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

    when( objFactory.get( any( Class.class ), anyString(), any( IPentahoSession.class ) ) ).thenReturn( mdxConnection );

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

    when( objFactory.get( any( Class.class ), anyString(), any( IPentahoSession.class ) ) ).thenReturn( mdxConnection );

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

    when( objFactory.get( any( Class.class ), anyString(), any( IPentahoSession.class ) ) ).thenReturn( mdxConnection );

    mdxBaseComponent.setRuntimeContext( runtimeContext );
    when( runtimeContext.getResourceDefintion( "catalog name" ) ).thenReturn( catalogActionSeqRes );

    when( catalogActionSeqRes.getSourceType() ).thenReturn( IActionSequenceResource.URL_RESOURCE );
    when( catalogActionSeqRes.getAddress() ).thenReturn( "sampledata" );

    when( connAction.getExtendedColumnNames() ).thenReturn( ActionInputConstant.NULL_INPUT );

    IPentahoConnection connectionOrig = mdxBaseComponent.getConnectionOrig();
    assertNotNull( connectionOrig );
    assertEquals( mdxConnection, connectionOrig );
  }

  @After
  public void tearDown() throws Exception {
    PentahoSystem.clearObjectFactory();
  }
}
