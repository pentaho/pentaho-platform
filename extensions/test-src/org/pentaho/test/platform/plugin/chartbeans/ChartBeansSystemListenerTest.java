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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.test.platform.plugin.chartbeans;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.plugin.action.chartbeans.ChartBeansSystemListener;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.pentaho.test.platform.engine.core.XmlSimpleSystemSettings;

@SuppressWarnings( "nls" )
public class ChartBeansSystemListenerTest {

  /**
   * @throws java.lang.Exception
   */
  @SuppressWarnings( "deprecation" )
  @Before
  public void setUp() throws Exception {

    String solutionsRelativePath = "test-src/org/pentaho/test/platform/plugin/chartbeans/solutions";
    MicroPlatform mp = new MicroPlatform( solutionsRelativePath );
    mp.define( ISolutionEngine.class, Object.class );
    ISystemSettings settings = new XmlSimpleSystemSettings();
    mp.setSettingsProvider( settings );
    mp.init();
  }

  /**
   * Test method for
   * {@link org.pentaho.platform.plugin.action.chartbeans.ChartBeansSystemListener#
   * startup(org.pentaho.platform.api.engine.IPentahoSession)}
   * .
   */
  @Test
  public void testStartup() {
    IPentahoSystemListener sl = new ChartBeansSystemListener();
    Assert.assertTrue( sl.startup( null ) );
  }

  /**
   * Test method for startup from file with no chart engines defined.
   */
  @Test
  public void testBadStartupFile() {
    ChartBeansSystemListener sl = new ChartBeansSystemListener();
    sl.setConfigFile( "chartbeans/chartbeans_config_no_engines.xml" );
    Assert.assertFalse( sl.startup( null ) );
  }

  /**
   * Test method for {@link org.pentaho.platform.plugin.action.chartbeans.ChartBeansSystemListener#shutdown()}.
   */
  @Test
  public void testShutdown() {
    // no shutdown code today
  }

}
