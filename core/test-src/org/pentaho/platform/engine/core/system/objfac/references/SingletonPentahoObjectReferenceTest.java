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
