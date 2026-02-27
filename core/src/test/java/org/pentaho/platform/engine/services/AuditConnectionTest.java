/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.engine.services;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.objfac.StandaloneSpringPentahoObjectFactory;
import org.pentaho.platform.engine.services.audit.AuditConnection;
import org.pentaho.test.platform.engine.core.BaseTest;

@SuppressWarnings( "nls" )
public class AuditConnectionTest extends BaseTest {
  private static String SOLUTION_PATH = "src/test/resources/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public void testAuditConnection() {
    startTest();
    AuditConnection auditConnection = new AuditConnection();
    auditConnection.setUseNewDatasourceService( true ); // make sure we get a datasource from the object factory
    auditConnection.initialize();
    MockDataSourceService.setThrowExceptionOnGetConnection( false );
    try {
      Connection connection = auditConnection.getAuditConnection();
      System.out.println( "Audit Connection Is  " + connection ); //$NON-NLS-1$

      DataSource datasource = auditConnection.getAuditDatasource();
      System.out.println( "Datasource Is  " + datasource ); //$NON-NLS-1$
    } catch ( Exception e ) {
      e.printStackTrace();
      fail( e.getMessage() );
    } finally {
      finishTest();
    }
  }

  public void testAuditConnectionNoConfigFile() {
    startTest();
    SOLUTION_PATH = ""; // We want to test what happens when the config file can't be found
    AuditConnection auditConnection = new AuditConnection();
    auditConnection.setUseNewDatasourceService( true ); // make sure we get a datasource from the object factory
    auditConnection.initialize();
    MockDataSourceService.setThrowExceptionOnGetConnection( false );
    try {
      Connection connection = auditConnection.getAuditConnection();
      System.out.println( "Audit Connection Is  " + connection ); //$NON-NLS-1$

      DataSource datasource = auditConnection.getAuditDatasource();
      System.out.println( "Datasource Is  " + datasource ); //$NON-NLS-1$
    } catch ( Exception e ) {
      e.printStackTrace();
      fail( e.getMessage() );
    } finally {
      finishTest();
    }
  }

  public void testAuditConnection_cannot_establish_connection() {
    startTest();

    try {
      // Load mock object factory with mock datasource service that produces null datasources
      StandaloneSpringPentahoObjectFactory factory = new StandaloneSpringPentahoObjectFactory();
      factory.init( "src/test/resources/solution/system/pentahoObjects.datasourceservice.null.spring.xml", null );
      PentahoSystem.registerObjectFactory( factory );

      AuditConnection auditConnection = new AuditConnection();
      auditConnection.setUseNewDatasourceService( true ); // make sure we get a datasource from the object factory
      auditConnection.initialize();
      MockDataSourceService.setThrowExceptionOnGetConnection( true );
      auditConnection.getAuditConnection();
      fail( "Expected exception when no audit connection could be established" );
    } catch ( SQLException ex ) {
      ex.printStackTrace();
      assertTrue( "Expected AUDSQLENT.ERROR_0001", ex.getMessage().contains( "AUDSQLENT.ERROR_0001" ) );
    } finally {
      finishTest();
    }
  }
}
