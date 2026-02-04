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
