package org.pentaho.platform.engine.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;

public class PentahoJsonValidator {

    public static void validateJson( String json, Class objType ) throws IllegalArgumentException {
        validateJson(json, Arrays.asList(objType));
    }

    public static void validateJson( String json, List<Class> objTypes ) throws IllegalArgumentException {
      // validate the json before deserializing it
      try {
          JSONObject jsonObject = new JSONObject( json );
          final List<String> classWhiteList =
            objTypes.stream()
              .map(c -> c.getName())
              .collect( Collectors.toList() );
          final List<String> classNames = new ArrayList<String>();
          findClassNames( jsonObject, classNames );
          for (String cn : classNames) {
            if ( !classWhiteList.contains( cn ) ) {
              throw new IllegalArgumentException( "Invalid Payload" );
            }
          }
      } catch ( JSONException e ) {
          throw new IllegalArgumentException( "Invalid Json" );
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

    public static boolean isJsonValid( String json, List<Class> objTypes ) {
      try {
        validateJson( json, objTypes );
      } catch ( IllegalArgumentException e ) {
          return false;
      }
      return true;
  }

  private static void findClassNames(JSONObject json, List<String> classNames) throws JSONException {
    // check this object for class attribute
    final String classAttr = (String) json.get( "class" );
    if (classAttr != null) {
      classNames.add( classAttr );
    }
    // check his children
    final Iterator keys = json.keys();
    while ( keys.hasNext() ) {
      Object value = json.get( keys.next().toString() );
      if (value instanceof JSONObject) {
        // Recurse into nested JSONObjects
        findClassNames( ( JSONObject ) value, classNames );
      }
    }
  }
}
