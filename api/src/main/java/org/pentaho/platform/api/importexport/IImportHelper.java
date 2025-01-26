package org.pentaho.platform.api.importexport;

public interface IImportHelper {
  void doImport( Object exportArg ) throws ImportException;

  String getName();
}
