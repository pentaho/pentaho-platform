package org.pentaho.platform.repository2.unified.lifecycle;

import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.service.IDatabaseDialectService;
import org.pentaho.database.util.DatabaseTypeHelper;
import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.api.repository2.unified.IBackingRepositoryLifecycleManager;
import org.pentaho.platform.engine.core.system.PathBasedSystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;

public class SampleDataRepositoryLifecycleManager implements IBackingRepositoryLifecycleManager{

  IDatasourceMgmtService datasourceMgmtService;
  DatabaseTypeHelper databaseTypeHelper;
  private static final String SAMPLE_DATA = "SampleData"; //$NON-NLS-1$
  private static final String DBMETA_HOSTNAME = "localhost"; //$NON-NLS-1$
  private static final String DBMETA_TYPE = "Hypersonic"; //$NON-NLS-1$
  private static final DatabaseAccessType DBMETA_ACCESS = DatabaseAccessType.NATIVE;
  private static final String DBMETA_DBNAME = SAMPLE_DATA; 
  private static final String DBMETA_PORT = "9001"; //$NON-NLS-1$
  private static final String DBMETA_USERNAME = "pentaho_user"; //$NON-NLS-1$
  private static final String DBMETA_PASSWORD = "password"; //$NON-NLS-1$
  private static final String DBMETA_ATTR_MAX_ACTIVE_VALUE = "20"; //$NON-NLS-1$
  private static final String DBMETA_ATTR_MAX_IDLE_VALUE = "5"; //$NON-NLS-1$
  private static final String DBMETA_ATTR_MAX_WAIT_VALUE = "1000"; //$NON-NLS-1$
  private static final String DBMETA_ATTR_QUERY_VALUE = "select count(*) from INFORMATION_SCHEMA.SYSTEM_SEQUENCES"; //$NON-NLS-1$
  private PathBasedSystemSettings settings = null;
  
  public SampleDataRepositoryLifecycleManager(IDatasourceMgmtService datasourceMgmtService, IDatabaseDialectService databaseDialectService) {
    super();
    this.databaseTypeHelper = new DatabaseTypeHelper(databaseDialectService.getDatabaseTypes());
    this.datasourceMgmtService = datasourceMgmtService;
    settings = new PathBasedSystemSettings();
  }

  @Override
  public void startup() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void shutdown() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void newTenant(String tenantId) {
    try {
      IDatabaseConnection databaseConnection = datasourceMgmtService.getDatasourceByName(DBMETA_DBNAME);
      if(databaseConnection == null) {
        createDatasource();
      }
    } catch (DatasourceMgmtServiceException dmse) {
      createDatasource();
    }
  }

  @Override
  public void newTenant() {
    newTenant((String) PentahoSessionHolder.getSession().getAttribute(IPentahoSession.TENANT_ID_KEY));
  }

  @Override
  public void newUser(String tenantId, String username) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void newUser() {
    // TODO Auto-generated method stub
    
  }
  
 
  private IDatabaseConnection createDatabaseconnection() throws Exception {
    IDatabaseConnection databaseConnection = new DatabaseConnection();
    databaseConnection.setName(settings.getSystemSetting("sampledata-datasource/name", SAMPLE_DATA)); //$NON-NLS-1$
    databaseConnection.setHostname(settings.getSystemSetting("sampledata-datasource/host", DBMETA_HOSTNAME)); //$NON-NLS-1$
    databaseConnection.setDatabaseType(databaseTypeHelper.getDatabaseTypeByName(settings.getSystemSetting("sampledata-datasource/type", DBMETA_TYPE))); //$NON-NLS-1$
    databaseConnection.setAccessType(DatabaseAccessType.valueOf(settings.getSystemSetting("sampledata-datasource/access", DBMETA_ACCESS.toString())));
    databaseConnection.setDatabaseName(settings.getSystemSetting("sampledata-datasource/name", DBMETA_DBNAME)); //$NON-NLS-1$
    databaseConnection.setDatabasePort(settings.getSystemSetting("sampledata-datasource/port", DBMETA_PORT)); //$NON-NLS-1$
    databaseConnection.setUsername(settings.getSystemSetting("sampledata-datasource/username", DBMETA_USERNAME)); //$NON-NLS-1$
    databaseConnection.setPassword(settings.getSystemSetting("sampledata-datasource/password", DBMETA_PASSWORD)); //$NON-NLS-1$
    databaseConnection.getAttributes().put(IDBDatasourceService.MAX_ACTIVE_KEY, 
        settings.getSystemSetting("sampledata-datasource/max-active", DBMETA_ATTR_MAX_ACTIVE_VALUE)); //$NON-NLS-1$
    databaseConnection.getAttributes().put(IDBDatasourceService.MAX_IDLE_KEY,
        settings.getSystemSetting("sampledata-datasource/max-idle", DBMETA_ATTR_MAX_IDLE_VALUE)); //$NON-NLS-1$
    databaseConnection.getAttributes().put(IDBDatasourceService.MAX_WAIT_KEY,
        settings.getSystemSetting("sampledata-datasource/max-wait", DBMETA_ATTR_MAX_WAIT_VALUE)); //$NON-NLS-1$
    databaseConnection.getAttributes().put(IDBDatasourceService.QUERY_KEY, 
        settings.getSystemSetting("sampledata-datasource/query", DBMETA_ATTR_QUERY_VALUE)); //$NON-NLS-1$
    return databaseConnection;
  }

  private void createDatasource() {
    try {
      datasourceMgmtService.createDatasource(createDatabaseconnection());
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
