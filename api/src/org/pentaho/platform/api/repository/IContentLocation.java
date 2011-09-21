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

import java.io.File;
import java.util.Iterator;

/**
 * A Content location is analagous to a folder. It is the location of the content.
 * 
 * @see IContentItem
 * @see IContentRepository
 * @author mbatchel
 *
 */

public interface IContentLocation {

  /**
   * Create a new ContentItem parented to this content location.
   * 
   * @param name
   *            The name of the content item
   * @param title
   *            The title of the content item
   * @param extension
   *            The extension (i.e. .txt or .pdf) of the content item.
   * @param mimeType
   *            The mime type of the content item
   * @param url
   *            Optional URL to get to the content.
   * @param writeMode
   *            The write mode of the content item. Please see IContentItem
   *            for valid write modes
   * @return A new IContentItem instance, parented to the ContentLocation
   * @throws ContentException
   */
  public IContentItem newContentItem(String name, String title, String extension, String mimeType, String url,
      int writeMode) throws ContentException;

  /**
   * Create a new ContentItem parented to this content location. This version
   * is used when the content Id is already generated.
   * 
   * @param contentId
   *            The Identifier for the new content item
   *            
   * @param name
   *            The name of the content item
   * @param title
   *            The title of the content item
   * @param extension
   *            The extension (i.e. .txt or .pdf) of the content item.
   * @param mimeType
   *            The mime type of the content item
   * @param url
   *            Optional URL to get to the content.
   * @param writeMode
   *            The write mode of the content item. Please see IContentItem
   *            for valid write modes
   * @return A new IContentItem instance, parented to the ContentLocation
   * @throws ContentException
   */
  public IContentItem newContentItem(String contentId, String name, String title, String extension, String mimeType,
      String url, int writeMode) throws ContentException;

  /**
   * @return The revision of the content item (as determined by Hibernate)
   */
  public int getRevision();

  /**
   * Iterates over registered content items.
   * 
   * @return Iterator of the child content
   */
  @SuppressWarnings("unchecked")
  public Iterator getContentItemIterator();

  /**
   * Gets a content item by its Id - this is the most efficient way to get a
   * content item from a location
   * 
   * @param contentItemId
   *            The id to retrieve
   * @return The content item
   */
  public IContentItem getContentItemById(String contentItemId);

  /**
   * Gets a child content item by name. Returns the ContentItem with the
   * specified name, and a parent of the content location
   * 
   * @param name
   *            The name to find
   * @return ContentItem
   */
  public IContentItem getContentItemByName(String name);

  /**
   * Returns the contentitem with the specified path
   * 
   * @param path
   *            The path to look for
   * @return The content item
   */
  public IContentItem getContentItemByPath(String path);

  /**
   * Creates a subdirectory in the content location.
   * 
   * @param subDirName
   *            The directory name to create
   * @return File created
   * @throws ContentException
   */
  public File makeSubdirectory(String subDirName) throws ContentException;

  /**
   * @return The directory path
   */
  public String getDirPath();

  /**
   * @return Returns the UUID of the content location
   */
  public String getId();

  /**
   * @return The name of the content location
   */
  public String getName();

  /**
   * @return The Solution Id
   */
  public String getSolutionId();

  /**
   * @return The description of the Content Location
   */
  public String getDescription();

}
