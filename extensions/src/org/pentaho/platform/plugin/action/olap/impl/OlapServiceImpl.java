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
package org.pentaho.platform.plugin.action.olap.impl;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import mondrian.olap.MondrianServer;
import mondrian.rolap.RolapConnection;
import mondrian.server.DynamicContentFinder;
import mondrian.server.MondrianServerRegistry;
import mondrian.spi.CatalogLocator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.olap4j.OlapConnection;
import org.olap4j.OlapException;
import org.pentaho.platform.api.engine.IConnectionUserRoleMapper;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.plugin.action.olap.IOlapConnectionFilter;
import org.pentaho.platform.plugin.action.olap.IOlapService;
import org.pentaho.platform.plugin.action.olap.IOlapServiceException;
import org.pentaho.platform.plugin.services.connections.mondrian.MDXConnection;
import org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper;
import org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper.HostedCatalogInfo;
import org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper.Olap4jServerInfo;
import org.pentaho.platform.repository.solution.filebased.MondrianVfs;

/**
 * Implementation of the IOlapService which uses the
 * {@link MondrianCatalogRepositoryHelper} as a backend to
 * store the connection informations and uses {@link DriverManager}
 * to create the connections.
 *
 * <p>It will also check for the presence of a {@link IConnectionUserRoleMapper}
 * and change the roles accordingly before creating a connection.
 *
 * <p>This implementation is thread safe. It will use a {@link ReadWriteLock}
 * to manage the access to its metadata.
 */
public class OlapServiceImpl implements IOlapService {

  static final String MONDRIAN_DATASOURCE_FOLDER = "mondrian"; //$NON-NLS-1$

  final ReadWriteLock cacheLock = new ReentrantReadWriteLock();
  private AtomicInteger cacheStateHash = new AtomicInteger();
  private List<IOlapService.Catalog> cache = new ArrayList<IOlapService.Catalog>();

  /**
   * This is the default name of an XMLA data source on the server.
   * Mondrian XMLA servers only support a single data source.
   */
  static final String DATASOURCE_NAME = "Pentaho";

  private static final Log LOG = getLogger();

  /*
   * Do not access these two fields directly. They need to be accessed through
   * getRepository and getHelper because we can't init them before spring is
   * done initializing the sub modules.
   */
  private IUnifiedRepository repository;
  private MondrianCatalogRepositoryHelper helper;

  private MondrianServer server = null;
  private final List<IOlapConnectionFilter> filters;

  private static Log getLogger() {
    return LogFactory.getLog( IOlapService.class );
  }

  /**
   * Empty constructor. Creating an instance from here will
   * use the {@link PentahoSystem} to fetch the {@link IUnifiedRepository}
   * at runtime.
   */
  public OlapServiceImpl() {
    this( null );
  }

  /**
   * Constructor for testing purposes. Takes a repository as a parameter.
   */
  public OlapServiceImpl( IUnifiedRepository repo ) {
    this.repository = repo;
    this.filters = new CopyOnWriteArrayList<IOlapConnectionFilter>();

    try {
      DefaultFileSystemManager dfsm = (DefaultFileSystemManager) VFS.getManager();
      if ( dfsm.hasProvider( "mondrian" ) == false ) {
        dfsm.addProvider( "mondrian", new MondrianVfs() );
      }
    } catch ( FileSystemException e ) {
      throw new RuntimeException( e );
    }
  }

  synchronized IUnifiedRepository getRepository() {
    if ( repository == null ) {
      repository = PentahoSystem.get( IUnifiedRepository.class );
    }
    return repository;
  }

  synchronized MondrianCatalogRepositoryHelper getHelper() {
    if ( helper == null ) {
      helper =
        new MondrianCatalogRepositoryHelper(
          getRepository() );
    }
    return helper;
  }

