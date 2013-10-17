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

package org.pentaho.platform.plugin.action.mdx;

import mondrian.olap.Util;
import mondrian.rolap.RolapConnectionProperties;
import mondrian.util.Pair;
import org.apache.commons.logging.Log;
import org.pentaho.actionsequence.dom.ActionInputConstant;
import org.pentaho.actionsequence.dom.IActionOutput;
import org.pentaho.actionsequence.dom.actions.MdxConnectionAction;
import org.pentaho.actionsequence.dom.actions.MdxQueryAction;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.api.data.IDataComponent;
import org.pentaho.platform.api.data.IPreparedComponent;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.connection.PentahoConnectionFactory;
import org.pentaho.platform.engine.services.runtime.MapParameterResolver;
import org.pentaho.platform.engine.services.runtime.TemplateUtil;
import org.pentaho.platform.engine.services.solution.ComponentBase;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.services.connections.mondrian.MDXConnection;
import org.pentaho.platform.plugin.services.connections.mondrian.MDXResultSet;
import org.pentaho.platform.util.messages.LocaleHelper;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

public abstract class MDXBaseComponent extends ComponentBase implements IDataComponent, IPreparedComponent {

  private static final long serialVersionUID = 495868243986115468L;

  public static final String FORMATTED_CELL_VALUES = "formattedCellValues"; //$NON-NLS-1$

  private IPentahoResultSet rSet;

  /** is set to false if using another IPreparedComponents connection vs own */
  private boolean connectionOwner = true;

  /** keep a reference to the connection for prepared component functionality */
  private IPentahoConnection connection;

  /** stores the prepared query for later use */
  String preparedQuery = null;

  @Override
  public abstract boolean validateSystemSettings();

  @Override
  public abstract Log getLogger();

  public IPentahoResultSet getResultSet() {
    return rSet;
  }

  @Override
  protected boolean validateAction() {
    boolean actionValidated = true;
    MdxQueryAction queryAction = null;
    MdxConnectionAction connAction = null;

    try {
      if ( getActionDefinition() instanceof MdxQueryAction ) {
        queryAction = (MdxQueryAction) getActionDefinition();
        actionValidated = isConnectionInfoSpecified( queryAction );

        if ( actionValidated ) {
          if ( queryAction.getQuery() == ActionInputConstant.NULL_INPUT ) {
            error( Messages.getInstance().getErrorString(
              "MDXBaseComponent.ERROR_0001_QUERY_NOT_SPECIFIED", getActionName() ) ); //$NON-NLS-1$
            actionValidated = false;
          }
        }

        if ( actionValidated ) {
          if ( ( queryAction.getOutputResultSet() == null ) && ( queryAction.getOutputPreparedStatement() == null ) ) {
            error( Messages.getInstance().getErrorString(
              "MDXBaseComponent.ERROR_0003_OUTPUT_NOT_SPECIFIED", getActionName() ) ); //$NON-NLS-1$
            actionValidated = false;
          }
        }
      } else if ( getActionDefinition() instanceof MdxConnectionAction ) {
        connAction = (MdxConnectionAction) getActionDefinition();
        actionValidated = isConnectionInfoSpecified( connAction );
        if ( connAction.getOutputConnection() == null ) {
          error( Messages.getInstance().getErrorString(
            "MDXBaseComponent.ERROR_0003_OUTPUT_NOT_SPECIFIED", getActionName() ) ); //$NON-NLS-1$
          actionValidated = false;
        }

      }
    } catch ( Exception e ) {
      actionValidated = false;
      error(
          Messages.getInstance().getErrorString( "MDXBaseComponent.ERROR_0004_VALIDATION_FAILED", getActionName() ), e ); //$NON-NLS-1$        
    }

    return actionValidated;
  }

