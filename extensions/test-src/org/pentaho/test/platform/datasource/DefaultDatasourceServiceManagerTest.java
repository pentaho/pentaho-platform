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
import org.pentaho.platform.api.datasource.GenericDatasourceServiceException;
import org.pentaho.platform.api.datasource.IGenericDatasource;
import org.pentaho.platform.api.datasource.IGenericDatasourceInfo;
import org.pentaho.platform.api.datasource.IGenericDatasourceService;
import org.pentaho.platform.api.datasource.IGenericDatasourceServiceManager;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.datasource.DefaultDatasourceServiceManager;
import org.pentaho.platform.datasource.GenericDatasourceInfo;
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
    
    IGenericDatasourceServiceManager serviceManager = new DefaultDatasourceServiceManager();
    serviceManager.registerService(new MockMondrianDatasourceService());
    serviceManager.registerService(new MockMetadataDatasourceService());
    serviceManager.registerService(new MockJDBCDatasourceService());
    
    IGenericDatasourceService jdbcService = serviceManager.getService(JDBCDatasourceService.TYPE);
    try {
      jdbcService.add(new JDBCDatasource(createDatabaseConnection("SampleData"), "SampleData", JDBCDatasourceService.TYPE));
    } catch (GenericDatasourceServiceException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (Exception e) {
      assertTrue(e != null);
    }

    IGenericDatasourceService metadataService = serviceManager.getService(MetadataDatasourceService.TYPE);
    try {
      metadataService.add(new MetadataDatasource(getTestDomain("MyTestDomain.xmi"), "MyTestDomain.xmi", MetadataDatasourceService.TYPE));
    } catch (GenericDatasourceServiceException e) {
      assertTrue(e != null);
    } catch (PentahoAccessControlException e) {
      assertTrue(e != null);
    }

    
    IGenericDatasourceService mondrianService =  serviceManager.getService(MondrianDatasourceService.TYPE);
    try {
      mondrianService.add(new MondrianDatasource(getTestCatalog("MyTestDomain"), "MyTestDomain", MetadataDatasourceService.TYPE));
    } catch (GenericDatasourceServiceException e) {
      assertTrue(e != null);
    } catch (PentahoAccessControlException e) {
      assertTrue(e != null);
    }

    
    List<IGenericDatasource> datasourceList = null;
    try {
      datasourceList = serviceManager.getAll();
    } catch (PentahoAccessControlException e) {
      assertTrue(e != null);
    }
    assertEquals(datasourceList.size(), 3);
  }
  
  class MockMondrianDatasourceService implements IGenericDatasourceService {

    List<IGenericDatasource> mondrianDatasourceList = new ArrayList<IGenericDatasource>();
    @Override
    public void add(IGenericDatasource datasource) throws GenericDatasourceServiceException {
      mondrianDatasourceList.add(datasource);
    }

    @Override
    public void edit(IGenericDatasource arg0) throws GenericDatasourceServiceException {
      // TODO Auto-generated method stub
      
    }

    @Override
    public IGenericDatasource get(String id) {
      for(IGenericDatasource mondrianDatasource:mondrianDatasourceList) {
        if(id.equals(mondrianDatasource.getId())) {
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
    public List<IGenericDatasource> getAll() {
      return mondrianDatasourceList;
    }

    @Override
    public void remove(String arg0) throws GenericDatasourceServiceException {
      
    }

    @Override
    public List<IGenericDatasourceInfo> getIds() {
      List<IGenericDatasourceInfo> datasourceInfoIds = new ArrayList<IGenericDatasourceInfo>();
      for(IGenericDatasource datasource:mondrianDatasourceList) {
        datasourceInfoIds.add(new GenericDatasourceInfo(datasource.getId(), datasource.getType()));
      }
      return datasourceInfoIds;
    }
    
  }
  
  class MockJDBCDatasourceService implements  IGenericDatasourceService {

    List<IGenericDatasource> jdbcDatasourceList = new ArrayList<IGenericDatasource>();
    
    @Override
    public void add(IGenericDatasource datasource) throws GenericDatasourceServiceException {
      jdbcDatasourceList.add(datasource);
    }

    @Override
    public void edit(IGenericDatasource arg0) throws GenericDatasourceServiceException {
      // TODO Auto-generated method stub
      
    }

    @Override
    public IGenericDatasource get(String id) {
      for(IGenericDatasource jdbcDatasource:jdbcDatasourceList) {
        if(id.equals(jdbcDatasource.getId())) {
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
    public List<IGenericDatasource> getAll() {
      return jdbcDatasourceList;
    }

    @Override
    public void remove(String arg0) throws GenericDatasourceServiceException {
      // TODO Auto-generated method stub
      
    }

    @Override
    public List<IGenericDatasourceInfo> getIds() {
      List<IGenericDatasourceInfo> datasourceInfoIds = new ArrayList<IGenericDatasourceInfo>();
      for(IGenericDatasource datasource:jdbcDatasourceList) {
        datasourceInfoIds.add(new GenericDatasourceInfo(datasource.getId(), datasource.getType()));
      }
      return datasourceInfoIds;
    }
    
  }
  
  class MockMetadataDatasourceService implements  IGenericDatasourceService{

    List<IGenericDatasource> metadataDatasourceList = new ArrayList<IGenericDatasource>();
    
    @Override
    public void add(IGenericDatasource datasource) throws GenericDatasourceServiceException {
      metadataDatasourceList.add(datasource);
      
    }

    @Override
    public void edit(IGenericDatasource arg0) throws GenericDatasourceServiceException {
      // TODO Auto-generated method stub
      
    }

    @Override
    public IGenericDatasource get(String id) {
      for(IGenericDatasource metadataDatasource:metadataDatasourceList) {
        if(id.equals(metadataDatasource.getId())) {
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
    public List<IGenericDatasource> getAll() {
      return metadataDatasourceList;
    }

    @Override
    public void remove(String arg0) throws GenericDatasourceServiceException {
      // TODO Auto-generated method stub
      
    }

    @Override
    public List<IGenericDatasourceInfo> getIds() {
      List<IGenericDatasourceInfo> datasourceInfoIds = new ArrayList<IGenericDatasourceInfo>();
      for(IGenericDatasource datasource:metadataDatasourceList) {
        datasourceInfoIds.add(new GenericDatasourceInfo(datasource.getId(), datasource.getType()));
      }
      return datasourceInfoIds;
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
