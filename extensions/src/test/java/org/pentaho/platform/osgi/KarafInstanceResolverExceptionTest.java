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
