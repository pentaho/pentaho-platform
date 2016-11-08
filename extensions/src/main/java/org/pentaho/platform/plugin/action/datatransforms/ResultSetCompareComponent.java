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

package org.pentaho.platform.plugin.action.datatransforms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.actionsequence.dom.ActionInputConstant;
import org.pentaho.actionsequence.dom.IActionOutput;
import org.pentaho.actionsequence.dom.actions.ResultSetCompareAction;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.engine.services.solution.ComponentBase;
import org.pentaho.platform.plugin.action.messages.Messages;

public class ResultSetCompareComponent extends ComponentBase {

  private static final long serialVersionUID = -1449352563247459588L;

  private static final String COMPARE_RESULT_OK = "No Mismatches"; //$NON-NLS-1$

  @Override
  protected boolean validateAction() {

    boolean actionValidated = true;
    ResultSetCompareAction compareAction = null;

    // get report connection setting
    if ( getActionDefinition() instanceof ResultSetCompareAction ) {
      compareAction = (ResultSetCompareAction) getActionDefinition();
      if ( compareAction.getResultSet1() == ActionInputConstant.NULL_INPUT ) {
        actionValidated = false;
        error( Messages.getInstance().getErrorString( "ResultSetCompareComponent.ERROR_0001_INPUT_RS1_UNDEFINED" ) ); //$NON-NLS-1$
      }

      if ( actionValidated && ( compareAction.getResultSet2() == ActionInputConstant.NULL_INPUT ) ) {
        actionValidated = false;
        error( Messages.getInstance().getErrorString( "ResultSetCompareComponent.ERROR_0002_INPUT_RS2_UNDEFINED" ) ); //$NON-NLS-1$
      }

      if ( actionValidated && ( compareAction.getCompareColumnNum() == ActionInputConstant.NULL_INPUT ) ) {
        actionValidated = false;
        error( Messages.getInstance().getErrorString( "ResultSetCompareComponent.ERROR_0003_COLUMN_UNDEFINED" ) ); //$NON-NLS-1$        
      }
    } else {
      actionValidated = false;
      error( Messages.getInstance().getErrorString(
          "ComponentBase.ERROR_0001_UNKNOWN_ACTION_TYPE", getActionDefinition().getElement().asXML() ) ); //$NON-NLS-1$      
    }

    return actionValidated;
  }

  @Override
  protected boolean validateSystemSettings() {
    // No system settings
    return true;
  }

  @Override
  public void done() {
    // No cleanup necessary
  }

  @Override
  protected boolean executeAction() throws Throwable {
    ResultSetCompareAction compareAction = (ResultSetCompareAction) getActionDefinition();

    Object obj1 = compareAction.getResultSet1().getValue();
    if ( !( obj1 instanceof IPentahoResultSet ) ) {
      error( Messages.getInstance().getErrorString( "ResultSetCompareComponent.ERROR_0004_INPUT_RS1_NOT_RS" ) ); //$NON-NLS-1$
      return false;
    }

    Object obj2 = compareAction.getResultSet2().getValue();
    if ( !( obj2 instanceof IPentahoResultSet ) ) {
      error( Messages.getInstance().getErrorString( "ResultSetCompareComponent.ERROR_0005_INPUT_RS2_NOT_RS" ) ); //$NON-NLS-1$
      return false;
    }

    IPentahoResultSet rs1 = (IPentahoResultSet) compareAction.getResultSet1().getValue();
    IPentahoResultSet rs2 = (IPentahoResultSet) compareAction.getResultSet2().getValue();

    String tempOutputMismatches = compareAction.getOutputMismatches().getStringValue();
    boolean outputMismatches = false;
    if ( ( tempOutputMismatches != null ) && tempOutputMismatches.trim().toLowerCase().equals( "true" ) ) { //$NON-NLS-1$
      outputMismatches = true;
    }

    boolean stopOnError = false;
    String tempStopOnError = compareAction.getStopOnError().getStringValue();
    if ( ( tempStopOnError != null ) && tempStopOnError.trim().toLowerCase().equals( "true" ) ) { //$NON-NLS-1$
      stopOnError = true;
    }

    int compareCol = Integer.parseInt( compareAction.getCompareColumnNum().getStringValue() );

    return compareEquals( rs1, rs2, compareCol, outputMismatches, stopOnError );
  }

  private boolean compareEquals( final IPentahoResultSet rs1, final IPentahoResultSet rs2, final int compareCol,
      boolean outputMismatches, final boolean stopOnError ) {
    int sourceRowCount = rs1.getRowCount();
    int sourceColCount = rs1.getColumnCount();
    int compRowCount = rs2.getRowCount();
    int compColCount = rs2.getColumnCount();
    StringBuffer outputBuf = new StringBuffer();
    if ( !outputMismatches ) {
      if ( sourceRowCount != compRowCount ) {
        error( Messages.getInstance().getErrorString( "ResultSetCompareComponent.ERROR_0006_RESULTSETS_ROWCOUNT_WRONG" ) ); //$NON-NLS-1$
        return false;
      }
      if ( sourceColCount != compColCount ) {
        error( Messages.getInstance().getErrorString(
            "ResultSetCompareComponent.ERROR_0007_RESULTSETS_COLUMNCOUNT_WRONG" ) ); //$NON-NLS-1$
        return false;
      }
    }
    if ( compareCol > sourceColCount ) {
      error( Messages.getInstance().getErrorString( "ResultSetCompareComponent.ERROR_0008_COLUMN_NOT_FOUND" ) + compareCol ); //$NON-NLS-1$
      return false;
    }
    if ( compareCol > compColCount ) {
      error( Messages.getInstance().getErrorString( "ResultSetCompareComponent.ERROR_0009_COMPARISON_COLUMN_NOT_FOUND" ) + compareCol ); //$NON-NLS-1$
      return false;
    }
    boolean anyMismatches = false;
    boolean foundIt;
    Object srcValue = null, compValue = null;
    ResultSetCompareAction compareAction = (ResultSetCompareAction) getActionDefinition();
    IActionOutput output = compareAction.getOutputCompareResult();
    for ( int sourceRows = 0; sourceRows < sourceRowCount; sourceRows++ ) {
      foundIt = false;
      srcValue = rs1.getValueAt( sourceRows, compareCol );
      // n+1 traversal. This accommodates non-ordered input
      for ( int compRows = 0; compRows < compRowCount; compRows++ ) {
        compValue = rs2.getValueAt( compRows, compareCol );
        if ( compValue.equals( srcValue ) ) {
          foundIt = true;
          break;
        }
      }
      if ( !foundIt ) {
        if ( outputBuf.length() > 0 ) {
          outputBuf.append( "," ).append( srcValue.toString().trim() ); //$NON-NLS-1$
        } else {
          outputBuf.append( srcValue.toString().trim() );
        }
        if ( output != null ) {
          output.setValue( outputBuf.toString() );
        }
        if ( outputMismatches ) {
          error( Messages.getInstance().getErrorString(
              "ResultSetCompareComponent.ERROR_0010_MISMATCH_OUTPUT", srcValue.toString() ) ); //$NON-NLS-1$
          anyMismatches = true;
        } else {
          if ( stopOnError ) {
            return false;
          }
        }
      }
    }
    if ( !anyMismatches ) {
      if ( output != null ) {
        output.setValue( ResultSetCompareComponent.COMPARE_RESULT_OK );
      }
    }
    return stopOnError ? !anyMismatches : true;
  }

  @Override
  public boolean init() {
    // no initialization required
    return true;
  }

  @Override
  public Log getLogger() {
    return LogFactory.getLog( ResultSetCompareComponent.class );
  }

}
