package org.pentaho.platform.repository.webservices;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.pentaho.platform.api.repository2.unified.RepositoryFilePermission.READ;
import static org.pentaho.platform.api.repository2.unified.RepositoryFilePermission.READ_ACL;
import static org.pentaho.platform.api.repository2.unified.RepositoryFilePermission.WRITE;
import static org.pentaho.platform.api.repository2.unified.RepositoryFilePermission.WRITE_ACL;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;
import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseType;
import org.pentaho.database.service.DatabaseDialectService;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.repository.JcrBackedDatasourceMgmtService;
import org.pentaho.test.platform.repository2.unified.MockUnifiedRepository;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;

public class DefaultDatasourceMgmtWebServiceTest  extends TestCase {
  
  private static final String EXP_DBMETA_NAME = "haha";

  private static final String EXP_DBMETA_HOSTNAME = "acme";

  private static final String EXP_DBMETA_PORT = "10521";
  
  private static final String EXP_UPDATED_DBMETA_NAME = "hahaUpdated";

  private static final String EXP_UPDATED_DBMETA_HOSTNAME = "acmeUpdated";
  
  private static final String EXP_UPDATED_DBMETA_PORT = "10522";
  
  public static final String EXP_LOGIN = "joe";
  
  private static final String FOLDER_PDI = "pdi";
 
  private static final String FOLDER_DATABASES = "databases"; //$NON-NLS-1$
  private IDatasourceMgmtService datasourceMgmtService;
  private IDatasourceMgmtWebService datasourceMgmtWebService;
  private DatabaseConnectionAdapter dbConnectionAdapter;
  public void setUp() throws Exception {
    IUnifiedRepository repository = new MockUnifiedRepository(new MockUnifiedRepository.SpringSecurityCurrentUserProvider());
    datasourceMgmtService = new JcrBackedDatasourceMgmtService(repository, new DatabaseDialectService());
    datasourceMgmtWebService = new DefaultDatasourceMgmtWebService(datasourceMgmtService);
    dbConnectionAdapter = new DatabaseConnectionAdapter();
    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(MockUnifiedRepository.root().getName(), null, new GrantedAuthority[0]));
    repository.createFolder(repository.getFile("/etc").getId(), new RepositoryFile.Builder(FOLDER_PDI).folder(true).build(), new RepositoryFileAcl.Builder(MockUnifiedRepository.root()).ace(MockUnifiedRepository.everyone(), READ, READ_ACL, WRITE, WRITE_ACL).build(), null);
    repository.createFolder(repository.getFile("/etc/pdi").getId(), new RepositoryFile.Builder(FOLDER_DATABASES).folder(true).build(), null);
    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(EXP_LOGIN, null, new GrantedAuthority[0]));
  }
  
  @Test
  public void testEverything() throws Exception {
    DatabaseConnection databaseConnection = createDatabaseConnection("testDatabase");
    datasourceMgmtWebService.createDatasource(dbConnectionAdapter.marshal(databaseConnection));
    DatabaseConnectionDto databaseConnectionDto = datasourceMgmtWebService.getDatasourceByName("testDatabase");
    assertNotNull(databaseConnectionDto);
    assertEquals(EXP_DBMETA_HOSTNAME, databaseConnectionDto.getHostname());
    DatabaseConnection databaseConnection1 = createDatabaseConnection("testDatabase1");
    datasourceMgmtWebService.createDatasource(dbConnectionAdapter.marshal(databaseConnection1));
    DatabaseConnectionDto databaseConnectionDto1 = datasourceMgmtWebService.getDatasourceByName("testDatabase1");
    assertNotNull(databaseConnectionDto1);
    assertEquals(EXP_DBMETA_HOSTNAME, databaseConnectionDto1.getHostname());
    List<DatabaseConnectionDto> databaseConnectionDtos = datasourceMgmtWebService.getDatasources();
    assertEquals(2, databaseConnectionDtos.size());
    updateDatabaseConnection(databaseConnection);
    datasourceMgmtWebService.updateDatasourceByName("testDatabase", dbConnectionAdapter.marshal(databaseConnection));
    DatabaseConnectionDto updatedDatabaseConnectionDto = datasourceMgmtWebService.getDatasourceByName("testDatabase");
    assertNotNull(updatedDatabaseConnectionDto);
    assertEquals(EXP_UPDATED_DBMETA_HOSTNAME, databaseConnection.getHostname());
    datasourceMgmtWebService.deleteDatasourceByName("testDatabase");
    DatabaseConnectionDto deletedDatabaseConnectionDto = datasourceMgmtWebService.getDatasourceByName("testDatabase");
    assertNull(deletedDatabaseConnectionDto);
  }
  
  private DatabaseConnection createDatabaseConnection(final String dbName) throws Exception {
    DatabaseConnection dbConnection = new DatabaseConnection();
    dbConnection.setName(dbName);
    dbConnection.setHostname(EXP_DBMETA_HOSTNAME);
    dbConnection.setDatabaseType(mockDatabaseType("Hypersonic"));
    dbConnection.setAccessType(DatabaseAccessType.NATIVE);
    dbConnection.setDatabasePort(EXP_DBMETA_PORT);
    return dbConnection;
  }

  private void updateDatabaseConnection(DatabaseConnection dbConnection) throws Exception {
    dbConnection.setHostname(EXP_UPDATED_DBMETA_HOSTNAME);
    dbConnection.setDatabaseType(mockDatabaseType("Generic database"));
    dbConnection.setAccessType(DatabaseAccessType.JNDI);
    dbConnection.setDatabasePort(EXP_UPDATED_DBMETA_PORT);
  }

  private IDatabaseType mockDatabaseType(final String shortName) {
    IDatabaseType dbType = mock(IDatabaseType.class);
    doReturn(shortName).when(dbType).getShortName();
    return dbType;
  }
}
