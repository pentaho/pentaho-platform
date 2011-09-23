/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2008 Pentaho Corporation.  All rights reserved. 
 * 
 * Created Mar 14, 2006 
 * @author wseyler
 */

package org.pentaho.platform.uifoundation.component.xml;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.pentaho.platform.api.engine.IAclSolutionFile;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.engine.IPermissionMask;
import org.pentaho.platform.api.engine.IPermissionRecipient;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SimplePermissionMask;
import org.pentaho.platform.engine.security.SimpleRole;
import org.pentaho.platform.engine.security.SimpleUser;
import org.pentaho.platform.engine.security.acls.PentahoAclEntry;
import org.pentaho.platform.repository.hibernate.HibernateUtil;
import org.pentaho.platform.uifoundation.messages.Messages;

/**
 * TODO mlowery Need to remove direct references to PentahoAclEntry permission constants. Instead, reference 
 * ISolutionRepository permission constants.
 */
public class PropertiesPanelUIComponent extends XmlComponent {

  private static final long serialVersionUID = 1L;

  private static final Log logger = LogFactory.getLog(PropertiesPanelUIComponent.class);

  private static final String TYPE_PARAM = "type"; //$NON-NLS-1$

  private static final String ACTION_PARAM = "action"; //$NON-NLS-1$

  private static final String ADD_NAME_PARAM = "add_name"; //$NON-NLS-1$

  private static final String PATH_PARAM = "path"; //$NON-NLS-1$

  private static final String LIST_ACTION = "list"; //$NON-NLS-1$

  private static final String ADD_BTN_PARAM = "addBtn"; //$NON-NLS-1$

  private static final String UPDATE_BTN_PARAM = "updateBtn"; //$NON-NLS-1$

  private static final String ROLE_TYPE = "role"; //$NON-NLS-1$

  private static final String PERM_TYPE = "perm"; //$NON-NLS-1$

  private static final String ROLE_PREFIX = PropertiesPanelUIComponent.ROLE_TYPE + "_"; //$NON-NLS-1$
  // NOTE: not related to PentahoAclEntry.PERMISSION_PREFIX
  private static final String PERMISSION_PREFIX = PropertiesPanelUIComponent.PERM_TYPE + "_"; //$NON-NLS-1$
  
  private static final String USER_TYPE = "user"; //$NON-NLS-1$

  private static final String USER_PREFIX = PropertiesPanelUIComponent.USER_TYPE + "_"; //$NON-NLS-1$

  private static final String PERMISSION_SEPERATOR = "#"; //$NON-NLS-1$

  private static final String DELETE_PREFIX = "delete_"; //$NON-NLS-1$

  private static final String NO_FILE_PATH_NODE_NAME = "no-file-path"; //$NON-NLS-1$

  private static final String SET_PERMISSIONS_DENIED_NAME = "set-permissions-denied"; //$NON-NLS-1$

  private static final String NO_ACLS_NODE_NAME = "no-acls"; //$NON-NLS-1$

  private static final String INPUT_PAGE_NODE_NAME = "input-page"; //$NON-NLS-1$

  private static final String FILE_PATH_NODE_NAME = "file-path"; //$NON-NLS-1$

  private static final String IS_DIR_NODE_NAME = "is-directory"; //$NON-NLS-1$

  private static final String RECIPIENTS_NODE_NAME = "recipients"; //$NON-NLS-1$

  private static final String ROLE_NODE_NAME = "role"; //$NON-NLS-1$

  private static final String USER_NODE_NAME = "user"; //$NON-NLS-1$

  private static final String PERMISSION_NAMES_NODE_NAME = "permission-names"; //$NON-NLS-1$

  private static final String NAME_NODE_NAME = "name"; //$NON-NLS-1$

  private static final String ACCESS_CONTROL_LIST_NODE_NAME = "ac-list"; //$NON-NLS-1$

  private static final String ACCESS_CONTROL_NODE_NAME = "access-control"; //$NON-NLS-1$

  private static final String RECIPIENT_NODE_NAME = "recipient"; //$NON-NLS-1$

  private static final String PERMISSION_NODE_NAME = "permission"; //$NON-NLS-1$

  private static final String PERMITTED_NODE_NAME = "permitted"; //$NON-NLS-1$

  private static final String EMPTY_STRING = ""; //$NON-NLS-1$

  private static final String TRUE = "true"; //$NON-NLS-1$

  private static final String FALSE = "false"; //$NON-NLS-1$

  private static final String ON = "on"; //$NON-NLS-1$

  private static final String DISPLAY_PATH_NODE_NAME = "display-path"; //$NON-NLS-1$

  protected IPentahoSession session = null;

  protected String baseUrl = null;

