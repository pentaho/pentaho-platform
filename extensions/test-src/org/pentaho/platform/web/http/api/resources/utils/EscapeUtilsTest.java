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
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */
package org.pentaho.platform.web.http.api.resources.utils;

import java.io.IOException;

import org.apache.commons.lang.StringEscapeUtils;
import org.codehaus.jackson.io.CharacterEscapes;
import org.eclipse.jetty.util.ajax.JSON;
import org.junit.Assert;
import org.junit.Test;
import org.pentaho.platform.web.http.api.resources.utils.EscapeUtils.HTMLCharacterEscapes;

public class EscapeUtilsTest {

  @Test
  public void testEscapeJsonOrRaw0() {
    final String src = "1";
    final String expect = "1";
    //Check test data
    Assert.assertEquals( "data", src, StringEscapeUtils.unescapeJava( expect ) );

    String actual = EscapeUtils.escapeJsonOrRaw( src );
    Assert.assertEquals( expect, actual );
  }

  @Test
  public void testEscapeJsonOrRaw1() {
    final String src = "[\"as<>df\",\"<xxx>\"]";
    final String expect = "[\"as\\u003C\\u003Edf\",\"\\u003Cxxx\\u003E\"]";
    //Check test data
    Assert.assertEquals( "data", JSON.toString( JSON.parse( src ) ), JSON.toString( JSON.parse( expect ) ) );

    String actual = EscapeUtils.escapeJsonOrRaw( src );
    Assert.assertEquals( expect, actual );
  }

  @Test
  public void testEscapeJsonOrRaw2() {
    final String src = "[\"asdf\",123,\"<html>\", \"f&g\"]";
    final String expect = "[\"asdf\",123,\"\\u003Chtml\\u003E\",\"f\\u0026g\"]";
    //Check test data
    Assert.assertEquals( "data", JSON.toString( JSON.parse( src ) ), JSON.toString( JSON.parse( expect ) ) );

    String actual = EscapeUtils.escapeJsonOrRaw( src );
    Assert.assertEquals( expect, actual );
  }

  @Test
  public void testEscapeJsonOrRaw3() {
    final String src = "{\"as&df\":\"<xxx>\", \"AS\" : \"zz\", \"X\":null}";
    final String expect = "{\"as\\u0026df\":\"\\u003Cxxx\\u003E\",\"AS\":\"zz\",\"X\":null}";
    //Check test data
    Assert.assertEquals( "data", JSON.toString( JSON.parse( src ) ), JSON.toString( JSON.parse( expect ) ) );

    String actual = EscapeUtils.escapeJsonOrRaw( src );
    Assert.assertEquals( expect, actual );
  }

  @Test
  public void testEscapeJsonOrRaw4() {
    final String src = "{\"as&df\":\"<xxx>\", \"A\\\"S\" : \"z\\\"z\", \"X\":[123,\"\",\">\\\\<\"]}";
    final String expect =
        "{\"as\\u0026df\":\"\\u003Cxxx\\u003E\",\"A\\u0022S\":\"z\\u0022z\",\"X\":[123,\"\",\"\\u003E\\\\\\u003C\"]}";
    //Check test data
    Assert.assertEquals( "data", JSON.toString( JSON.parse( src ) ), JSON.toString( JSON.parse( expect ) ) );

    String actual = EscapeUtils.escapeJsonOrRaw( src );
    Assert.assertEquals( expect, actual );
  }

  @Test
  public void testEscapeJsonOrRaw5() {
    final String src = "{as&df<html>\"\\123";
    final String expect = "{as\\u0026df\\u003Chtml\\u003E\\u0022\\\\123";
    //Check test data
    Assert.assertEquals( "data", src, StringEscapeUtils.unescapeJava( expect ) );

    String actual = EscapeUtils.escapeJsonOrRaw( src );
    Assert.assertEquals( expect, actual );
  }

  @Test
  public void testEscapeJson1() {
    final String src = "[\"as<>df\",\"<xxx>\"]";
    final String expect = "[\"as\\u003C\\u003Edf\",\"\\u003Cxxx\\u003E\"]";
    //Check test data
    Assert.assertEquals( "data", JSON.toString( JSON.parse( src ) ), JSON.toString( JSON.parse( expect ) ) );

    String actual = null;
    try {
      actual = EscapeUtils.escapeJson( src );
    } catch ( IOException e ) {
      e.printStackTrace();
      Assert.fail( "Exception is not expected: " + e.getClass().getName() + " " + e.getMessage() );
    }
    Assert.assertEquals( expect, actual );
  }

  @Test
  public void testEscapeRaw1() {
    final String src = "[\"as<>df\",\"<xxx>\"]";
    final String expect = "[\\u0022as\\u003C\\u003Edf\\u0022,\\u0022\\u003Cxxx\\u003E\\u0022]";
    //Check test data
    Assert.assertEquals( "data", src, StringEscapeUtils.unescapeJava( expect ) );

    String actual = EscapeUtils.escapeRaw( src );
    Assert.assertEquals( expect, actual );
  }

  @Test
  public void testEscapeJson5() {
    final String src = "{as&df<html>\"\\123";
    final String expect = "{as\\u0026df\\u003Chtml\\u003E\\u0022\\\\123";
    //Check test data
    Assert.assertEquals( "data", src, StringEscapeUtils.unescapeJava( expect ) );

    try {
      String actual = EscapeUtils.escapeJson( src );
      Assert.fail( "Exception expected. actual=" + actual );
    } catch ( Exception e ) {
      //expected
    }
  }

  @Test
  public void testEscapeRaw5() {
    final String src = "{as&df<html>\"\\123";
    final String expect = "{as\\u0026df\\u003Chtml\\u003E\\u0022\\\\123";
    //Check test data
    Assert.assertEquals( "data", src, StringEscapeUtils.unescapeJava( expect ) );

    String actual = EscapeUtils.escapeRaw( src );
    Assert.assertEquals( expect, actual );
  }

  @Test
  public void testHTMLCharacterEscapes() {
    HTMLCharacterEscapes ce = new EscapeUtils.HTMLCharacterEscapes();
    int[] actualEscapes = ce.getEscapeCodesForAscii();
    Assert.assertEquals( "<", CharacterEscapes.ESCAPE_STANDARD, actualEscapes[(int) '<'] );
    Assert.assertEquals( ">", CharacterEscapes.ESCAPE_STANDARD, actualEscapes[(int) '>'] );
    Assert.assertEquals( "&", CharacterEscapes.ESCAPE_STANDARD, actualEscapes[(int) '&'] );
    Assert.assertEquals( "\'", CharacterEscapes.ESCAPE_STANDARD, actualEscapes[(int) '\''] );
    Assert.assertEquals( "\"", CharacterEscapes.ESCAPE_STANDARD, actualEscapes[(int) '\"'] );

    int[] standardEscapes = CharacterEscapes.standardAsciiEscapesForJSON();
    for ( int i = 0; i < standardEscapes.length; i++ ) {
      if ( "<>&\'\"".indexOf( (char) i ) < 0 ) {
        Assert.assertEquals( "(char)" + i, standardEscapes[i], actualEscapes[i] );
      }
    }
    Assert.assertNull( ce.getEscapeSequence( 1 ) );
    Assert.assertNull( ce.getEscapeSequence( (int) '%' ) );
  }

}
