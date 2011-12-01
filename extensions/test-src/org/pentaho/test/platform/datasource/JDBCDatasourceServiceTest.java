package org.pentaho.test.platform.datasource;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.service.DatabaseDialectService;
import org.pentaho.database.service.IDatabaseDialectService;
import org.pentaho.database.util.DatabaseTypeHelper;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory.Scope;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.util.IPasswordService;
import org.pentaho.platform.datasource.DatasourceInfo;
import org.pentaho.platform.datasource.JDBCDatasource;
import org.pentaho.platform.datasource.JDBCDatasourceService;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository.JcrBackedDatasourceMgmtService;
import org.pentaho.platform.repository2.unified.fs.FileSystemBackedUnifiedRepository;
import org.pentaho.platform.util.Base64PasswordService;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.pentaho.test.platform.plugin.services.metadata.MockSessionAwareMetadataDomainRepository;

public class JDBCDatasourceServiceTest extends TestCase {

  private MicroPlatform microPlatform;
  
  private IUnifiedRepository repo;
  
  private boolean startupCalled;
  
  private IDatasourceMgmtService datasourceMgmtService;
  
  private JDBCDatasourceService jdbcDatasourceService;
  
  // ~ Static fields/initializers ======================================================================================

  protected static final String DIR_CONNECTIONS = "connections";

  protected static final String EXP_DBMETA_NAME = "haha";
  
  protected static final String EXP_DBMETA_NAME_1 = "haha1";
  
  protected static final String EXP_DBMETA_HOSTNAME = "acme";

  protected static final String EXP_DBMETA_TYPE = "ORACLE";

  protected static final int EXP_DBMETA_ACCESS = DatabaseMeta.TYPE_ACCESS_NATIVE;

  protected static final String EXP_DBMETA_DBNAME = "lksjdf";

  protected static final String EXP_DBMETA_PORT = "10521";

  protected static final String EXP_DBMETA_USERNAME = "elaine";

  protected static final String EXP_DBMETA_PASSWORD = "password";

  protected static final String EXP_DBMETA_SERVERNAME = "serverName";

  protected static final String EXP_UPDATED_DBMETA_NAME = "hahaUpdated";
  
  protected static final String EXP_UPDATED_DBMETA_NAME_1 = "haha1Updated";
  
  protected static final String EXP_UPDATED_DBMETA_HOSTNAME = "acmeUpdated";

  protected static final String EXP_UPDATED_DBMETA_TYPE = "MYSQL";

  protected static final int EXP_UPDATED_DBMETA_ACCESS = DatabaseMeta.TYPE_ACCESS_JNDI;

  protected static final String EXP_UPDATED_DBMETA_DBNAME = "lksjdfUpdated";

  protected static final String EXP_UPDATED_DBMETA_PORT = "10522";

  protected static final String EXP_UPDATED_DBMETA_USERNAME = "elaineUpdated";

  protected static final String EXP_UPDATED_DBMETA_PASSWORD = "passwordUpdated";

  protected static final String EXP_UPDATED_DBMETA_SERVERNAME = "serverNameUpdated";
  
  protected static final String EXP_DBMETA_DATA_TABLESPACE = "dataTablespace";

  protected static final String EXP_DBMETA_INDEX_TABLESPACE = "indexTablespace";
  
  private static final String EXP_DBMETA_ATTR1_VALUE = "LKJSDFKDSJKF";

  private static final String EXP_DBMETA_ATTR1_KEY = "IOWUEIOUEWR";

  private static final String EXP_DBMETA_ATTR2_KEY = "XDKDSDF";

  private static final String EXP_DBMETA_ATTR2_VALUE = "POYIUPOUI";
  
  private IDatabaseDialectService databaseDialectService;
  
  private DatabaseTypeHelper databaseTypeHelper;
  
  private FileSystemBackedUnifiedRepository unifiedRepository;
  private File pdiRootFolder = null;
  File tmpDir = null;
  public static File createTempDirectory(String folder) throws IOException {
    final File temp;

    temp = File.createTempFile(folder, Long.toString(System.nanoTime()));

    if (!(temp.delete())) {
      throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
    }

    if (!(temp.mkdir())) {
      throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
    }

    return (temp);
  }
  
