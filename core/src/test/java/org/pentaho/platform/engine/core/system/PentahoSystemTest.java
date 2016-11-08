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
 * Copyright 2006 - 2015 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.core.system;

import org.dom4j.Node;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.pentaho.platform.api.engine.IConfiguration;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoObjectReference;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISystemConfig;
import org.pentaho.platform.api.engine.ISystemSettings;

import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PentahoSystemTest {
  private final String testSystemPropertyName = "testSystemPropertyName";
  private final String testXMLValue = "testXMLValue";
  private final String testPropertyValue = "testPropertyValue";

  @After
  public void after() {
    PentahoSystem.shutdown();
    System.clearProperty( testSystemPropertyName );
  }

  /**
   * When there are settings in pentaho.xml, we should use it overwriting properties file
   */
  @Test
  public void initXMLFactoriesXMLTest() throws Exception {
    initXMLFactories( true );

    Assert.assertEquals( testXMLValue, System.getProperty( testSystemPropertyName ) );
  }

  /**
   * Use properties file when no settings in pentaho.xml
   */
  @Test
  public void initXMLFactoriesPropertiesTest() throws Exception {
    initXMLFactories( false );

    Assert.assertEquals( testPropertyValue, System.getProperty( testSystemPropertyName ) );
  }

  private void initXMLFactories( boolean factoriesInPentahoXML ) throws Exception {
    // mock pentaho.xml
    final ISystemSettings settingsService = mock( ISystemSettings.class );
    if ( factoriesInPentahoXML ) {
      final Node testNode = mock( Node.class );
      final Node testNodeName = mock( Node.class );
      when( testNodeName.getText() ).thenReturn( testSystemPropertyName );
      when( testNode.selectSingleNode( eq( "@name" ) ) ).thenReturn( testNodeName );
      final Node testNodeImplementation = mock( Node.class );
      when( testNodeImplementation.getText() ).thenReturn( testXMLValue );
      when( testNode.selectSingleNode( eq( "@implementation" ) ) ).thenReturn( testNodeImplementation );
      when( settingsService.getSystemSettings( eq( "xml-factories/factory-impl" ) ) ).thenReturn( new LinkedList() { {
          add( testNode );
        } } );
    } else {
      when( settingsService.getSystemSettings( eq( "xml-factories/factory-impl" ) ) ).thenReturn( new LinkedList() );
    }
    when( settingsService.getSystemSetting( anyString(), anyString() ) ).thenReturn( "" );

    PentahoSystem.setSystemSettingsService( settingsService );

    // mock java-system-properties.properties
    final IPentahoObjectFactory objectFactory = mock( IPentahoObjectFactory.class );
    final ISystemConfig systemConfig = mock( ISystemConfig.class );
    final IConfiguration configuration = mock( IConfiguration.class );
    when( configuration.getProperties() ).thenReturn( new Properties() { {
        setProperty( testSystemPropertyName, testPropertyValue );
      } } );
    when( systemConfig.getConfiguration( eq( PentahoSystem.JAVA_SYSTEM_PROPERTIES ) ) ).thenReturn( configuration );

    when( objectFactory.objectDefined( eq( ISystemConfig.class ) ) ).thenReturn( true );
    final IPentahoObjectReference pentahoObjectReference = mock( IPentahoObjectReference.class );
    when( pentahoObjectReference.getObject() ).thenReturn( systemConfig );
    when( objectFactory.getObjectReferences( eq( ISystemConfig.class ), any( IPentahoSession.class ),
        any( Map.class ) ) ).thenReturn( new LinkedList() { {
            add( pentahoObjectReference );
          } } );

    PentahoSystem.registerObjectFactory( objectFactory );

    PentahoSystem.init();
  }
}
