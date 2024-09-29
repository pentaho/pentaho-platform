/*!
 *
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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.security.policy.rolebased;

import org.junit.Test;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoRegistrableObjectFactory;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Created by nbaker on 2/23/16.
 */
public class AbstractJcrBackedRoleBindingDaoTest {

  @Test
  public void testDao() throws Exception {
    ICacheManager cm = mock( ICacheManager.class );
    PentahoSystem.registerObject( cm, IPentahoRegistrableObjectFactory.Types.INTERFACES );
    when( cm.cacheEnabled( "roleBindingCache" ) ).thenReturn( false );

    new AbstractJcrBackedRoleBindingDaoImpl();
    verify( cm, times( 1 ) ).addCacheRegion( "roleBindingCache");
  }

  private static class AbstractJcrBackedRoleBindingDaoImpl extends AbstractJcrBackedRoleBindingDao {
    @Override public RoleBindingStruct getRoleBindingStruct( String locale ) {
      return null;
    }

    @Override public RoleBindingStruct getRoleBindingStruct( ITenant tenant, String locale ) {
      return null;
    }

    @Override public void setRoleBindings( String runtimeRoleName, List<String> logicalRolesNames ) {

    }

    @Override public void setRoleBindings( ITenant tenant, String runtimeRoleName,
                                           List<String> logicalRolesNames ) {

    }

    @Override public List<String> getBoundLogicalRoleNames( List<String> runtimeRoleNames ) {
      return null;
    }

    @Override public List<String> getBoundLogicalRoleNames( ITenant tenant, List<String> runtimeRoleNames ) {
      return null;
    }
  }
}