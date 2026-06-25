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



package org.pentaho.platform.api.action;

import static org.pentaho.platform.api.test.ExceptionTester.hasValidExceptionConstructors;

import org.junit.jupiter.api.Test;

public class ActionPreProcessingExceptionTest {

  @Test
  public void test() {
    hasValidExceptionConstructors( ActionPreProcessingException.class );
  }
}
