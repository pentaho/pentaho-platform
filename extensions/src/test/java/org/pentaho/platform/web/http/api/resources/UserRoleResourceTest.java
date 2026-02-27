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


package org.pentaho.platform.web.http.api.resources;

import junit.framework.TestCase;

import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantManager;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.pentaho.platform.security.policy.rolebased.RoleBindingStruct;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Class Description
 * 
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public class UserRoleResourceTest extends TestCase {

  public void testConstruction() throws Exception {
    // Only one way to construct now. Normally spring injected
    String adminRole = "Administrator";
    ArrayList<String> systemRoles = new ArrayList<String>();
    systemRoles.add( adminRole );
    systemRoles.add( "Anonymous" );
    systemRoles.add( "Authenticated" );
    new UserRoleDaoResource( new MockRoleAuthorizationPolicyRoleBindingDao(), new MockTenantManager(), systemRoles,
        "Administrator" );
  }

  public void getAllUsers() throws Exception {

  }

  private class MockRoleAuthorizationPolicyRoleBindingDao implements IRoleAuthorizationPolicyRoleBindingDao {
    @Override
    public RoleBindingStruct getRoleBindingStruct( final String locale ) {
      return null;
    }

    @Override
    public void setRoleBindings( final String runtimeRoleName, final List<String> logicalRolesNames ) {
    }

    @Override
    public List<String> getBoundLogicalRoleNames( final List<String> runtimeRoleNames ) {
      return null;
    }

    @Override
    public RoleBindingStruct getRoleBindingStruct( ITenant tenant, String locale ) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public void setRoleBindings( ITenant tenant, String runtimeRoleName, List<String> logicalRolesNames ) {
      // TODO Auto-generated method stub

    }

    @Override
    public List<String> getBoundLogicalRoleNames( ITenant tenant, List<String> runtimeRoleNames ) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public List<String> getBoundLogicalRoleNames( Session session, List<String> runtimeRoleNames ) throws RepositoryException {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public List<String> getBoundLogicalRoleNames( Session session, ITenant tenant, List<String> runtimeRoleNames ) throws RepositoryException {
      // TODO Auto-generated method stub
      return null;
    }
  }

  private class MockTenantManager implements ITenantManager {

    @Override
    public List<ITenant> getChildTenants( ITenant parentTenant ) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public List<ITenant> getChildTenants( ITenant parentTenant, boolean includeDisabledTenants ) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public void updateTentant( String tenantPath, Map<String, Serializable> tenantInfo ) {
      // TODO Auto-generated method stub

    }

    @Override
    public void deleteTenant( ITenant tenant ) {
      // TODO Auto-generated method stub

    }

    @Override
    public void deleteTenants( List<ITenant> tenantPaths ) {
      // TODO Auto-generated method stub

    }

    @Override
    public void enableTenant( ITenant tenant, boolean enable ) {
      // TODO Auto-generated method stub

    }

    @Override
    public void enableTenants( List<ITenant> tenantPaths, boolean enable ) {
      // TODO Auto-generated method stub

    }

    @Override
    public boolean isSubTenant( ITenant parentTenant, ITenant descendantTenant ) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public RepositoryFile getTenantRootFolder( ITenant tenant ) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public ITenant getTenant( String tenantId ) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public ITenant createTenant( ITenant arg0, String arg1, String arg2, String arg3, String arg4 ) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public ITenant getTenantByRootFolderPath( String arg0 ) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public RepositoryFile createUserHomeFolder( ITenant tenant, String username ) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public RepositoryFile getUserHomeFolder( ITenant arg0, String arg1 ) {
      // TODO Auto-generated method stub
      return null;
    }

  }
}
