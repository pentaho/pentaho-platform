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
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.commons.connection.PentahoDataTransmuter;
import org.pentaho.platform.engine.services.solution.ComponentBase;
import org.pentaho.platform.engine.services.solution.StandardSettings;
import org.pentaho.platform.plugin.action.messages.Messages;

import java.util.Set;

/**
 * 
 * This class flattens an IPentahoResultSet by looking at a particular column.
 * 
 * 
 * The flattening is based on a particular column, as we build our consolidated row, we look at the this columns value
 * to create a new consolidated row when it changes.
 * 
 * eg)
 * 
 * Dec 20, 2005 3432 Dec 20, 2005 235 Dec 20, 2005 8568 Dec 20, 2005 5685 Dec 20, 2005 9873 Dec 29, 2005 24685 Dec 29,
 * 2005 12345 Dec 29, 2005 13151 Dec 29, 2005 12302 Dec 29, 2005 34772
 * 
 * Dec 20, 2005 3432 235 8568 5685 9873 Dec 29, 2005 24685 12345 13151 12302 34772
 */
public class ResultSetFlattenerComponent extends ComponentBase {
  /**
   * 
   */
  private static final String RESULT_SET = "result-set"; //$NON-NLS-1$

  private static final String FLATTEN_COLUMN = "flatten-column"; //$NON-NLS-1$

  private static final long serialVersionUID = 5969716585776621813L;

  @Override
  public Log getLogger() {
    return LogFactory.getLog( ResultSetFlattenerComponent.class );
  }

  @Override
  public boolean init() {
    // TODO Auto-generated method stub
    return true;
  }

  @Override
  protected boolean validateSystemSettings() {
    // This component does not have any system settings to validate
    return true;
  }

  @Override
  protected boolean validateAction() {
    if ( !isDefinedInput( ResultSetFlattenerComponent.RESULT_SET ) ) {
      error( Messages.getInstance().getString( "ResultSetFlattenerComponent.ERROR_0001_DATA_INPUT_INVALID_OBJECT" ) ); //$NON-NLS-1$
      return false;
    }
    if ( getResultOutputName() == null ) {
      error( Messages.getInstance().getString( "ResultSetFlattenerComponent.ERROR_0002_INVALID_OUTPUT" ) ); //$NON-NLS-1$
      return false;
    }
    if ( !isDefinedInput( ResultSetFlattenerComponent.FLATTEN_COLUMN ) ) {
      error( Messages.getInstance().getString( "ResultSetFlattenerComponent.ERROR_0003_INVALID_FLATTEN_COLUMN" ) ); //$NON-NLS-1$
      return false;
    }
    return true;
  }

  @Override
  protected boolean executeAction() {
    Object resultSetObject = getInputValue( ResultSetFlattenerComponent.RESULT_SET );
    if ( resultSetObject instanceof IPentahoResultSet ) {
      IPentahoResultSet resultset = (IPentahoResultSet) resultSetObject;
      int column = (int) getInputLongValue( ResultSetFlattenerComponent.FLATTEN_COLUMN, 0 );
      column--;
      if ( getResultOutputName() != null ) {
        setOutputValue( getResultOutputName(), PentahoDataTransmuter.flattenResultSet( resultset, column ) );
      }
    }
    return true;
  }

  @Override
  public void done() {
  }

  public String getResultOutputName() {
    Set outputs = getOutputNames();
    if ( ( outputs == null ) || ( outputs.size() == 0 ) ) {
      error( Messages.getInstance().getString( "Template.ERROR_0002_OUTPUT_COUNT_WRONG" ) ); //$NON-NLS-1$
      return null;
    }
    String outputName = null;
    try {
      outputName = getInputStringValue( StandardSettings.OUTPUT_NAME );
    } catch ( Exception e ) {
      //ignore
    }
    if ( outputName == null ) { // Drop back to the old behavior
      outputName = (String) outputs.iterator().next();
    }
    return outputName;
  }
}
