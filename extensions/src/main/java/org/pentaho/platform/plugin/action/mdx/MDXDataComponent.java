/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.plugin.action.mdx;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MDXDataComponent extends MDXBaseComponent {

  /**
   * 
   */
  private static final long serialVersionUID = -2067865149724338823L;

  @Override
  public boolean validateSystemSettings() {
    // This component does not have any system settings to validate
    return true;
  }

  @Override
  public Log getLogger() {
    return LogFactory.getLog( MDXDataComponent.class );
  }

  @Override
  public boolean init() {
    return true;
  }
}
