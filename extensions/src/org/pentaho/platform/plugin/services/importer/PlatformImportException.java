package org.pentaho.platform.plugin.services.importer;

/**
 * User: nbaker
 * Date: 5/29/12
 * modifed: tband
 * Date: 7/9/12
 * 1 - Publish failed
 * 2 - General Error -
 * 6 - Data source problem
 * 7 - XMLA Catalog Exists
 * 8 - XMLA Schema Exists
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

  private int errorStatusCode = 1;

  public PlatformImportException(String s) {
    super(s);
  }

  public PlatformImportException(String s, int statusCode) {
    super(s);
    this.errorStatusCode = statusCode;
  }

  public PlatformImportException(String s, Throwable t) {
    super(s, t);
  }

  public PlatformImportException(String s, int statusCode, Throwable t) {
    super(s, t);
    this.errorStatusCode = statusCode;
  }

  public int getErrorStatus() {
    return this.errorStatusCode;
  }
}
