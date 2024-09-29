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

package org.pentaho.test.platform.repository2.mt;

import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.NameFactory;
import org.apache.jackrabbit.spi.commons.name.NameFactoryImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.engine.core.system.TenantUtils;
import org.pentaho.platform.repository2.unified.DefaultUnifiedRepositoryBase;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.pentaho.platform.repository2.unified.jcr.JcrRepositoryDumpToFile;
import org.pentaho.platform.repository2.unified.jcr.JcrRepositoryDumpToFile.Mode;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author mlowery
 */
@RunWith( SpringJUnit4ClassRunner.class )
@SuppressWarnings( "nls" )
public class RepositoryTenantManagerIT extends DefaultUnifiedRepositoryBase {

  private final String TENANT_ID_APPLE = "apple";

  private final String TENANT_ID_MICROSOFT = "microsoft";

  private final String TENANT_ID_SUN = "sun";

  public static final String MAIN_TENANT_1 = "maintenant1";

  public static final String SUB_TENANT1_1 = "subtenant11";

  public static final String SUB_TENANT1_1_1 = "subtenant111";

  public static final String SUB_TENANT1_1_2 = "subtenant112";

  public static final String SUB_TENANT1_2 = "subtenant12";

  public static final String SUB_TENANT1_2_1 = "subtenant121";

  public static final String SUB_TENANT1_2_2 = "subtenant122";

  public static final String MAIN_TENANT_2 = "maintenant2";

  public static final String SUB_TENANT2_1 = "subtenant21";

  public static final String SUB_TENANT2_1_1 = "subtenant211";

  public static final String SUB_TENANT2_1_2 = "subtenant212";

  public static final String SUB_TENANT2_2 = "subtenant22";

  public static final String SUB_TENANT2_2_1 = "subtenant221";

  public static final String SUB_TENANT2_2_2 = "subtenant222";

  NameFactory NF = NameFactoryImpl.getInstance();

  Name P_PRINCIPAL_NAME = NF.create( Name.NS_REP_URI, "principalName" ); //$NON-NLS-1$

  String pPrincipalName;

  private void assertTenantNotNull( ITenant tenant ) {
    assertNotNull( tenant );
    assertNotNull( tenant.getId() );
    assertNotNull( tenant.getName() );
  }

  @Test
  public void testCreateSystemTenant() {
    loginAsRepositoryAdmin();
    ITenant duplicateTenant =
      tenantManager.createTenant( null, ServerRepositoryPaths.getPentahoRootFolderName(), tenantAdminRoleName,
        tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );
    assertNull( duplicateTenant );
  }

  @Test
  public void testCreateTenant() {
    loginAsSysTenantAdmin();
    assertNotNull( systemTenant );
    assertTrue( systemTenant.isEnabled() );

    ITenant tenantRoot =
      tenantManager.createTenant( systemTenant, TenantUtils.TENANTID_SINGLE_TENANT, tenantAdminRoleName,
        tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantRoot, USERNAME_ADMIN, PASSWORD, "", new String[]{ tenantAdminRoleName } );
    assertNotNull( tenantRoot );
    assertTrue( tenantRoot.isEnabled() );
    ITenant subTenantRoot =
      tenantManager.createTenant( tenantRoot, TENANT_ID_APPLE, tenantAdminRoleName,
        tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( subTenantRoot, USERNAME_ADMIN, PASSWORD, "", new String[]{ tenantAdminRoleName } );
    assertNotNull( subTenantRoot );
    assertTrue( subTenantRoot.isEnabled() );
    List<ITenant> childTenants = tenantManager.getChildTenants( tenantRoot );
    assertTrue( childTenants.size() == 1 );
    assertTrue( childTenants.get( 0 ).equals( subTenantRoot ) );

    cleanupUserAndRoles( tenantRoot );
    cleanupUserAndRoles( subTenantRoot );
  }

  @Test
  public void testEnableDisableTenant() {
    loginAsSysTenantAdmin();
    assertTenantNotNull( systemTenant );
    ITenant tenantRoot =
      tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName,
        tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantRoot, USERNAME_ADMIN, PASSWORD, "", new String[]{ tenantAdminRoleName } );
    assertTenantNotNull( tenantRoot );
    assertTrue( tenantRoot.isEnabled() );
    tenantManager.enableTenant( tenantRoot, false );
    tenantRoot = tenantManager.getTenant( tenantRoot.getRootFolderAbsolutePath() );
    assertTrue( !tenantRoot.isEnabled() );
    tenantManager.enableTenant( tenantRoot, true );
    tenantRoot = tenantManager.getTenant( tenantRoot.getRootFolderAbsolutePath() );
    assertTrue( tenantRoot.isEnabled() );
    cleanupUserAndRoles( tenantRoot );
  }

