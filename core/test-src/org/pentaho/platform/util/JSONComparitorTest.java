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
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.util;

import junit.framework.TestCase;
import org.codehaus.jettison.json.JSONException;
import org.junit.Test;

public class JSONComparitorTest extends TestCase {
  @Test
  public static void testJSONComparator() {
    // A JSON string that could be returned by ModelSerializer
    String json1 =
      "{\"MQLQuery\":{\"cols\":[{\"Column\":{\"id\":\"mycolumn\"}}],\"conds\":[{\"condition\":{\"@operator\":\"=\","
        + "\"@value\":\"myvalue1\"}},{\"condition\":{\"@operator\":\"in\",\"@value\":\"myvalue2\"}}]}}";
    // Same as json1 except this string has the order of some entries switched.
    String json2 =
      "{\"MQLQuery\":{\"cols\":[{\"Column\":{\"id\":\"mycolumn\"}}],"
        + "\"conds\":[{\"condition\":{\"@value\":\"myvalue1\",\"@operator\":\"=\"}},"
        + "{\"condition\":{\"@operator\":\"in\",\"@value\":\"myvalue2\"}}]}}";
    // Same as json1 except cols array is removed
    String json3 =
      "{\"MQLQuery\":{\"cols\":{\"Column\":{\"id\":\"mycolumn\"}},\"conds\":[{\"condition\":{\"@operator\":\"=\","
        + "\"@value\":\"myvalue1\"}},{\"condition\":{\"@operator\":\"in\",\"@value\":\"myvalue2\"}}]}}";
    // Change element value
    String json4 =
      "{\"MQLQuery\":{\"cols\":[{\"Column\":{\"id\":\"mycolumnx\"}}],\"conds\":[{\"condition\":{\"@operator\":\"=\","
        + "\"@value\":\"myvalue1\"}},{\"condition\":{\"@operator\":\"in\",\"@value\":\"myvalue2\"}}]}}";
    // Reverse cols/conditions
    String json5 =
      "{\"MQLQuery\":{\"conds\":[{\"condition\":{\"@operator\":\"=\",\"@value\":\"myvalue1\"}},"
        + "{\"condition\":{\"@operator\":\"in\",\"@value\":\"myvalue2\"}}],"
        + "\"cols\":[{\"Column\":{\"id\":\"mycolumn\"}}]}}";
    // Array order of conditions
    String json6 =
      "{\"MQLQuery\":{\"cols\":[{\"Column\":{\"id\":\"mycolumn\"}}],\"conds\":[{\"condition\":{\"@operator\":\"in\","
        + "\"@value\":\"myvalue2\"}},{\"condition\":{\"@operator\":\"=\",\"@value\":\"myvalue1\"}}]}}";
    // Change label name
    String json7 =
      "{\"MQLQuery\":{\"cols\":[{\"Columnx\":{\"id\":\"mycolumn\"}}],\"conds\":[{\"condition\":{\"@operator\":\"=\","
        + "\"@value\":\"myvalue1\"}},{\"condition\":{\"@operator\":\"in\",\"@value\":\"myvalue2\"}}]}}";
    // omit @Operator Element
    String json8 =
      "{\"MQLQuery\":{\"cols\":[{\"Column\":{\"id\":\"mycolumn\"}}],"
        + "\"conds\":[{\"condition\":{\"@value\":\"myvalue1\"}},{\"condition\":{\"@operator\":\"in\","
        + "\"@value\":\"myvalue2\"}}]}}";
    // Malformed JSON
    String json9 =
      "{\"MQLQuery\":{\"cols\":[{\"Column\":{\"id\":\"mycolumn\"}}],\"conds\":[{\"condition\":{\"@operator\":\"=\","
        + "\"@value\":\"myvalue1\"}},{\"condition\":{\"@operator\":\"in\",\"@value\":\"myvalue2\"}}]}";

    try {
      assertTrue( "Equals Test", JSONComparitor.jsonEqual( json1, json1, null ) );
      assertTrue( "Change Order", JSONComparitor.jsonEqual( json1, json2, null ) );
      assertFalse( "Remove Cols Array", JSONComparitor.jsonEqual( json1, json3, null ) );
      assertFalse( "Remove Cols Array (reversed compare order)", JSONComparitor.jsonEqual( json1, json3, null ) );
      assertFalse( "Change Element Value", JSONComparitor.jsonEqual( json1, json4, null ) );
      assertTrue( "Change order Cols/Conditions", JSONComparitor.jsonEqual( json1, json5, null ) );
      assertFalse( "Change array order", JSONComparitor.jsonEqual( json1, json6, null ) );
      assertFalse( "Change element name", JSONComparitor.jsonEqual( json1, json7, null ) );
      assertFalse( "Omit one element", JSONComparitor.jsonEqual( json1, json8, null ) );
      try {
        JSONComparitor.jsonEqual( json1, json9, null );
        fail( "malformed JSON did not cause exception" );
      } catch ( JSONException ex ) {
        // should catch here
      }
    } catch ( Exception ex ) {
      fail( "Unanticipated Exception " + ex );
    }
  }
}
