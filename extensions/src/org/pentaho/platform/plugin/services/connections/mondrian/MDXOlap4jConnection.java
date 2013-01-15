/*
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
 * Copyright 2005 - 2013 Pentaho Corporation.  All rights reserved.
 *
 * 
 * @author Benny Chow
 */
package org.pentaho.platform.plugin.services.connections.mondrian;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import mondrian.olap.Util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.olap4j.OlapConnection;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.plugin.services.messages.Messages;

/**
 * MDXOlap4jConnection implements IPentahoConenction to support olap4j
 * connections to any olap4j provider. Developers may subclass MDXOlap4jConnection
 * to unwrap the olap4j connection to directly manipulate the underlying connection
 * such as setting a DelegatingRole in the case of Mondrian.
 * 
 * @author Benny Chow
 * @version $Id: $
 * @created Jan 9, 2013
 * @updated $DateTime: $
 */
public class MDXOlap4jConnection implements IPentahoConnection {

	static final Log log = LogFactory.getLog(MDXOlap4jConnection.class);
	
	protected OlapConnection connection = null;

	public void close() {
		try {
			if (connection != null)
				connection.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean connect(Properties props) {
		
		String url = props.getProperty("url");
		String driver = props.getProperty("driver");
		
		try {
			if (connection != null)
				connection.close();
			
			// For Mondrian olap4j driver, we will also do role mapping
			if (driver.equals("mondrian.olap4j.MondrianOlap4jDriver")) {
				Util.PropertyList connectProperties = Util.parseConnectString(url);
				MDXConnection.mapPlatformRolesToMondrianRolesHelper(connectProperties);
				url = connectProperties.toString();
			}

			Class.forName(driver);
			java.sql.Connection sqlConnection = DriverManager.getConnection(url);
			connection = sqlConnection.unwrap(org.olap4j.OlapConnection.class);

		} catch (Exception e) {
			log.error(Messages.getInstance().getErrorString(
		            "MDXConnection.ERROR_0002_INVALID_CONNECTION", "driver=" + driver + ";url=" + url));
			return false;
		}

		return true;
	}

	public IPentahoResultSet executeQuery(String arg0) {
		throw new UnsupportedOperationException();
	}

	public IPentahoResultSet getResultSet() {
		throw new UnsupportedOperationException();
	}

	public boolean initialized() {
		return connection != null;
	}

	public boolean isClosed() {
		if (connection == null)
			throw new IllegalStateException();

		try {
			return connection.isClosed();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean isReadOnly() {
		if (connection == null)
			throw new IllegalStateException();

		try {
			return connection.isReadOnly();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public IPentahoResultSet prepareAndExecuteQuery(String arg0, List arg1) throws Exception {
		throw new UnsupportedOperationException();
	}

	public boolean preparedQueriesSupported() {
		return false;
	}

	public void setFetchSize(int arg0) {
		throw new UnsupportedOperationException();
	}

	public void setMaxRows(int arg0) {
		throw new UnsupportedOperationException();
	}

	public void setProperties(Properties props) {
		this.connect(props);
	}

	public void clearWarnings() {

	}

	public String getDatasourceType() {
		return IPentahoConnection.MDX_OLAP4J_DATASOURCE;
	}

	public String getLastQuery() {
		throw new UnsupportedOperationException();
	}

	public OlapConnection getConnection() {
	    return connection;
	}
}
