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
 * @created Jun 23, 2005 
 * @author Marc Batchelor
 * 
 */

package org.pentaho.platform.repository.content;

import java.io.File;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.pentaho.platform.api.repository.ContentException;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.api.repository.IContentLocation;
import org.pentaho.platform.api.repository.ISearchable;
import org.pentaho.platform.engine.core.system.PentahoBase;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository.hibernate.HibernateUtil;
import org.pentaho.platform.repository.messages.Messages;
import org.pentaho.platform.util.UUIDUtil;

public class ContentLocation extends PentahoBase implements IContentLocation, ISearchable {
  private static final long serialVersionUID = -86133203446335770L;

  private static final Log logger = LogFactory.getLog(ContentLocation.class);

  private String dirPath;

  private String name;

  private String description;

  private String solutionId;

  private String id;

  private int revision = -1;

  private static final String[] SearchableColumns = { "name", //$NON-NLS-1$ 
      "description", //$NON-NLS-1$ 
      "dirPath" }; //$NON-NLS-1$

  private static final String SearchableTable = "org.pentaho.platform.repository.content.ContentLocation"; //$NON-NLS-1$

  private static final String SearchablePhraseNamedQuery = "org.pentaho.platform.repository.content.ContentLocation.locationSearcher"; //$NON-NLS-1$

  /**
   * Constructor for Hibernate
   * 
   */
  protected ContentLocation() {
  }

  /**
   * Constructor
   * 
   * @param thePath
   *            The path in the file system
   * @param theName
   *            The "nice name" of the location
   * @param solId
   *            The solutionId it's associated with
   * @throws ContentException
   */
  protected ContentLocation(final String locId, final String thePath, final String theName,
      final String theDescription, final String solId, final boolean createIfNotExist) throws ContentException {
    checkPath(thePath, createIfNotExist);
    dirPath = thePath;
    name = theName;
    solutionId = solId;
    description = theDescription;
    id = locId;
  }

  public IContentItem newContentItem(final String itemName, final String title, final String extension,
      final String mType, final String url, final int writeMode) throws ContentException {
    String cntId = UUIDUtil.getUUIDAsString();
    return newContentItem(cntId, itemName, title, extension, mType, url, writeMode);
  }

  public IContentItem newContentItem(final String cntId, final String itemName, final String title,
      final String extension, final String mType, final String url, final int writeMode) throws ContentException {
    IContentItem rtn = new ContentItem(cntId, this, itemName, title, mType, extension, url, writeMode);
    HibernateUtil.makePersistent(rtn);
    HibernateUtil.flushSession();
    return rtn;
  }

  public IContentItem getContentItemByPath(final String path) {
    Session session = HibernateUtil.getSession();
    Query qry = session.getNamedQuery("org.pentaho.platform.repository.content.ContentItem.findItemByPath"); //$NON-NLS-1$
    qry.setString("inPath", path); //$NON-NLS-1$
    Object rtn = null;
    try {
      rtn = qry.uniqueResult();
    } catch (Exception ignored) {
    }
    return (ContentItem) rtn;
  }

  public List getMessages() {
    return null;
  }

  /**
   * @return Returns the revision.
   */
  public int getRevision() {
    return revision;
  }

  /**
   * @param revision
   *            The revision to set.
   */
  public void setRevision(final int revision) {
    this.revision = revision;
  }

  /**
   * Iterates over registered content items.
   * 
   * @return Iterator of the child content
   */
  public Iterator getContentItemIterator() {
    Session session = HibernateUtil.getSession();
    Query qry = session.createQuery("from ContentItem where parent = :contentParent"); //$NON-NLS-1$
    qry.setParameter("contentParent", this); //$NON-NLS-1$
    List list = qry.list();
    if (list != null) {
      return list.iterator();
    } else {
      return null;
    }
  }

  public IContentItem getContentItemById(final String itemId) {
    Session session = HibernateUtil.getSession();
    return (ContentItem) session.get(ContentItem.class, itemId);
  }

