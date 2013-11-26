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

package org.pentaho.platform.util.web;

import java.util.HashMap;
import java.util.Map;

public class MimeHelper {

  public static String MIMETYPE_XACTION = "text/xaction+xml"; //$NON-NLS-1$

  private static final Map<String, String> mimes = new HashMap<String, String>();

  private static final Map<String, String> extensions = new HashMap<String, String>();

  private static final Map<String, String> defaultCharset = new HashMap<String, String>();

  static {

    MimeHelper.mimes.put( "application/rtf", ".rtf" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.mimes.put( "application/msword", ".doc" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.mimes.put( "application/pdf", ".pdf" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.mimes.put( "application/vnd.ms-excel", ".xls" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.mimes.put( "application/vnd.ms-powerpoint", ".ppt" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.mimes.put( "application/vnd.ms-project", ".mpp" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.mimes.put( "application/zip", ".zip" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.mimes.put( "audio/mpeg", ".mp3" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.mimes.put( "audio/x-wav", ".wav" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.mimes.put( "image/bmp", ".bmp" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.mimes.put( "image/gif", ".gif" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.mimes.put( "image/jpeg", ".jpg" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.mimes.put( "image/png", ".png" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.mimes.put( "image/svg+xml", ".svg" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.mimes.put( "image/tiff", ".tif" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.mimes.put( "text/csv", ".csv" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.mimes.put( "text/html", ".html" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.mimes.put( "text/plain", ".txt" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.mimes.put( "video/mpeg", ".mpg" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.mimes.put( "video/x-msvideo", ".avi" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.mimes.put( "text/xaction+xml", ".xaction" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.mimes.put( "text/css", ".css" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.mimes.put( "text/javascript", ".js" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.mimes.put( "text/xml", ".xml" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.mimes.put( "application/x-shockwave-flash", ".swf" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.mimes.put( "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ".xlsx" ); //$NON-NLS-1$ //$NON-NLS-2$

    MimeHelper.extensions.put( ".rtf", "application/rtf" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.extensions.put( ".doc", "application/msword" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.extensions.put( ".pdf", "application/pdf" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.extensions.put( ".xls", "application/vnd.ms-excel" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.extensions.put( ".ppt", "application/vnd.ms-powerpoint" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.extensions.put( ".mpp", "application/vnd.ms-project" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.extensions.put( ".zip", "application/zip" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.extensions.put( ".mp3", "audio/mpeg" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.extensions.put( ".wav", "audio/x-wav" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.extensions.put( ".bmp", "image/bmp" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.extensions.put( ".gif", "image/gif" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.extensions.put( ".jpe", "image/jpeg" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.extensions.put( ".jpeg", "image/jpeg" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.extensions.put( ".jpg", "image/jpeg" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.extensions.put( ".png", "image/png" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.extensions.put( ".svg", "image/svg+xml" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.extensions.put( ".tif", "image/tiff" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.extensions.put( ".tiff", "image/tiff" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.extensions.put( ".csv", "text/csv" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.extensions.put( ".htm", "text/html" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.extensions.put( ".html", "text/html" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.extensions.put( ".txt", "text/plain" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.extensions.put( ".mpe", "video/mpeg" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.extensions.put( ".mpeg", "video/mpeg" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.extensions.put( ".mpg", "video/mpeg" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.extensions.put( ".avi", "video/x-msvideo" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.extensions.put( ".xaction", "text/xaction+xml" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.extensions.put( ".css", "text/css" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.extensions.put( ".js", "text/javascript" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.extensions.put( ".xml", "text/xml" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.extensions.put( ".swf", "application/x-shockwave-flash" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.extensions.put( ".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.extensions.put( ".properties", "text/plain" ); //$NON-NLS-1$ //$NON-NLS-2$

    MimeHelper.defaultCharset.put( "text/html", "UTF-8" ); //$NON-NLS-1$ //$NON-NLS-2$
    MimeHelper.defaultCharset.put( "text/plain", "UTF-8" ); //$NON-NLS-1$ //$NON-NLS-2$

  }

  public static String getExtension( final String mimeType ) {
    return MimeHelper.mimes.get( mimeType );
  }

  public static String getMimeTypeFromExtension( final String extension ) {
    return MimeHelper.extensions.get( extension.toLowerCase() );
  }

  public static String getMimeTypeFromFileName( final String fileName ) {
    int index = fileName.lastIndexOf( '.' );
    if ( index == -1 ) {
      return null;
    }
    return MimeHelper.extensions.get( fileName.substring( index ) );
  }

  public static String getDefaultCharset( final String mimeType ) {
    return MimeHelper.defaultCharset.get( mimeType );
  }

}
