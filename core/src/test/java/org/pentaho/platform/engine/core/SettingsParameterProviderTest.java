/*!
 *
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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

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
