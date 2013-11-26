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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.repository2.unified.webservices;

import junit.framework.TestCase;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

/**
 * Test class for the KeyStringKeyValueDto class
 * 
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public class StringKeyStringValueDtoTest extends TestCase {
  @Test
  public void testInitialization() {
    final StringKeyStringValueDto empty = new StringKeyStringValueDto();
    assertNull( empty.getKey() );
    assertNull( empty.getValue() );
    assertTrue( empty.equals( new StringKeyStringValueDto() ) );
    assertEquals( empty.hashCode(), new StringKeyStringValueDto().hashCode() );
    assertFalse( StringUtils.isEmpty( empty.toString() ) );

    final StringKeyStringValueDto sample1 = new StringKeyStringValueDto();
    sample1.setKey( "test key" );
    sample1.setValue( "test value" );
    final StringKeyStringValueDto sample2 = new StringKeyStringValueDto( "test key", "test value" );
    assertTrue( sample1.equals( sample2 ) );
    assertEquals( sample1.hashCode(), sample2.hashCode() );

    sample1.setKey( null );
    sample2.setKey( null );
    assertTrue( sample1.equals( sample2 ) );
    assertEquals( sample1.hashCode(), sample2.hashCode() );

    sample1.setKey( sample1.getValue() );
    sample1.setValue( null );
    sample2.setKey( sample2.getValue() );
    sample2.setValue( null );
    assertTrue( sample1.equals( sample2 ) );
    assertEquals( sample1.hashCode(), sample2.hashCode() );

    sample1.setKey( null );
    assertTrue( empty.equals( sample1 ) );
    assertEquals( empty.hashCode(), sample1.hashCode() );

    sample1.setValue( "test" );
    assertFalse( empty.equals( sample1 ) );
    assertFalse( empty.equals( sample2 ) );
    assertFalse( empty.hashCode() == sample1.hashCode() );
    assertFalse( empty.hashCode() == sample2.hashCode() );

    assertFalse( empty.equals( null ) );
    assertFalse( empty.equals( sample1.toString() ) );
  }
}
