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
package org.pentaho.platform.repository2.unified.jcr;

import java.io.IOException;
import java.io.Serializable;
import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.AccessControlEntry;
import javax.jcr.security.AccessControlList;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.AccessControlPolicy;
import javax.jcr.security.AccessControlPolicyIterator;
import javax.jcr.security.Privilege;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAce;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.repository2.messages.Messages;
import org.pentaho.platform.repository2.unified.IRepositoryFileAclDao;
import org.pentaho.platform.repository2.unified.jcr.IAclMetadataStrategy.AclMetadata;
import org.pentaho.platform.repository2.unified.jcr.jackrabbit.security.SpringSecurityRolePrincipal;
import org.pentaho.platform.repository2.unified.jcr.jackrabbit.security.SpringSecurityUserPrincipal;
import org.springframework.extensions.jcr.JcrCallback;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.util.Assert;

/**
 * Jackrabbit-based implementation of {@link IRepositoryFileAclDao}.
 * <p/>
 * <p>
 * All mutating public methods require checkout and checkin calls since the act of simply calling
 * {@code AccessControlManager.getApplicablePolicies()} (as is done in
 * {@link #toAcl(Session, PentahoJcrConstants, Serializable)}) will query that the node is allowed to have the "access
 * controlled" mixin type added. If the node is checked in, this query will return false. See Jackrabbit's
 * {@code ItemValidator.hasCondition()}.
 * </p>
 *
 * @author mlowery
 */
public class JcrRepositoryFileAclDao implements IRepositoryFileAclDao {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(JcrRepositoryFileAclDao.class);

  // ~ Instance fields =================================================================================================

  private JcrTemplate jcrTemplate;

  private IPermissionConversionHelper permissionConversionHelper;

  private IPathConversionHelper pathConversionHelper;
  
  private ITenantedPrincipleNameResolver tenantedRoleNameUtils;
  
  private ITenantedPrincipleNameResolver tenantedUserNameUtils;
  
  
  // ~ Constructors ====================================================================================================

  public JcrRepositoryFileAclDao(final JcrTemplate jcrTemplate, final IPathConversionHelper pathConversionHelper) {
    this(jcrTemplate, pathConversionHelper, new DefaultPermissionConversionHelper(), null, null);
  }

  public JcrRepositoryFileAclDao(final JcrTemplate jcrTemplate, final IPathConversionHelper pathConversionHelper,final ITenantedPrincipleNameResolver tenantedUserNameUtils , final ITenantedPrincipleNameResolver tenantedRoleNameUtils) {
    this(jcrTemplate, pathConversionHelper, new DefaultPermissionConversionHelper(), tenantedUserNameUtils, tenantedRoleNameUtils);
  }

  public JcrRepositoryFileAclDao(final JcrTemplate jcrTemplate, final IPathConversionHelper pathConversionHelper,
      final IPermissionConversionHelper permissionConversionHelper,final ITenantedPrincipleNameResolver tenantedUserNameUtils, final ITenantedPrincipleNameResolver tenantedRoleNameUtils) {
    super();
    this.jcrTemplate = jcrTemplate;
    this.pathConversionHelper = pathConversionHelper;
    this.permissionConversionHelper = permissionConversionHelper;
    this.tenantedUserNameUtils = tenantedUserNameUtils;
    this.tenantedRoleNameUtils = tenantedRoleNameUtils;
    
  }