  void initCache() {
    final Lock readLock = cacheLock.readLock();
    try {

      readLock.lock();

      if ( getCurrentCacheHash() != cacheStateHash.get() ) {

        final Lock writeLock = cacheLock.writeLock();

        try {

          writeLock.lock();

          // First clear the cache
          cache.clear();

          SecurityHelper.getInstance().runAsSystem(
            new Callable<Void>() {
              public Void call() throws Exception {
                // Now build the cache
                for ( String name : getHelper().getHostedCatalogs() ) {
                  addCatalogToCache( name );
                }
                for ( String name : getHelper().getOlap4jServers() ) {
                  addCatalogToCache( name );
                }
                return null;
              }
            } );

          // Sort it all.
          Collections.sort(
            cache,
            new Comparator<IOlapService.Catalog>() {
              public int compare( Catalog o1, Catalog o2 ) {
                return o1.name.compareTo( o2.name );
              }
            } );

          // Update the hash
          cacheStateHash.set( getCurrentCacheHash() );

        } catch ( IOlapServiceException e ) {

          LOG.error(
            "Failed to initialize the connection cache",
            e );

        } catch ( Exception e ) {

          throw new IOlapServiceException( e );

        } finally {
          writeLock.unlock();
        }
      }

    } finally {
      readLock.unlock();
    }
  }

  /**
   * Adds a catalog and its children to the cache.
   * Do not use directly. This must be called with a write lock
   * on the cache.
   * @param catalogName The name of the catalog to load in cache.
   */
  private void addCatalogToCache( String catalogName ) {

    final IOlapService.Catalog catalog =
      new Catalog( catalogName, new ArrayList<IOlapService.Schema>() );

    OlapConnection connection = null;

    try {

      connection =
        getConnection( catalogName, PentahoSessionHolder.getSession() );

      connection.setCatalog( catalogName );

      for ( org.olap4j.metadata.Schema schema4j : connection.getOlapSchemas() ) {

        connection.setSchema( schema4j.getName() );

        final IOlapService.Schema schema =
          new Schema(
            schema4j.getName(),
            catalog,
            new ArrayList<IOlapService.Cube>(),
            new ArrayList<String>( connection.getAvailableRoleNames() ) );

        for ( org.olap4j.metadata.Cube cube4j : schema4j.getCubes() ) {
          schema.cubes.add(
            new IOlapService.Cube( cube4j.getName(), schema ) );
        }
      }

      // We're done.
      cache.add( catalog );

    } catch ( OlapException e ) {

      LOG.warn(
        "Failed to initialize the olap connection cache for catalog "
        + catalogName,
        e );

      // We still add the catalog to the list.
      cache.add(
        new Catalog(
          catalogName,
          Collections.<IOlapService.Schema>emptyList() ) );

    } finally {
      try {
        if ( connection != null ) {
          connection.close();
        }
      } catch ( SQLException e ) {
        LOG.warn(
          "Failed to gracefully close an olap connection to catalog "
          + catalogName,
          e );
      }
    }
  }

  private int getCurrentCacheHash() {
    int hash = 37;
    for ( String name : getHelper().getHostedCatalogs() ) {
      hash = hash( hash, name );
    }
    for ( String name : getHelper().getOlap4jServers() ) {
      hash = hash( hash, name );
    }
    return hash;
  }

  private static int hash( int h, Object o ) {
    int k = ( o == null ) ? 0 : o.hashCode();
    return ( ( h << 4 ) | h ) ^ k;
  }

