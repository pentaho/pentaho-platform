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


package org.pentaho.platform.security.policy.rolebased.ws;

import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.pentaho.platform.security.policy.rolebased.RoleBindingStruct;
import org.pentaho.platform.security.policy.rolebased.messages.Messages;

import jakarta.jws.WebService;
import java.util.List;

/**
 * Implementation of {@link IRoleAuthorizationPolicyRoleBindingDaoWebService} that delegates to an
 * {@link IRoleAuthorizationPolicyRoleBindingDao} instance.
 * 
 * @author mlowery
 */
@WebService(
    endpointInterface = "org.pentaho.platform.security.policy.rolebased.ws."
      + "IRoleAuthorizationPolicyRoleBindingDaoWebService",
    serviceName = "roleBindingDao", portName = "roleBindingDaoPort", targetNamespace = "http://www.pentaho.org/ws/1.0" )
public class DefaultRoleAuthorizationPolicyRoleBindingDaoWebService implements
    IRoleAuthorizationPolicyRoleBindingDaoWebService {

  // ~ Static fields/initializers
  // ======================================================================================

  // ~ Instance fields
  // =================================================================================================

  private IRoleAuthorizationPolicyRoleBindingDao roleBindingDao;

  // ~ Constructors
  // ====================================================================================================

  /**
   * No-arg constructor for when in Pentaho BI Server.
   */
  public DefaultRoleAuthorizationPolicyRoleBindingDaoWebService() {
    super();
    roleBindingDao = PentahoSystem.get( IRoleAuthorizationPolicyRoleBindingDao.class );
    if ( roleBindingDao == null ) {
      throw new IllegalStateException( Messages.getInstance().getString(
          "DefaultRoleAuthorizationPolicyRoleBindingDaoWebService.ERROR_0001_MISSING_ROLE_BINDING_DAO" ) ); //$NON-NLS-1$
    }
  }

  public DefaultRoleAuthorizationPolicyRoleBindingDaoWebService(
      final IRoleAuthorizationPolicyRoleBindingDao roleBindingDao ) {
    super();
    this.roleBindingDao = roleBindingDao;
  }

  // ~ Methods
  // =========================================================================================================

  @Override
  public RoleBindingStruct getRoleBindingStruct( final String locale ) {
    return roleBindingDao.getRoleBindingStruct( locale );
  }

  @Override
  public List<String> getBoundLogicalRoleNames( final List<String> runtimeRoleNames ) {
    return roleBindingDao.getBoundLogicalRoleNames( runtimeRoleNames );
  }

  @Override
  public void setRoleBindings( final String runtimeRoleName, final List<String> logicalRolesNames ) {
    roleBindingDao.setRoleBindings( runtimeRoleName, logicalRolesNames );
  }

  @Override
  public List<String> getBoundLogicalRoleNamesForTenant( Tenant tenant, List<String> runtimeRoleNames ) {
    return roleBindingDao.getBoundLogicalRoleNames( tenant, runtimeRoleNames );
  }

  @Override
  public RoleBindingStruct getRoleBindingStructForTenant( Tenant tenant, String locale ) {
    return roleBindingDao.getRoleBindingStruct( tenant, locale );
  }

  @Override
  public void setRoleBindingsForTenant( Tenant tenant, String runtimeRoleName, List<String> logicalRolesNames ) {
    roleBindingDao.setRoleBindings( tenant, runtimeRoleName, logicalRolesNames );
  }

  @Override
  public void logout() {
    // no-op, handled in PentahoWSSpringServlet
  }
}
