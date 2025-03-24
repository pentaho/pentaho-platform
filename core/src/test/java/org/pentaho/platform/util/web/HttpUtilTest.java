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

package org.pentaho.platform.util.web;

import org.junit.*;
import org.mockito.MockedStatic;
import org.pentaho.platform.api.engine.ISystemConfig;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class HttpUtilTest {
  HttpUtil httpUtil = mock( HttpUtil.class );

  @Test
  public void testIsValidURLMatchingURL() {
    try ( MockedStatic<PentahoSystem> pentahoSystemMockedStatic = mockStatic( PentahoSystem.class ) ) {
      ISystemConfig systemSettings = mock( ISystemConfig.class );
      pentahoSystemMockedStatic.when( () -> PentahoSystem.get( ISystemConfig.class ) ).thenReturn( systemSettings );
      when( systemSettings.getProperty( "system.ssrf-protection-enabled", "false" ) ).thenReturn( "true" );
      when( systemSettings.getProperty( "server.alternative-fully-qualified-server-urls" ) )
        .thenReturn( "https://www.google.com, http://192.168.10.1:8080/pentaho/, https://192.168.10.1:8443/pentaho/, "
          + "http://localhost" );
      assertEquals( "true", systemSettings.getProperty( "system.ssrf-protection-enabled",
        "false" ) );
      assertTrue( httpUtil.isValidURL( "https://192.168.10.1" ) );
    }
  }

  @Test
  public void testIsValidURLNoMatchingURL() {
    try ( MockedStatic<PentahoSystem> pentahoSystemMockedStatic = mockStatic( PentahoSystem.class ) ) {
      ISystemConfig systemSettings = mock( ISystemConfig.class );
      pentahoSystemMockedStatic.when( () -> PentahoSystem.get( ISystemConfig.class ) ).thenReturn( systemSettings );
      when( systemSettings.getProperty( "system.ssrf-protection-enabled", "false" ) )
        .thenReturn( "true" );
      when( systemSettings.getProperty( "server.alternative-fully-qualified-server-urls" ) )
        .thenReturn( "https://www.google.com, http://192.168.10.1:8080/pentaho/, https://192.168.10.1:8443/pentaho/" );
      assertEquals( "true", systemSettings.getProperty( "system.ssrf-protection-enabled",
        "false" ) );
      assertFalse( httpUtil.isValidURL(  "http://www.pentaho.com" ) );
    }
  }

  @Test
  public void testIllegalArgumentExceptionDueToMalformedURL() {
    try ( MockedStatic<PentahoSystem> pentahoSystemMockedStatic = mockStatic( PentahoSystem.class ) ) {
      ISystemConfig systemSettings = mock( ISystemConfig.class );
      pentahoSystemMockedStatic.when( () -> PentahoSystem.get( ISystemConfig.class ) ).thenReturn( systemSettings );
      when( systemSettings.getProperty( "system.ssrf-protection-enabled", "false" ) )
        .thenReturn( "true" );
      when( systemSettings.getProperty( "server.alternative-fully-qualified-server-urls" ) )
        .thenReturn( "http://www.google.com, http://192.168.10.1:8080/pentaho/, https://192.168.10.1:8443/pentaho/" );
      assertEquals( "true", systemSettings.getProperty( "system.ssrf-protection-enabled",
        "false" ) );
      assertThrows( IllegalArgumentException.class, () -> {
        httpUtil.getURLInputStream(  "pentaho.com" );
      } );
    }
  }

  @Test
  public void testIsValidURLNoSsrfProtection() {
    try ( MockedStatic<PentahoSystem> pentahoSystemMockedStatic = mockStatic( PentahoSystem.class ) ) {
      ISystemConfig systemSettings = mock( ISystemConfig.class );
      pentahoSystemMockedStatic.when( () -> PentahoSystem.get( ISystemConfig.class ) ).thenReturn( systemSettings );
      when( systemSettings.getProperty( "system.ssrf-protection-enabled", "false" ) )
        .thenReturn( "false" );
      assertEquals( "false", systemSettings.getProperty( "system.ssrf-protection-enabled",
        "false" ) );
      assertTrue( httpUtil.isValidURL(   "http://www.anydomain.com" ) );
    }
  }

}
