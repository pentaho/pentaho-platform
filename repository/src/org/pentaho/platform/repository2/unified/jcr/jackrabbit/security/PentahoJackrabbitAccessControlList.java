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

import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.NodeIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.jackrabbit.api.jsr283.security.AccessControlEntry;
import org.apache.jackrabbit.api.jsr283.security.AccessControlException;
import org.apache.jackrabbit.api.jsr283.security.AccessControlManager;
import org.apache.jackrabbit.api.jsr283.security.Privilege;
import org.apache.jackrabbit.api.security.principal.NoSuchPrincipalException;
import org.apache.jackrabbit.api.security.principal.PrincipalManager;
import org.apache.jackrabbit.core.NodeImpl;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.security.UserPrincipal;
import org.apache.jackrabbit.core.security.authorization.AccessControlConstants;
import org.apache.jackrabbit.core.security.authorization.AccessControlEntryImpl;
import org.apache.jackrabbit.core.security.authorization.JackrabbitAccessControlList;
import org.apache.jackrabbit.core.security.authorization.Permission;
import org.apache.jackrabbit.core.security.authorization.PrivilegeRegistry;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.commons.name.NameFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copy of {@code org.apache.jackrabbit.core.security.authorization.acl.ACLTemplate}.
 * 
 * <p>Modifications</p>
 * <ul>
 * <li>Addition of owner and entriesInheriting properties.</li>
 * </ul>
 * 
 * @author mlowery
 */
@SuppressWarnings("nls")
class PentahoJackrabbitAccessControlList implements IPentahoJackrabbitAccessControlList {

  private static final Logger log = LoggerFactory.getLogger(PentahoJackrabbitAccessControlList.class);

  /**
   * Path of the node this ACL template has been created for.
   */
  private final String path;

  /**
   * Map containing the entries of this ACL Template using the principal
   * name as key. The value represents a List containing maximal one grant
   * and one deny ACE per principal.
   */
  private final Map entries = new ListOrderedMap();

  private static final Name PHO_NT_PENTAHOACL = NameFactoryImpl.getInstance().create(
      "http://www.pentaho.org/jcr/nt/1.0", "pentahoAcl");

  private static final Name PHO_ACLOWNERNAME = NameFactoryImpl.getInstance().create("http://www.pentaho.org/jcr/1.0",
      "aclOwnerName");
  
  private static final Name PHO_ACLOWNERTYPE = NameFactoryImpl.getInstance().create("http://www.pentaho.org/jcr/1.0",
      "aclOwnerType");

  private static final Name PHO_ACLINHERITING = NameFactoryImpl.getInstance().create("http://www.pentaho.org/jcr/1.0",
      "aclInheriting");

  private static final Name PHO_ACERECIPIENTTYPE = NameFactoryImpl.getInstance().create("http://www.pentaho.org/jcr/1.0",
      "aceRecipientType");
  
  public static final String PRINCIPAL_TYPE_USER = "user";
  
  public static final String PRINCIPAL_TYPE_ROLE = "role";

  private Principal owner;

  private boolean entriesInheriting = true;

  /**
   * The principal manager used for validation checks
   */
  private final PrincipalManager principalMgr;

  /**
   * The privilege registry
   */
  private final PrivilegeRegistry privilegeRegistry;

  /**
   * The value factory
   */
  private final ValueFactory valueFactory;

  /**
   * Construct a new empty {@link ACLTemplate}.
   *
   * @param path
   * @param privilegeRegistry
   * @param principalMgr
   */
  PentahoJackrabbitAccessControlList(String path, PrincipalManager principalMgr, PrivilegeRegistry privilegeRegistry,
      ValueFactory valueFactory) {
    this.path = path;
    this.principalMgr = principalMgr;
    this.privilegeRegistry = privilegeRegistry;
    this.valueFactory = valueFactory;
  }

