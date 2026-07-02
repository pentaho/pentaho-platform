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

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;

/**
 * Created by bgroves on 10/22/15.
 */
public class RepositoryFileAceTest {
  @Test
  public void testAce() {
    try {
      new RepositoryFileAce( null, null );
      fail( "Should of failed with null parameters" );
    } catch ( Exception e ) {
      // Pass
    }

    RepositoryFileSid sid = mock( RepositoryFileSid.class );
    EnumSet<RepositoryFilePermission> permissions = EnumSet.of( RepositoryFilePermission.READ, RepositoryFilePermission.WRITE );

    RepositoryFileAce ace = new RepositoryFileAce( sid, RepositoryFilePermission.READ, RepositoryFilePermission.WRITE );
    assertEquals( sid, ace.getSid() );
    assertEquals( permissions, ace.getPermissions() );
    assertNotNull( ace.toString() );

    // All variations of equals check
    RepositoryFileAce aceDup = new RepositoryFileAce( sid, permissions );
    assertTrue( ace.equals( ace ) );
    assertTrue( ace.equals( aceDup ) );
    assertFalse( ace.equals( null ) );
    assertFalse( ace.equals( new String() ) );

    RepositoryFileSid notRepoFile = new RepositoryFileSid( "notRepoFile" );
    RepositoryFileAce aceNotDup = new RepositoryFileAce( notRepoFile, permissions );
    assertFalse( ace.equals( aceNotDup ) );
    aceNotDup = new RepositoryFileAce( notRepoFile, RepositoryFilePermission.READ );
    assertFalse( ace.equals( aceNotDup ) );

    assertNotEquals( 29791, ace.hashCode() );
  }
}
