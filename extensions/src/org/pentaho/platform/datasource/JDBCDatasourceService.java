/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2005 - 2009 Pentaho Corporation.  All rights reserved. 
 *
 *
 * @created Nov 12, 2011
 * @author Ramaiz Mansoor
 */
package org.pentaho.platform.datasource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;
import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.service.IDatabaseDialectService;
import org.pentaho.database.util.DatabaseTypeHelper;
import org.pentaho.platform.api.datasource.DatasourceServiceException;
import org.pentaho.platform.api.datasource.IDatasourceInfo;
import org.pentaho.platform.api.datasource.IDatasourceService;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.DuplicateDatasourceException;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.api.repository.datasource.NonExistingDatasourceException;
import org.pentaho.platform.api.util.XmlParseException;
import org.pentaho.platform.engine.services.solution.PentahoEntityResolver;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

public class JDBCDatasourceService implements IDatasourceService{

  public static final String TYPE = "JDBC";
  private IDatasourceMgmtService datasourceMgmtService;
  private IAuthorizationPolicy policy;
  private ActionBasedSecurityService helper;
  private DatabaseTypeHelper databaseTypeHelper;
  boolean editable;
  boolean removable;
  boolean importable;
  boolean exportable;
  String defaultNewUI = "$wnd.pho.showDatabaseDialog({callback})";
  String defaultEditUI = "";
  String newUI;
  String editUI;

  public JDBCDatasourceService(IDatasourceMgmtService datasourceMgmtService, IAuthorizationPolicy policy, IDatabaseDialectService databaseDialectService) {
    this.databaseTypeHelper = new DatabaseTypeHelper(databaseDialectService.getDatabaseTypes());
    this.datasourceMgmtService = datasourceMgmtService;
    this.policy = policy;
    helper = new ActionBasedSecurityService(policy);
    this.editable = true;
    this.removable = true;
    this.importable = false;
    this.exportable = false;
  }
  @Override
  public void add(String datasourceXml, boolean overwrite) throws DatasourceServiceException, PentahoAccessControlException {
    helper.checkAdministratorAccess();
    try {
        datasourceMgmtService.createDatasource(convertToDatabaseConnection(datasourceXml));        
    } catch (DuplicateDatasourceException e) {
      throw new DatasourceServiceException(e);
    } catch (DatasourceMgmtServiceException e) {
      throw new DatasourceServiceException(e);
    }
  }

  @Override
  public String get(String id) throws DatasourceServiceException, PentahoAccessControlException {
    helper.checkAdministratorAccess();
    try {
      IDatabaseConnection databaseConnection = datasourceMgmtService.getDatasourceByName(id);
      return convertFromDatabaseConnection(databaseConnection);
    } catch (DatasourceMgmtServiceException e) {
      throw new DatasourceServiceException(e);
    } catch (ParserConfigurationException e) {
      throw new DatasourceServiceException(e);
    }
  }

  @Override
  public void remove(String id) throws DatasourceServiceException, PentahoAccessControlException {
    helper.checkAdministratorAccess();
    try {
      datasourceMgmtService.deleteDatasourceByName(id);
    } catch (NonExistingDatasourceException e) {
      throw new DatasourceServiceException(e);
    } catch (DatasourceMgmtServiceException e) {
      throw new DatasourceServiceException(e);
    }
  }

  @Override
  public void update(String datasourceXml) throws DatasourceServiceException, PentahoAccessControlException  {
    helper.checkAdministratorAccess();
    try {
        IDatabaseConnection databaseConnection = convertToDatabaseConnection(datasourceXml);
        datasourceMgmtService.updateDatasourceByName(databaseConnection.getName(), databaseConnection);          
    } catch (NonExistingDatasourceException e) {
      throw new DatasourceServiceException(e);
    } catch (DatasourceMgmtServiceException e) {
      throw new DatasourceServiceException(e);
    }
  }

  @Override
  public List<IDatasourceInfo> getIds() throws PentahoAccessControlException  {
    helper.checkAdministratorAccess();
    List<IDatasourceInfo> datasourceInfoList = new ArrayList<IDatasourceInfo>();
    try {
      for(IDatabaseConnection databaseConnection:datasourceMgmtService.getDatasources()) {
        datasourceInfoList.add(new DatasourceInfo(databaseConnection.getName(), databaseConnection.getName(), TYPE, editable, removable, importable, exportable));
      }
      return datasourceInfoList;
    } catch (DatasourceMgmtServiceException e) {
      return null;
    }
  }
  @Override
  public String getType() {
    return TYPE;
  }
  @Override
  public boolean exists(String id) throws PentahoAccessControlException {
    try {
      return get(id) != null;
    } catch (DatasourceServiceException e) {
       return false;
    }
  }

