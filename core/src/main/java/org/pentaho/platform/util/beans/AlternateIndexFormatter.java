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


package org.pentaho.platform.util.beans;

/**
 * Converts to a bean utils consumable expression and applies other customizations as necessary, such as suffix
 * additions.
 * 
 * @param name
 *          the property name to format
 * @return the formatted property name ready for use in bean utils
 */
public class AlternateIndexFormatter implements PropertyNameFormatter {

  public String format( String name ) {
    String formattedName = name;
    int indexDelimiter = name.lastIndexOf( '_' );
    if ( indexDelimiter > 0 ) { // implies there is a name and an index like 'b_1' or 'a_b'
      String possibleIndex = name.substring( indexDelimiter + 1 );
      try {
        int index = Integer.parseInt( possibleIndex );
        String propertyName = name.substring( 0, indexDelimiter );
        formattedName = propertyName + "[" + index + "]"; //$NON-NLS-1$ //$NON-NLS-2$
        return formattedName;
      } catch ( NumberFormatException e ) {
        // we don't have a numeric index, so just return the original expression
      }
    }
    return formattedName;
  }
}
