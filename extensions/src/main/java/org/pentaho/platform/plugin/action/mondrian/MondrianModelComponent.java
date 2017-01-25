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

package org.pentaho.platform.plugin.action.mondrian;

import mondrian.olap.AxisOrdinal;
import mondrian.olap.Connection;
import mondrian.olap.Cube;
import mondrian.olap.Dimension;
import mondrian.olap.Hierarchy;
import mondrian.olap.Member;
import mondrian.olap.MondrianException;
import mondrian.olap.Query;
import mondrian.olap.Schema;
import mondrian.olap.Util;
import mondrian.rolap.RolapConnection;
import mondrian.rolap.RolapConnectionProperties;
import mondrian.server.Locus;
import mondrian.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.connection.PentahoConnectionFactory;
import org.pentaho.platform.engine.services.solution.ComponentBase;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogComplementInfo;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogHelper;
import org.pentaho.platform.plugin.services.connections.mondrian.MDXConnection;
import org.pentaho.platform.plugin.services.connections.sql.SQLConnection;
import org.pentaho.platform.util.logging.Logger;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * @author James Dixon
 *         <p/>
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 *         Code Templates
 */
public class MondrianModelComponent extends ComponentBase {

  private static final long serialVersionUID = -718697500002076945L;

  @Override
  public Log getLogger() {
    return LogFactory.getLog( MondrianModelComponent.class );
  }

  @Override
  protected boolean validateSystemSettings() {
    // This component does not have any system settings to validate
    return true;
  }

  @Override
  public boolean init() {
    // get the settings from the system configuration file
    return true;

  }

  @Override
  public boolean validateAction() {

    return true;

  }

  @Override
  public boolean executeAction() {

    return true;
  }

  @Override
  public void done() {

  }

  public static String getInitialQuery( final Properties properties, final String cubeName, IPentahoSession session )
    throws Throwable {

    // Apply any properties for this catalog specified in datasource.xml
    IMondrianCatalogService mondrianCatalogService =
      PentahoSystem.get( IMondrianCatalogService.class, "IMondrianCatalogService",
        PentahoSessionHolder.getSession() );
    List<MondrianCatalog> catalogs = mondrianCatalogService.listCatalogs( PentahoSessionHolder.getSession(), true );
    String propCat = properties.getProperty( RolapConnectionProperties.Catalog.name() );
    for ( MondrianCatalog cat : catalogs ) {
      if ( cat.getDefinition().equalsIgnoreCase( propCat ) ) {
        Util.PropertyList connectProperties = Util.parseConnectString( cat.getDataSourceInfo() );
        Iterator<Pair<String, String>> iter = connectProperties.iterator();
        while ( iter.hasNext() ) {
          Pair<String, String> pair = iter.next();
          if ( !properties.containsKey( pair.getKey() ) ) // Only set if not set already
          {
            properties.put( pair.getKey(), pair.getValue() );
          }
        }
        break;
      }
    }

    MDXConnection mdxConnection =
      (MDXConnection) PentahoConnectionFactory.getConnection( IPentahoConnection.MDX_DATASOURCE, properties, session,
        null );
    // mdxConnection.setProperties( properties );
    Connection connection = mdxConnection.getConnection();
    if ( connection == null ) {
      Logger
        .error(
          "MondrianModelComponent", Messages.getInstance()
          .getErrorString( "MondrianModel.ERROR_0001_INVALID_CONNECTION",
            properties.toString() ) ); //$NON-NLS-1$ //$NON-NLS-2$
      return null;
    }

    try {
      return MondrianModelComponent.getInitialQuery( connection, cubeName );
    } catch ( Throwable t ) {
      if ( t instanceof MondrianException ) {
        // pull the cause out, otherwise it never gets logged
        Throwable cause = ( (MondrianException) t ).getCause();
        if ( cause != null ) {
          throw cause;
        } else {
          throw t;
        }
      } else {
        throw t;
      }
    }
  }

  /**
   * @param modelPath
   * @param connectionString
   * @param driver
   * @param user
   * @param password
   * @param cubeName
   * @return mdx string that represents the initial query
   * @throws Throwable
   * @deprecated
   */
  @Deprecated
  public static String getInitialQuery( final String modelPath, final String connectionString, final String driver,
                                        final String user, final String password, final String cubeName,
                                        IPentahoSession session ) throws Throwable {
    return MondrianModelComponent.getInitialQuery( modelPath, connectionString, driver, user, password, cubeName, null,
      session );
  }

