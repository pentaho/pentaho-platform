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
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.plugin.services.importexport;

import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;

import java.io.IOException;
import java.io.InputStream;

/**
 * A source of files for import operations.
 * 
 * @author mlowery
 */
public interface ImportSource {

  /**
   * Returns the set of files available by this Import Source.</p> NOTE: the Iterable object <b>MUST</b> support the
   * {@code Iterator.remove()} method as the consumer of this information will be iterating over the data and will
   * remove items that they process (and shouldn't be processed by down-stream consumers)
   * 
   * @throws IOException
   *           indicates an error getting the files
   */
  public Iterable<IRepositoryFileBundle> getFiles() throws IOException;

  /**
   * Returns the number of files to process (or -1 if that is not known)
   */
  public int getCount();

  /**
   * A struct-like object for bundling related objects together.
   * 
   * @author mlowery
   */
  interface IRepositoryFileBundle {

    RepositoryFile getFile();

    RepositoryFileAcl getAcl();

    /**
     * Gets the stream (if !getFile().isFolder()).
     */
    InputStream getInputStream() throws IOException;

    /**
     * Path to file. Will be appended to destination folder path to create absolute path.
     */
    String getPath();

    void setPath( String path );

    /**
     * Null for folders or binary types.
     */
    String getCharset();

    String getMimeType();

  }
}
