/*
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
 * Copyright 2013 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.platform.osgi;

import org.junit.AfterClass;
import org.junit.Test;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.objfac.AggregateObjectFactory;

import static junit.framework.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA. User: nbaker Date: 11/19/13 Time: 10:48 PM To change this template use File | Settings |
 * File Templates.
 */
public class OSGIBootTest {
  @Test
  public void testStartup() throws Exception {
    PentahoSystem.setApplicationContext( new StandaloneApplicationContext( "test-res/osgiSystem", "test-res/osgiSystem" ) );
    OSGIBoot boot = new OSGIBoot();
    boot.startup( new StandaloneSession() );
    assertEquals( org.osgi.framework.Bundle.ACTIVE, boot.framework.getState() );
  }

  @AfterClass
  public static void after() {
    ( (AggregateObjectFactory) PentahoSystem.getObjectFactory() ).clear();
  }
}
