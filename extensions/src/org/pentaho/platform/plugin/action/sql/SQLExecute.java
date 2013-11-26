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
import org.apache.commons.logging.LogFactory;
import org.pentaho.actionsequence.dom.actions.SqlExecuteAction;
import org.pentaho.commons.connection.memory.MemoryMetaData;
import org.pentaho.commons.connection.memory.MemoryResultSet;
import org.pentaho.platform.engine.services.solution.ComponentBase;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.plugin.services.connections.sql.SQLConnection;

import java.sql.SQLException;
import java.util.StringTokenizer;

public class SQLExecute extends SQLLookupRule {

  private static final long serialVersionUID = 2480019361917802106L;

  @Override
  public Log getLogger() {
    return LogFactory.getLog( SQLExecute.class );
  }

  @Override
  public boolean validateAction() {
    boolean result = true;
    if ( !( getActionDefinition() instanceof SqlExecuteAction ) ) {
      error( Messages.getInstance().getErrorString(
          "ComponentBase.ERROR_0001_UNKNOWN_ACTION_TYPE", getActionDefinition().getElement().asXML() ) ); //$NON-NLS-1$
      result = false;
    } else {
      result = super.validateAction();
    }
    return result;
  }

  @Override
  protected boolean runQuery( final String rawQuery, final boolean live ) {
    SQLConnection conn = (SQLConnection) connection;
    return runSqlQuery( conn, rawQuery, live );
  }

  protected boolean runSqlQuery( final SQLConnection conn, String rawQuery, final boolean live ) {
    SqlExecuteAction sqlExecuteAction = (SqlExecuteAction) getActionDefinition();
    boolean executed = false;
    boolean continueOnException = sqlExecuteAction.getContinueOnException().getBooleanValue( false );
    String[] columnHeaders =
        new String[] { Messages.getInstance().getString( "SQLExecute.USER_AFFECTED_ROWS_COLUMN_NAME" ), //$NON-NLS-1$
          Messages.getInstance().getString( "SQLExecute.USER_AFFECTED_ROW_STATUS" ) //$NON-NLS-1$
        };
    MemoryMetaData metaData = new MemoryMetaData( new String[][] { columnHeaders }, null );
    metaData.setColumnTypes( new String[] { "int", "string" } ); //$NON-NLS-1$ //$NON-NLS-2$
    MemoryResultSet affectedRowsResultSet = new MemoryResultSet( metaData );
    String successMsg = Messages.getInstance().getString( "SQLExecute.USER_SUCCESS" ); //$NON-NLS-1$
    String failMsg = Messages.getInstance().getString( "SQLExecute.USER_FAILED" ); //$NON-NLS-1$
    try {
      if ( conn == null ) {
        error( Messages.getInstance().getErrorString( "SQLBaseComponent.ERROR_0007_NO_CONNECTION" ) ); //$NON-NLS-1$
        return false;
      }
      if ( !conn.initialized() ) {
        error( Messages.getInstance().getErrorString( "SQLBaseComponent.ERROR_0007_NO_CONNECTION" ) ); //$NON-NLS-1$
        return false;
      }

      if ( sqlExecuteAction.getForceSingleStatement().getBooleanValue( false ) ) {
        // Forces original execution path.
        //
        // This execution path should be used if the query
        // has a semi-colon in the text of the SQL statement.
        //
        // This is a legitimate condition if there is (for example)
        // a statement with a where-clause that has a semi-colon.
        //
        // e.g.: UPDATE sometable SET somecolumn='val1;val2' WHERE somecolumn='val3;val4'
        //
        // In this case, using StringTokenizer on semi-colon will result in multiple un-executable
        // statements - the whole thing will fail.
        //
        // This is (arguably) unlikely, but it is possible. That's why I've chosen to make sure
        // that there is a mechanism for instating the old behavior.
        //
        String query = applyInputsToFormat( rawQuery );
        if ( ComponentBase.debug ) {
          debug( Messages.getInstance().getString( "SQLBaseComponent.DEBUG_RUNNING_QUERY", query ) ); //$NON-NLS-1$
        }
        int affectedRows = conn.execute( query );
        executed = true;
        affectedRowsResultSet.addRow( new Object[] { new Integer( affectedRows ), successMsg } );
      } else {
        //
        // Multiple statement execute support provided by contribution from Melanie Crouch
        //
        rawQuery = SQLExecute.removeLineTerminators( rawQuery.trim() ).toString();

        // tokenize the rawQuery passed into method to find if there are multiple updates to be executed.
        StringTokenizer st =
            new StringTokenizer( rawQuery, sqlExecuteAction.getMultiStatementSeparator().getStringValue( ";" ) ); //$NON-NLS-1$

        while ( st.hasMoreTokens() ) {
          // set rawQuery equal to the nextToken.
          rawQuery = st.nextToken();
          String query = applyInputsToFormat( rawQuery.trim() );
          if ( ComponentBase.debug ) {
            debug( Messages.getInstance().getString( "SQLBaseComponent.DEBUG_RUNNING_QUERY", query ) ); //$NON-NLS-1$
          }
          try {
            int affectedRows = conn.execute( query );
            // Normally, we'd check to see if the execution resulted in
            // some updated rows.
            affectedRowsResultSet.addRow( new Object[] { new Integer( affectedRows ), successMsg } );
            executed = true;
            debug( Messages.getInstance().getString( "SQLBaseComponent.DEBUG_UPDATED_QUERY", query ) ); //$NON-NLS-1$
          } catch ( SQLException e ) {
            error( Messages.getInstance().getErrorString(
                "SQLBaseComponent.ERROR_0006_EXECUTE_FAILED", getActionName() + " : " + e.getLocalizedMessage() ) ); //$NON-NLS-1$ //$NON-NLS-2$
            executed = continueOnException;
            if ( !continueOnException ) {
              break;
            }
            addErrorCode( affectedRowsResultSet, e, failMsg );
          }
        } // end while tokenizer
      }
      if ( getResultOutputName() != null ) {
        setOutputValue( this.getResultOutputName(), affectedRowsResultSet );
      }
    } catch ( SQLException e ) {
      error( Messages.getInstance().getErrorString(
          "SQLBaseComponent.ERROR_0006_EXECUTE_FAILED", getActionName() + " : " + e.getLocalizedMessage() ) ); //$NON-NLS-1$ //$NON-NLS-2$
      executed = continueOnException;
      addErrorCode( affectedRowsResultSet, e, e.getLocalizedMessage() );
    } finally {  // moved finally after last catch so one connection could be used to execute multiple updates.

      //
      // No matter what, make sure the connection
      // gets closed. Otherwise, the connection can
      // (ok, will) get stranded eating up resources
      // on the server. This is important.
      //

      if ( connectionOwner ) {
        conn.close();
      }
    }
    return executed;
  }

  public void addErrorCode( final MemoryResultSet affectedRowsResultSet, final SQLException e, final String failMsg ) {
    int eCode = e.getErrorCode();
    if ( eCode > 0 ) {
      eCode *= -1; // Make sure that error code results are negative.
    }
    affectedRowsResultSet.addRow( new Object[] { new Integer( eCode ), e.getLocalizedMessage() } );
  }

  public static String removeLineTerminators( final String inputStr ) {
    char[] rtn = new char[inputStr.length()];
    char ch;
    for ( int i = 0; i < inputStr.length(); i++ ) {
      ch = inputStr.charAt( i );
      switch ( ch ) {
        case '\r':
        case '\n':
          rtn[i] = ' ';
          break;
        default:
          rtn[i] = ( ch );
      }
    }
    return new String( rtn );
  }
}
