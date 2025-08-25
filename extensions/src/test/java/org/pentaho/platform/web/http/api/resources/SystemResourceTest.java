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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.platform.api.usersettings.IUserSettingService;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.springframework.util.Assert;

import jakarta.ws.rs.core.Response;

import static org.mockito.Mockito.mock;

public class SystemResourceTest {
  SystemResource systemResource;
  private static MicroPlatform platform;

  @BeforeClass
  public static void initPlatform() throws Exception {
    platform = new MicroPlatform();
    platform.defineInstance( IUserSettingService.class, mock( IUserSettingService.class ) );
    platform.start();
  }

  @Before
  public void setup() {
    PentahoSessionHolder.setSession( null );
    systemResource = new SystemResource();
  }

  @After
  public void teardown() {
    systemResource = null;
  }

  @AfterClass
  public static void shutdownPlatform() {
    platform.stop();
  }

  @Test
  public void testSetLocaleOverride() {
    Response resp = null;
    try {
      resp = systemResource.setLocaleOverride( "en_US" );
    } catch ( Exception e ) {

    }
    Assert.notNull( resp, "Response must not be null" );
  }
}