  public void addHostedCatalog(
    String name,
    String dataSourceInfo,
    InputStream inputStream,
    boolean overwrite,
    IPentahoSession session ) {

    // Access
    if ( !hasAccess( makeHostedPath( name ), EnumSet.of( RepositoryFilePermission.WRITE ), session ) ) {
      LOG.debug( "user does not have access; throwing exception" ); //$NON-NLS-1$
      throw new IOlapServiceException(
        Messages.getInstance().getErrorString(
          "OlapServiceImpl.ERROR_0003_INSUFFICIENT_PERMISSION" ), //$NON-NLS-1$
        IOlapServiceException.Reason.ACCESS_DENIED );
    }

    // check for existing vs. the overwrite flag.
    if ( getCatalogNames( session ).contains( name ) && !overwrite ) {
      throw new IOlapServiceException(
        Messages.getInstance().getErrorString(
          "OlapServiceImpl.ERROR_0004_ALREADY_EXISTS" ), //$NON-NLS-1$
        IOlapServiceException.Reason.ALREADY_EXISTS );
    }

    try {
      MondrianCatalogRepositoryHelper helper =
        new MondrianCatalogRepositoryHelper( getRepository() );
      helper.addHostedCatalog( inputStream, name, dataSourceInfo );
    } catch ( Exception e ) {
      throw new IOlapServiceException(
        e,
        IOlapServiceException.Reason.convert( e ) );
    }
  }

  protected boolean hasAccess(
    final String path,
    final EnumSet<RepositoryFilePermission> perms,
    IPentahoSession session ) {

    try {
      return SecurityHelper.getInstance().runAsUser(
        session.getName(),
        new Callable<Boolean>() {
          public Boolean call() throws Exception {
            return repository.hasAccess(
              path,
              perms );
          }
        } );
    } catch ( Exception e ) {
      throw new IOlapServiceException( e );
    }
  }

  public void addOlap4jCatalog(
    String name,
    String className,
    String URL,
    String user,
    String password,
    Properties props,
    boolean overwrite,
    IPentahoSession session ) {

    // Access
    if ( !hasAccess( makeGenericPath( name ), EnumSet.of( RepositoryFilePermission.WRITE ), session ) ) {
      LOG.debug( "user does not have access; throwing exception" ); //$NON-NLS-1$
      throw new IOlapServiceException(
        Messages.getInstance().getErrorString(
          "OlapServiceImpl.ERROR_0003_INSUFFICIENT_PERMISSION" ), //$NON-NLS-1$
        IOlapServiceException.Reason.ACCESS_DENIED );
    }

    // check for existing vs. the overwrite flag.
    if ( getCatalogNames( session ).contains( name ) && !overwrite ) {
      throw new IOlapServiceException(
        Messages.getInstance().getErrorString(
          "OlapServiceImpl.ERROR_0004_ALREADY_EXISTS" ), //$NON-NLS-1$
        IOlapServiceException.Reason.ALREADY_EXISTS );
    }

    MondrianCatalogRepositoryHelper helper =
        new MondrianCatalogRepositoryHelper( getRepository() );

    helper.addOlap4jServer( name, className, URL, user, password, props );
  }

  public void removeCatalog( String name, IPentahoSession session ) {

    // Check Access
    final String path =
      isHosted( name )
        ? makeHostedPath( name )
        : makeGenericPath( name );
    if ( !hasAccess( path, EnumSet.of( RepositoryFilePermission.DELETE ), session ) ) {
      LOG.debug( "user does not have access; throwing exception" ); //$NON-NLS-1$
      throw new IOlapServiceException(
        Messages.getInstance().getErrorString(
          "OlapServiceImpl.ERROR_0003_INSUFFICIENT_PERMISSION" ), //$NON-NLS-1$
        IOlapServiceException.Reason.ACCESS_DENIED );
    }

    if ( !getCatalogNames( session ).contains( name ) ) {
      throw new IOlapServiceException(
        Messages.getInstance().getErrorString(
          "MondrianCatalogHelper.ERROR_0015_CATALOG_NOT_FOUND",
          name ) );
    }

    // This could be a remote connection
    getHelper().deleteCatalog( name );
  }

