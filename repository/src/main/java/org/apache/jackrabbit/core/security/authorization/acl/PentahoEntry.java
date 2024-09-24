/*!
 *
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
 * Copyright (c) 2002-2024 Hitachi Vantara. All rights reserved.
 *
 */

package org.apache.jackrabbit.core.security.authorization.acl;

import org.apache.jackrabbit.api.JackrabbitWorkspace;
import org.apache.jackrabbit.api.security.principal.PrincipalManager;
import org.apache.jackrabbit.core.NodeImpl;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.id.NodeId;
import org.apache.jackrabbit.core.security.authorization.AccessControlConstants;
import org.apache.jackrabbit.core.security.authorization.GlobPattern;
import org.apache.jackrabbit.core.security.authorization.PrivilegeBits;
import org.apache.jackrabbit.core.security.authorization.PrivilegeManagerImpl;
import org.apache.jackrabbit.core.value.InternalValue;
import org.apache.jackrabbit.spi.Name;
import org.pentaho.platform.repository2.unified.jcr.jackrabbit.security.SpringSecurityRolePrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Copy-and-paste of {@code org.apache.jackrabbit.core.security.authorization.acl.Entry} in Jackrabbit 2.10.0.
 * This class is in {@code org.apache.jackrabbit.core.security.authorization.acl} package due to the scope of
 * collaborating classes.
 *
 * <p/>
 * <p/>
 * <p> Changes to original: </p> <ul> <li>{@code Entry} has a single private constructor, we changed the
 * scope to public {@code null} {@code nextId}.</li>
 * <p/>
 * </ul>
 */
public class PentahoEntry implements AccessControlConstants {

  private static final Logger log = LoggerFactory.getLogger( ACLTemplate.class );

  private final String principalName;
  private final boolean isGroupEntry;
  private final PrivilegeBits privilegeBits;
  private final boolean isAllow;
  private final NodeId id;
  private final GlobPattern pattern;
  private final boolean hasRestrictions;

  /**
   * https://issues.apache.org/jira/browse/JCR-3882
   *
   * We can't use 'pattern.equals( other.pattern )' for the time being, this is a
   * workaround while the above issue does not get pushed into a stable jackrabbit release
   */

  private final String path;
  private final String restriction;

  /**
   * end workaround
   */



  private int hashCode;

  public PentahoEntry(NodeId id, String principalName, boolean isGroupEntry,
      PrivilegeBits privilegeBits, boolean allow, String path, Value globValue) throws RepositoryException {

    this.principalName = principalName;
    this.isGroupEntry = isGroupEntry;
    this.privilegeBits = privilegeBits;
    this.isAllow = allow;
    this.id = id;
    this.pattern = calculatePattern(path, globValue);
    this.hasRestrictions = (globValue != null);

    /**
     * https://issues.apache.org/jira/browse/JCR-3882
     *
     * We can't use 'pattern.equals( other.pattern )' for the time being, this is a
     * workaround while the above issue does not get pushed into a stable jackrabbit release
     */

    this.path = path;
    this.restriction = globValue != null ? globValue.getString() : null;

    /**
     * end workaround
     */
  }

  public PentahoEntry(NodeId id, String principalName, boolean isGroupEntry,
      PrivilegeBits privilegeBits, boolean allow, String path, Map<Name, Value> restrictions ) throws RepositoryException {

    this.principalName = principalName;
    this.isGroupEntry = isGroupEntry;
    this.privilegeBits = privilegeBits;
    this.isAllow = allow;
    this.id = id;
    this.pattern = calculatePattern( path, ( restrictions != null ? restrictions.get( P_GLOB ) : null ) );
    this.hasRestrictions = ( restrictions != null && restrictions.get( P_GLOB ) != null );

    /**
     * https://issues.apache.org/jira/browse/JCR-3882
     *
     * We can't use 'pattern.equals( other.pattern )' for the time being, this is a
     * workaround while the above issue does not get pushed into a stable jackrabbit release
     */

    this.path = path;
    this.restriction = restrictions != null && restrictions.get( P_GLOB ) != null ?
        restrictions.get( P_GLOB ).getString() : null;

    /**
     * end workaround
     */
  }

