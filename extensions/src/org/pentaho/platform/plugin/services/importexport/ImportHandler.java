/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * Copyright 2011 Pentaho Corporation. All rights reserved.
 */
package org.pentaho.platform.plugin.services.importexport;

/**
 * Handles the import process for a specific content type
 *
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public interface ImportHandler {
  /**
   * Returns the name of this Import Handler
   */
  public String getName();

  /**
   * Processes the list of files and performs any processing required to import that data into the repository. If
   * during processing it handles file(s) which should not be handled by downstream import handlers, then it
   * should remove them from the set of files provided.
   *
   * @param importFileSet   the set of files to be imported - any files handled to completion by this Import Handler
   *                        should remove this files from this list
   * @param destinationPath the requested destination location in the repository
   * @param comment         the import comment provided
   * @param overwrite       indicates if the process is authorized to overwrite existing content in the repository
   * @throws ImportException indicates a significant error during import processing
   */
  public void doImport(final Iterable<ImportSource.IRepositoryFileBundle> importFileSet,
                       final String destinationPath, final String comment, final boolean overwrite)
      throws ImportException;
}
