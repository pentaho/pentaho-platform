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