  static List<PentahoEntry> readEntries(NodeImpl aclNode, String path) throws RepositoryException {
    if (aclNode == null || !NT_REP_ACL.equals(aclNode.getPrimaryNodeTypeName())) {
      throw new IllegalArgumentException("Node must be of type 'rep:ACL'");
    }
    SessionImpl sImpl = (SessionImpl) aclNode.getSession();
    PrincipalManager principalMgr = sImpl.getPrincipalManager();
    PrivilegeManagerImpl privilegeMgr = (PrivilegeManagerImpl) ((JackrabbitWorkspace) sImpl.getWorkspace()).getPrivilegeManager();

    NodeId nodeId = aclNode.getParentId();

    List<PentahoEntry> entries = new ArrayList<PentahoEntry>();
    // load the entries:
    NodeIterator itr = aclNode.getNodes();
    while (itr.hasNext()) {
      NodeImpl aceNode = (NodeImpl) itr.nextNode();
      try {
        String principalName = aceNode.getProperty(P_PRINCIPAL_NAME).getString();
        boolean isGroupEntry = false;
        Principal princ = principalMgr.getPrincipal(principalName);
        if (princ != null) {
          isGroupEntry = ( princ instanceof SpringSecurityRolePrincipal );
        }

        InternalValue[] privValues = aceNode.getProperty(P_PRIVILEGES).internalGetValues();
        Name[] privNames = new Name[privValues.length];
        for (int i = 0; i < privValues.length; i++) {
          privNames[i] = privValues[i].getName();
        }

        Value globValue = null;
        if (aceNode.hasProperty(P_GLOB)) {
          globValue = aceNode.getProperty(P_GLOB).getValue();
        }

        boolean isAllow = NT_REP_GRANT_ACE.equals(aceNode.getPrimaryNodeTypeName());
        PentahoEntry ace = new PentahoEntry(nodeId, principalName, isGroupEntry, privilegeMgr.getBits(privNames),
            isAllow, path, globValue);
        entries.add(ace);
      } catch (RepositoryException e) {
        log.debug("Failed to build ACE from content. {}", e.getMessage());
      }
    }

    return entries;
  }

  private static GlobPattern calculatePattern(String path, Value globValue) throws RepositoryException {
    if (path == null) {
      return null;
    } else {
      if (globValue == null) {
        return GlobPattern.create(path);
      } else {
        return GlobPattern.create(path, globValue.getString());
      }
    }
  }

  /**
   * @param nodeId
   * @return <code>true</code> if this entry is defined on the node
   * at <code>nodeId</code>
   */
  boolean isLocal(NodeId nodeId) {
    return id != null && id.equals(nodeId);
  }

  /**
   *
   * @param jcrPath
   * @return
   */
  boolean matches(String jcrPath) {
    return pattern != null && pattern.matches(jcrPath);
  }

  PrivilegeBits getPrivilegeBits() {
    return privilegeBits;
  }

  boolean isAllow() {
    return isAllow;
  }

  String getPrincipalName() {
    return principalName;
  }

  boolean isGroupEntry() {
    return isGroupEntry;
  }

  boolean hasRestrictions() {
    return hasRestrictions;
  }

  //-------------------------------------------------------------< Object >---
  /**
   * @see Object#hashCode()
   */
  @Override
  public int hashCode() {
    if (hashCode == -1) {
      int h = 17;
      h = 37 * h + principalName.hashCode();
      h = 37 * h + privilegeBits.hashCode();
      h = 37 * h + Boolean.valueOf(isAllow).hashCode();
      h = 37 * h + pattern.hashCode();
      hashCode = h;
    }
    return hashCode;
  }

  /**
   * @see Object#equals(Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof PentahoEntry) {
      PentahoEntry other = (PentahoEntry) obj;

      return principalName.equals( other.principalName ) &&
          privilegeBits.equals( other.privilegeBits ) &&
          isAllow == other.isAllow &&


          /* pattern.equals( other.pattern ) */

          /**
           * https://issues.apache.org/jira/browse/JCR-3882
           *
           * We can't use 'pattern.equals( other.pattern )' for the time being, this is a
           * workaround while the above issue does not get pushed into a stable jackrabbit release
           */

          (

            path.equals( other.path ) &&
                ( (restriction == null) ? other.restriction == null : restriction.equals(other.restriction) )

          )

          /**
           * end workaround
           */


          ;

    }
    return false;
  }
}
