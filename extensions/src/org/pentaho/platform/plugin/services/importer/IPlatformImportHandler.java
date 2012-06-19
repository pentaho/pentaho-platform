package org.pentaho.platform.plugin.services.importer;

/**
 * Implementations handle importing content into the repository.
 *
 * User: nbaker
 * Date: 5/29/12
 */
public interface IPlatformImportHandler {

  /**
   * Import the provided IPlatformImportBundle into the platform.
   *
   * @param bundle
   * @throws PlatformImportException
   */
  void importFile(IPlatformImportBundle bundle) throws PlatformImportException;
}
