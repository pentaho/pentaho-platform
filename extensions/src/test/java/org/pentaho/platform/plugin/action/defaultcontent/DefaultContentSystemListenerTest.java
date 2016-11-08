package org.pentaho.platform.plugin.action.defaultcontent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISystemConfig;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importer.IPlatformImporter;
import org.pentaho.test.platform.utils.TestResourceLocation;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Created by rfellows on 10/20/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class DefaultContentSystemListenerTest {
  @Mock ISystemConfig systemConfig;
  @Mock IApplicationContext appContext;
  @Mock IPlatformImporter platformImporter;
  @Mock IPentahoSession session;

  DefaultContentSystemListener defaultContentSystemListener;

  @Before
  public void setUp() throws Exception {
    PentahoSystem.registerObject( systemConfig );
    PentahoSystem.setApplicationContext( appContext );
    PentahoSystem.registerObject( platformImporter );

    defaultContentSystemListener = new DefaultContentSystemListener();
  }

  @Test
  public void testShutdown() throws Exception {
    // no code in here, solely for unit test coverage
    defaultContentSystemListener.shutdown();
  }

  @Test
  public void testStartup() throws Exception {
    when( systemConfig.getProperty( "system.enable-async-default-content-loading" ) ).thenReturn( "false" );
    when( appContext.getSolutionPath( "system/default-content" ) ).thenReturn( TestResourceLocation.TEST_RESOURCES + "/SystemConfig/system" );

    boolean startup = defaultContentSystemListener.startup( session );
    assertTrue( startup );
  }
}