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
 * Copyright (c) 2019 Hitachi Vantara. All rights reserved.
 *
 */
package org.pentaho.platform.util.web;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.pentaho.di.core.util.Assert.assertNull;

public class MimeHelperTest {
  private static final String UTF_ENCODING = "UTF-8";

  @Test
  public void getExtensionTest() {
    assertEquals( MimeHelper.RTF_EXTENSION, MimeHelper.getExtension( MimeHelper.MIMETYPE_RTF ) );
    assertEquals( MimeHelper.MS_WORD_EXTENSION, MimeHelper.getExtension( MimeHelper.MIMETYPE_MS_WORD ) );
    assertEquals( MimeHelper.PDF_EXTENSION, MimeHelper.getExtension( MimeHelper.MIMETYPE_PDF ) );
    assertEquals( MimeHelper.MS_EXCEL_EXTENSION, MimeHelper.getExtension( MimeHelper.MIMETYPE_MS_EXCEL ) );
    assertEquals( MimeHelper.MS_PPT_EXTENSION, MimeHelper.getExtension( MimeHelper.MIMETYPE_MS_PPT ) );
    assertEquals( MimeHelper.MS_PROJECT_EXTENSION, MimeHelper.getExtension( MimeHelper.MIMETYPE_MS_PROJECT ) );
    assertEquals( MimeHelper.ZIP_EXTENSION, MimeHelper.getExtension( MimeHelper.MIMETYPE_ZIP ) );
    assertEquals( MimeHelper.MP3_EXTENSION, MimeHelper.getExtension( MimeHelper.MIMETYPE_MP3 ) );
    assertEquals( MimeHelper.WAV_EXTENSION, MimeHelper.getExtension( MimeHelper.MIMETYPE_WAV ) );
    assertEquals( MimeHelper.BMP_EXTENSION, MimeHelper.getExtension( MimeHelper.MIMETYPE_BMP ) );
    assertEquals( MimeHelper.GIF_EXTENSION, MimeHelper.getExtension( MimeHelper.MIMETYPE_GIF ) );
    assertEquals( MimeHelper.JPEG_EXTENSION_1, MimeHelper.getExtension( MimeHelper.MIMETYPE_JPEG ) );
    assertEquals( MimeHelper.PNG_EXTENSION, MimeHelper.getExtension( MimeHelper.MIMETYPE_PNG ) );
    assertEquals( MimeHelper.SVG_EXTENSION, MimeHelper.getExtension( MimeHelper.MIMETYPE_SVG ) );
    assertEquals( MimeHelper.TIFF_EXTENSION_1, MimeHelper.getExtension( MimeHelper.MIMETYPE_TIFF ) );
    assertEquals( MimeHelper.CSV_EXTENSION, MimeHelper.getExtension( MimeHelper.MIMETYPE_CSV ) );
    assertEquals( MimeHelper.HTML_EXTENSION_1, MimeHelper.getExtension( MimeHelper.MIMETYPE_HTML ) );
    assertEquals( MimeHelper.TEXT_EXTENSION, MimeHelper.getExtension( MimeHelper.MIMETYPE_TEXT ) );
    assertEquals( MimeHelper.MPG_EXTENSION_1, MimeHelper.getExtension( MimeHelper.MIMETYPE_MPG ) );
    assertEquals( MimeHelper.AVI_EXTENSION, MimeHelper.getExtension( MimeHelper.MIMETYPE_AVI ) );
    assertEquals( MimeHelper.XACTION_EXTENSION, MimeHelper.getExtension( MimeHelper.MIMETYPE_XACTION ) );
    assertEquals( MimeHelper.CSS_EXTENSION, MimeHelper.getExtension( MimeHelper.MIMETYPE_CSS ) );
    assertEquals( MimeHelper.JS_EXTENSION, MimeHelper.getExtension( MimeHelper.MIMETYPE_JS ) );
    assertEquals( MimeHelper.XML_EXTENSION, MimeHelper.getExtension( MimeHelper.MIMETYPE_XML ) );
    assertEquals( MimeHelper.FLASH_EXTENSION, MimeHelper.getExtension( MimeHelper.MIMETYPE_FLASH ) );
    assertEquals( MimeHelper.MS_EXCEL_2007_EXTENSION, MimeHelper.getExtension( MimeHelper.MIMETYPE_MS_EXCEL_2007 ) );
    assertEquals( MimeHelper.EMAIL_MSG_EXTENSION, MimeHelper.getExtension( MimeHelper.MIMETYPE_EMAIL_MSG ) );
  }

