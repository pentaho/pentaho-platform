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

/**
 * Implementations handle importing the given content into the Pentaho System.
 * 
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

  /**
   * Import the given IPlatformImportBundle into the system.
   * 
   * @param mimeTypes This is an array of mimetypes that the handler deals with
   * @param extensions This is an array of extensions that the handler deals with, typically this is
   * a fallback for cases where we do not have the mimeType of a file.  An example of this is
   * when we upload pentaho specific file types via web browser, the extension is used instead of the
   * mimeType.
   * @param handler This is the handler that can import content for the provided mimeTypes/extensions
   * 
  **/
  void addHandler( String mimeTypes[], String extensions[], IPlatformImportHandler handler );
  
  IRepositoryImportLogger getRepositoryImportLogger();
}
