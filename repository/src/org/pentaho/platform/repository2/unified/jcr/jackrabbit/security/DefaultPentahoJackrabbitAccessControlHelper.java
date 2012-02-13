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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.version.VersionHistory;

import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.api.jsr283.security.AccessControlEntry;
import org.apache.jackrabbit.core.NodeImpl;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.security.authorization.AccessControlConstants;
import org.apache.jackrabbit.core.security.authorization.AccessControlEntryImpl;
import org.apache.jackrabbit.core.security.authorization.Permission;
import org.apache.jackrabbit.core.security.principal.EveryonePrincipal;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.Path;
import org.apache.jackrabbit.spi.commons.conversion.NamePathResolver;
import org.apache.jackrabbit.spi.commons.name.NameFactoryImpl;
import org.apache.jackrabbit.util.Text;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.TenantUtils;
import org.pentaho.platform.repository2.unified.jcr.jackrabbit.security.messages.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;


/**
 * Default {@code IPentahoJackrabbitAccessControlHelper} implementation.
 *
 * <p>To use this class, insert the following node into {@code /Repository/Workspace/WorkspaceSecurity} in 
 * {@code repository.xml}:</p>
 * <pre>
 * &lt;AccessControlProvider class="org.pentaho.platform.repository2.pur.jcr.jackrabbit.security.PentahoAccessControlProvider">
 *   &lt;!-- some params ommitted for brevity -->
 *   &lt;param name="helperClass" value="org.pentaho.platform.repository2.pur.jcr.jackrabbit.security.DefaultPentahoJackrabbitAccessControlHelper" />
 *   &lt;param name="wildcardDynamicMask0" value="/pentaho/{0}=org.pentaho.security.administerSecurity:4095" />
 *   &lt;param name="dynamicMask0" value="/pentaho/{0}=org.pentaho.repository.read:33" />
 *   &lt;param name="dynamicMask1" value="/pentaho/{0}/etc/pdi=org.pentaho.repository.read:33" />
 *   &lt;param name="dynamicMask2" value="/pentaho/{0}/etc/pdi=org.pentaho.repository.create:1023" />
 * &lt;/AccessControlProvider> 
 * </pre>
 * 
 * <p>
 * There are two types of dynamic mask rules: wildcard and non-wildcard. Wildcard rules have the prefix 
 * "wildcardDynamicMask" and non-wildcard rules have the prefix "dynamicMask." To add a new rule, create a param with 
 * a name consisting of the appropriate prefix and the next consecutive available integer for the type of rule with 
 * which you are working. The value attributes have the form,
 * </p>
 * <pre>path=action:bits</pre>
 * <p>
 * where path can contain a placeholder {0} which will be substituted with the current tenant before comparison,
 * action is the name of an action (a.k.a. task), and bits are an ORing of 
 * {@code org.apache.jackrabbit.core.security.authorization.Permission} constants.
 * </p>
 * 
 * <p>
 * Wildcard rules are processed first and if there is one or more rules matched, the bits will be ORed together and 
 * returned with no further processing. Next are the non-wildcard rules that are processed the same.
 * </p>
 * 
 * <p>
 * <strong>Note: It is imperative that entriesInheriting be set to false on the appropriate ACLs so that the algorithm
 * uses the appropriate effective ACL node. The path of this effective ACL node is the path that is used for dynamic
 * mask (of either kind) comparison. See {@code DefaultBackingRepositoryLifecycleManager}. (It is the class which sets
 * entriesInheriting on default folders.</strong>
 * </p>
 * 
 * @author mlowery
 */
public class DefaultPentahoJackrabbitAccessControlHelper implements IPentahoJackrabbitAccessControlHelper {

  private static final Logger log = LoggerFactory.getLogger(DefaultPentahoJackrabbitAccessControlHelper.class);

  protected static final Name PHO_NT_PENTAHOACL = NameFactoryImpl.getInstance().create(
      "http://www.pentaho.org/jcr/nt/1.0", "pentahoAcl"); //$NON-NLS-1$ //$NON-NLS-2$

  protected static final Name PHO_NT_PENTAHOHIERARCHYNODE = NameFactoryImpl.getInstance().create(
      "http://www.pentaho.org/jcr/nt/1.0", "pentahoHierarchyNode"); //$NON-NLS-1$//$NON-NLS-2$

  protected static final Name PHO_ACLOWNERNAME = NameFactoryImpl.getInstance().create("http://www.pentaho.org/jcr/1.0", //$NON-NLS-1$
      "aclOwnerName"); //$NON-NLS-1$

