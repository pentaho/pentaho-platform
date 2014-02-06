/*
 * Copyright 2002 - 2013 Pentaho Corporation.  All rights reserved.
 *
 * This software was developed by Pentaho Corporation and is provided under the terms
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
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
 *
 * <p>There are two types of connections that you can create. Either
 *
 * <ul><li>Hosted locally by the local Mondrian server instance</li>
 * <li>Hosted by some other OLAP server and configured as a generic
 * olap4j connection.</li></ul>
 *
 * <p>Whether hosted or generic, the OLAP connections must not share the same
 * name.
 *
 * <p>To create a hosted connection, you must use the method
 * {@link #addHostedCatalog(String, String, InputStream, boolean, IPentahoSession)}.
 * The InputStream parameter must be pointing to a Mondrian schema file.
 *
 * <p>When a hosted connection is added, the service will also attempt to use
 * the IConnectionUserRoleMapper, if one is configured.
 *
 * <p>To create a generic connection, use the method
 * {@link #addOlap4jCatalog(String, String, String, String, String, Properties, boolean, IPentahoSession)}.
 * The parameters are the standard ones used by JDBC and will be passed to the
 * java.sql.DriverManager as-is.
 *
 * <p>To obtain a list of the configured catalogs, there are a few options.
 * Calling {@link #getCatalogNames(IPentahoSession)} is the cheap and fast method.
 * It will return a list of the catalog names available. For more metadata, one can
 * use {@link #getCatalogs(IPentahoSession)}, {@link #getSchemas(String, IPentahoSession)}
 * or {@link #getCubes(String, String, IPentahoSession)}. These alternative methods
 * come at a higher price, since we will need to activate each of the configured
 * connections to populate the metadata.
 *
 * <p>Throughout the API, it is possible to pass a user's session. In that case,
 * only the catalogs which are accessible to the specified user will be available.
 * Passing 'null' as the sesison has the effect of granting root access and will
 * allow access to all catalogs.
 */
public interface IOlapService {

  /**
   * Provides olap connections to a named catalog.
   * For a list of available catalogs,
   * see {@link #getCatalogs(IPentahoSession)}.
   */
  OlapConnection getConnection(
      String catalogName,
      IPentahoSession session )
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
      IPentahoSession session )
    throws IOlapServiceException;

  /**
   * Adds a generic olap4j catalog to this server.
   * @param name Name of the catalog to use
   * @param className The class of the driver to use.
   * Must be an implementation of java.sql.Driver.
   * @param URL The URL to use.
   * @param user Username to use when connecting.
   * @param password Password to use when connecting.
   * @param props Extra connection parameters to pass.
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
      IPentahoSession pentahoSession )
    throws IOlapServiceException;

  /**
   * Provides a list of catalog names known to this server,
   * whether local or remote.
   * <p>This method is much cheaper to invoke than
   * {@link #getCatalogs(IPentahoSession)} since it doesn't
   * require the connection to be opened.
   * @param pentahoSession The session asking for catalogs.
   */
  List<String> getCatalogNames(
      final IPentahoSession pentahoSession )
    throws IOlapServiceException;

  /**
   * Provides a list of catalogs known to this server,
   * whether local or remote. Returns a tree, rooted at the catalog,
   * representing all of the schemas and cubes included in this catalog.
   * @param pentahoSession The session asking for catalogs.
   */
  List<IOlapService.Catalog> getCatalogs(
      final IPentahoSession pentahoSession )
    throws IOlapServiceException;

  /**
   * Provides a list of the available schemas, whether constrained to a
   * particular catalog or not, represented as a tree of the schema and all
   * of its cubes.
   * @param parentCatalog The catalog to constrain the list of schemas, or null
   * to return the whole list.
   * @param pentahoSession The session asking for schemas.
   * @return A list of schemas.
   */
  List<IOlapService.Schema> getSchemas(
      String parentCatalog,
      final IPentahoSession pentahoSession )
    throws IOlapServiceException;;

  /**
   * Provides a list of the available cubes, whether constrained to a
   * particular catalog and/or schema or not.
   * @param parentCatalog The catalog to constrain the list of cubes, or null
   * to return the whole list.
   * @param parentSchema The schema to constrain the list of cubes, or null
   * to return the whole list.
   * @param pentahoSession The session asking for cubes.
   * @return A list of cubes.
   */
  List<IOlapService.Cube> getCubes(
    String parentCatalog,
    String parentSchema,
    final IPentahoSession pentahoSession );

  /**
   * Removes a catalog from this server, whether hosted or remote.
   * @param catalogName Name of the catalog to delete.
   * @param pentahoSession Session to use when deleting.
   */
  void removeCatalog(
      final String catalogName,
      final IPentahoSession pentahoSession )
    throws IOlapServiceException;

  /**
   * Flushes all schema caches.
   */
  public void flushAll( IPentahoSession pentahoSession );

  /**
   * Representation of a catalog. Catalogs have {@link Schema} children.
   */
  public class Catalog {
    public final String name;
    public final List<Schema> schemas;
    public Catalog( String name, List<Schema> schemas ) {
      /**
       * The unique name of this catalog.
       */
      this.name = name;
      /**
       * A lost of schemas which are included in this catalog.
       */
      this.schemas = schemas;
    }
    public String toString() {
      return name;
    }
    // Bean accessors
    public String getName() {
      return name;
    }
    public List<Schema> getSchemas() {
      return schemas;
    }
  }

  /**
   * Representation of a schema. Schemas have {@link Cube} children
   * and a parent {@link Catalog}, along with a list of role names.
   */
  public class Schema {
    public final String name;
    public final Catalog catalog;
    public final List<Cube> cubes;
    public final List<String> roleNames;
    public Schema( String name, Catalog parent, List<Cube> cubes, List<String> roles ) {
      /**
       * The name of this schema.
       */
      this.name = name;
      /**
       * The parent catalog to which this schema belongs to.
       */
      this.catalog = parent;
      /**
       * A list of cubes included in this schema.
       */
      this.cubes = cubes;
      /**
       * A list of role names defined in this schema.
       */
      this.roleNames = roles;
    }
    public String toString() {
      return name;
    }
    // Bean accessors
    public String getName() {
      return name;
    }
    public List<Cube> getCubes() {
      return cubes;
    }
    public List<String> getRoleNames() {
      return roleNames;
    }
  }

  /**
   * Representation of a Cube with its parent {@link Schema}.
   */
  public class Cube {
    /**
     * The unique name of the cube.
     */
    public final String name;
    /**
     * The caption of the cube.
     */
    public final String caption;
    /**
     * The parent schema to which belongs this cube.
     */
    public final Schema schema;
    public Cube( String name, String caption, Schema parent ) {
      this.name = name;
      this.caption = caption;
      this.schema = parent;
    }
    public String toString() {
      return name;
    }
    // Bean accessors.
    public String getName() {
      return name;
    }
    public String getCaption() {
      return caption;
    }
  }
}