  @Test
  public void getMimeTypeFromExtensionTest() {
    assertEquals( MimeHelper.MIMETYPE_RTF, MimeHelper.getMimeTypeFromExtension( MimeHelper.RTF_EXTENSION ) );
    assertEquals( MimeHelper.MIMETYPE_MS_WORD, MimeHelper.getMimeTypeFromExtension( MimeHelper.MS_WORD_EXTENSION ) );
    assertEquals( MimeHelper.MIMETYPE_PDF, MimeHelper.getMimeTypeFromExtension( MimeHelper.PDF_EXTENSION ) );
    assertEquals( MimeHelper.MIMETYPE_MS_EXCEL, MimeHelper.getMimeTypeFromExtension( MimeHelper.MS_EXCEL_EXTENSION ) );
    assertEquals( MimeHelper.MIMETYPE_MS_PPT, MimeHelper.getMimeTypeFromExtension( MimeHelper.MS_PPT_EXTENSION ) );
    assertEquals( MimeHelper.MIMETYPE_MS_PROJECT, MimeHelper.getMimeTypeFromExtension( MimeHelper.MS_PROJECT_EXTENSION ) );
    assertEquals( MimeHelper.MIMETYPE_ZIP, MimeHelper.getMimeTypeFromExtension( MimeHelper.ZIP_EXTENSION ) );
    assertEquals( MimeHelper.MIMETYPE_MP3, MimeHelper.getMimeTypeFromExtension( MimeHelper.MP3_EXTENSION ) );
    assertEquals( MimeHelper.MIMETYPE_WAV, MimeHelper.getMimeTypeFromExtension( MimeHelper.WAV_EXTENSION ) );
    assertEquals( MimeHelper.MIMETYPE_BMP, MimeHelper.getMimeTypeFromExtension( MimeHelper.BMP_EXTENSION ) );
    assertEquals( MimeHelper.MIMETYPE_GIF, MimeHelper.getMimeTypeFromExtension( MimeHelper.GIF_EXTENSION ) );
    assertEquals( MimeHelper.MIMETYPE_JPEG, MimeHelper.getMimeTypeFromExtension( MimeHelper.JPEG_EXTENSION_1 ) );
    assertEquals( MimeHelper.MIMETYPE_JPEG, MimeHelper.getMimeTypeFromExtension( MimeHelper.JPEG_EXTENSION_2 ) );
    assertEquals( MimeHelper.MIMETYPE_JPEG, MimeHelper.getMimeTypeFromExtension( MimeHelper.JPEG_EXTENSION_3 ) );
    assertEquals( MimeHelper.MIMETYPE_PNG, MimeHelper.getMimeTypeFromExtension( MimeHelper.PNG_EXTENSION ) );
    assertEquals( MimeHelper.MIMETYPE_SVG, MimeHelper.getMimeTypeFromExtension( MimeHelper.SVG_EXTENSION ) );
    assertEquals( MimeHelper.MIMETYPE_TIFF, MimeHelper.getMimeTypeFromExtension( MimeHelper.TIFF_EXTENSION_1 ) );
    assertEquals( MimeHelper.MIMETYPE_TIFF, MimeHelper.getMimeTypeFromExtension( MimeHelper.TIFF_EXTENSION_2 ) );
    assertEquals( MimeHelper.MIMETYPE_CSV, MimeHelper.getMimeTypeFromExtension( MimeHelper.CSV_EXTENSION ) );
    assertEquals( MimeHelper.MIMETYPE_HTML, MimeHelper.getMimeTypeFromExtension( MimeHelper.HTML_EXTENSION_1 ) );
    assertEquals( MimeHelper.MIMETYPE_HTML, MimeHelper.getMimeTypeFromExtension( MimeHelper.HTML_EXTENSION_2 ) );
    assertEquals( MimeHelper.MIMETYPE_TEXT, MimeHelper.getMimeTypeFromExtension( MimeHelper.TEXT_EXTENSION ) );
    assertEquals( MimeHelper.MIMETYPE_TEXT, MimeHelper.getMimeTypeFromExtension( MimeHelper.PROPERTIES_EXTENSION ) );
    assertEquals( MimeHelper.MIMETYPE_MPG, MimeHelper.getMimeTypeFromExtension( MimeHelper.MPG_EXTENSION_1 ) );
    assertEquals( MimeHelper.MIMETYPE_MPG, MimeHelper.getMimeTypeFromExtension( MimeHelper.MPG_EXTENSION_2 ) );
    assertEquals( MimeHelper.MIMETYPE_MPG, MimeHelper.getMimeTypeFromExtension( MimeHelper.MPG_EXTENSION_3 ) );
    assertEquals( MimeHelper.MIMETYPE_AVI, MimeHelper.getMimeTypeFromExtension( MimeHelper.AVI_EXTENSION ) );
    assertEquals( MimeHelper.MIMETYPE_XACTION, MimeHelper.getMimeTypeFromExtension( MimeHelper.XACTION_EXTENSION ) );
    assertEquals( MimeHelper.MIMETYPE_CSS, MimeHelper.getMimeTypeFromExtension( MimeHelper.CSS_EXTENSION ) );
    assertEquals( MimeHelper.MIMETYPE_JS, MimeHelper.getMimeTypeFromExtension( MimeHelper.JS_EXTENSION ) );
    assertEquals( MimeHelper.MIMETYPE_XML, MimeHelper.getMimeTypeFromExtension( MimeHelper.XML_EXTENSION ) );
    assertEquals( MimeHelper.MIMETYPE_FLASH, MimeHelper.getMimeTypeFromExtension( MimeHelper.FLASH_EXTENSION ) );
    assertEquals( MimeHelper.MIMETYPE_MS_EXCEL_2007, MimeHelper.getMimeTypeFromExtension( MimeHelper.MS_EXCEL_2007_EXTENSION ) );
    assertEquals( MimeHelper.MIMETYPE_EMAIL_MSG, MimeHelper.getMimeTypeFromExtension( MimeHelper.EMAIL_MSG_EXTENSION ) );
  }

