/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


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