  @Override
  public void registerNewUI(String newUI) throws PentahoAccessControlException {
    this.newUI = newUI;    
  }
  @Override
  public void registerEditUI(String editUI) throws PentahoAccessControlException {
    this.editUI = editUI;
  }
  @Override
  public String getNewUI() throws PentahoAccessControlException {
    if(newUI == null) {
      return defaultNewUI;
    } else {
      return newUI;
    }
  }
  @Override
  public String getEditUI() throws PentahoAccessControlException {
    if(newUI == null) {
      return defaultNewUI;
    } else {
      return newUI;
    }
  }
  
  public IDatabaseConnection convertToDatabaseConnection(String datasourceXml) {
    Document document;
    try {
      document = XmlDom4JHelper.getDocFromString(datasourceXml, new PentahoEntityResolver());
    } catch (XmlParseException e) {
      return null;
    }
    Node rootNode = document.selectSingleNode("databaseConnection");
    IDatabaseConnection databaseConnection = new DatabaseConnection();
    databaseConnection.setDatabaseType(databaseTypeHelper.getDatabaseTypeByShortName(getValue(rootNode, "databaseType")));
    databaseConnection.setName(getValue(rootNode, "name"));
    databaseConnection.setAccessType(DatabaseAccessType.getAccessTypeByName(getValue(rootNode, "accessType")));
    databaseConnection.setHostname(getValue(rootNode, "hostname"));
    databaseConnection.setDatabaseName(getValue(rootNode, "databaseName"));
    databaseConnection.setDatabasePort(getValue(rootNode, "databasePort"));
    databaseConnection.setUsername(getValue(rootNode, "username"));
    databaseConnection.setPassword(getValue(rootNode, "password"));
    databaseConnection.setInformixServername(getValue(rootNode, "serverName"));
    databaseConnection.setDataTablespace(getValue(rootNode, "dataTablespace"));
    databaseConnection.setIndexTablespace(getValue(rootNode, "indexTablespace"));

    // Also, load all the properties we can find...
    List<Node> attributeNodes = document.selectNodes("//databaseConnection/attributes");

    for (Node node : attributeNodes) {
      String code = node.getName();
      String attribute = node.getStringValue();
      databaseConnection.getAttributes().put(code, (attribute == null || attribute.length() ==0) ? "": attribute); //$NON-NLS-1$
    }
    
    return databaseConnection;
  }
 
  public String convertFromDatabaseConnection(IDatabaseConnection databaseConnection) throws ParserConfigurationException {
    Document document = DocumentFactory.getInstance().createDocument();
    Element root = document.addElement("databaseConnection");
    Element typeElement = root.addElement("databaseType");
    typeElement.addText(databaseConnection.getDatabaseType().getShortName());
    Element nameElement = root.addElement("name");
    nameElement.addText(databaseConnection.getName());
    Element passwordElement = root.addElement("password");
    passwordElement.addText(databaseConnection.getPassword());
    Element databaseNameElement = root.addElement("databaseName");
    databaseNameElement.addText(databaseConnection.getDatabaseName());
    Element databasePortElement = root.addElement("databasePort");
    databasePortElement.addText(databaseConnection.getDatabasePort());
    Element hostnameElement = root.addElement("hostname");
    hostnameElement.addText(databaseConnection.getHostname());
    Element informixServernameElement = root.addElement("serverName");
    informixServernameElement.addText(databaseConnection.getInformixServername());
    Element accessTypeElement = root.addElement("accessType");
    accessTypeElement.addText(databaseConnection.getAccessType().getName());
    Element indexTablespaceElement = root.addElement("indexTablespace");
    indexTablespaceElement.addText(databaseConnection.getIndexTablespace());
    Element dataTablespaceElement = root.addElement("dataTablespace");
    dataTablespaceElement.addText(databaseConnection.getDataTablespace());
    Element attributes = root.addElement("attributes");
    Map<String, String> attributeMap = databaseConnection.getAttributes();
    for(String key:attributeMap.keySet()) {
      Element element = attributes.addElement(key);
      element.addText(attributeMap.get(key));
    }
    return document.asXML();
  }
  private String getValue(Node node, String xpath) {
    Element element = (Element)node.selectSingleNode(xpath);
    return element != null ? element.getText() : null;
  }  
}
