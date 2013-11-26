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

package org.pentaho.platform.plugin.action.mondrian.catalog;

import org.pentaho.platform.api.engine.IPentahoSession;

import java.io.InputStream;
import java.util.List;

/**
 * A service registering/enumerating registered Mondrian catalogs (schemas).
 * 
 * @author mlowery
 */
public interface IMondrianCatalogService {

  /**
   * Lists all catalogs (filtered according to access control rules).
   * 
   * @param jndiOnly
   *          return only JNDI-based catalogs
   */
  List<MondrianCatalog> listCatalogs( IPentahoSession pentahoSession, boolean jndiOnly );

  /**
   * Adds to the global catalog list and possibly persists this information.
   * 
   * @param overwrite
   *          true to overwrite existing catalog (based on match with definition and effectiveDataSourceInfo
   */
  void addCatalog( MondrianCatalog catalog, boolean overwrite, IPentahoSession pentahoSession )
    throws MondrianCatalogServiceException;

  /**
   * Returns the catalog with the given context - name or definition allowable. Returns <code>null</code> if context not
   * recognized.
   * 
   * @param context
   *          Either the name of the catalog to fetch, or the catalog's definition string
   * 
   *          NOTE that the context can be the catalog name or the definition string for the catalog. If you are using
   *          the definition string to retrieve the catalog from the cache, you cannot be guaranteed what datasource is
   *          in play; so under these circumstances, this catalog's definition is the only part of the catalog that can
   *          be trusted. As this feature was added to enable looking up Mondrian roles from the schema, we don't much
   *          care which datasource is in play.
   */
  MondrianCatalog getCatalog( String context, final IPentahoSession pentahoSession );

  /**
   * this method loads a Mondrian schema
   * 
   * @param solutionLocation
   *          location of the schema
   * @param pentahoSession
   *          current session object
   * 
   * @return Mondrian Schema object
   */
  MondrianSchema loadMondrianSchema( String solutionLocation, IPentahoSession pentahoSession );

  /**
   * this method removes a Mondrian schema from the platform
   * 
   * @param catalogName
   *          the name of the catalog to remove
   * @param pentahoSession
   *          current session object
   */
  void removeCatalog( final String catalogName, final IPentahoSession pentahoSession );

  /**
   * Flushes the catalog cache.
   * 
   * @param pentahoSession
   */
  public void reInit( IPentahoSession pentahoSession ) throws MondrianCatalogServiceException;

  /**
   * pass the input stream directly from data access PUC and schema workbench
   * 
   * @param inputStream
   * @param catalog
   * @param overwriteInRepossitory
   * @param session
   */
  void addCatalog( InputStream inputStream, MondrianCatalog catalog, boolean overwriteInRepossitory,
      IPentahoSession session );

}
