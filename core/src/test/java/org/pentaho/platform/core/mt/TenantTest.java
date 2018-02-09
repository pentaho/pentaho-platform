/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.core.mt;

import org.junit.Test;

import static org.junit.Assert.*;

public class TenantTest {

  @Test
  public void tenantTest() {
    Tenant t = new Tenant();
    assertNull( t.getRootFolderAbsolutePath() );
    assertNull( t.getId() );
    assertNull( t.getName() );
    assertTrue( t.isEnabled() );
    assertEquals( t.toString(), "TENANT ID = null" );

    t.setRootFolderAbsolutePath( "C:/TestPath" );
    assertEquals( t.getRootFolderAbsolutePath(), "C:/TestPath" );
    assertEquals( t.getId(), "C:/TestPath" );
    assertEquals( t.getName(), "TestPath" );
    t.setEnabled( false );
    assertFalse( t.isEnabled() );
    assertEquals( t.toString(), "TENANT ID = C:/TestPath" );
    assertEquals( t.hashCode(), 31 * "C:/TestPath".hashCode() );
  }

  @Test
  public void tenantTestEquals() {
    Tenant t = new Tenant( "C:/TestPath", true );
    assertFalse( t.equals( null ) );
    assertFalse( t.equals( "C:/TestPath" ) );
    Tenant t2 = new Tenant( "C:/TestPath2", true );
    assertFalse( t.equals( t2 ) );
    t2.setRootFolderAbsolutePath( t.getRootFolderAbsolutePath() );
    assertTrue( t.equals( t2 ) );
    assertTrue( t.equals( t ) );
  }
}
