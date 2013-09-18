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
 * Copyright 2013 Pentaho Corporation.  All rights reserved.
 *
*/
package org.pentaho.platform.plugin.action.olap;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.olap4j.OlapConnection;
import org.pentaho.platform.api.engine.IPentahoSession;

/**
 * This service manages the hosted OLAP connections, implemented with Mondrian,
 * as well as other generic olap4j connections.
 */
public interface IOlapService {

  /**
   * Provides olap connections to a named catalog.
   * For a list of available catalogs,
   * see {@link #getCatalogs(IPentahoSession)}.
   */
  OlapConnection getConnection(
      String catalogName,
      IPentahoSession session)
  throws IOlapServiceException;

  /**
   * Adds a hosted catalog on this server.
   * @param name NAme of the catalog to create
   * @param dataSourceInfo Connection properties. ie: Provider=mondrian;DataSource=SampleData.
   * @param inputStream Stream of the mondrian schema's XML
   * @param overwriteInRepossitory Whether to overwrite a catalog of the same name.
   * @param session Pentaho session to perform this task.
   */
  void addHostedCatalog(
      String name,
      String dataSourceInfo,
      InputStream inputStream,
      boolean overwriteInRepossitory,
      IPentahoSession session)
  throws IOlapServiceException;

  /**
   * Adds a generic olap4j catalog to this server.
   * @param name Name of the catalog to use
   * @param className The class of the driver to use.
   * Must be an imlementation of OlapConnection.
   * @param URL The URL to use.
   * @param user Username to use when connecting.
   * @param password Password to use when connecting.
   * @param props Extra conenction parameters to pass.
   * @param overwrite Whether to overwrite the catalog if it exists.
   * @param pentahoSession The session to use when creating the connection.
   */
  void addOlap4jCatalog(
      String name,
      String className,
      String URL,
      String user,
      String password,
      Properties props,
      boolean overwrite,
      IPentahoSession pentahoSession)
  throws IOlapServiceException;

  /**
   * Provides a list of catalog names known to this server,
   * whether local or remote,
   * @param pentahoSession The session asking for catalogs.
   */
  List<String> getCatalogs(
      final IPentahoSession pentahoSession)
  throws IOlapServiceException;

  /**
   * Removes a catalog from this server, whether hosted or remote.
   * @param catalogName Name of the catalog to delete.
   * @param pentahoSession Session to use when deleting.
   */
  void removeCatalog(
      final String catalogName,
      final IPentahoSession pentahoSession)
  throws IOlapServiceException;

  /**
   * Flushes all schema caches.
   */
  public void flushAll(IPentahoSession pentahoSession);
}