  @Test
  public void getMimeTypeFromFileNameTest() {
    String filename = "test";
    assertEquals( MimeHelper.MIMETYPE_RTF, MimeHelper.getMimeTypeFromFileName( filename + MimeHelper.RTF_EXTENSION ) );
    assertEquals( MimeHelper.MIMETYPE_MS_WORD, MimeHelper.getMimeTypeFromFileName( filename + MimeHelper.MS_WORD_EXTENSION ) );
    assertEquals( MimeHelper.MIMETYPE_PDF, MimeHelper.getMimeTypeFromFileName( filename + MimeHelper.PDF_EXTENSION ) );
    assertEquals( MimeHelper.MIMETYPE_MS_EXCEL, MimeHelper.getMimeTypeFromFileName( filename + MimeHelper.MS_EXCEL_EXTENSION ) );
    assertEquals( MimeHelper.MIMETYPE_MS_PPT, MimeHelper.getMimeTypeFromFileName( filename + MimeHelper.MS_PPT_EXTENSION ) );
    assertEquals( MimeHelper.MIMETYPE_MS_PROJECT, MimeHelper.getMimeTypeFromFileName( filename + MimeHelper.MS_PROJECT_EXTENSION ) );
    assertEquals( MimeHelper.MIMETYPE_ZIP, MimeHelper.getMimeTypeFromFileName( filename + MimeHelper.ZIP_EXTENSION ) );
    assertEquals( MimeHelper.MIMETYPE_MP3, MimeHelper.getMimeTypeFromFileName( filename + MimeHelper.MP3_EXTENSION ) );
    assertEquals( MimeHelper.MIMETYPE_WAV, MimeHelper.getMimeTypeFromFileName( filename + MimeHelper.WAV_EXTENSION ) );
    assertEquals( MimeHelper.MIMETYPE_BMP, MimeHelper.getMimeTypeFromFileName( filename + MimeHelper.BMP_EXTENSION ) );
    assertEquals( MimeHelper.MIMETYPE_GIF, MimeHelper.getMimeTypeFromFileName( filename + MimeHelper.GIF_EXTENSION ) );
    assertEquals( MimeHelper.MIMETYPE_JPEG, MimeHelper.getMimeTypeFromFileName( filename + MimeHelper.JPEG_EXTENSION_1 ) );
    assertEquals( MimeHelper.MIMETYPE_JPEG, MimeHelper.getMimeTypeFromFileName( filename + MimeHelper.JPEG_EXTENSION_2 ) );
    assertEquals( MimeHelper.MIMETYPE_JPEG, MimeHelper.getMimeTypeFromFileName( filename + MimeHelper.JPEG_EXTENSION_3 ) );
    assertEquals( MimeHelper.MIMETYPE_PNG, MimeHelper.getMimeTypeFromFileName( filename + MimeHelper.PNG_EXTENSION ) );
    assertEquals( MimeHelper.MIMETYPE_SVG, MimeHelper.getMimeTypeFromFileName( filename + MimeHelper.SVG_EXTENSION ) );
    assertEquals( MimeHelper.MIMETYPE_TIFF, MimeHelper.getMimeTypeFromFileName( filename + MimeHelper.TIFF_EXTENSION_1 ) );
    assertEquals( MimeHelper.MIMETYPE_TIFF, MimeHelper.getMimeTypeFromFileName( filename + MimeHelper.TIFF_EXTENSION_2 ) );
    assertEquals( MimeHelper.MIMETYPE_CSV, MimeHelper.getMimeTypeFromFileName( filename + MimeHelper.CSV_EXTENSION ) );
    assertEquals( MimeHelper.MIMETYPE_HTML, MimeHelper.getMimeTypeFromFileName( filename + MimeHelper.HTML_EXTENSION_1 ) );
    assertEquals( MimeHelper.MIMETYPE_HTML, MimeHelper.getMimeTypeFromFileName( filename + MimeHelper.HTML_EXTENSION_2 ) );
    assertEquals( MimeHelper.MIMETYPE_TEXT, MimeHelper.getMimeTypeFromFileName( filename + MimeHelper.TEXT_EXTENSION ) );
    assertEquals( MimeHelper.MIMETYPE_TEXT, MimeHelper.getMimeTypeFromFileName( filename + MimeHelper.PROPERTIES_EXTENSION ) );
    assertEquals( MimeHelper.MIMETYPE_MPG, MimeHelper.getMimeTypeFromFileName( filename + MimeHelper.MPG_EXTENSION_1 ) );
    assertEquals( MimeHelper.MIMETYPE_MPG, MimeHelper.getMimeTypeFromFileName( filename + MimeHelper.MPG_EXTENSION_2 ) );
    assertEquals( MimeHelper.MIMETYPE_MPG, MimeHelper.getMimeTypeFromFileName( filename + MimeHelper.MPG_EXTENSION_3 ) );
    assertEquals( MimeHelper.MIMETYPE_AVI, MimeHelper.getMimeTypeFromFileName( filename + MimeHelper.AVI_EXTENSION ) );
    assertEquals( MimeHelper.MIMETYPE_XACTION, MimeHelper.getMimeTypeFromFileName( filename + MimeHelper.XACTION_EXTENSION ) );
    assertEquals( MimeHelper.MIMETYPE_CSS, MimeHelper.getMimeTypeFromFileName( filename + MimeHelper.CSS_EXTENSION ) );
    assertEquals( MimeHelper.MIMETYPE_JS, MimeHelper.getMimeTypeFromFileName( filename + MimeHelper.JS_EXTENSION ) );
    assertEquals( MimeHelper.MIMETYPE_XML, MimeHelper.getMimeTypeFromFileName( filename + MimeHelper.XML_EXTENSION ) );
    assertEquals( MimeHelper.MIMETYPE_FLASH, MimeHelper.getMimeTypeFromFileName( filename + MimeHelper.FLASH_EXTENSION ) );
    assertEquals( MimeHelper.MIMETYPE_MS_EXCEL_2007, MimeHelper.getMimeTypeFromFileName( filename + MimeHelper.MS_EXCEL_2007_EXTENSION ) );
    assertEquals( MimeHelper.MIMETYPE_EMAIL_MSG, MimeHelper.getMimeTypeFromFileName( filename + MimeHelper.EMAIL_MSG_EXTENSION ) );
  }

