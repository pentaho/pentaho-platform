/*
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
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved.
 *
 * @created Jul 1, 2005 
 * @author Marc Batchelor
 * 
 */

package org.pentaho.platform.api.repository;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Date;
import java.util.List;

import org.pentaho.commons.connection.IPentahoStreamSource;
import org.pentaho.platform.api.engine.IMimeTypeListener;

/**
 * 
 * Construction of a new ContentItem is managed by
 * ContentLocation.newContentItem. This is because there is a parent-child
 * relationship between ContentLocations and content items. To avoid having a
 * content item reach back into it's parent to add itself to the children list,
 * you instead call the content location to construct a content item. A content
 * item cannot exist without a content location.
 * 
 * @see ContentLocation
 * @author mbatchel
 * 
 */
public interface IContentItem extends IMimeTypeListener {

  /**
   * Keep multiple versions when request for an output stream is received
   */
  public static final int WRITEMODE_KEEPVERSIONS = 0;

  /**
   * Overwrite each time a request for a new output stream is received.
   */
  public static final int WRITEMODE_OVERWRITE = 1;

  /**
   * Append to existing file when request for a new output stream is received.
   */
  public static final int WRITEMODE_APPEND = 2;

  // Probably not necessary or ever used. Wanted to consider it though.

  /**
   * @return The ContentItem Id
   */
  public String getId();

  /**
   * @return The ContentItem path
   */
  public String getPath();

  /**
   * @return The name of the content item
   */
  public String getName();

  /**
   * @return The title of the content item
   */
  public String getTitle();

  /**
   * @return The MimeType of the content item.
   */
  public String getMimeType();

  /**
   * @return The URL (optional) of the content item
   */
  public String getUrl();

  /**
   * @return If this is a multiple-versioned style of content item, return the
   *         whole list for admin purposes
   */
  @SuppressWarnings("unchecked")
  public List getFileVersions();

  /**
   * Removes all the version from Hibernate
   * 
   */
  public void removeAllVersions();

  /**
   * Removes the file with the id specified
   */
  public void removeVersion(String fileId);

  /**
   * Gets an input stream from the Content item. If the content item doesn't
   * exist on disk, throws an exception
   * 
   * @return An input stream from the file system that is represented by this
   *         content item
   * @throws ContentException
   */
  public InputStream getInputStream() throws ContentException;

  /**
   * Returns a reader from the content item. If the content item doesn't exist
   * an exception is thrown.
   * 
   * @return A Reader from the file system that is pointed to by this content
   *         item.
   * @throws ContentException
   */
  public IPentahoStreamSource getDataSource();

  public Reader getReader() throws ContentException;

  /**
   * The behavior of this method depends upon it's write mode (defined only at
   * construction).
   * 
   * If the write mode is WRITEMODE_KEEPVERSIONS, this method will create a
   * new file on the disk, and add it to it's internal list of files, and
   * return an output stream.
   * 
   * If the write mode is WRITEMODE_OVERWRITE, this method will create an
   * output stream and overwrite the existing file on the disk if it's found,
   * or will create the file if it doesn't exist.
   * 
   * If the write mode is WRITEMODE_APPEND, this method will append to the
   * existing file on disk (if it exists), or create it if it doesn't exist.
   * 
   * @param actionName
   *            The name of the action that is obtaining the output stream.
   * @throws IOException
   * @return the OutputStream to write to
   */
  public OutputStream getOutputStream(String actionName) throws IOException;

  public void closeOutputStream();

  /**
   * @return The name of the action from the latest ContentItemFile class that
   *         the ContentItem contains.
   */
  public String getActionName();

  /**
   * @return This returns the Id of the ContentItemFile class that the
   *         ContentItem contains.
   */
  public String getFileId();

  /**
   * @return The file size from the latest ContentItemFile class that the
   *         ContentItem contains.
   */
  public long getFileSize();

  /**
   * @return The file date/time from the latest ContentItemFile class that the
   *         ContentItem contains.
   */
  public Date getFileDateTime();

  /**
   * Sets the mime type
   * 
   * @param mimeType
   *            The mime type to set.
   */
  public void setMimeType(String mimeType);

  /**
   * Removes all versions of this item from
   * the repository and removes this item
   * from underlying persistence layer.
   *
   */
  public void makeTransient();

}
