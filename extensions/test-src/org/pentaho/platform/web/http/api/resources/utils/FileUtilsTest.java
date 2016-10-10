/*
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
 * Copyright 2006 - 2015 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.web.http.api.resources.utils;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.pentaho.platform.web.http.api.resources.utils.FileUtils.containsReservedCharacter;

/**
 * @author Andrey Khayrutdinov
 */
public class FileUtilsTest {

  private static final char[] TEST_RESERVED = new char[] {'/', '%'};

  @Test
  public void containsReservedCharacter_Empty() {
    assertFalse( containsReservedCharacter( "", TEST_RESERVED ) );
  }

  @Test
  public void containsReservedCharacter_Root() {
    assertFalse( containsReservedCharacter( "/", TEST_RESERVED ) );
  }

  @Test
  public void containsReservedCharacter_Folder_NoSlashAtEnd_Ok() {
    assertFalse( containsReservedCharacter( "/a", TEST_RESERVED ) );
  }

  @Test
  public void containsReservedCharacter_Folder_NoSlashAtEnd_Bad() {
    assertTrue( containsReservedCharacter( "/a%", TEST_RESERVED ) );
  }

  @Test
  public void containsReservedCharacter_Folder_SlashAtEnd_Ok() {
    assertFalse( containsReservedCharacter( "/a/", TEST_RESERVED ) );
  }

  @Test
  public void containsReservedCharacter_Folder_SlashAtEnd_Bad() {
    assertTrue( containsReservedCharacter( "/a%/", TEST_RESERVED ) );
  }

  @Test
  public void containsReservedCharacter_File_Ok() {
    assertFalse( containsReservedCharacter( "/a/b/c/qwerty.txt", TEST_RESERVED ) );
  }

  @Test
  public void containsReservedCharacter_File_Bad() {
    assertTrue( containsReservedCharacter( "/a/b/c/qwerty%.txt", TEST_RESERVED ) );
  }

  @Test
  public void convertCommaSeparatedString() {
    String comma = ",";
    String string2 = "string2";
    String string1 = "string1";

    String commaSeparatedStr = string1 + comma + string2;
    String[] result = FileUtils.convertCommaSeparatedStringToArray( commaSeparatedStr );
    assertEquals( result.length, 2 );
    assertEquals( result[ 0 ], string1 );
    assertEquals( result[ 1 ], string2 );
  }

  @Test
  public void convertSingleString() {
    String string = "string";

    String[] result = FileUtils.convertCommaSeparatedStringToArray( string );
    assertEquals( result.length, 1 );
    assertEquals( result[ 0 ], string );
  }

  @Test( expected = IllegalArgumentException.class )
  public void convertEmptyString() {
    FileUtils.convertCommaSeparatedStringToArray( StringUtils.EMPTY );
  }

  @Test
  public void getParentPath_Ok() {
    assertEquals( null, FileUtils.getParentPath( "/" ) );
    assertEquals( "/", FileUtils.getParentPath( "/asdf" ) );
    assertEquals( "/asdf", FileUtils.getParentPath( "/asdf/" ) );
    assertEquals( "/asdf", FileUtils.getParentPath( "/asdf/ghjk" ) );
    assertEquals( "/asdf/ghjk", FileUtils.getParentPath( "/asdf/ghjk/zxcv" ) );
    assertEquals( "/as~!@#$%df/gh^&*()jk/zx_+ ,.cv", FileUtils.getParentPath( "/as~!@#$%df/gh^&*()jk/zx_+ ,.cv/bnm" ) );

    assertEquals( "asdf", FileUtils.getParentPath( "asdf/" ) );
    assertEquals( "asdf", FileUtils.getParentPath( "asdf/ghjk" ) );
    assertEquals( "as~!@#$%df/gh^&*()jk/zx_+ ,.cv", FileUtils.getParentPath( "as~!@#$%df/gh^&*()jk/zx_+ ,.cv/bnm" ) );
  }

  @Test( expected = IllegalArgumentException.class )
  public void getParentPath_IAE_0() {
    FileUtils.getParentPath( null );
  }

  @Test( expected = IllegalArgumentException.class )
  public void getParentPath_IAE_1() {
    FileUtils.getParentPath( "" );
  }

  @Test( expected = IllegalArgumentException.class )
  public void getParentPath_IAE_2() {
    FileUtils.getParentPath( "asdf" );
  }

  @Test
  public void isRootPath_Ok() {
    assertEquals( false, FileUtils.isRootPath( null ) );
    assertEquals( true, FileUtils.isRootPath( "/" ) );
    assertEquals( false, FileUtils.isRootPath( "/asdf" ) );
    assertEquals( false, FileUtils.isRootPath( "/asdf/ghjk" ) );
    assertEquals( false, FileUtils.isRootPath( "/asdf/" ) );
    assertEquals( false, FileUtils.isRootPath( "/asdf/ghjk/zxcv" ) );
    assertEquals( false, FileUtils.isRootPath( "/as~!@#$%df/gh^&*()jk/zx_+ ,.cv/bnm" ) );

    assertEquals( false, FileUtils.isRootPath( "asdf/" ) );
    assertEquals( false, FileUtils.isRootPath( "asdf/ghjk" ) );
    assertEquals( false, FileUtils.isRootPath( "as~!@#$%df/gh^&*()jk/zx_+ ,.cv/bnm" ) );

    assertEquals( false, FileUtils.isRootPath( "" ) );
    assertEquals( false, FileUtils.isRootPath( "asdf" ) );
  }

  @Test
  public void isRootLevelPath_Ok() {
    assertEquals( false, FileUtils.isRootLevelPath( null ) );
    assertEquals( false, FileUtils.isRootLevelPath( "/" ) );
    assertEquals( true, FileUtils.isRootLevelPath( "/asdf" ) );
    assertEquals( false, FileUtils.isRootLevelPath( "/asdf/" ) );
    assertEquals( false, FileUtils.isRootLevelPath( "/asdf/ghjk" ) );
    assertEquals( false, FileUtils.isRootLevelPath( "/asdf/ghjk/zxcv" ) );
    assertEquals( false, FileUtils.isRootLevelPath( "/as~!@#$%df/gh^&*()jk/zx_+ ,.cv/bnm" ) );

    assertEquals( false, FileUtils.isRootLevelPath( "asdf/" ) );
    assertEquals( false, FileUtils.isRootLevelPath( "asdf/ghjk" ) );
    assertEquals( false, FileUtils.isRootLevelPath( "as~!@#$%df/gh^&*()jk/zx_+ ,.cv/bnm" ) );

    assertEquals( false, FileUtils.isRootLevelPath( "" ) );
    assertEquals( false, FileUtils.isRootLevelPath( "asdf" ) );
  }

}
