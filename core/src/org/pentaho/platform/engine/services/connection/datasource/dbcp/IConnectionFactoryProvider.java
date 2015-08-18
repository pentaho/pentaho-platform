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
 * Copyright 2006 - 2015 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.services.connection.datasource.dbcp;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.pentaho.database.IDatabaseDialect;
import org.pentaho.database.model.IDatabaseConnection;

/**
 * This class conceals the logic of creating instances of {@linkplain DriverManagerConnectionFactory} for different
 * dialects.
 *
 * @author Andrey Khayrutdinov
 */
public interface IConnectionFactoryProvider {

  /**
   * Creates a {@linkplain ConnectionFactory} instance for specified <tt>connection</tt>, <tt>dialect</tt> and
   * <tt>url</tt>.
   *
   * @param connection database connection
   * @param dialect    database dialect
   * @param url        url for connecting
   * @return DBCP's {@linkplain ConnectionFactory}
   */
  ConnectionFactory create( IDatabaseConnection connection, IDatabaseDialect dialect, String url );
}
