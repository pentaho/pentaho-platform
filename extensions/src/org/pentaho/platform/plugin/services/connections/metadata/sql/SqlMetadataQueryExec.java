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

package org.pentaho.platform.plugin.services.connections.metadata.sql;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.commons.connection.IPentahoMetaData;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.GenericDatabaseMeta;
import org.pentaho.metadata.model.SqlPhysicalModel;
import org.pentaho.metadata.query.BaseMetadataQueryExec;
import org.pentaho.metadata.query.impl.sql.MappedQuery;
import org.pentaho.metadata.query.impl.sql.SqlGenerator;
import org.pentaho.metadata.query.model.Parameter;
import org.pentaho.metadata.query.model.Query;
import org.pentaho.metadata.util.DatabaseMetaUtil;
import org.pentaho.metadata.util.ThinModelConverter;
import org.pentaho.platform.api.engine.IConfiguration;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISystemConfig;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.connection.PentahoConnectionFactory;
import org.pentaho.platform.plugin.services.connections.sql.SQLConnection;
import org.pentaho.platform.plugin.services.connections.sql.SQLResultSet;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.util.logging.SimpleLogger;
import org.pentaho.platform.util.messages.LocaleHelper;

public class SqlMetadataQueryExec extends BaseMetadataQueryExec {

  static final Log logger = LogFactory.getLog( SqlMetadataQueryExec.class );

  public static final String CONFIG_ID = "sqlmetadataqueryexec";

  public static final String FORCE_DB_META_CLASSES_PROP = "forceDbMetaClasses";

  protected final Set<String> driverClassesToForceMeta;

  // Must start out as null in order to allow injection points their turn at
  // specifying the implementing class.
  private String sqlGeneratorClass = null; //$NON-NLS-1$

  public SqlMetadataQueryExec() {
    this( PentahoSystem.get( ISystemConfig.class ) );
  }

  public SqlMetadataQueryExec( ISystemConfig systemConfig ) {
    String[] forceDbMetaClasses = new String[] {};
    if ( systemConfig != null ) {
      try {
        IConfiguration config = systemConfig.getConfiguration( CONFIG_ID );
        if ( config != null ) {
          Properties props = config.getProperties();
          if ( props != null ) {
            forceDbMetaClasses = props.getProperty( FORCE_DB_META_CLASSES_PROP, "" ).split( "," );
          }
        }
      } catch ( IOException e ) {
        logger.error( e );
      }
    }
    driverClassesToForceMeta = new HashSet<String>( forceDbMetaClasses.length );
    for ( String forceDbMetaClass : forceDbMetaClasses ) {
      if ( forceDbMetaClass != null ) {
        forceDbMetaClass = forceDbMetaClass.trim();
        if ( forceDbMetaClass.length() > 0 ) {
          driverClassesToForceMeta.add( forceDbMetaClass );
        }
      }
    }
  }

