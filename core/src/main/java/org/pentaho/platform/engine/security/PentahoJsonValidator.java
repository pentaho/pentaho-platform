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


package org.pentaho.platform.engine.security;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PentahoJsonValidator {
  public static void validateJson( String json, Class objType ) throws IllegalArgumentException {
    validateJson( json, Arrays.asList( objType ) );
  }

    /**
     *
     * @param json
     * @param whiteListObjectTypes
     * @throws IllegalArgumentException
     */
  public static void validateJson( String json, List<Class> whiteListObjectTypes ) throws IllegalArgumentException {
    try {
      JSONObject jsonObject = new JSONObject( json );
      final List<String> classWhiteList =
                whiteListObjectTypes.stream().map( Class::getName ).collect( Collectors.toList() );
      final Set<String> jsonClassNames = new HashSet<>();
      findJsonClassNames( jsonObject, jsonClassNames );
      if ( !classWhiteList.containsAll( jsonClassNames ) ) {
        throw new IllegalArgumentException( "Invalid Payload" );
      }
    } catch ( JSONException e ) {
      throw new IllegalArgumentException( "Invalid Json" );
    }
  }


    /**
     * Finding Class Names from requested json
     * @param json
     * @param jsonClassNames
     * @throws JSONException
     */
  private static void findJsonClassNames( JSONObject json, Set<String> jsonClassNames ) throws JSONException {
    if ( json.length() > 0 ) {
      if ( json.has( "class" ) ) {
        jsonClassNames.add( (String) json.get( "class" ) );
      }
      final Iterator keys = json.keys();
      while (keys.hasNext()) {
        Object value = json.get(keys.next().toString());
        if (value instanceof JSONObject) {
          findJsonClassNames((JSONObject) value, jsonClassNames);
        } else if (value instanceof JSONArray) {
          JSONArray jsonArray = (JSONArray) value;
          for ( int jsonIndex = 0; jsonIndex < jsonArray.length(); jsonIndex++ ) {
            if ( jsonArray.get( jsonIndex ) != null && jsonArray.get( jsonIndex ) instanceof JSONObject ) {
              findJsonClassNames( (JSONObject) jsonArray.get( jsonIndex ), jsonClassNames );
            }
          }
        }
      }
    }
  }
  public static boolean isJsonValid( String json, Class objType ) {
    try {
      validateJson( json, objType );
    } catch ( IllegalArgumentException e ) {
      return false;
    }
    return true;
  }

}
