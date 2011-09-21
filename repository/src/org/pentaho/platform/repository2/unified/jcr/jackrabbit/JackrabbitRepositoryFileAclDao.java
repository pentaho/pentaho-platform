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
package org.pentaho.platform.repository2.unified.jcr.jackrabbit;

import java.io.IOException;
import java.io.Serializable;
import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.api.jsr283.security.AccessControlEntry;
import org.apache.jackrabbit.api.jsr283.security.AccessControlManager;
import org.apache.jackrabbit.api.jsr283.security.AccessControlPolicy;
import org.apache.jackrabbit.api.jsr283.security.AccessControlPolicyIterator;
import org.apache.jackrabbit.api.jsr283.security.Privilege;
import org.apache.jackrabbit.api.security.principal.NoSuchPrincipalException;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.security.UserPrincipal;
import org.apache.jackrabbit.core.security.authorization.JackrabbitAccessControlEntry;
import org.apache.jackrabbit.core.security.authorization.JackrabbitAccessControlList;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAce;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.repository2.messages.Messages;
import org.pentaho.platform.repository2.unified.IRepositoryFileAclDao;
import org.pentaho.platform.repository2.unified.jcr.IPathConversionHelper;
import org.pentaho.platform.repository2.unified.jcr.PentahoJcrConstants;
import org.pentaho.platform.repository2.unified.jcr.jackrabbit.security.IPentahoJackrabbitAccessControlList;
import org.pentaho.platform.repository2.unified.jcr.jackrabbit.security.SpringSecurityRolePrincipal;
import org.springframework.extensions.jcr.JcrCallback;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.util.Assert;

/**
 * Jackrabbit-based implementation of {@link IRepositoryFileAclDao}.
 * 
 * <p>
 * All mutating public methods require checkout and checkin calls since the act of simply calling 
 * {@code AccessControlManager.getApplicablePolicies()} (as is done in 
 * {@link #toAcl(SessionImpl, Serializable, boolean)}) will query that the node is allowed to have the "access 
 * controlled" mixin type added. If the node is checked in, this query will return false. See Jackrabbit's 
 * {@code ItemValidator.hasCondition()}.
 * </p>
 * 
 * @author mlowery
 */
public class JackrabbitRepositoryFileAclDao implements IRepositoryFileAclDao {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(JackrabbitRepositoryFileAclDao.class);

  // ~ Instance fields =================================================================================================

  private JcrTemplate jcrTemplate;

  private IPermissionConversionHelper permissionConversionHelper = new DefaultPermissionConversionHelper();

  private IPathConversionHelper pathConversionHelper;

  // ~ Constructors ====================================================================================================

  public JackrabbitRepositoryFileAclDao(final JcrTemplate jcrTemplate, final IPathConversionHelper pathConversionHelper) {
    super();
    this.jcrTemplate = jcrTemplate;
    this.pathConversionHelper = pathConversionHelper;
  }

