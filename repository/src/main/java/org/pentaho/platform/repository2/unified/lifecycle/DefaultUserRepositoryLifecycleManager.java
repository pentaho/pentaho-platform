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

package org.pentaho.platform.repository2.unified.lifecycle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.util.IPasswordService;
import org.pentaho.platform.api.util.PasswordServiceException;
import org.pentaho.platform.repository2.unified.jcr.IPathConversionHelper;
import org.pentaho.platform.repository2.unified.jcr.JcrTenantUtils;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DefaultUserRepositoryLifecycleManager extends AbstractBackingRepositoryLifecycleManager {

  private static final Log logger = LogFactory.getLog( DefaultUserRepositoryLifecycleManager.class );
  private static final ITenant DEFAULT_TENANT = JcrTenantUtils.getDefaultTenant();
  private static final String[] EMPTY_STRING_ARRAY = new String[] {};

  private final IRoleAuthorizationPolicyRoleBindingDao roleBindingDao;
  private final IPasswordService passwordService;

  private final IUserRoleDao userRoleDao;
  private Map<String, List<String>> roleMappings;
  private Map<String, List<String>> userRoleMappings;
  private String singleTenantAdminPassword;
  private String nonAdminPassword;
  private String singleTenantAdminUserName;
  private List<String> systemRoles;
  public static final String DEFAULT_USERS_LOADED_METADATA = "defaultUsersLoaded";

  public DefaultUserRepositoryLifecycleManager( final IRoleAuthorizationPolicyRoleBindingDao roleBindingDao,
      final IPasswordService passwordService, final IUserRoleDao userRoleDao, final String singleTenantAdminUserName,
      final List<String> systemRoles, final TransactionTemplate txnTemplate, final JcrTemplate adminJcrTemplate,
      final IPathConversionHelper pathConversionHelper ) {
    super( txnTemplate, adminJcrTemplate, pathConversionHelper );
    this.roleBindingDao = roleBindingDao;
    this.passwordService = passwordService;
    this.userRoleDao = userRoleDao;
    this.singleTenantAdminUserName = singleTenantAdminUserName;
    this.systemRoles = systemRoles;
  }

  @Override
  public void newTenant() {
  }

  @Override
  public void newTenant( ITenant arg0 ) {
  }

  @Override
  public void newUser() {
  }

  @Override
  public void newUser( ITenant arg0, String arg1 ) {
  }

  @Override
  public void shutdown() {
  }

  @Override
  public void startup() {
    if ( !doesMetadataExists( DEFAULT_USERS_LOADED_METADATA ) ) {
      configureRoles();
      try {
        configureUsers();
      } catch ( PasswordServiceException e ) {
        logger.error( "Failed configuring users.", e );
      }
      addMetadataToRepository( DEFAULT_USERS_LOADED_METADATA );
    }
  }

  private void configureRoles() {

    if ( logger.isDebugEnabled() ) {
      logger.debug( "Configuring default role mappings." );
    }

    for ( final String roleName : roleMappings.keySet() ) {
      final IPentahoRole role = userRoleDao.getRole( DEFAULT_TENANT, roleName );
      if ( role == null ) {
        if ( logger.isDebugEnabled() ) {
          logger.debug( "Creating user role: " + roleName );
        }
        userRoleDao.createRole( DEFAULT_TENANT, roleName, "", EMPTY_STRING_ARRAY );
        final List<String> logicalRoles = roleMappings.get( roleName );
        if ( logicalRoles.size() > 0 ) {
          roleBindingDao.setRoleBindings( DEFAULT_TENANT, roleName, logicalRoles );
        }
        if ( logger.isDebugEnabled() ) {
          StringBuffer buffer = new StringBuffer();
          for ( String logicalRole : logicalRoles ) {
            buffer.append( logicalRole + " " );
          }
          logger.debug( "Create Role[" + roleName + "] with logical roles [ " + buffer + " ]" );
        }

      } else {
        if ( logger.isDebugEnabled() ) {
          logger.debug( "Skipping config. Role[" + roleName + "] already registered." );
        }
      }
    }
  }

  private void configureUsers() throws PasswordServiceException {

    String singleTenantAdminPlainTextPassword = passwordService.decrypt( singleTenantAdminPassword );
    String nonAdminPasswordPlainTextPassword = passwordService.decrypt( nonAdminPassword );

    for ( final String userName : userRoleMappings.keySet() ) {

      final IPentahoUser user = userRoleDao.getUser( DEFAULT_TENANT, userName );

      if ( user == null ) {
        StringBuffer buffer = new StringBuffer();
        if ( logger.isDebugEnabled() ) {
          logger.debug( "Creating user: " + userName );
        }

        List<String> roleNames = new ArrayList<String>();

        for ( String roleName : userRoleMappings.get( userName ) ) {

          if ( roleMappings.containsKey( roleName ) || systemRoles.contains( roleName ) ) {
            roleNames.add( roleName );
            buffer.append( roleName + "  " );
          } else {
            logger.error( "Unable to map undefined role to user. User[" + userName + "] Role[" + roleName + "]" );
          }
        }

        if ( singleTenantAdminUserName.equals( userName ) ) {
          userRoleDao.createUser( DEFAULT_TENANT, userName, singleTenantAdminPlainTextPassword, "user", roleNames
              .toArray( EMPTY_STRING_ARRAY ) );
        } else {
          userRoleDao.createUser( DEFAULT_TENANT, userName, nonAdminPasswordPlainTextPassword, "user", roleNames
              .toArray( EMPTY_STRING_ARRAY ) );
        }
        if ( logger.isDebugEnabled() ) {
          logger.debug( "Created user: " + userName + "with role mappings [" + buffer + "]" );
        }
      }
    }
  }

  public Map<String, List<String>> getRoleMappings() {
    return roleMappings;
  }

  public void setRoleMappings( Map<String, List<String>> roleMappings ) {
    this.roleMappings = roleMappings;
  }

  public Map<String, List<String>> getUserRoleMappings() {
    return userRoleMappings;
  }

  public void setUserRoleMappings( Map<String, List<String>> userRoleMappings ) {
    this.userRoleMappings = userRoleMappings;
  }

  public String getNonAdminPassword() {
    return nonAdminPassword;
  }

  public void setNonAdminPassword( String nonAdminPassword ) {
    this.nonAdminPassword = nonAdminPassword;
  }

  public String getSingleTenantAdminPassword() {
    return singleTenantAdminPassword;
  }

  public void setSingleTenantAdminPassword( String singleTenantAdminPassword ) {
    this.singleTenantAdminPassword = singleTenantAdminPassword;
  }
}
