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



package org.pentaho.platform.api.engine;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class IPentahoObjectReferenceTest {

  @Test
  public void testGetRanking() {
    @SuppressWarnings( "rawtypes" )
    IPentahoObjectReference reference = Mockito.mock( IPentahoObjectReference.class );
    Mockito.when( reference.getRanking() ).thenReturn( IPentahoObjectReference.DEFAULT_RANKING );
  }

}
