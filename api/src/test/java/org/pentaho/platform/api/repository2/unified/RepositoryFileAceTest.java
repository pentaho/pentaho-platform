/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright (c) 2002-2024 Hitachi Vantara. All rights reserved.
 *
 */

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
