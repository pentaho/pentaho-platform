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

package org.pentaho.platform.api.data;

import javax.sql.DataSource;

/**
 * This interface defines Pentaho's DatasourceService API
 * 
 * @author Ramaiz Mansoor (rmansoor@pentaho.org)
 * 
 */
public interface IDBDatasourceService {
  public static final String JDBC_POOL = "JDBC_POOL"; //$NON-NLS-1$
  public static final String JDBC_DATASOURCE = "DataSource"; //$NON-NLS-1$
  public static final String IDBDATASOURCE_SERVICE = "IDBDatasourceService"; //$NON-NLS-1$
  public static final String MAX_ACTIVE_KEY = "maxActive";
  public static final String MAX_IDLE_KEY = "maxIdle";
  public static final String MIN_IDLE_KEY = "minIdle";
  public static final String MAX_WAIT_KEY = "maxWait";
  public static final String QUERY_KEY = "validationQuery";
  public static final String TEST_ON_BORROW = "testOnBorrow";
  public static final String TEST_WHILE_IDLE = "testWhileIdle";
  public static final String TEST_ON_RETURN = "testOnReturn";
  public static final String DEFAULT_READ_ONLY = "defaultReadOnly";
  public static final String DEFAULT_AUTO_COMMIT = "defaultAutoCommit";
  public static final String DEFAULT_TRANSACTION_ISOLATION = "defaultTransactionIsolation";
  public static final String TRANSACTION_ISOLATION_NONE_VALUE = "NONE";
  public static final String DEFAULT_CATALOG = "defaultCatalog";
  public static final String POOL_PREPARED_STATEMENTS = "poolPreparedStatements";
  public static final String MAX_OPEN_PREPARED_STATEMENTS = "maxOpenPreparedStatements";
  public static final String ACCESS_TO_UNDERLYING_CONNECTION_ALLOWED = "accessToUnderlyingConnectionAllowed";
  public static final String TIME_BETWEEN_EVICTION_RUNS_MILLIS = "timeBetweenEvictionRunsMillis";
  public static final String REMOVE_ABANDONED = "removeAbandoned";
  public static final String REMOVE_ABANDONED_TIMEOUT = "removeAbandonedTimeout";
  public static final String LOG_ABANDONED = "logAbandoned";
  public static final String INITIAL_SIZE = "initialSize";

  /**
   * This method clears the JNDI DS cache. The need exists because after a JNDI connection edit the old DS must be
   * removed from the cache.
   */
  public void clearCache();

  /**
   * This method clears the JNDI DS cache. The need exists because after a JNDI connection edit the old DS must be
   * removed from the cache.
   */
  public void clearDataSource( String dsName );

  /**
   * Since JNDI is supported different ways in different app servers, it's nearly impossible to have a ubiquitous
   * way to look up a datasource. This method is intended to hide all the lookups that may be required to find a
   * jndi name.
   * 
   * @param dsName
   *          The Datasource name
   * @return DataSource if there is one bound in JNDI
   * @throws DBDatasourceServiceException
   */
  public DataSource getDataSource( String dsName ) throws DBDatasourceServiceException;

  /**
   * Since JNDI is supported different ways in different app servers, it's nearly impossible to have a ubiquitous
   * way to look up a datasource. This method is intended to hide all the lookups that may be required to find a
   * jndi name, and return the actual bound name.
   * 
   * @param dsName
   *          The Datasource name (like SampleData)
   * @return The bound DS name if it is bound in JNDI (like "jdbc/SampleData")
   * @throws DBDatasourceServiceException
   */
  public String getDSBoundName( String dsName ) throws DBDatasourceServiceException;

  /**
   * Since JNDI is supported different ways in different app servers, it's nearly impossible to have a ubiquitous
   * way to look up a datasource. This method is intended to extract just the regular name of a specified JNDI
   * source.
   * 
   * @param dsName
   *          The Datasource name (like "jdbc/SampleData")
   * @return The unbound DS name (like "SampleData")
   */
  public String getDSUnboundName( String dsName );
}
