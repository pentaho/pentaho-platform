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

import org.junit.Assert;
import org.junit.Test;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Unit tests for the {@link org.pentaho.platform.util.StringUtil} class.
 */
public class StringUtilTest {

  @Test
  public void testTrim() {
    Assert.assertEquals( StringUtil.trimToEmpty( " foo " ), "foo" );
    Assert.assertEquals( StringUtil.trimToEmpty(  "\n foo \t" ), "foo" );
    for ( int i = 0; i <= 32; i++ ) {
      Assert.assertEquals( StringUtil.trimToEmpty( (char) i  + "foo" ), "foo" );
    }
    Assert.assertEquals( StringUtil.trimToEmpty( "" ), "" );
    Assert.assertEquals( StringUtil.trimToEmpty( null ), "" );
  }

  @Test
  public void testTrimStringMap() {

    Map<String, String> nullMap = null;
    Assert.assertNull( StringUtil.trimStringMap( nullMap ) );

    Map<String, String> stringMap = new HashMap<>();
    stringMap.put( " spaces ", " spacesval " );
    stringMap.put( "\t tabs \t", "\t tabsval \t" );
    stringMap.put( "\n newline \n", "\n newlineval \n" );
    stringMap.put( "empty", "" );
    stringMap.put( "null", null );
    stringMap.put( null, null );
    stringMap = StringUtil.trimStringMap( stringMap );

    Assert.assertEquals( stringMap.get( "spaces" ), "spacesval" );
    Assert.assertEquals( stringMap.get( "tabs" ), "tabsval" );
    Assert.assertEquals( stringMap.get( "newline" ), "newlineval" );
    Assert.assertEquals( stringMap.get( "empty" ), "" );
    Assert.assertEquals( stringMap.get( "null" ), "" );
    Assert.assertEquals( stringMap.get( "" ), "" );
  }

  @Test
  public void testTrimStringList() {
    List<String> stringList = new ArrayList<>();
    for ( int i = 0; i <= 32; i++ ) {
      stringList.add( (char) i  + "foo" );
    }
    stringList.add( "" );
    stringList.add( null );
    stringList = StringUtil.trimStringList( stringList );
    for ( int i = 0; i <= 32; i++ ) {
      Assert.assertEquals( stringList.get( i ), "foo" );
    }
    Assert.assertEquals( stringList.get( 33 ), "" );
    Assert.assertEquals( stringList.get( 34 ), "" );
    Assert.assertEquals( StringUtil.trimStringList( null ), null );
    List<String> empty = new ArrayList();
    Assert.assertEquals( StringUtil.trimStringList( empty ), empty );
  }

  @Test
  public void testTokenStringToArray() {

    String token = " "; //$NON-NLS-2$
    String tokenString = "This is a test to convert this string into an array"; //$NON-NLS-2$
    String[] array = StringUtil.tokenStringToArray( tokenString, token );
    StringBuilder buffer = new StringBuilder();

    for ( String element : array ) {
      buffer.append( element );
      buffer.append( token );
    }
    Assert.assertEquals( buffer.toString().trim(), tokenString );

    // when tokenizedString is null, the result should be null
    Assert.assertNull( StringUtil.tokenStringToArray( null, token ) );
    Assert.assertNull( StringUtil.tokenStringToArray( null, null ) );

    // when no token is provided, we should get back an array containing the entire input token as a single string
    tokenString = "some text"; //$NON-NLS-2$
    array = StringUtil.tokenStringToArray( tokenString, null );
    Assert.assertNotNull( array );
    Assert.assertEquals( 1, array.length );
    Assert.assertEquals( tokenString, array[ 0 ] );
  }

  @Test
  public void testDoesPathContainParentPathSegment() {

    String[] matchStrings = { "../bart/maggie.xml", //$NON-NLS-1$
      "/bart/..", //$NON-NLS-1$
      "/bart/../maggie/homer.xml", //$NON-NLS-1$
      "..//bart/maggie.xml", //$NON-NLS-1$
      "/bart//..", //$NON-NLS-1$
      "/bart//../maggie/homer.xml", //$NON-NLS-1$
      "/bart/judi" + '\0' + ".xml", //$NON-NLS-1$ //$NON-NLS-2$
      "/../", //$NON-NLS-1$
      "/..", //$NON-NLS-1$
      "../", //$NON-NLS-1$
      "..\\bart\\maggie.xml", //$NON-NLS-1$
      "\\bart\\..", //$NON-NLS-1$
      "\\bart\\" + '\0' + ".png", //$NON-NLS-1$ //$NON-NLS-2$
      "\\bart\\..\\maggie\\homer.xml", //$NON-NLS-1$
      ".." //$NON-NLS-1$
    };

    for ( String testString : matchStrings ) {
      boolean matches = StringUtil.doesPathContainParentPathSegment( testString );
      Assert.assertTrue( matches );
    }

    String[] noMatchStrings = { "I am clean", //$NON-NLS-1$
      "/go/not/parent.xml", //$NON-NLS-1$
      "\\go\\not\\parent.xml", //$NON-NLS-1$
      "should/not..not/match", //$NON-NLS-1$
      "should/not%0Dnot/match", //$NON-NLS-1$
      "..should/not/match.xml", //$NON-NLS-1$
      "should/not..", //$NON-NLS-1$
      "..." //$NON-NLS-1$
    };

    for ( String testString : noMatchStrings ) {
      boolean matches = StringUtil.doesPathContainParentPathSegment( testString );
      Assert.assertFalse( matches );
    }
  }