  /**
   * Create a {@link ACLTemplate} that is used to edit an existing ACL
   * node.
   *
   * @param aclNode
   * @param privilegeRegistry
   * @throws RepositoryException
   */
  PentahoJackrabbitAccessControlList(NodeImpl aclNode, PrivilegeRegistry privilegeRegistry) throws RepositoryException {
    if (aclNode == null
        || !(aclNode.isNodeType(PHO_NT_PENTAHOACL) || aclNode.isNodeType(AccessControlConstants.NT_REP_ACL))) {
      throw new IllegalArgumentException("Node must be of type 'rep:ACL' or 'pho_nt:pentahoAcl'");
    }
    SessionImpl sImpl = (SessionImpl) aclNode.getSession();
    path = aclNode.getParent().getPath();
    principalMgr = sImpl.getPrincipalManager();
    valueFactory = sImpl.getValueFactory();

    this.privilegeRegistry = privilegeRegistry;

    if (!aclNode.getPrimaryNodeType().getName().equals(
        AccessControlConstants.NT_REP_ACL.NS_REP_PREFIX + ":" + AccessControlConstants.NT_REP_ACL.getLocalName())) {
      String ownerName = aclNode.getProperty(PHO_ACLOWNERNAME).getString();
      if (principalMgr.hasPrincipal(ownerName)) {
        try {
          owner = principalMgr.getPrincipal(ownerName);
        } catch (NoSuchPrincipalException e) {
          // should not get here.
        }
      }
      if (owner == null) {
        if (log.isTraceEnabled()) {
          log.trace("Owner with name " + ownerName + " unknown to PrincipalManager.");
        }
        String ownerType = aclNode.getProperty(PHO_ACLOWNERTYPE).getString();
        if (PentahoJackrabbitAccessControlList.PRINCIPAL_TYPE_ROLE.equals(ownerType)) {
          owner = new SpringSecurityRolePrincipal(ownerName);
        } else {
          owner = new UserPrincipal(ownerName);
        }
      }

      entriesInheriting = aclNode.getProperty(PHO_ACLINHERITING).getBoolean();
    }

    // load the entries:
    AccessControlManager acMgr = sImpl.getAccessControlManager();
    NodeIterator itr = aclNode.getNodes();
    while (itr.hasNext()) {
      NodeImpl aceNode = (NodeImpl) itr.nextNode();
      // ignore owner, etc nodes
      if (!aceNode.isNodeType(AccessControlConstants.NT_REP_ACE)) {
        continue;
      }
      try {
        String principalName = aceNode.getProperty(AccessControlConstants.P_PRINCIPAL_NAME).getString();
        Principal princ = null;
        if (principalMgr.hasPrincipal(principalName)) {
          try {
            princ = principalMgr.getPrincipal(principalName);
          } catch (NoSuchPrincipalException e) {
            // should not get here.
          }
        }
        if (princ == null) {
          if (log.isTraceEnabled()) {
            log.trace("Principal with name " + principalName + " unknown to PrincipalManager.");
          }
          String principalType = aceNode.getProperty(PHO_ACERECIPIENTTYPE).getString();
          if (PentahoJackrabbitAccessControlList.PRINCIPAL_TYPE_ROLE.equals(principalType)) {
            princ = new SpringSecurityRolePrincipal(principalName);
          } else {
            princ = new UserPrincipal(principalName);
          }
        }

        Value[] privValues = aceNode.getProperty(AccessControlConstants.P_PRIVILEGES).getValues();
        Privilege[] privs = new Privilege[privValues.length];
        for (int i = 0; i < privValues.length; i++) {
          privs[i] = acMgr.privilegeFromName(privValues[i].getString());
        }
        // create a new ACEImpl (omitting validation check)
        Entry ace = new Entry(princ, privs, aceNode.isNodeType(AccessControlConstants.NT_REP_GRANT_ACE), valueFactory);
        // add the entry
        internalAdd(ace);
      } catch (RepositoryException e) {
        log.debug("Failed to build ACE from content.", e.getMessage());
      }
    }
  }

  private List internalGetEntries() {
    List l = new ArrayList();
    for (Iterator it = entries.values().iterator(); it.hasNext();) {
      l.addAll((List) it.next());
    }
    return l;
  }

  private List internalGetEntries(Principal principal) {
    String principalName = principal.getName();
    if (entries.containsKey(principalName)) {
      return (List) entries.get(principalName);
    } else {
      return new ArrayList(2);
    }
  }

