/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.plugin.services.exporter.helper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.usersettings.IUserSettingService;
import org.pentaho.platform.api.util.IRepositoryExportLogger;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.exporter.PentahoPlatformExporter;
import org.pentaho.platform.plugin.services.importexport.RoleExport;
import org.pentaho.platform.plugin.services.importexport.UserExport;
import org.pentaho.platform.plugin.services.importexport.exportManifest.ExportManifest;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.pentaho.platform.security.policy.rolebased.RoleBindingStruct;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link UsersAndRolesExportHelper}, focused on the schedule-owner dependency export path.
 *
 * <p>When schedules are backed up (SCHEDULES profile), the schedule owner is exported as a
 * dependency via {@link UsersAndRolesExportHelper#exportUserAndRole(String, PentahoPlatformExporter)}.
 * These tests verify that path also exports the owner's runtime-role to logical-role bindings so that
 * the role permissions survive a schedules-only restore.</p>
 */
public class UsersAndRolesExportHelperTest {

  private static final String OWNER = "scheduleOwner";
  private static final String ROLE = "Power User";

  private IUserRoleListService userRoleListService;
  private UserDetailsService userDetailsService;
  private IRoleAuthorizationPolicyRoleBindingDao roleBindingDao;
  private IUserSettingService userSettingService;

  private PentahoPlatformExporter exporter;
  private UsersAndRolesExportHelper helper;

  @Before
  public void setUp() {
    IPentahoSession session = mock( IPentahoSession.class );
    PentahoSessionHolder.setSession( session );

    userRoleListService = mock( IUserRoleListService.class );
    userDetailsService = mock( UserDetailsService.class );
    roleBindingDao = mock( IRoleAuthorizationPolicyRoleBindingDao.class );
    userSettingService = mock( IUserSettingService.class );

    PentahoSystem.registerObject( userRoleListService );
    PentahoSystem.registerObject( userDetailsService );
    PentahoSystem.registerObject( roleBindingDao );
    PentahoSystem.registerObject( userSettingService );

    // Owner belongs to a single runtime role.
    when( userRoleListService.getRolesForUser( any(), eq( OWNER ) ) ).thenReturn( Arrays.asList( ROLE ) );

    List<GrantedAuthority> authorities = new ArrayList<>();
    UserDetails userDetails = new User( OWNER, "secret", true, true, true, true, authorities );
    when( userDetailsService.loadUserByUsername( nullable( String.class ) ) ).thenReturn( userDetails );

    // Runtime role -> logical roles (permissions) binding.
    Map<String, List<String>> bindingMap = new HashMap<>();
    bindingMap.put( ROLE, Arrays.asList( "AdministerSecurity", "SchedulerAction" ) );
    RoleBindingStruct struct = mock( RoleBindingStruct.class );
    struct.bindingMap = bindingMap;
    when( roleBindingDao.getRoleBindingStruct( nullable( String.class ) ) ).thenReturn( struct );

    IUnifiedRepository repo = mock( IUnifiedRepository.class );
    exporter = new PentahoPlatformExporter( repo );
    exporter.setExportManifest( new ExportManifest() );
    exporter.setRepositoryExportLogger( mock( IRepositoryExportLogger.class ) );

    helper = new UsersAndRolesExportHelper();
  }

  @After
  public void tearDown() {
    PentahoSystem.clearObjectFactory();
    PentahoSessionHolder.removeSession();
  }

  @Test
  public void scheduleOwnerExportEmitsUserAndRoleBinding() {
    boolean result = helper.exportUserAndRole( OWNER, exporter );

    assertTrue( "Owner export should succeed", result );

    // The owner user is exported with the role membership.
    List<UserExport> users = exporter.getExportManifest().getUserExports();
    assertEquals( 1, users.size() );
    assertEquals( OWNER, users.get( 0 ).getUsername() );

    // The runtime-role -> logical-role binding is exported as a RoleExport.
    List<RoleExport> roles = exporter.getExportManifest().getRoleExports();
    assertEquals( "Schedule owner's role binding must be exported", 1, roles.size() );
    RoleExport roleExport = roles.get( 0 );
    assertEquals( ROLE, roleExport.getRolename() );
    assertNotNull( "Logical-role permissions must be captured", roleExport.getPermissions() );
    assertEquals( Arrays.asList( "AdministerSecurity", "SchedulerAction" ), roleExport.getPermissions() );
  }

  @Test
  public void roleBindingIsNotDuplicatedWhenAlreadyInManifest() {
    // A previous user/role export already placed this role in the manifest.
    RoleExport existing = new RoleExport();
    existing.setRolename( ROLE );
    existing.setPermission( Arrays.asList( "AdministerSecurity", "SchedulerAction" ) );
    exporter.getExportManifest().addRoleExport( existing );

    helper.exportUserAndRole( OWNER, exporter );

    List<RoleExport> roles = exporter.getExportManifest().getRoleExports();
    assertEquals( "Role binding must not be duplicated", 1, roles.size() );
  }

  @Test
  public void exportRoleBindingSkipsRolesAlreadyTracked() {
    Set<String> tracked = new HashSet<>();
    tracked.add( ROLE );

    boolean added = helper.exportRoleBinding( ROLE, exporter, roleBindingDao, tracked );

    assertFalse( "Tracked role must be skipped", added );
    assertTrue( "Manifest must remain empty", exporter.getExportManifest().getRoleExports().isEmpty() );
  }

  @Test
  public void collectExportedRoleNamesReadsManifest() {
    RoleExport a = new RoleExport();
    a.setRolename( "RoleA" );
    RoleExport b = new RoleExport();
    b.setRolename( "RoleB" );
    exporter.getExportManifest().addRoleExport( a );
    exporter.getExportManifest().addRoleExport( b );

    Set<String> names = helper.collectExportedRoleNames( exporter );

    assertEquals( 2, names.size() );
    assertTrue( names.contains( "RoleA" ) );
    assertTrue( names.contains( "RoleB" ) );
  }
}
