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
 * Copyright (c) 2002-2024 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.security.policy.rolebased;

import org.junit.Test;
import org.mockito.MockedStatic;
import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoRegistrableObjectFactory;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginManagerListener;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.MockPluginManager;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.policy.rolebased.actions.HideAnalyzerAction;
import org.pentaho.platform.security.policy.rolebased.actions.HideDashboardsAction;
import org.pentaho.platform.security.policy.rolebased.actions.HideInteractiveReportingAction;
import org.pentaho.platform.security.policy.rolebased.actions.PublishAction;
import org.pentaho.platform.security.policy.rolebased.actions.SchedulerAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

  @Test
  public void testUpdateImmutableRoleBindingNames() {
    Map<String, List<IAuthorizationAction>> immutableBindings = new HashMap<>();
    immutableBindings.put( "admin", Arrays.asList( new AdministerSecurityAction(), new SchedulerAction() ) );

    AbstractJcrBackedRoleBindingDaoImpl rbDao = new AbstractJcrBackedRoleBindingDaoImpl(immutableBindings,
      new HashMap<>(), "admin", null, new ArrayList<>());

    // Immutable role binding names should be set by the constructor
    assertTrue( rbDao.immutableRoleBindingNames.get( "admin" ).contains( AdministerSecurityAction.NAME ) );
    assertTrue( rbDao.immutableRoleBindingNames.get( "admin" ).contains( SchedulerAction.NAME ) );

    // Different list of bindings with new Actions
    ArrayList<IAuthorizationAction> actions = new ArrayList<>();
    actions.add( new AdministerSecurityAction() );
    actions.add( new PublishAction() );
    actions.add( new HideDashboardsAction() );
    rbDao.updateImmutableRoleBindingNames( actions );

    // Should be replaced by the ones from the passed list (old bindings should be gone)
    assertTrue( rbDao.immutableRoleBindingNames.get( "admin" ).contains( AdministerSecurityAction.NAME ) );
    assertTrue( rbDao.immutableRoleBindingNames.get( "admin" ).contains( PublishAction.NAME ) );
    assertTrue( rbDao.immutableRoleBindingNames.get( "admin" ).contains( HideDashboardsAction.NAME ) );
    assertFalse( rbDao.immutableRoleBindingNames.get( "admin" ).contains( SchedulerAction.NAME ) );
  }

  @Test
  public void testPluginReload() {
    try( MockedStatic<PentahoSystem> mockedSystem = mockStatic( PentahoSystem.class ) ) {
      ICacheManager cm = mock( ICacheManager.class );
      when( cm.cacheEnabled( "roleBindingCache" ) ).thenReturn( false );

      IPluginManager miniManager = new MockPluginManager() {
        private IPluginManagerListener listener;

        @Override public boolean reload() {
          listener.onReload();
          return true;
        }

        @Override public void addPluginManagerListener( IPluginManagerListener listener ) {
          this.listener = listener;
        }
      };

      mockedSystem.when( () -> PentahoSystem.getCacheManager( any() ) ).thenReturn( cm );
      mockedSystem.when( () -> PentahoSystem.get( IPluginManager.class ) ).thenReturn( miniManager );

      // Make sure to start with the IAuthorizationAction listed in AbstractJcrBackedRoleBindingDao.EXCLUDE_FROM_IMMUTABLE_ROLE_BINDINGS
      Map<String, List<IAuthorizationAction>> immutableBindings = new HashMap<>();
      immutableBindings.put( "admin", Arrays.asList( new HideDashboardsAction(), new HideAnalyzerAction(), new HideInteractiveReportingAction() ) );
      AbstractJcrBackedRoleBindingDaoImpl rbDao = new AbstractJcrBackedRoleBindingDaoImpl(immutableBindings,
        new HashMap<>(), "admin", null, new ArrayList<>());

      // Make sure they're really in there, make sure we don't have anything else
      assertTrue( rbDao.immutableRoleBindingNames.get( "admin" ).contains( HideDashboardsAction.NAME ) );
      assertTrue( rbDao.immutableRoleBindingNames.get( "admin" ).contains( HideAnalyzerAction.NAME ) );
      assertTrue( rbDao.immutableRoleBindingNames.get( "admin" ).contains( HideInteractiveReportingAction.NAME ) );
      assertFalse( rbDao.immutableRoleBindingNames.get( "admin" ).contains( AdministerSecurityAction.NAME ) );

      // "Register" a new action with the system (we can pretend this is from a plugin, will get added on reload)
      mockedSystem.when( () -> PentahoSystem.getAll( IAuthorizationAction.class ) ).thenReturn( Arrays.asList( new AdministerSecurityAction() ) );

      // Reload to trigger refresh of immutableRoleBindingNames
      miniManager.reload();

      // Make sure EXCLUDE_FROM_IMMUTABLE_ROLE_BINDINGS actions are gone, but we have the new one
      assertFalse( rbDao.immutableRoleBindingNames.get( "admin" ).contains( HideDashboardsAction.NAME ) );
      assertFalse( rbDao.immutableRoleBindingNames.get( "admin" ).contains( HideAnalyzerAction.NAME ) );
      assertFalse( rbDao.immutableRoleBindingNames.get( "admin" ).contains( HideInteractiveReportingAction.NAME ) );
      assertTrue( rbDao.immutableRoleBindingNames.get( "admin" ).contains( AdministerSecurityAction.NAME ) );
    }
  }

  private static class AbstractJcrBackedRoleBindingDaoImpl extends AbstractJcrBackedRoleBindingDao {
    public AbstractJcrBackedRoleBindingDaoImpl() {
      super();
    }

    public AbstractJcrBackedRoleBindingDaoImpl( final Map<String, List<IAuthorizationAction>> immutableRoleBindings,
                                                final Map<String, List<String>> bootstrapRoleBindings,
                                                final String superAdminRoleName,
                                                final ITenantedPrincipleNameResolver tenantedRoleNameUtils,
                                                final List<IAuthorizationAction> authorizationActions ) {
      super(immutableRoleBindings, bootstrapRoleBindings, superAdminRoleName, tenantedRoleNameUtils, authorizationActions);
    }

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