  public void flushAll( IPentahoSession pentahoSession ) {
    final Lock writeLock = cacheLock.writeLock();
    try {
      writeLock.lock();

      // Start by flushing the local cache.
      cache.clear();
      cacheStateHash.set( 0 );

      // Now flush the hosted server's caches.
      getServer().getConnection( null, null, null )
        .unwrap( RolapConnection.class )
        .getCacheControl( null )
        .flushSchemaCache();

    } catch ( Exception e ) {
      throw new IOlapServiceException( e );
    } finally {
      writeLock.unlock();
    }
  }

  public List<String> getCatalogNames(
      IPentahoSession pentahoSession )
    throws IOlapServiceException {
    // This is the quick implementation to obtain a list of catalogs
    // without having to open connections. IT can be used by UI tools
    // and tests.
    final List<String> names = new ArrayList<String>();

    for ( String name : getHelper().getHostedCatalogs() ) {
      if ( hasAccess( makeHostedPath( name ), EnumSet.of( RepositoryFilePermission.READ ), pentahoSession ) ) {
        names.add( name );
      }
    }

    for ( String name : getHelper().getOlap4jServers() ) {
      if ( hasAccess( makeGenericPath( name ), EnumSet.of( RepositoryFilePermission.READ ), pentahoSession ) ) {
        names.add( name );
      }
    }

    // Sort it all.
    Collections.sort( names );

    return names;
  }

  public List<IOlapService.Catalog> getCatalogs(
    IPentahoSession session )
    throws IOlapServiceException {
    final Lock readLock = cacheLock.readLock();
    try {
      readLock.lock();
      initCache();

      // Do not leak the cache list.
      // Do not allow modifications on the list.
      final List<IOlapService.Catalog> catalogs =
        new ArrayList<IOlapService.Catalog>();
      for ( Catalog catalog : cache ) {
        if ( hasAccess( catalog.name, EnumSet.of( RepositoryFilePermission.READ ), session ) ) {
          catalogs.add( catalog );
        }
      }

      return Collections.unmodifiableList(
        new ArrayList<>( cache ) );
    } finally {
      readLock.unlock();
    }
  }


  public List<IOlapService.Schema> getSchemas(
    String parentCatalog,
    IPentahoSession pentahoSession ) {
    final List<IOlapService.Schema> schemas = new ArrayList<IOlapService.Schema>();
    final Lock readLock = cacheLock.readLock();
    try {
      readLock.lock();
      initCache();
      for ( IOlapService.Catalog catalog : getCatalogs( pentahoSession ) ) {
        if ( parentCatalog == null
          || catalog.name.equals( parentCatalog ) ) {
          schemas.addAll( catalog.schemas );
        }
      }
    } finally {
      readLock.unlock();
    }
    return schemas;
  }

  public List<Cube> getCubes(
    String parentCatalog,
    String parentSchema,
    IPentahoSession pentahoSession ) {
    final List<IOlapService.Cube> cubes = new ArrayList<IOlapService.Cube>();
    final Lock readLock = cacheLock.readLock();
    try {
      readLock.lock();
      initCache();
      for ( IOlapService.Schema schema : getSchemas( parentCatalog, pentahoSession ) ) {
        if ( parentSchema == null
          || schema.name.equals( parentSchema ) ) {
          cubes.addAll( schema.cubes );
        }
      }
    } finally {
      readLock.unlock();
    }
    return cubes;
  }

