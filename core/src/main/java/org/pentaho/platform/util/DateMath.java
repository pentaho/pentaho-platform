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

import org.pentaho.platform.util.messages.LocaleHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * Provides a utility for calculating relative dates. The class calculates a date based upon an expression. The
 * syntax of the expression is given below.
 * <p>
 * <b>Date Expression</b><br>
 * 
 * <pre>
 *       &lt;expression&gt;     := &lt;expression&gt;+ ( ';' DATESPEC )?
 *       &lt;expression&gt;     := OPERATION? OPERAND ':' &lt;unit&gt; &lt;position&gt;?
 *       &lt;unit&gt;           := 'Y' | 'M' | 'D' | 'W' | 'h' | 'm' | 's'
 *       &lt;position&gt;       := 'S' | 'E' 
 *       OPERATION        := '+' | '-'
 *       OPERAND          := [0..9]+
 *       DATESPEC         := &lt;i&gt;any {@link java.text.SimpleDateFormat} format pattern&lt;/i&gt;
 * </pre>
 * 
 * The <tt>OPERAND</tt> specifies the positive or negative offset to the date. The <tt>unit</tt> inidcates the
 * <i>unit</i> of the date to manipulate. The optional position indicates the relative position for the specified
 * unit: <i>S</i> for start and <i>E</i> for end. The following are the valid unit values.
 * 
 * <pre>
 *       Y        Year
 *       M        Month
 *       W        Week
 *       D        Day
 *       h        hour
 *       m        minute
 *       s        second
 * </pre>
 * 
 * <p>
 * <b>Examples</b>:
 * 
 * <pre>
 *       0:ME -1:DS       00:00:00.000 of the day before the last day of the current month
 *       0:MS  0:WE       23:59:59.999 the last day of the first week of the month
 *       0:ME             23:59:59.999 of the last day of the current month
 *       5:Y              the current month, day and time 5 years in the future
 *       5:YS             00:00:00.000 of the first day of the years 5 years in the future
 * </pre>
 */
public class DateMath {

  private static final char POSITION_END = 'E';

  private static final char POSITION_START = 'S';

  private static final char UNIT_YEAR = 'Y';

  private static final char UNIT_MONTH = 'M';

  private static final char UNIT_WEEK = 'W';

  private static final char UNIT_DAY = 'D';

  private static final char UNIT_HOUR = 'h';

  private static final char UNIT_MINUTE = 'm';

  private static final char UNIT_SECOND = 's';

  /**
   * Calculates a date, returning the formatted string version of the calculated date. The method is a short cut
   * for {@link #calculateDate(Calendar,String,Locale) calculateDate(null,expressionWithFormat,null)}. If the date
   * format is omitted, the short format for the {@link PentahoSystem#getLocale()} is used.
   * 
   * @param expressionWithFormat
   *          the relative date expression with optional format specification.
   * @return The calculated date as a string.
   * @throws IllegalArgumentException
   *           if <tt>expressionWithFormat</tt> is invalid.
   */
  public static String claculateDateString( final String expressionWithFormat ) {
    return DateMath.calculateDateString( null, expressionWithFormat, null );
  }

  /**
   * Calculates a date, returning the formatted string version of the calculated date. The method is a short cut
   * for {@link #calculateDate(Calendar,String,Locale) calculateDate(date,expressionWithFormat,null)}.
   * 
   * @param date
   *          the target date against the expression will be applied.
   * @param expressionWithFormat
   *          the relative date expression with optional format specification.
   * @return The calculated date as a string.
   * @throws IllegalArgumentException
   *           if <tt>expressionWithFormat</tt> is invalid.
   */
  public static String calculateDateString( final Calendar date, final String expressionWithFormat ) {
    return DateMath.calculateDateString( date, expressionWithFormat, null );
  }

  /**
   * Calculates a date, returning the formatted string version of the calculated date.
   * 
   * @param date
   *          the target date against the expression will be applied. If <tt>null</tt>, the current date is used.
   * @param expressionWithFormat
   *          the relative date expression with optional format specification.
   * @param locale
   *          the desired locale for the formatted string.
   * @return The calculated date as a string.
   * @throws IllegalArgumentException
   *           if <tt>expressionWithFormat</tt> is invalid.
   */
  public static String
  calculateDateString( final Calendar date, final String expressionWithFormat, final Locale locale ) {
    int index = expressionWithFormat.indexOf( ';' );
    String expression;
    String pattern = null;
    Calendar target = ( date == null ) ? Calendar.getInstance() : date;
    DateFormat format;
    Locale myLocale;

    if ( index >= 0 ) {
      pattern = expressionWithFormat.substring( index + 1 );
      expression = expressionWithFormat.substring( 0, index );
    } else {
      expression = expressionWithFormat;
    }

    target = DateMath.calculateDate( date, expression );

    myLocale = ( locale == null ) ? LocaleHelper.getLocale() : locale;
    if ( myLocale == null ) {
      myLocale = LocaleHelper.getDefaultLocale();
    }

    if ( pattern != null ) {
      format = new SimpleDateFormat( pattern, myLocale );
    } else {
      format = DateFormat.getDateTimeInstance( DateFormat.SHORT, DateFormat.SHORT, myLocale );
    }

    return format.format( target.getTime() );
  }

