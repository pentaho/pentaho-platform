package org.pentaho.platform.repository;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.service.IDatabaseDialectService;
import org.pentaho.database.util.DatabaseTypeHelper;
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
  
  private static final String NODE_POOLING_PROPS = "poolProps"; //$NON-NLS-1$
  
  private static final String PROP_CONNECT_SQL = "connectionSQL"; //$NON-NLS-1$
  
  private static final String PROP_INITIAL_POOL_SIZE = "initialPoolSize"; //$NON-NLS-1$
  
  private static final String PROP_MAX_POOL_SIZE = "maxPoolSize"; //$NON-NLS-1$
  
  private static final String PROP_IS_POOLING = "isPooling"; //$NON-NLS-1$
  
  private static final String PROP_IS_FORCING_TO_LOWER = "isForcingLower"; //$NON-NLS-1$
  
  private static final String PROP_IS_FORCING_TO_UPPER = "isForcingUpper"; //$NON-NLS-1$
  
  private static final String PROP_IS_QUOTE_FIELDS = "isQuoteFields"; //$NON-NLS-1$
  
  private static final String PROP_IS_DECIMAL_SEPERATOR = "isUsingDecimalSeperator";//$NON-NLS-1$
  
  private DatabaseTypeHelper databaseTypeHelper;
  
  public DatabaseHelper(IDatabaseDialectService databaseDialectService) {
    this.databaseTypeHelper = new DatabaseTypeHelper(databaseDialectService.getDatabaseTypes());
  }
  
  public DataNode databaseConnectionToDataNode(final IDatabaseConnection databaseConnection)  {
    DataNode rootNode = new DataNode(NODE_ROOT);

    // Then the basic db information
    if(databaseConnection.getDatabaseType() != null) {
      rootNode.setProperty(PROP_TYPE, databaseConnection.getDatabaseType().getShortName());      
    }
    String port = ("".equals(setNull(databaseConnection.getDatabasePort())))?"0":databaseConnection.getDatabasePort();
    rootNode.setProperty(PROP_CONTYPE, setNull(databaseConnection.getAccessType().getName()));
    rootNode.setProperty(PROP_HOST_NAME, setNull(databaseConnection.getHostname()));
    rootNode.setProperty(PROP_DATABASE_NAME, setNull(databaseConnection.getDatabaseName()));
    rootNode.setProperty(PROP_PORT, new Long(port));
    rootNode.setProperty(PROP_USERNAME, setNull(databaseConnection.getUsername()));
    rootNode.setProperty(PROP_PASSWORD, setNull(databaseConnection.getPassword()));
    rootNode.setProperty(PROP_SERVERNAME, setNull(databaseConnection.getInformixServername()));
    rootNode.setProperty(PROP_DATA_TBS, setNull(databaseConnection.getDataTablespace()));
    rootNode.setProperty(PROP_INDEX_TBS, setNull(databaseConnection.getIndexTablespace()));
    rootNode.setProperty(PROP_CONNECT_SQL, setNull(databaseConnection.getConnectSql()));
    rootNode.setProperty(PROP_INITIAL_POOL_SIZE, databaseConnection.getInitialPoolSize());
    rootNode.setProperty(PROP_MAX_POOL_SIZE, databaseConnection.getMaximumPoolSize());
    rootNode.setProperty(PROP_IS_POOLING, databaseConnection.isUsingConnectionPool());
    rootNode.setProperty(PROP_IS_FORCING_TO_LOWER, databaseConnection.isForcingIdentifiersToLowerCase());
    rootNode.setProperty(PROP_IS_FORCING_TO_UPPER, databaseConnection.isForcingIdentifiersToUpperCase());
    rootNode.setProperty(PROP_IS_QUOTE_FIELDS, databaseConnection.isQuoteAllFields());
    rootNode.setProperty(PROP_IS_DECIMAL_SEPERATOR, databaseConnection.isUsingDoubleDecimalAsSchemaTableSeparator());
    
    // Now store all the attributes set on the database connection...
    DataNode attrNode = rootNode.addNode(NODE_ATTRIBUTES);
    Map<String, String> attributes = databaseConnection.getAttributes();
    Set<String> keys = attributes.keySet();
    for(String key:keys) {
        String value = attributes.get(key);
        attrNode.setProperty(key, value);
    }
    
    // Now store the pooling parameters
    attrNode = rootNode.addNode(NODE_POOLING_PROPS);
    attributes = databaseConnection.getConnectionPoolingProperties();
    keys = attributes.keySet();
    for(String key:keys) {
      String value = attributes.get(key);
      attrNode.setProperty(key, value);
    }
    return rootNode;
  }
  
  public IDatabaseConnection dataNodeToDatabaseConnection(final Serializable id, final String name, final DataNode rootNode) {
    IDatabaseConnection databaseConnection = new DatabaseConnection();
    String databaseType  = getString(rootNode, PROP_TYPE);
    databaseConnection.setDatabaseType(databaseType != null ? databaseTypeHelper.getDatabaseTypeByShortName(databaseType) : null);
    databaseConnection.setName(name);
    if(id != null) {
      databaseConnection.setId(id.toString());      
    }
    String accessType = getString(rootNode, PROP_CONTYPE);
    databaseConnection.setAccessType(accessType != null ? DatabaseAccessType.getAccessTypeByName(accessType): null);
    databaseConnection.setHostname(getString(rootNode, PROP_HOST_NAME));
    databaseConnection.setDatabaseName(getString(rootNode, PROP_DATABASE_NAME));
    databaseConnection.setDatabasePort(getString(rootNode, PROP_PORT));
    databaseConnection.setUsername(getString(rootNode, PROP_USERNAME));
    databaseConnection.setPassword(getString(rootNode, PROP_PASSWORD));
    databaseConnection.setInformixServername(getString(rootNode, PROP_SERVERNAME));
    databaseConnection.setDataTablespace(getString(rootNode, PROP_DATA_TBS));
    databaseConnection.setIndexTablespace(getString(rootNode, PROP_INDEX_TBS));
    databaseConnection.setConnectSql(getString(rootNode, PROP_CONNECT_SQL));
    databaseConnection.setInitialPoolSize(getInt(rootNode, PROP_INITIAL_POOL_SIZE));
    databaseConnection.setMaximumPoolSize(getInt(rootNode, PROP_MAX_POOL_SIZE));
    databaseConnection.setUsingConnectionPool(getBoolean(rootNode, PROP_IS_POOLING));
    databaseConnection.setForcingIdentifiersToLowerCase(getBoolean(rootNode, PROP_IS_FORCING_TO_LOWER));
    databaseConnection.setForcingIdentifiersToUpperCase(getBoolean(rootNode, PROP_IS_FORCING_TO_UPPER));
    databaseConnection.setQuoteAllFields(getBoolean(rootNode, PROP_IS_QUOTE_FIELDS));
    databaseConnection.setUsingDoubleDecimalAsSchemaTableSeparator(getBoolean(rootNode, PROP_IS_DECIMAL_SEPERATOR));
    
    // Also, load all the properties we can find...
    DataNode attrNode = rootNode.getNode(NODE_ATTRIBUTES);
    if (attrNode != null) {
      for (DataProperty property : attrNode.getProperties()) {
        String code = property.getName();
        String attribute = property.getString();
        databaseConnection.getAttributes().put(code, (attribute == null || attribute.length() ==0) ? "": attribute); //$NON-NLS-1$
      }
    }
    
    // Also, load any pooling params
    attrNode = rootNode.getNode(NODE_POOLING_PROPS);
    if (attrNode != null) {
      for (DataProperty property : attrNode.getProperties()) {
        String code = property.getName();
        String attribute = property.getString();
        databaseConnection.getConnectionPoolingProperties().put(code, (attribute == null || attribute.length() ==0) ? "": attribute); //$NON-NLS-1$
      }
    }

    return databaseConnection;
  }
  
  private String setNull(String value) {
    String response = value;
    if(value == null)
      response = "";
    return response;
  }

  private int getInt(DataNode node, String name) {
    if (node.hasProperty(name)) {
      return (int) node.getProperty(name).getLong();
    } else {
      return 0;
    }
  }
  
  private boolean getBoolean(DataNode node, String name) {
    if (node.hasProperty(name)) {
      return node.getProperty(name).getBoolean();
    } else {
      return false;
    }
  }

  
  private String getString(DataNode node, String name) {
    if (node.hasProperty(name)) {
      return node.getProperty(name).getString();
    } else {
      return null;
    }
  }
  
}