  @Before
  public void setUp() throws Exception {
    File tmpDir = createTempDirectory("repository");
    microPlatform = new MicroPlatform("tests/integration-tests/resource/");
    microPlatform.define(IMetadataDomainRepository.class, MockSessionAwareMetadataDomainRepository.class, Scope.GLOBAL);
    microPlatform.define(IUnifiedRepository.class, FileSystemBackedUnifiedRepository.class, Scope.GLOBAL);
    unifiedRepository = (FileSystemBackedUnifiedRepository)PentahoSystem.get(IUnifiedRepository.class, null);
    unifiedRepository.setRootDir(tmpDir);
    microPlatform.define(IPasswordService.class, Base64PasswordService.class, Scope.GLOBAL);
    microPlatform.define(IDatabaseConnection.class, DatabaseConnection.class, Scope.GLOBAL);
    microPlatform.define(IDatabaseDialectService.class, DatabaseDialectService.class, Scope.GLOBAL);
    datasourceMgmtService = new JcrBackedDatasourceMgmtService(repo, PentahoSystem.get(IDatabaseDialectService.class));
    microPlatform.defineInstance(IDatasourceMgmtService.class, datasourceMgmtService);
    microPlatform.start();
    
    KettleEnvironment.init();
    if(!KettleEnvironment.isInitialized()) {
      throw new Exception("Kettle Environment not initialized");
    }
    databaseDialectService = PentahoSystem.get(IDatabaseDialectService.class);
    databaseTypeHelper = new DatabaseTypeHelper(databaseDialectService.getDatabaseTypes());
  }


