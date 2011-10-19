package org.pentaho.platform.repository2.unified.lifecycle;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.platform.api.data.IDatasourceService;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.api.repository2.unified.IBackingRepositoryLifecycleManager;
import org.pentaho.platform.engine.core.system.PathBasedSystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;

public class SampleDataRepositoryLifecycleManager implements IBackingRepositoryLifecycleManager{

  IDatasourceMgmtService datasourceMgmtService;
  private static final String SAMPLE_DATA = "SampleData"; //$NON-NLS-1$
  private static final String DBMETA_HOSTNAME = "localhost"; //$NON-NLS-1$
  private static final String DBMETA_TYPE = "Hypersonic"; //$NON-NLS-1$
  private static final String DBMETA_ACCESS = String.valueOf(DatabaseMeta.TYPE_ACCESS_NATIVE);
  private static final String DBMETA_DBNAME = SAMPLE_DATA; 
  private static final String DBMETA_PORT = "9001"; //$NON-NLS-1$
  private static final String DBMETA_USERNAME = "pentaho_user"; //$NON-NLS-1$
  private static final String DBMETA_PASSWORD = "password"; //$NON-NLS-1$
  private static final String DBMETA_ATTR_MAX_ACTIVE_VALUE = "20"; //$NON-NLS-1$
  private static final String DBMETA_ATTR_MAX_IDLE_VALUE = "5"; //$NON-NLS-1$
  private static final String DBMETA_ATTR_MAX_WAIT_VALUE = "1000"; //$NON-NLS-1$
  private static final String DBMETA_ATTR_QUERY_VALUE = "select count(*) from INFORMATION_SCHEMA.SYSTEM_SEQUENCES"; //$NON-NLS-1$
  private PathBasedSystemSettings settings = null;
  
  public SampleDataRepositoryLifecycleManager(IDatasourceMgmtService datasourceMgmtService) {
    super();
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
      datasourceMgmtService.getDatasourceByName(DBMETA_DBNAME);
    } catch (DatasourceMgmtServiceException dmse) {
      try {
        datasourceMgmtService.createDatasource(createDatabaseMeta());
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
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
  
  private boolean exists() {
    try {
      DatabaseMeta databaseMeta = datasourceMgmtService.getDatasourceByName(SAMPLE_DATA);
      return (databaseMeta != null);
    } catch (DatasourceMgmtServiceException e) {
      return false;
    }
  }
  
  
  private DatabaseMeta createDatabaseMeta() throws Exception {
    DatabaseMeta dbMeta = new DatabaseMeta();
    
    dbMeta.setName(settings.getSystemSetting("sampledata-datasource/name", SAMPLE_DATA)); //$NON-NLS-1$
    dbMeta.setHostname(settings.getSystemSetting("sampledata-datasource/host", DBMETA_HOSTNAME)); //$NON-NLS-1$
    dbMeta.setDatabaseType(settings.getSystemSetting("sampledata-datasource/type", DBMETA_TYPE)); //$NON-NLS-1$
    int dbAccess = Integer.parseInt(settings.getSystemSetting("sampledata-datasource/access", DBMETA_ACCESS)); //$NON-NLS-1$
    dbMeta.setAccessType(dbAccess);
    dbMeta.setDBName(settings.getSystemSetting("sampledata-datasource/name", DBMETA_DBNAME)); //$NON-NLS-1$
    dbMeta.setDBPort(settings.getSystemSetting("sampledata-datasource/port", DBMETA_PORT)); //$NON-NLS-1$
    dbMeta.setUsername(settings.getSystemSetting("sampledata-datasource/username", DBMETA_USERNAME)); //$NON-NLS-1$
    dbMeta.setPassword(settings.getSystemSetting("sampledata-datasource/password", DBMETA_PASSWORD)); //$NON-NLS-1$
    dbMeta.getAttributes().put(IDatasourceService.MAX_ACTIVE_KEY, 
        settings.getSystemSetting("sampledata-datasource/max-active", DBMETA_ATTR_MAX_ACTIVE_VALUE)); //$NON-NLS-1$
    dbMeta.getAttributes().put(IDatasourceService.MAX_IDLE_KEY,
        settings.getSystemSetting("sampledata-datasource/max-idle", DBMETA_ATTR_MAX_IDLE_VALUE)); //$NON-NLS-1$
    dbMeta.getAttributes().put(IDatasourceService.MAX_WAIT_KEY,
        settings.getSystemSetting("sampledata-datasource/max-wait", DBMETA_ATTR_MAX_WAIT_VALUE)); //$NON-NLS-1$
    dbMeta.getAttributes().put(IDatasourceService.QUERY_KEY, 
        settings.getSystemSetting("sampledata-datasource/query", DBMETA_ATTR_QUERY_VALUE)); //$NON-NLS-1$
    return dbMeta;
  }

}
