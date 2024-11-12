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


package org.pentaho.platform.engine.services.connection.datasource.dbcp;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.api.data.DBDatasourceServiceException;
import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.engine.services.MockDataSourceService;

@RunWith( MockitoJUnitRunner.class )
public class DynamicallyPooledDatasourceSystemListenerTest {

  private static final String CONNECTION_NAME = "TEST CONNECTION";

  @Mock
  IDBDatasourceService datasourceService;
  @Mock
  IDatabaseConnection connection;
  @Spy
  DynamicallyPooledDatasourceSystemListener listener;


  @Before
  public void before() {
    when( connection.getName() ).thenReturn( CONNECTION_NAME );
  }

  @Test
  public void testGetDataSource() {
    when( listener.getDatasourceService() ).thenReturn( new MockDataSourceService( false ) );
    DataSource ds = listener.getDataSource( connection );
    assertNotNull( ds );
  }

  @Test
  public void testGetDataSourceHandleDBDatasourceServiceException() throws Exception {

    when( listener.getDatasourceService() ).thenReturn( datasourceService );
    when( datasourceService.getDataSource( CONNECTION_NAME ) ).thenThrow( new DBDatasourceServiceException() );

    DataSource ds = listener.getDataSource( connection );
    assertNull( ds );
  }

  @Test
  public void testGetDataSourceHanldeDriverNotInitializedException() throws Exception {

    when( listener.getDatasourceService() ).thenReturn( datasourceService );
    when( datasourceService.getDataSource( CONNECTION_NAME ) ).thenThrow( new DriverNotInitializedException() );

    DataSource ds = listener.getDataSource( connection );
    assertNull( ds );
  }
}
