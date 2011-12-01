package org.pentaho.test.platform.datasource;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import mondrian.xmla.DataSourcesConfig.DataSource;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.api.datasource.DatasourceServiceException;
import org.pentaho.platform.api.datasource.IDatasource;
import org.pentaho.platform.api.datasource.IDatasourceInfo;
import org.pentaho.platform.api.datasource.IDatasourceService;
import org.pentaho.platform.api.datasource.IDatasourceServiceManager;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.datasource.DatasourceInfo;
import org.pentaho.platform.datasource.DefaultDatasourceServiceManager;
import org.pentaho.platform.datasource.JDBCDatasource;
import org.pentaho.platform.datasource.JDBCDatasourceService;
import org.pentaho.platform.datasource.MetadataDatasource;
import org.pentaho.platform.datasource.MetadataDatasourceService;
import org.pentaho.platform.datasource.MondrianDatasource;
import org.pentaho.platform.datasource.MondrianDatasourceService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianDataSource;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianSchema;

public class DefaultDatasourceServiceManagerTest extends TestCase{

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
  
  protected static final String EXP_DBMETA_DATA_TABLESPACE = "dataTablespace";

  protected static final String EXP_DBMETA_INDEX_TABLESPACE = "indexTablespace";
  
  private static final String EXP_DBMETA_ATTR1_VALUE = "LKJSDFKDSJKF";

  private static final String EXP_DBMETA_ATTR1_KEY = "IOWUEIOUEWR";

  private static final String EXP_DBMETA_ATTR2_KEY = "XDKDSDF";

  private static final String EXP_DBMETA_ATTR2_VALUE = "POYIUPOUI";

  @BeforeClass
  public static void setUpClass() throws Exception {
  
  }

  @AfterClass
  public static void tearDownClass() throws Exception {

  }


  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {

  }
  
