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

package org.pentaho.platform.api.repository2.unified.data.sample;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by bgroves on 11/6/15.
 */
public class SampleRepositoryFileDataTest {
  private static final String STRING = "String";
  private static final Boolean BOOLEAN = false;
  private static final Integer INTEGER = 10;

  @Test
  public void testGetters() {
    SampleRepositoryFileData file = new SampleRepositoryFileData( STRING, BOOLEAN, INTEGER );

    assertEquals( STRING, file.getSampleString() );
    assertEquals( BOOLEAN, file.getSampleBoolean() );
    assertTrue( INTEGER.equals( file.getSampleInteger() ) );
    assertEquals( STRING.length() + 2, file.getDataSize() );
  }
}
