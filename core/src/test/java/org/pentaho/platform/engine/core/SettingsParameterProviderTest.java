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


package org.pentaho.platform.engine.core;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.platform.engine.core.solution.CustomSettingsParameterProvider;
import org.pentaho.platform.engine.core.solution.SystemSettingsParameterProvider;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.boot.PlatformInitializationException;
import org.pentaho.test.platform.engine.core.MicroPlatform;

import junit.framework.Assert;

@SuppressWarnings( { "all" } )
public class SettingsParameterProviderTest {

  public static final String SOLUTION_PATH = "src/test/resources/solution";

  @BeforeClass
  public static void beforeClass() throws PlatformInitializationException {
    new MicroPlatform( SOLUTION_PATH ).start();
  }

  @Test
  public void testSystemSettingsParameterProvider1() throws PlatformInitializationException {
    // pull a string from pentaho.xml
    String value = SystemSettingsParameterProvider.getSystemSetting( "pentaho.xml{pentaho-system/log-file}" );

    Assert.assertEquals( "Could not get setting from pentaho.xml", "server.log", value );
  }

  @Test
  public void testSystemSettingsParameterProvider2() throws PlatformInitializationException {
    // pull a string from pentaho.xml
    SystemSettingsParameterProvider provider = new SystemSettingsParameterProvider();

    String value = provider.getStringParameter( "pentaho.xml{pentaho-system/log-file}", null );
    Assert.assertEquals( "Could not get setting from pentaho.xml", "server.log", value );

    value = (String) provider.getParameter( "pentaho.xml{pentaho-system/log-file}" );
    Assert.assertEquals( "Could not get setting from pentaho.xml", "server.log", value );

    Assert.assertFalse( provider.getParameterNames().hasNext() );
  }

  @Test
  public void testSystemSettingsParameterProvider3() {
    // pull a string from pentaho.xml
    SystemSettingsParameterProvider provider = new SystemSettingsParameterProvider();

    String value = provider.getStringParameter( "bogus.xml{pentaho-system/log-file}", null );

    Assert.assertEquals( "Expected null result", null, value );
  }

  @Test
  public void testSystemSettingsParameterProvider4() {
    // pull a string from pentaho.xml
    SystemSettingsParameterProvider provider = new SystemSettingsParameterProvider();

    String value = provider.getStringParameter( "pentaho.xml{bogus}", null );

    Assert.assertEquals( "Expected null result", null, value );
  }

  @Test
  public void testCustomSettingsParameterProvider() {
    CustomSettingsParameterProvider provider = new CustomSettingsParameterProvider();

    String value = provider.getStringParameter( "settings.xml{settings/setting1}", null );

    Assert.assertEquals( "Could not get setting from pentaho.xml", "value1", value );

    StandaloneSession session = new StandaloneSession( "user1" );
    provider.setSession( session );
    value = (String) provider.getParameter( "settings.xml{settings/{$user}/setting2}" );
    Assert.assertEquals( "Wrong setting value", "value2", value );

    Assert.assertFalse( provider.getParameterNames().hasNext() );

  }

}
