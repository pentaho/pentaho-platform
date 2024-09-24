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

package org.pentaho.platform.web.http.api.resources;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.stubbing.Answer;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.policy.rolebased.actions.PublishAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryReadAction;

public class RepositoryImportResourceIT {

  private static final String REAL_USER = "testUser";

  private static final String IMPORT_DIR = "/home/" + REAL_USER;

  private IPentahoObjectFactory pentahoObjectFactory;

  private IAuthorizationPolicy policy;

  private ITenantedPrincipleNameResolver resolver;

  @Before
  public void setUp() throws ObjectFactoryException {
    PentahoSystem.init();
    ITenant tenat = mock( ITenant.class );

    resolver = mock( ITenantedPrincipleNameResolver.class );
    doReturn( tenat ).when( resolver ).getTenant( nullable( String.class ) );
    doReturn( REAL_USER ).when( resolver ).getPrincipleName( nullable( String.class ) );
    policy = mock( IAuthorizationPolicy.class );
    pentahoObjectFactory = mock( IPentahoObjectFactory.class );
    when( pentahoObjectFactory.objectDefined( nullable( String.class ) ) ).thenReturn( true );
    when( pentahoObjectFactory.get( this.anyClass(), nullable( String.class ), any( IPentahoSession.class ) ) ).thenAnswer(
      invocation -> {
        if ( invocation.getArguments()[0].equals( IAuthorizationPolicy.class ) ) {
          return policy;
        }
        if ( invocation.getArguments()[0].equals( ITenantedPrincipleNameResolver.class ) ) {
          return resolver;
        }
        return null;
      } );
    PentahoSystem.registerObjectFactory( pentahoObjectFactory );
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
  public void testValidateAccess_Publish() throws PentahoAccessControlException {
    final List<String> perms =
        Arrays.asList( RepositoryReadAction.NAME, RepositoryCreateAction.NAME, PublishAction.NAME );
    testValidateAccess( perms );
  }

  @Test
  public void testValidateAccess_Admin() throws PentahoAccessControlException {
    final List<String> perms =
        Arrays.asList( RepositoryReadAction.NAME, RepositoryCreateAction.NAME, AdministerSecurityAction.NAME );
    testValidateAccess( perms );
  }

  @Test
  public void testValidateAccess_NonAdminValid() throws PentahoAccessControlException,
    SecurityException, IllegalArgumentException {
    final List<String> perms = Arrays.asList( RepositoryReadAction.NAME, RepositoryCreateAction.NAME );
    testValidateAccess( perms );
  }

  private void testValidateAccess( final List<String> perms ) throws PentahoAccessControlException {
    when( policy.isAllowed( nullable( String.class ) ) ).thenAnswer( (Answer<Boolean>) invocation -> {
      if ( perms.contains( invocation.getArguments()[0] ) ) {
        return true;
      }
      return false;
    } );

    RepositoryImportResource resource = new RepositoryImportResource();
    resource.validateImportAccess( IMPORT_DIR );

    ArgumentCaptor<String> captor = ArgumentCaptor.forClass( String.class );
    verify( policy, atLeastOnce() ).isAllowed( captor.capture() );
    assertTrue( captor.getAllValues().containsAll( perms ) );
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
