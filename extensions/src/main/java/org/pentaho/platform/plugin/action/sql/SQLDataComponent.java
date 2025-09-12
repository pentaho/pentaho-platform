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

public class SQLDataComponent extends SQLLookupRule {

  /**
   * 
   */
  private static final long serialVersionUID = -4727770529593034479L;

  @Override
  public Log getLogger() {
    return LogFactory.getLog( SQLDataComponent.class );
  }

  @Override
  public String getResultOutputName() {
    return null;
  }
}
