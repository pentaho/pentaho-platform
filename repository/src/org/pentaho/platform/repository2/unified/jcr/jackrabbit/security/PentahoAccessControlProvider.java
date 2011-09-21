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
 */
package org.pentaho.platform.repository2.unified.jcr.jackrabbit.security;

import java.lang.reflect.Constructor;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.api.jsr283.security.AccessControlList;
import org.apache.jackrabbit.api.jsr283.security.AccessControlManager;
import org.apache.jackrabbit.api.jsr283.security.AccessControlPolicy;
import org.apache.jackrabbit.api.jsr283.security.Privilege;
import org.apache.jackrabbit.api.security.principal.PrincipalManager;
import org.apache.jackrabbit.core.ItemImpl;
import org.apache.jackrabbit.core.NodeImpl;
import org.apache.jackrabbit.core.PropertyImpl;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.observation.SynchronousEventListener;
import org.apache.jackrabbit.core.security.SecurityConstants;
import org.apache.jackrabbit.core.security.authorization.AbstractAccessControlProvider;
import org.apache.jackrabbit.core.security.authorization.AbstractCompiledPermissions;
import org.apache.jackrabbit.core.security.authorization.AccessControlConstants;
import org.apache.jackrabbit.core.security.authorization.AccessControlEditor;
import org.apache.jackrabbit.core.security.authorization.AccessControlProvider;
import org.apache.jackrabbit.core.security.authorization.CompiledPermissions;
import org.apache.jackrabbit.core.security.authorization.Permission;
import org.apache.jackrabbit.core.security.principal.PrincipalImpl;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.Path;
import org.apache.jackrabbit.spi.commons.name.NameFactoryImpl;
import org.apache.jackrabbit.spi.commons.name.PathFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copy of {@code org.apache.jackrabbit.core.security.authorization.acl.ACLProvider} from Jackrabbit 1.6.0 with 
 * modifications.
 * 
 * <p>
 * To enable this class, edit {@code workspace.xml} and nest the following elements inside the {@code Workspace} 
 * element:
 * </p>
 * 
 * <pre>
 * {@code
 * <WorkspaceSecurity>
 *   <AccessControlProvider class="org.pentaho.platform.repository2.pur.jcr.jackrabbit.security.PentahoAccessControlProvider" />
 * </WorkspaceSecurity>
 * }
 * </pre>
 * 
 * Supported params: adminRole, helperClass, wildcardDynamicMaskN, dynamicMaskN.
 * </p>
 * 
 * <p>Modifications</p>
 * <ul>
 * <li>
 * Assumption that only one ACL for a given path affects the access control decision.
 *   <ul>
 *   <li>
 *   No readAllowed.
 *   </li>
 *   <li>
 *   No collectAcls.
 *   </li>
 *   </ul>
 * </li>
 * <li>
 * {@code initRootAcl}
 *   <ul>
 *   <li>
 *   ACE in root ACL refers to admin role instead of admin user.
 *   </li>
 *   <li>
 *   Root node is referenceable.
 *   </li>
 *   <li>
 *   Root ACL is a Pentaho ACL (with owner and isEntriesInheriting flag).
 *   </li>
 *   </ul> 
 * </li>
 * <li>
 * {@code compilePermissions}
 *   <ul>
 *   <li>
 *   Delegates to helper.
 *   </li>
 *   </ul>
 * </li>
 * <li>
 * {@code getEffectivePolicies}
 *   <ul>
 *   <li>
 *   Delegates to helper.
 *   </li>
 *   </ul>
 * </li>
 * <li>
 * No support for DENY ACEs.
 * </li>
 * <li>
 * Log debugs changed to traces.
 * </li>
 * </ul>
 * 
 * @author mlowery
 */
@SuppressWarnings("nls")
public class PentahoAccessControlProvider extends AbstractAccessControlProvider implements AccessControlConstants {

  protected static final Name PHO_NT_PENTAHOACL = NameFactoryImpl.getInstance().create(
      "http://www.pentaho.org/jcr/nt/1.0", "pentahoAcl");

  /**
   * Must match adminRole specified in PrincipalProvider.
   */
  public static final String PARAM_ADMIN_ROLE = "adminRole";

  /**
   * Fully-qualified class name of IPentahoJackrabbitAccessControlHelper implementation.
   */
  public static final String PARAM_HELPER_CLASS = "helperClass";
  
  private IPentahoJackrabbitAccessControlHelper helper;
  
  /**
   * Role (aka group) principal belonging to all repository super-users.
   */
  private String adminRole;

