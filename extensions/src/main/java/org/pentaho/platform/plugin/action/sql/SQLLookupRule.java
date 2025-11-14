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


package org.pentaho.platform.plugin.action.sql;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.actionsequence.dom.IActionOutput;
import org.pentaho.actionsequence.dom.actions.AbstractRelationalDbAction;

public class SQLLookupRule extends SQLBaseComponent {

  private static final long serialVersionUID = 5299778034643663502L;

  @Override
  public Log getLogger() {
    return LogFactory.getLog( SQLLookupRule.class );
  }

  @Override
  public String getResultOutputName() {
    IActionOutput actionOutput = ( (AbstractRelationalDbAction) getActionDefinition() ).getOutputResultSet();
    return actionOutput != null ? actionOutput.getPublicName() : null;
    // return ((AbstractRelationalDbAction)getActionDefinition()).getOutputResultSetName();
  }

  @Override
  public boolean validateSystemSettings() {
    // This component does not have any system settings to validate
    return true;
  }
}
