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

package org.pentaho.platform.plugin.action.hql;

import org.apache.commons.logging.Log;
import org.pentaho.actionsequence.dom.ActionInputConstant;
import org.pentaho.actionsequence.dom.IActionOutput;
import org.pentaho.actionsequence.dom.IActionResource;
import org.pentaho.actionsequence.dom.actions.HQLConnectionAction;
import org.pentaho.actionsequence.dom.actions.HQLQueryAction;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.data.IPreparedComponent;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.engine.services.connection.PentahoConnectionFactory;
import org.pentaho.platform.engine.services.runtime.MapParameterResolver;
import org.pentaho.platform.engine.services.runtime.TemplateUtil;
import org.pentaho.platform.engine.services.solution.ComponentBase;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.plugin.services.connections.hql.HQLConnection;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;

public abstract class HQLBaseComponent extends ComponentBase implements IPreparedComponent {

  private static final long serialVersionUID = 5949258346877934670L;

  private IPentahoResultSet rSet;

  /** reference to connection object */
  private IPentahoConnection connection;

  /** specifies whether component owns connection or not */
  private boolean connectionOwner = true;

  /** holds the query string for ipreparedcomponent functionality */
  private String preparedQuery = null;

  @Override
  public abstract boolean validateSystemSettings();

  public abstract String getResultOutputName();

  @Override
  public abstract Log getLogger();

  public IPentahoResultSet getResultSet() {
    return rSet;
  }

  @Override
  protected boolean validateAction() {
    HQLConnectionAction connAction = null;
    HQLQueryAction queryAction = null;
    boolean actionValidated = true;

    try {
      if ( getActionDefinition() instanceof HQLQueryAction ) {
        queryAction = (HQLQueryAction) getActionDefinition();

        actionValidated = isConnectionInfoSpecified( queryAction );

        // Check if the query is defined.
        if ( actionValidated && ( queryAction.getQuery() == ActionInputConstant.NULL_INPUT ) ) {
          actionValidated = false;
          error( Messages.getInstance().getErrorString(
            "HQLBaseComponent.ERROR_0004_QUERY_NOT_SPECIFIED", getActionName() ) ); //$NON-NLS-1$
        }

        // Check if output for the query is correctly defined.
        if ( actionValidated && ( queryAction.getOutputResultSetName() == null )
            && ( queryAction.getOutputPreparedStatementName() == null ) ) {
          actionValidated = false;
          error( Messages.getInstance().getErrorString(
            "HQLBaseComponent.ERROR_0005_OUTPUT_NOT_SPECIFIED", getActionName() ) ); //$NON-NLS-1$
        }
      } else if ( getActionDefinition() instanceof HQLConnectionAction ) {
        connAction = (HQLConnectionAction) getActionDefinition();
        actionValidated = isConnectionInfoSpecified( connAction );
      } else {
        actionValidated = false;
        error( Messages.getInstance().getErrorString(
            "ComponentBase.ERROR_0001_UNKNOWN_ACTION_TYPE", getActionDefinition().getElement().asXML() ) ); //$NON-NLS-1$      
      }
    } catch ( Exception e ) {
      actionValidated = false;
      error(
          Messages.getInstance().getErrorString( "HQLBaseComponent.ERROR_0006_VALIDATION_FAILED", getActionName() ), e ); //$NON-NLS-1$     
    }

    return actionValidated;
  }

  private boolean isConnectionInfoSpecified( final HQLConnectionAction connAction ) {
    boolean value = true;

    if ( connAction instanceof HQLQueryAction ) {
      if ( ( ( (HQLQueryAction) connAction ).getInputSharedConnection() == ActionInputConstant.NULL_INPUT )
          && !isBasicConnectionInfoSpecified( connAction ) ) {
        value = false;
        error( Messages.getInstance().getErrorString(
            "HQLBaseComponent.ERROR_0003_CONNECTION_INFO_NOT_SPECIFIED", getActionName() ) ); //$NON-NLS-1$        
      }
    } else {
      if ( !isBasicConnectionInfoSpecified( connAction ) ) {
        value = false;
        error( Messages.getInstance().getErrorString(
            "HQLBaseComponent.ERROR_0003_CONNECTION_INFO_NOT_SPECIFIED", getActionName() ) ); //$NON-NLS-1$        
      }
    }
    return value;
  }

  private boolean isBasicConnectionInfoSpecified( final HQLConnectionAction connAction ) {
    boolean value = true;

    if ( connAction.getClassNames() == ActionInputConstant.NULL_INPUT ) {
      value = false;
      error( Messages.getInstance().getErrorString(
          "HQLBaseComponent.ERROR_0001_CLASS_NAMES_INFO_NOT_SPECIFIED", getActionName() ) ); //$NON-NLS-1$  
    }
    if ( connAction.getHibernateConfigResource() == null ) {
      value = false;
      error( Messages.getInstance().getErrorString(
          "HQLBaseComponent.ERROR_0002_HIBERNATE_CONFIG_INFO_NOT_SPECIFIED", getActionName() ) ); //$NON-NLS-1$        
    }

    return value;
  }

