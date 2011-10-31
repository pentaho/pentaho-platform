package org.pentaho.platform.repository2.unified;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.service.DatabaseDialectService;
import org.pentaho.database.service.IDatabaseDialectService;
import org.pentaho.database.util.DatabaseTypeHelper;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory.Scope;
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.util.IPasswordService;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.Base64PasswordService;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/repository.spring.xml",
    "classpath:/repository-test-override.spring.xml" })
@SuppressWarnings("nls")
public class DatasourceMgmtServiceTest extends JackrabbitRepositoryTestBase implements ApplicationContextAware {
  
  // ~ Instance fields =================================================================================================

  private MicroPlatform booter;
  
  private IUnifiedRepository repo;

  private boolean startupCalled;
  
  private IDatasourceMgmtService datasourceMgmtService;

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

  public DatasourceMgmtServiceTest() {
    super();
    // TODO Auto-generated constructor stub
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
    // unfortunate reference to superclass
    JackrabbitRepositoryTestBase.setUpClass();
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
    JackrabbitRepositoryTestBase.tearDownClass();
  }

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    startupCalled = true;
    booter = new MicroPlatform();
    booter.define(IPasswordService.class, Base64PasswordService.class, Scope.GLOBAL);
    booter.defineInstance(IAuthorizationPolicy.class, authorizationPolicy);
    booter.define(IDatabaseConnection.class, DatabaseConnection.class, Scope.GLOBAL);
    booter.define(IDatabaseDialectService.class, DatabaseDialectService.class, Scope.GLOBAL);
    booter.start();
    datasourceMgmtService = new JcrBackedDatasourceMgmtService(repo, PentahoSystem.get(IDatabaseDialectService.class));
    KettleEnvironment.init();
    if(!KettleEnvironment.isInitialized()) {
      throw new Exception("Kettle Environment not initialized");
    }
    databaseDialectService = PentahoSystem.get(IDatabaseDialectService.class);
    databaseTypeHelper = new DatabaseTypeHelper(databaseDialectService.getDatabaseTypes());
  }

  @Override
  @After
  public void tearDown() throws Exception {
    super.tearDown();
    if (startupCalled) {
      manager.shutdown();
    }

    // null out fields to get back memory
    repo = null;
  }

  @Test
  public void testCreateDatasource() throws Exception {
    manager.startup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    try {
      IDatabaseConnection databaseConnection = createDatabaseConnection(EXP_DBMETA_NAME);
      datasourceMgmtService.createDatasource(databaseConnection);
      IDatabaseConnection returnDatabaseConnection = datasourceMgmtService.getDatasourceByName(EXP_DBMETA_NAME);
      assertEquals(databaseConnection.getName(), returnDatabaseConnection.getName());
    } catch(DatasourceMgmtServiceException dmse) {
      assertFalse(true);
    }
  }

