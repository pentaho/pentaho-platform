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

package org.pentaho.platform.security.policy.rolebased.actions;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.pentaho.platform.security.policy.rolebased.messages.Messages;
import org.pentaho.platform.util.messages.LocaleHelper;

import java.util.Locale;
import java.util.ResourceBundle;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AbstractLocalizedAuthorizationActionTest {
  private ResourceBundle resourceBundle;
  private AbstractLocalizedAuthorizationAction action;

  @Before
  public void setUp() {
    action = mock( AbstractLocalizedAuthorizationAction.class, Mockito.CALLS_REAL_METHODS );
    String ACTION_NAME = "org.pentaho.action";
    when( action.getName() ).thenReturn( ACTION_NAME );

    resourceBundle = mock( ResourceBundle.class );
    when( resourceBundle.getString( ACTION_NAME ) ).thenReturn( "Action Name" );
    when( resourceBundle.getString( ACTION_NAME + ".description" ) ).thenReturn( "Action Description" );
  }

  @Test
  public void testGetResourceBundleLocaleString() {
    try ( MockedStatic<Messages> messagesMockedStatic = Mockito.mockStatic( Messages.class ) ) {
      Messages messagesMock = mock( Messages.class );
      when( messagesMock.getBundle( Locale.US ) ).thenReturn( resourceBundle );
      messagesMockedStatic.when( Messages::getInstance ).thenReturn( messagesMock );

      Assert.assertNotNull( action.getResourceBundle( Locale.US.toString() ) );
      verify( messagesMock ).getBundle( Locale.US );
    }
  }

  @Test
  public void testGetResourceBundleLocale() {
    try ( MockedStatic<Messages> messagesMockedStatic = Mockito.mockStatic( Messages.class ) ) {
      Messages messagesMock = mock( Messages.class );
      when( messagesMock.getBundle( Locale.US ) ).thenReturn( resourceBundle );
      messagesMockedStatic.when( Messages::getInstance ).thenReturn( messagesMock );

      Assert.assertNotNull( action.getResourceBundle( Locale.US ) );
      verify( messagesMock ).getBundle( Locale.US );
    }
  }

  @Test
  public void testGetResourceBundleWithNullLocale() {
    try ( MockedStatic<Messages> messagesMockedStatic = Mockito.mockStatic( Messages.class ) ) {
      Messages messagesMock = mock( Messages.class );
      when( messagesMock.getBundle( LocaleHelper.getLocale() ) ).thenReturn( resourceBundle );
      messagesMockedStatic.when( Messages::getInstance ).thenReturn( messagesMock );

      Assert.assertNotNull( action.getResourceBundle( (String) null ) );
      verify( messagesMock ).getBundle( LocaleHelper.getLocale() );

      Assert.assertNotNull( action.getResourceBundle( (Locale) null ) );
      verify( messagesMock, times( 2 ) ).getBundle( LocaleHelper.getLocale() );
    }
  }

  @Test
  public void testGetResourceBundleWithEmptyLocale() {
    try ( MockedStatic<Messages> messagesMockedStatic = Mockito.mockStatic( Messages.class ) ) {
      Messages messagesMock = mock( Messages.class );
      when( messagesMock.getBundle( LocaleHelper.getLocale() ) ).thenReturn( resourceBundle );
      messagesMockedStatic.when( Messages::getInstance ).thenReturn( messagesMock );

      Assert.assertNotNull( action.getResourceBundle( "" ) );
      verify( messagesMock ).getBundle( LocaleHelper.getLocale() );
    }
  }

  @Test
  public void testParseLocaleNull() {
    Assert.assertEquals( null, action.parseLocale( null ) );
  }

  @Test
  public void testParseLocaleEmpty() {
    Assert.assertEquals( null, action.parseLocale( "" ) );
  }

  @Test
  public void testParseLocaleOneToken() {
    Assert.assertEquals( Locale.ENGLISH, action.parseLocale( "en" ) );
  }

  @Test
  public void testParseLocaleTwoTokens() {
    Assert.assertEquals( Locale.FRANCE, action.parseLocale( "fr_FR" ) );
  }

  @Test
  public void testParseLocaleThreeTokens() {
    Assert.assertEquals( "de_DE_DE", action.parseLocale( "de_DE_DE" ).toString() );
  }

  @Test
  public void testGetLocalizedDisplayName() {
    try ( MockedStatic<Messages> messagesMockedStatic = Mockito.mockStatic( Messages.class ) ) {
      Messages messagesMock = mock( Messages.class );
      when( messagesMock.getBundle( Locale.US ) ).thenReturn( resourceBundle );
      messagesMockedStatic.when( Messages::getInstance ).thenReturn( messagesMock );

      Assert.assertEquals( "Action Name", action.getLocalizedDisplayName( Locale.US.toString() ) );
      verify( messagesMock ).getBundle( Locale.US );
    }
  }

  @Test
  public void testGetLocalizedDescription() {
    try ( MockedStatic<Messages> messagesMockedStatic = Mockito.mockStatic( Messages.class ) ) {
      Messages messagesMock = mock( Messages.class );
      when( messagesMock.getBundle( Locale.US ) ).thenReturn( resourceBundle );
      messagesMockedStatic.when( Messages::getInstance ).thenReturn( messagesMock );

      Assert.assertEquals( "Action Description", action.getLocalizedDescription( Locale.US.toString() ) );
      verify( messagesMock ).getBundle( Locale.US );
    }
  }
}
