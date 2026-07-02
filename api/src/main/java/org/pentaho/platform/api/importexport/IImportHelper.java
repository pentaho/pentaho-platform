package org.pentaho.platform.api.importexport;

import org.apache.commons.logging.Log;

public interface IImportHelper {

  public interface ImportContext {
    Log getLogger();
    boolean isPerformingRestore();
    boolean isOverwriteFile();
  }

  void doImport( ImportContext context ) throws ImportException;

  String getName();
}
