package org.pentaho.platform.api.importexport;

public interface IImportHelper {
  void doImport( Object importArg ) throws ImportException;

  String getName();

  /**
   * Determines if this helper should execute for the given restore profile.
   * Allows helpers to be profile-aware without the framework needing to know about them.
   * 
   * @param componentOverrides the backup component configuration (null = full restore)
   *                           Implementation receives BackupComponentConfig from plugin layer
   * @return true if this helper should execute, false to skip
   */
  default boolean shouldExecute( Object componentOverrides ) {
    // Default: always execute (for backward compatibility with existing helpers)
    return true;
  }
}
