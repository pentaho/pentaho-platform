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
 * Copyright 2005-2008 Pentaho Corporation.  All rights reserved. 
 * 
 * @created Jul 1, 2005 
 * @author Marc Batchelor
 * 
 */

package org.pentaho.platform.repository.content;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.pentaho.platform.api.engine.IBackgroundExecution;
import org.pentaho.platform.api.engine.IPentahoInitializer;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.ContentException;
import org.pentaho.platform.api.repository.IBackgroundExecutedContentId;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.api.repository.IContentLocation;
import org.pentaho.platform.api.repository.IContentRepository;
import org.pentaho.platform.api.repository.ISearchable;
import org.pentaho.platform.api.repository.RepositoryException;
import org.pentaho.platform.engine.core.system.PentahoBase;
import org.pentaho.platform.repository.hibernate.HibernateUtil;
import org.pentaho.platform.repository.messages.Messages;
import org.pentaho.platform.util.UUIDUtil;

public class ContentRepository extends PentahoBase implements IContentRepository, IPentahoInitializer {

  /**
   * 
   */
  private static final long serialVersionUID = -1096153176439041908L;

  private static final Log logger = LogFactory.getLog(ContentRepository.class);

  private static final ThreadLocal threadSession = new ThreadLocal();

  /**
   * @return Returns the userSession.
   */
  public static IPentahoSession getUserSession() {
    IPentahoSession userSession = (IPentahoSession) ContentRepository.threadSession.get();
    return userSession;
  }

  /**
   * Constructor
   */

  public ContentRepository() {

  }

  public List getMessages() {
    return null;
  }

  public void setSession(final IPentahoSession session) {
    ContentRepository.threadSession.set(session);
    if (session != null) {
      genLogIdFromSession(session);
      HibernateUtil.beginTransaction();
    }
  }

  public void init(final IPentahoSession session) {
    this.setSession(session);
  }

  public static IContentRepository getInstance(final IPentahoSession sess) {
    ContentRepository.threadSession.set(sess);
    IContentRepository rtn = new ContentRepository();
    rtn.setSession(sess);
    return rtn;
  }

  public IContentLocation newContentLocation(final String thePath, final String theName, final String description,
      final String solId, final boolean createIfNotExists) {
    debug(Messages.getInstance().getString("CONTREP.DEBUG_NEW_LOCATION", thePath)); //$NON-NLS-1$
    Session session = HibernateUtil.getSession();
    String locId = UUIDUtil.getUUIDAsString();
    ContentLocation cl = new ContentLocation(locId, thePath, theName, description, solId, createIfNotExists);
    debug(Messages.getInstance().getString("CONTREP.DEBUG_CREATE_LOCATION_ID", locId)); //$NON-NLS-1$
    try {
      session.save(cl);
      session.flush();
    } catch (HibernateException ex) {
      error(Messages.getInstance().getErrorString("CONTREP.ERROR_0004_SAVING_LOCATION"), ex); //$NON-NLS-1$
      throw new RepositoryException(Messages.getInstance().getErrorString("CONTREP.ERROR_0004_SAVING_LOCATION"), ex); //$NON-NLS-1$
    }
    return cl;
  }

  public IContentLocation getContentLocationById(final String theId) {
    Session session = HibernateUtil.getSession();
    try {
      return (ContentLocation) session.get(ContentLocation.class, theId);
    } catch (HibernateException ex) {
      throw new ContentException(Messages.getInstance().getErrorString("CONTREP.ERROR_0002_GETTING_LOCATION", theId), ex); //$NON-NLS-1$
    }
  }

  public IContentLocation getContentLocationByPath(final String thePath) {
    Session session = HibernateUtil.getSession();
    Query qry = session
        .getNamedQuery("org.pentaho.platform.repository.content.ContentLocation.findContentLocationByPath"); //$NON-NLS-1$
    qry.setString("inPath", thePath); //$NON-NLS-1$
    Object rtn = null;
    try {
      rtn = qry.uniqueResult();
    } catch (Exception ignored) {
    }
    return (ContentLocation) rtn;
  }

  public IContentItem getContentItemByPath(final String thePath) {
    Session session = HibernateUtil.getSession();
    Query qry = session.getNamedQuery("org.pentaho.platform.repository.content.ContentItem.findItemByPath"); //$NON-NLS-1$
    qry.setString("inPath", thePath); //$NON-NLS-1$
    Object rtn = null;
    try {
      rtn = qry.uniqueResult();
    } catch (Exception ignored) {
    }
    return (IContentItem) rtn;
  }

  public IContentItem getContentItemById(final String theId) {
    Session session = HibernateUtil.getSession();
    try {
      return (IContentItem) session.get(ContentItem.class, theId);
    } catch (HibernateException ex) {
      throw new ContentException(Messages.getInstance().getErrorString("CONTREP.ERROR_0003_GETTING_CONTENT_BY_ID", theId), ex); //$NON-NLS-1$
    }
  }