  public IPentahoResultSet executeQuery( Query queryObject ) {

    // need to get the correct DatabaseMeta
    SqlPhysicalModel sqlModel = (SqlPhysicalModel) queryObject.getLogicalModel().getPhysicalModel();
    DatabaseMeta databaseMeta = ThinModelConverter.convertToLegacy( sqlModel.getId(), sqlModel.getDatasource() );
    // this connection needs closed
    boolean closeConnection = true;

    DatabaseMeta activeDatabaseMeta = getActiveDatabaseMeta( databaseMeta );
    SQLConnection sqlConnection = getConnection( activeDatabaseMeta );
    String sql = null;
    try {
      if ( ( sqlConnection == null ) || !sqlConnection.initialized() ) {
        logger.error( Messages.getInstance().getErrorString( "SQLBaseComponent.ERROR_0007_NO_CONNECTION" ) ); //$NON-NLS-1$
        // TODO: throw an exception up the stack.
        return null;
      }

      // Make sure all parameters are of the correct type.
      // Fix for PDB-1753
      for ( Parameter param : queryObject.getParameters() ) {
        String pName = param.getName();
        if ( parameters.containsKey( pName ) && !parameters.get( pName ).getClass().isArray() ) {
          parameters.put( pName, this.convertParameterValue( param, parameters.get( pName ) ) );
        }
      }

      MappedQuery mappedQuery = null;
      try {
        SqlGenerator sqlGenerator = createSqlGenerator();
        mappedQuery =
            sqlGenerator.generateSql( queryObject, LocaleHelper.getLocale().toString(), getMetadataDomainRepository(),
              activeDatabaseMeta, parameters, true );
      } catch ( Exception e ) {
        throw new RuntimeException( e.getLocalizedMessage(), e );
      }

      Integer timeout = getTimeout();
      if ( timeout != null && timeout >= 0 ) {
        sqlConnection.setQueryTimeout( timeout );
      }

      Integer maxRows = getMaxRows();
      if ( maxRows != null && maxRows >= 0 ) {
        sqlConnection.setMaxRows( maxRows );
      }

      Boolean readOnly = isReadOnly();
      if ( readOnly != null && readOnly.booleanValue() ) {
        sqlConnection.setReadOnly( true );
      }

      IPentahoResultSet localResultSet = null;
      sql = mappedQuery.getQuery();
      if ( logger.isDebugEnabled() ) {
        logger.debug( "SQL: " + sql ); //$NON-NLS-1$
      }
      if ( getDoQueryLog() ) {
        logger.info( "SQL: " + sql ); //$NON-NLS-1$
      }

      // populate prepared sql params
      List<Object> sqlParams = null;
      if ( mappedQuery.getParamList() != null ) {
        sqlParams = new ArrayList<Object>();
        for ( String param : mappedQuery.getParamList() ) {
          Object sqlParam = parameters.get( param );
          // lets see if the parameter is a multi valued param
          if ( sqlParam instanceof Object[] ) {
            Object[] multivaluedParamValues = (Object[]) sqlParam;
            for ( Object p : multivaluedParamValues ) {
              sqlParams.add( p );
            }
          } else {
            sqlParams.add( sqlParam );
          }
        }
      }

      try {
        if ( !isForwardOnly() ) {
          if ( sqlParams != null ) {
            localResultSet = sqlConnection.prepareAndExecuteQuery( sql, sqlParams );
          } else {
            localResultSet = sqlConnection.executeQuery( sql );
          }
        } else {
          if ( sqlParams != null ) {
            localResultSet =
                sqlConnection.prepareAndExecuteQuery( sql, sqlParams, SQLConnection.RESULTSET_FORWARDONLY,
                    SQLConnection.CONCUR_READONLY );
          } else {
            localResultSet =
                sqlConnection.executeQuery( sql, SQLConnection.RESULTSET_FORWARDONLY, SQLConnection.CONCUR_READONLY );
          }
        }
        IPentahoMetaData metadata = mappedQuery.generateMetadata( localResultSet.getMetaData() );
        ( (SQLResultSet) localResultSet ).setMetaData( metadata );
        closeConnection = false;

      } catch ( Exception e ) {
        logger.error( Messages.getInstance().getErrorString(
          "SqlMetadataQueryExec.ERROR_0002_ERROR_EXECUTING_QUERY", e.getLocalizedMessage(), sql ) ); //$NON-NLS-1$
        logger.debug( "error", e ); //$NON-NLS-1$
        return null;
      }

      return localResultSet;
    } finally {
      if ( closeConnection && sqlConnection != null ) {
        sqlConnection.close();
      }
    }

  }

  public boolean isLive() {
    return true;
  }

  public boolean getForceDbDialect() {
    Object obj = inputs.get( "forcedbdialect" );
    if ( obj instanceof String && "true".equalsIgnoreCase( (String) obj ) ) {
      return true;
    }
    if ( obj instanceof Boolean && (Boolean) obj ) {
      return true;
    }
    return false;
  }

  protected DatabaseMeta getActiveDatabaseMeta( DatabaseMeta databaseMeta ) {
    if ( getForceDbDialect() || driverClassesToForceMeta.contains( databaseMeta.getDriverClass() ) ) {
      return databaseMeta;
    }

    // retrieve a temporary connection to determine if a dialect change is necessary
    // for generating the MQL Query.
    SQLConnection tempConnection = getConnection( databaseMeta );
    try {

      // if the connection type is not of the current dialect, regenerate the query
      DatabaseInterface di = getDatabaseInterface( tempConnection );

      if ( ( di != null ) && ( !databaseMeta.getPluginId().equals( di.getPluginId() ) ) ) {
        // we need to reinitialize our mqlQuery object and reset the query.
        // note that using this di object wipes out connection info
        DatabaseMeta meta = (DatabaseMeta) databaseMeta.clone();
        DatabaseInterface di2 = (DatabaseInterface) di.clone();
        di2.setAccessType( databaseMeta.getAccessType() );
        di2.setDatabaseName( databaseMeta.getDatabaseName() );
        di2.setAttributes( databaseMeta.getAttributes() );
        di2.setUsername( databaseMeta.getUsername() );
        di2.setPassword( databaseMeta.getPassword() );
        di2.setHostname( databaseMeta.getHostname() );
        meta.setDatabaseInterface( di2 );
        return meta;
      } else {
        return databaseMeta;
      }
    } finally {
      if ( tempConnection != null ) {
        tempConnection.close();
      }
    }

  }

