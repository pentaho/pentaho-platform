/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.platform.config;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.junit.Assume;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
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

  @Test
  public void testReload_fileBackedConfigurationReloadsUpdatedProperties() throws Exception {
    File file = File.createTempFile( "PropertiesFileConfigurationTest", ".properties" );
    file.deleteOnExit();
    storeProperties( file, propertiesOf( "one", "1" ) );

    PropertiesFileConfiguration config = new PropertiesFileConfiguration( "id", file );

    assertEquals( "1", config.getProperties().getProperty( "one" ) );

    storeProperties( file, propertiesOf( "one", "2", "two", "3" ) );

    config.reload();

    Properties reloadedProperties = config.getProperties();
    assertEquals( "2", reloadedProperties.getProperty( "one" ) );
    assertEquals( "3", reloadedProperties.getProperty( "two" ) );
    assertEquals( 2, reloadedProperties.size() );
  }

  @Test
  public void testReload_inMemoryConfigurationDoesNothing() throws Exception {
    Properties properties = propertiesOf( "one", "1" );
    PropertiesFileConfiguration config = new PropertiesFileConfiguration( "id", properties );

    config.reload();

    Properties reloadedProperties = config.getProperties();
    assertEquals( "1", reloadedProperties.getProperty( "one" ) );
    assertEquals( 1, reloadedProperties.size() );
  }

  @Test
  public void testReload_fileBackedConfigurationPropagatesIOException() throws Exception {
    File file = File.createTempFile( "PropertiesFileConfigurationTest", ".properties" );
    storeProperties( file, propertiesOf( "one", "1" ) );

    PropertiesFileConfiguration config = new PropertiesFileConfiguration( "id", file );

    Assume.assumeTrue( file.delete() );

    try {
      config.reload();
      fail( "Expected reload to propagate IOException when the file no longer exists" );
    } catch ( IOException expected ) {
      assertTrue( expected.getMessage().contains( file.getName() ) );
    }
  }

  private Properties propertiesOf( String... keyValuePairs ) {
    Properties properties = new Properties();
    for ( int index = 0; index < keyValuePairs.length; index += 2 ) {
      properties.setProperty( keyValuePairs[ index ], keyValuePairs[ index + 1 ] );
    }
    return properties;
  }

  private void storeProperties( File file, Properties properties ) throws IOException {
    try ( FileOutputStream outputStream = new FileOutputStream( file ) ) {
      properties.store( outputStream, "" );
    }
  }
}
