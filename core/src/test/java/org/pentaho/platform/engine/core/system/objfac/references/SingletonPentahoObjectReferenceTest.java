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


package org.pentaho.platform.engine.core.system.objfac.references;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertSame;

/**
 * Created by nbaker on 4/16/14.
 */
public class SingletonPentahoObjectReferenceTest {
  @Test
  public void testReference() throws Exception {

    SingletonPentahoObjectReference<UUID> sessionRef =
      new SingletonPentahoObjectReference.Builder<UUID>( UUID.class ).object( UUID.randomUUID() ).build();
    UUID s1Uuid = sessionRef.getObject();

    UUID s2Uuid = sessionRef.getObject();
    assertSame( s1Uuid, s2Uuid );

  }
}