  boolean includeUsers = PentahoSystem.getSystemSetting("access-ui/include-users", "true").equals("true"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

  boolean includeRoles = PentahoSystem.getSystemSetting("access-ui/include-roles", "true").equals("true"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

  protected ISolutionRepository repository;

  private List allUsersList;

  private List allRolesList;

  protected IUserRoleListService userRoleListService;

  public PropertiesPanelUIComponent(final IPentahoUrlFactory urlFactory, final List messages,
      final IPentahoSession session) {
    super(urlFactory, messages, null);
    this.session = session;
    setXsl("text/html", "PropertiesPanel.xsl"); //$NON-NLS-1$ //$NON-NLS-2$
    setXslProperty("baseUrl", urlFactory.getDisplayUrlBuilder().getUrl()); //$NON-NLS-1$ 
    repository = PentahoSystem.get(ISolutionRepository.class, session);
    if (!repository.supportsAccessControls()) {
      error(Messages.getInstance().getString("PropertiesPanelUIComponent.ERROR_0001_BAD_CONFIGURATION")); //$NON-NLS-1$
    }
    userRoleListService = PentahoSystem.get(IUserRoleListService.class);
  }

  @Override
  public Document getXmlContent() {
    if (!repository.supportsAccessControls()) {
      return noACLSPage();
    }

    String actionStr = this.getParameter(PropertiesPanelUIComponent.ACTION_PARAM,
        PropertiesPanelUIComponent.EMPTY_STRING); // No
    // nulls
    String pathStr = this.getParameter(PropertiesPanelUIComponent.PATH_PARAM, null);
    ISolutionFile file = null;
    try {
      HibernateUtil.beginTransaction();
      file = repository.getSolutionFile(pathStr, ISolutionRepository.ACTION_EXECUTE);
    } catch (Exception e) {
      // do nothing since we want file to be null if it wasn't found
      // TODO sbarkdull, arg, let's at least log it
    }

    // default action if none is passed is to list (showInputPage) the acls
    if ((actionStr == null) || actionStr.equalsIgnoreCase(PropertiesPanelUIComponent.LIST_ACTION)
        || actionStr.equalsIgnoreCase(PropertiesPanelUIComponent.EMPTY_STRING)) {
      HibernateUtil.commitTransaction();
      if (file != null) {
        return showInputPage(file);
      }
      return noPathPage();
    } else {
      IParameterProvider request = ((IParameterProvider) getParameterProviders().get(IParameterProvider.SCOPE_REQUEST));
      if ((request.getParameter(PropertiesPanelUIComponent.ADD_BTN_PARAM) != null)
          && !request.getParameter(PropertiesPanelUIComponent.ADD_BTN_PARAM).equals("")) { //$NON-NLS-1$
        doAddToAcls(file);
      }
      try {
        if ((request.getParameter(PropertiesPanelUIComponent.UPDATE_BTN_PARAM) != null)
            && !request.getParameter(PropertiesPanelUIComponent.UPDATE_BTN_PARAM).equals("")) { //$NON-NLS-1$
          doUpdateAcls(file);
        }
      } catch (PentahoAccessControlException e) {
        HibernateUtil.rollbackTransaction();
        return setPermissionsFailedPage(e.getLocalizedMessage());
      }
      HibernateUtil.commitTransaction();
      repository.resetRepository();
      if (file != null) {
        return showInputPage(file);
      }
      return noPathPage();
    }
  }

  private void doUpdateAcls(final ISolutionFile file) throws PentahoAccessControlException {
    IParameterProvider request = ((IParameterProvider) getParameterProviders().get(IParameterProvider.SCOPE_REQUEST));

    Map permMap = new LinkedHashMap<IPermissionRecipient, IPermissionMask>();
    Iterator it = request.getParameterNames();
    while (it.hasNext()) {
      String name = (String) it.next();
      if (name.startsWith(PropertiesPanelUIComponent.USER_PREFIX)
          || name.startsWith(PropertiesPanelUIComponent.ROLE_PREFIX)) {
        boolean isRole = name.startsWith(PropertiesPanelUIComponent.ROLE_PREFIX);
        name = name.replaceFirst(isRole ? PropertiesPanelUIComponent.ROLE_PREFIX
            : PropertiesPanelUIComponent.USER_PREFIX, PropertiesPanelUIComponent.EMPTY_STRING);
        String lineNumber = name.substring(name.lastIndexOf('_') + 1);
        name = name.substring(0, name.lastIndexOf('_'));
        if (!isFlaggedForDelete(name)) { // If this is one being deleted we don't do anything with it
          IPermissionRecipient permissionRecipient = isRole ? new SimpleRole(name) : new SimpleUser(name);
          SimplePermissionMask permissionMask = new SimplePermissionMask();
          Iterator it1 = request.getParameterNames();
          while (it1.hasNext()) {
            String perm = (String) it1.next();
            if (perm.startsWith(PropertiesPanelUIComponent.PERMISSION_PREFIX)) {
              perm = perm.replaceFirst(PropertiesPanelUIComponent.PERMISSION_PREFIX,
                  PropertiesPanelUIComponent.EMPTY_STRING);
              String permNumber = perm.substring(perm.lastIndexOf('_') + 1);
              if (permNumber.equals(lineNumber)) { // Congratulation... we have a winner!
                perm = perm.substring(0, perm.lastIndexOf('_'));
                permissionMask.addPermission(((Integer) PentahoAclEntry.getValidPermissionsNameMap().get(perm))
                    .intValue());
              }
            }
          }
          permMap.put(permissionRecipient, permissionMask);
        }
      }
    }
    if (file instanceof IAclSolutionFile) {
      repository.setPermissions(file, permMap);
    }
  }

  private boolean isFlaggedForDelete(final String name) {
    IParameterProvider request = ((IParameterProvider) getParameterProviders().get(IParameterProvider.SCOPE_REQUEST));
    return PropertiesPanelUIComponent.ON.equalsIgnoreCase(request.getStringParameter(
        PropertiesPanelUIComponent.DELETE_PREFIX + name, null));
  }

  private void doAddToAcls(final ISolutionFile file) {
    IParameterProvider request = ((IParameterProvider) getParameterProviders().get(IParameterProvider.SCOPE_REQUEST));

    String[] names = request.getStringArrayParameter(PropertiesPanelUIComponent.ADD_NAME_PARAM, new String[] {});

    for (String name : names) {
      IPermissionRecipient permissionRecipient = null;
      if (name.startsWith(PropertiesPanelUIComponent.ROLE_PREFIX)) {
        permissionRecipient = new SimpleRole(name.replaceFirst(PropertiesPanelUIComponent.ROLE_PREFIX,
            PropertiesPanelUIComponent.EMPTY_STRING));
      } else {
        permissionRecipient = new SimpleUser(name.replaceFirst(PropertiesPanelUIComponent.USER_PREFIX,
            PropertiesPanelUIComponent.EMPTY_STRING));
      }

      SimplePermissionMask permissionMask = new SimplePermissionMask();
      Iterator it = request.getParameterNames();
      while (it.hasNext()) {
        String paramName = it.next().toString();
        if (paramName.startsWith(PropertiesPanelUIComponent.PERMISSION_PREFIX)) {
          String permKey = paramName.replaceFirst(PropertiesPanelUIComponent.PERMISSION_PREFIX,
              PropertiesPanelUIComponent.EMPTY_STRING);
          StringTokenizer tokenizer = new StringTokenizer(permKey, PropertiesPanelUIComponent.PERMISSION_SEPERATOR);
          String permName = tokenizer.nextToken();
          String perm = tokenizer.nextToken();

          if (permName.equals("Untitled-0")) { //$NON-NLS-1$
            permissionMask.addPermission(((Integer) PentahoAclEntry.getValidPermissionsNameMap().get(perm)).intValue());
          }
        }
      }
      if (file instanceof IAclSolutionFile) {
        repository.addPermission(file, permissionRecipient, permissionMask);
      }
    }
  }

  private Document noPathPage() {
    Document document = DocumentHelper.createDocument();
    document.addElement(PropertiesPanelUIComponent.NO_FILE_PATH_NODE_NAME).addText(
        Messages.getInstance().getString("PropertiesPanelUIComponent.USER_NO_FILE_SELECTED")); //$NON-NLS-1$

    return document;
  }

  private Document setPermissionsFailedPage(final String msg) {
    Document document = DocumentHelper.createDocument();
    document.addElement(PropertiesPanelUIComponent.SET_PERMISSIONS_DENIED_NAME).addText(msg);

    return document;
  }

  private Document noACLSPage() {
    Document document = DocumentHelper.createDocument();
    document.addElement(PropertiesPanelUIComponent.NO_ACLS_NODE_NAME).addText(
        Messages.getInstance().getString("PropertiesPanelUIComponent.ERROR_0001_BAD_CONFIGURATION")); //$NON-NLS-1$

    return document;
  }

  protected Document showInputPage(final ISolutionFile file) {
    Document document = DocumentHelper.createDocument();
    Element root = document.addElement(PropertiesPanelUIComponent.INPUT_PAGE_NODE_NAME).addText(file.getFullPath());

    // Add the info for the file we're working on
    root.addElement(PropertiesPanelUIComponent.FILE_PATH_NODE_NAME).addText(file.getFullPath());
    root.addElement(PropertiesPanelUIComponent.DISPLAY_PATH_NODE_NAME).addText(
        file.getFullPath().replaceFirst(repository.getRepositoryName(), PropertiesPanelUIComponent.EMPTY_STRING)
            .replaceFirst("//", "/")); //$NON-NLS-1$//$NON-NLS-2$
    root.addElement(PropertiesPanelUIComponent.IS_DIR_NODE_NAME).addText(
        file.isDirectory() ? PropertiesPanelUIComponent.TRUE : PropertiesPanelUIComponent.FALSE);
    Element recipients = root.addElement(PropertiesPanelUIComponent.RECIPIENTS_NODE_NAME);

    Iterator iter = null;
    if (includeRoles) {
      // Add all the possible roles
      List rList = getAllRolesList();
      if (rList != null) {
        iter = rList.iterator();
        while (iter.hasNext()) {
          recipients.addElement(PropertiesPanelUIComponent.ROLE_NODE_NAME).addText(iter.next().toString());
        }
      }
    }
    if (includeUsers) {
      // Add all the possible users
      List uList = getAllUsersList();
      if (uList != null) {
        iter = uList.iterator();
        while (iter.hasNext()) {
          recipients.addElement(PropertiesPanelUIComponent.USER_NODE_NAME).addText(iter.next().toString());
        }
      }
    }
    // Add the names of all the permissions
    Map permissionsMap = PentahoAclEntry.getValidPermissionsNameMap();
    // permissionsMap.remove(Messages.getInstance().getString("PentahoAclEntry.USER_SUBSCRIBE")); //$NON-NLS-1$
    Iterator keyIter = permissionsMap.keySet().iterator();
    Element permNames = root.addElement(PropertiesPanelUIComponent.PERMISSION_NAMES_NODE_NAME);
    while (keyIter.hasNext()) {
      permNames.addElement(PropertiesPanelUIComponent.NAME_NODE_NAME).addText(keyIter.next().toString());
    }

    Element acListNode = root.addElement(PropertiesPanelUIComponent.ACCESS_CONTROL_LIST_NODE_NAME);
    TreeMap<IPermissionRecipient, IPermissionMask> sortedMap = new TreeMap<IPermissionRecipient, IPermissionMask>(
        new Comparator<IPermissionRecipient>() {
          public int compare(IPermissionRecipient arg0, IPermissionRecipient arg1) {
            return arg0.getName().compareTo(arg1.getName());
          }
        });
    sortedMap.putAll(repository.getPermissions(file));
    for (Map.Entry<IPermissionRecipient, IPermissionMask> mapEntry : sortedMap.entrySet()) {
      IPermissionRecipient permissionRecipient = mapEntry.getKey();
      Element acNode = acListNode.addElement(PropertiesPanelUIComponent.ACCESS_CONTROL_NODE_NAME);
      Element recipientNode = acNode.addElement(PropertiesPanelUIComponent.RECIPIENT_NODE_NAME);
      recipientNode.setText(permissionRecipient.getName());
      recipientNode.addAttribute(PropertiesPanelUIComponent.TYPE_PARAM,
          (permissionRecipient instanceof SimpleRole) ? PropertiesPanelUIComponent.ROLE_TYPE
              : PropertiesPanelUIComponent.USER_TYPE);
      // Add individual permissions for this group
      for (Iterator keyIterator = permissionsMap.keySet().iterator(); keyIterator.hasNext();) {
        Element aPermission = acNode.addElement(PropertiesPanelUIComponent.PERMISSION_NODE_NAME);
        String permName = keyIterator.next().toString();
        aPermission.addElement(PropertiesPanelUIComponent.NAME_NODE_NAME).setText(permName);
        int permMask = ((Integer) permissionsMap.get(permName)).intValue();
//        boolean isPermitted = repository.hasAccess(permissionRecipient, file, permMask);
//        broken on purpose
        boolean isPermitted = false;
        aPermission.addElement(PropertiesPanelUIComponent.PERMITTED_NODE_NAME).addText(
            isPermitted ? PropertiesPanelUIComponent.TRUE : PropertiesPanelUIComponent.FALSE);
      }
    }
    return document;
  }

  public static void main(String[] args) {
    
  }
  
  public List getAllUsersList() {
    if (allUsersList == null) {
      allUsersList = userRoleListService.getAllUsers();
    }
    return allUsersList;
  }

  public List getAllRolesList() {
    if (allRolesList == null) {
      allRolesList = userRoleListService.getAllRoles();
    }
    return allRolesList;
  }

  @Override
  public Log getLogger() {
    return PropertiesPanelUIComponent.logger;
  }

  @Override
  public boolean validate() {
    return true;
  }
}
