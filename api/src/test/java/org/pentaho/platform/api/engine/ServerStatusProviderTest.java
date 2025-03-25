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


package org.pentaho.platform.api.engine;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

/**
 * This is the default implementation of server status log message provider.
 * 
 * @author tkafalas
 *
 */
@RunWith( MockitoJUnitRunner.class )
public class ServerStatusProviderTest {
  private IServerStatusProvider serverStatusProvider;
  static final String MESSAGE1 = "This is a test";
  private static final String PROVIDER_CLASS = "org.pentaho.platform.api.engine.IServerStatusProvider.class";

  private void setup() {
    IServerStatusProvider.LOCATOR.instance = null;
    System.clearProperty( PROVIDER_CLASS );
    serverStatusProvider = IServerStatusProvider.LOCATOR.getProvider();
    serverStatusProvider.setStatusMessages( new String[] { MESSAGE1 } );
    serverStatusProvider.setStatus( IServerStatusProvider.ServerStatus.STARTED );
  }

  @Test
  public void testSettings() {
    setup();
    assertEquals( MESSAGE1, serverStatusProvider.getStatusMessages()[0] );

    assertEquals( IServerStatusProvider.ServerStatus.STARTED, serverStatusProvider.getStatus() );
  }

  @Test
  public void testListener() {
    setup();
    IServerStatusChangeListener mockListener = mock( IServerStatusChangeListener.class );
    serverStatusProvider.registerServerStatusChangeListener( mockListener );
    serverStatusProvider.setStatus( IServerStatusProvider.ServerStatus.STOPPING );
    verify( mockListener, times( 1 ) ).onStatusChange();

    serverStatusProvider.removeServerStatusChangeListener( mockListener );
  }

  @Test
  public void testExceptions() {
    IServerStatusProvider.LOCATOR.instance = null;
    System.setProperty( PROVIDER_CLASS, "foo" );
    serverStatusProvider = IServerStatusProvider.LOCATOR.getProvider();
    assertNotNull( serverStatusProvider );
    assertTrue( serverStatusProvider instanceof ServerStatusProvider );
  }

  @Test
  public void testInstantiationExceptionException() {
    IServerStatusProvider.LOCATOR.instance = null;
    System.setProperty( PROVIDER_CLASS, "org.pentaho.platform.api.engine.IServerStatusProvider" );
    serverStatusProvider = IServerStatusProvider.LOCATOR.getProvider();
    assertNotNull( serverStatusProvider );
    assertTrue( serverStatusProvider instanceof ServerStatusProvider );
  }

  @Test
  public void testLocatorInstanceNotNull() {
    IServerStatusProvider.LOCATOR.instance = new MockPluggedInProvider();
    serverStatusProvider = IServerStatusProvider.LOCATOR.getProvider();
    assertNotNull( serverStatusProvider );
    assertTrue( serverStatusProvider instanceof MockPluggedInProvider );
  }

  @Test
  public void testServerStatusEnum() {
    assertNotNull( IServerStatusProvider.ServerStatus.valueOf( "DOWN" ) );
    assertNotNull( IServerStatusProvider.ServerStatus.valueOf( "STARTING" ) );
    assertNotNull( IServerStatusProvider.ServerStatus.valueOf( "STARTED" ) );
    assertNotNull( IServerStatusProvider.ServerStatus.valueOf( "STOPPING" ) );
    assertNotNull( IServerStatusProvider.ServerStatus.valueOf( "ERROR" ) );
  }

  @Test
  public void testPluggableClass() {
    IServerStatusProvider.LOCATOR.instance = null;
    System.setProperty( PROVIDER_CLASS, MockPluggedInProvider.class.getName() );
    serverStatusProvider = IServerStatusProvider.LOCATOR.getProvider();
    assertNotNull( serverStatusProvider );
    assertTrue( serverStatusProvider instanceof MockPluggedInProvider );
  }

  public static class MockPluggedInProvider implements IServerStatusProvider {

    @Override
    public ServerStatus getStatus() {
      return null;
    }

    @Override
    public void setStatus( ServerStatus serverStatus ) {

    }

    @Override
    public String[] getStatusMessages() {
      return null;
    }

    @Override
    public void setStatusMessages( String[] messages ) {

    }

    @Override
    public void registerServerStatusChangeListener( IServerStatusChangeListener serverStatusChangeListener ) {

    }

    @Override
    public void removeServerStatusChangeListener( IServerStatusChangeListener serverStatusChangeListener ) {

    }

  }
}
