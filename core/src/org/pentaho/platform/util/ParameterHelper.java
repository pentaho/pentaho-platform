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

import org.pentaho.platform.util.logging.Logger;
import org.pentaho.platform.util.messages.Messages;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.Date;

/**
 * @author James Dixon
 * 
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code
 *         Style - Code Templates
 */
public class ParameterHelper {

  public static String parameterToString( final String value, final String defaultValue ) {
    if ( value == null ) {
      return defaultValue;
    }
    return value;
  }

  public static long parameterToLong( final String value, final long defaultValue ) {
    if ( value == null ) {
      return defaultValue;
    }
    try {
      long longValue = Long.valueOf( value ).longValue();
      return longValue;
    } catch ( Exception e ) {
      Logger.error( ParameterHelper.class.getName(), Messages.getInstance().getErrorString(
          "ParameterHelper.ERROR_0001_INVALID_NUMERIC" ), e ); //$NON-NLS-1$
    }
    return defaultValue;
  }

  public static Date parameterToDate( final String value, final Date defaultValue ) {
    try {
      Date date = DateFormat.getInstance().parse( value );
      if ( date == null ) {
        return defaultValue;
      }
      return date;
    } catch ( Exception e ) {
      return defaultValue;
    }
  }

  public static BigDecimal parameterToDecimal( final String value, final BigDecimal defaultValue ) {
    if ( value == null ) {
      return defaultValue;
    }
    try {
      BigDecimal decimal = new BigDecimal( value );
      return decimal;
    } catch ( Exception e ) {
      Logger.error( ParameterHelper.class.getName(), Messages.getInstance().getErrorString(
          "ParameterHelper.ERROR_0001_INVALID_NUMERIC" ), e ); //$NON-NLS-1$
    }
    return defaultValue;
  }

  public static Object[] parameterToObjectArray( final Object value, final Object[] defaultValue ) {
    if ( value == null ) {
      return defaultValue;
    }

    if ( value instanceof Object[] ) {
      return (Object[]) value;
    }

    return new Object[] { value };
  }

  public static String[] parameterToStringArray( final Object value, final String[] defaultValue ) {
    if ( value == null ) {
      return defaultValue;
    }

    if ( value instanceof String[] ) {
      return (String[]) value;
    }

    if ( value instanceof Object[] ) {
      Object[] oArray = (Object[]) value;
      for ( int i = 0; i < oArray.length; ++i ) {
        oArray[i] = oArray[i].toString();
      }
      return (String[]) oArray;
    }

    return new String[] { value.toString() };
  }

}