  /*
   * 
   */
  private boolean isConnectionInfoSpecified( final MdxConnectionAction connAction ) {
    boolean value = true;

    if ( connAction instanceof MdxQueryAction ) {
      if ( ( connAction.getConnection() == ActionInputConstant.NULL_INPUT )
          && ( connAction.getMdxConnectionString() == null )
          && ( connAction.getJndi() == ActionInputConstant.NULL_INPUT )
          && ( connAction.getConnectionProps() == ActionInputConstant.NULL_INPUT )
          && ( ( (MdxQueryAction) connAction ).getMdxConnection() == ActionInputConstant.NULL_INPUT ) ) {
        error( Messages.getInstance().getErrorString(
          "MDXBaseComponent.ERROR_0002_CONNECTION_NOT_SPECIFIED", getActionName() ) ); //$NON-NLS-1$
        value = false;
      }
    } else if ( connAction instanceof MdxConnectionAction ) {
      if ( ( connAction.getConnection() == ActionInputConstant.NULL_INPUT )
          && ( connAction.getMdxConnectionString() == null )
          && ( connAction.getJndi() == ActionInputConstant.NULL_INPUT )
          && ( connAction.getConnectionProps() == ActionInputConstant.NULL_INPUT ) ) {
        error( Messages.getInstance().getErrorString(
          "MDXBaseComponent.ERROR_0002_CONNECTION_NOT_SPECIFIED", getActionName() ) ); //$NON-NLS-1$
        value = false;
      }
    }

    return value;
  }

  @Override
  public void done() {
  }

  @Override
  protected boolean executeAction() {
    boolean value = false;
    /*
     * This is the query part. You would need a connection to execute the query. The connection will either come in as
     * an INPUT (prepared_component) or will be specified right there.
     * 
     * So check if a prepared component exists, if not create a new connection. If connection is not null, proceed to
     * work on the query part.
     * 
     * In the query section you can either execute the query right away or prepare it to be used later by a sub report.
     */
    try {
      if ( getActionDefinition() instanceof MdxQueryAction ) {
        MdxQueryAction queryAction = (MdxQueryAction) getActionDefinition();
        // if there is a prepared component specified as an input, use its connection
        // instead of creating our own.
        if ( queryAction.getMdxConnection() != ActionInputConstant.NULL_INPUT ) {
          if ( queryAction.getMdxConnection().getValue() != null ) {
            connectionOwner = false;
            IPreparedComponent component = (IPreparedComponent) queryAction.getMdxConnection().getValue();
            IPentahoConnection conn = component.shareConnection();
            if ( conn.getDatasourceType() == IPentahoConnection.MDX_DATASOURCE ) {
              connection = conn;
            } else {
              error( Messages.getInstance().getErrorString(
                  "IPreparedComponent.ERROR_0001_INVALID_CONNECTION_TYPE", getActionName() ) ); //$NON-NLS-1$            
            }
          } else {
            error( Messages.getInstance().getErrorString(
                "IPreparedComponent.ERROR_0002_CONNECTION_NOT_AVAILABLE", getActionName() ) ); //$NON-NLS-1$
          }
        } else {
          dispose();
          connection = getDatasourceConnection();
        }

        if ( connection != null ) {
          String query = queryAction.getQuery().getStringValue();
          if ( queryAction.getOutputPreparedStatement() != null ) {
            // prepare the query for execution, but don't execute quite yet.
            prepareQuery( query );
            // set the output as self, which will be used later by another component.
            setOutputValue( IPreparedComponent.PREPARED_COMPONENT_NAME, this );
            value = true;
          } else {
            value = runQuery( connection, query );
          }
        } else {
          error( Messages.getInstance().getErrorString(
            "IPreparedComponent.ERROR_0004_NO_CONNECTION_INFO", getActionName() ) ); //$NON-NLS-1$
        }
      } else if ( getActionDefinition() instanceof MdxConnectionAction ) {
        dispose();
        connection = getDatasourceConnection();

        if ( connection != null ) {
          setOutputValue( IPreparedComponent.PREPARED_COMPONENT_NAME, this );
          value = true;
        }
      } else {
        error( Messages.getInstance().getErrorString( "MDXBaseComponent.ERROR_0004_VALIDATION_FAILED",
          getActionName() ) ); //$NON-NLS-1$
      }
    } catch ( Exception e ) {
      error( Messages.getInstance().getErrorString( "MDXBaseComponent.ERROR_0006_EXECUTE_FAILED", getActionName() ), e ); //$NON-NLS-1$
    }

    return value;
  }

  /**
   * called when in prepared-component mode, this method populates the preparedQuery string and preparedParameters
   * object.
   * 
   * @param rawQuery
   * @return
   */
  protected boolean prepareQuery( final String rawQuery ) {

    try {
      if ( connection == null ) {
        error( Messages.getInstance().getErrorString( "MDXBaseComponent.ERROR_0008_NO_CONNECTION" ) ); //$NON-NLS-1$
        return false;
      }
      if ( !connection.initialized() ) {
        error( Messages.getInstance().getErrorString( "MDXBaseComponent.ERROR_0008_NO_CONNECTION" ) ); //$NON-NLS-1$
        return false;
      }

      if ( rawQuery != null ) {
        preparedQuery = applyInputsToFormat( rawQuery );
      }

      return true;
    } catch ( Exception e ) {
      error( Messages.getInstance().getErrorString( "MDXBaseComponent.ERROR_0006_EXECUTE_FAILED", getActionName() ), e ); //$NON-NLS-1$
    }

    return false;
  }