  /**
   * @param modelPath
   * @param connectionString
   * @param driver
   * @param user
   * @param password
   * @param cubeName
   * @param roleName
   * @return mdx string that represents the initial query
   * @throws Throwable
   * @deprecated
   */
  @Deprecated
  public static String getInitialQuery( String modelPath, final String connectionString, final String driver,
                                        final String user, final String password, final String cubeName,
                                        final String roleName, IPentahoSession session )
    throws Throwable {

    Properties properties = new Properties();

    // TODO support driver manager connections
    if ( !PentahoSystem.ignored ) {
      if ( driver != null ) {
        properties.put( "Driver", driver ); //$NON-NLS-1$
      }
      if ( user != null ) {
        properties.put( "User", user ); //$NON-NLS-1$
      }
      if ( password != null ) {
        properties.put( "Password", password ); //$NON-NLS-1$
      }
    }

    if ( modelPath.indexOf( "http" ) == 0 ) { //$NON-NLS-1$
      properties.put( RolapConnectionProperties.Catalog.name(), modelPath ); //$NON-NLS-1$
    } else {
      if ( modelPath.indexOf( "http" ) == 0 ) { //$NON-NLS-1$
        properties.put( RolapConnectionProperties.Catalog.name(), modelPath ); //$NON-NLS-1$
      } else {
        if ( !modelPath.startsWith( "solution:" ) && !modelPath.startsWith( "mondrian:" ) ) { //$NON-NLS-1$
          modelPath = "solution:" + modelPath; //$NON-NLS-1$
        }
        properties.put( RolapConnectionProperties.Catalog.name(), modelPath ); //$NON-NLS-1$
      }
    }
    properties.put( RolapConnectionProperties.Provider.name(), "mondrian" ); //$NON-NLS-1$ //$NON-NLS-2$
    properties.put( RolapConnectionProperties.PoolNeeded.name(), "false" ); //$NON-NLS-1$//$NON-NLS-2$
    properties.put( RolapConnectionProperties.DataSource.name(), connectionString ); //$NON-NLS-1$
    if ( roleName != null ) {
      properties.put( RolapConnectionProperties.Role.name(), roleName ); //$NON-NLS-1$
    }

    return MondrianModelComponent.getInitialQuery( properties, cubeName, session );
  }

  /**
   * @param modelPath
   * @param connectionString
   * @param cubeName
   * @return mdx string that represents the initial query
   * @throws Throwable
   * @deprecated
   */
  @Deprecated
  public static String getInitialQuery( final String modelPath, final String connectionString, final String cubeName,
                                        IPentahoSession session ) throws Throwable {
    return MondrianModelComponent.getInitialQuery( modelPath, connectionString, cubeName, null, session );
  }

  /**
   * @param modelPath
   * @param jndi
   * @param cubeName
   * @param roleName
   * @return mdx string that represents the initial query
   * @throws Throwable
   * @deprecated
   */
  @Deprecated
  public static String getInitialQuery( String modelPath, String jndi, final String cubeName, final String roleName,
                                        IPentahoSession session ) throws Throwable {

    Properties properties = new Properties();

    if ( modelPath.indexOf( "http" ) == 0 ) { //$NON-NLS-1$
      properties.put( RolapConnectionProperties.Catalog.name(), modelPath ); //$NON-NLS-1$
    } else {
      if ( !modelPath.startsWith( "solution:" ) && !modelPath.startsWith( "mondrian:" ) ) { //$NON-NLS-1$
        modelPath = "solution:" + modelPath; //$NON-NLS-1$
      }
      properties.put( RolapConnectionProperties.Catalog.name(), modelPath ); //$NON-NLS-1$
    }

    properties.put( RolapConnectionProperties.Provider.name(), "mondrian" ); //$NON-NLS-1$ //$NON-NLS-2$
    properties.put( RolapConnectionProperties.PoolNeeded.name(), "false" ); //$NON-NLS-1$ //$NON-NLS-2$
    properties.put( RolapConnectionProperties.DataSource.name(), jndi ); //$NON-NLS-1$

    if ( roleName != null ) {
      properties.put( RolapConnectionProperties.Role.name(), roleName ); //$NON-NLS-1$
    }
    return MondrianModelComponent.getInitialQuery( properties, cubeName, session );
  }