  @Test
  public void getDefaultCharsetTest() {
    assertEquals( UTF_ENCODING, MimeHelper.getDefaultCharset( MimeHelper.MIMETYPE_HTML ) );
    assertEquals( UTF_ENCODING, MimeHelper.getDefaultCharset( MimeHelper.MIMETYPE_TEXT ) );
  }

  @Test
  public void verifyNullForBadMimeTypesTest() {
    String falseMimeType = "false/mimeType";
    String falseExtension = ".badExt";
    String badFilename = "test.badExt";
    String noExtension = "test";
    assertNull( MimeHelper.getExtension( falseMimeType ) );
    assertNull( MimeHelper.getMimeTypeFromExtension( falseExtension ) );
    assertNull( MimeHelper.getMimeTypeFromFileName( badFilename ) );
    assertNull( MimeHelper.getMimeTypeFromFileName( noExtension ) );
    assertNull( MimeHelper.getDefaultCharset( "false/mimeType" ) );
  }

  @Test
  public void testGetExtensionReturningDefaultValue() {
    String falseMimeType = "false/mimeType";
    String defaultExtension = ".bin";
    assertNull( MimeHelper.getExtension( falseMimeType ) );
    assertEquals( defaultExtension, MimeHelper.getExtension( falseMimeType, defaultExtension ) );
  }
}
