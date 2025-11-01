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


package org.pentaho.platform.api.repository;

import org.pentaho.commons.connection.IPentahoStreamSource;
import org.pentaho.platform.api.engine.IMimeTypeListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 
 * Construction of a new ContentItem is managed by ContentLocation.newContentItem. This is because there is a
 * parent-child relationship between ContentLocations and content items. To avoid having a content item reach back
 * into it's parent to add itself to the children list, you instead call the content location to construct a
 * content item. A content item cannot exist without a content location.
 * 
 * @see ContentLocation
 * @author mbatchel
 * 
 */
public interface IContentItem extends IMimeTypeListener {

  /**
   * @return The ContentItem path
   */
  public String getPath();

  /**
   * @return The MimeType of the content item.
   */
  public String getMimeType();

  /**
   * Gets an input stream from the Content item. If the content item doesn't exist on disk, throws an exception
   * 
   * @return An input stream from the file system that is represented by this content item
   * @throws ContentException
   */
  public InputStream getInputStream() throws ContentException;

  public IPentahoStreamSource getDataSource();

  /**
   * The behavior of this method depends upon it's write mode (defined only at construction).
   * 
   * If the write mode is WRITEMODE_KEEPVERSIONS, this method will create a new file on the disk, and add it to
   * it's internal list of files, and return an output stream.
   * 
   * If the write mode is WRITEMODE_OVERWRITE, this method will create an output stream and overwrite the existing
   * file on the disk if it's found, or will create the file if it doesn't exist.
   * 
   * If the write mode is WRITEMODE_APPEND, this method will append to the existing file on disk (if it exists), or
   * create it if it doesn't exist.
   * 
   * @param actionName
   *          The name of the action that is obtaining the output stream.
   * @throws IOException
   * @return the OutputStream to write to
   */
  public OutputStream getOutputStream( String actionName ) throws IOException;

  public void closeOutputStream();

  /**
   * Sets the mime type
   * 
   * @param mimeType
   *          The mime type to set.
   */
  public void setMimeType( String mimeType );

}
