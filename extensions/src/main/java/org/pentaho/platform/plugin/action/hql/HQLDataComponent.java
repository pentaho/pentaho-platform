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

public class HQLDataComponent extends HQLBaseComponent {

  /**
   * 
   */
  private static final long serialVersionUID = 3685952097719826928L;

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
    return null;
  }

  @Override
  public Log getLogger() {
    return LogFactory.getLog( HQLDataComponent.class );
  }

  @Override
  public boolean init() {
    return true;
  }
}
