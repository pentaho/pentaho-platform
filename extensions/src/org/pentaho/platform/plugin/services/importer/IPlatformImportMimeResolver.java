package org.pentaho.platform.plugin.services.importer;

/**
 * Implementations of this class compute mime-types based on the given IPlatformImportBundle.
 *
 * User: nbaker
 * Date: 6/18/12
 */
public interface IPlatformImportMimeResolver {
  String resolveMimeForBundle(IPlatformImportBundle bundle);
  String resolveMimeForFileName(String fileName);
}