  // ~ Methods =========================================================================================================

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  public List<RepositoryFileAce> getEffectiveAces(final Serializable id, final boolean forceEntriesInheriting) {
    return (List<RepositoryFileAce>) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        Node node = session.getNodeByUUID(id.toString());

        if (node == null) {
          throw new RepositoryException(Messages.getInstance().getString(
              "JackrabbitRepositoryFileAclDao.ERROR_0001_NODE_NOT_FOUND", id.toString())); //$NON-NLS-1$
        }

        // consult the parent node's effective policy if force is true and parent is not null
        if (forceEntriesInheriting && session.getNodeByUUID(id.toString()).getParent() != null) {
          node = node.getParent();
        }

        Assert.isInstanceOf(SessionImpl.class, session);
        SessionImpl jrSession = (SessionImpl) session;

        String absPath = node.getPath();

        AccessControlPolicy[] acPolicies = jrSession.getAccessControlManager().getEffectivePolicies(absPath);
        Assert.isTrue(acPolicies != null && acPolicies.length == 1
            && acPolicies[0] instanceof IPentahoJackrabbitAccessControlList);
        IPentahoJackrabbitAccessControlList acl = (IPentahoJackrabbitAccessControlList) acPolicies[0];

        AccessControlEntry[] acEntries = acl.getAccessControlEntries();
        List<RepositoryFileAce> aces = new ArrayList<RepositoryFileAce>();
        for (int i = 0; i < acEntries.length; i++) {
          aces.add(toAce(jrSession, acEntries[i]));
        }
        return aces;
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  public boolean hasAccess(final String relPath, final EnumSet<RepositoryFilePermission> permissions) {
    return (Boolean) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        Assert.isInstanceOf(SessionImpl.class, session);
        SessionImpl jrSession = (SessionImpl) session;

        Privilege[] privs = permissionConversionHelper.pentahoPermissionsToJackrabbitPrivileges(jrSession, permissions);
        try {
          String absPath = pathConversionHelper.relToAbs(relPath);
          return jrSession.getAccessControlManager().hasPrivileges(absPath, privs);
        } catch (PathNotFoundException e) {
          // never throw an exception if the path does not exist; just return false
          return false;
        }
      }
    });
  }

  private AccessControlPolicy getAccessControlPolicy(final AccessControlManager acMgr, final String absPath)
      throws RepositoryException {
    AccessControlPolicy[] policies = acMgr.getPolicies(absPath);
    Assert.notEmpty(policies, Messages.getInstance().getString("JackrabbitRepositoryFileAclDao.ERROR_0002_POLICY")); //$NON-NLS-1$
    return policies[0];
  }

  private String getUsername() {
    IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
    Assert.state(pentahoSession != null);
    return pentahoSession.getName();
  }
  
  public RepositoryFileAcl createDefaultAcl() {
    return new RepositoryFileAcl.Builder(new RepositoryFileSid(getUsername())).entriesInheriting(true).build();
  }

  private RepositoryFileAcl toAcl(final SessionImpl jrSession, final PentahoJcrConstants pentahoJcrConstants,
      final Serializable id) throws RepositoryException {

    Node node = jrSession.getNodeByUUID(id.toString());
    if (node == null) {
      throw new RepositoryException(Messages.getInstance().getString(
          "JackrabbitRepositoryFileAclDao.ERROR_0001_NODE_NOT_FOUND", id.toString())); //$NON-NLS-1$
    }
    String absPath = node.getPath();
    AccessControlManager acMgr = jrSession.getAccessControlManager();
    AccessControlPolicy acPolicy = getAccessControlPolicy(acMgr, absPath);

    Assert.isInstanceOf(IPentahoJackrabbitAccessControlList.class, acPolicy);

    IPentahoJackrabbitAccessControlList acList = (IPentahoJackrabbitAccessControlList) acPolicy;

    RepositoryFileSid owner = null;
    Principal ownerPrincipal = acList.getOwner();

    // special handling for root node; it doesn't have a "pentaho acl" so we make some assumptions; see 
    // PentahoAccessControlEditor#createAclNode
    if (ownerPrincipal == null) {
      owner = null;
    } else {
      if (ownerPrincipal instanceof Group) {
        owner = new RepositoryFileSid(ownerPrincipal.getName(), RepositoryFileSid.Type.ROLE);
      } else {
        owner = new RepositoryFileSid(ownerPrincipal.getName(), RepositoryFileSid.Type.USER);
      }
    }

    RepositoryFileAcl.Builder aclBuilder = new RepositoryFileAcl.Builder(id, owner);

    // special handling for root node; it doesn't have a "pentaho acl" so we make some assumptions; see 
    // PentahoAccessControlEditor#createAclNode
    if (!jrSession.getRootNode().isSame(node)) {
      aclBuilder.entriesInheriting(acList.isEntriesInheriting());
    } else {
      aclBuilder.entriesInheriting(false);
    }
    AccessControlEntry[] acEntries = acList.getAccessControlEntries();
    for (int i = 0; i < acEntries.length; i++) {
      aclBuilder.ace(toAce(jrSession, acEntries[i]));
    }
    return aclBuilder.build();

  }

  protected RepositoryFileAce toAce(final SessionImpl jrSession, final AccessControlEntry acEntry)
      throws RepositoryException {
    Assert.isInstanceOf(JackrabbitAccessControlEntry.class, acEntry);
    JackrabbitAccessControlEntry jrAce = (JackrabbitAccessControlEntry) acEntry;
    Principal principal = jrAce.getPrincipal();
    RepositoryFileSid sid = null;
    if (principal instanceof Group) {
      sid = new RepositoryFileSid(principal.getName(), RepositoryFileSid.Type.ROLE);
    } else {
      sid = new RepositoryFileSid(principal.getName(), RepositoryFileSid.Type.USER);
    }
    logger.debug(String.format("principal class [%s]", principal.getClass().getName())); //$NON-NLS-1$
    Privilege[] privileges = jrAce.getPrivileges();
    return new RepositoryFileAce(sid, permissionConversionHelper.jackrabbitPrivilegesToPentahoPermissions(jrSession,
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
    Privilege[] pentahoPermissionsToJackrabbitPrivileges(final SessionImpl jrSession,
        final EnumSet<RepositoryFilePermission> permission) throws RepositoryException;

    EnumSet<RepositoryFilePermission> jackrabbitPrivilegesToPentahoPermissions(final SessionImpl jrSession,
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

  /**
   * <p>
   * In Jackrabbit 1.6 (and maybe 2.0), objects already have an AccessControlPolicy of type AccessControlList. It is 
   * empty by default with implicit read access for everyone.
   * </p>
   */
  public RepositoryFileAcl createAcl(final Serializable fileId, final RepositoryFileAcl acl) {
    return (RepositoryFileAcl) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        Assert.isInstanceOf(SessionImpl.class, session);
        SessionImpl jrSession = (SessionImpl) session;

        Node node = session.getNodeByUUID(fileId.toString());

        String absPath = node.getPath();
        AccessControlManager acMgr = jrSession.getAccessControlManager();

        AccessControlPolicyIterator iter = acMgr.getApplicablePolicies(absPath);
        // acMrg.getApplicablePolicies returns non-empty iterator when there is no existing ACL on the node; if 
        // non-empty, call setPolicy on the absPath for the node; subsequent calls then use 
        // acMgr.getPolicies(absPath)[0]
        if (iter.hasNext()) {
          AccessControlPolicy acPolicy = iter.nextAccessControlPolicy();
          Assert.isInstanceOf(IPentahoJackrabbitAccessControlList.class, acPolicy);
          IPentahoJackrabbitAccessControlList jrPolicy = (IPentahoJackrabbitAccessControlList) acPolicy;
          // owner cannot be null; set temporarily here; will be possibly changed in internalUpdateAcl below

          Principal ownerPrincipal = null;
          try {
            ownerPrincipal = jrSession.getPrincipalManager().getPrincipal(getUsername());
          } catch (NoSuchPrincipalException e) {
            ownerPrincipal = new UserPrincipal(getUsername());
          }

          jrPolicy.setOwner(ownerPrincipal);

          acMgr.setPolicy(absPath, acPolicy);
        }
        // nothing needs to be returned
        return internalUpdateAcl(fileId, acl);
      }
    });
  }

  public RepositoryFileAcl getAcl(final Serializable id) {
    return (RepositoryFileAcl) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        Assert.isTrue(session instanceof SessionImpl);
        SessionImpl jrSession = (SessionImpl) session;

        return toAcl(jrSession, pentahoJcrConstants, id);
      }
    });
  }

  protected RepositoryFileAcl getParentAcl(final Serializable id) {
    return (RepositoryFileAcl) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        Assert.isTrue(session instanceof SessionImpl);
        SessionImpl jrSession = (SessionImpl) session;
        Node node = jrSession.getNodeByUUID(id.toString());
        if (!node.getParent().isSame(jrSession.getRootNode())) {
          return toAcl(jrSession, pentahoJcrConstants, node.getParent().getUUID());
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
    return internalUpdateAcl(acl.getId(), acl);
  }

  protected RepositoryFileAcl internalUpdateAcl(final Serializable fileId, final RepositoryFileAcl acl) {
    return (RepositoryFileAcl) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        Assert.isTrue(session instanceof SessionImpl);
        SessionImpl jrSession = (SessionImpl) session;
        Node node = session.getNodeByUUID(fileId.toString());
        if (node == null) {
          throw new RepositoryException(Messages.getInstance().getString(
              "JackrabbitRepositoryFileAclDao.ERROR_0001_NODE_NOT_FOUND", fileId.toString())); //$NON-NLS-1$
        }
        String absPath = node.getPath();
        AccessControlManager acMgr = jrSession.getAccessControlManager();
        AccessControlPolicy acPolicy = getAccessControlPolicy(acMgr, absPath);
        Assert.isInstanceOf(JackrabbitAccessControlList.class, acPolicy);
        // cast to JackrabbitAccessControlList to get addEntry call (for isAllow parameter)
        IPentahoJackrabbitAccessControlList acList = (IPentahoJackrabbitAccessControlList) acPolicy;

        acList.setEntriesInheriting(acl.isEntriesInheriting());

        Principal ownerPrincipal = null;
        try {
          ownerPrincipal = jrSession.getPrincipalManager().getPrincipal(acl.getOwner().getName());
        } catch (NoSuchPrincipalException e) {
          if (RepositoryFileSid.Type.ROLE == acl.getOwner().getType()) {
            ownerPrincipal = new SpringSecurityRolePrincipal(acl.getOwner().getName());
          } else {
            ownerPrincipal = new UserPrincipal(acl.getOwner().getName());
          }
        }

        acList.setOwner(ownerPrincipal);

        // clear all entries
        AccessControlEntry[] acEntries = acList.getAccessControlEntries();
        for (int i = 0; i < acEntries.length; i++) {
          acList.removeAccessControlEntry(acEntries[i]);
        }
        // add entries to now empty list but only if not inheriting; force user to start with clean slate 
        if (!acl.isEntriesInheriting()) {
          for (RepositoryFileAce ace : acl.getAces()) {
            Principal principal = null;
            try {
              principal = jrSession.getPrincipalManager().getPrincipal(ace.getSid().getName());
            } catch (NoSuchPrincipalException e) {
              if (RepositoryFileSid.Type.ROLE == ace.getSid().getType()) {
                principal = new SpringSecurityRolePrincipal(ace.getSid().getName());
              } else {
                principal = new UserPrincipal(ace.getSid().getName());
              }
            }

            acList.addEntry(principal,
                permissionConversionHelper.pentahoPermissionsToJackrabbitPrivileges(jrSession, ace.getPermissions()),
                true);
          }
        }
        acMgr.setPolicy(absPath, acList);
        session.save();
        return getAcl(fileId);
      }
    });

  }

}
