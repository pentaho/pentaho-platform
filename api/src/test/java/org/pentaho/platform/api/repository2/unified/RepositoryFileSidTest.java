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


package org.pentaho.platform.api.repository2.unified;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * Created by bgroves on 10/23/15.
 */
public class RepositoryFileSidTest {

  private static final String NAME = anyString();

  @Test
  public void testSid() {
    RepositoryFileSid sid = new RepositoryFileSid( NAME );

    assertEquals( NAME, sid.getName() );
    assertEquals( RepositoryFileSid.Type.USER, sid.getType() );
    assertNotEquals( 961, sid.hashCode() );
    assertNotNull( sid.toString() );

    try {
      RepositoryFileSid throwError = new RepositoryFileSid( null );
      fail( "Should of thrown an illegal argument exception" );
    } catch ( Exception e ) {
      // Pass
    }

    // Test variations of equals
    assertTrue( sid.equals( sid ) );
    assertFalse( sid.equals( null ) );
    assertFalse( sid.equals( new String() ) );

    RepositoryFileSid dupSid = new RepositoryFileSid( NAME );
    assertTrue( sid.equals( dupSid ) );

    RepositoryFileSid diffSid = new RepositoryFileSid( NAME, RepositoryFileSid.Type.ROLE );
    assertFalse( sid.equals( diffSid ) );
    diffSid = new RepositoryFileSid( "diffName" );
    assertFalse( sid.equals( diffSid ) );
  }
}
