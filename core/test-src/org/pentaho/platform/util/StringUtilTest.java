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

package org.pentaho.platform.util;

import junit.framework.Assert;
import junit.framework.TestCase;

public class StringUtilTest extends TestCase {

  public void testTokenStringToArray() {

    String[] array = StringUtil.tokenStringToArray( "This is a test to convert this string into an array", " " ); //$NON-NLS-1$ //$NON-NLS-2$
    StringBuffer buffer = new StringBuffer();

    for ( String element : array ) {
      buffer.append( element.toString() );
      buffer.append( " " ); //$NON-NLS-1$
    }

    Assert.assertEquals( buffer.toString().trim(), "This is a test to convert this string into an array" ); //$NON-NLS-1$
  }

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

  public static void main( final String[] args ) {
    StringUtilTest test = new StringUtilTest();
    try {
      test.testTokenStringToArray();
      test.testDoesPathContainParentPathSegment();

    } finally {
    }

  }
}
