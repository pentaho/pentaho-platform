package org.pentaho.platform.config;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.junit.Test;

import java.io.File;
import java.util.Collection;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Created by rfellows on 10/22/15.
 */
public class PropertiesFileConfigurationTest {

  @Test
  public void testConstructor_StringProperties() throws Exception {
    Properties props = new Properties();
    props.put( "one", "1" );

    PropertiesFileConfiguration config = new PropertiesFileConfiguration( "id", props );
    assertEquals( "id", config.getId() );
    assertEquals( props, config.getProperties() );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructor_nullId() throws Exception {
    Properties props = new Properties();
    props.put( "one", "1" );

    PropertiesFileConfiguration config = new PropertiesFileConfiguration( null, props );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructor_nullProperties() throws Exception {
    Properties props = null;
    PropertiesFileConfiguration config = new PropertiesFileConfiguration( "id", props );
  }

  @Test
  public void testConstructor_StringFile() throws Exception {
    File file = mock( File.class );
    when( file.exists() ).thenReturn( true );
    PropertiesFileConfiguration config = new PropertiesFileConfiguration( "id", file );
    assertEquals( "id", config.getId() );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructor_nullId_file() throws Exception {
    File file = mock( File.class );
    PropertiesFileConfiguration config = new PropertiesFileConfiguration( null, file );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructor_nullFile() throws Exception {
    File file = null;
    PropertiesFileConfiguration config = new PropertiesFileConfiguration( "id", file );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructor_FileDoesNotExist() throws Exception {
    File file = mock( File.class );
    when( file.exists() ).thenReturn( false );
    PropertiesFileConfiguration config = new PropertiesFileConfiguration( "id", file );
  }

  @Test
  public void testGetProperties_callsLoadWhenNeeded() throws Exception {
    File file = mock( File.class );
    when( file.exists() ).thenReturn( true );
    PropertiesFileConfiguration config = spy( new PropertiesFileConfiguration( "id", file ) );

    try {
      config.getProperties();
    } catch ( NullPointerException e ) {
      // the underlying file is mocked, so we will get an exception when it tries to use it
      // but it doesn't matter since we are just making sure load is called if properties are empty
      verify( config ).loadProperties();
    }
  }

  @Test
  public void testUpdate() throws Exception {
    Properties props = new Properties();
    props.put( "one", "1" );

    PropertiesFileConfiguration config = new PropertiesFileConfiguration( "id", props );
    final File tmpPropsFile = File.createTempFile( "PropertiesFileConfiguratorTest", ".properties" );

    config.setPropFile( tmpPropsFile );

    Properties newProps = new Properties();
    props.put( "two", "2" );

    config.update( newProps );

    Collection<File> files = FileUtils.listFiles( tmpPropsFile.getParentFile(), new IOFileFilter() {
      @Override public boolean accept( File file ) {
        return file.getName().startsWith( tmpPropsFile.getName() );
      }

      @Override public boolean accept( File file, String s ) {
        return file.getName().startsWith( tmpPropsFile.getName() );
      }
    }, null );

    assertEquals( 2, files.size() );
    for ( File file : files ) {
      // clean up the files manually. the delete on exit will only get the one, not the timestamped one created
      // in the update method
      file.deleteOnExit();
    }

  }
}