  private synchronized boolean internalAdd(Entry entry) throws AccessControlException {
    Principal principal = entry.getPrincipal();
    List l = internalGetEntries(principal);
    if (l.isEmpty()) {
      // simple case: just add the new entry
      l.add(entry);
      entries.put(principal.getName(), l);
      return true;
    } else {
      if (l.contains(entry)) {
        // the same entry is already contained -> no modification
        return false;
      }
      // ev. need to adjust existing entries
      Entry complementEntry = null;
      Entry[] entries = (Entry[]) l.toArray(new Entry[l.size()]);
      for (int i = 0; i < entries.length; i++) {
        if (entry.isAllow() == entries[i].isAllow()) {
          int existingPrivs = entries[i].getPrivilegeBits();
          if ((existingPrivs | ~entry.getPrivilegeBits()) == -1) {
            // all privileges to be granted/denied are already present
            // in the existing entry -> not modified
            return false;
          }

          // remove the existing entry and create a new that includes
          // both the new privileges and the existing onces.
          l.remove(i);
          int mergedBits = entries[i].getPrivilegeBits() | entry.getPrivilegeBits();
          Privilege[] mergedPrivs = privilegeRegistry.getPrivileges(mergedBits);
          // omit validation check.
          entry = new Entry(entry.getPrincipal(), mergedPrivs, entry.isAllow(), valueFactory);
        } else {
          complementEntry = entries[i];
        }
      }

      // make sure, that the complement entry (if existing) does not
      // grant/deny the same privileges -> remove privs that are now
      // denied/granted.
      if (complementEntry != null) {
        int complPrivs = complementEntry.getPrivilegeBits();
        int resultPrivs = Permission.diff(complPrivs, entry.getPrivilegeBits());
        if (resultPrivs == PrivilegeRegistry.NO_PRIVILEGE) {
          l.remove(complementEntry);
        } else if (resultPrivs != complPrivs) {
          l.remove(complementEntry);
          // omit validation check
          Entry tmpl = new Entry(entry.getPrincipal(), privilegeRegistry.getPrivileges(resultPrivs), !entry.isAllow(),
              valueFactory);
          l.add(tmpl);
        } /* else: does not need to be modified.*/
      }

      // finally add the new entry at the end.
      l.add(entry);
      return true;
    }
  }

  /**
   *
   * @param principal
   * @param privileges
   * @param isAllow
   * @throws AccessControlException
   */
  private void checkValidEntry(Principal principal, Privilege[] privileges, boolean isAllow)
      throws AccessControlException {
    // validate principal
//    if (!principalMgr.hasPrincipal(principal.getName())) {
//      throw new AccessControlException("Principal " + principal.getName() + " does not exist.");
//    }
    // additional validation: a group may not have 'denied' permissions
    if (!isAllow && principal instanceof Group) {
      throw new AccessControlException("For group principals permissions can only be added but not denied.");
    }
  }

  //--------------------------------------------------< AccessControlList >---
  /**
   * @see org.apache.jackrabbit.api.jsr283.security.AccessControlList#getAccessControlEntries()
   */
  public AccessControlEntry[] getAccessControlEntries() throws RepositoryException {
    List l = internalGetEntries();
    return (AccessControlEntry[]) l.toArray(new AccessControlEntry[l.size()]);
  }

  /**
   * @see org.apache.jackrabbit.api.jsr283.security.AccessControlList#addAccessControlEntry(Principal, Privilege[])
   */
  public boolean addAccessControlEntry(Principal principal, Privilege[] privileges) throws AccessControlException,
      RepositoryException {
    return addEntry(principal, privileges, true, Collections.EMPTY_MAP);
  }

  /**
   * @see org.apache.jackrabbit.api.jsr283.security.AccessControlList#removeAccessControlEntry(AccessControlEntry)
   */
  public synchronized void removeAccessControlEntry(AccessControlEntry ace) throws AccessControlException,
      RepositoryException {
    if (!(ace instanceof Entry)) {
      throw new AccessControlException("Invalid AccessControlEntry implementation " + ace.getClass().getName() + ".");
    }
    List l = internalGetEntries(ace.getPrincipal());
    if (l.remove(ace)) {
      if (l.isEmpty()) {
        entries.remove(ace.getPrincipal().getName());
      }
    } else {
      throw new AccessControlException("AccessControlEntry " + ace + " cannot be removed from ACL defined at "
          + getPath());
    }
  }

