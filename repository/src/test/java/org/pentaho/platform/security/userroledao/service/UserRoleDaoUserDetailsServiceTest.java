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

package org.pentaho.platform.security.userroledao.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.mt.ITenant;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@RunWith( MockitoJUnitRunner.class )
public class UserRoleDaoUserDetailsServiceTest {

  @Mock
  ITenant tenant;

  @Mock
  IUserRoleDao userRoleDao;

  @Test
  public void testGetPentahoOAuthUser() {
    UserRoleDaoUserDetailsService userDetailsService = new UserRoleDaoUserDetailsService();
    userDetailsService.setUserRoleDao( userRoleDao );
    userDetailsService.getPentahoOAuthUser( tenant, "test" );
    verify( userRoleDao, times( 1 ) ).getPentahoOAuthUser( tenant, "test" );
  }

}
