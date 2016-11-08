package org.pentaho.platform.engine.services.connection.datasource.dbcp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;
import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.api.data.DBDatasourceServiceException;
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;

import static org.mockito.Mockito.*;


/**
 * Created by gmoran on 5/2/14.
 */
@RunWith(Parameterized.class)
public class PentahoConnectionDatasourceServiceTest {

  private BaseDatasourceService service;
  private BaseDatasourceService spyService;
  private IDatabaseConnection mockConnection;
  private String dsName;

  @Parameterized.Parameters
  public static Collection services() {
    return Arrays.asList( new Object[][] {
        { new NonPooledDatasourceService(), "test1" },
        { new PooledDatasourceService(), "test2" },
        { new PooledOrJndiDatasourceService(), "test3" },
        { new NonPooledOrJndiDatasourceService(), "test4" }
    } );
  }

  @Before
  public void setUp(){

    mockConnection = mock(IDatabaseConnection.class);

    // Set it up - this is a NATIVE connection
    when( mockConnection.getAccessType()).thenReturn( DatabaseAccessType.NATIVE );
    when( mockConnection.getDatabaseName()).thenReturn( dsName );

    DataSource mockDs = mock(DataSource.class);
    IDatasourceMgmtService mockMgmtService = mock(IDatasourceMgmtService.class);

    spyService = spy(service );

    try {
      when( mockMgmtService.getDatasourceByName( dsName ) ).thenReturn( mockConnection );
    } catch ( DatasourceMgmtServiceException e ) {
      e.printStackTrace();
    }

    try {
      doReturn(mockDs).when( spyService ).resolveDatabaseConnection( mockConnection );
    } catch ( DBDatasourceServiceException e ) {
      e.printStackTrace();
    }

    doReturn( mockMgmtService ).when( spyService ).getDatasourceMgmtService();

    service.clearCache();

  }

  public PentahoConnectionDatasourceServiceTest( BaseDatasourceService service, String name ){
    this.service = service;
    this.dsName = name;
  }

  @Test
  public void testPentahoConnectionServices( ){


    try {

      // Now make sure that the resolve.. service method gets called; if not,
      // fail the test
      spyService.getDataSource( dsName );
      verify(spyService).resolveDatabaseConnection( mockConnection );
      verify(spyService, Mockito.never()).getJndiDataSource( dsName );

    } catch ( DBDatasourceServiceException e ) {
      e.printStackTrace();
    }

  }

  @Test
  public void testCachedPentahoConnectionServices( ){

    try {

      // Now make sure that the JNDI service method gets called the first time,
      // but the second time it should retrieve from cache.
      spyService.getDataSource( dsName );
      spyService.getDataSource( dsName );
      verify(spyService, Mockito.times( 1 )).resolveDatabaseConnection( mockConnection );

    } catch ( DBDatasourceServiceException e ) {
      e.printStackTrace();
    }

  }

}
