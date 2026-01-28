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

  public static final int PUBLISH_JOB_OR_TRANS_WITH_MISSING_PLUGINS = 11;

  public static final int PUBLISH_PARTIAL_UPLOAD = 12;

  public static final int PUBLISH_NAME_ERROR = 13;

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
