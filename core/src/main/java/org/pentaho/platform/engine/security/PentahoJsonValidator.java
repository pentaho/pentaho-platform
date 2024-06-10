/*!
 * HITACHI VANTARA PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2024 Hitachi Vantara. All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Hitachi Vantara and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Hitachi Vantara and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Hitachi Vantara is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Hitachi Vantara,
 * explicitly covering such access.
 */

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
