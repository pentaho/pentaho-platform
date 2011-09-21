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

import java.security.Principal;
import java.security.acl.Group;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.jackrabbit.api.security.principal.NoSuchPrincipalException;
import org.apache.jackrabbit.core.SessionImpl;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.springframework.util.Assert;

/**
 * Default {@code IOwnerLookupHelper} implementation. Uses Jackrabbit-specific node types. Uses low-level node 
 * operations to keep the fetching of the owner fast. (Otherwise, we could have used the {@code AccessControlManager} 
 * API but that would entail fetching the entire ACL along with its ACEs.)
 * 
 * <p>
 * This implementation fails silently (but returns {@code null}) when there is no ACL yet applied to the node.
 * </p>
 * 
 * @author mlowery
 */
public class DefaultOwnerLookupHelper implements IOwnerLookupHelper {

  // ~ Static fields/initializers ======================================================================================

  /**
   * Copy of PentahoJackrabbitAccessControlList.PRINCIPAL_TYPE_ROLE.
   */
  private static final String PRINCIPAL_TYPE_ROLE = "role"; //$NON-NLS-1$

  // ~ Instance fields =================================================================================================

  // ~ Constructors ====================================================================================================

  public DefaultOwnerLookupHelper() {
    super();
  }

  // ~ Methods =========================================================================================================

  public RepositoryFileSid getOwner(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Node node) throws RepositoryException {
    RepositoryFileSid owner = null;
    // TODO mlowery use proper namespaces
    if (node.hasNode("rep:policy")) { //$NON-NLS-1$
      Node aclNode = node.getNode("rep:policy"); //$NON-NLS-1$
      final String aclOwnerName = aclNode.getProperty(pentahoJcrConstants.getPHO_ACLOWNERNAME()).getString();

      Assert.isTrue(session instanceof SessionImpl);
      SessionImpl jrSession = (SessionImpl) session;

      Principal ownerPrincipal = null;
      try {
        ownerPrincipal = jrSession.getPrincipalManager().getPrincipal(aclOwnerName);
        if (ownerPrincipal instanceof Group) {
          owner = new RepositoryFileSid(ownerPrincipal.getName(), RepositoryFileSid.Type.ROLE);
        } else {
          owner = new RepositoryFileSid(ownerPrincipal.getName(), RepositoryFileSid.Type.USER);
        }
      } catch (NoSuchPrincipalException e) {
        String ownerType = aclNode.getProperty(pentahoJcrConstants.getPHO_ACLOWNERTYPE()).getString();
        if (PRINCIPAL_TYPE_ROLE.equals(ownerType)) {
          owner = new RepositoryFileSid(aclOwnerName, RepositoryFileSid.Type.ROLE);
        } else {
          owner = new RepositoryFileSid(aclOwnerName, RepositoryFileSid.Type.USER);
        }
      }

    }
    return owner;
  }

}
