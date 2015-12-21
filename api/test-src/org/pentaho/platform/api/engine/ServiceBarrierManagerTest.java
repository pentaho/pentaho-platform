/*!
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
 * Copyright (c) 2002-2015 Pentaho Corporation..  All rights reserved.
 */
package org.pentaho.platform.api.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IServerStatusProvider.ServerStatus;
import org.pentaho.platform.api.engine.ServerStatusProviderTest.MockPluggedInProvider;

public class ServiceBarrierManagerTest {
  private static final String MANAGER_CLASS = "org.pentaho.platform.api.engine.IServiceBarrierManager.class";

  IServiceBarrierManager manager;
  IServiceBarrier barrier1;
  IServiceBarrier barrier2;

  @Before
  public void setup() {
    IServiceBarrierManager.LOCATOR.instance = null;
    System.clearProperty( MANAGER_CLASS );
    manager = IServiceBarrierManager.LOCATOR.getManager();
    barrier1 = manager.getServiceBarrier( "1" );
    barrier2 = manager.getServiceBarrier( "2" );
  }

  @Test
  public void testGetServiceBarrier() throws Exception {
    IServiceBarrier barrier1a = manager.getServiceBarrier( "1" );

    assertSame( barrier1, barrier1a );
    assertNotSame( barrier1, barrier2 );
  }

  @Test
  public void testGetAllServiceBarriers() throws Exception {
    assertEquals( 2, manager.getAllServiceBarriers().size() );
    assertTrue( manager.getAllServiceBarriers().contains( barrier1 ) );
    assertTrue( manager.getAllServiceBarriers().contains( barrier2 ) );
  }

  @Test
  public void testExceptions() {
    IServiceBarrierManager.LOCATOR.instance = null;
    System.setProperty( MANAGER_CLASS, "foo" );
    manager = IServiceBarrierManager.LOCATOR.getManager();
    assertNotNull( manager );
    assertTrue( manager instanceof ServiceBarrierManager );
  }

  @Test
  public void testPluggableClass() {
    IServiceBarrierManager.LOCATOR.instance = null;
    System.setProperty( MANAGER_CLASS, MockPluggedInManager.class.getName() );
    manager = IServiceBarrierManager.LOCATOR.getManager();
    assertNotNull( manager );
    assertTrue( manager instanceof MockPluggedInManager );
  }

  public static class MockPluggedInManager implements IServiceBarrierManager {

    @Override
    public IServiceBarrier getServiceBarrier( String serviceID ) {
      return null;
    }

    @Override
    public List<IServiceBarrier> getAllServiceBarriers() {
      return null;
    }

  }

}
