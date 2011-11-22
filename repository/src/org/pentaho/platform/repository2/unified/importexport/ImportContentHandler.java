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
package org.pentaho.platform.repository2.unified.importexport;

import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;

import java.util.Map;

/**
 * The interface for any content handler that will handle the import process for any content type
 * User: dkincade
 */
public interface ImportContentHandler {
  /**
   * Enumeration which defines the results of the processing of the import</p>
   * <ul>
   * <li>{@code SUCCESS} indicates the handler processed the items correctly and other handlers should be skipped</li>
   * <li>{@code SKIPPED} indicates the handler can/should not process the item and it should be passed to the other
   * handlers in the list</li>
   * </ul>
   */
  enum Result {
    SUCCESS,
    SKIPPED,
  };

  /**
   * Performs any initialization required prior to handling any import processing
   *
   * @param repository            the {@link org.pentaho.platform.api.repository2.unified.IUnifiedRepository} into which content is being imported
   * @param converters
   * @param destinationFolderPath
   * @param versionMessage
   */
  public void initialize(final IUnifiedRepository repository, final Map<String, Converter> converters, final String destinationFolderPath, final String versionMessage) throws
      InitializationException;

  /**
   * Attempts to perform the import process on the bundle. The handler will determine if it should handle the import
   * process for this content and will perform the processing specific to this content and type. If this handler
   * completes the import process and knows that all other handlers should be skipped, it will return {@code true}.
   * Otherwise it will return {@code false}.
   *
   * @param bundle    the information being imported
   * @param overwrite indicates if this content handler should overwrite existing content with this new content
   * @return {@code true} if processing on this bundle should continue by other handlers, {@code false} otherwise.
   * @throws ImportException indicates an error trying to perform the import process on this content
   */
  public Result performImport(final ImportSource.IRepositoryFileBundle bundle,
                              final boolean overwrite) throws ImportException;

  /**
   * Returns a simple name to describe this ImportContentHandler
   *
   * @return
   */
  public String getName();
}
