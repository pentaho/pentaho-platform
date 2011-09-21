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

import java.util.Date;
import java.util.List;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISessionContainer;

/**
 * The ContentRepository is responsible for all the DAO calls used to get
 * content items out of a repository. This interface is used to create
 * <tt>IContentLocation</tt> objects. The <tt>IContentLocation</tt> object can then
 * be used to create child <tt>IContentItem</tt> objects.
 * @author mbatchel
 *
 */

public interface IContentRepository extends ISessionContainer {

  /**
   * Creates a new Content Location. A content location is analogous to a
   * folder
   * 
   * @param thePath
   *            The path
   * @param theName
   *            The name of the location
   * @param description
   *            The description of the location
   * @param solutionId
   *            The Id of the solution
   * @param createIfNotExists
   *            Attempt to create the physical folder on the hard drive
   * @return The new content location
   * @throws RepositoryException
   */
  public IContentLocation newContentLocation(String thePath, String theName, String description, String solutionId,
      boolean createIfNotExists) throws RepositoryException;

  /**
   * Retrieves a content location by the path.
   * 
   * @param thePath
   *            The path to search for
   * @return The content location
   * @throws RepositoryException
   */
  public IContentLocation getContentLocationByPath(String thePath) throws RepositoryException;

  /**
   * Retrieves a content location by Id. This is the most efficient way to
   * retrieve a content location
   * 
   * @param theId
   *            The Id to retrieve
   * @return The content location
   */
  public IContentLocation getContentLocationById(String theId);

  /**
   * Gets a content item by path
   * 
   * @param thePath
   *            The path of the content item to find
   * @return The Content Item with the specified path
   */
  public IContentItem getContentItemByPath(String thePath);

  /**
   * Gets a content item by id. This is the most efficient way to retrieve a
   * content item.
   * 
   * @param id
   *            The id of the content item to find
   * @return The Content Item with the specified path
   */
  public IContentItem getContentItemById(String theId);

  /**
   * @return A list of all content locations
   */
  @SuppressWarnings("unchecked")
  public List getAllContentLocations();

  /**
   * Content Location finder - searches for the terms amongst the content
   * locations
   * 
   * @param searchTerm
   *            The search term(s) to find
   * @param searchType
   * @see org.pentaho.platform.api.repository.ISearchable
   * @return List of matching Content Locations.
   */
  @SuppressWarnings("unchecked")
  public List searchLocationsForTerms(String searchTerm, int searchType);

  /**
   * Content Item finder - searches for the terms amongst content items
   * 
   * @param searchTerm
   *            The search term(s) to find
   * @param searchType
   * @see org.pentaho.platform.api.repository.ISearchable
   * @return List of matching Content Items.
   */
  @SuppressWarnings("unchecked")
  public List searchContentItemsForTerms(String searchTerm, int searchType);

  /**
   * Content Location and Item finder - Simply aggregates the output of the
   * searchLocationsForTerms and searchContentItemsForTerms.
   * 
   * @param searchTerm
   *            The search term(s) to find
   * @param searchType
   * @see org.pentaho.platform.api.repository.ISearchable
   * @return List of matching Content Locations first, followed by Content
   *         Items.
   */
  @SuppressWarnings("unchecked")
  public List searchLocationsAndItemsForTerms(String searchTerm, int searchType);

  /**
   * @param session
   *            Sets the IPentahoSession Content Repository. This also begins
   *            the Hibernate transaction.
   */
  public void setSession(IPentahoSession session);

  /**
   * This method is used to delete ContentItemFile objects that are older than
   * the specified date.
   * 
   * @param agingDate
   *            Date to use for selecting items for deleting. The argument is
   *            used as a "Less Than". The date is NOT inclusive. I.e., not
   *            "Less Than or Equal To".
   * @return Count of content item files that were removed from the content
   *         repository and the file system.
   */
  public int deleteContentOlderThanDate(Date agingDate);

  /**
   * Returns a new background executed content object
   * @param session Users' session object
   * @param contentId The content id to reference.
   * @return new BackgroundExecutedContent
   */
  public IBackgroundExecutedContentId newBackgroundExecutedContentId(IPentahoSession session, String contentId);

  /**
   * Gets list of Content Items from a users' background execution list.
   * @param session The users' session
   * @return List of IContentItem objects
   */
  @SuppressWarnings("unchecked")
  public List getBackgroundExecutedContentItemsForUser(IPentahoSession session);

  /**
   * Gets list of all content items in the Background Execution id list. Should only be used in an administrative capacity
   * @param session Users session
   * @return List of IContentItem objects
   */
  @SuppressWarnings("unchecked")
  public List getAllBackgroundExecutedContentItems(IPentahoSession session);

  /**
   * Removes an ID from the background executed content Id list
   * @param session Users' session
   * @param contentId The content id to remove.
   */
  public void removeBackgroundExecutedContentId(IPentahoSession session, String contentId);

}