  protected List<PathActionMask> wildcardDynamicMaskList = new ArrayList<PathActionMask>();

  protected List<PathActionMask> dynamicMaskList = new ArrayList<PathActionMask>();

  public void init(final Map configuration) {
    populateWildcardDynamicMaskList(configuration);
    populateDynamicMaskList(configuration);
  }

  protected void populateDynamicMaskList(final Map configuration) {
    for (int i = 0;; i++) {
      String value = (String) configuration.get("dynamicMask" + i); //$NON-NLS-1$
      if (value == null) {
        break;
      }
      PathActionMask pam = getPathActionMask(value);
      dynamicMaskList.add(pam);
    }
  }

  protected void populateWildcardDynamicMaskList(final Map configuration) {
    for (int i = 0;; i++) {
      String value = (String) configuration.get("wildcardDynamicMask" + i); //$NON-NLS-1$
      if (value == null) {
        break;
      }
      PathActionMask pam = getPathActionMask(value);
      wildcardDynamicMaskList.add(pam);
    }
  }

  protected PathActionMask getPathActionMask(final String value) {
    String[] tokens = value.split("="); //$NON-NLS-1$
    Assert.isTrue(tokens.length == 2);
    Assert.isTrue(StringUtils.hasLength(tokens[0]));
    Assert.isTrue(StringUtils.hasLength(tokens[1]));

    String[] subTokens = tokens[1].split(":"); //$NON-NLS-1$
    Assert.isTrue(subTokens.length == 2);
    Assert.isTrue(StringUtils.hasLength(subTokens[0]));
    Assert.isTrue(StringUtils.hasLength(subTokens[1]));
    return new PathActionMask(tokens[0], subTokens[0], Integer.parseInt(subTokens[1]));
  }

  protected static class PathActionMask {
    public String path;

    public String action;

    public int mask;

    public PathActionMask(String path, String action, int mask) {
      super();
      this.path = path;
      this.action = action;
      this.mask = mask;
    }

    @Override
    public String toString() {
      return "path=" + path + ", action=" + action + ", mask=" + mask; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
    }

  }

