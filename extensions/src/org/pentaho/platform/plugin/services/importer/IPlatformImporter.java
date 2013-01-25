package org.pentaho.platform.plugin.services.importer;

import org.pentaho.platform.plugin.services.importexport.IRepositoryImportLogger;

/**
 * Implementations handle importing the given content into the Pentaho System.
 *
 * User: nbaker
 * Date: 6/18/12
 */
public interface IPlatformImporter {
  /**
   * Import the given IPlatformImportBundle into the system.
   * @param bundle
   * @throws PlatformImportException
   */
  void importFile(IPlatformImportBundle bundle) throws PlatformImportException;
  
  IRepositoryImportLogger getRepositoryImportLogger();
}
