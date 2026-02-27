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


package org.pentaho.platform.plugin.action.hql;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.engine.services.solution.StandardSettings;
import org.pentaho.platform.plugin.action.messages.Messages;

import java.util.Set;

public class HQLLookupRule extends HQLBaseComponent {

  /**
   * 
   */
  private static final long serialVersionUID = 4633628885885427927L;

  /**
   * 
   */
  @Override
  public boolean validateSystemSettings() {
    // This component does not have any system settings to validate
    return true;
  }

  @Override
  public String getResultOutputName() {
    Set outputs = getOutputNames();
    if ( ( outputs == null ) || ( outputs.size() == 0 ) ) {
      // if( (outputs == null) || (outputs.size() == 0 ) ||
      // (outputs.size() > 1 ) ) {
      error( Messages.getInstance().getString( "Template.ERROR_0002_OUTPUT_COUNT_WRONG" ) ); //$NON-NLS-1$
      return null;
    }

    // Did we override the output name? // TODO Deprecation Warning
    String outputName = getInputStringValue( StandardSettings.OUTPUT_NAME );
    if ( ( outputName == null ) && outputs.contains( "query-result" ) ) { //$NON-NLS-1$ // Get the query-result node - This is the preferred method to use 
      outputName = "query-result"; //$NON-NLS-1$
    }

    if ( outputName == null ) { // Drop back to the old behavior
      outputName = (String) outputs.iterator().next();
      // TODO Deprecation Warning
    }
    return outputName;
  }

  @Override
  public Log getLogger() {
    return LogFactory.getLog( HQLLookupRule.class );
  }

  @Override
  public boolean init() {
    return true;
  }
}
