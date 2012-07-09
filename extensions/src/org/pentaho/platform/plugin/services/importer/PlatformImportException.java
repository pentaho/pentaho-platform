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
  int errorStatusCode = 1;

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
}
