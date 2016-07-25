/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2016 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.api.engine;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

/**
 * This is the default implementation of server status log message provider.
 * 
 * @author tkafalas
 *
 */
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