/*  @Test
  public void testDeleteDatasourceWithId() throws Exception {
    manager.startup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    try {
      IDatabaseConnection databaseConnection = createDatabaseConnection(EXP_DBMETA_NAME);
      datasourceMgmtService.createDatasource(databaseConnection);
      IDatabaseConnection returnDatabaseConnection = datasourceMgmtService.getDatasourceByName(EXP_DBMETA_NAME);
      assertEquals(returnDatabaseConnection.getName(), returnDatabaseConnection.getName());
      datasourceMgmtService.deleteDatasourceById(returnDatabaseConnection.getId());
      try {
        IDatabaseConnection deletedDatabaseConnection = datasourceMgmtService.getDatasourceByName(EXP_DBMETA_NAME);
      } catch(DatasourceMgmtServiceException ex) {
        assertNotNull(ex);
      }
    } catch(DatasourceMgmtServiceException dmse) {
      assertFalse(true);
    }
  }*/

  @Test
  public void testDeleteDatasourceWithName() throws Exception {
    manager.startup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    try {
      IDatabaseConnection databaseConnection = createDatabaseConnection(EXP_DBMETA_NAME);
      datasourceMgmtService.createDatasource(databaseConnection);
      IDatabaseConnection returnDatabaseConnection = datasourceMgmtService.getDatasourceByName(EXP_DBMETA_NAME);
      assertEquals(databaseConnection.getName(), returnDatabaseConnection.getName());
      datasourceMgmtService.deleteDatasourceByName(returnDatabaseConnection.getName());
      try {
        IDatabaseConnection deletedDatabaseConnection = datasourceMgmtService.getDatasourceByName(EXP_DBMETA_NAME);
      } catch(DatasourceMgmtServiceException ex) {
        assertNotNull(ex);
      }
    } catch(DatasourceMgmtServiceException dmse) {
      assertFalse(true);
    }
  }
  
 /* @Test
  public void testUpdateDatasourceWithId() throws Exception {
    manager.startup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    try {
      IDatabaseConnection databaseConnection = createDatabaseConnection(EXP_DBMETA_NAME);
      datasourceMgmtService.createDatasource(databaseConnection);
      IDatabaseConnection returnDatabaseConnection = datasourceMgmtService.getDatasourceByName(EXP_DBMETA_NAME);
      assertEquals(databaseConnection.getName(), returnDatabaseConnection.getName());
      Serializable id = returnDatabaseConnection.getId();
      returnDatabaseConnection.setName(EXP_UPDATED_DBMETA_NAME);
      updateDatabaseConnection(returnDatabaseConnection);
      datasourceMgmtService.updateDatasourceById(id, returnDatabaseConnection);
      IDatabaseConnection updatedDatabaseConnection = datasourceMgmtService.getDatasourceByName(EXP_UPDATED_DBMETA_NAME);
      assertNotNull(updatedDatabaseConnection);
      try {
        IDatabaseConnection movedDatabaseConnection = datasourceMgmtService.getDatasourceByName(EXP_DBMETA_NAME);
      } catch(DatasourceMgmtServiceException dmse) {
        assertNotNull(dmse);
      } 
    } catch(DatasourceMgmtServiceException dmse) {
      assertFalse(true);
    }
  }  */

  @Test
  public void testUpdateDatasourceWithName() throws Exception {
    manager.startup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    try {
      IDatabaseConnection databaseConnection = createDatabaseConnection(EXP_DBMETA_NAME);
      datasourceMgmtService.createDatasource(databaseConnection);
      IDatabaseConnection returnDatabaseConnection = datasourceMgmtService.getDatasourceByName(EXP_DBMETA_NAME);
      assertEquals(databaseConnection.getName(), returnDatabaseConnection.getName());
      String name = returnDatabaseConnection.getName();
      returnDatabaseConnection.setName(EXP_UPDATED_DBMETA_NAME);
      updateDatabaseConnection(returnDatabaseConnection);
      datasourceMgmtService.updateDatasourceByName(name, returnDatabaseConnection);
      IDatabaseConnection updatedDatabaseConnection = datasourceMgmtService.getDatasourceByName(EXP_UPDATED_DBMETA_NAME);
      assertNotNull(updatedDatabaseConnection);
      try {
        IDatabaseConnection movedDatabaseConnection = datasourceMgmtService.getDatasourceByName(EXP_DBMETA_NAME);
      } catch(DatasourceMgmtServiceException dmse) {
        assertNotNull(dmse);
      } 
    } catch(DatasourceMgmtServiceException dmse) {
      assertFalse(true);
    }
  }  

  @Test
  public void testGetDatasources() throws Exception {
    manager.startup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    try {
      IDatabaseConnection databaseConnection = createDatabaseConnection(EXP_DBMETA_NAME);
      datasourceMgmtService.createDatasource(databaseConnection);
      IDatabaseConnection returnDatbaseConnection = datasourceMgmtService.getDatasourceByName(EXP_DBMETA_NAME);
      assertEquals(databaseConnection.getName(), returnDatbaseConnection.getName());
      IDatabaseConnection databaseConnection1 = createDatabaseConnection(EXP_DBMETA_NAME_1);
      datasourceMgmtService.createDatasource(databaseConnection1);
      IDatabaseConnection returnDatabaseConnection = datasourceMgmtService.getDatasourceByName(EXP_DBMETA_NAME_1);
      assertEquals(databaseConnection1.getName(), returnDatabaseConnection.getName());
  //    IDatabaseConnection anotherDbMeta1 = datasourceMgmtService.getDatasourceById(returnDatabaseConnection.getId());
  //    assertEquals(returnDatabaseConnection.getId(), anotherDbMeta1.getId());
      List<IDatabaseConnection> databaseConnectionList = datasourceMgmtService.getDatasources();
      assertEquals(2, databaseConnectionList.size());
  //    List<Serializable> datasourceIdList = datasourceMgmtService.getDatasourceIds();
  //    assertEquals(2, datasourceIdList.size());
      
    } catch(DatasourceMgmtServiceException dmse) {
      assertFalse(true);
    }
  }  

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

  @Override
  public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
    super.setApplicationContext(applicationContext);
    repo = (IUnifiedRepository) applicationContext.getBean("unifiedRepository");
  }
}
