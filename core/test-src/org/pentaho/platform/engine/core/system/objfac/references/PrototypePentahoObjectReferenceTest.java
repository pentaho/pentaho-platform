package org.pentaho.platform.engine.core.system.objfac.references;

import org.junit.Test;
import org.pentaho.platform.api.engine.IObjectCreator;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;

import java.util.UUID;

import static org.junit.Assert.assertNotSame;

/**
 * Created by nbaker on 4/16/14.
 */
public class PrototypePentahoObjectReferenceTest {
  @Test
  public void testReference() throws Exception {
    PrototypePentahoObjectReference<UUID> sessionRef =
      new PrototypePentahoObjectReference.Builder<UUID>( UUID.class ).creator(
        new IObjectCreator<UUID>() {
          @Override public UUID create( IPentahoSession session ) {
            return UUID.randomUUID();
          }
        }
      ).build();

    IPentahoSession s1 = new StandaloneSession( "joe" );
    IPentahoSession s2 = new StandaloneSession( "admin" );

    PentahoSessionHolder.setSession( s1 );
    UUID s1Uuid = sessionRef.getObject();

    PentahoSessionHolder.setSession( s2 );
    UUID s2Uuid = sessionRef.getObject();
    assertNotSame( s1Uuid, s2Uuid );

    PentahoSessionHolder.setSession( s1 );
    UUID s1UuidAgain = sessionRef.getObject();
    assertNotSame( s1Uuid, s1UuidAgain );

  }
}
