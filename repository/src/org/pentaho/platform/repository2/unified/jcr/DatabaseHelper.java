package org.pentaho.platform.repository2.unified.jcr;

import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.DataProperty;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;

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

  public static DataNode DatabaseMetaToDataNode(final DatabaseMeta databaseMeta)  {
    DataNode rootNode = new DataNode(NODE_ROOT);

    // Then the basic db information
    rootNode.setProperty(PROP_TYPE, databaseMeta.getPluginId());
    rootNode.setProperty(PROP_CONTYPE, DatabaseMeta.getAccessTypeDesc(databaseMeta.getAccessType()));
    rootNode.setProperty(PROP_HOST_NAME, databaseMeta.getHostname());
    rootNode.setProperty(PROP_DATABASE_NAME, databaseMeta.getDatabaseName());
    rootNode.setProperty(PROP_PORT, new Long(databaseMeta.getDatabasePortNumberString()));
    rootNode.setProperty(PROP_USERNAME, databaseMeta.getUsername());
    rootNode.setProperty(PROP_PASSWORD, databaseMeta.getPassword());
    rootNode.setProperty(PROP_SERVERNAME, databaseMeta.getServername());
    rootNode.setProperty(PROP_DATA_TBS, databaseMeta.getDataTablespace());
    rootNode.setProperty(PROP_INDEX_TBS, databaseMeta.getIndexTablespace());

    DataNode attrNode = rootNode.addNode(NODE_ATTRIBUTES);

  // Now store all the attributes set on the database connection...
  // 
  Properties attributes = databaseMeta.getAttributes();
  Enumeration<Object> keys = databaseMeta.getAttributes().keys();
  while (keys.hasMoreElements()) {
    String code = (String) keys.nextElement();
    String attribute = (String) attributes.get(code);
  
    // Save this attribute
    //
    attrNode.setProperty(code, attribute);
  }
    return rootNode;
  }
  
  public static DatabaseMeta dataNodeToDatabaseMeta(final DataNode rootNode) {
    DatabaseMeta databaseMeta = new DatabaseMeta();
    databaseMeta.setDatabaseType(getString(rootNode, PROP_TYPE));
    databaseMeta.setAccessType(DatabaseMeta.getAccessType(getString(rootNode, PROP_CONTYPE)));
    databaseMeta.setHostname(getString(rootNode, PROP_HOST_NAME));
    databaseMeta.setDBName(getString(rootNode, PROP_DATABASE_NAME));
    databaseMeta.setDBPort(getString(rootNode, PROP_PORT));
    databaseMeta.setUsername(getString(rootNode, PROP_USERNAME));
    databaseMeta.setPassword(getString(rootNode, PROP_PASSWORD));
    databaseMeta.setServername(getString(rootNode, PROP_SERVERNAME));
    databaseMeta.setDataTablespace(getString(rootNode, PROP_DATA_TBS));
    databaseMeta.setIndexTablespace(getString(rootNode, PROP_INDEX_TBS));

    // Also, load all the properties we can find...

    DataNode attrNode = rootNode.getNode(NODE_ATTRIBUTES);
    for (DataProperty property : attrNode.getProperties()) {
      String code = property.getName();
      String attribute = property.getString();
      databaseMeta.getAttributes().put(code, (attribute == null || attribute.length() ==0) ? "": attribute); //$NON-NLS-1$
    }
    
    return databaseMeta;
  }
  
  public static DatabaseMeta assemble(RepositoryFile file, NodeRepositoryFileData data) {
    DatabaseMeta databaseMeta = (DatabaseMeta) dataNodeToDatabaseMeta(data.getNode());
    databaseMeta.setName(file.getTitle());
    databaseMeta.setObjectId(new StringObjectId(file.getId().toString()));
    databaseMeta.clearChanged();
    return databaseMeta;
  }
  private static String getString(DataNode node, String name) {
    if (node.hasProperty(name)) {
      return node.getProperty(name).getString();
    } else {
      return null;
    }
  }
  
  /**
   * Performs one-way conversion on incoming String to produce a syntactically valid JCR path (section 4.6 Path Syntax). 
   */
  public static String checkAndSanitize(final String in) {
    if (in == null) {
      throw new IllegalArgumentException();
    }
    String extension = null;
    if (in.endsWith(RepositoryObjectType.DATABASE.getExtension())) {
      extension = RepositoryObjectType.DATABASE.getExtension();
    }
    String out = in;
    if (extension != null) {
      out = out.substring(0, out.length()-extension.length());
    }
    if (out.contains("/") || out.equals("..") || out.equals(".") || StringUtils.isBlank(out)) {
      throw new IllegalArgumentException();
    }
    out = out.replaceAll("[/:\\[\\]\\*'\"\\|\\s\\.]", "_");  //$NON-NLS-1$//$NON-NLS-2$
    if (extension != null) {
      return out + extension;
    } else {
      return out;
    }
  }
}
