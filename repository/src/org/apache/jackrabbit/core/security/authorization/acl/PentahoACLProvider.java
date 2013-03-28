package org.apache.jackrabbit.core.security.authorization.acl;

import java.security.Principal;
import java.util.Map;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.AccessControlEntry;
import javax.jcr.security.AccessControlList;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.AccessControlPolicy;
import javax.jcr.security.Privilege;

import org.apache.jackrabbit.api.security.principal.PrincipalManager;
import org.apache.jackrabbit.core.id.NodeId;
import org.apache.jackrabbit.core.security.SystemPrincipal;
import org.apache.jackrabbit.core.security.authorization.CompiledPermissions;
import org.apache.jackrabbit.core.security.principal.AdminPrincipal;
import org.apache.jackrabbit.core.NodeImpl;
import org.apache.jackrabbit.core.SessionImpl;

import org.pentaho.platform.repository2.unified.jcr.jackrabbit.security.SpringSecurityRolePrincipal;

/**
 * Customization of {@link ACLProvider}.
 * 
 * @author mlowery
 */
public class PentahoACLProvider extends ACLProvider {

  private Map configuration;

  // Overrides to CompiledPermissions creation require we keep an extra reference
  // because this is private in ACLProvider
  private EntryCollector entryCollector;

  /**
   * Overridden to:
   * <ul>
   * <li>Store {@code configuration} for later passing to {@link PentahoEntryCollector}.</li>
   * <li>Add JCR_READ_ACCESS_CONTROL to root ACL. This is harmless and avoids more customization.</li>
   * </ul>
   */
  @Override
  public void init(final Session systemSession, final Map conf) throws RepositoryException {
    this.configuration = conf;
    super.init(systemSession, conf);
    // original initRootACL should run during super.init call above
    updateRootAcl((SessionImpl) systemSession, new ACLEditor(session, this));
  }

  /**
   * Adds ACE so that everyone can read access control. This allows Jackrabbit's default collectAcls to work without
   * change. Otherwise, you have to be an admin to call acMgr.getEffectivePolicies.
   */
  protected void updateRootAcl(SessionImpl systemSession, ACLEditor editor) throws RepositoryException {
    String rootPath = session.getRootNode().getPath();
    AccessControlPolicy[] acls = editor.getPolicies(rootPath);
    if (acls.length > 0) {
      PrincipalManager pMgr = systemSession.getPrincipalManager();
      AccessControlManager acMgr = session.getAccessControlManager();
      Principal everyone = pMgr.getEveryone();
      Privilege[] privs = new Privilege[] { acMgr.privilegeFromName(Privilege.JCR_READ),
          acMgr.privilegeFromName(Privilege.JCR_READ_ACCESS_CONTROL) };
      AccessControlList acList = (AccessControlList) acls[0];
      AccessControlEntry[] acEntries = acList.getAccessControlEntries();
      for (AccessControlEntry acEntry : acEntries) {
        if (acEntry.getPrincipal().equals(everyone)) {
          acList.removeAccessControlEntry(acEntry);
        }
      }
      acList.addAccessControlEntry(everyone, privs);
      editor.setPolicy(rootPath, acList);
      session.save();
    }
  }

  /**
   * Overridden to:
   * <ul>
   * <li>Return custom {@code EntryCollector}.
   * <li>Later access to the {@code EntryCollector}
   * </ul>
   */
  @Override
  protected EntryCollector createEntryCollector(SessionImpl systemSession) throws RepositoryException {
    // keep our own private reference; the one in ACLProvider is private
    entryCollector = new PentahoEntryCollector(systemSession, getRootNodeId(), configuration);
    return entryCollector;
  }
  
  /**
   * Overridden to:
   * <ul>
   * <li>Return custom {@code CompiledPermissions}.
   * </ul>
   * @see PentahoCompiledPermissionsImpl
   */
  @Override
  public CompiledPermissions compilePermissions(Set<Principal> principals) throws RepositoryException {
      checkInitialized();
      if (isAdminOrSystem(principals)) {
          return getAdminPermissions();
      } else if (isReadOnly(principals)) {
          return getReadOnlyPermissions();
      } else {
          return new PentahoCompiledPermissionsImpl(principals, session, entryCollector, this, true);
      }
  }

  /**
   * Overridden to:
   * <ul>
   * <li>Use custom {@code CompiledPermissions}.
   * </ul>
   * @see PentahoCompiledPermissionsImpl
   */
  @Override
  public boolean canAccessRoot(Set<Principal> principals) throws RepositoryException {
      checkInitialized();
      if (isAdminOrSystem(principals)) {
          return true;
      } else {
          CompiledPermissions cp = new PentahoCompiledPermissionsImpl(principals, session, entryCollector, this, false);
          try {
              return cp.canRead(null, getRootNodeId());
          } finally {
              cp.close();
          }
      }
  }

  private NodeId getRootNodeId() throws RepositoryException {
    // TODO: how expensive is this? Should we keep a reference?
    return ((NodeImpl) session.getRootNode()).getNodeId();
  }

}
