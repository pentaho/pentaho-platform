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


package org.pentaho.platform.settings;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author Joao L. M. Pereira
 *
 */
public class ServerPortTest {
  private static final String ID = "id";
  private static final String FRIENDLY_NAME = "friendlyName";
  private static final Integer ASSIGNED_PORT = 123456;
  private static final Integer NEW_ASSIGNED_PORT = 7548;
  private static final Integer START_PORT = 123450;
  private static final String SERVICE_NAME = "serviceName";

  @Test
  public void testParameters() {
    ServerPort serverPort = new ServerPort( ID, FRIENDLY_NAME, START_PORT );

    Assert.assertEquals( serverPort.getId(), ID );
    Assert.assertEquals( serverPort.getFriendlyName(), FRIENDLY_NAME );
    Assert.assertEquals( serverPort.getStartPort(), START_PORT );
    Assert.assertEquals( serverPort.getServiceName(), "" );

    serverPort = new ServerPort( ID, FRIENDLY_NAME, START_PORT, SERVICE_NAME );
    Assert.assertEquals( serverPort.getId(), ID );
    Assert.assertEquals( serverPort.getFriendlyName(), FRIENDLY_NAME );
    Assert.assertEquals( serverPort.getStartPort(), START_PORT );
    Assert.assertEquals( serverPort.getServiceName(), SERVICE_NAME );
  }

  @Test
  public void testFriendlyName() {
    ServerPort serverPort = new ServerPort( ID, FRIENDLY_NAME, START_PORT );
    Assert.assertEquals( serverPort.getFriendlyName(), FRIENDLY_NAME );

    String newFriendlyName = "New Friendly Name";
    serverPort.setFriendlyName( newFriendlyName );
    Assert.assertEquals( serverPort.getFriendlyName(), newFriendlyName );

    serverPort.setFriendlyName( "" );
    Assert.assertEquals( serverPort.getFriendlyName(), ID );
  }

  @Test
  public void testAssignedPort() {
    ServerPort serverPort = new ServerPort( ID, FRIENDLY_NAME, START_PORT );
    PortFileManager portFileManager = PortFileManager.getInstance();
    portFileManager.clear();
    Assert.assertEquals( serverPort.getAssignedPort(), null );
    Assert.assertFalse( portFileManager.removePort( ASSIGNED_PORT ) );

    serverPort.setAssignedPort( ASSIGNED_PORT );
    Assert.assertEquals( ASSIGNED_PORT, serverPort.getAssignedPort() );
    Assert.assertTrue( portFileManager.removePort( ASSIGNED_PORT ) );
    Assert.assertTrue( portFileManager.addPort( ASSIGNED_PORT ) );

    serverPort.setAssignedPort( NEW_ASSIGNED_PORT );
    Assert.assertEquals( NEW_ASSIGNED_PORT, serverPort.getAssignedPort() );
    Assert.assertFalse( portFileManager.removePort( ASSIGNED_PORT ) );
    Assert.assertTrue( portFileManager.removePort( NEW_ASSIGNED_PORT ) );
    Assert.assertTrue( portFileManager.addPort( NEW_ASSIGNED_PORT ) );

    serverPort.releasePort();
    Assert.assertEquals( serverPort.getAssignedPort(), null );
    Assert.assertFalse( portFileManager.removePort( NEW_ASSIGNED_PORT ) );
  }

}
