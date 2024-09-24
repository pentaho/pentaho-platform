/*!
 *
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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

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
