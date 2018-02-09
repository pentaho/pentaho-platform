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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.config;

import org.junit.Test;
import org.pentaho.test.BeanTester;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by rfellows on 10/20/15.
 */
public class AclEntryTest extends BeanTester {
  public AclEntryTest() {
    super( AclEntry.class );
  }

  @Override
  @Test
  public void testHasValidBeanToString() {
    AclEntry aclEntry = new AclEntry( "admin", "publish" );
    assertEquals( "SERVICE NAME = admin ATTRIBUTE NAME =   publish", aclEntry.toString() );
  }
  @Override
  @Test
  public void testHasValidBeanEquals() {
    AclEntry aclEntry1 = new AclEntry( "admin", "publish" );
    AclEntry aclEntry2 = new AclEntry( "admin", "publish" );
    AclEntry aclEntry3 = new AclEntry( "admin", "write" );
    assertTrue( aclEntry1.equals( aclEntry2 ) );
    assertFalse( aclEntry1.equals( aclEntry3 ) );
  }
}