  @Test
  public void testAddNoAdminAccess() throws Exception {
    try  {
      jdbcDatasourceService = new JDBCDatasourceService(new MockJcrBackedDatasourceMgmtService(unifiedRepository, databaseDialectService), new NonAdministratorAuthorizationPolicy());
      jdbcDatasourceService.add(new JDBCDatasource(createDatabaseConnection("mySampleData"), new DatasourceInfo("mySampleData", "mySampleData", JDBCDatasourceService.TYPE)), false);
      assertFalse(true);
    } catch(PentahoAccessControlException e) {
      assertTrue(e != null);
    }
  }
/*
  @Test
  public void testAdd() throws Exception {
    try  {
      jdbcDatasourceService = new JDBCDatasourceService(new MockJcrBackedDatasourceMgmtService(unifiedRepository, databaseDialectService), new AdministratorAuthorizationPolicy());
      jdbcDatasourceService.add(new JDBCDatasource(createDatabaseConnection("mySampleData"), "mySampleData", JDBCDatasourceService.TYPE));
      JDBCDatasource datasource = jdbcDatasourceService.get("mySampleData");
      assertNotNull(datasource);
      assertEquals(datasource.getId(), "mySampleData");
    } catch(GenericDatasourceServiceException e) {
      assertFalse(true);
    }
  }

  @Test
  public void testEditNoAdminAccess() throws Exception {
    try  {
      jdbcDatasourceService = new JDBCDatasourceService(new MockJcrBackedDatasourceMgmtService(unifiedRepository, databaseDialectService), new AdministratorAuthorizationPolicy());
      jdbcDatasourceService.add(new JDBCDatasource(createDatabaseConnection("mySampleData"), "mySampleData", JDBCDatasourceService.TYPE));
      JDBCDatasource datasource = jdbcDatasourceService.get("mySampleData");
      IDatabaseConnection connection = datasource.getDatasource();
      updateDatabaseConnection(connection);
      jdbcDatasourceService = new JDBCDatasourceService(new MockJcrBackedDatasourceMgmtService(unifiedRepository, databaseDialectService), new NonAdministratorAuthorizationPolicy());
      jdbcDatasourceService.edit(new JDBCDatasource(connection, connection.getName(), JDBCDatasourceService.TYPE));
      assertFalse(true);
    } catch(PentahoAccessControlException e) {
      assertTrue(e != null);
    }
  }

  @Test
  public void testEdit() throws Exception {
    try  {
      jdbcDatasourceService = new JDBCDatasourceService(new MockJcrBackedDatasourceMgmtService(unifiedRepository, databaseDialectService), new AdministratorAuthorizationPolicy());
      jdbcDatasourceService.add(new JDBCDatasource(createDatabaseConnection("mySampleData"), "mySampleData", JDBCDatasourceService.TYPE));
      JDBCDatasource datasource = jdbcDatasourceService.get("mySampleData");
      IDatabaseConnection connection = datasource.getDatasource();
      updateDatabaseConnection(connection);
      jdbcDatasourceService.edit(new JDBCDatasource(connection, connection.getName(), JDBCDatasourceService.TYPE));
      JDBCDatasource datasourceUpdated = jdbcDatasourceService.get("mySampleData");
      assertNotNull(datasource);
      assertEquals(datasource.getId(), "mySampleData");
      IDatabaseConnection connectionUpdated = datasource.getDatasource();
      assertEquals(connectionUpdated.getDatabaseName(), EXP_UPDATED_DBMETA_DBNAME);
    } catch(GenericDatasourceServiceException e) {
      assertFalse(true);
    }
  }


  @Test
  public void testRemove() throws Exception {
    try  {
      jdbcDatasourceService = new JDBCDatasourceService(new MockJcrBackedDatasourceMgmtService(unifiedRepository, databaseDialectService), new AdministratorAuthorizationPolicy());
      jdbcDatasourceService = new JDBCDatasourceService();
      jdbcDatasourceService.add(new JDBCDatasource(createDatabaseConnection("mySampleData"), "mySampleData", JDBCDatasourceService.TYPE));
      JDBCDatasource datasource = jdbcDatasourceService.get("mySampleData");
      jdbcDatasourceService.remove("mySampleData");
      datasource = jdbcDatasourceService.get("mySampleData");
      assertEquals(datasource, null);
    } catch(GenericDatasourceServiceException e) {
      assertTrue(true);
    }
  }
  
  @Test
  public void testRemoveNoAdminAccess() throws Exception {
    try  {
      jdbcDatasourceService = new JDBCDatasourceService(new MockJcrBackedDatasourceMgmtService(unifiedRepository, databaseDialectService), new AdministratorAuthorizationPolicy());
      jdbcDatasourceService.add(new JDBCDatasource(createDatabaseConnection("mySampleData"), "mySampleData", JDBCDatasourceService.TYPE));
      jdbcDatasourceService = new JDBCDatasourceService();
      JDBCDatasource datasource = jdbcDatasourceService.get("mySampleData");
      jdbcDatasourceService = new JDBCDatasourceService(new MockJcrBackedDatasourceMgmtService(unifiedRepository, databaseDialectService), new NonAdministratorAuthorizationPolicy());
      jdbcDatasourceService.remove("mySampleData");
      assertFalse(true);
    } catch(PentahoAccessControlException e) {
      assertTrue(e != null);
    }
  }
  
  @Test
  public void testList() throws Exception {
    try  {
      jdbcDatasourceService = new JDBCDatasourceService(new MockJcrBackedDatasourceMgmtService(unifiedRepository, databaseDialectService), new AdministratorAuthorizationPolicy());
      jdbcDatasourceService.add(new JDBCDatasource(createDatabaseConnection("mySampleData"), "mySampleData", JDBCDatasourceService.TYPE));
      jdbcDatasourceService.add(new JDBCDatasource(createDatabaseConnection("mySampleData1"), "mySampleData1", JDBCDatasourceService.TYPE));
      List<IDatasource> datasourceList = jdbcDatasourceService.getAll();
      assertEquals(datasourceList.size(), 2);
    } catch(GenericDatasourceServiceException e) {
      assertFalse(true);
    }
  }


  @Test
  public void testListNoAdminAccess() throws Exception {
    try  {
      jdbcDatasourceService = new JDBCDatasourceService(new MockJcrBackedDatasourceMgmtService(unifiedRepository, databaseDialectService), new AdministratorAuthorizationPolicy());
      jdbcDatasourceService = new JDBCDatasourceService();
      jdbcDatasourceService.add(new JDBCDatasource(createDatabaseConnection("mySampleData"), "mySampleData", JDBCDatasourceService.TYPE));
      jdbcDatasourceService.add(new JDBCDatasource(createDatabaseConnection("mySampleData1"), "mySampleData1", JDBCDatasourceService.TYPE));
      jdbcDatasourceService = new JDBCDatasourceService(new MockJcrBackedDatasourceMgmtService(unifiedRepository, databaseDialectService), new NonAdministratorAuthorizationPolicy());
      List<IDatasource> datasourceList = jdbcDatasourceService.getAll();
      assertFalse(true);
    } catch(PentahoAccessControlException e) {
      assertTrue(e != null);
    }
  }*/

