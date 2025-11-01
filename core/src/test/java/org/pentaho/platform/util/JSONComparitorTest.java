/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.util;

import org.codehaus.jettison.json.JSONException;

import junit.framework.TestCase;

public class JSONComparitorTest extends TestCase {

  public void testJSONComparator() {
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
