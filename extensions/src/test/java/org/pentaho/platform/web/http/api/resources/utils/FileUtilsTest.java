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
 * Copyright (c) 2002-2020 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.api.resources.utils;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.pentaho.platform.web.http.api.resources.utils.FileUtils.containsReservedCharacter;

/**
 * @author Andrey Khayrutdinov
 */
public class FileUtilsTest {

  private static final char[] TEST_RESERVED = new char[] {'/', '%'};

  @Test
  public void testCloseQuietly( ) {
    File newFile = new File( new File( "src/test/resources/newFile.txt" ).getAbsolutePath() );
    try {
      newFile.createNewFile();
      FileInputStream fileInputStream = new FileInputStream( newFile );
      fileInputStream = spy( fileInputStream );
      doThrow( new IOException() ).when( fileInputStream ).close();
      assertFalse( FileUtils.closeQuietly( fileInputStream ) );
      assertTrue( FileUtils.closeQuietly( new FileInputStream( newFile ) ) );
      doCallRealMethod().when( fileInputStream ).close();
      fileInputStream.close();
      assertTrue( newFile.delete() );
    } catch ( IOException e ) {
      fail();
    }
    assertFalse( FileUtils.closeQuietly( null ) );
  }

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
    assertEquals( "/", FileUtils.getParentPath( "/asdf/" ) );
    assertEquals( "/asdf", FileUtils.getParentPath( "/asdf/ghjk" ) );
    assertEquals( "/asdf", FileUtils.getParentPath( "/asdf/ghjk/" ) );
    assertEquals( "/asdf/ghjk", FileUtils.getParentPath( "/asdf/ghjk/zxcv" ) );
    assertEquals( "/as~!@#$%df/gh^&*()jk/zx_+ ,.cv", FileUtils.getParentPath( "/as~!@#$%df/gh^&*()jk/zx_+ ,.cv/bnm" ) );

    assertEquals( null, FileUtils.getParentPath( "asdf/" ) );
    assertEquals( "asdf", FileUtils.getParentPath( "asdf/ghjk" ) );
    assertEquals( "as~!@#$%df/gh^&*()jk/zx_+ ,.cv", FileUtils.getParentPath( "as~!@#$%df/gh^&*()jk/zx_+ ,.cv/bnm" ) );
  }

  @Test( expected = IllegalArgumentException.class )
  public void isRootLevelPath_Null() {
    FileUtils.isRootLevelPath( null );
  }

  @Test( expected = IllegalArgumentException.class )
  public void isRootLevelPath_Empty() {
    FileUtils.isRootLevelPath( "" );
  }

  @Test( expected = IllegalArgumentException.class )
  public void isRootLevelPath_NotAbsolute() {
    FileUtils.isRootLevelPath( "test" );
  }

  @Test
  public void isRootLevelPath_True() {
    assertEquals( true, FileUtils.isRootLevelPath( "/" ) );
    assertEquals( true, FileUtils.isRootLevelPath( "/asdf" ) );
    assertEquals( true, FileUtils.isRootLevelPath( "/asdf/" ) );
    assertEquals( true, FileUtils.isRootLevelPath( "/asdf/ghjk" ) );
    assertEquals( true, FileUtils.isRootLevelPath( "/asdf/ghjk/zxcv" ) );
    assertEquals( true, FileUtils.isRootLevelPath( "/as~!@#$%df/gh^&*()jk/zx_+ ,.cv/bnm" ) );

    assertEquals( true, FileUtils.isRootLevelPath( "/home6" ) );
    assertEquals( true, FileUtils.isRootLevelPath( "/home6/" ) );
    assertEquals( true, FileUtils.isRootLevelPath( "/public7" ) );
    assertEquals( true, FileUtils.isRootLevelPath( "/public7/" ) );
    assertEquals( true, FileUtils.isRootLevelPath( "/etc8" ) );
    assertEquals( true, FileUtils.isRootLevelPath( "/etc8/" ) );
  }

  @Test
  public void isRootLevelPath_Public() {
    assertEquals( true, FileUtils.isRootLevelPath( "/public" ) );
    assertEquals( true, FileUtils.isRootLevelPath( "/public/" ) );
    assertEquals( false, FileUtils.isRootLevelPath( "/public/ghjk" ) );
    assertEquals( false, FileUtils.isRootLevelPath( "/public/ghjk/zxcv" ) );
    assertEquals( false, FileUtils.isRootLevelPath( "/public/as~!@#$%df/gh^&*()jk/zx_+ ,.cv/bnm" ) );
  }

  @Test
  public void isRootLevelPath_Home() {
    assertEquals( true, FileUtils.isRootLevelPath( "/home" ) );
    assertEquals( true, FileUtils.isRootLevelPath( "/home/" ) );
    assertEquals( false, FileUtils.isRootLevelPath( "/home/ghjk" ) );
    assertEquals( false, FileUtils.isRootLevelPath( "/home/ghjk/zxcv" ) );
    assertEquals( false, FileUtils.isRootLevelPath( "/home/as~!@#$%df/gh^&*()jk/zx_+ ,.cv/bnm" ) );
  }

  @Test
  public void isRootLevelPath_Etc() {
    assertEquals( true, FileUtils.isRootLevelPath( "/etc" ) );
    assertEquals( true, FileUtils.isRootLevelPath( "/etc/" ) );
    assertEquals( false, FileUtils.isRootLevelPath( "/etc/ghjk" ) );
    assertEquals( false, FileUtils.isRootLevelPath( "/etc/ghjk/zxcv" ) );
    assertEquals( false, FileUtils.isRootLevelPath( "/etc/as~!@#$%df/gh^&*()jk/zx_+ ,.cv/bnm" ) );
  }

  @Test
  public void testContainsControlCharacters() {
    assertTrue( FileUtils.containsControlCharacters( "/home/Create Control Character \u0017 Folder" ) );
    assertFalse( FileUtils.containsControlCharacters( "/home/Create normal Folder" ) );
  }
}
