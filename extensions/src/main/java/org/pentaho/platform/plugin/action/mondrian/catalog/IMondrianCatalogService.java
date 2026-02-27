/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


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
   * Returns the stream corresponding to the content of the catalog's schema with the given name.
   * The returned schema will not have any DSP changes applied to it.
   * Returns   * <code>null</code> if name is not found.
   *
   * @param catalogName      The name of the catalog to fetch
   * @param applyAnnotations Whether the returned schema should be the result of applying any existing annotations
   * @return String corresponding to the catalog's schema
   */
  InputStream getCatalogSchemaAsStream( String catalogName, boolean applyAnnotations );

  /**
   * Returns the stream corresponding to the content of the catalog's annotations with the given name. Returns
   * <code>null</code> if there are no annotations.
   *
   * @param catalogName The name of the catalog to fetch
   * @return InputStream corresponding to the catalog's annotations
   */
  InputStream getCatalogAnnotationsAsStream( String catalogName );

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
   * @param overwriteInRepository
   * @param session
   */
  void addCatalog( InputStream inputStream, MondrianCatalog catalog, boolean overwriteInRepository,
      IPentahoSession session );

}
