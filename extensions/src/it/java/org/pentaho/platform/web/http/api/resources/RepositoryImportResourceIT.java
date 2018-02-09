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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.api.resources;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.invocation.InvocationOnMock;
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
    doReturn( tenat ).when( resolver ).getTenant( anyString() );
    doReturn( REAL_USER ).when( resolver ).getPrincipleName( anyString() );
    policy = mock( IAuthorizationPolicy.class );
    pentahoObjectFactory = mock( IPentahoObjectFactory.class );
    when( pentahoObjectFactory.objectDefined( anyString() ) ).thenReturn( true );
    when( pentahoObjectFactory.get( this.anyClass(), anyString(), any( IPentahoSession.class ) ) ).thenAnswer(
        new Answer<Object>() {
          @Override
          public Object answer( InvocationOnMock invocation ) throws Throwable {
            if ( invocation.getArguments()[0].equals( IAuthorizationPolicy.class ) ) {
              return policy;
            }
            if ( invocation.getArguments()[0].equals( ITenantedPrincipleNameResolver.class ) ) {
              return resolver;
            }
            return null;
          }
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
  public void testValidateAccess_NonAdminValid() throws PentahoAccessControlException, NoSuchFieldException,
    SecurityException, IllegalArgumentException, IllegalAccessException {
    final List<String> perms = Arrays.asList( RepositoryReadAction.NAME, RepositoryCreateAction.NAME );
    testValidateAccess( perms );
  }

  private void testValidateAccess( final List<String> perms ) throws PentahoAccessControlException {
    when( policy.isAllowed( anyString() ) ).thenAnswer( new Answer<Boolean>() {

      @Override
      public Boolean answer( InvocationOnMock invocation ) throws Throwable {
        if ( perms.contains( invocation.getArguments()[0] ) ) {
          return true;
        }
        ;
        return false;
      }

    } );

    RepositoryImportResource resource = new RepositoryImportResource();
    resource.validateAccess( IMPORT_DIR );

    ArgumentCaptor<String> captor = ArgumentCaptor.forClass( String.class );
    verify( policy, atLeastOnce() ).isAllowed( captor.capture() );
    assertTrue( captor.getAllValues().containsAll( perms ) );
  }

  private Class<?> anyClass() {
    return argThat( new AnyClassMatcher() );
  }

  private class AnyClassMatcher extends ArgumentMatcher<Class<?>> {
    @Override
    public boolean matches( final Object arg ) {
      return true;
    }
  }
}
