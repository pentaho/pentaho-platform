package org.pentaho.platform.config;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.SystemSettings;
import org.pentaho.test.platform.utils.TestResourceLocation;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by rfellows on 10/22/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class SystemSettingsConfigurationTest {

  org.pentaho.platform.config.SystemSettingsConfiguration config;
  @Mock ISystemSettings settings;

  @Before
  public void setUp() throws Exception {
    config = new SystemSettingsConfiguration( "id", settings );
    assertNotNull( config );

    // id is hardcoded, the id passed is ignored
    assertEquals( "system", config.getId() );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructor_IllegalId() throws Exception {
    config = new SystemSettingsConfiguration( null, settings );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructor_IllegalSettings() throws Exception {
    config = new SystemSettingsConfiguration( "id", null );
  }

  @Test
  public void testGetProperties() throws Exception {
    SystemSettings settings = new SystemSettings();
    IApplicationContext appContext = mock( IApplicationContext.class );
    PentahoSystem.setApplicationContext( appContext );
    when( appContext.getSolutionPath( anyString() ) ).thenReturn( TestResourceLocation.TEST_RESOURCES + "/solution/system/pentaho.xml" );

    config = new SystemSettingsConfiguration( "system", settings );

    Properties properties = config.getProperties();
    assertNotNull( properties );
    assertEquals( "DEBUG", properties.getProperty( "log-level" ) );
  }

  @Test( expected = UnsupportedOperationException.class )
  public void testUpdate() throws Exception {
    config.update( new Properties() );
  }

}
