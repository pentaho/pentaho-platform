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
 * Copyright (c) 2002-2020 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.util.web;

import java.util.HashMap;
import java.util.Map;

public class MimeHelper {

  public static final String MIMETYPE_RTF = "application/rtf";
  public static final String RTF_EXTENSION = ".rtf";
  public static final String MIMETYPE_MS_WORD = "application/msword";
  public static final String MS_WORD_EXTENSION = ".doc";
  public static final String MIMETYPE_PDF = "application/pdf";
  public static final String PDF_EXTENSION = ".pdf";
  public static final String MIMETYPE_MS_EXCEL = "application/vnd.ms-excel";
  public static final String MS_EXCEL_EXTENSION = ".xls";
  public static final String MIMETYPE_MS_PPT = "application/vnd.ms-powerpoint";
  public static final String MS_PPT_EXTENSION = ".ppt";
  public static final String MIMETYPE_MS_PROJECT = "application/vnd.ms-project";
  public static final String MS_PROJECT_EXTENSION = ".mpp";
  public static final String MIMETYPE_ZIP = "application/zip";
  public static final String ZIP_EXTENSION = ".zip";
  public static final String MIMETYPE_MP3 = "audio/mpeg";
  public static final String MP3_EXTENSION = ".mp3";
  public static final String MIMETYPE_WAV = "audio/x-wav";
  public static final String WAV_EXTENSION = ".wav";
  public static final String MIMETYPE_BMP = "image/bmp";
  public static final String BMP_EXTENSION = ".bmp";
  public static final String MIMETYPE_GIF = "image/gif";
  public static final String GIF_EXTENSION = ".gif";
  public static final String MIMETYPE_JPEG = "image/jpeg";
  public static final String JPEG_EXTENSION_1 = ".jpg";
  public static final String JPEG_EXTENSION_2 = ".jpe";
  public static final String JPEG_EXTENSION_3 = ".jpeg";
  public static final String MIMETYPE_PNG = "image/png";
  public static final String PNG_EXTENSION = ".png";
  public static final String MIMETYPE_SVG = "image/svg+xml";
  public static final String SVG_EXTENSION = ".svg";
  public static final String MIMETYPE_TIFF = "image/tiff";
  public static final String TIFF_EXTENSION_1 = ".tif";
  public static final String TIFF_EXTENSION_2 = ".tiff";
  public static final String MIMETYPE_CSV = "text/csv";
  public static final String CSV_EXTENSION = ".csv";
  public static final String MIMETYPE_HTML = "text/html";
  public static final String HTML_EXTENSION_1 = ".html";
  public static final String HTML_EXTENSION_2 = ".htm";
  public static final String MIMETYPE_TEXT = "text/plain";
  public static final String TEXT_EXTENSION = ".txt";
  public static final String PROPERTIES_EXTENSION = ".properties";
  public static final String MIMETYPE_MPG = "video/mpeg";
  public static final String MPG_EXTENSION_1 = ".mpg";
  public static final String MPG_EXTENSION_2 = ".mpe";
  public static final String MPG_EXTENSION_3 = ".mpeg";
  public static final String MIMETYPE_AVI = "video/x-msvideo";
  public static final String AVI_EXTENSION = ".avi";
  public static final String MIMETYPE_XACTION = "text/xaction+xml";
  public static final String XACTION_EXTENSION = ".xaction";
  public static final String MIMETYPE_CSS = "text/css";
  public static final String CSS_EXTENSION = ".css";
  public static final String MIMETYPE_JS = "text/javascript";
  public static final String JS_EXTENSION = ".js";
  public static final String MIMETYPE_XML = "text/xml";
  public static final String XML_EXTENSION = ".xml";
  public static final String MIMETYPE_FLASH = "application/x-shockwave-flash";
  public static final String FLASH_EXTENSION = ".swf";
  public static final String MIMETYPE_MS_EXCEL_2007 = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
  public static final String MS_EXCEL_2007_EXTENSION = ".xlsx";
  public static final String MIMETYPE_EMAIL_MSG = "mime-message/text/html";
  public static final String EMAIL_MSG_EXTENSION = ".eml";

  private static final String UTF_CHARACTER_ENCODING = "UTF-8";
  private static final Map<String, String> mimes = new HashMap<>();
  private static final Map<String, String> extensions = new HashMap<>();
  private static final Map<String, String> defaultCharset = new HashMap<>();

