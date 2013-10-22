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

package org.pentaho.platform.util.messages;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class MessageUtil {

  /**
   * Get a formatted error message. The message consists of two parts. The first part is the error numeric Id
   * associated with the key used to identify the message in the resource file. For instance, suppose the error key
   * is MyClass.ERROR_0068_TEST_ERROR. The first part of the error msg would be "0068". The second part of the
   * returned string is simply the <code>msg</code> parameter.
   * 
   * Currently the format is: error key - error msg For instance: "0068 - A test error message."
   * 
   * @param key
   *          String containing the key that was used to obtain the <code>msg</code> parameter from the resource
   *          file.
   * @param msg
   *          String containing the message that was obtained from the resource file using the <code>key</code>
   *          parameter.
   * @return String containing the formatted error message.
   */
  public static String formatErrorMessage( final String key, final String msg ) {
    int end = key.indexOf( ".ERROR_" ); //$NON-NLS-1$
    end = ( end < 0 ) ? key.length() : Math.min( end + ".ERROR_0000".length(), key.length() ); //$NON-NLS-1$
    // we have decided not to localize this
    return key.substring( 0, end ) + " - " + msg; //$NON-NLS-1$
  }

  /**
   * Get the message from the specified resource bundle using the specified key.
   * 
   * @param bundle
   *          ResourceBundle containing the desired String
   * @param key
   *          String containing the key to locate the desired String in the ResourceBundle.
   * @return String containing the message from the specified resource bundle accessed using the specified key
   */
  public static String getString( final ResourceBundle bundle, final String key ) {
    try {
      return bundle.getString( key );
    } catch ( Exception e ) {
      return '!' + key + '!';
    }
  }

  public static String getString( final ResourceBundle bundle, final String key, final Object... params ) {
    try {
      return MessageFormat.format( bundle.getString( key ), params );
    } catch ( Exception e ) {
      return '!' + key + '!';
    }
  }

  /**
   * Get a message from the specified resource bundle using the specified key, and format it. see
   * <code>formatErrorMessage</code> for details on how the message is formatted.
   * 
   * @param bundle
   *          ResourceBundle containing the desired String
   * @param key
   *          String containing the key to locate the desired String in the ResourceBundle.
   * @return String containing the formatted message.
   */
  public static String getErrorString( final ResourceBundle bundle, final String key ) {
    return MessageUtil.formatErrorMessage( key, MessageUtil.getString( bundle, key ) );
  }

  public static String getErrorString( final ResourceBundle bundle, final String key, final Object... params ) {
    return MessageUtil.formatErrorMessage( key, MessageUtil.getString( bundle, key, params ) );
  }

  public static String formatMessage( final String pattern, final Object... params ) {
    try {
      return MessageFormat.format( pattern, params );
    } catch ( Exception e ) {
      return '!' + pattern + '!';
    }
  }
}
