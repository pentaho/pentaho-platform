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


package org.pentaho.platform.plugin.action.mdx;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MDXLookupRule extends MDXBaseComponent {

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
  public Log getLogger() {
    return LogFactory.getLog( MDXLookupRule.class );
  }

  @Override
  public boolean init() {
    return true;
  }
}
