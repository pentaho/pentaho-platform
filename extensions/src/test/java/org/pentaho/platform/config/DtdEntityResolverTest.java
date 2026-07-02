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


package org.pentaho.platform.config;

import org.junit.Test;
import org.xml.sax.InputSource;

import static org.junit.Assert.*;

/**
 * Created by rfellows on 10/21/15.
 */
public class DtdEntityResolverTest {

  @Test
  public void testResolveEntity() throws Exception {
    DtdEntityResolver entityResolver = new DtdEntityResolver();

    InputSource inputSource = entityResolver.resolveEntity( "id", "system/sid" );
    assertNull( inputSource );
  }
}