  @Override
  public void done() {
    // TODO Auto-generated method stub
  }

  @Override
  protected boolean executeAction() {
    boolean returnValue = true;

    try {
      if ( getActionDefinition() instanceof HQLQueryAction ) {
        HQLQueryAction queryAction = (HQLQueryAction) getActionDefinition();
        String[] classNames = null;
        String query = queryAction.getQuery().getStringValue();

        if ( queryAction.getInputSharedConnection() != ActionInputConstant.NULL_INPUT ) {
          connectionOwner = false;
          IPreparedComponent component = (IPreparedComponent) queryAction.getInputSharedConnection().getValue();
          IPentahoConnection conn = component.shareConnection();
          if ( IPentahoConnection.HQL_DATASOURCE.equals( conn.getDatasourceType() ) ) {
            connection = conn;
          } else {
            connection = null;
            returnValue = false;
            error( Messages.getInstance().getErrorString(
                "IPreparedComponent.ERROR_0001_INVALID_CONNECTION_TYPE", getActionName() ) ); //$NON-NLS-1$            
          }
        } else {
          createBasicConnection( queryAction, classNames );
        }

        if ( connection != null ) {
          IActionOutput actionOutput = queryAction.getOutputPreparedStatementParam();
          if ( actionOutput != null ) {
            // prepare the query for execution, but don't execute quite yet.
            prepareQuery( query );

            // set the output as self, which will be used later by another component.
            actionOutput.setValue( this );
          } else {
            return runQuery( connection, classNames, query );
          }
        }
      } else if ( getActionDefinition() instanceof HQLConnectionAction ) {
        HQLConnectionAction connAction = (HQLConnectionAction) getActionDefinition();
        String[] classNames = null;
        createBasicConnection( connAction, classNames );
        if ( connection != null ) {
          IActionOutput outputConnection = connAction.getOutputConnectionParam();
          if ( outputConnection != null ) {
            outputConnection.setValue( this );
          }
        }
      } else {
        returnValue = false;
        error( Messages.getInstance().getErrorString(
          "HQLBaseComponent.ERROR_00011_INVALID_HQL_COMPONENT", getActionName() ) ); //$NON-NLS-1$
      }
    } catch ( Exception e ) {
      returnValue = false;
      error( Messages.getInstance().getErrorString( "HQLBaseComponent.ERROR_00012_EXECUTE_FAILED", getActionName() ), e ); //$NON-NLS-1$
    }
    return returnValue;
  }

  /*
   * Create the basic connection. This requires retrieving class names and catalog info.
   */
  private void createBasicConnection( final HQLConnectionAction connAction, String[] classNames ) {
    boolean proceed = true;
    String catalog = null;

    if ( connAction.getClassNames() != ActionInputConstant.NULL_INPUT ) {
      classNames = getClassNames( connAction );
    } else {
      proceed = false;
      error( Messages.getInstance().getErrorString(
          "HQLBaseComponent.ERROR_0001_CLASS_NAMES_INFO_NOT_SPECIFIED", getActionName() ) ); //$NON-NLS-1$ 
    }

    if ( proceed ) {
      catalog = getCatalog();

      if ( ( null == catalog ) || ( catalog.trim().length() <= 0 ) ) {
        proceed = false;
        error( Messages.getInstance().getErrorString(
          "HQLBaseComponent.ERROR_00010_CATALOG_INFO_NOT_SPECIFIED", getActionName() ) ); //$NON-NLS-1$
      }
    }

    if ( proceed ) {
      connection = getConnection( new File( catalog ), classNames );

      if ( connection == null ) {
        error( Messages.getInstance().getErrorString(
            "HQLBaseComponent.ERROR_0009_COULD_NOT_ESTABLISH_CONNECTION", getActionName() ) ); //$NON-NLS-1$
      }
    }
  }

  /*
   * Utitlity function to get the class names from the XML.
   */
  private String[] getClassNames( final HQLConnectionAction connAction ) {
    ArrayList classNamesList = new ArrayList();
    String[] classNames = null;
    try {
      String classes = connAction.getClassNames().getStringValue();
      StringTokenizer st = new StringTokenizer( classes, "," ); //getInputStringValue(CLASSNAMES), ","); //$NON-NLS-1$
      while ( st.hasMoreTokens() ) {
        String token = st.nextToken();
        classNamesList.add( token.trim() );
      }

      classNames = (String[]) classNamesList.toArray( new String[0] );

    } catch ( Exception e ) {
      error( Messages.getInstance().getErrorString(
        "HQLBaseComponent.ERROR_0008_COULD_NOT_RETRIEVE_CLASS_NAMES", getActionName() ), e ); //$NON-NLS-1$
    }

    return classNames;
  }

