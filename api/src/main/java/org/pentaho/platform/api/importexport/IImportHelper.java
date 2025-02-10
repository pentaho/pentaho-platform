package org.pentaho.platform.api.importexport;

public interface IImportHelper {
  void doImport( Object importArg ) throws ImportException;

  String getName();
}
