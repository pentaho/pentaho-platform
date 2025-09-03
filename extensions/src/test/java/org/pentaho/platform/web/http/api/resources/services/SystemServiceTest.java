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


package org.pentaho.platform.web.http.api.resources.services;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SystemServiceTest {

  private SystemService systemService;

  @Before
  public void setUp() throws Exception {
    systemService = spy( new SystemService() );
  }

  @After
  public void tearDown() throws Exception {
    systemService = null;
  }

  @Test
  public void testGetUsers() throws Exception {
    IUserRoleListService service = mock( IUserRoleListService.class );
    PentahoSystem.registerObject( service );

    doReturn( true ).when( systemService ).canAdminister();
    assertNotNull( systemService.getUsers() );

    PentahoSystem.clearObjectFactory();
  }

  @Test
  public void testGetUsersNoPermission() throws Exception {
    IUserRoleListService service = mock( IUserRoleListService.class );
    PentahoSystem.registerObject( service );

    doReturn( false ).when( systemService ).canAdminister();
    try {
      systemService.getUsers();
      fail();
    } catch ( IllegalAccessException e ) {
      //expected exception
    }

    PentahoSystem.clearObjectFactory();
  }
}
