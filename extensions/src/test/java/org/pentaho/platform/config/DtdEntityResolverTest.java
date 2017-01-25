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
