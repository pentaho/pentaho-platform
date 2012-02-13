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

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.api.jsr283.security.Privilege;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.security.authorization.PrivilegeRegistry;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.repository2.unified.jcr.jackrabbit.JackrabbitRepositoryFileAclDao.IPermissionConversionHelper;
import org.springframework.util.Assert;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * Default {@link IPermissionConversionHelper} implementation.
 * 
 * @author mlowery
 */
public class DefaultPermissionConversionHelper implements IPermissionConversionHelper {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(DefaultPermissionConversionHelper.class);

  // ~ Instance fields =================================================================================================

  protected Multimap<RepositoryFilePermission, String> permissionEnumToPrivilegeNamesMap;

  protected Multimap<String, RepositoryFilePermission> privilegeNameToPermissionEnumsMap;

  // ~ Constructors ====================================================================================================

  public DefaultPermissionConversionHelper() {
    super();
    initMaps();

  }

  // ~ Methods =========================================================================================================

  public Privilege[] pentahoPermissionsToJackrabbitPrivileges(final SessionImpl jrSession,
      final EnumSet<RepositoryFilePermission> permissions) throws RepositoryException {
    Assert.notNull(jrSession);
    Assert.notNull(permissions);
    Assert.notEmpty(permissions);
    PrivilegeRegistry privilegeRegistry = new PrivilegeRegistry(jrSession);

    Set<Privilege> privileges = new HashSet<Privilege>();

    for (RepositoryFilePermission currentPermission : permissions) {
      if (permissionEnumToPrivilegeNamesMap.containsKey(currentPermission)) {
        Collection<String> privNames = permissionEnumToPrivilegeNamesMap.get(currentPermission);
        for (String privName : privNames) {
          privileges.add(privilegeRegistry.getPrivilege(privName));
        }
      } else {
        logger.debug("skipping permission=" + currentPermission + " as it doesn't have any corresponding privileges");  //$NON-NLS-1$//$NON-NLS-2$
      }
    }

    Assert.isTrue(!privileges.isEmpty(), "no privileges; see previous 'skipping permission' messages");

    return privileges.toArray(new Privilege[0]);
  }

  public EnumSet<RepositoryFilePermission> jackrabbitPrivilegesToPentahoPermissions(final SessionImpl jrSession,
      final Privilege[] privileges) throws RepositoryException {
    Assert.notNull(jrSession);
    Assert.notNull(privileges);

    EnumSet<RepositoryFilePermission> permissions = EnumSet.noneOf(RepositoryFilePermission.class);

    for (Privilege privilege : privileges) {
      // this privilege name is of the format xyz:blah where xyz is the namespace prefix;
      // convert it to match the Privilege.JCR_* string constants from Jackrabbit
      String extendedPrivilegeName = privilege.getName();
      String privilegeName = privilege.getName();
      int colonIndex = privilegeName.indexOf(":"); //$NON-NLS-1$
      if (colonIndex > -1) {
        String namespaceUri = jrSession.getNamespaceURI(privilegeName.substring(0, colonIndex));
        extendedPrivilegeName = "{" + namespaceUri + "}" + privilegeName.substring(colonIndex + 1); //$NON-NLS-1$ //$NON-NLS-2$
      }

      if (privilegeNameToPermissionEnumsMap.containsKey(extendedPrivilegeName)) {
        Collection<RepositoryFilePermission> permEnums = privilegeNameToPermissionEnumsMap.get(extendedPrivilegeName);
        for (RepositoryFilePermission perm : permEnums) {
          permissions.add(perm);
        }
      } else {
        logger.debug("skipping privilege with name=" + extendedPrivilegeName //$NON-NLS-1$
            + " as it doesn't have any corresponding permissions"); //$NON-NLS-1$
      }
    }

    Assert.isTrue(!permissions.isEmpty(), "no permissions; see previous 'skipping privilege' messages");

    return permissions;
  }

  protected void initMaps() {
    permissionEnumToPrivilegeNamesMap = HashMultimap.create();

    // READ
    permissionEnumToPrivilegeNamesMap.put(RepositoryFilePermission.READ, Privilege.JCR_READ);
    // WRITE
    permissionEnumToPrivilegeNamesMap.put(RepositoryFilePermission.WRITE, PrivilegeRegistry.REP_WRITE);
    permissionEnumToPrivilegeNamesMap.put(RepositoryFilePermission.WRITE, Privilege.JCR_VERSION_MANAGEMENT);
    permissionEnumToPrivilegeNamesMap.put(RepositoryFilePermission.WRITE, Privilege.JCR_LOCK_MANAGEMENT);
    // READ_ACL
    permissionEnumToPrivilegeNamesMap.put(RepositoryFilePermission.READ_ACL, Privilege.JCR_READ_ACCESS_CONTROL);
    // WRITE_ACL
    permissionEnumToPrivilegeNamesMap.put(RepositoryFilePermission.WRITE_ACL, Privilege.JCR_MODIFY_ACCESS_CONTROL);
    // ALL
    permissionEnumToPrivilegeNamesMap.put(RepositoryFilePermission.ALL, Privilege.JCR_ALL);

    privilegeNameToPermissionEnumsMap = HashMultimap.create();
    // JCR_READ
    privilegeNameToPermissionEnumsMap.put(Privilege.JCR_READ, RepositoryFilePermission.READ);
    // JCR_WRITE
    privilegeNameToPermissionEnumsMap.put(Privilege.JCR_WRITE, RepositoryFilePermission.WRITE);
    // REP_WRITE (Jackrabbit's combination of Privilege.JCR_WRITE and Privilege.JCR_NODE_TYPE_MNGMT
    privilegeNameToPermissionEnumsMap.put(PrivilegeRegistry.REP_WRITE, RepositoryFilePermission.WRITE);
    // JCR_READ_ACCESS_CONTROL
    privilegeNameToPermissionEnumsMap.put(Privilege.JCR_READ_ACCESS_CONTROL, RepositoryFilePermission.READ_ACL);
    // JCR_MODIFY_ACCESS_CONTROL
    privilegeNameToPermissionEnumsMap.put(Privilege.JCR_MODIFY_ACCESS_CONTROL, RepositoryFilePermission.WRITE_ACL);
    // JCR_ALL
    privilegeNameToPermissionEnumsMap.put(Privilege.JCR_ALL, RepositoryFilePermission.ALL);

    // None of the following translate into a RepositoryFilePermission:
    // JCR_NODE_TYPE_MANAGEMENT
    // JCR_VERSION_MANAGEMENT
    // JCR_LOCK_MANAGEMENT
  }

}
