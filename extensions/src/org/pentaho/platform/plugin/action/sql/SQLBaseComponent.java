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

package org.pentaho.platform.plugin.action.sql;

import org.apache.commons.logging.Log;
import org.pentaho.actionsequence.dom.ActionInputConstant;
import org.pentaho.actionsequence.dom.IActionDefinition;
import org.pentaho.actionsequence.dom.IActionInput;
import org.pentaho.actionsequence.dom.IActionOutput;
import org.pentaho.actionsequence.dom.actions.AbstractRelationalDbAction;
import org.pentaho.actionsequence.dom.actions.SqlConnectionAction;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.commons.connection.IPentahoMetaData;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.commons.connection.PentahoDataTransmuter;
import org.pentaho.commons.connection.memory.MemoryMetaData;
import org.pentaho.platform.api.data.IDataComponent;
import org.pentaho.platform.api.data.IPreparedComponent;
import org.pentaho.platform.api.engine.IParameterResolver;
import org.pentaho.platform.engine.services.connection.PentahoConnectionFactory;
import org.pentaho.platform.engine.services.runtime.TemplateUtil;
import org.pentaho.platform.engine.services.solution.ComponentBase;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.plugin.services.connections.sql.SQLConnection;

import java.text.Format;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;

/**
 * SQLBaseComponent is the base class for SQLExecute and SQLLookupRule. it does the majority of work when interacting
 * with Pentaho's BI Platform, including implementing the necessary component features. It also implements
 * IDataComponent and IPreparedComponent.
 * 
 * @see SQLExecute
 * @see SQLLookupRule
 */
