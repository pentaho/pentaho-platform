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


package org.pentaho.platform.web.http.api.resources;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IConfiguration;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.ISystemConfig;
import org.pentaho.platform.config.SystemConfig;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.objfac.StandaloneSpringPentahoObjectFactory;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.pentaho.test.platform.utils.TestResourceLocation;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResource;

import jakarta.ws.rs.core.Response;
import java.io.File;
import java.util.List;
import java.util.Properties;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SystemResourceIT {

  SystemResource systemResource;
  Response response;

  private static final String SOLUTION_PATH = TestResourceLocation.TEST_RESOURCES + "/solution1-no-config"; //$NON-NLS-1$

  private static final String ALT_SOLUTION_PATH = TestResourceLocation.TEST_RESOURCES + "/solution1-no-config"; //$NON-NLS-1$

  private static final String PENTAHO_XML_PATH = "/system/pentahoObjects.spring.xml"; //$NON-NLS-1$

  final String SYSTEM_FOLDER = "/system"; //$NON-NLS-1$
  private MicroPlatform mp;

  @Before
  public void setUp() throws Exception {
    mp = new MicroPlatform();
    mp.defineInstance( IAuthorizationPolicy.class, new TestAuthorizationPolicy() );
    mp.start();

    ISystemConfig systemConfig = new SystemConfig();
    IConfiguration securityConfig = mock( IConfiguration.class );
    Properties props = new Properties();
    props.setProperty( "provider", "jackrabbit" );
    when( securityConfig.getProperties() ).thenReturn( props );
    when( securityConfig.getId() ).thenReturn( "security" );
    systemConfig.registerConfiguration( securityConfig );

    systemResource = new SystemResource( systemConfig );

    StandaloneApplicationContext applicationContext = new StandaloneApplicationContext( getSolutionPath(), "" ); //$NON-NLS-1$

    ApplicationContext springApplicationContext = getSpringApplicationContext();
    IPentahoObjectFactory pentahoObjectFactory = new StandaloneSpringPentahoObjectFactory();
    pentahoObjectFactory.init( null, springApplicationContext );
    PentahoSystem.registerObjectFactory( pentahoObjectFactory );

    // force Spring to populate PentahoSystem
    boolean initOk = PentahoSystem.init( applicationContext );

    /*
     * StandaloneSession session = new StandaloneSession();
     * 
     * StandaloneSpringPentahoObjectFactory factory = new StandaloneSpringPentahoObjectFactory( );
     * 
     * File f = new File(TestResourceLocation.TEST_RESOURCES + "/solution/system/pentahoObjects.spring.xml"); FileSystemResource fsr = new
     * FileSystemResource(f); GenericApplicationContext appCtx = new GenericApplicationContext();
     * XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(appCtx); xmlReader.loadBeanDefinitions(fsr);
     * 
     * factory.init(TestResourceLocation.TEST_RESOURCES + "/solution/system/pentahoObjects.spring.xml", appCtx );
     */
  }

  /**
   * Test that we get a valid document structure back. Make sure the document contains the elements we expect. Since we
   * are not working with a real session, we don't need to check real values.
   *
   * @throws Exception
   */
  @Test
  public void testGetAll() throws Exception {
    response = systemResource.getAll();

    String responseString = (String) response.getEntity();

    Assert.assertTrue( responseString.toLowerCase().contains( "roles" ) );
    Assert.assertTrue( responseString.toLowerCase().contains( "users" ) );
    Assert.assertTrue( responseString.toLowerCase().contains( "acls" ) );
  }

  /**
   * Test for expected default value JCR_BASED_AUTHENTICATION
   *
   * @throws Exception
   */
  @Test
  public void testGetAuthenticationProvider() throws Exception {
    Response response = systemResource.getAuthenticationProvider();

    // default configuration should be JCR
    AuthenticationProvider expectedResult = new AuthenticationProvider( "jackrabbit" );
    AuthenticationProvider actualResult = (AuthenticationProvider) response.getEntity();
    Assert.assertTrue( actualResult.toString().equals( expectedResult.toString() ) );
  }

  /**
   * Make spring configs available for test
   *
   * @return ApplicationContext
   */
  private ApplicationContext getSpringApplicationContext() {

    String[] fns =
    {
      "pentahoObjects.spring.xml", "adminPlugins.xml", "sessionStartupActions.xml",
      "systemListeners.xml", "pentahoSystemConfig.xml", "repository.spring.xml"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

    GenericApplicationContext appCtx = new GenericApplicationContext();
    XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader( appCtx );

    for ( String fn : fns ) {
      File f = new File( getSolutionPath() + SYSTEM_FOLDER + "/" + fn ); //$NON-NLS-1$
      if ( f.exists() ) {
        FileSystemResource fsr = new FileSystemResource( f );
        xmlReader.loadBeanDefinitions( fsr );
      }
    }

    return appCtx;
  }

  /**
   * @return String
   */
  private String getSolutionPath() {
    File file = new File( SOLUTION_PATH + PENTAHO_XML_PATH );
    if ( file.exists() ) {
      return SOLUTION_PATH;
    } else {
      return ALT_SOLUTION_PATH;
    }
  }

  class TestAuthorizationPolicy implements IAuthorizationPolicy {

    @Override
    public boolean isAllowed( String actionName ) {
      // TODO Auto-generated method stub
      return true;
    }

    @Override
    public List<String> getAllowedActions( String actionNamespace ) {
      // TODO Auto-generated method stub
      return null;
    }

  }
}
