/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.platform.api.util;

import org.pentaho.platform.api.importexport.IExportHelper;
import org.pentaho.platform.api.importexport.ExportException;

public interface IPentahoPlatformExporter {
  void addExportHelper( IExportHelper helper );

  IRepositoryExportLogger getRepositoryExportLogger();

  /**
   * Export a specific file from the repository to the export bundle.
   * Used by export helpers to export files referenced by other components (e.g., files referenced by schedules).
   * 
   * @param repositoryFilePath the repository path of the file to export
   * @throws ExportException if the file cannot be exported
   */
  void exportFileByPath( String repositoryFilePath ) throws ExportException;

  /**
   * Export a single user and their roles with user settings.
   * Called by plugins (e.g., scheduler) to export individual users as dependencies.
   * 
   * @param username the username to export
   * @return true if the user was successfully exported, false otherwise
   */
  boolean exportUserAndRole( String username );

  /**
   * Export only the runtime-to-logical role bindings for the roles assigned to the given user,
   * without exporting the user object itself. Used when the platform is configured with an external
   * authentication provider (jdbc/ldap): user and role objects are managed externally, but their
   * logical-role bindings live in the Pentaho repository and must be backed up so they can be
   * restored.
   *
   * @param username the username whose role mappings should be exported
   * @return true if the role mappings were exported successfully, false otherwise
   */
  boolean exportUserRoleBindings( String username );

  Object getComponentConfig();

}
