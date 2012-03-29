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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.jcr.Session;

import org.apache.jackrabbit.api.security.principal.PrincipalIterator;
import org.apache.jackrabbit.core.security.AnonymousPrincipal;
import org.apache.jackrabbit.core.security.SecurityConstants;
import org.apache.jackrabbit.core.security.UserPrincipal;
import org.apache.jackrabbit.core.security.principal.AdminPrincipal;
import org.apache.jackrabbit.core.security.principal.EveryonePrincipal;
import org.apache.jackrabbit.core.security.principal.PrincipalIteratorAdapter;
import org.apache.jackrabbit.core.security.principal.PrincipalProvider;
import org.pentaho.platform.repository2.unified.jcr.JcrRepositoryFileAclUtils;
import org.pentaho.platform.repository2.unified.jcr.JcrAclMetadataStrategy.AclMetadataPrincipal;

/**
 * PrincipalProvider for unit test purposes. Has joe and the other Pentaho users. In addition, it has the Jackrabbit 
 * principals "everyone", "admin", and "anonymous".
 * 
 * <p>
 * Some parts copied from SimplePrincipalProvider.
 * </p>
 * 
 * @author mlowery
 */
@SuppressWarnings("nls")
public class TestPrincipalProvider implements PrincipalProvider {

  // ~ Static fields/initializers ======================================================================================

  private static boolean georgeAndDuffEnabled = true;

  // ~ Instance fields =================================================================================================

  private Map<String, Principal> principals = new HashMap<String, Principal>();

  private String adminId;

  private AdminPrincipal adminPrincipal;

  private String anonymousId;

  private AnonymousPrincipal anonymousPrincipal = new AnonymousPrincipal();

  private String adminRole;

  private SpringSecurityRolePrincipal adminRolePrincipal;

  private static final String KEY_ADMIN_ID = "adminId"; //$NON-NLS-1$

  private static final String KEY_ANONYMOUS_ID = "anonymousId"; //$NON-NLS-1$

  private static final String KEY_ADMIN_ROLE = "adminRole"; //$NON-NLS-1$

  // ~ Constructors ====================================================================================================

  public TestPrincipalProvider() {
    super();
  }

  // ~ Methods =========================================================================================================

  /**
   * {@inheritDoc}
   */
  public void init(Properties options) {
    adminId = options.getProperty(KEY_ADMIN_ID, SecurityConstants.ADMIN_ID);
    adminPrincipal = new AdminPrincipal(adminId);
    anonymousId = options.getProperty(KEY_ANONYMOUS_ID, SecurityConstants.ANONYMOUS_ID);
    adminRole = options.getProperty(KEY_ADMIN_ROLE, SecurityConstants.ADMINISTRATORS_NAME);
    adminRolePrincipal = new SpringSecurityRolePrincipal(adminRole);

    principals.put(adminId, adminPrincipal);
    principals.put(adminRole, adminRolePrincipal);
    principals.put(anonymousId, anonymousPrincipal);

    EveryonePrincipal everyone = EveryonePrincipal.getInstance();
    principals.put(everyone.getName(), everyone);

    principals.put("joe", new UserPrincipal("joe"));
    principals.put("suzy", new UserPrincipal("suzy"));
    principals.put("tiffany", new UserPrincipal("tiffany"));
    principals.put("pat", new UserPrincipal("pat"));
    principals.put("george", new UserPrincipal("george"));
    principals.put("Authenticated", new SpringSecurityRolePrincipal("Authenticated"));
    principals.put("acme_Authenticated", new SpringSecurityRolePrincipal("acme_Authenticated"));
    principals.put("acme_Admin", new SpringSecurityRolePrincipal("acme_Admin"));
    principals.put("duff_Authenticated", new SpringSecurityRolePrincipal("duff_Authenticated"));
    principals.put("duff_Admin", new SpringSecurityRolePrincipal("duff_Admin"));

  }

  public static void enableGeorgeAndDuff(final boolean enabled) {
    TestPrincipalProvider.georgeAndDuffEnabled = enabled;
  }

  /**
   * {@inheritDoc}
   */
  public void close() {
    // nothing to do
  }

  /**
   * {@inheritDoc}
   */
  public boolean canReadPrincipal(Session session, Principal principal) {
    return true;
  }

  /**
   * {@inheritDoc}
   */
  public Principal getPrincipal(String principalName) {
    if (AclMetadataPrincipal.isAclMetadataPrincipal(principalName)) {
      return new AclMetadataPrincipal(principalName);
    }
    if ("george".equals(principalName)) {
      if (georgeAndDuffEnabled) {
        return principals.get(principalName);
      } else {
        return null;
      }
    } else if ("duff_Authenticated".equals(principalName) || "duff_Admin".equals(principalName)) {
      if (georgeAndDuffEnabled) {
        return principals.get(principalName);
      } else {
        return null;
      }
    } else if (principals.containsKey(principalName)) {
      return principals.get(principalName);
    } else {
      return null;
    }
  }

  /**
   * {@inheritDoc}
   * 
   * <p>
   * Called from {@code AbstractLoginModule.getPrincipals()}
   * </p>
   */
  public PrincipalIterator getGroupMembership(Principal principal) {
    if (principal instanceof EveryonePrincipal) {
      return PrincipalIteratorAdapter.EMPTY;
    }
    if (principal instanceof AclMetadataPrincipal) {
      return PrincipalIteratorAdapter.EMPTY;
    }

    Set<Principal> principals = new HashSet<Principal>();
    if (principal.getName().equals("joe") || principal.getName().equals("suzy")
        || principal.getName().equals("tiffany")) {
      principals.add(new SpringSecurityRolePrincipal("Authenticated"));
      principals.add(new SpringSecurityRolePrincipal("acme_Authenticated"));
    } else if (principal.getName().equals("pat") || principal.getName().equals("george")) {
      principals.add(new SpringSecurityRolePrincipal("Authenticated"));
      principals.add(new SpringSecurityRolePrincipal("duff_Authenticated"));
    } else if (principal.getName().equals(adminId)) {
      principals.add(adminRolePrincipal);
    }
    if (principal.getName().equals("joe")) {
      principals.add(new SpringSecurityRolePrincipal("acme_Admin"));
    }
    if (principal.getName().equals("george")) {
      principals.add(new SpringSecurityRolePrincipal("duff_Admin"));
    }

    principals.add(EveryonePrincipal.getInstance());
    return new PrincipalIteratorAdapter(principals);
  }

  /**
   * {@inheritDoc}
   * 
   * <p>
   * Not implemented. This method only ever called from method in {@code PrincipalManagerImpl} and that method is never 
   * called.
   * </p>
   */
  public PrincipalIterator findPrincipals(String simpleFilter) {
    throw new UnsupportedOperationException("not implemented");
  }

  /**
   * {@inheritDoc}
   * 
   * <p>
   * Not implemented. This method only ever called from method in {@code PrincipalManagerImpl} and that method is never 
   * called.
   * </p>
   */
  public PrincipalIterator findPrincipals(String simpleFilter, int searchType) {
    throw new UnsupportedOperationException("not implemented");
  }

  /**
   * {@inheritDoc}
   * 
   * <p>
   * Not implemented. This method only ever called from method in {@code PrincipalManagerImpl} and that method is never 
   * called.
   * </p>
   */
  public PrincipalIterator getPrincipals(int searchType) {
    throw new UnsupportedOperationException("not implemented");
  }
}