  /**
   * if the owner, dispose of the connection
   */
  public void dispose() {
    if ( connectionOwner ) {
      if ( connection != null ) {
        connection.close();
      }
    }
    connection = null;
  }

  /**
   * return this class's connection. This implements the IPreparedComponent interface, which may share its connection
   * with others.
   * 
   * @return connection object
   */
  public IPentahoConnection shareConnection() {
    return connection;
  }

  /**
   * executes a prepared method that returns a result set executePrepared looks up any "PREPARELATER" params in the
   * preparedParams map.
   * 
   * @param preparedParams
   *          a map of possible parameters.
   * @return result set
   */
  public IPentahoResultSet executePrepared( final Map preparedParams ) {
    try {
      if ( connection == null ) {
        error( Messages.getInstance().getErrorString( "MDXBaseComponent.ERROR_0008_NO_CONNECTION" ) ); //$NON-NLS-1$
        return null;
      }
      if ( !connection.initialized() ) {
        error( Messages.getInstance().getErrorString( "MDXBaseComponent.ERROR_0008_NO_CONNECTION" ) ); //$NON-NLS-1$
        return null;
      }
      if ( preparedQuery == null ) {
        error( Messages.getInstance().getErrorString(
          "MDXBaseComponent.ERROR_0001_QUERY_NOT_SPECIFIED", getActionName() ) ); //$NON-NLS-1$
        return null;
      }

      // parse preparedQuery, replacing any {PREPARELATER:NAME} with appropriate values
      String query =
          TemplateUtil.applyTemplate( preparedQuery, getRuntimeContext(), new MapParameterResolver( preparedParams,
              IPreparedComponent.PREPARE_LATER_PREFIX, getRuntimeContext() ) );

      if ( ComponentBase.debug ) {
        debug( Messages.getInstance().getString( "MDXBaseComponent.DEBUG_RUNNING_QUERY", query ) ); //$NON-NLS-1$
      }

      // evaluate
      IPentahoResultSet resultSet = connection.executeQuery( query );
      rSet = resultSet;
      return resultSet;
    } catch ( Exception e ) {
      error( Messages.getInstance().getErrorString( "MDXBaseComponent.ERROR_0006_EXECUTE_FAILED", getActionName() ), e ); //$NON-NLS-1$
    }
    return null;
  }

  protected boolean runQuery( final IPentahoConnection localConnection, final String rawQuery ) {

    try {
      if ( localConnection == null ) {
        error( Messages.getInstance().getErrorString( "MDXBaseComponent.ERROR_0008_NO_CONNECTION" ) ); //$NON-NLS-1$
        return false;
      }
      if ( !localConnection.initialized() ) {
        error( Messages.getInstance().getErrorString( "MDXBaseComponent.ERROR_0008_NO_CONNECTION" ) ); //$NON-NLS-1$
        return false;
      }
      if ( rawQuery == null ) {
        error( Messages.getInstance().getErrorString(
          "MDXBaseComponent.ERROR_0001_QUERY_NOT_SPECIFIED", getActionName() ) ); //$NON-NLS-1$
        return false;
      }

      if ( ComponentBase.debug ) {
        debug( Messages.getInstance().getString( "MDXBaseComponent.DEBUG_RUNNING_QUERY", rawQuery ) ); //$NON-NLS-1$
      }

      // execute the query, read the results and cache them
      IPentahoResultSet resultSet = localConnection.executeQuery( rawQuery );
      if ( resultSet != null && resultSet instanceof MDXResultSet ) {
        // BISERVER-3543 - set the result set to return formatted cell values
        boolean formattedCellValues = false;
        if ( isDefinedInput( FORMATTED_CELL_VALUES ) ) {
          formattedCellValues = getInputBooleanValue( FORMATTED_CELL_VALUES, false );
        }
        ( (MDXResultSet) resultSet ).setFormattedCellValues( formattedCellValues );
      }
      rSet = resultSet;
      if ( resultSet != null ) {
        MdxQueryAction mdxQueryAction = (MdxQueryAction) getActionDefinition();
        IActionOutput actionOutput = mdxQueryAction.getOutputResultSet();
        if ( actionOutput != null ) {
          actionOutput.setValue( resultSet );
        }
        return true;
      } else {
        // close the connection
        error( Messages.getInstance().getErrorString( "MDXBaseComponent.ERROR_0006_EXECUTE_FAILED",
          getActionName() ) ); //$NON-NLS-1$
        localConnection.close();
        return false;
      }

    } catch ( Exception e ) {
      error( Messages.getInstance().getErrorString( "MDXBaseComponent.ERROR_0006_EXECUTE_FAILED", getActionName() ), e ); //$NON-NLS-1$
    }

    return false;
  }