public abstract class SQLBaseComponent extends ComponentBase implements IDataComponent, IPreparedComponent,
    IParameterResolver {

  public static final String PREPARE_PARAMETER_PREFIX = "PREPARE"; //$NON-NLS-1$

  /** stores the prepared query for later use */
  protected String preparedQuery = null;

  /** stores the prepared parameters for later use */
  protected List preparedParameters = new ArrayList();

  /** is set to false if using another IPreparedComponents connection vs own */
  protected boolean connectionOwner = true;

  /** reference to latest result set */
  private IPentahoResultSet rSet;

  /** reference to connection object */
  protected IPentahoConnection connection;

  // Added by Arijit Chatterjee.Takes the value of timeout
  private int timeout = -1;
  private int maxRows = -1; // Add ability to set this as an input
  private boolean readOnly = false;

  @Override
  public abstract boolean validateSystemSettings();

  public abstract String getResultOutputName();

  @Override
  public abstract Log getLogger();

  /**
   * returns the result set object
   * 
   * @return pentaho result set
   */
  public IPentahoResultSet getResultSet() {
    return rSet;
  }

  /**
   * validates the action. checks to verify inputs are available to execute
   * 
   * - verify query is available - verify connection is available, via jndi, connection string, or prepared component -
   * verify output is specified
   * 
   * 
   */
  @Override
  public boolean validateAction() {
    boolean result = true;

    IActionDefinition actionDefinition = getActionDefinition();
    String actionName = getActionName();

    if ( actionDefinition instanceof AbstractRelationalDbAction ) {
      AbstractRelationalDbAction relationalDbAction = (AbstractRelationalDbAction) actionDefinition;
      IActionInput query = relationalDbAction.getQuery();
      IActionInput dbUrl = relationalDbAction.getDbUrl();
      IActionInput jndi = relationalDbAction.getJndi();

      IActionInput sharedConnection = relationalDbAction.getSharedConnection();
      if ( query == ActionInputConstant.NULL_INPUT ) {

        error( Messages.getInstance().getErrorString( "SQLBaseComponent.ERROR_0001_QUERY_NOT_SPECIFIED", actionName ) ); //$NON-NLS-1$
        result = false;
      }

      if ( ( jndi == ActionInputConstant.NULL_INPUT ) && ( dbUrl == ActionInputConstant.NULL_INPUT )
          && ( sharedConnection == ActionInputConstant.NULL_INPUT ) ) {
        error( Messages.getInstance().getErrorString(
          "SQLBaseComponent.ERROR_0002_CONNECTION_NOT_SPECIFIED", actionName ) ); //$NON-NLS-1$
        result = false;
      }
    } else if ( actionDefinition instanceof SqlConnectionAction ) {
      SqlConnectionAction sqlConnectionAction = (SqlConnectionAction) actionDefinition;
      IActionInput dbUrl = sqlConnectionAction.getDbUrl();
      IActionInput jndi = sqlConnectionAction.getJndi();
      if ( ( jndi == ActionInputConstant.NULL_INPUT ) && ( dbUrl == ActionInputConstant.NULL_INPUT ) ) {
        error( Messages.getInstance().getErrorString(
          "SQLBaseComponent.ERROR_0002_CONNECTION_NOT_SPECIFIED", actionName ) ); //$NON-NLS-1$
        result = false;
      }
    } else {
      error( Messages.getInstance().getErrorString(
          "ComponentBase.ERROR_0001_UNKNOWN_ACTION_TYPE", actionDefinition.getElement().asXML() ) ); //$NON-NLS-1$
      result = false;
    }
    return result;
  }

  /**
   * nothing to do in done call from runtime context.
   */
  @Override
  public void done() {
  }

  /**
   * determines state of component, and executes accordingly.
   * 
   * various inputs that impact the state include:
   * 
   * live - returns a live result set vs. an in memory copy transform - transform a result set based on additional
   * inputs prepared_component - if available, use existing connection from prepared component max_rows - sets the
   * number of rows that should be returned in result sets
   * 
   * The specified output also impacts the state of the execution. If prepared_component is defined as an output, setup
   * the query but delay execution.
   * 
   */
  @Override
  protected boolean executeAction() {
    IActionDefinition actionDefinition = getActionDefinition();
    try {

      if ( actionDefinition instanceof AbstractRelationalDbAction ) {
        AbstractRelationalDbAction relationalDbAction = (AbstractRelationalDbAction) actionDefinition;
        // Added by Arijit Chatterjee
        IActionInput queryTimeoutInput = relationalDbAction.getQueryTimeout();
        IActionInput maxRowsInput = relationalDbAction.getMaxRows();
        IActionInput readOnlyInput = relationalDbAction.getReadOnly();

        String baseQuery = getQuery();
        if ( baseQuery == null ) {
          error( Messages.getInstance().getErrorString(
              "SQLBaseComponent.ERROR_0001_QUERY_NOT_SPECIFIED", actionDefinition.getDescription() ) ); //$NON-NLS-1$
          return false;
        }

        IPreparedComponent sharedConnection = (IPreparedComponent) relationalDbAction.getSharedConnection().getValue();

        if ( readOnlyInput != ActionInputConstant.NULL_INPUT ) {
          this.setReadOnly( readOnlyInput.getBooleanValue() );
        }

        if ( sharedConnection != null ) {
          connectionOwner = false;
          IPentahoConnection conn = sharedConnection.shareConnection();
          if ( conn == null ) {
            error( Messages.getInstance().getErrorString(
                "IPreparedComponent.ERROR_0002_CONNECTION_NOT_AVAILABLE", getActionName() ) ); //$NON-NLS-1$
            return false;
          } else if ( conn.getDatasourceType() == IPentahoConnection.SQL_DATASOURCE ) {
            connection = conn;
          } else {
            error( Messages.getInstance().getErrorString(
                "IPreparedComponent.ERROR_0001_INVALID_CONNECTION_TYPE", getActionName() ) ); //$NON-NLS-1$
            return false;
          }
        } else {
          dispose();
          connection = getDatasourceConnection();
        }

        if ( connection == null ) {
          return false;
        }

        // Check if this is a prepared query that will be executed later. If so cache the
        // query and set this component as the output. This query will be run later from a subreport.
        if ( relationalDbAction.getOutputPreparedStatement() != null ) {
          prepareQuery( baseQuery );
          IActionOutput actionOutput = relationalDbAction.getOutputPreparedStatement();
          if ( actionOutput != null ) {
            actionOutput.setValue( this );
          }
          return true;
        }

        // TODO not sure if this should be allowed without connection ownership?
        // int maxRows = relationalDbAction.getMaxRows().getIntValue(-1);
        if ( maxRowsInput != ActionInputConstant.NULL_INPUT ) {
          this.setMaxRows( maxRowsInput.getIntValue() );
        }

        // Added by Arijit Chatterjee.Sets the value of timeout. Default is -1, if parameter not found.
        if ( queryTimeoutInput != ActionInputConstant.NULL_INPUT ) {
          this.setQueryTimeout( queryTimeoutInput.getIntValue() );
        }

        if ( relationalDbAction.getPerformTransform().getBooleanValue( false ) ) {
          runQuery( baseQuery, false ); // The side effect of
          // transform rSet here

          rSet =
              PentahoDataTransmuter.crossTab( rSet, relationalDbAction.getTransformPivotColumn().getIntValue( -1 ) - 1,
                  relationalDbAction.getTransformMeasuresColumn().getIntValue( -1 ) - 1, relationalDbAction
                      .getTransformSortColumn().getIntValue( 0 ) - 1, (Format) relationalDbAction
                      .getTransformPivotDataFormat().getValue(), (Format) relationalDbAction
                      .getTransformSortDataFormat().getValue(), relationalDbAction.getTransformOrderOutputColumns()
                      .getBooleanValue( false ) );

          IActionOutput actionOutput = relationalDbAction.getOutputResultSet();
          if ( actionOutput != null ) {
            actionOutput.setValue( rSet );
          }
          return true;
        } else {
          return runQuery( baseQuery, relationalDbAction.getLive().getBooleanValue( false ) );
        }
      } else if ( actionDefinition instanceof SqlConnectionAction ) {
        SqlConnectionAction sqlConnectionAction = (SqlConnectionAction) actionDefinition;
        dispose();
        connection = getDatasourceConnection();
        if ( connection == null ) {
          return false;
        } else {
          IActionOutput actionOutput = sqlConnectionAction.getOutputConnection();
          if ( actionOutput != null ) {
            actionOutput.setValue( this );
            return true;
          } else {
            return false;
          }
        }
      }
    } catch ( Exception e ) {
      error( Messages.getInstance().getErrorString( "SQLBaseComponent.ERROR_0006_EXECUTE_FAILED", getActionName() ), e ); //$NON-NLS-1$
    }

    return false;
  }

  /**
   * returns metadata based on the result set. if not live, create an in memory version
   * 
   * @param resultSet
   *          result set object to find metadata
   * @param live
   *          if false, create an in memory version
   * 
   * @return metadata object
   */
  protected IPentahoMetaData getMetadata( final IPentahoResultSet resultSet, final boolean live ) {
    if ( live ) {
      return resultSet.getMetaData();
    } else {
      Object[][] columnHeaders = resultSet.getMetaData().getColumnHeaders();
      return new MemoryMetaData( columnHeaders, null );
    }
  }

  /**
   * This inner class is used as a resolver for TemplateUtil.
   */
  private class ParamResolver implements IParameterResolver {
    List paramList;

    Map lookupMap;

    public ParamResolver( final List list, final Map map ) {
      lookupMap = map;
      paramList = list;
    }

    /**
     * This method is called when TemplateUtil.applyTemplate() encounters a parameter.
     * 
     * @param template
     *          the source string
     * @param parameter
     *          the parameter value
     * @param parameterMatcher
     *          the regex parameter matcher
     * @param copyStart
     *          the start of the copy
     * @param results
     *          the output result
     * @return the next copystart
     */
    public int resolveParameter( final String template, final String parameter, final Matcher parameterMatcher,
        int copyStart, final StringBuffer results ) {

      StringTokenizer tokenizer = new StringTokenizer( parameter, ":" ); //$NON-NLS-1$
      if ( tokenizer.countTokens() == 2 ) { // Currently, the component only handles one bit of metadata
        String parameterPrefix = tokenizer.nextToken();
        String inputName = tokenizer.nextToken();

        if ( parameterPrefix.equals( IPreparedComponent.PREPARE_LATER_INTER_PREFIX ) ) {
          // We know this parameter is for us.
          // First, is this a special input
          Object parameterValue = TemplateUtil.getSystemInput( inputName, getRuntimeContext() );
          if ( ( parameterValue == null ) && ( lookupMap != null ) && lookupMap.containsKey( inputName ) ) {
            parameterValue = lookupMap.get( inputName );
          }
          if ( parameterValue != null ) {
            // We have a parameter value - now, it's time to create a parameter and build up the
            // parameter string
            int start = parameterMatcher.start();
            int end = parameterMatcher.end();
            // First, find out if the parameter was quoted...
            if ( ( start > 0 ) && ( end < template.length() ) ) {
              if ( ( template.charAt( start - 1 ) == '\'' ) && ( template.charAt( end ) == '\'' ) ) {
                // Ok, the parameter was quoted as near as we can tell. So, we need
                // to increase the size of the amount we overwrite by one in each
                // direction. This is for backward compatibility.
                start--;
                end++;
              }
            }
            // We now have a valid start and end. It's time to see whether we're dealing
            // with an array, a result set, or a scalar.
            StringBuffer parameterBuffer = new StringBuffer();

            // find and remove the next placeholder, to be replaced by the new value
            int index = paramList.indexOf( IPreparedComponent.PREPARE_LATER_PLACEHOLDER );
            paramList.remove( index );
            if ( parameterValue instanceof String ) {
              paramList.add( index, parameterValue );
              // preparedParameters.add(parameterValue);
              parameterBuffer.append( '?' );
            } else if ( parameterValue instanceof Object[] ) {
              Object[] pObj = (Object[]) parameterValue;
              for ( Object element : pObj ) {
                paramList.add( index++, element );
                parameterBuffer.append( ( parameterBuffer.length() == 0 ) ? "?" : ",?" ); //$NON-NLS-1$ //$NON-NLS-2$
              }
            } else if ( parameterValue instanceof IPentahoResultSet ) {
              IPentahoResultSet rs = (IPentahoResultSet) parameterValue;
              // See if we can find a column in the metadata with the same
              // name as the input
              IPentahoMetaData md = rs.getMetaData();
              int columnIdx = -1;
              if ( md.getColumnCount() == 1 ) {
                columnIdx = 0;
              } else {
                columnIdx = md.getColumnIndex( new String[] { parameter } );
              }
              if ( columnIdx < 0 ) {
                error( Messages.getInstance().getErrorString( "Template.ERROR_0005_COULD_NOT_DETERMINE_COLUMN" ) ); //$NON-NLS-1$
                return -1;
              }
              int rowCount = rs.getRowCount();
              Object valueCell = null;
              // TODO support non-string columns
              for ( int i = 0; i < rowCount; i++ ) {
                valueCell = rs.getValueAt( i, columnIdx );
                paramList.add( index++, valueCell );
                parameterBuffer.append( ( parameterBuffer.length() == 0 ) ? "?" : ",?" ); //$NON-NLS-1$ //$NON-NLS-2$
              }
            } else if ( parameterValue instanceof List ) {
              List pObj = (List) parameterValue;
              for ( int i = 0; i < pObj.size(); i++ ) {
                paramList.add( index++, pObj.get( i ) );
                parameterBuffer.append( ( parameterBuffer.length() == 0 ) ? "?" : ",?" ); //$NON-NLS-1$ //$NON-NLS-2$
              }
            } else {
              // If we're here, we know parameterValue is not null and not a string
              paramList.add( index, parameterValue );
              parameterBuffer.append( '?' );
            }

            // OK - We have a parameterBuffer and have filled out the preparedParameters
            // list. It's time to change the SQL to insert our parameter marker and tell
            // the caller we've done our job.
            results.append( template.substring( copyStart, start ) );
            copyStart = end;
            results.append( parameterBuffer );
            return copyStart;
          }
        }
      }

      return -1; // Nothing here for us - let default behavior through
    }
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
        error( Messages.getInstance().getErrorString( "SQLBaseComponent.ERROR_0007_NO_CONNECTION" ) ); //$NON-NLS-1$
        return null;
      }
      if ( !connection.initialized() ) {
        error( Messages.getInstance().getErrorString( "SQLBaseComponent.ERROR_0007_NO_CONNECTION" ) ); //$NON-NLS-1$
        return null;
      }

      if ( preparedQuery == null ) {
        error( Messages.getInstance().getErrorString(
          "SQLBaseComponent.ERROR_0001_QUERY_NOT_SPECIFIED", getActionName() ) ); //$NON-NLS-1$
        return null;
      }

      // copy the preparedParams list, so it can be used multiple times.
      ArrayList copyOfPreparedParameters = new ArrayList( preparedParameters );

      // parse preparedQuery, replacing any {PREPARELATER:NAME} with appropriate values
      String query =
          TemplateUtil.applyTemplate( preparedQuery, getRuntimeContext(), new ParamResolver( copyOfPreparedParameters,
              preparedParams ) );

      if ( ComponentBase.debug ) {
        dumpQuery( query );
      }

      // evaluate
      IPentahoResultSet resultSet = null;
      if ( preparedParameters.size() > 0 ) {
        resultSet = connection.prepareAndExecuteQuery( query, copyOfPreparedParameters );
      } else {
        resultSet = connection.executeQuery( query );
      }

      if ( connection instanceof SQLConnection ) {
        if ( ( (SQLConnection) connection ).isForcedForwardOnly() ) {
          warn( Messages.getInstance().getString( "SQLBaseComponent.WARN_FALL_BACK_TO_NONSCROLLABLE" ) ); //$NON-NLS-1$
        }
      }

      boolean live = true;
      IActionDefinition actionDefinition = getActionDefinition();
      if ( actionDefinition instanceof AbstractRelationalDbAction ) {
        AbstractRelationalDbAction relationalDbAction = (AbstractRelationalDbAction) actionDefinition;
        live = relationalDbAction.getLive().getBooleanValue( false );
      }

      IPentahoResultSet rs = resultSet;

      // BISERVER-5915, BISERVER-5875 - if the live setting is false, return an in memory resultset.
      if ( !live ) {
        rs = resultSet.memoryCopy();
      }

      rSet = rs;
      return rs;

    } catch ( Exception e ) {
      error( Messages.getInstance().getErrorString( "SQLBaseComponent.ERROR_0006_EXECUTE_FAILED", getActionName() ), e ); //$NON-NLS-1$
    }
    return null;
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
        error( Messages.getInstance().getErrorString( "SQLBaseComponent.ERROR_0007_NO_CONNECTION" ) ); //$NON-NLS-1$
        return false;
      }
      if ( !connection.initialized() ) {
        error( Messages.getInstance().getErrorString( "SQLBaseComponent.ERROR_0007_NO_CONNECTION" ) ); //$NON-NLS-1$
        return false;
      }

      preparedQuery = rawQuery;

      return true;
    } catch ( Exception e ) {
      error( Messages.getInstance().getErrorString( "SQLBaseComponent.ERROR_0006_EXECUTE_FAILED", getActionName() ), e ); //$NON-NLS-1$
    }

    return false;
  }

  /**
   * executes the specified query template. The query template is first formatted and then executed. If live, the
   * original result set is made available as an output. If not live, the result set is converted into memory and the
   * connection and live result set are closed.
   * 
   * @param rawQuery
   *          query template
   * @param live
   *          returns original result set if true, memory result set if false
   * @return true if successful
   */
  protected boolean runQuery( final String rawQuery, boolean live ) {
    try {
      if ( ( connection == null ) || !connection.initialized() ) {
        error( Messages.getInstance().getErrorString( "SQLBaseComponent.ERROR_0007_NO_CONNECTION" ) ); //$NON-NLS-1$
        return false;
      }

      String query = applyInputsToFormat( rawQuery );
      SQLConnection sqlConnection = null;
      if ( ( connection instanceof SQLConnection ) ) {
        sqlConnection = (SQLConnection) connection;
      }
      // Some of the following Added by Arijit Chatterjee passing the timeout value to SQLConnection class
      if ( sqlConnection != null ) {
        if ( this.getQueryTimeout() >= 0 ) {
          sqlConnection.setQueryTimeout( this.getQueryTimeout() );
        }
        if ( this.getMaxRows() >= 0 ) {
          sqlConnection.setMaxRows( this.getMaxRows() );
        }
        if ( this.getReadOnly() ) {
          sqlConnection.setReadOnly( true );
        }
      }

      AbstractRelationalDbAction relationalDbAction = (AbstractRelationalDbAction) getActionDefinition();

      IPentahoResultSet resultSet = null;
      boolean isForwardOnly = relationalDbAction.getUseForwardOnlyResultSet().getBooleanValue( false );

      resultSet = doQuery( sqlConnection, query, isForwardOnly );

      if ( sqlConnection.isForcedForwardOnly() ) {
        isForwardOnly = true;
        live = false;
        warn( Messages.getInstance().getString( "SQLBaseComponent.WARN_FALL_BACK_TO_NONSCROLLABLE" ) ); //$NON-NLS-1$
      }

      if ( live ) {

        // set the result set as the output
        rSet = resultSet;

        // After preparation and execution, we need to clear out the
        // prepared parameters.
        preparedParameters.clear();
        if ( resultSet != null ) {
          getMetadata( resultSet, true );
          IActionOutput actionOutput = relationalDbAction.getOutputResultSet();
          if ( actionOutput != null ) {
            actionOutput.setValue( resultSet );
          }
          return true;
        } else {
          // close the connection if owner
          error( Messages.getInstance().getErrorString( "SQLBaseComponent.ERROR_0006_EXECUTE_FAILED",
            getActionName() ) ); //$NON-NLS-1$
          if ( connectionOwner ) {
            connection.close();
          }
          return false;
        }

      } else {
        // execute the query, read the results and cache them
        try {
          // After preparation and execution, we need to clear out the
          // prepared parameters.
          preparedParameters.clear();

          IPentahoResultSet cachedResultSet = resultSet.memoryCopy();
          rSet = cachedResultSet;

          IActionOutput actionOutput = relationalDbAction.getOutputResultSet();
          if ( actionOutput != null ) {
            actionOutput.setValue( cachedResultSet );
          }
        } finally {
          // close the connection if owner
          if ( connectionOwner ) {
            connection.close();
            connection = null;
          }
        }
      }
      return true;
    } catch ( Exception e ) {
      error( Messages.getInstance().getErrorString( "SQLBaseComponent.ERROR_0006_EXECUTE_FAILED", getActionName() ), e ); //$NON-NLS-1$
    }

    return false;
  }

  public IPentahoResultSet
  doQuery( final SQLConnection sqlConnection, final String query, boolean forwardOnlyResultset ) throws Exception {
    //
    // At this point, 'connection' and 'sqlConnection' should be pointers to
    // the same object iff the 'connection' is a subclass of pentaho's SQLConnection.
    // It is possible that the sqlConnection will be null, but the connection
    // won't be if someone is using their own implementation of the SQLConnection from
    // the factory.
    //

    IPentahoResultSet resultSet = null;
    if ( ComponentBase.debug ) {
      dumpQuery( query );
    }

    if ( preparedParameters.size() > 0 ) {
      if ( !forwardOnlyResultset ) {
        resultSet = connection.prepareAndExecuteQuery( query, preparedParameters );
      } else {
        if ( sqlConnection != null ) {
          resultSet =
              sqlConnection.prepareAndExecuteQuery( query, preparedParameters, SQLConnection.RESULTSET_FORWARDONLY,
                  SQLConnection.CONCUR_READONLY );
        } else {
          throw new IllegalStateException( Messages.getInstance().getErrorString(
            "SQLBaseComponent.ERROR_0008_UNSUPPORTED_CURSOR_TYPE" ) ); //$NON-NLS-1$
        }
      }
    } else {
      if ( !forwardOnlyResultset ) {
        resultSet = connection.executeQuery( query );
      } else {
        if ( sqlConnection != null ) {
          resultSet =
              sqlConnection.executeQuery( query, SQLConnection.RESULTSET_FORWARDONLY, SQLConnection.CONCUR_READONLY );
        } else {
          throw new IllegalStateException( Messages.getInstance().getErrorString(
            "SQLBaseComponent.ERROR_0008_UNSUPPORTED_CURSOR_TYPE" ) ); //$NON-NLS-1$
        }
      }
    }
    return resultSet;
  }

  /**
   * dispose of the resultset, and if the owner, dispose of the connection.
   */
  public void dispose() {

    rSet = null;

    // close connection if owner
    if ( connectionOwner ) {
      if ( connection != null ) {
        connection.close();
      }
      connection = null;
    }

  }

  /**
   * This method is called when TemplateUtil.applyTemplate() encounters a parameter. TemplateUtil.applyTemplate is
   * called when someone makes a call to applyInputsToFormat() In this class it is called in the above "runQuery()"
   * method.
   * 
   * @param template
   *          the source string
   * @param parameter
   *          the parameter value
   * @param parameterMatcher
   *          the regex parameter matcher
   * @param copyStart
   *          the start of the copy
   * @param results
   *          the output result
   * @return the next copystart
   */
  @Override
  public int resolveParameter( final String template, final String parameter, final Matcher parameterMatcher,
      int copyStart, final StringBuffer results ) {

    StringTokenizer tokenizer = new StringTokenizer( parameter, ":" ); //$NON-NLS-1$
    if ( tokenizer.countTokens() == 2 ) { // Currently, the component only handles one bit of metadata
      String parameterPrefix = tokenizer.nextToken();
      String inputName = tokenizer.nextToken();

      // if the template contains a prepare later prefix,
      // mark a spot in the preparedParameters list and move on.
      if ( parameterPrefix.equals( IPreparedComponent.PREPARE_LATER_PREFIX ) ) {
        if ( !isDefinedOutput( IPreparedComponent.PREPARED_COMPONENT_NAME ) ) {
          error( Messages.getInstance().getErrorString( "IPreparedComponent.ERROR_0003_INVALID_PARAMETER_STATE" ) ); //$NON-NLS-1$
          return -1;
        }
        preparedParameters.add( IPreparedComponent.PREPARE_LATER_PLACEHOLDER );
        int start = parameterMatcher.start();
        int end = parameterMatcher.end();
        results.append( template.substring( copyStart, start ) );
        results.append( "{" + IPreparedComponent.PREPARE_LATER_INTER_PREFIX + ":" + inputName + "}" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        return end;
      }

      if ( parameterPrefix.equals( SQLBaseComponent.PREPARE_PARAMETER_PREFIX ) ) {
        // We know this parameter is for us.
        // First, is this a special input
        Object parameterValue = TemplateUtil.getSystemInput( inputName, getRuntimeContext() );
        if ( ( parameterValue == null ) && isDefinedInput( inputName ) ) {
          parameterValue = this.getInputValue( inputName );
        }
        if ( parameterValue != null ) {
          // We have a parameter value - now, it's time to create a parameter and build up the
          // parameter string
          int start = parameterMatcher.start();
          int end = parameterMatcher.end();
          // First, find out if the parameter was quoted...
          if ( ( start > 0 ) && ( end < template.length() ) ) {
            if ( ( template.charAt( start - 1 ) == '\'' ) && ( template.charAt( end ) == '\'' ) ) {
              // Ok, the parameter was quoted as near as we can tell. So, we need
              // to increase the size of the amount we overwrite by one in each
              // direction. This is for backward compatibility.
              start--;
              end++;
            }
          }
          // We now have a valid start and end. It's time to see whether we're dealing
          // with an array, a result set, or a scalar.
          StringBuffer parameterBuffer = new StringBuffer();
          if ( parameterValue instanceof String ) {
            preparedParameters.add( parameterValue );
            parameterBuffer.append( '?' );
          } else if ( parameterValue instanceof Object[] ) {
            Object[] pObj = (Object[]) parameterValue;
            for ( Object element : pObj ) {
              preparedParameters.add( element );
              parameterBuffer.append( ( parameterBuffer.length() == 0 ) ? "?" : ",?" ); //$NON-NLS-1$ //$NON-NLS-2$
            }
          } else if ( parameterValue instanceof IPentahoResultSet ) {
            IPentahoResultSet rs = (IPentahoResultSet) parameterValue;
            // See if we can find a column in the metadata with the same
            // name as the input
            IPentahoMetaData md = rs.getMetaData();
            int columnIdx = -1;
            if ( md.getColumnCount() == 1 ) {
              columnIdx = 0;
            } else {
              columnIdx = md.getColumnIndex( new String[] { parameter } );
            }
            if ( columnIdx < 0 ) {
              error( Messages.getInstance().getErrorString( "Template.ERROR_0005_COULD_NOT_DETERMINE_COLUMN" ) ); //$NON-NLS-1$
              return -1;
            }
            int rowCount = rs.getRowCount();
            Object valueCell = null;
            // TODO support non-string columns
            for ( int i = 0; i < rowCount; i++ ) {
              valueCell = rs.getValueAt( i, columnIdx );
              preparedParameters.add( valueCell );
              parameterBuffer.append( ( parameterBuffer.length() == 0 ) ? "?" : ",?" ); //$NON-NLS-1$ //$NON-NLS-2$
            }
          } else if ( parameterValue instanceof List ) {
            List pObj = (List) parameterValue;
            for ( int i = 0; i < pObj.size(); i++ ) {
              preparedParameters.add( pObj.get( i ) );
              parameterBuffer.append( ( parameterBuffer.length() == 0 ) ? "?" : ",?" ); //$NON-NLS-1$ //$NON-NLS-2$
            }
          } else {
            // If we're here, we know parameterValue is not null and not a string
            this.preparedParameters.add( parameterValue );
            parameterBuffer.append( '?' );
          }

          // OK - We have a parameterBuffer and have filled out the preparedParameters
          // list. It's time to change the SQL to insert our parameter marker and tell
          // the caller we've done our job.
          results.append( template.substring( copyStart, start ) );
          copyStart = end;
          results.append( parameterBuffer );
          return copyStart;
        }
      }
    }

    return -1; // Nothing here for us - let default behavior through
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
          //ignore
        }
        return con;
      } catch ( Exception ex ) {
        //ignore
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

  /**
   * pause the thread a certain number of milliseconds
   * 
   * @param millis
   *          time to sleep
   */
  protected void waitFor( final int millis ) {
    try {
      if ( ComponentBase.debug ) {
        debug( Messages.getInstance().getString(
          "SQLBaseComponent.DEBUG_WAITING_FOR_CONNECTION", Integer.toString( millis ) ) ); //$NON-NLS-1$
      }
      Thread.sleep( millis );
    } catch ( Exception ex ) {
      // ignore the interrupted exception, if it happens
    }
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
   * pass through to getConnection(defaultConnection)
   * 
   * @return connection
   */
  protected IPentahoConnection getConnection() {
    return getConnection( null );
  }

  /**
   * This method retrieves a connection based on the components inputs.
   * 
   * @param defaultConnection
   *          a default connection to use if no other is available
   * @return new connection object
   */
  protected IPentahoConnection getConnection( final IPentahoConnection defaultConnection ) {
    IPentahoConnection localConnection = null;
    try {
      String jndiName = null;
      String driver = null;
      String userId = null;
      String password = null;
      String connectionInfo = null;
      if ( getActionDefinition() instanceof SqlConnectionAction ) {
        SqlConnectionAction sqlConnectionAction = (SqlConnectionAction) getActionDefinition();
        jndiName = sqlConnectionAction.getJndi().getStringValue();
        driver = sqlConnectionAction.getDriver().getStringValue();
        userId = sqlConnectionAction.getUserId().getStringValue();
        password = sqlConnectionAction.getPassword().getStringValue();
        connectionInfo = sqlConnectionAction.getDbUrl().getStringValue();
      } else if ( getActionDefinition() instanceof AbstractRelationalDbAction ) {
        AbstractRelationalDbAction relationalDbAction = (AbstractRelationalDbAction) getActionDefinition();
        jndiName = relationalDbAction.getJndi().getStringValue();
        driver = relationalDbAction.getDriver().getStringValue();
        userId = relationalDbAction.getUserId().getStringValue();
        password = relationalDbAction.getPassword().getStringValue();
        connectionInfo = relationalDbAction.getDbUrl().getStringValue();
      }
      if ( jndiName != null ) {
        localConnection =
            PentahoConnectionFactory.getConnection( IPentahoConnection.SQL_DATASOURCE, jndiName, getSession(), this );
      }
      if ( localConnection == null ) {
        localConnection =
              PentahoConnectionFactory.getConnection( IPentahoConnection.SQL_DATASOURCE, driver, connectionInfo,
                  userId, password, getSession(), this );
      }
      if ( localConnection == null ) {
        if ( defaultConnection == null ) {
          error( Messages.getInstance().getErrorString( "SQLBaseComponent.ERROR_0005_INVALID_CONNECTION" ) ); //$NON-NLS-1$
          return null;
        } else {
          localConnection = defaultConnection;
        }
      }
      return localConnection;
    } catch ( Exception e ) {
      error( Messages.getInstance().getErrorString( "SQLBaseComponent.ERROR_0006_EXECUTE_FAILED", getActionName() ), e ); //$NON-NLS-1$
    }
    return null;
  }

  /**
   * nothing is done in the init function
   * 
   * @return true always
   */
  @Override
  public boolean init() {
    return true;
  }

  // Added By Arijit Chatterjee,This method is not used anywhere added for only testing purposes
  public int getQueryTimeout() {
    // removed the destruction of parameters on a get.
    // preparedParameters.clear();
    return timeout;
  }

  // Added By Arijit Chatterjee.Sets the value of timeout
  public void setQueryTimeout( final int timeInSec ) {
    timeout = timeInSec;
  }

  public int getMaxRows() {
    return this.maxRows;
  }

  public void setMaxRows( final int value ) {
    this.maxRows = value;
  }

  public String getQuery() {
    preparedParameters.clear();
    return ( (AbstractRelationalDbAction) getActionDefinition() ).getQuery().getStringValue();
  }

  public void setReadOnly( final boolean value ) {
    this.readOnly = value;
  }

  public boolean getReadOnly() {
    return this.readOnly;
  }

  private void dumpQuery( final String query ) {
    if ( timeout == 0 ) {
      debug( Messages.getInstance().getString( "SQLBaseComponent.DEBUG_RUNNING_QUERY", query ) ); //$NON-NLS-1$
    } else {
      debug( Messages.getInstance().getString( "SQLBaseComponent.DEBUG_RUNNING_QUERY_TIMEOUT", query, "" + timeout ) ); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }
}
