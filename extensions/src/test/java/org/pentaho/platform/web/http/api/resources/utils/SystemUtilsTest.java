/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.api.resources.utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.policy.rolebased.actions.PublishAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryReadAction;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class SystemUtilsTest {


  private static final String REAL_USER = "testUser";

  private static final String IMPORT_DIR = "/home/" + REAL_USER;

  private IPentahoObjectFactory pentahoObjectFactory;

  private ITenantedPrincipleNameResolver resolver;

  @Before
  public void setUp() throws ObjectFactoryException {

    PentahoSystem.init();
    ITenant tenant = mock( ITenant.class );

    resolver = mock( ITenantedPrincipleNameResolver.class );
    doReturn( tenant ).when( resolver ).getTenant( nullable( String.class ) );
    doReturn( REAL_USER ).when( resolver ).getPrincipleName( nullable( String.class ) );
    pentahoObjectFactory = mock( IPentahoObjectFactory.class );
    when( pentahoObjectFactory.objectDefined( nullable( String.class ) ) ).thenReturn( true );
    when( pentahoObjectFactory.get( this.anyClass(), nullable( String.class ), any( IPentahoSession.class ) ) ).thenAnswer(
      invocation -> {
        if ( invocation.getArguments()[0].equals( ITenantedPrincipleNameResolver.class ) ) {
          return resolver;
        }
        return null;
      } );

    PentahoSystem.registerObjectFactory( pentahoObjectFactory );

    IUserRoleListService userRoleListService = mock( IUserRoleListService.class );
    PentahoSystem.registerObject( userRoleListService );

    IPentahoSession session = mock( IPentahoSession.class );
    doReturn( "sampleSession" ).when( session ).getName();
    PentahoSessionHolder.setSession( session );
  }

  @After
  public void tearDown() {
    PentahoSystem.deregisterObjectFactory( pentahoObjectFactory );
    PentahoSystem.shutdown();
  }

  @Test
  public void testCanDownload() {
    IAuthorizationPolicy mockAuthPolicy = mock( IAuthorizationPolicy.class );

    /* register  mockAuthPolicy with PentahoSystem so SystemUtils can use it */
    PentahoSystem.registerObject( mockAuthPolicy );

    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryReadAction.NAME ); /* user has 'Read Content' */
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryCreateAction.NAME ); /* user has 'Create Content' */
    /* non-admin user */
    doReturn( false ).when( mockAuthPolicy ).isAllowed( AdministerSecurityAction.NAME );

    // Test 1: empty folder specified, should not grant access
    assertFalse( SystemUtils.canDownload( "" ) );

    // Test 2: user gains administer security, should grant access
    doReturn( true ).when( mockAuthPolicy ).isAllowed( AdministerSecurityAction.NAME );
    assertTrue( SystemUtils.canDownload( "/mock/path" ) );

    // Test 3: user loses administer security, neither does it have download roles nor it's on home folder shouldn't grant access
    doReturn( false ).when( mockAuthPolicy ).isAllowed( AdministerSecurityAction.NAME );
    assertFalse( SystemUtils.canDownload( "/mock/path" ) );

    // Test 4: user loses administer security, neither does it have download roles but it's on home folder, so it should grant access
    assertTrue( SystemUtils.canDownload( IMPORT_DIR ) );

    // Test 5: user is on home folder but loses read content, should not grant access
    doReturn( false ).when( mockAuthPolicy ).isAllowed( RepositoryReadAction.NAME );
    assertFalse( SystemUtils.canDownload( IMPORT_DIR ) );

  }

  @Test
  public void testCanUpload() {
    IAuthorizationPolicy mockAuthPolicy = mock( IAuthorizationPolicy.class );

    /* register  mockAuthPolicy with PentahoSystem so SystemUtils can use it */
    PentahoSystem.registerObject( mockAuthPolicy );

    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryReadAction.NAME ); /* user has 'Read Content' */
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryCreateAction.NAME ); /* user has 'Create Content' */
    /* non-admin user */
    doReturn( false ).when( mockAuthPolicy ).isAllowed( AdministerSecurityAction.NAME );
    doReturn( false ).when( mockAuthPolicy ).isAllowed( PublishAction.NAME );

    // Test 1: empty folder specified, should not grant access
    assertFalse( SystemUtils.canUpload( "" ) );

    // Test 2: user gains administer security, should grant access
    doReturn( true ).when( mockAuthPolicy ).isAllowed( AdministerSecurityAction.NAME );
    assertTrue( SystemUtils.canUpload( "/mock/path" ) );

    // Test 3: user loses administer security, but has publish action, should grant access
    doReturn( false ).when( mockAuthPolicy ).isAllowed( AdministerSecurityAction.NAME );
    assertFalse( SystemUtils.canDownload( "/mock/path" ) );

    // Test 4: user loses administer security, neither does it have publish content, but on ome folder, should grant access
    assertTrue( SystemUtils.canDownload( IMPORT_DIR ) );

    // Test 5: user is on home folder but loses read content, should not grant access
    doReturn( false ).when( mockAuthPolicy ).isAllowed( RepositoryReadAction.NAME );
    assertFalse( SystemUtils.canDownload( IMPORT_DIR ) );

    // Test 5: user is on home folder but loses create content, should not grant access
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( false ).when( mockAuthPolicy ).isAllowed( RepositoryCreateAction.NAME );
    assertFalse( SystemUtils.canDownload( IMPORT_DIR ) );
  }

  private Class<?> anyClass() {
    return argThat( new AnyClassMatcher() );
  }

  private static class AnyClassMatcher implements ArgumentMatcher<Class<?>> {
    @Override
    public boolean matches( final Class<?> arg ) {
      return true;
    }
  }
}
