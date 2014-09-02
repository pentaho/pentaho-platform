/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.plugin.services.importer;

import org.pentaho.platform.plugin.services.importexport.IRepositoryImportLogger;

import java.util.Map;

/**
 * Implementations handle importing the given content into the Pentaho System.
 * <p/>
 * User: nbaker Date: 6/18/12
 */
public interface IPlatformImporter {
  /**
   * Import the given IPlatformImportBundle into the system.
   *
   * @param bundle
   * @throws PlatformImportException
   */
  void importFile( IPlatformImportBundle bundle ) throws PlatformImportException;

  IRepositoryImportLogger getRepositoryImportLogger();

  /**
   * Add a new IPlatformImportHandler to process the MimeTypes given by that import handler.  An
   * Import Handler registers what MimeTypes it processes via the getMimeTypes() method.  This
   * IPlatformImporter then adds the MimeTypes to its master list, and, adds the extensions handled
   * by the mimetypes to the IPlatformImportResolver managed by this class.
   */
  void addHandler( IPlatformImportHandler platformImportHandler );

  /**
   * Returns the platform importer's map of registered handlers
   */
  Map<String, IPlatformImportHandler> getHandlers();
}
