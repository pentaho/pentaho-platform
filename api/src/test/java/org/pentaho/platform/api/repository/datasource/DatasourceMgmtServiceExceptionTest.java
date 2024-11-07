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

package org.pentaho.platform.api.repository.datasource;

import org.junit.jupiter.api.Test;

import static org.pentaho.platform.api.test.ExceptionTester.hasValidExceptionConstructors;

/**
 * Created by bgroves on 11/9/15.
 */
public class DatasourceMgmtServiceExceptionTest {

  @Test
  public void testExceptionClasses() {
    hasValidExceptionConstructors( DatasourceMgmtServiceException.class );
  }
}
