package org.pentaho.platform.api.importexport;

public class ImportException extends Exception {
  public ImportException( ) {
  }

  public ImportException( final String message ) {
    super( message );
  }

  public ImportException( final String message, final Throwable cause ) {
    super( message, cause );
  }

  public ImportException( final Throwable cause ) {
    super( cause );
  }
}

