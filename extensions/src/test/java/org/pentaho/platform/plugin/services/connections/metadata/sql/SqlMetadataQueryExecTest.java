/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.plugin.services.connections.metadata.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
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
    when( props.getProperty( eq( SqlMetadataQueryExec.FORCE_DB_META_CLASSES_PROP ), anyString() ) ).thenReturn(
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
    when( props.getProperty( eq( SqlMetadataQueryExec.FORCE_DB_META_CLASSES_PROP ), anyString() ) ).thenReturn(
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