  // ~ Methods =========================================================================================================

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  public List<RepositoryFileAce> getEffectiveAces(final Serializable id, final boolean forceEntriesInheriting) {
    return (List<RepositoryFileAce>) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        Node node = session.getNodeByIdentifier(id.toString());

        if (node == null) {
          throw new RepositoryException(Messages.getInstance().getString(
              "JackrabbitRepositoryFileAclDao.ERROR_0001_NODE_NOT_FOUND", id.toString())); //$NON-NLS-1$
        }

        // consult the parent node's effective policy if force is true and parent is not null
        if (forceEntriesInheriting && session.getNodeByIdentifier(id.toString()).getParent() != null) {
          node = node.getParent();
        }

        String absPath = node.getPath();

        AccessControlPolicy[] acPolicies = session.getAccessControlManager().getEffectivePolicies(absPath);
        // logic assumes policies are ordered from leaf to root
        for (AccessControlPolicy policy : acPolicies) {
          Assert.isTrue(policy instanceof AccessControlList);
          AccessControlList acList = ((AccessControlList) policy);
          if (!isEntriesInheriting(session, absPath, acList)) {
            List<RepositoryFileAce> aces = new ArrayList<RepositoryFileAce>();
            AccessControlEntry[] acEntries = acList.getAccessControlEntries();
            List<AccessControlEntry> cleanedAcEntries = JcrRepositoryFileAclUtils.removeAclMetadata(Arrays
                .asList(acEntries));
            for (AccessControlEntry acEntry : cleanedAcEntries) {
              aces.add(toAce(session, acEntry));
            }
            return aces;
          }
        }

        // none are entriesInheriting=false so root aces are the effective aces
        AccessControlList acList = (AccessControlList) acPolicies[acPolicies.length - 1];
        List<RepositoryFileAce> aces = new ArrayList<RepositoryFileAce>();
        AccessControlEntry[] acEntries = acList.getAccessControlEntries();
        List<AccessControlEntry> cleanedAcEntries = JcrRepositoryFileAclUtils.removeAclMetadata(Arrays
            .asList(acEntries));
        for (AccessControlEntry acEntry : cleanedAcEntries) {
          aces.add(toAce(session, acEntry));
        }
        return aces;
      }
    });
  }

  protected String getOwner(final Session session, final String path, final AccessControlList acList)
      throws RepositoryException {
    AclMetadata aclMetadata = JcrRepositoryFileAclUtils.getAclMetadata(session, path, acList);
    if (aclMetadata != null) {
      return aclMetadata.getOwner();
    } else {
      return null;
    }
  }

  protected boolean isEntriesInheriting(final Session session, final String path, final AccessControlList acList)
      throws RepositoryException {
    AclMetadata aclMetadata = JcrRepositoryFileAclUtils.getAclMetadata(session, path, acList);
    if (aclMetadata != null) {
      return aclMetadata.isEntriesInheriting();
    } else {
      return false;
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean hasAccess(final String relPath, final EnumSet<RepositoryFilePermission> permissions) {
    return (Boolean) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        Privilege[] privs = permissionConversionHelper.pentahoPermissionsToPrivileges(session, permissions);
        try {
          String absPath = pathConversionHelper.relToAbs(relPath);
          return session.getAccessControlManager().hasPrivileges(absPath, privs);
        } catch (PathNotFoundException e) {
          // never throw an exception if the path does not exist; just return false
          return false;
        }
      }
    });
  }
  
  private RepositoryFileAcl toAcl(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Serializable id) throws RepositoryException {

    Node node = session.getNodeByIdentifier(id.toString());
    if (node == null) {
      throw new RepositoryException(Messages.getInstance().getString(
          "JackrabbitRepositoryFileAclDao.ERROR_0001_NODE_NOT_FOUND", id.toString())); //$NON-NLS-1$
    }
    String absPath = node.getPath();
    AccessControlManager acMgr = session.getAccessControlManager();
    AccessControlList acList = getAccessControlList(acMgr, absPath);

    RepositoryFileSid owner = null;
    String ownerString = getOwner(session, absPath, acList);

    if (ownerString != null) {
      // for now, just assume all owners are users; only has UI impact
      if(tenantedUserNameUtils != null) {
        ownerString = tenantedUserNameUtils.getPrincipleName(ownerString);
      }
      owner = new RepositoryFileSid(ownerString, RepositoryFileSid.Type.USER);
    }

    RepositoryFileAcl.Builder aclBuilder = new RepositoryFileAcl.Builder(id, owner);

    aclBuilder.entriesInheriting(isEntriesInheriting(session, absPath, acList));

    List<AccessControlEntry> cleanedAcEntries = JcrRepositoryFileAclUtils.removeAclMetadata(Arrays.asList(acList
        .getAccessControlEntries()));

    for (AccessControlEntry acEntry : cleanedAcEntries) {
      aclBuilder.ace(toAce(session, acEntry));
    }
    return aclBuilder.build();

  }

  protected RepositoryFileAce toAce(final Session session, final AccessControlEntry acEntry)
      throws RepositoryException {
    Principal principal = acEntry.getPrincipal();
    RepositoryFileSid sid = null;
    String name = principal.getName();
    if (principal instanceof Group) {
      if(tenantedRoleNameUtils != null) {
        name = tenantedRoleNameUtils.getPrincipleName(name);
      }
      sid = new RepositoryFileSid(name, RepositoryFileSid.Type.ROLE);
    } else {
      if(tenantedUserNameUtils != null) {
        name = tenantedUserNameUtils.getPrincipleName(name);
      }
      sid = new RepositoryFileSid(name, RepositoryFileSid.Type.USER);
    }
    logger.debug(String.format("principal class [%s]", principal.getClass().getName())); //$NON-NLS-1$
    Privilege[] privileges = acEntry.getPrivileges();
    return new RepositoryFileAce(sid, permissionConversionHelper.privilegesToPentahoPermissions(session,
        privileges));
  }

  public void setPermissionConversionHelper(final IPermissionConversionHelper permissionConversionHelper) {
    Assert.notNull(permissionConversionHelper);
    this.permissionConversionHelper = permissionConversionHelper;
  }

  /**
   * Converts between {@code RepositoryFilePermission} and {@code Privilege} instances.
   */
  public static interface IPermissionConversionHelper {
    Privilege[] pentahoPermissionsToPrivileges(final Session session,
        final EnumSet<RepositoryFilePermission> permission) throws RepositoryException;

    EnumSet<RepositoryFilePermission> privilegesToPentahoPermissions(final Session session,
        final Privilege[] privileges) throws RepositoryException;
  }

  public void addAce(final Serializable id, final RepositoryFileSid recipient,
      final EnumSet<RepositoryFilePermission> permission) {
    Assert.notNull(id);
    Assert.notNull(recipient);
    Assert.notNull(permission);
    RepositoryFileAcl acl = getAcl(id);
    Assert.notNull(acl);
    // TODO mlowery find an ACE with the recipient and update that rather than adding a new ACE
    RepositoryFileAcl updatedAcl = new RepositoryFileAcl.Builder(acl).ace(recipient, permission).build();
    updateAcl(updatedAcl);
    logger.debug("added ace: id=" + id + ", sid=" + recipient + ", permission=" + permission); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

  public RepositoryFileAcl createAcl(final Serializable fileId, final RepositoryFileAcl acl) {
    return (RepositoryFileAcl) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        Node node = session.getNodeByIdentifier(fileId.toString());
        String absPath = node.getPath();
        AccessControlManager acMgr = session.getAccessControlManager();
        AccessControlList acList = getAccessControlList(acMgr, absPath);
        acMgr.setPolicy(absPath, acList);
        return internalUpdateAcl(session, pentahoJcrConstants, fileId, acl);
      }
    });
  }

  private AccessControlList getAccessControlList(final AccessControlManager acMgr, final String path)
      throws RepositoryException {
    AccessControlPolicyIterator applicablePolicies = acMgr.getApplicablePolicies(path);
    while (applicablePolicies.hasNext()) {
      AccessControlPolicy policy = applicablePolicies.nextAccessControlPolicy();
      if (policy instanceof AccessControlList) {
        return (AccessControlList) policy;
      }
    }
    AccessControlPolicy[] policies = acMgr.getPolicies(path);
    for (int i = 0; i < policies.length; i++) {
      if (policies[i] instanceof AccessControlList) {
        return (AccessControlList) policies[i];
      }
    }
    throw new IllegalStateException("no access control list applies or is bound to node");
  }

  public RepositoryFileAcl getAcl(final Serializable id) {
    return (RepositoryFileAcl) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        return toAcl(session, pentahoJcrConstants, id);
      }
    });
  }

  protected RepositoryFileAcl getParentAcl(final Serializable id) {
    return (RepositoryFileAcl) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        Node node = session.getNodeByIdentifier(id.toString());
        if (!node.getParent().isSame(session.getRootNode())) {
          return toAcl(session, pentahoJcrConstants, node.getParent().getIdentifier());
        } else {
          return null;
        }
      }
    });
  }

  public void setFullControl(Serializable id, RepositoryFileSid sid, RepositoryFilePermission permission) {
    addAce(id, sid, EnumSet.of(permission));
  }

  public RepositoryFileAcl updateAcl(final RepositoryFileAcl acl) {
    return (RepositoryFileAcl) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary(session, pentahoJcrConstants, acl.getId());
        RepositoryFileAcl updatedAcl = internalUpdateAcl(session, pentahoJcrConstants, acl.getId(), acl);
        JcrRepositoryFileUtils.checkinNearestVersionableFileIfNecessary(session, pentahoJcrConstants, acl.getId(),
            null,
            true);
        return updatedAcl;
      }
    });
  }

  protected RepositoryFileAcl internalUpdateAcl(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Serializable fileId, final RepositoryFileAcl acl) throws RepositoryException {
    Node node = session.getNodeByIdentifier(fileId.toString());
    if (node == null) {
      throw new RepositoryException(Messages.getInstance().getString(
          "JackrabbitRepositoryFileAclDao.ERROR_0001_NODE_NOT_FOUND", fileId.toString())); //$NON-NLS-1$
    }
    String absPath = node.getPath();
    AccessControlManager acMgr = session.getAccessControlManager();
    AccessControlList acList = getAccessControlList(acMgr, absPath);

    // clear all entries
    AccessControlEntry[] acEntries = acList.getAccessControlEntries();
    for (int i = 0; i < acEntries.length; i++) {
      acList.removeAccessControlEntry(acEntries[i]);
    }

    JcrRepositoryFileAclUtils.setAclMetadata(session, absPath, acList, new AclMetadata(acl.getOwner().getName(), acl.isEntriesInheriting()));

    // add entries to now empty list but only if not inheriting; force user to start with clean slate 
    if (!acl.isEntriesInheriting()) {
      for (RepositoryFileAce ace : acl.getAces()) {
        Principal principal = null;
        if (RepositoryFileSid.Type.ROLE == ace.getSid().getType()) {
          principal = new SpringSecurityRolePrincipal(ace.getSid().getName());
        } else {
          principal = new SpringSecurityUserPrincipal(ace.getSid().getName());
        }
        acList.addAccessControlEntry(principal,
            permissionConversionHelper.pentahoPermissionsToPrivileges(session, ace.getPermissions()));
      }
    }
    acMgr.setPolicy(absPath, acList);
    session.save();
    return getAcl(fileId);

  }
  
  public void setTenantedRoleNameUtils(ITenantedPrincipleNameResolver tenantedRoleNameUtils) {
    this.tenantedRoleNameUtils = tenantedRoleNameUtils;
  }

  public ITenantedPrincipleNameResolver getTenantedRoleNameUtils() {
    return tenantedRoleNameUtils;
  }
  
  public ITenantedPrincipleNameResolver getTenantedUserNameUtils() {
    return tenantedUserNameUtils;
  }

  public void setTenantedUserNameUtils(ITenantedPrincipleNameResolver tenantedUserNameUtils) {
    this.tenantedUserNameUtils = tenantedUserNameUtils;
  }

}
