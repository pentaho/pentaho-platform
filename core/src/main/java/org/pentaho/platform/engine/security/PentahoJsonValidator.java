package org.pentaho.platform.engine.security;

import org.json.JSONException;
import org.json.JSONObject;

public class PentahoJsonValidator {

    public PentahoJsonValidator( String json, Class objType ){
        validateJson( json, objType );
    }

    public static void validateJson( String json, Class objType ) throws IllegalArgumentException {
        // validate the json before deserializing it
        try {
            JSONObject jsonObject = new JSONObject( json );
            String jsonClassName = (String) jsonObject.get( "class" );

            if ( !hasSameClass( objType, jsonClassName ) ) {
                throw new IllegalArgumentException( "Invalid Payload" );
            }
        } catch ( JSONException e ) {
            throw new IllegalArgumentException( "Invalid Json" );
        }
    }

    private static boolean hasSameClass( Class objType, String jsonClassName ) {
        return objType.getName().equals( jsonClassName );
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
