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

import javax.servlet.ServletContext;
import javax.servlet.SessionCookieConfig;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