  /**
   * Calculates the date specified by the expression, relative to the current date/time. The method is a short cut
   * for {@link #calculate(Calendar, String) calculate(null,expression)}.
   * 
   * @param expression
   *          the date expression as described above.
   * @return The calculated date.
   * @throws IllegalArgumentException
   *           if <tt>expression</tt> is invalid.
   */
  public static Calendar calculateDate( final String expression ) {
    return DateMath.calculateDate( null, expression );
  }

  /**
   * Calculates the date specified by the expression, relative to the indicated date/time.
   * 
   * @param date
   *          the target date against the expression is evaluated. If <tt>null</tt>, the current date/time is used.
   *          If not <tt>null</tt>, the object is manipulated by the expression.
   * @param expression
   *          the date expression as described above.
   * @return The calculated date. This will be <tt>date</tt> if <tt>date</tt> is not <tt>null</tt>.
   */
  public static Calendar calculateDate( final Calendar date, final String expression ) {
    StringTokenizer tok;
    String myExpression;
    Calendar target = date;
    int index = expression.indexOf( ';' );

    if ( index >= 0 ) {
      myExpression = expression.substring( index + 1 );
    } else {
      myExpression = expression;
    }

    tok = new StringTokenizer( myExpression, " \t;" ); //$NON-NLS-1$
    while ( tok.hasMoreElements() ) {
      target = DateMath.parseAndCalculateDate( target, (String) tok.nextElement() );
    }

    return target;
  }

  /**
   * Parses and executes a single expression, one without subexpressions.
   * 
   * @param date
   *          the target date against the expression is evaluated. If <tt>null</tt>, the current date/time is used.
   *          If not <tt>null</tt>, the object is manipulated by the expression.
   * @param expression
   *          the date expression as described above.
   * @return The calculated date. This will be <tt>date</tt> if <tt>date</tt> is not <tt>null</tt>.
   */
  private static Calendar parseAndCalculateDate( final Calendar date, final String expression ) {
    int index = expression.indexOf( ':' ); // $NON-NLS-1$
    char operation = '+'; // $NON-NLS-1$
    char unit = ' '; // $NON-NLS-1$
    char position = ' '; // $NON-NLS-1$
    int operand = 0;
    Calendar result;

    if ( index >= 0 ) {
      try {
        String number = expression.substring( 0, index );

        operation = number.charAt( 0 );
        if ( ( operation == '+' ) || ( operation == '-' ) ) { // $NON-NLS-1$
          // Integer.praseInt doesn't handle '+' for positive numbers
          //
          number = number.substring( 1 );
        } else {
          operation = '+';
        }

        operand = Integer.parseInt( number );

        index++;
        unit = expression.charAt( index );

        index++;
        if ( index < expression.length() ) {
          position = expression.charAt( index );
        }

        result = DateMath.calculateDate( date, operation, operand, unit, position );
      } catch ( Exception ex ) {
        IllegalArgumentException err = new IllegalArgumentException( expression );

        err.initCause( ex );
        throw err;
      }
    } else {
      throw new IllegalArgumentException( expression );
    }

    return result;
  }

