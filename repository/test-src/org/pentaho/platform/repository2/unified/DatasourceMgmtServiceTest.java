package org.pentaho.platform.repository2.unified;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.Serializable;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory.Scope;
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.util.IPasswordService;
import org.pentaho.platform.security.policy.rolebased.RoleAuthorizationPolicy;
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
    booter.start();
    datasourceMgmtService = new JcrBackedDatasourceMgmtService(repo);
    KettleEnvironment.init();
    if(!KettleEnvironment.isInitialized()) {
      throw new Exception("Kettle Environment not initialized");
    }
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
      DatabaseMeta dbMeta = createDatabaseMeta(EXP_DBMETA_NAME);
      datasourceMgmtService.createDatasource(dbMeta);
      DatabaseMeta returnDbMeta = datasourceMgmtService.getDatasourceByName(EXP_DBMETA_NAME);
      assertEquals(dbMeta.getName(), returnDbMeta.getName());
    } catch(DatasourceMgmtServiceException dmse) {
      assertFalse(true);
    }
  }

  @Test
  public void testDeleteDatasourceWithId() throws Exception {
    manager.startup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    try {
      DatabaseMeta dbMeta = createDatabaseMeta(EXP_DBMETA_NAME);
      datasourceMgmtService.createDatasource(dbMeta);
      DatabaseMeta returnDbMeta = datasourceMgmtService.getDatasourceByName(EXP_DBMETA_NAME);
      assertEquals(dbMeta.getName(), returnDbMeta.getName());
      datasourceMgmtService.deleteDatasourceById(returnDbMeta.getObjectId().getId());
      try {
        DatabaseMeta deletedDbMeta = datasourceMgmtService.getDatasourceByName(EXP_DBMETA_NAME);
      } catch(DatasourceMgmtServiceException ex) {
        assertNotNull(ex);
      }
    } catch(DatasourceMgmtServiceException dmse) {
      assertFalse(true);
    }
  }

  @Test
  public void testDeleteDatasourceWithName() throws Exception {
    manager.startup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    try {
      DatabaseMeta dbMeta = createDatabaseMeta(EXP_DBMETA_NAME);
      datasourceMgmtService.createDatasource(dbMeta);
      DatabaseMeta returnDbMeta = datasourceMgmtService.getDatasourceByName(EXP_DBMETA_NAME);
      assertEquals(dbMeta.getName(), returnDbMeta.getName());
      datasourceMgmtService.deleteDatasourceByName(returnDbMeta.getName());
      try {
        DatabaseMeta deletedDbMeta = datasourceMgmtService.getDatasourceByName(EXP_DBMETA_NAME);
      } catch(DatasourceMgmtServiceException ex) {
        assertNotNull(ex);
      }
    } catch(DatasourceMgmtServiceException dmse) {
      assertFalse(true);
    }
  }
  
  @Test
  public void testUpdateDatasourceWithId() throws Exception {
    manager.startup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    try {
      DatabaseMeta dbMeta = createDatabaseMeta(EXP_DBMETA_NAME);
      datasourceMgmtService.createDatasource(dbMeta);
      DatabaseMeta returnDbMeta = datasourceMgmtService.getDatasourceByName(EXP_DBMETA_NAME);
      assertEquals(dbMeta.getName(), returnDbMeta.getName());
      Serializable id = returnDbMeta.getObjectId().getId();
      returnDbMeta.setName(EXP_UPDATED_DBMETA_NAME);
      updateDatabaseMeta(returnDbMeta);
      datasourceMgmtService.updateDatasourceById(id, returnDbMeta);
      DatabaseMeta updatedDbMeta = datasourceMgmtService.getDatasourceByName(EXP_UPDATED_DBMETA_NAME);
      assertNotNull(updatedDbMeta);
      try {
        DatabaseMeta movedDbMeta = datasourceMgmtService.getDatasourceByName(EXP_DBMETA_NAME);
      } catch(DatasourceMgmtServiceException dmse) {
        assertNotNull(dmse);
      } 
    } catch(DatasourceMgmtServiceException dmse) {
      assertFalse(true);
    }
  }  

  @Test
  public void testUpdateDatasourceWithName() throws Exception {
    manager.startup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    try {
      DatabaseMeta dbMeta = createDatabaseMeta(EXP_DBMETA_NAME);
      datasourceMgmtService.createDatasource(dbMeta);
      DatabaseMeta returnDbMeta = datasourceMgmtService.getDatasourceByName(EXP_DBMETA_NAME);
      assertEquals(dbMeta.getName(), returnDbMeta.getName());
      String name = returnDbMeta.getName();
      returnDbMeta.setName(EXP_UPDATED_DBMETA_NAME);
      updateDatabaseMeta(returnDbMeta);
      datasourceMgmtService.updateDatasourceByName(name, returnDbMeta);
      DatabaseMeta updatedDbMeta = datasourceMgmtService.getDatasourceByName(EXP_UPDATED_DBMETA_NAME);
      assertNotNull(updatedDbMeta);
      try {
        DatabaseMeta movedDbMeta = datasourceMgmtService.getDatasourceByName(EXP_DBMETA_NAME);
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
      DatabaseMeta dbMeta = createDatabaseMeta(EXP_DBMETA_NAME);
      datasourceMgmtService.createDatasource(dbMeta);
      DatabaseMeta returnDbMeta = datasourceMgmtService.getDatasourceByName(EXP_DBMETA_NAME);
      assertEquals(dbMeta.getName(), returnDbMeta.getName());
      DatabaseMeta dbMeta1 = createDatabaseMeta(EXP_DBMETA_NAME_1);
      datasourceMgmtService.createDatasource(dbMeta1);
      DatabaseMeta returnDbMeta1 = datasourceMgmtService.getDatasourceByName(EXP_DBMETA_NAME_1);
      assertEquals(dbMeta1.getName(), returnDbMeta1.getName());
      DatabaseMeta anotherDbMeta1 = datasourceMgmtService.getDatasourceById(returnDbMeta1.getObjectId().getId());
      assertEquals(returnDbMeta1.getObjectId().getId(), anotherDbMeta1.getObjectId().getId());
      List<DatabaseMeta> datasourcesList = datasourceMgmtService.getDatasources();
      assertEquals(2, datasourcesList.size());
      List<Serializable> datasourceIdList = datasourceMgmtService.getDatasourceIds();
      assertEquals(2, datasourceIdList.size());
      
    } catch(DatasourceMgmtServiceException dmse) {
      assertFalse(true);
    }
  }  

  private DatabaseMeta createDatabaseMeta(final String dbName) throws Exception {
    DatabaseMeta dbMeta = new DatabaseMeta();
    dbMeta.setName(dbName);
    dbMeta.setHostname(EXP_DBMETA_HOSTNAME);
    dbMeta.setDatabaseType(EXP_DBMETA_TYPE);
    dbMeta.setAccessType(EXP_DBMETA_ACCESS);
    dbMeta.setDBName(EXP_DBMETA_DBNAME);
    dbMeta.setDBPort(EXP_DBMETA_PORT);
    dbMeta.setUsername(EXP_DBMETA_USERNAME);
    dbMeta.setPassword(EXP_DBMETA_PASSWORD);
    dbMeta.setServername(EXP_DBMETA_SERVERNAME);
    dbMeta.setDataTablespace(EXP_DBMETA_DATA_TABLESPACE);
    dbMeta.setIndexTablespace(EXP_DBMETA_INDEX_TABLESPACE);
    // Properties attrs = new Properties();
    // exposed mutable state; yikes
    dbMeta.getAttributes().put(EXP_DBMETA_ATTR1_KEY, EXP_DBMETA_ATTR1_VALUE);
    dbMeta.getAttributes().put(EXP_DBMETA_ATTR2_KEY, EXP_DBMETA_ATTR2_VALUE);
    // TODO mlowery more testing on DatabaseMeta options
    return dbMeta;
  }

  private void updateDatabaseMeta(DatabaseMeta dbMeta) throws Exception {
    dbMeta.setName(dbMeta.getName());
    dbMeta.setHostname(EXP_UPDATED_DBMETA_HOSTNAME);
    dbMeta.setDatabaseType(EXP_UPDATED_DBMETA_TYPE);
    dbMeta.setAccessType(EXP_UPDATED_DBMETA_ACCESS);
    dbMeta.setDBName(EXP_UPDATED_DBMETA_DBNAME);
    dbMeta.setDBPort(EXP_UPDATED_DBMETA_PORT);
    dbMeta.setUsername(EXP_UPDATED_DBMETA_USERNAME);
    dbMeta.setPassword(EXP_UPDATED_DBMETA_PASSWORD);
    dbMeta.setServername(EXP_UPDATED_DBMETA_SERVERNAME);
    dbMeta.setDataTablespace(EXP_DBMETA_DATA_TABLESPACE);
    dbMeta.setIndexTablespace(EXP_DBMETA_INDEX_TABLESPACE);
    // Properties attrs = new Properties();
    // exposed mutable state; yikes
    dbMeta.getAttributes().put(EXP_DBMETA_ATTR1_KEY, EXP_DBMETA_ATTR1_VALUE);
    dbMeta.getAttributes().put(EXP_DBMETA_ATTR2_KEY, EXP_DBMETA_ATTR2_VALUE);
  }
  
  @Override
  public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
    super.setApplicationContext(applicationContext);
    repo = (IUnifiedRepository) applicationContext.getBean("unifiedRepository");
  }
}
