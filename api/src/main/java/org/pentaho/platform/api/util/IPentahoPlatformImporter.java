package org.pentaho.platform.api.util;

import org.pentaho.platform.api.importexport.IImportHelper;

public interface IPentahoPlatformImporter {
  void addImportHelper( IImportHelper helper );

  IRepositoryExportLogger getRepositoryImportLogger();
}
