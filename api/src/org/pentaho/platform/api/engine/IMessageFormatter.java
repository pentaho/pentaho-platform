/*
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
 * Copyright 2008-2009 Pentaho Corporation.  All rights reserved.
 *
 */
package org.pentaho.platform.api.engine;

import java.util.List;

import org.pentaho.commons.connection.IPentahoResultSet;

public interface IMessageFormatter {

  void formatErrorMessage(final String mimeType, final String title, final String message,
      final StringBuffer messageBuffer);

  /**
   * If PentahoMessenger.getUserString("ERROR") returns the string:
   * "Error: {0} ({1})" (which is the case for English)
   * Find the substring before the first "{". In this case, that 
   * would be: "Error: ".
   * Return the first string in the messages list that contains
   * the string "Error: ". If no string in the list contains
   * "Error: ", return null;
   * @param messages
   * @return
   */
  @SuppressWarnings("unchecked")
  String getFirstError(final List messages);

  @SuppressWarnings("unchecked")
  void formatErrorMessage(final String mimeType, final String title, final List messages,
      final StringBuffer messageBuffer);

  @SuppressWarnings("unchecked")
  void formatFailureMessage(final String mimeType, final IRuntimeContext context, final StringBuffer messageBuffer,
      final List defaultMessages);

  void formatFailureMessage(final String mimeType, final IRuntimeContext context, final StringBuffer messageBuffer);

  void formatResultSetAsHTMLRows(final IPentahoResultSet resultSet, final StringBuffer messageBuffer);

  void formatSuccessMessage(final String mimeType, final IRuntimeContext context, final StringBuffer messageBuffer,
      final boolean doMessages);

  void formatSuccessMessage(final String mimeType, final IRuntimeContext context, final StringBuffer messageBuffer,
      final boolean doMessages, final boolean doWrapper);

}