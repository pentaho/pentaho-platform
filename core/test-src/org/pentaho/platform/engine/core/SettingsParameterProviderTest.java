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
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.core;

import junit.framework.TestCase;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.engine.core.solution.CustomSettingsParameterProvider;
import org.pentaho.platform.engine.core.solution.SystemSettingsParameterProvider;
import org.pentaho.platform.engine.core.system.PathBasedSystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.objfac.StandaloneSpringPentahoObjectFactory;

import java.io.File;

@SuppressWarnings( { "all" } )
public class SettingsParameterProviderTest extends TestCase {

  public static final String SOLUTION_PATH = "test-res/solution";
  private static final String ALT_SOLUTION_PATH = "test-res/solution";
  private static final String PENTAHO_XML_PATH = "/system/pentaho.xml";
  final String SYSTEM_FOLDER = "/system";
  private static final String DEFAULT_SPRING_CONFIG_FILE_NAME = "pentahoObjects.spring.xml";

  public String getSolutionPath() {
    File file = new File( SOLUTION_PATH + PENTAHO_XML_PATH );
    if ( file.exists() ) {
      System.out.println( "File exist returning " + SOLUTION_PATH );
      return SOLUTION_PATH;
    } else {
      System.out.println( "File does not exist returning " + ALT_SOLUTION_PATH );
      return ALT_SOLUTION_PATH;
    }
  }

  private void init() {
    if ( !PentahoSystem.getInitializedOK() ) {
      PentahoSystem.setSystemSettingsService( new PathBasedSystemSettings() );
      StandaloneApplicationContext applicationContext = new StandaloneApplicationContext( getSolutionPath(), "" ); //$NON-NLS-1$
      String objectFactoryCreatorCfgFile = getSolutionPath() + SYSTEM_FOLDER + "/" + DEFAULT_SPRING_CONFIG_FILE_NAME; //$NON-NLS-1$
      IPentahoObjectFactory pentahoObjectFactory = new StandaloneSpringPentahoObjectFactory();
      pentahoObjectFactory.init( objectFactoryCreatorCfgFile, null );
      PentahoSystem.registerObjectFactory( pentahoObjectFactory );
      PentahoSystem.init( applicationContext );
    }
  }

  public void testSystemSettingsParameterProvider1() {

    init();
    // pull a string from pentaho.xml
    String value = SystemSettingsParameterProvider.getSystemSetting( "pentaho.xml{pentaho-system/log-file}" );

    assertEquals( "Could not get setting from pentaho.xml", "server.log", value );
  }

  public void testSystemSettingsParameterProvider2() {

    init();
    // pull a string from pentaho.xml
    SystemSettingsParameterProvider provider = new SystemSettingsParameterProvider();

    String value = provider.getStringParameter( "pentaho.xml{pentaho-system/log-file}", null );

    assertEquals( "Could not get setting from pentaho.xml", "server.log", value );

    value = (String) provider.getParameter( "pentaho.xml{pentaho-system/log-file}" );
    assertEquals( "Could not get setting from pentaho.xml", "server.log", value );

    assertFalse( provider.getParameterNames().hasNext() );
  }

  public void testSystemSettingsParameterProvider3() {

    init();
    // pull a string from pentaho.xml
    SystemSettingsParameterProvider provider = new SystemSettingsParameterProvider();

    String value = provider.getStringParameter( "bogus.xml{pentaho-system/log-file}", null );

    assertEquals( "Expected null result", null, value );
  }

  public void testSystemSettingsParameterProvider4() {

    init();
    // pull a string from pentaho.xml
    SystemSettingsParameterProvider provider = new SystemSettingsParameterProvider();

    String value = provider.getStringParameter( "pentaho.xml{bogus}", null );

    assertEquals( "Expected null result", null, value );
  }

  public void testCustomSettingsParameterProvider() {

    init();
    // pull a string from pentaho.xml
    CustomSettingsParameterProvider provider = new CustomSettingsParameterProvider();

    String value = provider.getStringParameter( "settings.xml{settings/setting1}", null );

    assertEquals( "Could not get setting from pentaho.xml", "value1", value );

    StandaloneSession session = new StandaloneSession( "user1" );
    provider.setSession( session );
    value = (String) provider.getParameter( "settings.xml{settings/{$user}/setting2}" );
    assertEquals( "Wrong setting value", "value2", value );

    assertFalse( provider.getParameterNames().hasNext() );

  }

}
