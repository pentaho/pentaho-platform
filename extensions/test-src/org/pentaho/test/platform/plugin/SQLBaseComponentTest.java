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

package org.pentaho.test.platform.plugin;

import com.mockrunner.mock.jdbc.MockConnection;
import com.mockrunner.mock.jdbc.MockPreparedStatement;
import com.mockrunner.mock.jdbc.MockResultSet;
import com.mockrunner.mock.jdbc.MockStatement;
import org.apache.commons.logging.Log;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.actionsequence.dom.actions.SqlQueryAction;
import org.pentaho.platform.plugin.action.sql.SQLBaseComponent;
import org.pentaho.platform.plugin.services.connections.sql.SQLConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@SuppressWarnings( "nls" )
public class SQLBaseComponentTest {

  public static final MockResultSet set = new MockResultSet( "id" );

  @BeforeClass
  public static void createMockResultSet() {
    set.addColumn( new String[] { "column1", "column2", "column3" } );
    set.addRow( new String[] { "row1", "row12", "row13" } );
    set.addRow( new String[] { "row2", "row22", "row23" } );
  }

  // IMPORTANT!!! This test is solely to exercise the SQLConnection's
  // "fallBackToNonscrollableOnError" flag. We have set up a mock connection that
  // does not support scrollable cursors. The connection should throw a SQLException, then
  // we set the cursor to forward-only and attempt the query again.

  @Test
  public void testRunQuery() {

    java.sql.Connection connection = new MockNativeConnection();
    SQLConnection sqlConnection = new MockSQLConnection( connection );
    MockSQLBaseComponent component = new MockSQLBaseComponent( sqlConnection );

    Assert.assertTrue( component.runQuery() );
  }

  class MockNativeConnection extends MockConnection {

    @Override
    public Statement createStatement( int scrollType, int concur ) throws SQLException {
      if ( scrollType == ResultSet.TYPE_SCROLL_INSENSITIVE ) {
        throw new SQLException();
      }
      return new MockSQLBaseStatement( this, scrollType, concur );
    }

    @Override
    public PreparedStatement prepareStatement( String query, int scrollType, int concur ) throws SQLException {
      if ( scrollType == ResultSet.TYPE_SCROLL_INSENSITIVE ) {
        throw new SQLException();
      }
      return new MockSQLBasePreparedStatement( this, query, scrollType, concur );
    }

  }

  class MockSQLConnection extends SQLConnection {
    public MockSQLConnection( Connection connection ) {
      this.nativeConnection = connection;
    }
  }

  class MockSQLBasePreparedStatement extends MockPreparedStatement {

    public MockSQLBasePreparedStatement( Connection conn, String query, int scrollType, int concur ) {
      super( conn, query, scrollType, concur );
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
      return set;
    }

  }

  class MockSQLBaseStatement extends MockStatement {

    public MockSQLBaseStatement( Connection conn, int scrollType, int concur ) {
      super( conn, scrollType, concur );
    }

    @Override
    public ResultSet executeQuery( String query ) throws SQLException {
      return set;
    }

  }

  class MockSQLBaseComponent extends SQLBaseComponent {

    private static final long serialVersionUID = 1L;

    public MockSQLBaseComponent( SQLConnection conn ) {

      connection = conn;
      preparedQuery = "Select *";
      SqlQueryAction action = new SqlQueryAction();
      setActionDefinition( action );

    }

    public boolean runQuery() {
      return this.runQuery( "select *", true );
    }

    @Override
    public Log getLogger() {
      return null;
    }

    @Override
    public String getResultOutputName() {
      return null;
    }

    @Override
    public boolean validateSystemSettings() {
      return true;
    }

    @Override
    protected String applyInputsToFormat( String format ) {
      return format;
    }

  }

}