  public static String getInitialQuery( final Connection connection, final String cubeName ) throws Throwable {

    String measuresMdx = null;
    String columnsMdx = null;
    String whereMdx = ""; //$NON-NLS-1$
    StringBuffer rowsMdx = new StringBuffer();

    // Get catalog info, if exists
    String catalog = connection.getCatalogName();
    MondrianCatalogComplementInfo catalogComplementInfo =
      MondrianCatalogHelper.getInstance().getCatalogComplementInfoMap( catalog );

    try {

      Schema schema = connection.getSchema();
      if ( schema == null ) {
        Logger
          .error(
            "MondrianModelComponent", Messages.getInstance()
            .getErrorString( "MondrianModel.ERROR_0002_INVALID_SCHEMA",
              connection.getConnectString() ) ); //$NON-NLS-1$ //$NON-NLS-2$
        return null;
      }

      Cube[] cubes = schema.getCubes();
      if ( ( cubes == null ) || ( cubes.length == 0 ) ) {
        Logger
          .error(
            "MondrianModelComponent", Messages.getInstance()
            .getErrorString( "MondrianModel.ERROR_0003_NO_CUBES",
              connection.getConnectString() ) ); //$NON-NLS-1$ //$NON-NLS-2$
        return null;
      }

      if ( ( cubes.length > 1 ) && ( cubeName == null ) ) {
        Logger
          .error(
            "MondrianModelComponent", Messages.getInstance()
            .getErrorString( "MondrianModel.ERROR_0004_CUBE_NOT_SPECIFIED",
              connection.getConnectString() ) ); //$NON-NLS-1$ //$NON-NLS-2$
        return null;
      }

      Cube cube = null;
      if ( cubes.length == 1 ) {
        cube = cubes[ 0 ];
      } else {
        for ( Cube element : cubes ) {
          if ( element.getName().equals( cubeName ) ) {
            cube = element;
            break;
          }
        }
      }

      if ( cube == null ) {
        Logger
          .error(
            "MondrianModelComponent", Messages.getInstance()
            .getErrorString( "MondrianModel.ERROR_0005_CUBE_NOT_FOUND", cubeName,
              connection.getConnectString() ) ); //$NON-NLS-1$ //$NON-NLS-2$
        return null;
      }

      // If we have any whereConditions block, we need to find which hierarchies they are in
      // and not include them in the rows
      HashSet<Hierarchy> whereHierarchies = new HashSet<Hierarchy>();
      if ( catalogComplementInfo != null && catalogComplementInfo.getWhereCondition( cube.getName() ) != null
        && !catalogComplementInfo.getWhereCondition( cube.getName() ).equals( "" ) ) { //$NON-NLS-1$

        final String rawString = catalogComplementInfo.getWhereCondition( cube.getName() );

        // Caveat - It's possible that we have in the where condition a hierarchy that we don't have access
        // permissions; In this case, we'll ditch the where condition at all. Same for any error that
        // we find here

        try {

          // According to Julian, the better way to resolve the names is to build a query
          final String queryStr =
            "select " + rawString + " on columns, {} on rows from " + cube.getName(); //$NON-NLS-1$ //$NON-NLS-2$
          final Query query = connection.parseQuery( queryStr );

          final Hierarchy[] hierarchies = query.getMdxHierarchiesOnAxis( AxisOrdinal.StandardAxisOrdinal.COLUMNS );
          boolean isWhereValid = true;

          for ( int i = 0; i < hierarchies.length && isWhereValid; i++ ) {
            final Hierarchy hierarchy = hierarchies[ i ];
            if ( connection.getRole().canAccess( hierarchy ) ) {
              whereHierarchies.add( hierarchy );
            } else {
              isWhereValid = false;
              whereHierarchies.clear();
            }
          }

          if ( isWhereValid ) {
            whereMdx = " WHERE " + rawString; //$NON-NLS-1$
          }
        } catch ( Exception e ) {
          // We found an error in the where slicer, so we'll just act like it wasn't here
          whereHierarchies.clear();
        }
      }

      Dimension[] dimensions = cube.getDimensions();
      if ( ( dimensions == null ) || ( dimensions.length == 0 ) ) {
        Logger
          .error(
            "MondrianModelComponent", Messages.getInstance()
            .getErrorString( "MondrianModel.ERROR_0006_NO_DIMENSIONS", cubeName,
              connection.getConnectString() ) ); //$NON-NLS-1$ //$NON-NLS-2$
        return null;
      }

      for ( Dimension element : dimensions ) {

        final Hierarchy hierarchy = element.getHierarchy();
        if ( hierarchy == null ) {
          Logger
            .error(
              "MondrianModelComponent", Messages.getInstance()
              .getErrorString( "MondrianModel.ERROR_0007_NO_HIERARCHIES", element.getName(), cubeName,
                connection.getConnectString() ) ); //$NON-NLS-1$ //$NON-NLS-2$
          return null;
        }

        if ( !connection.getRole().canAccess( hierarchy ) ) {
          // We can't access this element
          continue;
        }

        if ( whereHierarchies.contains( hierarchy ) ) {
          // We have it on the where condition - skip it
          continue;
        }

        Member member =
          Locus.execute( (RolapConnection) connection, "Retrieving default members in plugin",
            new Locus.Action<Member>() {
              public Member execute() {
                return connection.getSchemaReader().getHierarchyDefaultMember( hierarchy );
              }
            } );

        if ( member == null ) {
          Logger
            .error(
              "MondrianModelComponent", Messages.getInstance()
              .getErrorString( "MondrianModel.ERROR_0008_NO_DEFAULT_MEMBER", element.getName(), cubeName,
                connection.getConnectString() ) ); //$NON-NLS-1$ //$NON-NLS-2$
          return null;
        }
        if ( element.isMeasures() ) {
          // measuresMdx = "with member "+ member.getUniqueName();
          // //$NON-NLS-1$
          measuresMdx = ""; //$NON-NLS-1$
          columnsMdx = " select NON EMPTY {" + member.getUniqueName() + "} ON columns, "; //$NON-NLS-1$ //$NON-NLS-2$
        } else {
          if ( rowsMdx.length() > 0 ) {
            rowsMdx.append( ", " ); //$NON-NLS-1$
          }
          rowsMdx.append( member.getUniqueName() );
        }
      }
      if ( ( measuresMdx != null ) && ( columnsMdx != null ) && ( rowsMdx.length() > 0 ) ) {
        StringBuffer result = new StringBuffer( measuresMdx.length() + columnsMdx.length() + rowsMdx.length() + 50 );
        result.append( measuresMdx ).append( columnsMdx ).append( "NON EMPTY {(" ) //$NON-NLS-1$
          .append( rowsMdx ).append( ")} ON rows " ) //$NON-NLS-1$
          .append( "from [" + cube.getName() + "]" ) //$NON-NLS-1$ //$NON-NLS-2$
          .append( whereMdx );

        return result.toString();

      }
      return null;
    } catch ( Throwable t ) {
      if ( t instanceof MondrianException ) {
        // pull the cause out, otherwise it never gets logged
        Throwable cause = ( (MondrianException) t ).getCause();
        if ( cause != null ) {
          throw cause;
        } else {
          throw t;
        }
      } else {
        throw t;
      }
    }
  }

  protected SQLConnection getConnection( final String jndiName, final String driver, final String userId,
                                         final String password, final String connectionInfo ) {
    SQLConnection connection = null;
    try {
      if ( jndiName != null ) {
        connection =
          (SQLConnection) PentahoConnectionFactory.getConnection( IPentahoConnection.SQL_DATASOURCE, jndiName,
            getSession(), this );
      }
      if ( connection == null ) {
        connection =
          (SQLConnection) PentahoConnectionFactory.getConnection( IPentahoConnection.SQL_DATASOURCE, driver,
            connectionInfo, userId, password, getSession(), this );
      }
      if ( connection == null ) {
        Logger
          .error(
            "MondrianModelComponent",
            Messages.getInstance().getErrorString( "SQLBaseComponent.ERROR_0005_INVALID_CONNECTION" ) );
        //$NON-NLS-1$ //$NON-NLS-2$
        return null;
      }
      return connection;
    } catch ( Exception e ) {
      Logger
        .error(
          "MondrianModelComponent",
          Messages.getInstance().getErrorString( "SQLBaseComponent.ERROR_0006_EXECUTE_FAILED", "" ),
          e ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    return null;
  }

}
