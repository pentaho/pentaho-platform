/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.plugin.action.datatransforms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.commons.connection.DataUtilities;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.engine.services.solution.ComponentBase;
import org.pentaho.platform.engine.services.solution.StandardSettings;
import org.pentaho.platform.plugin.action.messages.Messages;

import java.util.Set;

/**
 * 
 */
public class ResultSetExportComponent extends ComponentBase {
  /**
   * 
   */
  private static final long serialVersionUID = 3289900246113442203L;

  @Override
  public Log getLogger() {
    return LogFactory.getLog( ResultSetExportComponent.class );
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
    boolean hasResultSetParameter = isDefinedInput( "result-set" ); //$NON-NLS-1$ 
    if ( !hasResultSetParameter ) {
      error( Messages.getInstance().getString( "JFreeReport.ERROR_0022_DATA_INPUT_INVALID_OBJECT" ) ); //$NON-NLS-1$
      return false;
    }
    if ( getResultOutputName() == null ) {
      error( Messages.getInstance().getString( "JFreeReport.ERROR_0022_DATA_INPUT_INVALID_OBJECT" ) ); //$NON-NLS-1$
      return false;
    }
    return true;
  }

  @Override
  protected boolean executeAction() {
    Object resultSetObject = getInputValue( "result-set" ); //$NON-NLS-1$
    if ( resultSetObject instanceof IPentahoResultSet ) {
      IPentahoResultSet resultset = (IPentahoResultSet) resultSetObject;
      if ( getResultOutputName() != null ) {
        setOutputValue( getResultOutputName(), DataUtilities.getXMLString( resultset ) );
      }
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void done() {
    // TODO Auto-generated method stub
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
