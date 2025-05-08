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


package org.pentaho.platform.plugin.services.connections.metadata.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Properties;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.platform.api.engine.IConfiguration;
import org.pentaho.platform.api.engine.ISystemConfig;

public class SqlMetadataQueryExecTest {
  @Test
  public void testDriverClassesToForceMetaNoEntries() throws IOException {
    ISystemConfig sysConfig = mock( ISystemConfig.class );
    IConfiguration config = mock( IConfiguration.class );
    when( sysConfig.getConfiguration( SqlMetadataQueryExec.CONFIG_ID ) ).thenReturn( config );
    Properties props = mock( Properties.class );
    when( config.getProperties() ).thenReturn( props );
    final ArgumentCaptor<String> defaultValue = ArgumentCaptor.forClass( String.class );
    when( props.getProperty( eq( SqlMetadataQueryExec.FORCE_DB_META_CLASSES_PROP ), defaultValue.capture() ) )
        .thenAnswer( new Answer<String>() {

          @Override
          public String answer( InvocationOnMock invocation ) throws Throwable {
            return defaultValue.getValue();
          }
        } );
    SqlMetadataQueryExec sqlMetadataQueryExec = new SqlMetadataQueryExec( sysConfig );
    assertEquals( 0, sqlMetadataQueryExec.driverClassesToForceMeta.size() );
  }

  @Test
  public void testDriverClassesToForceMetaOneEntry() throws IOException {
    ISystemConfig sysConfig = mock( ISystemConfig.class );
    IConfiguration config = mock( IConfiguration.class );
    String className = "test";
    when( sysConfig.getConfiguration( SqlMetadataQueryExec.CONFIG_ID ) ).thenReturn( config );
    Properties props = mock( Properties.class );
    when( config.getProperties() ).thenReturn( props );
    when( props.getProperty( eq( SqlMetadataQueryExec.FORCE_DB_META_CLASSES_PROP ), nullable( String.class ) ) ).thenReturn(
        " " + className + " " );
    SqlMetadataQueryExec sqlMetadataQueryExec = new SqlMetadataQueryExec( sysConfig );
    assertEquals( 1, sqlMetadataQueryExec.driverClassesToForceMeta.size() );
    assertTrue( sqlMetadataQueryExec.driverClassesToForceMeta.contains( className ) );
  }

  @Test
  public void testDriverClassesToForceMetaTwoEntries() throws IOException {
    ISystemConfig sysConfig = mock( ISystemConfig.class );
    IConfiguration config = mock( IConfiguration.class );
    String className = "test";
    String className2 = "test2";
    when( sysConfig.getConfiguration( SqlMetadataQueryExec.CONFIG_ID ) ).thenReturn( config );
    Properties props = mock( Properties.class );
    when( config.getProperties() ).thenReturn( props );
    when( props.getProperty( eq( SqlMetadataQueryExec.FORCE_DB_META_CLASSES_PROP ), nullable( String.class ) ) ).thenReturn(
        " , " + className + " , " + className2 + " ,,, " );
    SqlMetadataQueryExec sqlMetadataQueryExec = new SqlMetadataQueryExec( sysConfig );
    assertEquals( 2, sqlMetadataQueryExec.driverClassesToForceMeta.size() );
    assertTrue( sqlMetadataQueryExec.driverClassesToForceMeta.contains( className ) );
    assertTrue( sqlMetadataQueryExec.driverClassesToForceMeta.contains( className2 ) );
  }

  @Test
  public void testDriverClassesToForceMetaNullSysConfig() throws IOException {
    SqlMetadataQueryExec sqlMetadataQueryExec = new SqlMetadataQueryExec( null );
    assertEquals( 0, sqlMetadataQueryExec.driverClassesToForceMeta.size() );
  }

  @Test
  public void testDriverClassesToForceMetaNullConfig() throws IOException {
    ISystemConfig sysConfig = mock( ISystemConfig.class );
    SqlMetadataQueryExec sqlMetadataQueryExec = new SqlMetadataQueryExec( sysConfig );
    assertEquals( 0, sqlMetadataQueryExec.driverClassesToForceMeta.size() );
  }

  @Test
  public void testDriverClassesToForceMetaNullProps() throws IOException {
    ISystemConfig sysConfig = mock( ISystemConfig.class );
    IConfiguration config = mock( IConfiguration.class );
    when( sysConfig.getConfiguration( SqlMetadataQueryExec.CONFIG_ID ) ).thenReturn( config );
    SqlMetadataQueryExec sqlMetadataQueryExec = new SqlMetadataQueryExec( sysConfig );
    assertEquals( 0, sqlMetadataQueryExec.driverClassesToForceMeta.size() );
  }

  @Test
  public void testDriverClassesToForceMetaIOE() throws IOException {
    ISystemConfig sysConfig = mock( ISystemConfig.class );
    IConfiguration config = mock( IConfiguration.class );
    when( sysConfig.getConfiguration( SqlMetadataQueryExec.CONFIG_ID ) ).thenReturn( config );
    when( config.getProperties() ).thenThrow( new IOException() );
    SqlMetadataQueryExec sqlMetadataQueryExec = new SqlMetadataQueryExec( sysConfig );
    assertEquals( 0, sqlMetadataQueryExec.driverClassesToForceMeta.size() );
  }
}
