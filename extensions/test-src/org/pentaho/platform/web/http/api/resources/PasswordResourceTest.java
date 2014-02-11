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
 *
 * Copyright 2014 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.platform.web.http.api.resources;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.util.Base64PasswordService;
import org.pentaho.platform.util.PasswordHelper;

import javax.ws.rs.core.Response;

public class PasswordResourceTest {
  @Test
  public void testEncodePassword() throws Exception {
    final IAuthorizationPolicy policy = Mockito.mock( IAuthorizationPolicy.class );
    PasswordResource resource = getPasswordResource( policy );
    Mockito.when( policy.isAllowed( AdministerSecurityAction.NAME ) ).thenReturn( true );
    Response response = resource.encryptPassword( "password" );
    Assert.assertTrue( response.getEntity().toString().contains( "ENC:cGFzc3dvcmQ=" ) );
    Assert.assertEquals( 200, response.getStatus() );
    Assert.assertEquals( 200, resource.encryptionForm().getStatus() );
  }

  @Test
  public void testMustHaveAdminAccess() throws Exception {
    final IAuthorizationPolicy policy = Mockito.mock( IAuthorizationPolicy.class );
    PasswordResource resource = getPasswordResource( policy );
    Mockito.when( policy.isAllowed( AdministerSecurityAction.NAME ) ).thenReturn( false );
    Response response = resource.encryptPassword( "password" );
    Assert.assertEquals( 401, response.getStatus() );
    Assert.assertEquals( 401, resource.encryptionForm().getStatus() );
  }

  private PasswordResource getPasswordResource( final IAuthorizationPolicy policy ) {
    return new PasswordResource() {
      @Override IAuthorizationPolicy getAuthorizationPolicy() {
        return policy;
      }

      @Override PasswordHelper getPasswordHelper() {
        return new PasswordHelper( new Base64PasswordService() );
      }
    };
  }
}
