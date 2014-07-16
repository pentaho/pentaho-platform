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

package org.pentaho.platform.plugin.services.importer;

/**
 * User: nbaker Date: 5/29/12 modifed: tband Date: 7/9/12 1 - Publish failed 2 - General Error - 6 - Data source problem
 * 7 - XMLA Catalog Exists 8 - XMLA Schema Exists 9 - Content exists
 * 
 * 
 */
public class PlatformImportException extends Exception {

  public static final int PUBLISH_TO_SERVER_FAILED = 1;

  public static final int PUBLISH_GENERAL_ERROR = 2;

  public static final int PUBLISH_DATASOURCE_ERROR = 6;

  public static final int PUBLISH_USERNAME_PASSWORD_FAIL = 5;

  public static final int PUBLISH_XMLA_CATALOG_EXISTS = 7;

  public static final int PUBLISH_SCHEMA_EXISTS_ERROR = 8;

  public static final int PUBLISH_CONTENT_EXISTS_ERROR = 9;

  public static final int PUBLISH_PROHIBITED_SYMBOLS_ERROR = 10;

  private int errorStatusCode = PlatformImportException.PUBLISH_TO_SERVER_FAILED;

  public PlatformImportException( String s ) {
    super( s );
  }

  public PlatformImportException( String s, int statusCode ) {
    super( s );
    this.errorStatusCode = statusCode;
  }

  public PlatformImportException( String s, Throwable t ) {
    super( s, t );
  }

  public PlatformImportException( String s, int statusCode, Throwable t ) {
    super( s, t );
    this.errorStatusCode = statusCode;
  }

  public int getErrorStatus() {
    return this.errorStatusCode;
  }
}
