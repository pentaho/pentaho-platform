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
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */
package org.pentaho.platform.repository2.unified.jcr;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mockStatic;

@RunWith( MockitoJUnitRunner.class )
public class DefaultPathConversionHelperTest {
  private DefaultPathConversionHelper converter;


  @Before
  public void setup() {
    converter = new DefaultPathConversionHelper();
  }


  @Test( expected = IllegalArgumentException.class )
  public void absToRelEmptyTest() {
    converter.absToRel( "" );
  }

  @Test( expected = IllegalArgumentException.class )
  public void absToRelNullTest() {
    converter.absToRel( null );
  }

  @Test( expected = IllegalArgumentException.class )
  public void absToRelStartsWithSeparatorTest() {
    converter.absToRel( "somepath/somefile" );
  }

  @Test
  public void absToRelNullTenantRootFolderTest() {
    try ( MockedStatic<ServerRepositoryPaths> srp = mockStatic( ServerRepositoryPaths.class ) ) {
      srp.when( ServerRepositoryPaths::getTenantRootFolderPath ).thenReturn( null );
      assertNull( converter.absToRel( "/somepath/somefile" ) );
    }
  }

  @Test
  public void absToRelWrongTenantRootFolderTest() {
    try ( MockedStatic<ServerRepositoryPaths> srp = mockStatic( ServerRepositoryPaths.class ) ) {
      srp.when( ServerRepositoryPaths::getTenantRootFolderPath ).thenReturn( "/somepath" );
      assertNull( converter.absToRel( "/otherpath/somefile" ) );
    }
  }

  @Test
  public void absToRelRootFolderTest() {
    try ( MockedStatic<ServerRepositoryPaths> srp = mockStatic( ServerRepositoryPaths.class ) ) {
      srp.when( ServerRepositoryPaths::getTenantRootFolderPath ).thenReturn( "/somepath" );
      assertEquals( RepositoryFile.SEPARATOR, converter.absToRel( "/somepath" ) );
    }
  }

  @Test
  public void absToRelTest() {
    try ( MockedStatic<ServerRepositoryPaths> srp = mockStatic( ServerRepositoryPaths.class ) ) {
      srp.when( ServerRepositoryPaths::getTenantRootFolderPath ).thenReturn( "/somepath" );
      assertEquals( "/other/thefile", converter.absToRel( "/somepath/other\\thefile" ) );
      assertEquals( "/other/filename", converter.absToRel( "/somepath/other\\filename" ) );
      assertEquals( "/other/filename", converter.absToRel( "/somepath\\other\\filename" ) );
      assertEquals( "/other/else/filename", converter.absToRel( "/somepath\\other\\else\\filename" ) );
    }
  }

  @Test( expected = IllegalArgumentException.class )
  public void relToAbsEmptyTest() {
    converter.relToAbs( "" );
  }

  @Test( expected = IllegalArgumentException.class )
  public void relToAbsNullTest() {
    converter.relToAbs( null );
  }

  @Test( expected = IllegalArgumentException.class )
  public void relToAbsStartsWithSeparatorTest() {
    converter.relToAbs( "somepath/somefile" );
  }

  @Test
  public void relToAbsTest() {
    try ( MockedStatic<ServerRepositoryPaths> srp = mockStatic( ServerRepositoryPaths.class ) ) {
      srp.when( ServerRepositoryPaths::getTenantRootFolderPath ).thenReturn( "/somepath" );
      assertEquals( "/somepath/somefile", converter.relToAbs( "/somefile" ) );
      assertEquals( "/somepath/somefolder/other/somefile/", converter.relToAbs( "/somefolder\\other/somefile/" ) );
      assertEquals( "/somepath/somefolder/other/somefile/", converter.relToAbs( "/somefolder\\other\\somefile\\" ) );
      assertEquals( "/somepath", converter.relToAbs( "/" ) );
    }
  }

}