  @Test
  public void testList() {
    
    IDatasourceServiceManager serviceManager = new DefaultDatasourceServiceManager();
    serviceManager.registerService(new MockMondrianDatasourceService());
    serviceManager.registerService(new MockMetadataDatasourceService());
    serviceManager.registerService(new MockJDBCDatasourceService());
    
    IDatasourceService jdbcService = serviceManager.getService(JDBCDatasourceService.TYPE);
    try {
      jdbcService.add(new JDBCDatasource(createDatabaseConnection("SampleData"), new DatasourceInfo("SampleData", "SampleData", JDBCDatasourceService.TYPE)), false);
    } catch (DatasourceServiceException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (Exception e) {
      assertTrue(e != null);
    }

    IDatasourceService metadataService = serviceManager.getService(MetadataDatasourceService.TYPE);
    try {
      metadataService.add(new MetadataDatasource(getTestDomain("MyTestDomain.xmi"), new DatasourceInfo("MyTestDomain", "MyTestDomain.xmi", MetadataDatasourceService.TYPE)), false);
    } catch (DatasourceServiceException e) {
      assertTrue(e != null);
    } catch (PentahoAccessControlException e) {
      assertTrue(e != null);
    }

    
    IDatasourceService mondrianService =  serviceManager.getService(MondrianDatasourceService.TYPE);
    try {
      mondrianService.add(new MondrianDatasource(getTestCatalog("MyTestDomain"), new DatasourceInfo("MyTestDomain", "MyTestDomain", MetadataDatasourceService.TYPE)), false);
    } catch (DatasourceServiceException e) {
      assertTrue(e != null);
    } catch (PentahoAccessControlException e) {
      assertTrue(e != null);
    }
  }
  
  class MockMondrianDatasourceService implements IDatasourceService {

    List<IDatasource> mondrianDatasourceList = new ArrayList<IDatasource>();
    @Override
    public void add(IDatasource datasource, boolean overwrite) throws DatasourceServiceException {
      mondrianDatasourceList.add(datasource);
    }

    @Override
    public void update(IDatasource arg0) throws DatasourceServiceException {
      // TODO Auto-generated method stub
      
    }

    @Override
    public IDatasource get(String id) {
      for(IDatasource mondrianDatasource:mondrianDatasourceList) {
        if(id.equals(mondrianDatasource.getDatasourceInfo().getId())) {
          return mondrianDatasource;
        }
      }
      return null;
    }

    @Override
    public String getType() {
      return MondrianDatasourceService.TYPE;
    }

    @Override
    public void remove(String arg0) throws DatasourceServiceException {
      
    }

    @Override
    public List<IDatasourceInfo> getIds() {
      List<IDatasourceInfo> datasourceInfoIds = new ArrayList<IDatasourceInfo>();
      for(IDatasource datasource:mondrianDatasourceList) {
        datasourceInfoIds.add(new DatasourceInfo(datasource.getDatasourceInfo().getId(), datasource.getDatasourceInfo().getId(), datasource.getDatasourceInfo().getType()));
      }
      return datasourceInfoIds;
    }

    @Override
    public boolean exists(String id) throws PentahoAccessControlException {
      return false;
    }
    
  }
  
  class MockJDBCDatasourceService implements  IDatasourceService {

    List<IDatasource> jdbcDatasourceList = new ArrayList<IDatasource>();
    
    @Override
    public void add(IDatasource datasource, boolean overwrite) throws DatasourceServiceException {
      jdbcDatasourceList.add(datasource);
    }

    @Override
    public void update(IDatasource arg0) throws DatasourceServiceException {
      // TODO Auto-generated method stub
      
    }

    @Override
    public IDatasource get(String id) {
      for(IDatasource jdbcDatasource:jdbcDatasourceList) {
        if(id.equals(jdbcDatasource.getDatasourceInfo().getId())) {
          return jdbcDatasource;
        }
      }
      return null;
    }

    @Override
    public String getType() {
      return JDBCDatasourceService.TYPE;
    }

    @Override
    public void remove(String arg0) throws DatasourceServiceException {
      // TODO Auto-generated method stub
      
    }

    @Override
    public List<IDatasourceInfo> getIds() {
      List<IDatasourceInfo> datasourceInfoIds = new ArrayList<IDatasourceInfo>();
      for(IDatasource datasource:jdbcDatasourceList) {
        datasourceInfoIds.add(new DatasourceInfo(datasource.getDatasourceInfo().getId(), datasource.getDatasourceInfo().getId(), datasource.getDatasourceInfo().getType()));
      }
      return datasourceInfoIds;
    }

    @Override
    public boolean exists(String id) throws PentahoAccessControlException {
      // TODO Auto-generated method stub
      return false;
    }
    
  }
  
  class MockMetadataDatasourceService implements  IDatasourceService{

    List<IDatasource> metadataDatasourceList = new ArrayList<IDatasource>();
    
    @Override
    public void add(IDatasource datasource, boolean overwrite) throws DatasourceServiceException {
      metadataDatasourceList.add(datasource);
      
    }

    @Override
    public void update(IDatasource arg0) throws DatasourceServiceException {
      // TODO Auto-generated method stub
      
    }

    @Override
    public IDatasource get(String id) {
      for(IDatasource metadataDatasource:metadataDatasourceList) {
        if(id.equals(metadataDatasource.getDatasourceInfo().getId())) {
          return metadataDatasource;
        }
      }
      return null;
    }

    @Override
    public String getType() {
      return MetadataDatasourceService.TYPE;
    }

    @Override
    public void remove(String arg0) throws DatasourceServiceException {
      // TODO Auto-generated method stub
      
    }

    @Override
    public List<IDatasourceInfo> getIds() {
      List<IDatasourceInfo> datasourceInfoIds = new ArrayList<IDatasourceInfo>();
      for(IDatasource datasource:metadataDatasourceList) {
        datasourceInfoIds.add(new DatasourceInfo(datasource.getDatasourceInfo().getId(), datasource.getDatasourceInfo().getId(), datasource.getDatasourceInfo().getType()));
      }
      return datasourceInfoIds;
    }

    @Override
    public boolean exists(String id) throws PentahoAccessControlException {
      // TODO Auto-generated method stub
      return false;
    }
    
  }
  
  
  private Domain getTestDomain(String id) {
    Domain d = new Domain();
    d.setId(id);
    return d;
  }
  
  private MondrianCatalog getTestCatalog(String id) {
    // Add an entry to the datasources.
    final MondrianSchema schema = 
      new MondrianSchema("testListRestrictedCatalogs-schema", null);
    final MondrianDataSource ds = 
      new MondrianDataSource(
        "Provider=Mondrian;DataSource=Pentaho",
        "Pentaho BI Platform Datasources",
        "http://localhost:8080/pentaho/Xmla?userid=joe&amp;password=password", 
        "Provider=Mondrian",
        "PentahoXMLA",
        DataSource.PROVIDER_TYPE_MDP, 
        DataSource.AUTH_MODE_UNAUTHENTICATED, 
        null
      );
    final MondrianCatalog cat =
      new MondrianCatalog(
        "testListRestrictedCatalogs-catalog", 
        "Provider=mondrian;DataSource=SampleDataTest",
        "solution:security/steelwheels.mondrian.xml",
        ds, 
        schema);
    return cat;
  }

  private IDatabaseConnection createDatabaseConnection(final String dbName) throws Exception {
    IDatabaseConnection dbConnection = new DatabaseConnection();
    dbConnection.setName(dbName);
    dbConnection.setHostname(EXP_DBMETA_HOSTNAME);
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
  
}