  /**
   * <p>
   * This algorithm is content-aware. In other words, it has knowledge of the types of nodes going into the 
   * repository. It understands nodes of type nt:folder, nt:file, and nt:linkedFile. It enforces access control by 
   * finding the nearest enclosing persisted nt:folder and uses the ACL for that node as the starting point. Why is it 
   * just a starting point and not the end? Because the nearest enclosing persisted node might not have any ACEs of 
   * its own, forcing us to traverse the tree heading for the root until we either hit the root or hit an nt:folder
   * that has a non-empty ACL. This is the ACL (and only this ACL) that is consulted to build the mask.
   * </p>
   * 
   * <p>Special behavior when dealing with version storage</p>
   * <p>
   * Sometimes the {@code absPath} starts with {@code /jcr:system/jcr:versionStorage}. In this case
   * a caller is attempting to do something involving versioning (e.g. applying a label to a version). Nodes in this
   * are not part of the nt:folder structure discussed above. In other words, we will never find an enclosing 
   * persisted folder. We will always hit the root--which has its own ACL which we do not want to use as it allows 
   * Permission.READ for all. So what do we do?
   * </p> 
   * 
   * <p>
   * We first see if
   * a persistent version history node is part of the {@code absPath}. If the version history is persistent, then we
   * find the node with which the version history is associated and use its ACL to build the result. Sometimes the
   * version history node is not yet persistent. This happens because the user hasn't committed the transaction in 
   * which a {@code checkin} takes place. In this case, we give READ and VERSION_MNGMT in the result. Why? Because the user
   * has already successfully checked in--and the Permission.VERSION_MNGMT was already checked on that node--why 
   * should we stop them now? What about the justification for providing read access? Well, if the caller is asking
   * to read a node that has not yet been persisted, then that node was created in the that user's session to begin
   * with. He should have READ access to a node that he created but has not yet persisted. Giving a session READ access
   * to a file that is transient is only useful to the session that created the node (but has not yet persisted the 
   * node).
   * </p>
   */
  public int buildMask(final Path absPath, final SessionImpl session, final List<String> principalNames,
      final NamePathResolver resolver, final PentahoAccessControlEditor systemEditor) throws RepositoryException {
    String jcrPath = resolver.getJCRPath(absPath);
    if (log.isTraceEnabled()) {
      log.trace("building result for jcrPath=" + jcrPath); //$NON-NLS-1$
    }

    NodeImpl aclNode = getEffectiveAclNode(session, systemEditor, jcrPath);

    // version history node is not yet persistent
    if (aclNode == null) {
      return Permission.READ | Permission.VERSION_MNGMT;
    }

    // special handling for owners; owners can do whatever they want with object at jcrPath
    if (aclNode.isNodeType(PHO_NT_PENTAHOACL)) {
      String aclOwnerName = aclNode.getProperty(PHO_ACLOWNERNAME).getString();
      if (principalNames.contains(aclOwnerName)) {
        if (log.isTraceEnabled()) {
          log.trace("current user is owner of " + jcrPath); //$NON-NLS-1$
        }
        return Permission.ALL;
      }
    }

    // path of parent of ACL node is the path we want to match 
    String aclNodeParentPath = aclNode.getParent().getPath();

    // handle wildcards
    int wildcardDynamicMask = 0;
    boolean wildcardDynamicMaskFoundMatch = false;
    for (PathActionMask pam : wildcardDynamicMaskList) {
      String substitutedPath = MessageFormat.format(pam.path, TenantUtils.getTenantId());
      if (aclNodeParentPath.equals(substitutedPath) || aclNodeParentPath.startsWith(substitutedPath + "/")) { //$NON-NLS-1$
        if (getAuthorizationPolicy().isAllowed(pam.action)) {
          if (log.isTraceEnabled()) {
            log.trace("wildcard dynamic mask in effect: " + pam); //$NON-NLS-1$
          }
          wildcardDynamicMaskFoundMatch = true;
          wildcardDynamicMask |= pam.mask;
        }
      }
    }
    if (wildcardDynamicMaskFoundMatch) {
      return wildcardDynamicMask;
    }

    // dynamic ACEs
    int dynamicMask = 0;
    boolean dynamicMaskFoundMatch = false;
    for (PathActionMask pam : dynamicMaskList) {
      String substitutedPath = MessageFormat.format(pam.path, TenantUtils.getTenantId());
      if (aclNodeParentPath.equals(substitutedPath)) {
        if (getAuthorizationPolicy().isAllowed(pam.action)) {
          if (log.isTraceEnabled()) {
            log.trace("dynamic mask in effect: " + pam); //$NON-NLS-1$
          }
          dynamicMaskFoundMatch = true;
          dynamicMask |= pam.mask;
        }
      }
    }
    if (dynamicMaskFoundMatch) {
      return dynamicMask;
    }

    List<AccessControlEntry> aces = Arrays.asList(systemEditor.getACL(aclNode).getAccessControlEntries());

    if (log.isTraceEnabled()) {
      log.trace("building result using ACEs: " + aces.toString()); //$NON-NLS-1$
    }

    int mask = Permission.NONE;

    for (AccessControlEntry ace : aces) {
      Assert.isInstanceOf(AccessControlEntryImpl.class, ace);
      AccessControlEntryImpl jrAce = (AccessControlEntryImpl) ace;
      // if either there is an exact match on principal name or the ace uses the "everyone" principal
      if (principalNames.contains(ace.getPrincipal().getName())
          || EveryonePrincipal.getInstance().getName().equals(ace.getPrincipal().getName())) {
        if (jrAce.isAllow()) {
          mask |= jrAce.getPrivilegeBits();
        }
      }
    }

    if (log.isTraceEnabled()) {
      log.trace("mask=" + mask); //$NON-NLS-1$
    }
    return mask;

  }