  public List getAllContentLocations() {
    Session session = HibernateUtil.getSession();
    Query qry = session
        .getNamedQuery("org.pentaho.platform.repository.content.ContentLocation.findAllContentLocations"); //$NON-NLS-1$
    return qry.list();
  }

  public List searchLocationsForTerms(final String searchTerm, final int searchType) {
    ISearchable location = new ContentLocation();
    return HibernateUtil.searchForTerm(location, searchTerm, searchType);
  }

  public List searchContentItemsForTerms(final String searchTerm, final int searchType) {
    ISearchable anItem = new ContentItem();
    return HibernateUtil.searchForTerm(anItem, searchTerm, searchType);
  }

  public List searchLocationsAndItemsForTerms(final String searchTerm, final int searchType) {
    List rtn1 = searchLocationsForTerms(searchTerm, searchType);
    List rtn2 = searchContentItemsForTerms(searchTerm, searchType);
    List rtn = new ArrayList(rtn1);
    rtn.addAll(rtn2);
    return rtn;
  }

  public int deleteContentOlderThanDate(final Date agingDate) {
    // First, get the list of content older than the specified
    // date.
    int removedCount = 0;
    Session session = HibernateUtil.getSession();
    Query qry = session
        .getNamedQuery("org.pentaho.platform.repository.content.ContentItemFile.agingContentSearcher").setDate("archiveDate", agingDate); //$NON-NLS-1$ //$NON-NLS-2$
    List contentItemFilesForDeletion = qry.list();
    ContentItem parentContentItem;
    ContentItemFile fileForDeletion;
    for (int i = 0; i < contentItemFilesForDeletion.size(); i++) {
      fileForDeletion = (ContentItemFile) contentItemFilesForDeletion.get(i);
      parentContentItem = fileForDeletion.getParent();
      parentContentItem.removeVersion(fileForDeletion);
      removedCount++;
    }
    return removedCount;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.core.system.PentahoBase#getLogger()
   */
  @Override
  public Log getLogger() {
    return ContentRepository.logger;
  }

  /**
   * Returns a new background executed content object
   * @param session Users' session object
   * @param contentId The content id to reference.
   * @return new BackgroundExecutedContent
   */
  public IBackgroundExecutedContentId newBackgroundExecutedContentId(final IPentahoSession session,
      final String contentId) {
    Session hibSession = HibernateUtil.getSession();
    String userName = (session.getName() != null ? session.getName() : IBackgroundExecution.DEFAULT_USER_NAME);
    BackgroundExecutedContentId beci = new BackgroundExecutedContentId(userName, contentId);
    try {
      hibSession.save(beci);
    } catch (HibernateException ex) {
      error(Messages.getInstance().getErrorString("CONTREP.ERROR_0005_SAVING_BACKGROUND_CONTENT_ID", contentId), ex); //$NON-NLS-1$
      throw new RepositoryException(Messages.getInstance().getErrorString(
          "CONTREP.ERROR_0005_SAVING_BACKGROUND_CONTENT_ID", contentId), ex); //$NON-NLS-1$
    }
    return beci;

  }

  /**
   * Gets list of users' background execution content ids.
   * @param session The users' session
   * @return List of IBackgroundExecutedContentId objects
   */
  public List getBackgroundExecutedContentItemsForUser(final IPentahoSession session) {
    Session hibSession = HibernateUtil.getSession();
    String userName = (session.getName() != null ? session.getName() : IBackgroundExecution.DEFAULT_USER_NAME);
    Query qry = hibSession
        .getNamedQuery("org.pentaho.platform.repository.content.BackgroundExecutedContentId.findBackgroundContentItemsForUsers"); //$NON-NLS-1$
    qry.setString("user", userName); //$NON-NLS-1$
    return qry.list();

  }

  /**
   * Gets list of all background content ids. Should only be used in an administrative capacity
   * @param session Users session
   * @return List of IBackgroundExecutedContentId objects
   */
  public List getAllBackgroundExecutedContentItems(final IPentahoSession session) {
    Session hibSession = HibernateUtil.getSession();
    Query qry = hibSession
        .getNamedQuery("org.pentaho.platform.repository.content.BackgroundExecutedContentId.findAllBackgroundContent"); //$NON-NLS-1$
    return qry.list();
  }

  /**
   * Removes an ID from the background executed content Id list
   * @param session Users' session
   * @param contentId The content id to remove.
   */
  public void removeBackgroundExecutedContentId(final IPentahoSession session, final String contentId) {
    Session hibSession = HibernateUtil.getSession();
    try {
      BackgroundExecutedContentId beci = (BackgroundExecutedContentId) hibSession.get(
          BackgroundExecutedContentId.class, contentId);
      if (beci != null) {
        HibernateUtil.makeTransient(beci);
      }
    } catch (HibernateException ignored) {

    }

  }

}