  /**
   * the default logger
   */
  private static final Logger log = LoggerFactory.getLogger(PentahoAccessControlProvider.class);

  /**
   * the system acl editor.
   */
  protected PentahoAccessControlEditor systemEditor;

  /**
   * Flag indicating whether or not this provider should be create the default
   * ACLs upon initialization.
   */
  private boolean initializedWithDefaults;

  //-------------------------------------------------< AccessControlUtils >---
  /**
   * @see AbstractAccessControlProvider#isAcItem(Path)
   */
  public boolean isAcItem(Path absPath) throws RepositoryException {
    Path.Element[] elems = absPath.getElements();
    for (int i = 0; i < elems.length; i++) {
      if (N_POLICY.equals(elems[i].getName())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Test if the given node is itself a rep:ACL or a rep:ACE node.
   * @see AbstractAccessControlProvider#isAcItem(ItemImpl)
   */
  public boolean isAcItem(ItemImpl item) throws RepositoryException {
    NodeImpl n = ((item.isNode()) ? (NodeImpl) item : (NodeImpl) item.getParent());
    return n.isNodeType(NT_REP_ACL) || n.isNodeType(NT_REP_ACE);
  }

  //----------------------------------------------< AccessControlProvider >---
  /**
   * @see AccessControlProvider#init(Session, Map)
   */
  public void init(Session systemSession, Map configuration) throws RepositoryException {
    super.init(systemSession, configuration);

    // make sure the workspace of the given systemSession has a
    // minimal protection on the root node.
    NodeImpl root = (NodeImpl) session.getRootNode();
    systemEditor = new PentahoAccessControlEditor(systemSession, this);
    initializedWithDefaults = !configuration.containsKey(PARAM_OMIT_DEFAULT_PERMISSIONS);
    pentahoInit(systemSession, configuration);
    if (initializedWithDefaults && !isAccessControlled(root)) {
      initRootACL(session, systemEditor);
    }
  }

  private void pentahoInit(Session systemSession, Map configuration) throws RepositoryException {
    String helperClassString = (String) configuration.get(PARAM_HELPER_CLASS);
    if (helperClassString == null) {
      if (log.isTraceEnabled()) {
        log.trace("no param named " + PARAM_HELPER_CLASS + " found; using default");
      }
      helperClassString = "org.pentaho.platform.repository2.pur.jcr.jackrabbit.security.DefaultPentahoJackrabbitAccessControlHelper";
    } else {
      if (log.isTraceEnabled()) {
        log.trace("found " + PARAM_HELPER_CLASS + "=" + helperClassString);
      }
    }
    try {
      Class<?> helperClass = Class.forName(helperClassString);
      Constructor<?> ct = helperClass.getConstructor();
      helper = (IPentahoJackrabbitAccessControlHelper) ct.newInstance();
      helper.init(configuration);
    } catch (Exception e) {
      throw new RepositoryException(e);
    }
    adminRole = (String) configuration.get(PARAM_ADMIN_ROLE);
    if (adminRole == null) {
      adminRole = SecurityConstants.ADMINISTRATORS_NAME;
    }

  }
  
  /**
   * @see AccessControlProvider#getEffectivePolicies(Path)
   * @param absPath
   */
  public AccessControlPolicy[] getEffectivePolicies(Path absPath) throws ItemNotFoundException, RepositoryException {
    checkInitialized();

    String jcrPath = resolver.getJCRPath(absPath);
    NodeImpl aclNode = helper.getEffectiveAclNode(session, systemEditor, jcrPath);
    AccessControlList acl = systemEditor.getACL(aclNode);
    return new AccessControlPolicy[] { acl };
  }

  /**
   * @see AccessControlProvider#getEditor(Session)
   */
  public AccessControlEditor getEditor(Session session) {
    checkInitialized();
    return new PentahoAccessControlEditor(session, this);
  }

  /**
   * @see AccessControlProvider#compilePermissions(Set)
   */
  public CompiledPermissions compilePermissions(Set principals) throws RepositoryException {
    checkInitialized();
    if (isAdminOrSystem(principals)) {
      return getAdminPermissions();
    } else if (isReadOnly(principals)) {
      return getReadOnlyPermissions();
    } else {
      return new AclPermissions(principals);
    }
  }

  /**
   * @see AccessControlProvider#canAccessRoot(Set)
   */
  public boolean canAccessRoot(Set principals) throws RepositoryException {
    checkInitialized();
    if (isAdminOrSystem(principals)) {
      return true;
    } else {
      CompiledPermissions cp = new AclPermissions(principals, false);
      return cp.grants(PathFactoryImpl.getInstance().getRootPath(), Permission.READ);
    }
  }

  //------------------------------------------------------------< private >---

  /**
   * Set-up minimal permissions for the workspace:
   *
   * <ul>
   * <li>adminstrators principal -> all privileges</li>
   * <li>everybody -> read privilege</li>
   * </ul>
   *
   * @param session to the workspace to set-up inital ACL to
   * @param editor for the specified session.
   * @throws RepositoryException If an error occurs.
   */
  private void initRootACL(SessionImpl session, AccessControlEditor editor) throws RepositoryException {
    try {
      // mlowery not security related but it's convenient
      if (log.isTraceEnabled()) {
        log.trace("making root node referenceable...");
      }
      session.getRootNode().addMixin(JcrConstants.MIX_REFERENCEABLE);

      if (log.isTraceEnabled()) {
        log.trace("Install initial ACL:...");
      }
      String rootPath = session.getRootNode().getPath();
      AccessControlPolicy[] acls = editor.editAccessControlPolicies(rootPath);
      PentahoJackrabbitAccessControlList acl = (PentahoJackrabbitAccessControlList) acls[0];

      PrincipalManager pMgr = session.getPrincipalManager();
      AccessControlManager acMgr = session.getAccessControlManager();

      if (log.isTraceEnabled()) {
        log.trace("... Privilege.ALL for administrators.");
      }
      Principal administrators;
      if (pMgr.hasPrincipal(adminRole)) {
        administrators = pMgr.getPrincipal(adminRole);
      } else {
        if (log.isWarnEnabled()) {
          log.warn("Administrators principal group [" + adminRole + "] is missing.");
        }
        administrators = new PrincipalImpl(adminRole);
      }
      Privilege[] privs = new Privilege[] { acMgr.privilegeFromName(Privilege.JCR_ALL) };
      acl.addAccessControlEntry(administrators, privs);

      Principal everyone = pMgr.getEveryone();
      if (log.isTraceEnabled()) {
        log.trace("... Privilege.READ for everyone.");
      }
      privs = new Privilege[] { acMgr.privilegeFromName(Privilege.JCR_READ),
          acMgr.privilegeFromName(Privilege.JCR_READ_ACCESS_CONTROL) };
      acl.addAccessControlEntry(everyone, privs);

      acl.setEntriesInheriting(false);
      acl.setOwner(administrators);

      editor.setPolicy(rootPath, acl);
      session.save();

    } catch (RepositoryException e) {
      if (log.isErrorEnabled()) {
        log.error("Failed to set-up minimal access control for root node of workspace "
            + session.getWorkspace().getName(), e);
      }
      session.getRootNode().refresh(false);
    }
  }

  /**
   * Test if the given node is access controlled. The node is access
   * controlled if it is of nodetype
   * {@link AccessControlConstants#NT_REP_ACCESS_CONTROLLABLE "rep:AccessControllable"}
   * and if it has a child node named
   * {@link AccessControlConstants#N_POLICY "rep:ACL"}.
   *
   * @param node
   * @return <code>true</code> if the node is access controlled;
   *         <code>false</code> otherwise.
   * @throws RepositoryException
   */
  static boolean isAccessControlled(NodeImpl node) throws RepositoryException {
    return node.isNodeType(NT_REP_ACCESS_CONTROLLABLE) && node.hasNode(N_POLICY);
  }

  //------------------------------------------------< CompiledPermissions >---
  /**
   *
   */
  protected class AclPermissions extends AbstractCompiledPermissions implements SynchronousEventListener {

    private final List<String> principalNames;

    private AclPermissions(Set principals) throws RepositoryException {
      this(principals, true);
    }

    private AclPermissions(Set principals, boolean listenToEvents) throws RepositoryException {
      principalNames = new ArrayList<String>(principals.size());
      for (Iterator it = principals.iterator(); it.hasNext();) {
        principalNames.add(((Principal) it.next()).getName());
      }

      if (listenToEvents && isPentahoNodeTypesRegistered()) {
        /*
         Make sure this AclPermission recalculates the permissions if
         any ACL concerning it is modified. interesting events are:
         - new ACE-entry for any of the principals (NODE_ADDED)
         - changing ACE-entry for any of the principals (PROPERTY_CHANGED)
           > new permissions granted/denied
           >
         - removed ACE-entry for any of the principals (NODE_REMOVED)
        */
        int events = Event.PROPERTY_CHANGED | Event.NODE_ADDED | Event.NODE_REMOVED;
        String[] ntNames = new String[] { session.getJCRName(AccessControlConstants.NT_REP_ACE),
            session.getJCRName(PHO_NT_PENTAHOACL) };
        session.getWorkspace().getObservationManager().addEventListener(this, events, session.getRootNode().getPath(),
            true, null, ntNames, true);
      }
    }

    /**
     * Returns true if custom Pentaho node types have been registered yet. In the early sessions, namespaces and node
     * types aren't yet registered. (The act of registering a namespace and node type requires a session.)
     */
    private boolean isPentahoNodeTypesRegistered() {
      try {
        session.getNodeTypeManager().getNodeType(PHO_NT_PENTAHOACL);
        return true;
      } catch (NoSuchNodeTypeException e) {
        return false;
      }
    }

    //------------------------------------< AbstractCompiledPermissions >---

    protected Result buildResult(Path absPath) throws RepositoryException {
      int mask = helper.buildMask(absPath, session, principalNames, resolver, systemEditor);
      // third arg is for when callers call Result.getPrivileges() as is done in 
      // AbstractCompiledPermissions.getPrivileges()
      return new Result(mask, 0, mask, 0);
    }

    //--------------------------------------------< CompiledPermissions >---
    /**
     * @see CompiledPermissions#close()
     */
    public void close() {
      try {
        observationMgr.removeEventListener(this);
      } catch (RepositoryException e) {
        if (log.isTraceEnabled()) {
          log.trace("Unable to unregister listener: ", e.getMessage());
        }
      }
      super.close();
    }

    /**
     * Override to print some trace statements.
     */
    @Override
    public boolean grants(Path absPath, int permissions) throws RepositoryException {
      String jcrPath = resolver.getJCRPath(absPath);
      if (log.isTraceEnabled()) {
        log.trace("processing access control query for jcrPath=" + jcrPath + " and perm bits=" + permissions
            + " for principals=" + principalNames);
      }
      return super.grants(absPath, permissions);
    }

    //--------------------------------------------------< EventListener >---
    /**
     * @see EventListener#onEvent(EventIterator)
     */
    public synchronized void onEvent(EventIterator events) {
      // only invalidate cache if any of the events affects the
      // nodes defining permissions for principals compiled here.
      boolean clearCache = false;
      while (events.hasNext() && !clearCache) {
        try {
          Event ev = events.nextEvent();
          String path = ev.getPath();
          switch (ev.getType()) {
            case Event.NODE_ADDED:
              // test if the new node is an ACE node that affects
              // the permission of any of the principals listed in
              // principalNames.
              NodeImpl n = (NodeImpl) session.getNode(path);
              if (n.isNodeType(AccessControlConstants.NT_REP_ACE)
                  && principalNames.contains(n.getProperty(AccessControlConstants.P_PRINCIPAL_NAME).getString())) {
                clearCache = true;
              }
              break;
            case Event.PROPERTY_REMOVED:
            case Event.NODE_REMOVED:
              // can't find out if the removed ACL/ACE node was
              // relevant for the principals
              clearCache = true;
              break;
            case Event.PROPERTY_ADDED:
            case Event.PROPERTY_CHANGED:
              // test if the added/changed prop belongs to an ACe
              // node and affects the permission of any of the
              // principals listed in principalNames.
              PropertyImpl p = (PropertyImpl) session.getProperty(path);
              NodeImpl parent = (NodeImpl) p.getParent();
              if (parent.isNodeType(AccessControlConstants.NT_REP_ACE)) {
                String principalName = null;
                if (AccessControlConstants.P_PRIVILEGES.equals(p.getQName())) {
                  // test if principal-name sibling-prop matches
                  principalName = parent.getProperty(AccessControlConstants.P_PRINCIPAL_NAME).getString();
                } else if (AccessControlConstants.P_PRINCIPAL_NAME.equals(p.getQName())) {
                  // a new ace or an ace change its principal-name.
                  principalName = p.getString();
                }
                if (principalName != null && principalNames.contains(principalName)) {
                  clearCache = true;
                }
              }
              if (parent.isNodeType(PHO_NT_PENTAHOACL)) {
                // acl owner or inheriting flag changed
                clearCache = true;
              }
              break;
            default:
              // illegal event-type: should never occur. ignore
          }
        } catch (RepositoryException e) {
          // should not get here
          log.warn("Internal error: ", e.getMessage());
        }
      }
      if (clearCache) {
        clearCache();
      }
    }

  }

}