  @Test
  public void testIsTenantRoot() {
    loginAsSysTenantAdmin();
    assertTenantNotNull( systemTenant );
    assertTrue( systemTenant.isEnabled() );
    ITenant tenantRoot =
      tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName,
        tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantRoot, USERNAME_ADMIN, PASSWORD, "", new String[]{ tenantAdminRoleName } );
    assertTenantNotNull( tenantRoot );

    cleanupUserAndRoles( tenantRoot );
  }

  @Test
  public void testIsSubTenant() {
    loginAsSysTenantAdmin();
    ITenant mainTenant_1 =
      tenantManager.createTenant( systemTenant, MAIN_TENANT_1, tenantAdminRoleName,
        tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( mainTenant_1, USERNAME_ADMIN, PASSWORD, "", new String[]{ tenantAdminRoleName } );
    ITenant mainTenant_2 =
      tenantManager.createTenant( systemTenant, MAIN_TENANT_2, tenantAdminRoleName,
        tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( mainTenant_2, USERNAME_ADMIN, PASSWORD, "", new String[]{ tenantAdminRoleName } );
    ITenant subTenant1_1 =
      tenantManager.createTenant( mainTenant_1, SUB_TENANT1_1, tenantAdminRoleName,
        tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( subTenant1_1, USERNAME_ADMIN, PASSWORD, "", new String[]{ tenantAdminRoleName } );
    ITenant subTenant1_2 =
      tenantManager.createTenant( mainTenant_1, SUB_TENANT1_2, tenantAdminRoleName,
        tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( subTenant1_2, USERNAME_ADMIN, PASSWORD, "", new String[]{ tenantAdminRoleName } );
    ITenant subTenant2_1 =
      tenantManager.createTenant( mainTenant_2, SUB_TENANT2_1, tenantAdminRoleName,
        tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( subTenant2_1, USERNAME_ADMIN, PASSWORD, "", new String[]{ tenantAdminRoleName } );
    ITenant subTenant2_2 =
      tenantManager.createTenant( mainTenant_2, SUB_TENANT2_2, tenantAdminRoleName,
        tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( subTenant2_2, USERNAME_ADMIN, PASSWORD, "", new String[]{ tenantAdminRoleName } );
    assertTrue( tenantManager.isSubTenant( mainTenant_1, mainTenant_1 ) );
    assertTrue( tenantManager.isSubTenant( mainTenant_2, mainTenant_2 ) );
    assertTrue( tenantManager.isSubTenant( mainTenant_1, subTenant1_2 ) );
    assertTrue( tenantManager.isSubTenant( mainTenant_1, subTenant1_1 ) );
    assertFalse( tenantManager.isSubTenant( mainTenant_1, subTenant2_1 ) );
    assertFalse( tenantManager.isSubTenant( mainTenant_1, subTenant2_2 ) );
    assertFalse( tenantManager.isSubTenant( mainTenant_2, subTenant1_2 ) );
    assertFalse( tenantManager.isSubTenant( mainTenant_2, subTenant1_1 ) );
    assertTrue( tenantManager.isSubTenant( mainTenant_2, subTenant2_1 ) );
    assertTrue( tenantManager.isSubTenant( mainTenant_2, subTenant2_2 ) );
    assertTrue( tenantManager.isSubTenant( subTenant2_2, subTenant2_2 ) );
    assertTrue( tenantManager.isSubTenant( subTenant1_2, subTenant1_2 ) );

    JcrRepositoryDumpToFile dumpToFile =
      new JcrRepositoryDumpToFile( testJcrTemplate, jcrTransactionTemplate, repositoryAdminUsername,
        "c:/tmp/testdump122", Mode.CUSTOM );
    dumpToFile.execute();
    cleanupUserAndRoles( mainTenant_1 );
    cleanupUserAndRoles( mainTenant_2 );
    cleanupUserAndRoles( subTenant1_1 );
    cleanupUserAndRoles( subTenant1_2 );
    cleanupUserAndRoles( subTenant2_1 );
    cleanupUserAndRoles( subTenant2_2 );
  }

  @Test
  public void testGetChildrenTenants() {
    loginAsSysTenantAdmin();
    ITenant tenantRoot =
      tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName,
        tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantRoot, USERNAME_ADMIN, PASSWORD, "", new String[]{ tenantAdminRoleName } );
    assertNotNull( tenantRoot );
    assertTrue( tenantRoot.isEnabled() );

    ITenant subTenantRoot1 =
      tenantManager.createTenant( tenantRoot, TENANT_ID_APPLE, tenantAdminRoleName,
        tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( subTenantRoot1, USERNAME_ADMIN, PASSWORD, "", new String[]{ tenantAdminRoleName } );
    assertTrue( subTenantRoot1.isEnabled() );

    ITenant subTenantRoot2 =
      tenantManager.createTenant( tenantRoot, TENANT_ID_MICROSOFT, tenantAdminRoleName,
        tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( subTenantRoot2, USERNAME_ADMIN, PASSWORD, "", new String[]{ tenantAdminRoleName } );
    assertTrue( subTenantRoot2.isEnabled() );

    List<ITenant> tenantChildren = tenantManager.getChildTenants( tenantRoot );
    assertTrue( tenantChildren.size() == 2 );
    List<ITenant> tenantChildrenId = tenantManager.getChildTenants( tenantRoot );
    assertTrue( tenantChildrenId.size() == 2 );
    cleanupUserAndRoles( tenantRoot );
    cleanupUserAndRoles( subTenantRoot1 );
    cleanupUserAndRoles( subTenantRoot2 );
  }

  @Test
  public void testDeleteTenant() {
    loginAsSysTenantAdmin();
    ITenant mainTenant_1 =
      tenantManager.createTenant( systemTenant, MAIN_TENANT_1, tenantAdminRoleName,
        tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( mainTenant_1, USERNAME_ADMIN, PASSWORD, "", new String[]{ tenantAdminRoleName } );
    ITenant mainTenant_2 =
      tenantManager.createTenant( systemTenant, MAIN_TENANT_2, tenantAdminRoleName,
        tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( mainTenant_2, USERNAME_ADMIN, PASSWORD, "", new String[]{ tenantAdminRoleName } );
    ITenant subTenant1_1 = null;
    // Testing SubTenant1_1 as a TenantAdmin of MainTenant2. This should fail
    login( USERNAME_ADMIN, mainTenant_2, new String[]{ tenantAdminRoleName, tenantAuthenticatedRoleName } );
    try {
      subTenant1_1 =
        tenantManager.createTenant( mainTenant_1, SUB_TENANT1_1, tenantAdminRoleName,
          tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );
      fail( "should have thrown an exception" );
    } catch ( Throwable th ) {
      assertNotNull( th );
    }
    logout();
    login( USERNAME_ADMIN, mainTenant_1, new String[]{ tenantAdminRoleName, tenantAuthenticatedRoleName } );
    subTenant1_1 =
      tenantManager.createTenant( mainTenant_1, SUB_TENANT1_1, tenantAdminRoleName,
        tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( subTenant1_1, USERNAME_ADMIN, PASSWORD, "", new String[]{ tenantAdminRoleName } );

    ITenant subTenant1_1_1 =
      tenantManager.createTenant( subTenant1_1, SUB_TENANT1_1_1, tenantAdminRoleName,
        tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( subTenant1_1_1, USERNAME_ADMIN, PASSWORD, "", new String[]{ tenantAdminRoleName } );

    ITenant subTenant1_1_2 =
      tenantManager.createTenant( subTenant1_1, SUB_TENANT1_1_2, tenantAdminRoleName,
        tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( subTenant1_1_2, USERNAME_ADMIN, PASSWORD, "", new String[]{ tenantAdminRoleName } );
    logout();
    // Testing SubTenant1_2 as a TenantAdmin of MainTenant2. This should fail
    login( USERNAME_ADMIN, mainTenant_2, new String[]{ tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant subTenant1_2 = null;
    try {
      subTenant1_2 =
        tenantManager.createTenant( mainTenant_1, SUB_TENANT1_2, tenantAdminRoleName,
          tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );
      fail( "should have thrown an exception" );
    } catch ( Throwable th ) {
      assertNotNull( th );
    }
    logout();
    login( USERNAME_ADMIN, mainTenant_1, new String[]{ tenantAdminRoleName, tenantAuthenticatedRoleName } );
    subTenant1_2 =
      tenantManager.createTenant( mainTenant_1, SUB_TENANT1_2, tenantAdminRoleName,
        tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( subTenant1_2, USERNAME_ADMIN, PASSWORD, "", new String[]{ tenantAdminRoleName } );
    logout();
    login( USERNAME_ADMIN, subTenant1_2, new String[]{ tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant subTenant1_2_1 =
      tenantManager.createTenant( subTenant1_2, SUB_TENANT1_2_1, tenantAdminRoleName,
        tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( subTenant1_2_1, USERNAME_ADMIN, PASSWORD, "", new String[]{ tenantAdminRoleName } );

    ITenant subTenant1_2_2 =
      tenantManager.createTenant( subTenant1_2, SUB_TENANT1_2_2, tenantAdminRoleName,
        tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( subTenant1_2_2, USERNAME_ADMIN, PASSWORD, "", new String[]{ tenantAdminRoleName } );
    logout();
    login( USERNAME_ADMIN, mainTenant_2, new String[]{ tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant subTenant2_1 =
      tenantManager.createTenant( mainTenant_2, SUB_TENANT2_1, tenantAdminRoleName,
        tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( subTenant2_1, USERNAME_ADMIN, PASSWORD, "", new String[]{ tenantAdminRoleName } );
    ITenant subTenant2_1_1 =
      tenantManager.createTenant( subTenant2_1, SUB_TENANT2_1_1, tenantAdminRoleName,
        tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( subTenant2_1_1, USERNAME_ADMIN, PASSWORD, "", new String[]{ tenantAdminRoleName } );

    ITenant subTenant2_1_2 =
      tenantManager.createTenant( subTenant2_1, SUB_TENANT2_1_2, tenantAdminRoleName,
        tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( subTenant2_1_2, USERNAME_ADMIN, PASSWORD, "", new String[]{ tenantAdminRoleName } );
    logout();
    login( USERNAME_ADMIN, mainTenant_1, new String[]{ tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant subTenant2_2 = null;
    try {
      subTenant2_2 =
        tenantManager.createTenant( mainTenant_2, SUB_TENANT2_2, tenantAdminRoleName,
          tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );
      fail( "should have thrown an exception" );
    } catch ( Throwable th ) {
      assertNotNull( th );
    }
    logout();
    login( USERNAME_ADMIN, mainTenant_2, new String[]{ tenantAdminRoleName, tenantAuthenticatedRoleName } );
    subTenant2_2 =
      tenantManager.createTenant( mainTenant_2, SUB_TENANT2_2, tenantAdminRoleName,
        tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( subTenant2_2, USERNAME_ADMIN, PASSWORD, "", new String[]{ tenantAdminRoleName } );

    logout();
    login( USERNAME_ADMIN, subTenant2_2, new String[]{ tenantAdminRoleName, tenantAuthenticatedRoleName } );

    ITenant subTenant2_2_1 =
      tenantManager.createTenant( subTenant2_2, SUB_TENANT2_2_1, tenantAdminRoleName,
        tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( subTenant2_2_1, USERNAME_ADMIN, PASSWORD, "", new String[]{ tenantAdminRoleName } );

    ITenant subTenant2_2_2 =
      tenantManager.createTenant( subTenant2_2, SUB_TENANT2_2_2, tenantAdminRoleName,
        tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( subTenant2_2_2, USERNAME_ADMIN, PASSWORD, "", new String[]{ tenantAdminRoleName } );

    // Delete Tenants

    login( USERNAME_ADMIN, subTenant2_1, new String[]{ tenantAdminRoleName, tenantAuthenticatedRoleName } );
    try {
      tenantManager.deleteTenant( subTenant2_1 );
      fail( "should have thrown an exception" );
    } catch ( Throwable th ) {
      assertNotNull( th );
    }
    logout();

    login( USERNAME_ADMIN, mainTenant_2, new String[]{ tenantAdminRoleName, tenantAuthenticatedRoleName } );
    tenantManager.deleteTenant( subTenant2_1 );
    ITenant tenant = tenantManager.getTenant( subTenant2_1.getRootFolderAbsolutePath() );
    assertNull( tenant );
    tenant = tenantManager.getTenant( subTenant2_1_1.getRootFolderAbsolutePath() );
    assertNull( tenant );
    tenant = tenantManager.getTenant( subTenant2_1_2.getRootFolderAbsolutePath() );
    assertNull( tenant );
    logout();

    login( USERNAME_ADMIN, subTenant2_2, new String[]{ tenantAdminRoleName, tenantAuthenticatedRoleName } );
    try {
      tenantManager.deleteTenant( subTenant2_2 );
      fail( "should have thrown an exception" );
    } catch ( Throwable th ) {
      assertNotNull( th );
    }
    logout();

    login( USERNAME_ADMIN, mainTenant_2, new String[]{ tenantAdminRoleName, tenantAuthenticatedRoleName } );
    tenantManager.deleteTenant( subTenant2_2 );
    tenant = tenantManager.getTenant( subTenant2_2.getRootFolderAbsolutePath() );
    assertNull( tenant );
    tenant = tenantManager.getTenant( subTenant2_2_1.getRootFolderAbsolutePath() );
    assertNull( tenant );
    tenant = tenantManager.getTenant( subTenant2_2_2.getRootFolderAbsolutePath() );
    assertNull( tenant );
    logout();

    login( USERNAME_ADMIN, mainTenant_1, new String[]{ tenantAdminRoleName, tenantAuthenticatedRoleName } );
    tenantManager.deleteTenant( subTenant1_1 );
    tenantManager.deleteTenant( subTenant1_2 );
    logout();

    loginAsSysTenantAdmin();
    tenantManager.deleteTenant( mainTenant_1 );
    tenantManager.deleteTenant( mainTenant_2 );
    logout();
  }
}
