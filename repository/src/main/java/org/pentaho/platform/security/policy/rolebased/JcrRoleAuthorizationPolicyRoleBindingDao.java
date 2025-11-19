/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.security.policy.rolebased;

import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.pentaho.platform.api.engine.security.authorization.caching.IAuthorizationDecisionCache;
import org.pentaho.platform.api.engine.security.userroledao.NotFoundException;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.engine.core.system.TenantUtils;
import org.pentaho.platform.engine.security.authorization.core.AuthorizationRole;
import org.pentaho.platform.repository2.unified.jcr.JcrTenantUtils;
import org.springframework.extensions.jcr.JcrCallback;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.util.Assert;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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

  private final JcrTemplate jcrTemplate;

  private final IAuthorizationDecisionCache decisionCache;

  // ~ Constructors
  // ====================================================================================================

  public JcrRoleAuthorizationPolicyRoleBindingDao( final JcrTemplate jcrTemplate,
                                                   final Map<String, List<IAuthorizationAction>> immutableRoleBindings,
                                                   final Map<String, List<String>> bootstrapRoleBindings,
                                                   final String superAdminRoleName,
                                                   final ITenantedPrincipleNameResolver tenantedRoleNameUtils,
                                                   final List<IAuthorizationAction> authorizationActions ) {
    this( jcrTemplate, immutableRoleBindings, bootstrapRoleBindings, superAdminRoleName, tenantedRoleNameUtils,
      authorizationActions, null );
  }

  public JcrRoleAuthorizationPolicyRoleBindingDao( final JcrTemplate jcrTemplate,
                                                   final Map<String, List<IAuthorizationAction>> immutableRoleBindings,
                                                   final Map<String, List<String>> bootstrapRoleBindings,
                                                   final String superAdminRoleName,
                                                   final ITenantedPrincipleNameResolver tenantedRoleNameUtils,
                                                   final List<IAuthorizationAction> authorizationActions,
                                                   final IAuthorizationDecisionCache decisionCache ) {
    super( immutableRoleBindings, bootstrapRoleBindings, superAdminRoleName, tenantedRoleNameUtils,
      authorizationActions );
    Assert.notNull( jcrTemplate, "The JCR template must not be null. Ensure a valid JCR template is provided." );
    this.jcrTemplate = jcrTemplate;
    this.decisionCache = decisionCache;
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
      return new RoleBindingStruct( new HashMap<String, String>(), new HashMap<String, List<String>>(),
        new HashSet<String>() );
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
    Assert.notNull( logicalRoleNames,
      "The logical role names list must not be null. Ensure a valid list is provided." );
    jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        setRoleBindings( session, tenant, runtimeRoleName, logicalRoleNames );
        return null;
      }
    } );

    invalidateDecisionCacheForRole( runtimeRoleName );
  }

  protected void invalidateDecisionCacheForRole( String runtimeRoleName ) {
    if ( decisionCache != null ) {
      // Invalidate all authorization requests which have a related role equal to the runtime-role.
      // Regarding actions / logical-roles, because derived action rules exist, it would not be enough to invalidate
      // requests which also reference the logical-roles being changed. So this condition is left out, at the cost of
      // invalidating more requests than strictly necessary.
      var role = new AuthorizationRole( runtimeRoleName );

      decisionCache.invalidateAll( key ->
        key.getRequest().getAllRoles().contains( role )
      );
    }
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