  /**
   * Calculates the relative date based upon the values of the BNF non-terminals above.
   * 
   * @param date
   *          the target date against the expression is evaluated. If <tt>null</tt>, the current date/time is used.
   *          If not <tt>null</tt>, the object is manipulated by the expression.
   * @param operation
   *          the value of the operation. Currently, this is the sign on the operand. However, in the future, it
   *          could be some value to indicate a relative or specific value.
   * @param operand
   *          the value of the NUM token.
   * @param unit
   *          the value of the &lt;unit&gt; non-terminal
   * @param position
   *          the value of teh &lt;position&gt; non-terminal
   * @return The calculated date. This will be <tt>date</tt> if <tt>date</tt> is not <tt>null</tt>.
   */
  private static Calendar calculateDate( final Calendar date, final char operation, int operand, final char unit,
      final char position ) {
    Calendar target = ( date == null ) ? Calendar.getInstance() : date;
    int calendarField = -1;

    switch ( unit ) {
      case UNIT_YEAR:
        calendarField = Calendar.YEAR;
        break;
      case UNIT_MONTH:
        calendarField = Calendar.MONTH;
        break;
      case UNIT_WEEK:
        calendarField = Calendar.DAY_OF_YEAR;
        operand = operand * 7;
        break;
      case UNIT_DAY:
        calendarField = Calendar.DAY_OF_YEAR;
        break;
      case UNIT_HOUR:
        calendarField = Calendar.HOUR_OF_DAY;
        break;
      case UNIT_MINUTE:
        calendarField = Calendar.MINUTE;
        break;
      case UNIT_SECOND:
        calendarField = Calendar.SECOND;
        break;

      default:
        throw new IllegalArgumentException();
    }

    if ( operation == ' ' ) {
      target.set( calendarField, operand );
    } else if ( operation == '+' ) {
      target.add( calendarField, operand );
    } else if ( operation == '-' ) {
      target.add( calendarField, -Math.abs( operand ) );
    }

    if ( unit == DateMath.UNIT_YEAR ) {
      if ( position == DateMath.POSITION_START ) {
        target.set( Calendar.DAY_OF_YEAR, 1 );
        DateMath.setTimeToStart( target );
      } else if ( position == DateMath.POSITION_END ) {
        target.set( Calendar.DAY_OF_YEAR, target.getActualMaximum( Calendar.DAY_OF_YEAR ) );
        DateMath.setTimeToEnd( target );
      }
    } else if ( unit == DateMath.UNIT_MONTH ) {
      if ( position == DateMath.POSITION_START ) {
        target.set( Calendar.DAY_OF_MONTH, 1 );
        DateMath.setTimeToStart( target );
      } else if ( position == DateMath.POSITION_END ) {
        target.set( Calendar.DAY_OF_MONTH, target.getActualMaximum( Calendar.DAY_OF_MONTH ) );
        DateMath.setTimeToEnd( target );
      }
    } else if ( unit == DateMath.UNIT_WEEK ) {
      int firstDOW = target.getFirstDayOfWeek();
      int dayOfWeek = target.get( Calendar.DAY_OF_WEEK ); // force
      // calculation
      int dayOffset = 0;

      if ( position == DateMath.POSITION_START ) {
        if ( dayOfWeek > firstDOW ) {

          // Past first day of week; go backwards to first day
          //
          dayOffset = firstDOW - dayOfWeek;
        } else if ( dayOfWeek < firstDOW ) {

          // Before the first day; go back a week and move forward to
          // first day
          // Should only happen if first day is not Sunday.
          //
          dayOffset = -7 + ( firstDOW - dayOfWeek );
        }

        DateMath.setTimeToStart( target );
      } else if ( position == DateMath.POSITION_END ) {
        int lastDOW;

        if ( firstDOW == Calendar.SUNDAY ) {
          lastDOW = Calendar.SATURDAY;
        } else {
          lastDOW = firstDOW - 1;
        }

        if ( dayOfWeek < lastDOW ) {

          // Before the last day of week; move forward to last day
          //
          dayOffset = lastDOW - dayOfWeek;
        } else if ( dayOfWeek > lastDOW ) {

          // Should only happen if last day is anything but Saturday;
          // Move to next week; roll back to last day.
          dayOffset = 7 - ( dayOfWeek - lastDOW );
        }

        DateMath.setTimeToEnd( target );
      }

      if ( dayOffset != 0 ) {
        target.add( Calendar.DAY_OF_YEAR, dayOffset );
      }
    } else if ( unit == DateMath.UNIT_DAY ) {
      if ( position == DateMath.POSITION_START ) {
        DateMath.setTimeToStart( target );
      } else if ( position == DateMath.POSITION_END ) {
        DateMath.setTimeToEnd( target );
      }
    } else if ( unit == DateMath.UNIT_HOUR ) {
      if ( position == DateMath.POSITION_START ) {
        target.set( Calendar.MINUTE, 0 );
        target.set( Calendar.SECOND, 0 );
        target.set( Calendar.MILLISECOND, 0 );
      } else if ( position == DateMath.POSITION_END ) {
        target.set( Calendar.MINUTE, 59 );
        target.set( Calendar.SECOND, 59 );
        target.set( Calendar.MILLISECOND, 999 );
      }
    } else if ( unit == DateMath.UNIT_MINUTE ) {
      if ( position == DateMath.POSITION_START ) {
        target.set( Calendar.SECOND, 0 );
        target.set( Calendar.MILLISECOND, 0 );
      } else if ( position == DateMath.POSITION_END ) {
        target.set( Calendar.SECOND, 59 );
        target.set( Calendar.MILLISECOND, 999 );
      }
    } else if ( unit == DateMath.UNIT_SECOND ) {
      if ( position == DateMath.POSITION_START ) {
        target.set( Calendar.MILLISECOND, 0 );
      } else if ( position == DateMath.POSITION_END ) {
        target.set( Calendar.MILLISECOND, 999 );
      }
    }

    target.getTimeInMillis(); // force calculations

    return target;
  }

  /**
   * Sets the time to the start of the day (00:00:00.000).
   * 
   * @param target
   *          the target calendar for which the time will be set.
   */
  private static void setTimeToStart( final Calendar target ) {
    target.set( Calendar.MILLISECOND, 0 );
    target.set( Calendar.SECOND, 0 );
    target.set( Calendar.MINUTE, 0 );
    target.set( Calendar.HOUR_OF_DAY, 0 );
  }

  /**
   * Sets the time to the endof the day (23:59:59.999).
   * 
   * @param target
   *          the target calendar for which the time will be set.
   */
  private static void setTimeToEnd( final Calendar target ) {
    target.set( Calendar.MILLISECOND, 999 );
    target.set( Calendar.SECOND, 59 );
    target.set( Calendar.MINUTE, 59 );
    target.set( Calendar.HOUR_OF_DAY, 23 );
  }
}
