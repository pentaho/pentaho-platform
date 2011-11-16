package org.pentaho.platform.repository2.unified;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.model.IDatabaseType;
import org.pentaho.database.service.DatabaseDialectService;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;

@SuppressWarnings("nls")
public class DatasourceMgmtServiceTest {

  // ~ Instance fields =================================================================================================

  //  private MicroPlatform booter;

  //  private IDatasourceMgmtService datasourceMgmtService;

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

  //  private IDatabaseDialectService databaseDialectService;
  //  
  //  private DatabaseTypeHelper databaseTypeHelper;

  public DatasourceMgmtServiceTest() {
    super();
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
    KettleEnvironment.init();
    if (!KettleEnvironment.isInitialized()) {
      throw new Exception("Kettle Environment not initialized");
    }
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testCreateDatasource() throws Exception {
    Mockery context = new Mockery();
    final IUnifiedRepository repo = context.mock(IUnifiedRepository.class);
    context.checking(new Expectations() {
      {
        final String PARENT_FOLDER_ID = "123";
        // get parent folder
        atMost(1).of(repo).getFile("/etc/pdi/databases");
        will(returnValue(new RepositoryFile.Builder(PARENT_FOLDER_ID, "databases").folder(true).build()));

        Matcher<RepositoryFile> m1 = Matchers.hasProperty("name", equal(EXP_DBMETA_NAME + ".kdb"));

        oneOf(repo).createFile(with(equal(PARENT_FOLDER_ID)), with(Matchers.allOf(m1)),
            with(any(NodeRepositoryFileData.class)), with(aNull(String.class)));
      }
    });

    IDatasourceMgmtService datasourceMgmtService = new JcrBackedDatasourceMgmtService(repo,
        new DatabaseDialectService());

    IDatabaseConnection databaseConnection = createDatabaseConnection(EXP_DBMETA_NAME);
    datasourceMgmtService.createDatasource(databaseConnection);

    context.assertIsSatisfied();
  }

  @Test
  public void testDeleteDatasourceWithName() throws Exception {
    Mockery context = new Mockery();
    final IUnifiedRepository repo = context.mock(IUnifiedRepository.class);
    context.checking(new Expectations() {
      {
        final String PARENT_FOLDER_ID = "123";
        // get parent folder
        atMost(1).of(repo).getFile("/etc/pdi/databases");
        will(returnValue(new RepositoryFile.Builder(PARENT_FOLDER_ID, "databases").folder(true).build()));
        oneOf(repo).getFile("/etc/pdi/databases/" + EXP_DBMETA_NAME + ".kdb");
        final String DB_FILE_ID = "456";
        will(returnValue(new RepositoryFile.Builder(DB_FILE_ID, EXP_DBMETA_NAME + ".kdb").build()));
        oneOf(repo).deleteFile(with(equal(DB_FILE_ID)), with(aNull(String.class)));
      }
    });

    IDatasourceMgmtService datasourceMgmtService = new JcrBackedDatasourceMgmtService(repo,
        new DatabaseDialectService());
    datasourceMgmtService.deleteDatasourceByName(EXP_DBMETA_NAME);

    context.assertIsSatisfied();
  }

  @Test
  public void testUpdateDatasourceWithName() throws Exception {
    Mockery context = new Mockery();
    final IUnifiedRepository repo = context.mock(IUnifiedRepository.class);
    context.checking(new Expectations() {
      {
        final String PARENT_FOLDER_ID = "123";
        // get parent folder
        atMost(1).of(repo).getFile("/etc/pdi/databases");
        will(returnValue(new RepositoryFile.Builder(PARENT_FOLDER_ID, "databases").folder(true).build()));
        oneOf(repo).getFile("/etc/pdi/databases/" + EXP_DBMETA_NAME + ".kdb");
        final RepositoryFile f = new RepositoryFile.Builder("456", EXP_DBMETA_NAME + ".kdb").path(
            "/etc/pdi/databases/" + EXP_DBMETA_NAME + ".kdb").build();
        will(returnValue(f));
        oneOf(repo).updateFile(with(equal(f)), with(any(NodeRepositoryFileData.class)), with(aNull(String.class)));
        will(returnValue(f));
        oneOf(repo).moveFile(with(equal(f.getId())),
            with(equal("/etc/pdi/databases/" + EXP_UPDATED_DBMETA_NAME + ".kdb")), with(aNull(String.class)));
      }
    });

    IDatasourceMgmtService datasourceMgmtService = new JcrBackedDatasourceMgmtService(repo,
        new DatabaseDialectService());

    IDatabaseConnection databaseConnection = createDatabaseConnection(EXP_DBMETA_NAME);
    updateDatabaseConnection(databaseConnection);
    datasourceMgmtService.updateDatasourceByName(EXP_DBMETA_NAME, databaseConnection);
    
    context.assertIsSatisfied();
  }

  @Test(expected = DatasourceMgmtServiceException.class)
  public void testRename() throws Exception {
    Mockery context = new Mockery();
    final IUnifiedRepository repo = context.mock(IUnifiedRepository.class);
    context.checking(new Expectations() {
      {
        final String PARENT_FOLDER_ID = "123";
        // get parent folder
        atMost(1).of(repo).getFile("/etc/pdi/databases");
        will(returnValue(new RepositoryFile.Builder(PARENT_FOLDER_ID, "databases").folder(true).build()));
        oneOf(repo).getFile("/etc/pdi/databases/not_here.kdb");
        will(returnValue(null));
      }
    });

    IDatasourceMgmtService datasourceMgmtService = new JcrBackedDatasourceMgmtService(repo,
        new DatabaseDialectService());
    datasourceMgmtService.getDatasourceByName("not_here");
    
    context.assertIsSatisfied();
  }

  @Test
  public void testGetDatasources() throws Exception {
    final String EXP_HOST_NAME = "hello";
    Mockery context = new Mockery();
    final IUnifiedRepository repo = context.mock(IUnifiedRepository.class);
    context.checking(new Expectations() {
      {
        final String PARENT_FOLDER_ID = "123";
        // get parent folder
        atMost(1).of(repo).getFile("/etc/pdi/databases");
        will(returnValue(new RepositoryFile.Builder(PARENT_FOLDER_ID, "databases").folder(true).build()));
        oneOf(repo).getFile("/etc/pdi/databases/haha.kdb");
        will(returnValue(new RepositoryFile.Builder("456", EXP_DBMETA_NAME + ".kdb").path(
            "/etc/pdi/databases/" + EXP_DBMETA_NAME + ".kdb").build()));
        oneOf(repo).getDataForRead(with(equal("456")), with(equal(NodeRepositoryFileData.class)));
        DataNode rootNode = new DataNode("databaseMeta");
        rootNode.setProperty("TYPE", "Hypersonic");
        rootNode.setProperty("HOST_NAME", EXP_HOST_NAME);
        rootNode.addNode("attributes");
        will(returnValue(new NodeRepositoryFileData(rootNode)));
      }
    });

    IDatasourceMgmtService datasourceMgmtService = new JcrBackedDatasourceMgmtService(repo,
        new DatabaseDialectService());
    IDatabaseConnection conn = datasourceMgmtService.getDatasourceByName(EXP_DBMETA_NAME);
    assertEquals(EXP_HOST_NAME, conn.getHostname());
    
    context.assertIsSatisfied();
  }

  private IDatabaseConnection createDatabaseConnection(final String dbName) throws Exception {
    IDatabaseConnection dbConnection = new DatabaseConnection();
    dbConnection.setName(dbName);
    dbConnection.setHostname(EXP_DBMETA_HOSTNAME);
    dbConnection.setDatabaseType(new MockDatabaseType("Hypersonic"));
    dbConnection.setAccessType(DatabaseAccessType.NATIVE);
    dbConnection.setDatabaseName(EXP_DBMETA_DBNAME);
    dbConnection.setDatabasePort(EXP_DBMETA_PORT);
    dbConnection.setUsername(EXP_DBMETA_USERNAME);
    dbConnection.setPassword(EXP_DBMETA_PASSWORD);
    dbConnection.setInformixServername(EXP_DBMETA_SERVERNAME);
    dbConnection.setDataTablespace(EXP_DBMETA_DATA_TABLESPACE);
    dbConnection.setIndexTablespace(EXP_DBMETA_INDEX_TABLESPACE);
    dbConnection.getAttributes().put(EXP_DBMETA_ATTR1_KEY, EXP_DBMETA_ATTR1_VALUE);
    dbConnection.getAttributes().put(EXP_DBMETA_ATTR2_KEY, EXP_DBMETA_ATTR2_VALUE);
    return dbConnection;
  }

  private void updateDatabaseConnection(IDatabaseConnection dbConnection) throws Exception {
    dbConnection.setName(EXP_UPDATED_DBMETA_NAME);
    dbConnection.setHostname(EXP_UPDATED_DBMETA_HOSTNAME);
    dbConnection.setDatabaseType(new MockDatabaseType("Generic database"));
    dbConnection.setAccessType(DatabaseAccessType.JNDI);
    dbConnection.setDatabaseName(EXP_UPDATED_DBMETA_DBNAME);
    dbConnection.setDatabasePort(EXP_UPDATED_DBMETA_PORT);
    dbConnection.setUsername(EXP_UPDATED_DBMETA_USERNAME);
    dbConnection.setPassword(EXP_UPDATED_DBMETA_PASSWORD);
    dbConnection.setInformixServername(EXP_UPDATED_DBMETA_SERVERNAME);
    dbConnection.setDataTablespace(EXP_DBMETA_DATA_TABLESPACE);
    dbConnection.setIndexTablespace(EXP_DBMETA_INDEX_TABLESPACE);
    dbConnection.getAttributes().put(EXP_DBMETA_ATTR1_KEY, EXP_DBMETA_ATTR1_VALUE);
    dbConnection.getAttributes().put(EXP_DBMETA_ATTR2_KEY, EXP_DBMETA_ATTR2_VALUE);
  }

  private static class MockDatabaseType implements IDatabaseType {

    private String shortName;

    public MockDatabaseType(final String shortName) {
      this.shortName = shortName;
    }

    @Override
    public String getName() {
      throw new UnsupportedOperationException();
    }

    @Override
    public String getShortName() {
      return shortName;
    }

    @Override
    public List<DatabaseAccessType> getSupportedAccessTypes() {
      throw new UnsupportedOperationException();
    }

    @Override
    public int getDefaultDatabasePort() {
      throw new UnsupportedOperationException();
    }

    @Override
    public String getExtraOptionsHelpUrl() {
      throw new UnsupportedOperationException();
    }

  }

}
