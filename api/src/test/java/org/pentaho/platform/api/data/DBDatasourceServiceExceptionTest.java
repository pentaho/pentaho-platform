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


package org.pentaho.platform.api.data;

import static org.pentaho.platform.api.test.ExceptionTester.hasValidExceptionConstructors;
import org.junit.jupiter.api.Test;

public class DBDatasourceServiceExceptionTest {

  @Test
  public void test() {
    hasValidExceptionConstructors( DBDatasourceServiceException.class );
  }
}
