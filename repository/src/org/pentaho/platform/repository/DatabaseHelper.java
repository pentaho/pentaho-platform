package org.pentaho.platform.repository;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.service.IDatabaseDialectService;
import org.pentaho.database.util.DatabaseTypeHelper;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.DataProperty;

public class DatabaseHelper {

  // ~ Static fields/initializers ======================================================================================

 
  private static final String PROP_INDEX_TBS = "INDEX_TBS"; //$NON-NLS-1$

  private static final String PROP_DATA_TBS = "DATA_TBS"; //$NON-NLS-1$

  private static final String PROP_SERVERNAME = "SERVERNAME"; //$NON-NLS-1$

  private static final String PROP_PASSWORD = "PASSWORD"; //$NON-NLS-1$

  private static final String PROP_USERNAME = "USERNAME"; //$NON-NLS-1$

  private static final String PROP_PORT = "PORT"; //$NON-NLS-1$

  private static final String PROP_DATABASE_NAME = "DATABASE_NAME"; //$NON-NLS-1$

  private static final String PROP_HOST_NAME = "HOST_NAME"; //$NON-NLS-1$

  private static final String PROP_CONTYPE = "CONTYPE"; //$NON-NLS-1$

  private static final String PROP_TYPE = "TYPE"; //$NON-NLS-1$

  private static final String NODE_ROOT = "databaseMeta"; //$NON-NLS-1$

  private static final String NODE_ATTRIBUTES = "attributes"; //$NON-NLS-1$
  
  private DatabaseTypeHelper databaseTypeHelper;
  
  public DatabaseHelper(IDatabaseDialectService databaseDialectService) {
    this.databaseTypeHelper = new DatabaseTypeHelper(databaseDialectService.getDatabaseTypes());
  }
  
  public DataNode databaseConnectionToDataNode(final IDatabaseConnection databaseConnection)  {
    DataNode rootNode = new DataNode(NODE_ROOT);

    // Then the basic db information
    rootNode.setProperty(PROP_TYPE, databaseConnection.getDatabaseType().getShortName());
    rootNode.setProperty(PROP_CONTYPE, databaseConnection.getAccessType().getName());
    rootNode.setProperty(PROP_HOST_NAME, databaseConnection.getHostname());
    rootNode.setProperty(PROP_DATABASE_NAME, databaseConnection.getDatabaseName());
    rootNode.setProperty(PROP_PORT, new Long(databaseConnection.getDatabasePort()));
    rootNode.setProperty(PROP_USERNAME, databaseConnection.getUsername());
    rootNode.setProperty(PROP_PASSWORD, databaseConnection.getPassword());
    rootNode.setProperty(PROP_SERVERNAME, databaseConnection.getInformixServername());
    rootNode.setProperty(PROP_DATA_TBS, databaseConnection.getDataTablespace());
    rootNode.setProperty(PROP_INDEX_TBS, databaseConnection.getIndexTablespace());
    DataNode attrNode = rootNode.addNode(NODE_ATTRIBUTES);

  // Now store all the attributes set on the database connection...
  // 
  Map<String, String> attributes = databaseConnection.getAttributes();
  Set<String> keys = attributes.keySet();
  for(String key:keys) {
    String value = attributes.get(key);
    attrNode.setProperty(key, value);
  }
    return rootNode;
  }
  
  public IDatabaseConnection dataNodeToDatabaseConnection(final Serializable id, final String name, final DataNode rootNode) {
    IDatabaseConnection databaseConnection = new DatabaseConnection();
    databaseConnection.setDatabaseType(databaseTypeHelper.getDatabaseTypeByShortName(getString(rootNode, PROP_TYPE)));
    databaseConnection.setName(name);
    databaseConnection.setAccessType(DatabaseAccessType.getAccessTypeByName(getString(rootNode, PROP_CONTYPE)));
    databaseConnection.setHostname(getString(rootNode, PROP_HOST_NAME));
    databaseConnection.setDatabaseName(getString(rootNode, PROP_DATABASE_NAME));
    databaseConnection.setDatabasePort(getString(rootNode, PROP_PORT));
    databaseConnection.setUsername(getString(rootNode, PROP_USERNAME));
    databaseConnection.setPassword(getString(rootNode, PROP_PASSWORD));
    databaseConnection.setInformixServername(getString(rootNode, PROP_SERVERNAME));
    databaseConnection.setDataTablespace(getString(rootNode, PROP_DATA_TBS));
    databaseConnection.setIndexTablespace(getString(rootNode, PROP_INDEX_TBS));

    // Also, load all the properties we can find...

    DataNode attrNode = rootNode.getNode(NODE_ATTRIBUTES);
    for (DataProperty property : attrNode.getProperties()) {
      String code = property.getName();
      String attribute = property.getString();
      databaseConnection.getAttributes().put(code, (attribute == null || attribute.length() ==0) ? "": attribute); //$NON-NLS-1$
    }
    
    return databaseConnection;
  }

  private String getString(DataNode node, String name) {
    if (node.hasProperty(name)) {
      return node.getProperty(name).getString();
    } else {
      return null;
    }
  }
  
}