  private IDatabaseConnection createDatabaseConnection(final String dbName) throws Exception {
    IDatabaseConnection dbConnection = new DatabaseConnection();
    dbConnection.setName(dbName);
    dbConnection.setHostname(EXP_DBMETA_HOSTNAME);
    dbConnection.setDatabaseType(databaseTypeHelper.getDatabaseTypeByName("Hypersonic"));
    dbConnection.setAccessType(DatabaseAccessType.NATIVE);
    dbConnection.setDatabaseName(EXP_DBMETA_DBNAME);
    dbConnection.setDatabasePort(EXP_DBMETA_PORT);
    dbConnection.setUsername(EXP_DBMETA_USERNAME);
    dbConnection.setPassword(EXP_DBMETA_PASSWORD);
    dbConnection.setInformixServername(EXP_DBMETA_SERVERNAME);
    dbConnection.setDataTablespace(EXP_DBMETA_DATA_TABLESPACE);
    dbConnection.setIndexTablespace(EXP_DBMETA_INDEX_TABLESPACE);
    // Properties attrs = new Properties();
    // exposed mutable state; yikes
    dbConnection.getAttributes().put(EXP_DBMETA_ATTR1_KEY, EXP_DBMETA_ATTR1_VALUE);
    dbConnection.getAttributes().put(EXP_DBMETA_ATTR2_KEY, EXP_DBMETA_ATTR2_VALUE);
    // TODO mlowery more testing on DatabaseMeta options
    return dbConnection;
  }
  
  private void updateDatabaseConnection(IDatabaseConnection dbConnection) throws Exception {
    dbConnection.setName(dbConnection.getName());
    dbConnection.setHostname(EXP_UPDATED_DBMETA_HOSTNAME);
    dbConnection.setDatabaseType(databaseTypeHelper.getDatabaseTypeByName("Generic database"));
    dbConnection.setAccessType(DatabaseAccessType.JNDI);
    dbConnection.setDatabaseName(EXP_UPDATED_DBMETA_DBNAME);
    dbConnection.setDatabasePort(EXP_UPDATED_DBMETA_PORT);
    dbConnection.setUsername(EXP_UPDATED_DBMETA_USERNAME);
    dbConnection.setPassword(EXP_UPDATED_DBMETA_PASSWORD);
    dbConnection.setInformixServername(EXP_UPDATED_DBMETA_SERVERNAME);
    dbConnection.setDataTablespace(EXP_DBMETA_DATA_TABLESPACE);
    dbConnection.setIndexTablespace(EXP_DBMETA_INDEX_TABLESPACE);
    // Properties attrs = new Properties();
    // exposed mutable state; yikes
    dbConnection.getAttributes().put(EXP_DBMETA_ATTR1_KEY, EXP_DBMETA_ATTR1_VALUE);
    dbConnection.getAttributes().put(EXP_DBMETA_ATTR2_KEY, EXP_DBMETA_ATTR2_VALUE);
  }
  class AdministratorAuthorizationPolicy implements IAuthorizationPolicy {

    public AdministratorAuthorizationPolicy() {
      super();
      // TODO Auto-generated constructor stub
    }

    @Override
    public boolean isAllowed(String actionName) {
      // TODO Auto-generated method stub
      return true;
    }

    @Override
    public List<String> getAllowedActions(String actionNamespace) {
      // TODO Auto-generated method stub
      return null;
    }
    
  }

  class NonAdministratorAuthorizationPolicy implements IAuthorizationPolicy {

    public NonAdministratorAuthorizationPolicy() {
      super();
      // TODO Auto-generated constructor stub
    }

    @Override
    public boolean isAllowed(String actionName) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public List<String> getAllowedActions(String actionNamespace) {
      // TODO Auto-generated method stub
      return null;
    }
    
  }
  
  class MockJcrBackedDatasourceMgmtService extends JcrBackedDatasourceMgmtService {
    private FileSystemBackedUnifiedRepository repository;
    public MockJcrBackedDatasourceMgmtService(IUnifiedRepository repository,
        IDatabaseDialectService databaseDialectService) {
      super(repository, databaseDialectService);
      this.repository = (FileSystemBackedUnifiedRepository) repository;
      // TODO Auto-generated constructor stub
    }


    protected Serializable getDatabaseParentFolderId() {
    //  return repository.getRootDir().getAbsolutePath();
      return null;
    }


    protected String getDatabaseParentFolderPath() {
      return "";
    }
    
  }
}
