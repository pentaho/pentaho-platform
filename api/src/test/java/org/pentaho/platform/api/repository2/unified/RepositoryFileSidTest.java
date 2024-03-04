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
