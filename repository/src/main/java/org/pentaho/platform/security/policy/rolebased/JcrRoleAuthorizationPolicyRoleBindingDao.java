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
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.security.policy.rolebased;

import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.pentaho.platform.api.engine.security.userroledao.NotFoundException;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.engine.core.system.TenantUtils;
import org.pentaho.platform.repository2.unified.jcr.JcrTenantUtils;
import org.springframework.extensions.jcr.JcrCallback;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.util.Assert;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;
import java.util.*;

/**
 * An {@link IRoleAuthorizationPolicyRoleBindingDao} implementation that uses JCR. Storage is done using nodes and
 * properties, not XML. Storage looks like this:
 * 
 * <pre>
 * {@code 
 * - acme
 *   - .authz
 *     - roleBased
 *       - runtimeRoles
 *         - runtimeRole1
 *           - logicalRole1,logicalRole2 (multi-valued property)
 *         - runtimeRole2
 *           - logicalRole2 (multi-valued property)
 * }
 * </pre>
 * 
 * <p>
 * Note: All multi-valued properties are ordered.
 * </p>
 * 
 * <p>
 * Note: This code runs as the repository superuser. Ideally this would run as the tenant admin but such a named
 * user doesn't exist for us to run as. Now that the repo uses IAuthorizationPolicy for access control, this code
 * MUST continue to run as the repository superuser. This is one reason not to implement this on top of PUR.
 * </p>
 * 
 * @author mlowery
 */
public class JcrRoleAuthorizationPolicyRoleBindingDao extends AbstractJcrBackedRoleBindingDao {

  // ~ Static fields/initializers
  // ======================================================================================

  // ~ Instance fields
  // =================================================================================================

  private JcrTemplate jcrTemplate;

  // ~ Constructors
  // ====================================================================================================

  public JcrRoleAuthorizationPolicyRoleBindingDao( final JcrTemplate jcrTemplate, final Map<String, List<IAuthorizationAction>> immutableRoleBindings,
      final Map<String, List<String>> bootstrapRoleBindings, final String superAdminRoleName,
      final ITenantedPrincipleNameResolver tenantedRoleNameUtils, final List<IAuthorizationAction> authorizationActions ) {
    super(immutableRoleBindings, bootstrapRoleBindings, superAdminRoleName, tenantedRoleNameUtils,
        authorizationActions );
    Assert.notNull( jcrTemplate );
    this.jcrTemplate = jcrTemplate;
  }

  // ~ Methods
  // =========================================================================================================

  /**
   * {@inheritDoc}
   */
  @Override
  public RoleBindingStruct getRoleBindingStruct( final String locale ) {
    return (RoleBindingStruct) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        return getRoleBindingStruct( session, null, locale );
      }
    } );
  }

  @Override
  public RoleBindingStruct getRoleBindingStruct( final ITenant tenant, final String locale ) {
    if ( ( tenant != null ) && !TenantUtils.isAccessibleTenant( tenant ) ) {
      return new RoleBindingStruct( new HashMap<String, String>(), new HashMap<String, List<String>>(), new HashSet<String>() );
    }
    return (RoleBindingStruct) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        return getRoleBindingStruct( session, tenant, locale );
      }
    } );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setRoleBindings( final String runtimeRoleName, final List<String> logicalRoleNames ) {
    setRoleBindings( (ITenant) null, runtimeRoleName, logicalRoleNames );
  }

  @Override
  public void setRoleBindings( final ITenant tenant, final String runtimeRoleName,
                               final List<String> logicalRoleNames ) {
    ITenant tempTenant = tenant;
    if ( tenant == null ) {
      tempTenant = JcrTenantUtils.getTenant( runtimeRoleName, false );
    }
    if ( !TenantUtils.isAccessibleTenant( tempTenant ) ) {
      throw new NotFoundException( "Tenant " + tenant.getId() + " not found" );
    }
    Assert.notNull( logicalRoleNames );
    jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        setRoleBindings( session, tenant, runtimeRoleName, logicalRoleNames );
        return null;
      }
    } );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings( "unchecked" )
  public List<String> getBoundLogicalRoleNames( final List<String> runtimeRoleNames ) {
    // what runtimeRoleNames are in the cache; we don't need to fetch them
    return (List<String>) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        return getBoundLogicalRoleNames( session, runtimeRoleNames );
      }
    } );
  }

  @Override
  public List<String> getBoundLogicalRoleNames( final ITenant tenant, final List<String> runtimeRoleNames ) {
    if ( ( tenant != null ) && !TenantUtils.isAccessibleTenant( tenant ) ) {
      return new ArrayList<String>();
    }
    // what runtimeRoleNames are in the cache; we don't need to fetch them
    return (List<String>) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        return getBoundLogicalRoleNames( session, tenant, runtimeRoleNames );
      }
    } );
  }
}
