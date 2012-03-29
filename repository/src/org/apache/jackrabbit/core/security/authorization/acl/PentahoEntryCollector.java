package org.apache.jackrabbit.core.security.authorization.acl;

import java.security.Principal;
import java.security.acl.Group;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.security.AccessControlEntry;
import javax.jcr.security.Privilege;
import javax.jcr.version.VersionHistory;

import org.apache.jackrabbit.core.NodeImpl;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.id.NodeId;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.TenantUtils;
import org.pentaho.platform.repository2.unified.jcr.IAclMetadataStrategy.AclMetadata;
import org.pentaho.platform.repository2.unified.jcr.JcrRepositoryFileAclUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copy-and-paste of {@code org.apache.jackrabbit.core.security.authorization.acl.EntryCollector} in Jackrabbit 
 * 2.4.0. This class is in {@code org.apache.jackrabbit.core.security.authorization.acl} package due to the scope of
 * collaborating classes.
 * 
 * <p>Changes to original:</p>
 * <ul>
 * <li>{@code Entries} never has a non-null {@code nextId}.</li>
 * <li>{@code collectEntries()} copied from {@code EntryCollector} uses {@code entries.getNextId()} instead of 
 * {@code node.getParentId()}</li>
 * <li>{@code filterEntries()} copied from {@code EntryCollector} as it was {@code static} and {@code private}.</li>
 * <li>No caching is done in the presence of dynamic ACEs. This may need to be revisited but due to the short lifetime
 * of the way we use Sessions, it may be acceptable.</li>
 * </ul>
 * 
 * <p>Original Javadoc:</p>
 * <code>CachingEntryCollector</code> extends <code>EntryCollector</code> by
 * keeping a cache of ACEs per access controlled nodeId.
 * 
 * @author mlowery
 */
public class PentahoEntryCollector extends EntryCollector {

  /**
   * logger instance
   */
  private static final Logger log = LoggerFactory.getLogger(PentahoEntryCollector.class);

  public PentahoEntryCollector(final SessionImpl systemSession, final NodeId rootID, final Map configuration)
      throws RepositoryException {
    super(systemSession, rootID);
    parseMagicAceDefinitions(configuration);
  }

  private List<MagicAceDefinition> magicAceDefinitions = new ArrayList<MagicAceDefinition>();

  protected void parseMagicAceDefinitions(final Map configuration) throws RepositoryException {
    for (int i = 0;; i++) {
      String value = (String) configuration.get("magicAceDefinition" + i); //$NON-NLS-1$
      if (value == null) {
        break;
      }
      MagicAceDefinition pam = parseMagicAceDefinition(value);
      magicAceDefinitions.add(pam);
    }
    if (log.isDebugEnabled()) {
      log.debug("magic ACE definitions: " + magicAceDefinitions); //$NON-NLS-1$
    }
  }

  protected MagicAceDefinition parseMagicAceDefinition(final String value) throws RepositoryException {
    String[] tokens = value.split("\\;"); //$NON-NLS-1$
    if (tokens.length != 4) {
      throw new IllegalArgumentException();
    }
    String path = tokens[0];
    String logicalRole = tokens[1];
    String privilegeString = tokens[2];
    boolean recursive = Boolean.valueOf(tokens[3]);

    String[] privilegeTokens = privilegeString.split("\\,"); //$NON-NLS-1$
    List<Privilege> privileges = new ArrayList<Privilege>(privilegeTokens.length);
    for (String privilegeToken : privilegeTokens) {
      privileges.add(systemSession.getAccessControlManager().privilegeFromName(privilegeToken));
    }

    return new MagicAceDefinition(path, logicalRole, privileges.toArray(new Privilege[0]), recursive);
  }

  @Override
  protected Entries getEntries(NodeImpl node) throws RepositoryException {
    // find nearest node with an ACL that is not inheriting ACEs
    NodeImpl currentNode = node;
    NodeImpl aclNode;
    ACLTemplate acl;

    // version history governed by ACL on "versionable" which could be the root if no version history exists for file;
    // if we do hit the root, then you get jcr:read for everyone which is acceptable
    if (currentNode.getPath().startsWith("/jcr:system/jcr:versionStorage")) { //$NON-NLS-1$
      currentNode = getVersionable(currentNode);
    }

    boolean firstAccessControlledNode = true;
    String owner = null;
    
    while (true) {
      // skip all nodes that are not access-controlled
      if (!ACLProvider.isAccessControlled(currentNode)) {
        currentNode = (NodeImpl) currentNode.getParent();
        continue;
      }
      aclNode = currentNode.getNode(N_POLICY);
      acl = new ACLTemplate(aclNode);
    
      // owner comes from the first access-controlled node
      if (firstAccessControlledNode) {
        firstAccessControlledNode = false;
        AclMetadata aclMetadata = JcrRepositoryFileAclUtils.getAclMetadata(systemSession,
            currentNode.getPath(), acl);
        if (aclMetadata != null) {
          owner = aclMetadata.getOwner();
        }
      }
      
      // skip all nodes that are inheriting
      AclMetadata aclMetadata = JcrRepositoryFileAclUtils.getAclMetadata(systemSession,
          currentNode.getPath(), acl);
      if (aclMetadata != null && aclMetadata.isEntriesInheriting()) {
        currentNode = (NodeImpl) currentNode.getParent();
        continue;
      }
      break;
    }
    
    if (owner != null) {
      addOwnerAce(owner, acl);
    }
    
    // now acl points to the nearest ancestor that is access-controlled and is not inheriting;
    return new Entries(new ArrayList<AccessControlEntry>(getAcesIncludingMagicAces(currentNode.getPath(), acl)), null);
  }