  public OlapConnection getConnection(
    String catalogName,
    IPentahoSession session )
    throws IOlapServiceException {

    // Check valid name.
    if ( catalogName == null ) {
      throw new NullPointerException( "catalogName cannot be null." );
    }

    // Check Access
    final String path =
      isHosted( catalogName )
        ? makeHostedPath( catalogName )
        : makeGenericPath( catalogName );
    if ( !hasAccess( path, EnumSet.of( RepositoryFilePermission.READ ), session ) ) {
      LOG.debug( "user does not have access; throwing exception" ); //$NON-NLS-1$
      throw new IOlapServiceException(
        Messages.getInstance().getErrorString(
          "OlapServiceImpl.ERROR_0003_INSUFFICIENT_PERMISSION" ), //$NON-NLS-1$
        IOlapServiceException.Reason.ACCESS_DENIED );
    }

    // Check its existence.
    if ( !getCatalogNames( session ).contains( catalogName ) ) {
      throw new IOlapServiceException(
        Messages.getInstance().getErrorString(
          "MondrianCatalogHelper.ERROR_0015_CATALOG_NOT_FOUND",
          catalogName ) );
    }

    // Check if it is a remote server
    if ( getHelper().getOlap4jServers().contains( catalogName ) ) {
      return makeOlap4jConnection( catalogName, session );
    }

    final IConnectionUserRoleMapper mapper =
      PentahoSystem.get(
        IConnectionUserRoleMapper.class,
        MDXConnection.MDX_CONNECTION_MAPPER_KEY,
        null ); // Don't use the user session here yet.

    String[] effectiveRoles = new String[0];

    /*
     * If Catalog/Schema are null (this happens with high level metadata requests,
     * like DISCOVER_DATASOURCES) we can't use the role mapper, even if it
     * is present and configured.
     */
    if ( mapper != null ) {
      // Use the role mapper.
      try {
        effectiveRoles =
          mapper
            .mapConnectionRoles(
              session,
              catalogName );
        if ( effectiveRoles == null ) {
          effectiveRoles = new String[0];
        }
      } catch ( PentahoAccessControlException e ) {
        throw new IOlapServiceException( e );
      }
    }

    // Now we tokenize that list.
    boolean addComma = false;
    StringBuilder roleName = new StringBuilder();
    for ( String role : effectiveRoles ) {
      if ( addComma ) {
        roleName.append( "," ); //$NON-NLS-1$
      }
      roleName.append( role );
      addComma = true;
    }

    // Return a connection
    try {
      return getServer().getConnection(
        DATASOURCE_NAME,
        catalogName,
        roleName.toString(),
        new Properties() );
    } catch ( Exception e ) {
      throw new IOlapServiceException( e );
    }
  }

  private OlapConnection makeOlap4jConnection(
    String name,
    IPentahoSession session ) {
    final Olap4jServerInfo olapServerInfo =
      getHelper().getOlap4jServerInfo( name );
    assert olapServerInfo != null;

    // Make sure the driver is present
    try {
      Class.forName( olapServerInfo.className );
    } catch ( ClassNotFoundException e ) {
      throw new IOlapServiceException( e );
    }

    // As per the JDBC specs, we can set the user/pass into
    // connection properties called 'user' and 'password'.
    final Properties newProps =
      new Properties( olapServerInfo.properties );

    // First, apply the filters.
    for ( IOlapConnectionFilter filter : this.filters ) {
      filter.filterProperties( newProps );
    }

    // Then override the user and password. We do this after the filters
    // so as not to expose this.
    if ( olapServerInfo.user != null ) {
      newProps.put(
        "user", olapServerInfo.user );
    }
    if ( olapServerInfo.password != null ) {
      newProps.put(
        "password", olapServerInfo.password );
    }

    try {
      final Connection conn =
        DriverManager.getConnection(
          olapServerInfo.URL, newProps );
      return conn.unwrap( OlapConnection.class );
    } catch ( SQLException e ) {
      throw new IOlapServiceException( e );
    }
  }

  private synchronized MondrianServer getServer() {
    if ( server == null ) {
      server =
        MondrianServerRegistry.INSTANCE.createWithRepository(
          new DynamicContentFinder( "http://not-needed.com" ) {
            @Override
            public String getContent() {
              // We dynamically generate the XML required by the
              // XMLA servlet. It must conform to Datasources.dtd,
              // as specified by olap4j-xmlaserver.
              return getDatasourcesXml();
            }
          },
          new CatalogLocator() {
            public String locate( String URL ) {
              return URL;
            }
          }
        );
    }
    return server;
  }

