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


package org.pentaho.platform.util;

import org.pentaho.platform.api.action.IAction;

/**
 * A no-op test implementation of {@link IAction}
 */
public class MyTestAction implements IAction {

  public MyTestAction() {
  }

  public void execute() throws Exception {
    // do nothing
  }
}