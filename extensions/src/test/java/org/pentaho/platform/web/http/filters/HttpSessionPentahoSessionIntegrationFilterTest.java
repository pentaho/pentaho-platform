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


package org.pentaho.platform.web.http.filters;


import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.springframework.security.authentication.AuthenticationProvider;

import jakarta.servlet.ServletContext;
import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class HttpSessionPentahoSessionIntegrationFilterTest {

  private HttpServletResponse servletResponse;
  private HttpSession httpSession;
  private ServletContext servletContext;
  private SessionCookieConfig sessionCookieConfig;
  private IPentahoSession pentahoSession;

  @Before
  public void setUp() {
    servletResponse = Mockito.mock( HttpServletResponse.class );
    httpSession = Mockito.mock( HttpSession.class );
    pentahoSession = Mockito.mock( IPentahoSession.class );
    servletContext = Mockito.mock( ServletContext.class );
    sessionCookieConfig = Mockito.mock( SessionCookieConfig.class );
    Mockito.when( httpSession.getServletContext() ).thenReturn( servletContext );
    Mockito.when( servletContext.getSessionCookieConfig() ).thenReturn( sessionCookieConfig );
    PentahoSystem.clearObjectFactory();
  }

  @Test
  public void testSessionCookieNoHttpSession() {
    new HttpSessionPentahoSessionIntegrationFilter()
      .setSessionExpirationCookies( null, pentahoSession, servletResponse );
    Mockito.verify( servletResponse, Mockito.never() ).addCookie( Mockito.any() );
  }

  @Test
  public void testSessionCookieNoPentahoSession() {
    new HttpSessionPentahoSessionIntegrationFilter().setSessionExpirationCookies( httpSession, null, servletResponse );
    Mockito.verify( servletResponse, Mockito.never() ).addCookie( Mockito.any() );
  }

  @Test
  public void testSessionCookieDisabledInSettings() {
    final ISystemSettings systemSettings = PentahoSystem.getSystemSettings();
    try {
      final ISystemSettings mockSettings = Mockito.mock( ISystemSettings.class );
      Mockito.when( mockSettings.getSystemSetting( "session-expired-dialog", "true" ) ).thenReturn( "false" );
      PentahoSystem.setSystemSettingsService( mockSettings );
      new HttpSessionPentahoSessionIntegrationFilter()
        .setSessionExpirationCookies( httpSession, pentahoSession, servletResponse );
      Mockito.verify( servletResponse, Mockito.never() ).addCookie( Mockito.any() );
    } finally {
      PentahoSystem.setSystemSettingsService( systemSettings );
    }
  }

  @Test
  public void testSessionCookieNoAuthenticationProviders() {
    final ISystemSettings systemSettings = PentahoSystem.getSystemSettings();
    try {
      final ISystemSettings mockSettings = Mockito.mock( ISystemSettings.class );
      Mockito.when( mockSettings.getSystemSetting( "session-expired-dialog", "true" ) ).thenReturn( "true" );
      PentahoSystem.setSystemSettingsService( mockSettings );
      new HttpSessionPentahoSessionIntegrationFilter()
        .setSessionExpirationCookies( httpSession, pentahoSession, servletResponse );
      Mockito.verify( servletResponse, Mockito.never() ).addCookie( Mockito.any() );
    } finally {
      PentahoSystem.setSystemSettingsService( systemSettings );
    }
  }

  @Test
  public void testSessionCookieSsoEnabled() throws ObjectFactoryException {

    final ISystemSettings systemSettings = PentahoSystem.getSystemSettings();
    try {
      final ISystemSettings mockSettings = Mockito.mock( ISystemSettings.class );
      Mockito.when( mockSettings.getSystemSetting( "session-expired-dialog", "true" ) ).thenReturn( "true" );
      PentahoSystem.setSystemSettingsService( mockSettings );
      final CasAuthenticationProvider mockObj = Mockito.mock( CasAuthenticationProvider.class );
      PentahoSystem.registerObject( mockObj, AuthenticationProvider.class );
      HttpSessionPentahoSessionIntegrationFilter hspsif = new HttpSessionPentahoSessionIntegrationFilter();
      hspsif.setSsoEnabled( true );
      hspsif.setSessionExpirationCookies( httpSession, pentahoSession, servletResponse );
      Mockito.verify( servletResponse, Mockito.never() ).addCookie( Mockito.any() );
    } finally {
      PentahoSystem.setSystemSettingsService( systemSettings );
    }
  }

  @Test
  public void testSessionCookieNoSettings() throws ObjectFactoryException {
    final AuthenticationProvider mockObj = Mockito.mock( AuthenticationProvider.class );
    PentahoSystem.registerObject( mockObj, AuthenticationProvider.class );
    Mockito.when( sessionCookieConfig.isHttpOnly() ).thenReturn( false );
    Mockito.when( sessionCookieConfig.isSecure() ).thenReturn( false );
    new HttpSessionPentahoSessionIntegrationFilter()
      .setSessionExpirationCookies( httpSession, pentahoSession, servletResponse );
    Mockito.verify( servletResponse, Mockito.times( 2 ) ).addCookie( Mockito.any() );
  }

  @Test
  public void testSessionCookieSettingsEnabled() throws ObjectFactoryException {

    final ISystemSettings systemSettings = PentahoSystem.getSystemSettings();
    try {
      final ISystemSettings mockSettings = Mockito.mock( ISystemSettings.class );
      Mockito.when( mockSettings.getSystemSetting( "session-expired-dialog", "true" ) ).thenReturn( "true" );
      Mockito.when( sessionCookieConfig.isHttpOnly() ).thenReturn( false );
      Mockito.when( sessionCookieConfig.isSecure() ).thenReturn( false );
      PentahoSystem.setSystemSettingsService( mockSettings );
      final AuthenticationProvider mockObj = Mockito.mock( AuthenticationProvider.class );
      PentahoSystem.registerObject( mockObj, AuthenticationProvider.class );
      new HttpSessionPentahoSessionIntegrationFilter()
        .setSessionExpirationCookies( httpSession, pentahoSession, servletResponse );
      Mockito.verify( servletResponse, Mockito.times( 2 ) ).addCookie( Mockito.any() );
    } finally {
      PentahoSystem.setSystemSettingsService( systemSettings );
    }
  }

}
