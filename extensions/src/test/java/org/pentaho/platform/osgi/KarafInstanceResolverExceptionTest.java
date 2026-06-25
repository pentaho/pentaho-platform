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



package org.pentaho.platform.osgi;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class KarafInstanceResolverExceptionTest {

  @Test
  public void test() {
    KarafInstanceResolverException ex = new KarafInstanceResolverException( "testMsg" );
    assertNotNull( ex );
  }
}