  /**
   * Gets the node whose ACL contains the ACEs that will be used for the access control decision.
   */
  public NodeImpl getEffectiveAclNode(final SessionImpl session, final PentahoAccessControlEditor systemEditor,
      final String jcrPath) throws RepositoryException {
    NodeImpl nearestPersistedNode = findNearestPersistedNode(session, jcrPath);

    NodeImpl startingNode = null;

    if (jcrPath.startsWith("/jcr:system/jcr:versionStorage")) { //$NON-NLS-1$
      VersionHistory versionHistory = findVersionHistoryNode(session, nearestPersistedNode);
      if (versionHistory == null) {
        // version history node is not yet persistent
        return null;
      } else {
        startingNode = (NodeImpl) session.getNodeByUUID(versionHistory.getVersionableUUID());
      }
    } else {
      startingNode = nearestPersistedNode;
    }

    NodeImpl folderOrFileOrLinkedFileOrRootNode = findFolderOrFileOrLinkedFileOrRootNode(session, startingNode);

    if (folderOrFileOrLinkedFileOrRootNode == null) {
      // should never get here
      throw new ItemNotFoundException(Messages.getInstance().getString(
          "DefaultPentahoJackrabbitAccessControlHelper.ERROR_0001_ITEM_NOT_FOUND")); //$NON-NLS-1$
    }

    NodeImpl nodeWithAclToUse = folderOrFileOrLinkedFileOrRootNode;

    // now we have a folder, file, or linkedFile node whose ACL will serve as the starting ACL
    NodeImpl aclNode;
    aclNode = nodeWithAclToUse.getNode(AccessControlConstants.N_POLICY);
    PentahoJackrabbitAccessControlList acList = (PentahoJackrabbitAccessControlList) systemEditor.getACL(aclNode);
    while (acList.isEntriesInheriting() && !isRootNode(session, nodeWithAclToUse)) {
      // find nearest enclosing file, folder, linkedFile, or root
      nodeWithAclToUse = findFolderOrFileOrLinkedFileOrRootNode(session, (NodeImpl) nodeWithAclToUse.getParent());
      aclNode = nodeWithAclToUse.getNode(AccessControlConstants.N_POLICY);
      acList = (PentahoJackrabbitAccessControlList) systemEditor.getACL(aclNode);
    }

    List<AccessControlEntry> aces = Arrays.asList(acList.getAccessControlEntries());

    // if hit root and it has no ACEs, that is a problem as AbstractPentahoAccessControlProvider.initRootACL should
    // have written some ACEs
    if (aces.isEmpty() && isRootNode(session, nodeWithAclToUse)) {
      throw new RepositoryException(Messages.getInstance().getString(
          "DefaultPentahoJackrabbitAccessControlHelper.ERROR_0002_NO_ROOT_NODE_ACES")); //$NON-NLS-1$
    }
    return aclNode;
  }

  protected boolean isPentahoHierarchyNode(final Node node) throws RepositoryException {
    return node.isNodeType(PHO_NT_PENTAHOHIERARCHYNODE.toString());
  }

  protected NodeImpl findNearestPersistedNode(final SessionImpl session, final String jcrPath)
      throws RepositoryException {
    NodeImpl nearestPersistedNode = null;

    // set node to node at jcrPath or if not yet persisted, set node to nearest persisted node
    if (session.nodeExists(jcrPath)) {
      nearestPersistedNode = (NodeImpl) session.getNode(jcrPath);
    } else {
      // path points non-existing node or property; find the nearest persisted node
      String parentPath = Text.getRelativeParent(jcrPath, 1);
      while (parentPath.length() > 0) {
        if (session.nodeExists(parentPath)) {
          nearestPersistedNode = (NodeImpl) session.getNode(parentPath);
          break;
        }
        parentPath = Text.getRelativeParent(parentPath, 1);
      }
    }
    return nearestPersistedNode;
  }

  protected boolean isRootNode(final SessionImpl session, final NodeImpl node) throws RepositoryException {
    return node.getId().equals(((NodeImpl) session.getRootNode()).getNodeId());
  }

  protected boolean isVersionHistory(final NodeImpl node) throws RepositoryException {
    return node.isNodeType(JcrConstants.NT_VERSIONHISTORY);
  }

  protected VersionHistory findVersionHistoryNode(final SessionImpl session, final NodeImpl node)
      throws RepositoryException {
    // also, if incoming path involves version history, find the versionHistory node
    NodeImpl currentNode = node;
    while (!isVersionHistory(currentNode) && !isRootNode(session, currentNode)) {
      currentNode = (NodeImpl) currentNode.getParent();
    }
    if (isRootNode(session, currentNode)) {
      return null;
    } else {
      return (VersionHistory) currentNode;
    }
  }

  protected NodeImpl findFolderOrFileOrLinkedFileOrRootNode(final SessionImpl session, final NodeImpl node)
      throws RepositoryException {
    // now we have a node; it may not be a folder, file, or linkedFile node so find the nearest enclosing folder,
    // file, or linkedFile node; that will be the ACL that we start with; also, stop if we hit the root node;
    NodeImpl currentNode = node;
    while (!isVersionHistory(currentNode) && !isPentahoHierarchyNode(currentNode) && !isRootNode(session, currentNode)) {
      currentNode = (NodeImpl) currentNode.getParent();
    }
    return currentNode;
  }

  protected IAuthorizationPolicy getAuthorizationPolicy() {
    IAuthorizationPolicy authorizationPolicy = PentahoSystem.get(IAuthorizationPolicy.class);
    Assert.state(authorizationPolicy != null);
    return authorizationPolicy;
  }

}