  protected NodeImpl getVersionable(final NodeImpl node) throws RepositoryException {
    NodeImpl currentNode = node;
    while (!currentNode.isNodeType("nt:versionHistory") && !rootID.equals(currentNode.getNodeId())) { //$NON-NLS-1$
      currentNode = (NodeImpl) currentNode.getParent();
    }
    if (rootID.equals(currentNode.getNodeId())) {
      return currentNode;
    } else {
      return (NodeImpl) systemSession.getNodeByIdentifier(((VersionHistory) currentNode).getVersionableIdentifier());
    }
  }

  protected IAuthorizationPolicy getAuthorizationPolicy() {
    IAuthorizationPolicy authorizationPolicy = PentahoSystem.get(IAuthorizationPolicy.class);
    if (authorizationPolicy == null) {
      throw new IllegalStateException();
    }
    return authorizationPolicy;
  }

  protected List<AccessControlEntry> getAcesIncludingMagicAces(final String path, final ACLTemplate acl)
      throws RepositoryException {
    if (PentahoSessionHolder.getSession() == null || PentahoSessionHolder.getSession().getName() == null
        || PentahoSessionHolder.getSession().getName().trim().equals("")) { //$NON-NLS-1$
      if (log.isDebugEnabled()) {
        log.debug("no PentahoSession so no magic ACEs"); //$NON-NLS-1$
      }
      return Collections.emptyList();
    }
    boolean match = false;
    for (final MagicAceDefinition def : magicAceDefinitions) {
      match = false;
      String substitutedPath = MessageFormat.format(def.path, TenantUtils.getTenantId());
      if (getAuthorizationPolicy().isAllowed(def.logicalRole)) {
        if (def.recursive) {
          if (path.equals(substitutedPath) || path.startsWith(substitutedPath + "/")) { //$NON-NLS-1$
            match = true;
          }
        } else {
          if (path.equals(substitutedPath)) {
            match = true;
          }
        }
      }
      if (match) {
        Principal principal = new MagicPrincipal(PentahoSessionHolder.getSession().getName());
        // unfortunately, we need the ACLTemplate because it alone can create ACEs that can be cast successfully later; changed never persisted
        acl.addAccessControlEntry(principal, def.privileges);
      }
    }
    return acl.getEntries();
  }

  protected void addOwnerAce(final String owner, final ACLTemplate acl) throws RepositoryException {
      Principal ownerPrincipal = systemSession.getPrincipalManager().getPrincipal(owner);
      if (ownerPrincipal != null) {
        Principal magicPrincipal = null;
        if (ownerPrincipal instanceof Group) {
          magicPrincipal = new MagicGroup((Group) ownerPrincipal);
        } else {
          magicPrincipal = new MagicPrincipal(ownerPrincipal.getName());
        }
        // unfortunately, we need the ACLTemplate because it alone can create ACEs that can be cast successfully later; changed never persisted
        acl.addAccessControlEntry(magicPrincipal, new Privilege[] { systemSession.getAccessControlManager()
            .privilegeFromName("jcr:all") }); //$NON-NLS-1$
      } else {
        // if the Principal doesn't exist anymore, then there's no reason to add an ACE for it
        if (log.isDebugEnabled()) {
          log.debug("PrincipalManager cannot find owner=" + owner); //$NON-NLS-1$
        }
      }

  }

  /**
   * Collect the ACEs effective at the given node applying the specified
   * filter.
   * 
   * @param node
   * @param filter
   * @return
   * @throws RepositoryException
   */
  @Override
  protected List<AccessControlEntry> collectEntries(NodeImpl node, EntryFilter filter) throws RepositoryException {
    LinkedList<AccessControlEntry> userAces = new LinkedList<AccessControlEntry>();
    LinkedList<AccessControlEntry> groupAces = new LinkedList<AccessControlEntry>();

    if (node == null) {
      // repository level permissions
      NodeImpl root = (NodeImpl) systemSession.getRootNode();
      if (ACLProvider.isRepoAccessControlled(root)) {
        NodeImpl aclNode = root.getNode(N_REPO_POLICY);
        filterEntries(filter, new ACLTemplate(aclNode).getEntries(), userAces, groupAces);
      }
    } else {
      Entries entries = getEntries(node);
      filterEntries(filter, entries.getACEs(), userAces, groupAces);
      NodeId next = entries.getNextId();
      while (next != null) {
        entries = getEntries(next);
        filterEntries(filter, entries.getACEs(), userAces, groupAces);
        next = entries.getNextId();
      }
    }

    List<AccessControlEntry> entries = new ArrayList<AccessControlEntry>(userAces.size() + groupAces.size());
    entries.addAll(userAces);
    entries.addAll(groupAces);

    return entries;
  }

  /**
   * Filter the specified access control <code>entries</code>
   *
   * @param filter
   * @param aces
   * @param userAces
   * @param groupAces
   */
  @SuppressWarnings("unchecked")
  protected void filterEntries(EntryFilter filter, List<AccessControlEntry> aces,
      LinkedList<AccessControlEntry> userAces,
      LinkedList<AccessControlEntry> groupAces) {
    if (!aces.isEmpty() && filter != null) {
      filter.filterEntries(aces, userAces, groupAces);
    }
  }

}
