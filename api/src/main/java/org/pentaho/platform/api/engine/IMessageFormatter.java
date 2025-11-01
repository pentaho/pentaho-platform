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


package org.pentaho.platform.api.engine;

import org.pentaho.commons.connection.IPentahoResultSet;

import java.util.List;

public interface IMessageFormatter {

  void formatErrorMessage( final String mimeType, final String title, final String message,
      final StringBuffer messageBuffer );

  /**
   * If PentahoMessenger.getUserString("ERROR") returns the string: "Error: {0} ({1})" (which is the case for
   * English) Find the substring before the first "{". In this case, that would be: "Error: ". Return the first
   * string in the messages list that contains the string "Error: ". If no string in the list contains "Error: ",
   * return null;
   * 
   * @param messages
   * @return
   */
  @SuppressWarnings( "rawtypes" )
  String getFirstError( final List messages );

  @SuppressWarnings( "rawtypes" )
  void formatErrorMessage( final String mimeType, final String title, final List messages,
      final StringBuffer messageBuffer );

  @SuppressWarnings( "rawtypes" )
  void formatFailureMessage( final String mimeType, final IRuntimeContext context, final StringBuffer messageBuffer,
      final List defaultMessages );

  /**
   * @param showStacktrace if true, exception stacktrace (if it is in messages)
   *                       will be put into formatted message for debug purposes
   */
  default void formatFailureMessage( final String mimeType, final IRuntimeContext context,
      final StringBuffer messageBuffer, final List defaultMessages, final boolean showStacktrace ) {
    // do nothing by default.
    // just making sure no descendant is broken.
  }

  void formatFailureMessage( final String mimeType, final IRuntimeContext context, final StringBuffer messageBuffer );

  void formatResultSetAsHTMLRows( final IPentahoResultSet resultSet, final StringBuffer messageBuffer );

  void formatSuccessMessage( final String mimeType, final IRuntimeContext context, final StringBuffer messageBuffer,
      final boolean doMessages );

  void formatSuccessMessage( final String mimeType, final IRuntimeContext context, final StringBuffer messageBuffer,
      final boolean doMessages, final boolean doWrapper );

}
