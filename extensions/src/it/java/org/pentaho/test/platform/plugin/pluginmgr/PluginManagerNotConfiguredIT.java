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


package org.pentaho.test.platform.plugin.pluginmgr;

import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoPublisher;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.engine.core.system.PathBasedSystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.objfac.StandaloneSpringPentahoObjectFactory;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.plugin.services.pluginmgr.PluginAdapter;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.test.platform.engine.core.TestManager;
import org.pentaho.test.platform.utils.TestResourceLocation;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PluginManagerNotConfiguredIT extends BaseTest {
  private static final String SOLUTION_PATH = TestResourceLocation.TEST_RESOURCES + "/solution1-no-config"; //$NON-NLS-1$

  private static final String ALT_SOLUTION_PATH = TestResourceLocation.TEST_RESOURCES + "/solution1-no-config"; //$NON-NLS-1$

  private static final String PENTAHO_XML_PATH = "/system/pentahoObjects.spring.xml"; //$NON-NLS-1$

  final String SYSTEM_FOLDER = "/system"; //$NON-NLS-1$

  @Override
  public String getSolutionPath() {
    File file = new File( SOLUTION_PATH + PENTAHO_XML_PATH );
    if ( file.exists() ) {
      return SOLUTION_PATH;
    } else {
      return ALT_SOLUTION_PATH;
    }
  }

  private boolean initOk;

  public void setUp() {

    List<?> messages = TestManager.getMessagesList();
    if ( messages == null ) {
      messages = new ArrayList<String>();
    }

    if ( initOk ) {
      return;
    }

    PentahoSystem.setSystemSettingsService( new PathBasedSystemSettings() );
    if ( PentahoSystem.getApplicationContext() == null ) {
      StandaloneApplicationContext applicationContext = new StandaloneApplicationContext( getSolutionPath(), "" ); //$NON-NLS-1$
      String inContainer = System.getProperty( "incontainer", "false" ); //$NON-NLS-1$ //$NON-NLS-2$
      if ( inContainer.equalsIgnoreCase( "false" ) ) { //$NON-NLS-1$
        // Setup simple-jndi for datasources
        System.setProperty( "java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory" ); //$NON-NLS-1$ //$NON-NLS-2$
        System.setProperty( "org.osjava.sj.root", getSolutionPath() + "/system/simple-jndi" ); //$NON-NLS-1$ //$NON-NLS-2$
        System.setProperty( "org.osjava.sj.delimiter", "/" ); //$NON-NLS-1$ //$NON-NLS-2$
      }
      ApplicationContext springApplicationContext = getSpringApplicationContext();

      IPentahoObjectFactory pentahoObjectFactory = new StandaloneSpringPentahoObjectFactory();
      pentahoObjectFactory.init( null, springApplicationContext );
      PentahoSystem.registerObjectFactory( pentahoObjectFactory );

      // force Spring to populate PentahoSystem
      springApplicationContext.getBean( "pentahoSystemProxy" ); //$NON-NLS-1$
      initOk = PentahoSystem.init( applicationContext );
    } else {
      initOk = true;
    }
    initOk = true;
  }

  @Override
  protected ApplicationContext getSpringApplicationContext() {

    String[] fns =
    {
      "pentahoObjects.spring.xml", "adminPlugins.xml", "sessionStartupActions.xml",
      "systemListeners.xml", "pentahoSystemConfig.xml"
    }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

    GenericApplicationContext appCtx = new GenericApplicationContext();
    XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader( appCtx );

    for ( String fn : fns ) {
      File f = new File( getSolutionPath() + SYSTEM_FOLDER + "/" + fn ); //$NON-NLS-1$
      if ( f.exists() ) {
        FileSystemResource fsr = new FileSystemResource( f );
        xmlReader.loadBeanDefinitions( fsr );
      }
    }

    appCtx.refresh();
    return appCtx;
  }

  public void testBadConfig1() {
    startTest();

    IPentahoSession session = new StandaloneSession( "test user" ); //$NON-NLS-1$
    IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class, session );
    assertNull( pluginManager );

    finishTest();
  }

  public void testPluginAdapterViaPublish() throws Exception {
    startTest();

    IPentahoSession session = new StandaloneSession( "test user" ); //$NON-NLS-1$

    String str = PentahoSystem.publish( session, "org.pentaho.platform.plugin.services.pluginmgr.PluginAdapter" ); //$NON-NLS-1$
    assertEquals( str, Messages.getInstance().getString( "PluginAdapter.ERROR_0001_PLUGIN_MANAGER_NOT_CONFIGURED" ) ); //$NON-NLS-1$
    finishTest();
  }

  @SuppressWarnings( "cast" )
  public void testPluginAdapterViaPublisherAPI() throws Exception {
    startTest();

    IPentahoSession session = new StandaloneSession( "test user" ); //$NON-NLS-1$

    PluginAdapter mgr = new PluginAdapter();
    assertTrue( mgr instanceof IPentahoPublisher );
    IPentahoPublisher publisher = (IPentahoPublisher) mgr;

    assertEquals( Messages.getInstance().getString( "PluginAdapter.USER_PLUGIN_MANAGER" ), publisher.getName() ); //$NON-NLS-1$
    assertNotSame( "!PluginAdapter.USER_PLUGIN_MANAGER!", publisher.getName() ); //$NON-NLS-1$

    assertEquals( Messages.getInstance().getString( "PluginAdapter.USER_REFRESH_PLUGINS" ), publisher.getDescription() ); //$NON-NLS-1$
    assertNotSame( "!PluginAdapter.USER_REFRESH_PLUGINS!", publisher.getName() ); //$NON-NLS-1$

    String str = publisher.publish( session, ILogger.DEBUG );
    assertEquals( str, Messages.getInstance().getString( "PluginAdapter.ERROR_0001_PLUGIN_MANAGER_NOT_CONFIGURED" ) ); //$NON-NLS-1$
    finishTest();
  }

  @SuppressWarnings( "cast" )
  public void testPluginAdapterViaSystemListenerAPI() throws Exception {
    startTest();

    IPentahoSession session = new StandaloneSession( "test user" ); //$NON-NLS-1$

    PluginAdapter mgr = new PluginAdapter();
    assertTrue( mgr instanceof IPentahoSystemListener );

    IPentahoSystemListener listener = (IPentahoSystemListener) mgr;

    assertFalse( listener.startup( session ) );

    // this does not do anything but it shouldn't error
    listener.shutdown();

    finishTest();
  }

}
