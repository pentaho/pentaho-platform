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
 * Copyright (c) 2017 Hitachi Vantara..  All rights reserved.
 */

package org.pentaho.platform.util;

/**
 * A Utility class for methods related to unique request ids.
 */
public class RequestIdUtil {

  public static final String X_REQUEST_ID = "x-request-id"; //$NON-NLS-1$
  public static final String REQUEST_ID = "requestId"; //$NON-NLS-1$
  private static final String REQUEST_ID_FORMAT = "rid-%s"; //$NON-NLS-1$

  public static String getFormattedRequestUid( final String requestId ) {
    return  String.format( REQUEST_ID_FORMAT, requestId );
  }
}
