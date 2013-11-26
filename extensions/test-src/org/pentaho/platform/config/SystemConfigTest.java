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

package org.pentaho.platform.config;

import org.junit.Test;
import org.pentaho.platform.api.engine.IConfiguration;
import org.pentaho.platform.engine.core.system.PathBasedSystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.io.File;
import java.util.Properties;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * User: nbaker Date: 4/2/13
 */
public class SystemConfigTest {

  SystemConfig systemConfig = new SystemConfig();

  @Test
  public void testSetConfiguration() throws Exception {

    IConfiguration c =
        new PropertiesFileConfiguration( "test", new File( "test-res/SystemConfig/system/test.properties" ) );
    systemConfig.registerConfiguration( c );
    assertNotNull( systemConfig.getConfiguration( "test" ) );
  }

  @Test
  public void testGetConfiguration() throws Exception {

    IConfiguration c =
        new PropertiesFileConfiguration( "test", new File( "test-res/SystemConfig/system/test.properties" ) );
    systemConfig.registerConfiguration( c );

    assertNotNull( systemConfig.getConfiguration( "test" ) );
    assertEquals( "A dog's tale", systemConfig.getProperty( "test.someProperty" ) );

    assertEquals( "A dog's tale", systemConfig.getConfiguration( "test" ).getProperties().get( "someProperty" ) );
  }

  @Test
  public void testListConfigurations() throws Exception {

    IConfiguration c =
        new PropertiesFileConfiguration( "test", new File( "test-res/SystemConfig/system/test.properties" ) );
    systemConfig.registerConfiguration( c );
    assertEquals( 1, systemConfig.listConfigurations().length );

    c = new PropertiesFileConfiguration( "test", new File( "test-res/SystemConfig/system/test.properties" ) );
    systemConfig.registerConfiguration( c );
    assertEquals( 1, systemConfig.listConfigurations().length );

    c = new PropertiesFileConfiguration( "test2", new File( "test-res/SystemConfig/system/test.properties" ) );
    systemConfig.registerConfiguration( c );
    assertEquals( 2, systemConfig.listConfigurations().length );
  }

  @Test
  public void testSpring() throws Exception {
    PentahoSystem.clearObjectFactory();
    FileSystemXmlApplicationContext context =
        new FileSystemXmlApplicationContext( "test-res/SystemConfig/system/testPropertyPlaceholders.spring.xml" );
    context.refresh();

    assertNotNull( context.getBean( "testPlaceHolder" ) );
    assertEquals( "A dog's tale", context.getBean( "testPlaceHolder" ) );
  }

  @Test
  public void testWrite() throws Exception {
    PentahoSystem.clearObjectFactory();

    IConfiguration c =
        new PropertiesFileConfiguration( "test", new File( "test-res/SystemConfig/system/test.properties" ) );
    systemConfig.registerConfiguration( c );
    IConfiguration configuration = systemConfig.getConfiguration( "test" );
    Properties props = configuration.getProperties();
    props.setProperty( "someProperty", "new value" );
    configuration.update( props );

    FileSystemXmlApplicationContext context =
        new FileSystemXmlApplicationContext( "test-res/SystemConfig/system/testPropertyPlaceholders.spring.xml" );
    context.refresh();

    assertNotNull( context.getBean( "testPlaceHolder" ) );
    assertEquals( "new value", context.getBean( "testPlaceHolder" ) );
    props = configuration.getProperties();
    props.setProperty( "someProperty", "A dog's tale" );
    configuration.update( props );

  }

  @Test
  public void testSystemSettingsConfiguration() throws Exception {

    PentahoSystem.clearObjectFactory();
    PentahoSystem.setApplicationContext( new StandaloneApplicationContext( "test-res/SystemConfig", "" ) );
    SystemSettingsConfiguration settings = new SystemSettingsConfiguration( "system", new PathBasedSystemSettings() );
    Properties props = settings.getProperties();
    assertNotNull( props );
    assertEquals( "Hypersonic", props.get( "sampledata-datasource.type" ) );
  }

  @Test
  public void testSystemSettingsFromSpring() throws Exception {
    PentahoSystem.clearObjectFactory();
    PentahoSystem.setApplicationContext( new StandaloneApplicationContext( "test-res/SystemConfig", "" ) );

    FileSystemXmlApplicationContext context =
        new FileSystemXmlApplicationContext(
            "test-res/SystemConfig/system/testPropertyPlaceholders-system-settings.spring.xml" );
    context.refresh();

    assertNotNull( context.getBean( "testPlaceHolder" ) );
    assertEquals( "Hypersonic", context.getBean( "testPlaceHolder" ) );

    assertNotNull( context.getBean( "testPlaceHolder2" ) );
    assertEquals( "A dog's tale", context.getBean( "testPlaceHolder2" ) );
  }

}
