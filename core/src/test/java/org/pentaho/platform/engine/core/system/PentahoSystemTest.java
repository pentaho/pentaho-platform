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


package org.pentaho.platform.engine.core.system;

import org.dom4j.Node;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.pentaho.platform.api.engine.IConfiguration;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoObjectReference;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISessionStartupAction;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.engine.ISystemConfig;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.util.logging.Logger;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PentahoSystemTest {
  private final String testSystemPropertyName = "testSystemPropertyName";
  private final String testXMLValue = "testXMLValue";
  private final String testPropertyValue = "testPropertyValue";

  private IPentahoSession session;

  private IPentahoObjectFactory pentahoObjectFactory;

  private ByteArrayOutputStream baos;
  private PrintStream oldOut;

  @Before
  public void setUp() {
    baos = new ByteArrayOutputStream();
    oldOut = System.out;
    System.setOut( new PrintStream( baos ) );

    session = mock( IPentahoSession.class );
    PentahoSessionHolder.setSession( session );
  }

  @After
  public void tearDown() {
    System.setOut( oldOut );
    PentahoSystem.deregisterObjectFactory( pentahoObjectFactory );
    PentahoSystem.setSessionStartupActions( null );
    PentahoSystem.shutdown();
    System.clearProperty( testSystemPropertyName );
  }

  @Test
  public void testSessionStartup() throws ObjectFactoryException {
    int oldLogLevel = Logger.getLogLevel();
    Logger.setLogLevel( ILogger.TRACE );

    final ISolutionEngine engine = mock( ISolutionEngine.class );
    pentahoObjectFactory = mock( IPentahoObjectFactory.class );
    when( pentahoObjectFactory.objectDefined( anyString() ) ).thenReturn( true );
    when( pentahoObjectFactory.get( this.anyClass(), anyString(), any( IPentahoSession.class ) ) ).thenAnswer( invocation -> engine );
    PentahoSystem.registerObjectFactory( pentahoObjectFactory );

    ISessionStartupAction action = mock( ISessionStartupAction.class );
    when( action.getActionOutputScope() ).thenReturn( PentahoSystem.SCOPE_SESSION );
    when( action.getSessionType() ).thenReturn( session.getClass().getName() );

    when( session.isAuthenticated() ).thenReturn( true );

    PentahoSystem.setSessionStartupActions( Collections.singletonList( action ) );
    PentahoSystem.sessionStartup( session, null );

    System.out.flush();

    assertNotNull( baos );
    assertTrue( baos.toString().contains( "Process session startup actions" ) );

    Logger.setLogLevel( oldLogLevel );
  }
  /**
   * When there are settings in pentaho.xml, we should use it overwriting properties file
   */
  @Test
  public void testInitXMLFactoriesXMLTest() throws Exception {
    initXMLFactories( true );
    assertEquals( testXMLValue, System.getProperty( testSystemPropertyName ) );
  }

  /**
   * Use properties file when no settings in pentaho.xml
   */
  @Test
  public void testInitXMLFactoriesPropertiesTest() throws Exception {
    initXMLFactories( false );
    assertEquals( testPropertyValue, System.getProperty( testSystemPropertyName ) );
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
      when( settingsService.getSystemSettings( "xml-factories/factory-impl" ) ).thenReturn( new LinkedList<Node>() {
        {
          add( testNode );
        } } );
    } else {
      when( settingsService.getSystemSettings(  "xml-factories/factory-impl" ) ).thenReturn( new LinkedList<Node>() );
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
        nullable( Map.class ) ) ).thenReturn( new LinkedList() { {
            add( pentahoObjectReference );
          } } );

    PentahoSystem.registerObjectFactory( objectFactory );

    PentahoSystem.init();
  }

  private Class<?> anyClass() {
    return argThat( new AnyClassMatcher() );
  }

  private static class AnyClassMatcher implements ArgumentMatcher<Class<?>> {
    @Override
    public boolean matches( Class<?> arg ) {
      // We return true, because we want to acknowledge all class types
      return true;
    }
  }
}
