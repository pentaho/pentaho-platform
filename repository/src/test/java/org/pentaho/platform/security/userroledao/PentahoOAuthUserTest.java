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

package org.pentaho.platform.security.userroledao;

import org.apache.tika.utils.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.api.mt.ITenant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotEquals;

@RunWith( MockitoJUnitRunner.class )
public class PentahoOAuthUserTest {

  @Mock
  ITenant tenant;

  @Test
  public void getRegistrationId_shouldReturnCorrectValue() {
    PentahoUser pentahoUser = new PentahoUser( tenant, "username", "password", "description", true );
    PentahoOAuthUser oAuthUser = new PentahoOAuthUser( pentahoUser, "registrationId", "userId" );

    assertEquals( "registrationId", oAuthUser.getRegistrationId() );
  }

  @Test
  public void setRegistrationId_shouldUpdateValue() {
    PentahoUser pentahoUser = new PentahoUser( tenant, "username", "password", "description", true );
    PentahoOAuthUser oAuthUser = new PentahoOAuthUser( pentahoUser, "registrationId", "userId" );

    oAuthUser.setRegistrationId( "newRegistrationId" );

    assertEquals( "newRegistrationId", oAuthUser.getRegistrationId() );
  }

  @Test
  public void getUserId_shouldReturnCorrectValue() {
    PentahoUser pentahoUser = new PentahoUser( tenant, "username", "password", "description", true );
    PentahoOAuthUser oAuthUser = new PentahoOAuthUser( pentahoUser, "registrationId", "userId" );

    assertEquals( "userId", oAuthUser.getUserId() );
  }

  @Test
  public void setUserId_shouldUpdateValue() {
    PentahoUser pentahoUser = new PentahoUser( tenant, "username", "password", "description", true );
    PentahoOAuthUser oAuthUser = new PentahoOAuthUser( pentahoUser, "registrationId", "userId" );

    oAuthUser.setUserId( "newUserId" );

    assertEquals( "newUserId", oAuthUser.getUserId() );
  }

  @Test
  public void constructor_shouldInitializeAllFieldsCorrectly() {
    PentahoUser pentahoUser = new PentahoUser( tenant, "username", "password", "description", true );
    PentahoOAuthUser oAuthUser = new PentahoOAuthUser( pentahoUser, "registrationId", "userId" );

    assertEquals( tenant, oAuthUser.getTenant() );
    assertEquals( "username", oAuthUser.getUsername() );
    assertEquals( "password", oAuthUser.getPassword() );
    assertEquals( "description", oAuthUser.getDescription() );
    assertTrue( oAuthUser.isEnabled() );
    assertEquals( "registrationId", oAuthUser.getRegistrationId() );
    assertEquals( "userId", oAuthUser.getUserId() );
  }

  @Test
  public void equalsReturnsTrueForIdenticalObjects() {
    PentahoUser pentahoUser = new PentahoUser( tenant, "username", "password", "description", true );
    PentahoOAuthUser oAuthUser1 = new PentahoOAuthUser( pentahoUser, "registrationId", "userId" );
    PentahoOAuthUser oAuthUser2 = new PentahoOAuthUser( pentahoUser, "registrationId", "userId" );

    assertEquals( oAuthUser1,  oAuthUser2 );
  }

  @Test
  public void equalsReturnsFalseForDifferentRegistrationId() {
    PentahoUser pentahoUser = new PentahoUser( tenant, "username", "password", "description", true );
    PentahoOAuthUser oAuthUser1 = new PentahoOAuthUser( pentahoUser, "registrationId1", "userId" );
    PentahoOAuthUser oAuthUser2 = new PentahoOAuthUser( pentahoUser, "registrationId2", "userId" );

    assertNotEquals( oAuthUser1, oAuthUser2 );
  }

  @Test
  public void equalsReturnsFalseForDifferentUserId() {
    PentahoUser pentahoUser = new PentahoUser( tenant, "username", "password", "description", true );
    PentahoOAuthUser oAuthUser1 = new PentahoOAuthUser( pentahoUser, "registrationId", "userId1" );
    PentahoOAuthUser oAuthUser2 = new PentahoOAuthUser( pentahoUser, "registrationId", "userId2" );

    assertNotEquals( oAuthUser1, oAuthUser2 );
  }

  @Test
  public void hashCodeIsConsistentForEqualObjects() {
    PentahoUser pentahoUser = new PentahoUser( tenant, "username", "password", "description", true );
    PentahoOAuthUser oAuthUser1 = new PentahoOAuthUser( pentahoUser, "registrationId", "userId" );
    PentahoOAuthUser oAuthUser2 = new PentahoOAuthUser( pentahoUser, "registrationId", "userId" );

    assertEquals( oAuthUser1.hashCode(), oAuthUser2.hashCode() );
  }

  @Test
  public void hashCodeDiffersForDifferentObjects() {
    PentahoUser pentahoUser = new PentahoUser( tenant, "username", "password", "description", true );
    PentahoOAuthUser oAuthUser1 = new PentahoOAuthUser( pentahoUser, "registrationId1", "userId" );
    PentahoOAuthUser oAuthUser2 = new PentahoOAuthUser( pentahoUser, "registrationId2", "userId" );

    assertNotEquals( oAuthUser1.hashCode(), oAuthUser2.hashCode() );
  }

  @Test
  public void hashCodeDiffersForAnotherObject() {
    PentahoOAuthUser oAuthUser2 = new PentahoOAuthUser( new PentahoUser( "Hi" ), "registrationId2", "userId" );
    assertNotEquals( oAuthUser2, StringUtils.EMPTY );
  }

  @Test
  public void hashCodeDiffersForDiffUserameObject() {
    PentahoOAuthUser oAuthUser1 = new PentahoOAuthUser( new PentahoUser( "Hi" ), "registrationId2", "userId" );
    PentahoOAuthUser oAuthUser2 = new PentahoOAuthUser( new PentahoUser( "Hello" ), "registrationId2", "userId" );

    assertNotEquals( oAuthUser2,  oAuthUser1 );
  }

}