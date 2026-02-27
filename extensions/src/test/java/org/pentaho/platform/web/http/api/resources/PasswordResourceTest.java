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


package org.pentaho.platform.web.http.api.resources;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.util.KettlePasswordService;
import org.pentaho.platform.util.PasswordHelper;

import jakarta.ws.rs.core.Response;

public class PasswordResourceTest {
  @Test
  public void testEncodePassword() throws Exception {
    final IAuthorizationPolicy policy = Mockito.mock( IAuthorizationPolicy.class );
    PasswordResource resource = getPasswordResource( policy );
    Mockito.when( policy.isAllowed( AdministerSecurityAction.NAME ) ).thenReturn( true );
    Response response = resource.encryptPassword( "password" );
    Assert.assertTrue( response.getEntity().toString().contains( "ENC:Encrypted 2be98afc86aa7f2e4bb18bd63c99dbdde" ) );
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
        return new PasswordHelper( new KettlePasswordService() );
      }
    };
  }
}