  /**
   * attempt to aquire a connection. if connection isn't available, wait a certain period of time before trying again.
   * 
   * @return connection
   */
  public IPentahoConnection getDatasourceConnection() {
    IPentahoConnection con;
    int[] timeouts = { 200, 500, 2000 };
    for ( int element : timeouts ) {
      try {
        con = getConnection();
        try {
          con.clearWarnings();
        } catch ( Exception ex ) {
          //ignored
        }
        return con;
      } catch ( Exception ex ) {
        //ignored
      }
      waitFor( element );
    }
    con = getConnection();
    try {
      con.clearWarnings();
    } catch ( Exception ex ) {
      //ignore
    }
    return con;
  }

  protected void waitFor( final int millis ) {
    try {
      if ( ComponentBase.debug ) {
        debug( Messages.getInstance().getString(
          "MDXBaseComponent.DEBUG_WAITING_FOR_CONNECTION", Integer.toString( millis ) ) ); //$NON-NLS-1$
      }
      Thread.sleep( millis );
    } catch ( Exception ex ) {
      // ignore the interrupted exception, if it happens
    }
  }

  protected IPentahoConnection getConnection() {

    // first attempt to get the connection metadata from the catalog service. if that is not successful,
    // get the connection using the original approach.

    MdxConnectionAction connAction = (MdxConnectionAction) getActionDefinition();
    String catalogName = connAction.getCatalog().getStringValue();
    IMondrianCatalogService mondrianCatalogService =
        PentahoSystem.get( IMondrianCatalogService.class, "IMondrianCatalogService", PentahoSessionHolder.getSession() ); //$NON-NLS-1$
    MondrianCatalog catalog = mondrianCatalogService.getCatalog( catalogName, PentahoSessionHolder.getSession() );

    if ( catalog == null ) {
      return getConnectionOrig();
    }

    Util.PropertyList connectProperties = Util.parseConnectString( catalog.getDataSourceInfo() );

    Properties properties = new Properties();

    Iterator<Pair<String, String>> iter = connectProperties.iterator();
    while ( iter.hasNext() ) {
      Pair<String, String> pair = iter.next();
      properties.put( pair.getKey(), pair.getValue() );
    }

    properties.put( "Catalog", catalog.getDefinition() );
    properties.put( "Provider", "mondrian" );
    properties.put( "PoolNeeded", "false" );
    properties.put( RolapConnectionProperties.Locale.name(), LocaleHelper.getLocale().toString() );

    debug( "Mondrian Connection Properties: " + properties.toString() );

    MDXConnection mdxConnection =
        (MDXConnection) PentahoConnectionFactory.getConnection( IPentahoConnection.MDX_DATASOURCE, properties,
          PentahoSessionHolder.getSession(), this );

    if ( connAction != null ) {
      if ( ( connAction.getExtendedColumnNames() != ActionInputConstant.NULL_INPUT ) ) {
        mdxConnection.setUseExtendedColumnNames( connAction.getExtendedColumnNames().getBooleanValue() );
      }
    }

    return mdxConnection;
  }