  //-----------------------------------------------------< JackrabbitAccessControlList >---
  /**
   * @see JackrabbitAccessControlList#getPath()
   */
  public String getPath() {
    return path;
  }

  /**
   * Returns an empty String array.
   *
   * @see JackrabbitAccessControlList#getRestrictionType(String)
   */
  public String[] getRestrictionNames() {
    return new String[0];
  }

  /**
   * Always returns {@link PropertyType#UNDEFINED} as no restrictions are
   * supported.
   *
   * @see JackrabbitAccessControlList#getRestrictionType(String)
   */
  public int getRestrictionType(String restrictionName) {
    return PropertyType.UNDEFINED;
  }

  /**
   * @see JackrabbitAccessControlList#isEmpty()
   */
  public boolean isEmpty() {
    return entries.isEmpty();
  }

  /**
   * @see JackrabbitAccessControlList#size()
   */
  public int size() {
    return internalGetEntries().size();
  }

  /**
   * @see JackrabbitAccessControlList#addEntry(Principal, Privilege[], boolean)
   */
  public boolean addEntry(Principal principal, Privilege[] privileges, boolean isAllow) throws AccessControlException,
      RepositoryException {
    return addEntry(principal, privileges, isAllow, null);
  }

  /**
   * @see JackrabbitAccessControlList#addEntry(Principal, Privilege[], boolean, Map)
   */
  public boolean addEntry(Principal principal, Privilege[] privileges, boolean isAllow, Map restrictions)
      throws AccessControlException, RepositoryException {
    if (restrictions != null && !restrictions.isEmpty()) {
      throw new AccessControlException("This AccessControlList does not allow for additional restrictions.");
    }

    checkValidEntry(principal, privileges, isAllow);
    Entry ace = new Entry(principal, privileges, isAllow, valueFactory);
    return internalAdd(ace);
  }

  //-------------------------------------------------------------< Object >---
  /**
   * Returns zero to satisfy the Object equals/hashCode contract.
   * This class is mutable and not meant to be used as a hash key.
   *
   * @return always zero
   * @see Object#hashCode()
   */
  public int hashCode() {
    return 0;
  }

  /**
   * Returns true if the path and the entries are equal; false otherwise.
   *
   * @param obj Object to be tested.
   * @return true if the path and the entries are equal; false otherwise.
   * @see Object#equals(Object)
   */
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }

    if (obj instanceof PentahoJackrabbitAccessControlList) {
      PentahoJackrabbitAccessControlList acl = (PentahoJackrabbitAccessControlList) obj;
      return path.equals(acl.path) && entries.equals(acl.entries) && owner.equals(acl.owner)
          && entriesInheriting == acl.entriesInheriting;
    }
    return false;
  }

  //--------------------------------------------------------------------------
  /**
   *
   */
  static class Entry extends AccessControlEntryImpl {

    Entry(Principal principal, Privilege[] privileges, boolean allow, ValueFactory valueFactory)
        throws AccessControlException {
      super(principal, privileges, allow, Collections.EMPTY_MAP, valueFactory);
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
      StringBuilder buf = new StringBuilder();
      buf.append(getClass().getName()).append("[").append("principal").append("=").append(getPrincipal().getName())
          .append(",").append("bits").append("=").append(getPrivilegeBits()).append(",").append("allow").append("=")
          .append(isAllow()).append("]");
      return buf.toString();
    }

  }

  public Principal getOwner() {
    return owner;
  }

  public boolean isEntriesInheriting() {
    return entriesInheriting;
  }

  @Override
  public String toString() {
    return "PentahoJackrabbitAccessControlList[entries=" + entries + ", owner=" + owner + ", entriesInheriting="  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
        + entriesInheriting + "]"; //$NON-NLS-1$
  }

  public void setOwner(Principal owner) {
    this.owner = owner;
  }

  public void setEntriesInheriting(boolean entriesInheriting) {
    this.entriesInheriting = entriesInheriting;
  }
}
