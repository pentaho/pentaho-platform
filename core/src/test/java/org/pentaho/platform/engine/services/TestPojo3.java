/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.platform.engine.services;

import org.pentaho.platform.api.engine.ISimplePojoComponent;

public class TestPojo3 implements ISimplePojoComponent {

  public boolean execute() throws Exception {

    // we should not get here
    PojoComponentTest.executeCalled = true;

    return true;
  }

  public boolean validate() throws Exception {
    PojoComponentTest.validateCalled = true;
    return false;
  }

}