  static {
    MimeHelper.mimes.put( MIMETYPE_RTF, RTF_EXTENSION );
    MimeHelper.mimes.put( MIMETYPE_MS_WORD, MS_WORD_EXTENSION );
    MimeHelper.mimes.put( MIMETYPE_PDF, PDF_EXTENSION );
    MimeHelper.mimes.put( MIMETYPE_MS_EXCEL, MS_EXCEL_EXTENSION );
    MimeHelper.mimes.put( MIMETYPE_MS_PPT, MS_PPT_EXTENSION );
    MimeHelper.mimes.put( MIMETYPE_MS_PROJECT, MS_PROJECT_EXTENSION );
    MimeHelper.mimes.put( MIMETYPE_ZIP, ZIP_EXTENSION );
    MimeHelper.mimes.put( MIMETYPE_MP3, MP3_EXTENSION );
    MimeHelper.mimes.put( MIMETYPE_WAV, WAV_EXTENSION );
    MimeHelper.mimes.put( MIMETYPE_BMP, BMP_EXTENSION );
    MimeHelper.mimes.put( MIMETYPE_GIF, GIF_EXTENSION );
    MimeHelper.mimes.put( MIMETYPE_JPEG, JPEG_EXTENSION_1 );
    MimeHelper.mimes.put( MIMETYPE_PNG, PNG_EXTENSION );
    MimeHelper.mimes.put( MIMETYPE_SVG, SVG_EXTENSION );
    MimeHelper.mimes.put( MIMETYPE_TIFF, TIFF_EXTENSION_1 );
    MimeHelper.mimes.put( MIMETYPE_CSV, CSV_EXTENSION );
    MimeHelper.mimes.put( MIMETYPE_HTML, HTML_EXTENSION_1 );
    MimeHelper.mimes.put( MIMETYPE_TEXT, TEXT_EXTENSION );
    MimeHelper.mimes.put( MIMETYPE_MPG, MPG_EXTENSION_1 );
    MimeHelper.mimes.put( MIMETYPE_AVI, AVI_EXTENSION );
    MimeHelper.mimes.put( MIMETYPE_XACTION, XACTION_EXTENSION );
    MimeHelper.mimes.put( MIMETYPE_CSS, CSS_EXTENSION );
    MimeHelper.mimes.put( MIMETYPE_JS, JS_EXTENSION );
    MimeHelper.mimes.put( MIMETYPE_XML, XML_EXTENSION );
    MimeHelper.mimes.put( MIMETYPE_FLASH, FLASH_EXTENSION );
    MimeHelper.mimes.put( MIMETYPE_MS_EXCEL_2007, MS_EXCEL_2007_EXTENSION );
    MimeHelper.mimes.put( MIMETYPE_EMAIL_MSG, EMAIL_MSG_EXTENSION );

    MimeHelper.extensions.put( RTF_EXTENSION, MIMETYPE_RTF );
    MimeHelper.extensions.put( MS_WORD_EXTENSION, MIMETYPE_MS_WORD );
    MimeHelper.extensions.put( PDF_EXTENSION, MIMETYPE_PDF );
    MimeHelper.extensions.put( MS_EXCEL_EXTENSION, MIMETYPE_MS_EXCEL );
    MimeHelper.extensions.put( MS_PPT_EXTENSION, MIMETYPE_MS_PPT );
    MimeHelper.extensions.put( MS_PROJECT_EXTENSION, MIMETYPE_MS_PROJECT );
    MimeHelper.extensions.put( ZIP_EXTENSION, MIMETYPE_ZIP );
    MimeHelper.extensions.put( MP3_EXTENSION, MIMETYPE_MP3 );
    MimeHelper.extensions.put( WAV_EXTENSION, MIMETYPE_WAV );
    MimeHelper.extensions.put( BMP_EXTENSION, MIMETYPE_BMP );
    MimeHelper.extensions.put( GIF_EXTENSION, MIMETYPE_GIF );
    MimeHelper.extensions.put( JPEG_EXTENSION_1, MIMETYPE_JPEG );
    MimeHelper.extensions.put( JPEG_EXTENSION_2, MIMETYPE_JPEG );
    MimeHelper.extensions.put( JPEG_EXTENSION_3, MIMETYPE_JPEG );
    MimeHelper.extensions.put( PNG_EXTENSION, MIMETYPE_PNG );
    MimeHelper.extensions.put( SVG_EXTENSION, MIMETYPE_SVG );
    MimeHelper.extensions.put( TIFF_EXTENSION_1, MIMETYPE_TIFF );
    MimeHelper.extensions.put( TIFF_EXTENSION_2, MIMETYPE_TIFF );
    MimeHelper.extensions.put( CSV_EXTENSION, MIMETYPE_CSV );
    MimeHelper.extensions.put( HTML_EXTENSION_1, MIMETYPE_HTML );
    MimeHelper.extensions.put( HTML_EXTENSION_2, MIMETYPE_HTML );
    MimeHelper.extensions.put( TEXT_EXTENSION, MIMETYPE_TEXT );
    MimeHelper.extensions.put( MPG_EXTENSION_1, MIMETYPE_MPG );
    MimeHelper.extensions.put( MPG_EXTENSION_2, MIMETYPE_MPG );
    MimeHelper.extensions.put( MPG_EXTENSION_3, MIMETYPE_MPG );
    MimeHelper.extensions.put( AVI_EXTENSION, MIMETYPE_AVI );
    MimeHelper.extensions.put( XACTION_EXTENSION, MIMETYPE_XACTION );
    MimeHelper.extensions.put( CSS_EXTENSION, MIMETYPE_CSS );
    MimeHelper.extensions.put( JS_EXTENSION, MIMETYPE_JS );
    MimeHelper.extensions.put( XML_EXTENSION, MIMETYPE_XML );
    MimeHelper.extensions.put( FLASH_EXTENSION, MIMETYPE_FLASH );
    MimeHelper.extensions.put( MS_EXCEL_2007_EXTENSION, MIMETYPE_MS_EXCEL_2007 );
    MimeHelper.extensions.put( PROPERTIES_EXTENSION, MIMETYPE_TEXT );
    MimeHelper.extensions.put( EMAIL_MSG_EXTENSION, MIMETYPE_EMAIL_MSG );

    MimeHelper.defaultCharset.put( MIMETYPE_HTML, UTF_CHARACTER_ENCODING );
    MimeHelper.defaultCharset.put( MIMETYPE_TEXT, UTF_CHARACTER_ENCODING );
  }

  public static String getExtension( final String mimeType ) {
    return MimeHelper.mimes.get( mimeType );
  }

  public static String getExtension( final String mimeType, String defaultExtension ) {
    String extension = getExtension( mimeType );
    return extension == null ? defaultExtension : extension;
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
