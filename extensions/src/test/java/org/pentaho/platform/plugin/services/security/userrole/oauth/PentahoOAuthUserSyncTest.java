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

package org.pentaho.platform.plugin.services.security.userrole.oauth;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.security.userroledao.PentahoOAuthUser;
import org.pentaho.platform.security.userroledao.PentahoUser;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class PentahoOAuthUserSyncTest {

  @Mock
  IUserRoleDao userRoleDao;

  @Mock
  PentahoOAuthProviderFactory pentahoOAuthProviderFactory;

  @Mock
  AzurePentahoOAuthHandler azurePentahoOAuthHandler;

  @Test
  public void testReadAllUsers() {
    PentahoOAuthUserSync pentahoOAuthUserSync =
      spy( new PentahoOAuthUserSync( userRoleDao, pentahoOAuthProviderFactory ) );
    pentahoOAuthUserSync.readAllUsers();

    verify( userRoleDao, times( 1 ) ).getAllOAuthUsers();
  }

  @Test
  public void testPerformSync() {
    PentahoOAuthUserSync pentahoOAuthUserSync =
      spy( new PentahoOAuthUserSync( userRoleDao, pentahoOAuthProviderFactory ) );
    pentahoOAuthUserSync.performSync();

    verify( azurePentahoOAuthHandler, times( 0 ) ).performSyncForUser( any() );
  }

  @Test
  public void testPerformSyncForUser() {
    PentahoOAuthUserSync pentahoOAuthUserSync =
      spy( new PentahoOAuthUserSync( userRoleDao, pentahoOAuthProviderFactory ) );
    PentahoOAuthUser pentahoOAuthUser =
      new PentahoOAuthUser( new PentahoUser( "admin", "password", "", true ), "azure", "" );
    when( pentahoOAuthProviderFactory.getInstance( "azure" ) ).thenReturn( azurePentahoOAuthHandler );
    pentahoOAuthUserSync.performSyncForUser( pentahoOAuthUser );

    verify( azurePentahoOAuthHandler, times( 1 ) ).performSyncForUser( any() );
  }

}
