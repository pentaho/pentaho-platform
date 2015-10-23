package org.pentaho.platform.config;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.platform.api.engine.IConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by rfellows on 10/22/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class SystemConfigTest {

  SystemConfig systemConfig;
  private List<IConfiguration> configs;
  private Properties props1;
  private Properties props2;

  @Mock IConfiguration config1;
  @Mock IConfiguration config2;
  @Mock IConfiguration config3;

  @Before
  public void setUp() throws Exception {
    configs = new ArrayList<>();

    props1 = new Properties();
    props1.put( "one", "un" );

    props2 = new Properties();
    props2.put( "two", "deux" );

    when( config1.getId() ).thenReturn( "1" );
    when( config1.getProperties() ).thenReturn( props1 );
    when( config2.getId() ).thenReturn( "2" );
    when( config2.getProperties() ).thenReturn( props2 );

    configs.add( config1 );
    configs.add( config2 );
  }

  @Test
  public void testConstructor() throws Exception {
    systemConfig = new SystemConfig( configs );
    assertNotNull( systemConfig.getConfiguration( "1" ) );
    assertNotNull( systemConfig.getConfiguration( "2" ) );
  }

  @Test
  public void testGetProperty() throws Exception {
    systemConfig = new SystemConfig( configs );
    String property = systemConfig.getProperty( "1.one" );
    assertEquals( "un", property );
  }

  @Test
  public void testGetProperty_unknown() throws Exception {
    systemConfig = new SystemConfig( configs );
    String property = systemConfig.getProperty( "1000.X" );
    assertNull( property );
  }

  @Test
  public void testRegisterConfiguration() throws Exception {
    systemConfig = new SystemConfig();
    systemConfig.registerConfiguration( config1 );
    assertEquals( 1, systemConfig.listConfigurations().length );

    // do it again, make sure it didn't add another, but it undated the existing one
    when( config3.getId() ).thenReturn( "1" );
    when( config3.getProperties() ).thenReturn( props2 );

    systemConfig.registerConfiguration( config3 );
    assertEquals( 1, systemConfig.listConfigurations().length );
    verify( config1 ).update( props2 );
  }

  @Test
  public void testListConfigurations() throws Exception {

  }
}