  protected SQLConnection getConnection( DatabaseMeta databaseMeta ) {
    // use the connection specified in the query
    SQLConnection localConnection = null;
    try {
      IPentahoSession session = PentahoSessionHolder.getSession();
      if ( databaseMeta.getAccessType() == DatabaseMeta.TYPE_ACCESS_JNDI ) {
        String jndiName = databaseMeta.getDatabaseName();
        if ( jndiName != null ) {
          SimpleLogger simpleLogger = new SimpleLogger( this );
          localConnection =
              (SQLConnection) PentahoConnectionFactory.getConnection( IPentahoConnection.SQL_DATASOURCE, jndiName,
                  session, simpleLogger );
        }
      }
      if ( localConnection == null ) {
        String driver = databaseMeta.getDriverClass();
        String userId = databaseMeta.getUsername();
        String password = databaseMeta.getPassword();
        String connectionInfo = databaseMeta.getURL();

        // Fix for BISERVER-6350
        // Creating connections in PEC generate GenericDatabaseMeta objects that lack the DatabaseName (since
        // GenericDatabaseMeta use a "custom URL" instead).
        // Later on when the db dialect of the database meta gets changed (this.getActiveDatabaseMeta()) to other than
        // the "Generic" the
        // DatabaseName is still missing which produces a bougus url connection throwing exceptions.

        if ( StringUtils.isEmpty( databaseMeta.getDatabaseName() ) ) {
          String genericDBMetaDriver =
              databaseMeta.getAttributes().getProperty( GenericDatabaseMeta.ATRRIBUTE_CUSTOM_DRIVER_CLASS, "" );
          if ( !StringUtils.isEmpty( genericDBMetaDriver ) ) {
            driver = genericDBMetaDriver;
          }
          String genericDBMetaURL =
              databaseMeta.getAttributes().getProperty( GenericDatabaseMeta.ATRRIBUTE_CUSTOM_URL, "" );
          if ( !StringUtils.isEmpty( genericDBMetaURL ) ) {
            connectionInfo = genericDBMetaURL;
          }
        }

        SimpleLogger simpleLogger = new SimpleLogger( this );
        localConnection =
            (SQLConnection) PentahoConnectionFactory.getConnection( IPentahoConnection.SQL_DATASOURCE, driver,
                connectionInfo, userId, password, session, simpleLogger );
      }

      // This no longer is functional, it used to work with the old MQLRelationalDataComponent
      // try the parent to allow the connection to be overridden
      // localConnection = getConnection(localConnection);
      return localConnection;
    } catch ( Exception e ) {
      logger.error( Messages.getInstance().getErrorString( "MetadataQueryComponent.ERROR_0006_EXECUTE_FAILED" ), e ); //$NON-NLS-1$
    }
    return null;
  }

  /**
   * There are 3 levels at which a SqlGenerator class can be found:
   * The default class is specified in this component:
   * org.pentaho.metadata.query.impl.sql.SqlGenerator; if no other 
   * overrides are in play, the default is used. 
   * If a SqlGenerator class is set using the setter (through an
   * input in the action sequence or programmatically), then the
   * default is overridden by the class set in the setter. In between, we check the pentahoSpring.objects.xml,
   * and if there is a SqlGenerator specified in there, we use that SqlGenerator, overriding
   * the default. The setter always overrides the pentahoSpring.objects.xml class.   
   */
  private SqlGenerator createSqlGenerator() throws Exception {

    SqlGenerator sqlGenerator = null;

    String inputClass = (String) inputs.get( "sqlgenerator" );
    if ( inputClass != null ) {
      sqlGeneratorClass = inputClass;
    }

    if ( sqlGeneratorClass == null ) {
      sqlGenerator = PentahoSystem.get( SqlGenerator.class, "sqlGenerator", null );
      if ( sqlGenerator == null ) {
        sqlGeneratorClass = "org.pentaho.metadata.query.impl.sql.SqlGenerator"; //$NON-NLS-1$
      }
    }
    if ( sqlGeneratorClass != null ) {
      Class<?> clazz = Class.forName( sqlGeneratorClass );
      sqlGenerator = (SqlGenerator) clazz.getConstructor( new Class[] {} ).newInstance( new Object[] {} );
    }
    return sqlGenerator;

  }

  protected DatabaseInterface getDatabaseInterface( final SQLConnection conn ) {
    String prod = null;
    try {
      prod = conn.getNativeConnection().getMetaData().getDatabaseProductName();
      DatabaseInterface di = DatabaseMetaUtil.getDatabaseInterface( prod );
      if ( prod != null && di == null ) {
        logger.warn( Messages.getInstance()
            .getString( "MQLRelationalDataComponent.WARN_0001_NO_DIALECT_DETECTED", prod ) ); //$NON-NLS-1$
      }
      return di;
    } catch ( SQLException e ) {
      logger.warn(
          Messages.getInstance().getString( "MQLRelationalDataComponent.WARN_0002_DIALECT_EXCEPTION", prod ), e ); //$NON-NLS-1$
    }
    return null;
  }

}