  protected IPentahoConnection getConnectionOrig() {
    IPentahoConnection localConnection = null;
    MdxConnectionAction connAction = (MdxConnectionAction) getActionDefinition();
    try {
      String mdxConnectionStr = connAction.getMdxConnectionString().getStringValue();
      Properties mdxConnectionProps = (Properties) connAction.getConnectionProps().getValue();
      String jdbcStr = connAction.getConnection().getStringValue();
      String jndiStr = connAction.getJndi().getStringValue();
      String location = connAction.getLocation().getStringValue();
      String role = connAction.getRole().getStringValue();
      String catalog = connAction.getCatalog().getStringValue();

      if ( ( catalog == null ) && ( connAction.getCatalogResource() != null ) ) {
        IActionSequenceResource resource = getResource( connAction.getCatalogResource().getName() );
        catalog = resource.getAddress();
        if ( ( resource.getSourceType() == IActionSequenceResource.URL_RESOURCE )
            && ( catalog.indexOf( "solution:" ) != 0 ) ) { //$NON-NLS-1$
          // Extra step to make sure that remote mondrian models
          // fully qualified aren't munged
          // MB
          if ( !catalog.startsWith( "http:" ) ) { //$NON-NLS-1$ 
            catalog = "solution:" + catalog; //$NON-NLS-1$
          }
        } else if ( ( resource.getSourceType() == IActionSequenceResource.SOLUTION_FILE_RESOURCE )
            || ( resource.getSourceType() == IActionSequenceResource.FILE_RESOURCE ) ) {
          if ( !catalog.startsWith( "solution:" ) ) {
            catalog = "solution:" + catalog; //$NON-NLS-1$
          }
        }
      }
      if ( catalog == null ) {
        warn( Messages.getInstance().getString( "MDXBaseComponent.ERROR_0007_CATALOG_NOT_DEFINED", getActionName() ) ); //$NON-NLS-1$
      } else {
        if ( mdxConnectionProps != null ) {
          mdxConnectionProps.put( MdxConnectionAction.CATALOG_ELEMENT, catalog );
        }
      }

      String userId = connAction.getUserId().getStringValue();
      String password = connAction.getPassword().getStringValue();
      if ( mdxConnectionProps != null ) {
        localConnection =
            PentahoConnectionFactory.getConnection( IPentahoConnection.MDX_DATASOURCE, mdxConnectionProps,
                getSession(), this );
      } else {
        if ( mdxConnectionStr != null ) {
          localConnection =
              PentahoConnectionFactory.getConnection( IPentahoConnection.MDX_DATASOURCE, mdxConnectionStr,
                  getSession(), this );
        } else {
          String connectStr = null;
          if ( jdbcStr != null ) {
            connectStr = jdbcStr + "; Catalog=" + catalog; //$NON-NLS-1$
          } else if ( jndiStr != null ) {

            IDBDatasourceService datasourceService =
                PentahoSystem.getObjectFactory().get( IDBDatasourceService.class, null );
            if ( datasourceService.getDataSource( jndiStr ) == null ) {
              error( Messages.getInstance().getErrorString( "MDXBaseComponent.ERROR_0005_INVALID_CONNECTION" ) ); //$NON-NLS-1$
              return null;
            }

            connectStr = "dataSource=" + jndiStr + "; Catalog=" + catalog; //$NON-NLS-1$ //$NON-NLS-2$
            // Add extra definitions from platform mondrian metadata
            MondrianCatalog mc =
                org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogHelper.getInstance().getCatalog(
                  catalog, getSession() );
            try {
              connectStr += ";" + mc.getDataSourceInfo();
            } catch ( Exception e ) {
              // Just swallow the exception
            }
          }
          if ( role != null ) {
            connectStr += "; Role=" + role; //$NON-NLS-1$
          }
          Properties props = new Properties();
          props.setProperty( IPentahoConnection.CONNECTION, connectStr );
          props.setProperty( IPentahoConnection.PROVIDER, location );
          if ( userId != null ) {
            props.setProperty( IPentahoConnection.USERNAME_KEY, userId );
          }
          if ( password != null ) {
            props.setProperty( IPentahoConnection.PASSWORD_KEY, password );
          }

          localConnection =
              PentahoConnectionFactory.getConnection( IPentahoConnection.MDX_DATASOURCE, props, getSession(), this );
        }
        if ( localConnection == null ) {
          error( Messages.getInstance().getErrorString( "MDXBaseComponent.ERROR_0005_INVALID_CONNECTION" ) ); //$NON-NLS-1$
          return null;
        }
      }

      if ( localConnection instanceof MDXConnection ) {
        MDXConnection mdxConn = (MDXConnection) localConnection;
        if ( connAction != null ) {
          if ( ( connAction.getExtendedColumnNames() != ActionInputConstant.NULL_INPUT ) ) {
            mdxConn.setUseExtendedColumnNames( connAction.getExtendedColumnNames().getBooleanValue() );
          }
        }
      }
      return localConnection;
    } catch ( Exception e ) {
      error( Messages.getInstance().getErrorString( "MDXBaseComponent.ERROR_0006_EXECUTE_FAILED", getActionName() ), e ); //$NON-NLS-1$
    }
    return null;
  }

  @Override
  public boolean init() {
    return true;
  }
}