  private String getDatasourcesXml() {
    try {
      return
        SecurityHelper.getInstance().runAsSystem(
          new Callable<String>() {
            public String call() throws Exception {
              return generateInMemoryDatasourcesXml();
            }
          } );
    } catch ( Exception e ) {
      throw new IOlapServiceException( e );
    }
  }

  private String generateInMemoryDatasourcesXml() {
    StringBuffer datasourcesXML = new StringBuffer();
    datasourcesXML.append( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" ); //$NON-NLS-1$
    datasourcesXML.append( "<DataSources>\n" ); //$NON-NLS-1$

    datasourcesXML.append( "<DataSource>\n" ); //$NON-NLS-1$
    datasourcesXML.append( "<DataSourceName>" + DATASOURCE_NAME + "</DataSourceName>\n" ); //$NON-NLS-1$
    datasourcesXML.append( "<DataSourceDescription>Pentaho BI Platform Datasources</DataSourceDescription>\n" ); //$NON-NLS-1$
    datasourcesXML.append( "<URL>" + PentahoRequestContextHolder.getRequestContext().getContextPath() + "Xmla</URL>\n" ); //$NON-NLS-1$
    datasourcesXML.append( "<DataSourceInfo>Provider=mondrian</DataSourceInfo>\n" ); //$NON-NLS-1$
    datasourcesXML.append( "<ProviderName>PentahoXMLA</ProviderName>\n" ); //$NON-NLS-1$
    datasourcesXML.append( "<ProviderType>MDP</ProviderType>\n" ); //$NON-NLS-1$
    datasourcesXML.append( "<AuthenticationMode>Unauthenticated</AuthenticationMode>\n" ); //$NON-NLS-1$
    datasourcesXML.append( "<Catalogs>\n" ); //$NON-NLS-1$

    // Start with local catalogs.
    for ( String name : getHelper().getHostedCatalogs() ) {
      final HostedCatalogInfo hostedServerInfo =
        getHelper().getHostedCatalogInfo( name );
      addCatalogXml(
        datasourcesXML,
        hostedServerInfo.name,
        hostedServerInfo.dataSourceInfo,
        hostedServerInfo.definition );
    }

    // Don't add the olap4j catalogs. This doesn't work for now.

    datasourcesXML.append( "</Catalogs>\n" ); //$NON-NLS-1$
    datasourcesXML.append( "</DataSource>\n" ); //$NON-NLS-1$
    datasourcesXML.append( "</DataSources>\n" ); //$NON-NLS-1$
    return datasourcesXML.toString();
  }

  private void addCatalogXml( StringBuffer str, String catalogName, String dsInfo, String definition ) {
    assert definition != null;
    str.append( "<Catalog name=\"" + catalogName + "\">\n" ); //$NON-NLS-1$ //$NON-NLS-2$
    if ( dsInfo != null ) {
      str.append( "<DataSourceInfo>" + dsInfo + "</DataSourceInfo>\n" ); //$NON-NLS-1$ //$NON-NLS-2$
    }
    str.append( "<Definition>" + definition + "</Definition>\n" ); //$NON-NLS-1$ //$NON-NLS-2$
    str.append( "</Catalog>\n" ); //$NON-NLS-1$
  }

  public void setConnectionFilters( Collection<IOlapConnectionFilter> filters ) {
    this.filters.addAll( filters );
  }

  private String makeHostedPath( String name ) {
    return
      MondrianCatalogRepositoryHelper.ETC_MONDRIAN_JCR_FOLDER
      + RepositoryFile.SEPARATOR
      + name;
  }

  private String makeGenericPath( String name ) {
    return
      MondrianCatalogRepositoryHelper.ETC_OLAP_SERVERS_JCR_FOLDER
      + RepositoryFile.SEPARATOR
      + name;
  }

  private boolean isHosted( String name ) {
    if ( getHelper().getHostedCatalogs().contains( name ) ) {
      return true;
    } else {
      return false;
    }
  }
}
