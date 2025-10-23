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
