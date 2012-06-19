package org.pentaho.platform.plugin.services.importer;

/**
 * User: nbaker
 * Date: 5/29/12
 */
public class PlatformImportException extends Exception {
  public PlatformImportException(String s) {
    super(s);
  }

  public PlatformImportException(String s, Throwable t){
    super(s, t);
  }
}
