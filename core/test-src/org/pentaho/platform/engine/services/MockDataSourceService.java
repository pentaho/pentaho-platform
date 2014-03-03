/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.services;

import org.pentaho.platform.api.data.DBDatasourceServiceException;
import org.pentaho.platform.engine.services.connection.datasource.dbcp.BaseDatasourceService;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

/**
 * Produces {@link MockDataSource}s
 */
public class MockDataSourceService extends BaseDatasourceService {

  private static boolean throwExceptionOnGetConnection = false;

  /**
   * @param throwException
   *          If {@code true} the {@link MockDataSource} will throw a SQL exception when
   *          {@code MockDataSource#getConnection()} is called
   */
  public MockDataSourceService( boolean throwException ) {
    this.throwExceptionOnGetConnection = throwException;
  }

  /**
   * @param exceptionOnGetConnection
   *          If {@code true} the {@link MockDataSource} will throw a SQL exception when
   *          {@code MockDataSource#getConnection()} is called
   */
  public static void setThrowExceptionOnGetConnection ( boolean exceptionOnGetConnection ) {
    throwExceptionOnGetConnection = exceptionOnGetConnection;
  }

  /**
   * @param dsName
   *          Not used.
   * @return A new {@link MockDataSource}.
   */
  @Override
  public DataSource getDataSource( String dsName ) throws DBDatasourceServiceException {
    return new MockDataSource();
  }

  /**
   * A mock data source that throws a {@link SQLException} from {@link #getConnection()} and
   * {@link #getConnection(String, String)} if {@link MockDataSourceService#throwExceptionOnGetConnection} ==
   * {@code true}.
   */
  private class MockDataSource implements DataSource {

    @Override
    public Connection getConnection() throws SQLException {
      if ( MockDataSourceService.this.throwExceptionOnGetConnection ) {
        throw new SQLException( "mock not producing connections" ); //$NON-NLS-1$
      }
      return new MockConnection();
    }

    @Override
    public Connection getConnection( String username, String password ) throws SQLException {
      if ( MockDataSourceService.this.throwExceptionOnGetConnection ) {
        throw new SQLException( "mock not producing connections" ); //$NON-NLS-1$
      }
      return new MockConnection();
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
      return null;
    }

    @Override
    public void setLogWriter( PrintWriter out ) throws SQLException {
    }

    @Override
    public void setLoginTimeout( int seconds ) throws SQLException {
    }

    @Override
    public int getLoginTimeout() throws SQLException {
      return 0;
    }

    @Override
    public <T> T unwrap( Class<T> iface ) throws SQLException {
      return null;
    }

    @Override
    public boolean isWrapperFor( Class<?> iface ) throws SQLException {
      return false;
    }

    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
      return null;
    }
  }

  private class MockConnection implements Connection {

    @Override
    public <T> T unwrap( Class<T> iface ) throws SQLException {

      return null;
    }

    @Override
    public boolean isWrapperFor( Class<?> iface ) throws SQLException {

      return false;
    }

    @Override
    public Statement createStatement() throws SQLException {

      return null;
    }

    @Override
    public PreparedStatement prepareStatement( String sql ) throws SQLException {

      return null;
    }

    @Override
    public CallableStatement prepareCall( String sql ) throws SQLException {

      return null;
    }

    @Override
    public String nativeSQL( String sql ) throws SQLException {

      return null;
    }

    @Override
    public void setAutoCommit( boolean autoCommit ) throws SQLException {

    }

    @Override
    public boolean getAutoCommit() throws SQLException {

      return false;
    }

    @Override
    public void commit() throws SQLException {

    }

    @Override
    public void rollback() throws SQLException {

    }

    @Override
    public void close() throws SQLException {

    }

    @Override
    public boolean isClosed() throws SQLException {

      return false;
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {

      return null;
    }

    @Override
    public void setReadOnly( boolean readOnly ) throws SQLException {

    }

    @Override
    public boolean isReadOnly() throws SQLException {

      return false;
    }

    @Override
    public void setCatalog( String catalog ) throws SQLException {

    }

    @Override
    public String getCatalog() throws SQLException {

      return null;
    }

    @Override
    public void setTransactionIsolation( int level ) throws SQLException {

    }

    @Override
    public int getTransactionIsolation() throws SQLException {

      return 0;
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {

      return null;
    }

    @Override
    public void clearWarnings() throws SQLException {

    }

    @Override
    public Statement createStatement( int resultSetType, int resultSetConcurrency ) throws SQLException {

      return null;
    }

    @Override
    public PreparedStatement prepareStatement( String sql, int resultSetType, int resultSetConcurrency )
      throws SQLException {

      return null;
    }

    @Override
    public CallableStatement prepareCall( String sql, int resultSetType, int resultSetConcurrency )
      throws SQLException {

      return null;
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {

      return null;
    }

    @Override
    public void setTypeMap( Map<String, Class<?>> map ) throws SQLException {

    }

    @Override
    public void setHoldability( int holdability ) throws SQLException {

    }

    @Override
    public int getHoldability() throws SQLException {

      return 0;
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {

      return null;
    }

    @Override
    public Savepoint setSavepoint( String name ) throws SQLException {

      return null;
    }

    @Override
    public void rollback( Savepoint savepoint ) throws SQLException {

    }

    @Override
    public void releaseSavepoint( Savepoint savepoint ) throws SQLException {

    }

    @Override
    public Statement createStatement( int resultSetType, int resultSetConcurrency, int resultSetHoldability )
      throws SQLException {

      return null;
    }

    @Override
    public PreparedStatement prepareStatement( String sql, int resultSetType, int resultSetConcurrency,
        int resultSetHoldability ) throws SQLException {

      return null;
    }

    @Override
    public CallableStatement prepareCall( String sql, int resultSetType, int resultSetConcurrency,
        int resultSetHoldability ) throws SQLException {

      return null;
    }

    @Override
    public PreparedStatement prepareStatement( String sql, int autoGeneratedKeys ) throws SQLException {

      return null;
    }

    @Override
    public PreparedStatement prepareStatement( String sql, int[] columnIndexes ) throws SQLException {

      return null;
    }

    @Override
    public PreparedStatement prepareStatement( String sql, String[] columnNames ) throws SQLException {

      return null;
    }

    @Override
    public Clob createClob() throws SQLException {

      return null;
    }

    @Override
    public Blob createBlob() throws SQLException {

      return null;
    }

    @Override
    public NClob createNClob() throws SQLException {

      return null;
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {

      return null;
    }

    @Override
    public boolean isValid( int timeout ) throws SQLException {

      return false;
    }

    @Override
    public void setClientInfo( String name, String value ) throws SQLClientInfoException {

    }

    @Override
    public void setClientInfo( Properties properties ) throws SQLClientInfoException {

    }

    @Override
    public String getClientInfo( String name ) throws SQLException {

      return null;
    }

    @Override
    public Properties getClientInfo() throws SQLException {

      return null;
    }

    @Override
    public Array createArrayOf( String typeName, Object[] elements ) throws SQLException {

      return null;
    }

    @Override
    public Struct createStruct( String typeName, Object[] attributes ) throws SQLException {

      return null;
    }

    public void setSchema( String schema ) throws SQLException {
    }

    public String getSchema() throws SQLException {
      return null;
    }

    public void abort( Executor executor ) throws SQLException {
    }

    public void setNetworkTimeout( Executor executor, int milliseconds ) throws SQLException {
    }

    public int getNetworkTimeout() throws SQLException {
      return 0;
    }
  }
}
