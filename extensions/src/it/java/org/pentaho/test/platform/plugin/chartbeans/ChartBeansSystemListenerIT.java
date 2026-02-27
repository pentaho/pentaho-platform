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
import org.pentaho.test.platform.utils.TestResourceLocation;

@SuppressWarnings( "nls" )
public class ChartBeansSystemListenerIT {

  /**
   * @throws java.lang.Exception
   */
  @SuppressWarnings( "deprecation" )
  @Before
  public void setUp() throws Exception {

    String solutionsRelativePath = TestResourceLocation.TEST_RESOURCES + "/org/pentaho/test/platform/plugin/chartbeans/solutions";
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
