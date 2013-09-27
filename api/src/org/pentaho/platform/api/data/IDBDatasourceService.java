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
  public static final String MAX_ACTIVE_KEY = "POOLING_maxActive";
  public static final String MAX_IDLE_KEY = "POOLING_maxIdle";
  public static final String MAX_WAIT_KEY = "POOLING_maxWait";
  public static final String QUERY_KEY = "query";

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
   * Since JNDI is supported different ways in different app servers, it's nearly impossible to have a ubiquitous way to
   * look up a datasource. This method is intended to hide all the lookups that may be required to find a jndi name.
   * 
   * @param dsName
   *          The Datasource name
   * @return DataSource if there is one bound in JNDI
   * @throws DBDatasourceServiceException
   */
  public DataSource getDataSource( String dsName ) throws DBDatasourceServiceException;

  /**
   * Since JNDI is supported different ways in different app servers, it's nearly impossible to have a ubiquitous way to
   * look up a datasource. This method is intended to hide all the lookups that may be required to find a jndi name, and
   * return the actual bound name.
   * 
   * @param dsName
   *          The Datasource name (like SampleData)
   * @return The bound DS name if it is bound in JNDI (like "jdbc/SampleData")
   * @throws DBDatasourceServiceException
   */
  public String getDSBoundName( String dsName ) throws DBDatasourceServiceException;

  /**
   * Since JNDI is supported different ways in different app servers, it's nearly impossible to have a ubiquitous way to
   * look up a datasource. This method is intended to extract just the regular name of a specified JNDI source.
   * 
   * @param dsName
   *          The Datasource name (like "jdbc/SampleData")
   * @return The unbound DS name (like "SampleData")
   */
  public String getDSUnboundName( String dsName );
}
