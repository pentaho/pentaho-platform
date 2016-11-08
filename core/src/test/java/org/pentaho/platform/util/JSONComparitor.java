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

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.Iterator;

/**
 * Static methods to test two JSON strings, object or arrays for equivalence.
 * 
 * @author tkafalas
 */
public class JSONComparitor {

  // Boolean, JSONArray, JSONObject, Number, String, or the JSONObject.NULL
  // object but treat numbers and boolean as strings for comparison purposes.
  public enum KeyType {
    OBJECT, ARRAY, STRING, NULL
  }

  /**
   * Compare two JSON strings for equivalence.
   * 
   * @param json1
   *          First JSON string
   * @param json2
   *          Second JSON string
   * @param compareKey
   *          root key to start compare, if null compare the entire string.
   * @return true if equivalent, false if different.
   * @throws JSONException
   */
  public static boolean jsonEqual( String json1, String json2, String compareKey ) throws JSONException {
    return jsonEqual( new JSONObject( json1 ), new JSONObject( json2 ), compareKey );
  }

  /**
   * Compare two JSON objects for equivalence.
   * 
   * @param json1
   *          First JSON object
   * @param json2
   *          Second JSON object
   * @param compareKey
   *          root key to start compare, if null compare the entire string.
   * @return true if equivalent, false if different.
   * @throws JSONException
   */
  public static boolean jsonEqual( JSONObject jso1, JSONObject jso2, String compareKey ) throws JSONException {
    if ( jso1 == null && jso2 == null ) {
      return true;
    }
    if ( jso1 == null || jso2 == null || jso1.length() != jso2.length() ) {
      return false;
    }

    if ( compareKey != null ) {
      return compareKey( jso1, jso2, compareKey );
    } else {
      Iterator<String> i = jso1.keys();
      while ( i.hasNext() ) {
        String key = i.next();
        if ( !compareKey( jso1, jso2, key ) ) {
          return false;
        }
      }
      return true;
    }
  }

  /**
   * Compare two JSONArrays for equivalence.
   * 
   * @param jsan1
   *          First JSONArray
   * @param jsan2
   *          Second JSONArray
   * @param item
   *          Index of array to compare, null compare entire array
   * @return true if equivalent, false if different.
   * @throws JSONException
   */
  public static boolean jsonEqual( JSONArray jsa1, JSONArray jsa2, Integer item ) throws JSONException {
    if ( jsa1 == null && jsa2 == null ) {
      return true;
    }
    if ( jsa1 == null || jsa2 == null || jsa1.length() != jsa2.length() ) {
      return false;
    }

    if ( item != null ) {
      return compareArray( jsa1, jsa2, item );
    } else {
      for ( int i = 0; i < jsa1.length(); i++ ) {
        // System.out.println("Array item " + i);
        if ( !compareArray( jsa1, jsa2, i ) ) {
          return false;
        }
      }
      return true;
    }
  }

  private static boolean compareKey( JSONObject jso1, JSONObject jso2, String key ) throws JSONException {
    KeyType keyType = getKeyType( jso1, key );
    if ( jso2.has( key ) ) {
      if ( keyType == getKeyType( jso2, key ) ) {
        if ( keyType == KeyType.OBJECT ) {
          return jsonEqual( jso1.getJSONObject( key ), jso2.getJSONObject( key ), null );
        } else {
          if ( keyType == KeyType.ARRAY ) {
            return jsonEqual( jso1.getJSONArray( key ), jso2.getJSONArray( key ), null );
          } else {
            return jso1.getString( key ).equals( jso2.getString( key ) );
          }
        }
      }
    }
    return false;
  }

  private static boolean compareArray( JSONArray jsa1, JSONArray jsa2, int i ) throws JSONException {
    KeyType keyType = getKeyType( jsa1, i );
    if ( keyType == getKeyType( jsa2, i ) ) {
      if ( keyType == KeyType.OBJECT ) {
        return jsonEqual( jsa1.getJSONObject( i ), jsa2.getJSONObject( i ), null );
      } else {
        if ( keyType == KeyType.ARRAY ) {
          return jsonEqual( jsa1.getJSONArray( i ), jsa2.getJSONArray( i ), null );
        } else {
          return jsa1.getString( i ).equals( jsa2.getString( i ) );
        }
      }
    }
    return false;
  }

  private static KeyType getKeyType( JSONObject jso, String key ) {
    try {
      Object o = jso.get( key );
      if ( o instanceof JSONObject ) {
        return KeyType.OBJECT;
      }
      if ( o instanceof JSONArray ) {
        return KeyType.ARRAY;
      }
      return KeyType.STRING;

    } catch ( Exception ex ) {
      return KeyType.NULL;
    }
  }

  private static KeyType getKeyType( JSONArray jso, int item ) {
    try {
      Object o = jso.get( item );
      if ( o instanceof JSONObject ) {
        return KeyType.OBJECT;
      }
      if ( o instanceof JSONArray ) {
        return KeyType.ARRAY;
      }
      return KeyType.STRING;

    } catch ( Exception ex ) {
      return KeyType.NULL;
    }
  }
}
