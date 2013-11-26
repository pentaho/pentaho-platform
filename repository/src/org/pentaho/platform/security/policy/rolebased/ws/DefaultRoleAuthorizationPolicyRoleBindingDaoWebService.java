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

package org.pentaho.platform.security.policy.rolebased.ws;

import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.pentaho.platform.security.policy.rolebased.RoleBindingStruct;
import org.pentaho.platform.security.policy.rolebased.messages.Messages;

import javax.jws.WebService;
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
}
