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

package org.pentaho.platform.api.action;

import static org.pentaho.platform.api.test.ExceptionTester.hasValidExceptionConstructors;

import org.junit.jupiter.api.Test;

public class ActionPreProcessingExceptionTest {

  @Test
  public void test() {
    hasValidExceptionConstructors( ActionPreProcessingException.class );
  }
}
