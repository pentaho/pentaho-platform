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

package org.pentaho.platform.util;

import junit.framework.Assert;

import org.junit.Test;

public class RepositoryPathEncoderTest {
  @Test
  public void testEncode() {
    Assert.assertEquals( "%3Apublic%3ASteel%20Wheels%3AFile%09WithColon", RepositoryPathEncoder
        .encode( ":public:Steel Wheels:File\tWithColon" ) );

    Assert.assertEquals( "%3Apublic%3ASteel%20Wheels%3AFile%09With%255CColon", RepositoryPathEncoder
        .encode( ":public:Steel Wheels:File\tWith%5CColon" ) );
  }

  @Test
  public void testEncodeRepositoryPath() {
    Assert.assertEquals( ":public:Steel Wheels:File\tWithColon", RepositoryPathEncoder
        .encodeRepositoryPath( "/public/Steel Wheels/File:WithColon" ) );
  }
  
  @Test
  public void testDecodeRepositoryPath() {
    Assert.assertEquals( "/public/Steel Wheels/File:WithColon", RepositoryPathEncoder
        .decodeRepositoryPath( ":public:Steel Wheels:File\tWithColon" ) );
  }
}
