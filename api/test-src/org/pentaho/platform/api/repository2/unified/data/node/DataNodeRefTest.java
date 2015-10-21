/*!
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
 * Copyright (c) 2002-2015 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.api.repository2.unified.data.node;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DataNodeRefTest {

  public static final String REF_ID = "refId";

  @Test
  public void testRef() {
    DataNodeRef ref = new DataNodeRef( REF_ID );
    assertEquals( REF_ID, ref.getId() );
    assertEquals( REF_ID, ref.toString() );

    System.out.println( ref.hashCode() );

    assertTrue( ref.equals( new DataNodeRef( REF_ID ) ) );
    assertTrue( ref.equals( ref ) );
    assertFalse( ref.equals( null ) );
    assertFalse( ref.equals( new DataNodeRef( "blah" ) ) );
    assertFalse( ref.equals( new String() ) );
  }
}
