package org.apache.jackrabbit.core.security.authorization.acl;

import java.security.Principal;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.AccessControlEntry;
import javax.jcr.security.AccessControlList;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.AccessControlPolicy;
import javax.jcr.security.Privilege;

import org.apache.jackrabbit.api.security.principal.PrincipalManager;
import org.apache.jackrabbit.core.NodeImpl;
import org.apache.jackrabbit.core.SessionImpl;

/**
 * Subclass of {@link ACLProvider} to customize the {@link EntryCollector} returned.
 * 
 * @author mlowery
 */
public class PentahoACLProvider extends ACLProvider {

  private Map configuration;

  /**
   * Overridden to:
   * <ul>
   * <li>Store {@code configuration} for later passing to {@link PentahoEntryCollector}.</li>
   * <li>Add READ_AC to root ACL. This is harmless and avoids more copy-and-pasting.</li>
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
   * </ul>
   */
  @Override
  protected EntryCollector createEntryCollector(SessionImpl systemSession) throws RepositoryException {
    NodeImpl root = (NodeImpl) session.getRootNode();
    return new PentahoEntryCollector(systemSession, root.getNodeId(), configuration);
  }

}
