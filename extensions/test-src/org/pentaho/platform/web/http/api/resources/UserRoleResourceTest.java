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
 * Copyright 2012 Pentaho Corporation. All rights reserved.
 */
package org.pentaho.platform.web.http.api.resources;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantManager;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.pentaho.platform.security.policy.rolebased.RoleBindingStruct;

/**
 * Class Description
 *
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public class UserRoleResourceTest extends TestCase {

  public void testConstruction() throws Exception {
    // Failures
    try {
      new UserRoleResource();
      fail("The use of PentahoSystem should throw an exception since it has not been setup correctly");
    } catch (Throwable success) {
    }

    // Successful
    new UserRoleResource(new MockRoleAuthorizationPolicyRoleBindingDao(), new MockTenantManager());

    PentahoSystem.setObjectFactory(new MockPentahoObjectFactory());
    new UserRoleResource();
  }

  public void getAllUsers() throws Exception {

  }


  private class MockRoleAuthorizationPolicyRoleBindingDao implements IRoleAuthorizationPolicyRoleBindingDao {
    @Override
    public RoleBindingStruct getRoleBindingStruct(final String locale) {
      return null;
    }

    @Override
    public void setRoleBindings(final String runtimeRoleName, final List<String> logicalRolesNames) {
    }

    @Override
    public List<String> getBoundLogicalRoleNames(final List<String> runtimeRoleNames) {
      return null;
    }

    @Override
    public RoleBindingStruct getRoleBindingStruct(ITenant tenant, String locale) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public void setRoleBindings(ITenant tenant, String runtimeRoleName, List<String> logicalRolesNames) {
      // TODO Auto-generated method stub
      
    }

    @Override
    public List<String> getBoundLogicalRoleNames(ITenant tenant, List<String> runtimeRoleNames) {
      // TODO Auto-generated method stub
      return null;
    }
  }


  private class MockPentahoObjectFactory implements IPentahoObjectFactory {
    @Override
    public <T> T get(final Class<T> interfaceClass, final IPentahoSession session) throws ObjectFactoryException {
      if (interfaceClass.equals(IRoleAuthorizationPolicyRoleBindingDao.class)) {
        return (T) new MockRoleAuthorizationPolicyRoleBindingDao();
      }
      return null;
    }

    @Override
    public <T> T get(final Class<T> interfaceClass, final String key, final IPentahoSession session) throws ObjectFactoryException {
      if (interfaceClass.equals(IRoleAuthorizationPolicyRoleBindingDao.class)) {
        return (T) new MockRoleAuthorizationPolicyRoleBindingDao();
      }
      return null;
    }

    @Override
    public boolean objectDefined(final String key) {
      return StringUtils.equals(key, "IRoleAuthorizationPolicyRoleBindingDao");
    }

    @Override
    public Class<?> getImplementingClass(final String key) {
      return StringUtils.equals(key, "IRoleAuthorizationPolicyRoleBindingDao") ? MockRoleAuthorizationPolicyRoleBindingDao.class : null;
    }

    @Override
    public void init(final String configFile, final Object context) {
    }
  }
  
  private class MockTenantManager implements ITenantManager {

    @Override
    public List<ITenant> getChildTenants(ITenant parentTenant) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public List<ITenant> getChildTenants(ITenant parentTenant, boolean includeDisabledTenants) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public void updateTentant(String tenantPath, Map<String, Serializable> tenantInfo) {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void deleteTenant(ITenant tenant) {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void deleteTenants(List<ITenant> tenantPaths) {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void enableTenant(ITenant tenant, boolean enable) {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void enableTenants(List<ITenant> tenantPaths, boolean enable) {
      // TODO Auto-generated method stub
      
    }

    @Override
    public boolean isSubTenant(ITenant parentTenant, ITenant descendantTenant) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public RepositoryFile getTenantRootFolder(ITenant tenant) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public ITenant getTenant(String tenantId) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public ITenant createTenant(ITenant arg0, String arg1, String arg2, String arg3, String arg4) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public ITenant getTenantByRootFolderPath(String arg0) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public RepositoryFile createUserHomeFolder(ITenant tenant, String username) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public RepositoryFile getUserHomeFolder(ITenant arg0, String arg1) {
      // TODO Auto-generated method stub
      return null;
    }
    
  }
}