  /*
   * Utility function to get the catalog info from hibernate config.
   */
  private String getCatalog() {
    IActionResource hibernateConfigRes = ( (HQLConnectionAction) getActionDefinition() ).getHibernateConfigResource();
    String catalog = null;
    String resAddress = null;

    if ( hibernateConfigRes != null ) {
      String resName = this.applyInputsToFormat( hibernateConfigRes.getName() );
      IActionSequenceResource resource = getResource( resName );
      resAddress = resource.getAddress();
      if ( resAddress != null ) {
        catalog = this.applyInputsToFormat( resAddress );
      }
    }
    return catalog;
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
        error( Messages.getInstance().getErrorString( "HQLBaseComponent.ERROR_00013_NO_CONNECTION" ) ); //$NON-NLS-1$
        return false;
      }
      if ( !connection.initialized() ) {
        error( Messages.getInstance().getErrorString( "HQLBaseComponent.ERROR_00013_NO_CONNECTION" ) ); //$NON-NLS-1$
        return false;
      }
      if ( rawQuery != null ) {
        preparedQuery = applyInputsToFormat( rawQuery );
      }

      return true;
    } catch ( Exception e ) {
      error( Messages.getInstance().getErrorString(
        "HQLBaseComponent.ERROR_00014_COULD_NOT_PREPARE_QUERY", getActionName() ), e ); //$NON-NLS-1$
    }

    return false;
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
        error( Messages.getInstance().getErrorString( "HQLBaseComponent.ERROR_0009_COULD_NOT_ESTABLISH_CONNECTION" )
        ); //$NON-NLS-1$
        return null;
      }
      if ( !connection.initialized() ) {
        error( Messages.getInstance().getErrorString( "HQLBaseComponent.ERROR_0009_COULD_NOT_ESTABLISH_CONNECTION" ) ); //$NON-NLS-1$
        return null;
      }

      if ( preparedQuery == null ) {
        error( Messages.getInstance().getErrorString( "HQLBaseComponent.ERROR_00016_QUERY_NOT_SPECIFIED" ) ); //$NON-NLS-1$
        return null;
      }

      // parse preparedQuery, replacing any {PREPARELATER:NAME} with appropriate values
      String query =
          TemplateUtil.applyTemplate( preparedQuery, getRuntimeContext(), new MapParameterResolver( preparedParams,
              IPreparedComponent.PREPARE_LATER_PREFIX, getRuntimeContext() ) );

      if ( ComponentBase.debug ) {
        debug( Messages.getInstance().getString( "HQLBaseComponent.DEBUG_RUNNING_QUERY", query ) ); //$NON-NLS-1$
      }

      // evaluate
      IPentahoResultSet resultSet = connection.executeQuery( query );
      rSet = resultSet;
      return resultSet;
    } catch ( Exception e ) {
      error( Messages.getInstance().getErrorString( "HQLBaseComponent.ERROR_00012_EXECUTE_FAILED", getActionName() ), e ); //$NON-NLS-1$
    }
    return null;
  }

  protected boolean runQuery( final IPentahoConnection conn, final String[] classNames, final String query ) {
    try {

      if ( conn == null ) {
        return false;
      }

      rSet = ( (HQLConnection) conn ).executeQuery( query );

      IActionOutput actionOutput = ( (HQLQueryAction) getActionDefinition() ).getOutputResultSetParam();
      if ( actionOutput != null ) {
        actionOutput.setValue( rSet );
      }
      return true;
    } catch ( Exception e ) {
      error( Messages.getInstance().getErrorString(
          "HQLBaseComponent.ERROR_0007_QUERY_EXECUTION_FAILED", getActionName() ), e ); //$NON-NLS-1$
      return false;
    }
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

  protected IPentahoConnection getConnection( final File hbmCfgFile, final String[] classNames ) {
    IPentahoConnection conn = null;
    try {
      conn = (HQLConnection) PentahoConnectionFactory.getConnection( "HQL", getSession(), this ); //$NON-NLS-1$
      HQLConnection hconn = (HQLConnection) conn;
      hconn.setConfigFile( hbmCfgFile );
      hconn.setClassNames( classNames );
      return conn;
    } catch ( Exception e ) {
      error( Messages.getInstance().getErrorString(
        "HQLBaseComponent.ERROR_0009_COULD_NOT_ESTABLISH_CONNECTION", getActionName() ), e ); //$NON-NLS-1$
    }
    return null;
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

  @Override
  public boolean init() {
    return true;
  }
}