  public IContentItem getContentItemByName(final String itemName) {
    Session session = HibernateUtil.getSession();
    Query qry = session.getNamedQuery("org.pentaho.platform.repository.content.ContentItem.findItemByName"); //$NON-NLS-1$
    qry.setEntity("parent", this); //$NON-NLS-1$
    qry.setString("name", itemName); //$NON-NLS-1$
    Object rtn = null;
    try {
      rtn = qry.uniqueResult();
    } catch (Exception ignored) {
      ContentLocation.logger.debug(ignored);
    }
    return (ContentItem) rtn;
  }

  /**
   * Creates a subdirectory in the content location.
   * 
   * @param subDirName
   *            The directory name to create
   * @return File created
   * @throws ContentException
   */
  public File makeSubdirectory(final String subDirName) throws ContentException {
    File f = checkPath();
    File newDir = new File(f, subDirName);
    if (newDir.mkdirs()) {
      return newDir;
    }
    throwError(Messages.getInstance().getErrorString("CONTLOC.ERROR_0003_MKDIR", newDir.getAbsolutePath())); //$NON-NLS-1$
    return null; // Unreachable
  }

  /*
   * Utility Methods
   */
  protected File checkPath() throws ContentException {
    return checkPath(getDirPath());
  }

  protected File checkPath(final String thePath) throws ContentException {
    return checkPath(thePath, false);
  }

  protected File checkPath(final String thePath, boolean createIfNotExist) {
    File f = new File(PentahoSystem.getApplicationContext().getFileOutputPath("system/content") + "/" + thePath); //$NON-NLS-1$ //$NON-NLS-2$
    if ((!f.exists()) || (!f.isDirectory())) {
      if (!createIfNotExist) {
        throwError(Messages.getInstance().getErrorString("CONTLOC.ERROR_0004_PATH_DOES_NOT_EXIST", thePath)); //$NON-NLS-1$
      } else {
        if (!f.mkdirs()) {
          throwError(Messages.getInstance().getErrorString("CONTLOC.ERROR_0003_MKDIR", thePath)); //$NON-NLS-1$
        }
      }
    }
    return f;
  }

  protected void throwError(final String msg) throws ContentException {
    ContentLocation.logger.error(msg);
    throw new ContentException(msg);
  }

  /*
   * ************* * Accessors * *************
   */

  /**
   * @return Returns the dirPath.
   */
  public String getDirPath() {
    return dirPath;
  }

  /**
   * @param dirPath
   *            The dirPath to set.
   */
  public void setDirPath(final String dirPath) {
    this.dirPath = dirPath;
    checkPath(dirPath, true);
  }

  /**
   * @return Returns the id.
   */
  public String getId() {
    return id;
  }

  /**
   * @param id
   *            The id to set.
   */
  public void setId(final String id) {
    this.id = id;
  }

  /**
   * @return Returns the name.
   */
  public String getName() {
    return name;
  }

  /**
   * @param name
   *            The name to set.
   */
  public void setName(final String name) {
    this.name = name;
  }

  /**
   * @return Returns the solutionId.
   */
  public String getSolutionId() {
    return solutionId;
  }

  /**
   * @param solutionId
   *            The solutionId to set.
   */
  public void setSolutionId(final String solutionId) {
    this.solutionId = solutionId;
  }

  /**
   * @return Returns the description.
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description
   *            The description to set.
   */
  public void setDescription(final String description) {
    this.description = description;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.core.system.PentahoBase#getLogger()
   */
  @Override
  public Log getLogger() {
    return ContentLocation.logger;
  }

  /* ISearchable Needs */
  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.repository.ISearchable#getSearchableColumns()
   */
  public String[] getSearchableColumns() {
    return ContentLocation.SearchableColumns;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.repository.ISearchable#getSearchableTable()
   */
  public String getSearchableTable() {
    return ContentLocation.SearchableTable;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.repository.ISearchable#getPhraseSearchQueryName()
   */
  public String getPhraseSearchQueryName() {
    return ContentLocation.SearchablePhraseNamedQuery;
  }

  @Override
  public String toString() {
    return MessageFormat.format("{0}, {1}", new Object[] { this.getDescription(), this.getDirPath() }); //$NON-NLS-1$
  }
}
