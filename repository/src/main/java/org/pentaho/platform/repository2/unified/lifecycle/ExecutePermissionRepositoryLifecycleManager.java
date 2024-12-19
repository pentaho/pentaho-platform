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


package org.pentaho.platform.repository2.unified.lifecycle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.repository2.unified.jcr.IPathConversionHelper;
import org.pentaho.platform.repository2.unified.jcr.JcrTenantUtils;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Arrays;
import java.util.List;

public class ExecutePermissionRepositoryLifecycleManager extends AbstractBackingRepositoryLifecycleManager {

  private static final Log logger = LogFactory.getLog( DefaultUserRepositoryLifecycleManager.class );
  private static final ITenant DEFAULT_TENANT = JcrTenantUtils.getDefaultTenant();

  private final IRoleAuthorizationPolicyRoleBindingDao roleBindingDao;

  private List<String> rolesNeedingExecutePermission;

  public static final String EXECUTE_PERMISSION_ADDED_METADATA = "executePermissionAdded";
  public static final String EXECUTE_PERMISSION_NAME = "org.pentaho.repository.execute";

  public ExecutePermissionRepositoryLifecycleManager( final IRoleAuthorizationPolicyRoleBindingDao roleBindingDao,
      final TransactionTemplate txnTemplate, final JcrTemplate adminJcrTemplate,
      final IPathConversionHelper pathConversionHelper ) {
    super( txnTemplate, adminJcrTemplate, pathConversionHelper );
    this.roleBindingDao = roleBindingDao;
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
    if ( !doesMetadataExists( EXECUTE_PERMISSION_ADDED_METADATA ) ) {
      addExecutePermissionToRoles();

      addMetadataToRepository( EXECUTE_PERMISSION_ADDED_METADATA );
    }
  }

  private void addExecutePermissionToRoles() {
    if ( logger.isDebugEnabled() ) {
      logger.debug( "Configuring execute permissions for specified roles." );
    }

    if ( rolesNeedingExecutePermission != null ) {
      for ( String roleNeedingExecutePermission : rolesNeedingExecutePermission ) {
        List<String> roleNeedingExecutePermissionAsList =
            roleBindingDao.getBoundLogicalRoleNames( DEFAULT_TENANT, Arrays.asList( roleNeedingExecutePermission ) );

        if ( roleNeedingExecutePermissionAsList != null ) {
          roleNeedingExecutePermissionAsList.add( EXECUTE_PERMISSION_NAME );
          roleBindingDao.setRoleBindings( DEFAULT_TENANT, roleNeedingExecutePermission, roleNeedingExecutePermissionAsList );
          if ( logger.isDebugEnabled() ) {
            logger.debug( "Adding Execute permission to role: " + roleNeedingExecutePermission );
          }
        }
      }
    }
  }

  public void setRolesNeedingExecutePermission( List<String> rolesNeedingExecutePermission ) {
    this.rolesNeedingExecutePermission = rolesNeedingExecutePermission;
  }

  public List<String> getRolesNeedingExecutePermission() {
    return rolesNeedingExecutePermission;
  }
}