  @Test
  public void testMain() {
    try {
      StringUtil.main( null );
      Assert.assertTrue( true );
    } catch ( final Exception e ) {
      Assert.fail( "Exception occured: " + e );
    }
  }

  @Test
  public void testIsEmpty() {
    Assert.assertTrue( StringUtil.isEmpty( null ) );
    Assert.assertTrue( StringUtil.isEmpty( "" ) );
    Assert.assertFalse( StringUtil.isEmpty( " " ) );
    Assert.assertFalse( StringUtil.isEmpty( "foo" ) );
  }

  @Test
  public void testGetMapAsPrettyStringEmpty() {
    // When the provided map is empty or null, the result should be an empty string
    Assert.assertEquals( "Map = null" + System.getProperty( "line.separator" ), StringUtil.getMapAsPrettyString( null
    ) );
    Assert.assertEquals( "Map = " + System.getProperty( "line.separator" ) + "{" + System.getProperty( "line"
      + ".separator" ) + "} java.util.HashMap" + System.getProperty( "line.separator" ), StringUtil
      .getMapAsPrettyString( new HashMap() ) );
  }

  @Test
  public void testGetMapAsPrettyString() {
    final Map testMap = getTestMap();
    final String output = StringUtil.getMapAsPrettyString( testMap );

    final String expectedOutput = getExpectedPrettyMapOutput();
    Assert.assertEquals( expectedOutput, output );
  }

  private String getExpectedPrettyMapOutput() {

    final String NL = System.getProperty( "line.separator" );
    final StringBuilder expectedOutput = new StringBuilder();
    expectedOutput.append( "Map = " ).append( NL );
    expectedOutput.append( "{" ).append( NL );
    expectedOutput.append( "    " ).append( " =  java.lang.String" ).append( NL );
    expectedOutput.append( "    " ).append( "null" ).append( " =  java.lang.String" ).append( NL );
    expectedOutput.append( "    " ).append( "Suzy" ).append( " = " ).append( "null" ).append( NL );
    expectedOutput.append( "    " ).append( "John" ).append( " = " ).append( "Doe java.lang.String" ).append( NL );
    expectedOutput.append( "    " ).append( "map" ).append( " = " ).append( NL );
    expectedOutput.append( "    " ).append( "{" ).append( NL );
    expectedOutput.append( "        " ).append( "John" ).append( " = " ).append( "Doe java.lang.String" ).append( NL );
    expectedOutput.append( "        " ).append( "testObj" ).append( " = " ).append( "someVar:testVar org.pentaho"
      + ".platform.util.StringUtilTest$TestObject" ).append( NL );
    expectedOutput.append( "    " ).append( "} java.util.TreeMap" ).append( NL );
    expectedOutput.append( "    " ).append( "testObj2" ).append( " = " ).append( "someVar:testVar2 org.pentaho"
      + ".platform.util.StringUtilTest$TestObject" );
    expectedOutput.append( NL ).append( "} java.util.HashMap" ).append( NL );
    return expectedOutput.toString();
  }

  private static Map getTestMap() {

    final Map subMap = new TreeMap();
    subMap.put( "John", "Doe" );
    subMap.put( "testObj", new TestObject( "testVar" ) );

    final Map testMap = new HashMap();
    testMap.put( "", "" );
    testMap.put( null, "" );
    testMap.put( "Suzy", null );
    testMap.put( "John", "Doe" );
    testMap.put( "map", subMap );
    testMap.put( "testObj2", new TestObject( "testVar2" ) );
    return testMap;
  }

  static class TestObject {
    private String someVar;

    TestObject( final String someVar ) {
      this.someVar = someVar;
    }

    public String toString() {
      return "someVar:" + this.someVar;
    }
  }
}
