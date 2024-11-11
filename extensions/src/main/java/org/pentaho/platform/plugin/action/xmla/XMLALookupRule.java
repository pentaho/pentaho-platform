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


package org.pentaho.platform.plugin.action.xmla;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.plugin.action.messages.Messages;

import java.util.Set;

public class XMLALookupRule extends XMLABaseComponent {

  /**
   * 
   */
  private static final long serialVersionUID = 7178952532238358504L;

  @Override
  public boolean validateSystemSettings() {
    // This component does not have any system settings to validate
    return true;
  }

  @Override
  public String getResultOutputName() {
    Set outputs = getOutputNames();
    if ( ( outputs == null ) || ( outputs.size() == 0 ) ) {
      error( Messages.getInstance().getString( "Template.ERROR_0002_OUTPUT_COUNT_WRONG" ) ); //$NON-NLS-1$
      return null;
    }
    String outputName = (String) outputs.iterator().next();
    return outputName;
  }

  @Override
  public Log getLogger() {
    return LogFactory.getLog( XMLALookupRule.class );
  }

  @Override
  public boolean init() {
    return true;
  }
}
