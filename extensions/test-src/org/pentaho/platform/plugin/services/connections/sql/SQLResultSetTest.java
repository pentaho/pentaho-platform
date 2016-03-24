/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
package org.pentaho.platform.plugin.services.connections.sql;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.commons.connection.IPentahoResultSet;

public class SQLResultSetTest {

  @Test
  public void testMemoryCopy() throws SQLException {
    final Object[][] columns = new Object[][]{ { "column1", "column2" } };

    ResultSetMetaData metaData = mock( ResultSetMetaData.class );
    when( metaData.getColumnCount() ).thenReturn( columns[0].length );
    when( metaData.getColumnLabel( anyInt() ) ).thenAnswer( new Answer<String>() {

      //offset for compatibility with #ResultSetMetaData
      private int offset = 1;

      @Override
      public String answer( InvocationOnMock invocation ) throws Throwable {
        return (String) columns[0][ (Integer) invocation.getArguments()[0] - offset];
      }
    } );

    SQLConnection nativeConnection = mock( SQLConnection.class );
    ResultSet nativeResultSet = mock( ResultSet.class );
    when( nativeResultSet.getMetaData() ).thenReturn( metaData );

    SQLResultSet resultSet = new SQLResultSet( nativeResultSet, nativeConnection );
    IPentahoResultSet  memoryCopyResultSet = resultSet.memoryCopy();

    assertEquals( columns[0].length, memoryCopyResultSet.getColumnCount() );
    assertTrue( Arrays.equals( columns[0], memoryCopyResultSet.getMetaData().getColumnHeaders()[0] ) );

    verify( nativeResultSet, times( 1 ) ).close();
  }

}
