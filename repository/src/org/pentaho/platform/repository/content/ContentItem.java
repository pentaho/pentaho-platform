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
 * @created Jun 17, 2005 
 * @author Marc Batchelor
 * 
 */

package org.pentaho.platform.repository.content;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.pentaho.commons.connection.IPentahoStreamSource;
import org.pentaho.platform.api.repository.ContentException;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.api.repository.ISearchable;
import org.pentaho.platform.engine.core.system.PentahoBase;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository.hibernate.HibernateUtil;
import org.pentaho.platform.repository.messages.Messages;
import org.pentaho.platform.util.UUIDUtil;
import org.pentaho.platform.util.messages.MessageUtil;
import org.pentaho.platform.util.web.MimeHelper;

public class ContentItem extends PentahoBase implements IContentItem, ISearchable {
  private static final long serialVersionUID = 823604019645900631L;

  private static final Log logger = LogFactory.getLog(ContentItem.class);

  private String id; // Required

  private ContentLocation parent; // Required

  private String path; // Derived

  private String name; // Required

  private String title; // required

  private String mimeType; // Required

  private String url; // Optional

  private String extension; // Required

  private ContentItemFile latestFile;

  private OutputStream outputStream = null;

  private int revision = -1; // Hibernate Revision

  private int writeMode; // Indicates whether this kind of file is versioned

  private int latestVersionNum; // This is now defunct. Don't bother using this anymore

  private static final String PATH_BUILDER = "{0}/{1}"; //$NON-NLS-1$

  private static final String[] SearchableColumns = { "name", //$NON-NLS-1$
      "title", //$NON-NLS-1$
      "path" }; //$NON-NLS-1$

  private static final String SearchableTable = "org.pentaho.platform.repository.content.ContentItem"; //$NON-NLS-1$

  private static final String SearchablePhraseNamedQuery = "org.pentaho.platform.repository.content.ContentItem.itemSearcher"; //$NON-NLS-1$

  /* Constructor for Hibernate */

  protected ContentItem() {
  }

  /**
   * Constructor
   * 
   * @param cntId
   * @param theParent
   * @param theName
   * @param title
   * @param mType
   * @param url
   */
  protected ContentItem(final String cntId, final ContentLocation theParent, final String theName, final String title,
      final String mType, final String extension, final String url, final int writeMode) {
    name = theName;
    mimeType = mType;
    parent = theParent;
    id = cntId;
    String extnSep = "."; //$NON-NLS-1$
    if (!extension.startsWith(extnSep)) {
      this.extension = extnSep + extension;
    } else {
      this.extension = extension;
    }
    this.title = title;
    this.url = url;
    this.path = MessageUtil.formatMessage(ContentItem.PATH_BUILDER, parent.getDirPath(), this.getName());
    this.writeMode = writeMode;
  }

  public List getMessages() {
    return null;
  }

  protected ContentItemFile newContentFile(final String actionName) {
    String fileGuid = UUIDUtil.getUUIDAsString();
    String fileName = fileGuid + this.getExtension();
    ContentItemFile theFile = new ContentItemFile(this, fileGuid, parent.getDirPath(), fileName, actionName);
    this.latestFile = theFile;
    HibernateUtil.makePersistent(theFile);
    return theFile;
  }

  public InputStream getInputStream() throws ContentException {
    ContentItemFile cif = getLatestFile();
    if (this.latestFile == null) {
      throw new ContentException(Messages.getInstance().getErrorString("CONTITEM.ERROR_0001_NO_EXISTING_FILES", this.getName())); //$NON-NLS-1$
    }
    return cif.getInputStream();
  }

  public IPentahoStreamSource getDataSource() {
    ContentItemFile cif = getLatestFile();
    if (this.latestFile == null) {
      throw new ContentException(Messages.getInstance().getErrorString("CONTITEM.ERROR_0001_NO_EXISTING_FILES", this.getName())); //$NON-NLS-1$
    }

    String fullPath = PentahoSystem.getApplicationContext().getFileOutputPath(
        "system/content/" + cif.getOsPath() + "/" + cif.getId() + extension); //$NON-NLS-1$ //$NON-NLS-2$ 
    return new FilePentahoStreamSource(fullPath);
  }

  public static final class FilePentahoStreamSource implements IPentahoStreamSource {
    // TODO: Move this into the pentaho-connections project.
    File file;

    String fullPath;

    String mimeType;

    public FilePentahoStreamSource(String fp) {
      super();
      assert (fp != null);
      this.fullPath = fp;
      this.file = new File(fullPath);
      mimeType = MimeHelper.getMimeTypeFromFileName(this.file.getName());
      if (mimeType == null) {
        this.mimeType = "application/octet-stream"; //$NON-NLS-1$
      }
    }

    public InputStream getInputStream() throws IOException {
      return new BufferedInputStream(new FileInputStream(this.file));
    }

    public String getName() {
      return file.getName();
    }

    public OutputStream getOutputStream() throws IOException {
      return new FileOutputStream(this.file);
    }

    public String getContentType() {
      return mimeType;
    }
  }

  public Reader getReader() throws ContentException {
    ContentItemFile cif = getLatestFile();
    if (this.latestFile == null) {
      throw new ContentException(Messages.getInstance().getErrorString("CONTITEM.ERROR_0002_NO_EXISTING_FILES", this.getName())); //$NON-NLS-1$
    }
    return cif.getReader();
  }

  public OutputStream getOutputStream(final String actionName) throws IOException {
    outputStream = null;
    if (actionName == null) {
      throw new IllegalArgumentException(Messages.getInstance().getErrorString("CONTITEM.ERROR_0006_ACTION_NAME_CANNOT_BE_NULL")); //$NON-NLS-1$
    }
    switch (getWriteMode()) {
      case WRITEMODE_KEEPVERSIONS: {
        ContentItemFile cif = newContentFile(actionName);
        outputStream = cif.getOutputStream(false);
        return outputStream;
      }
      case WRITEMODE_OVERWRITE: {
        ContentItemFile cif = getLatestFile();
        if (cif == null) {
          cif = newContentFile(actionName);
        }
        if (cif != null) {
          cif.setFileDateTime(new Date());
          outputStream = cif.getOutputStream(true);
          return outputStream;
        }
        throw new IOException(Messages.getInstance().getErrorString("CONTITEM.ERROR_0004_OUTPUT_STREAM_NOT_AVAILABLE")); //$NON-NLS-1$
      }
      case WRITEMODE_APPEND: {
        ContentItemFile cif = getLatestFile();
        if (cif == null) {
          cif = newContentFile(actionName);
        }
        if (cif != null) {
          cif.setFileDateTime(new Date());
          outputStream = cif.getOutputStream(true, true);
          return outputStream;
        }
        throw new IOException(Messages.getInstance().getErrorString("CONTITEM.ERROR_0004_OUTPUT_STREAM_NOT_AVAILABLE")); //$NON-NLS-1$
      }
      default: {
        throw new ContentException(Messages.getInstance().getErrorString(
            "CONTITEM.ERROR_0003_BAD_WRITE_MODE", Integer.toString(getWriteMode()))); //$NON-NLS-1$
      }
    }
  }

  public void closeOutputStream() {
    if (outputStream != null) {
      try {
        outputStream.close();
      } catch (IOException e) {
        error(Messages.getInstance().getErrorString("ContentItem.ERROR_0001_CLOSE_OUTPUT_STREAM"), e); //$NON-NLS-1$
      }
    }
  }

  public String getActionName() {
    return this.getLatestFile() != null ? this.getLatestFile().getActionName() : null;
  }

  public String getFileId() {
    return this.getLatestFile() != null ? this.getLatestFile().getId() : null;
  }

  public long getFileSize() {
    return this.getLatestFile() != null ? this.getLatestFile().getFileSize() : -1;
  }

  public Date getFileDateTime() {
    return this.getLatestFile() != null ? this.getLatestFile().getFileDateTime() : null;
  }

  /**
   * @return Returns the writeMode.
   */
  public int getWriteMode() {
    return writeMode;
  }

  /**
   * @param writeMode
   *            The writeMode to set.
   */
  public void setWriteMode(final int writeMode) {
    this.writeMode = writeMode;
  }

  /**
   * equals override for Hibernate
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof ContentItem)) {
      return false;
    }
    final ContentItem that = (ContentItem) other;
    return this.getId().equals(that.getId());
  }

  /**
   * hashcode override for Hibernate
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return getId().hashCode();
  }

  /**
   * @return Returns the extension.
   */
  public String getExtension() {
    return extension;
  }

  /**
   * @param extension
   *            The extension to set.
   */
  public void setExtension(final String extension) {
    this.extension = extension;
  }

  /**
   * @return Returns the latestFile.
   */
  protected ContentItemFile getLatestFile() {
    if (latestFile == null) {
      Session session = HibernateUtil.getSession();
      Query qry = session
          .createQuery("from ContentItemFile cif where cif.parent = :contentParent and cif.fileDateTime = (select max(fileDateTime) from ContentItemFile where parent = :contentParent2) "); //$NON-NLS-1$
      qry.setParameter("contentParent", this); //$NON-NLS-1$
      qry.setParameter("contentParent2", this); //$NON-NLS-1$
      try {
        this.latestFile = (ContentItemFile) qry.uniqueResult();
      } catch (org.hibernate.TransientObjectException ignored) {
        // The object doesn't exist in Hibernate yet - that's
        // OK, there's no reason for this exception to escape
        // from this method. We are purposely swallowing
        // the exception and returning null...
        this.latestFile = null;
      }
    }
    return this.latestFile;
  }

  /**
   * @param latestFile
   *            The latestFile to set.
   */
  public void setLatestFile(final ContentItemFile latestFile) {
    this.latestFile = latestFile;
  }

  /**
   * @deprecated
   * @return Returns the latestVersionNum. Don't set or get this value any more - retained for backward compatibility.
   */
  @Deprecated
  public int getLatestVersionNum() {
    return latestVersionNum;
  }

  /**
   * @deprecated
   * @param latestVersionNum
   *            The latestVersionNum to set. Don't set or get this value any more - retained for backward compatibility.
   */
  @Deprecated
  public void setLatestVersionNum(final int latestVersionNum) {
    this.latestVersionNum = latestVersionNum;
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

  public void removeVersion(final ContentItemFile cif) {
    try {
      cif.deleteOsFile();
    } catch (Exception ex) {
      ContentItem.logger.error(Messages.getInstance().getErrorString(
          "CONTITEM.ERROR_0005_COULD_NOT_DELETE_OS_FILE", cif.getCompleteFileName()), ex); //$NON-NLS-1$
    }
    HibernateUtil.makeTransient(cif);

    if ((latestFile == null) || latestFile.getId().equals(cif.getId())) {
      this.latestFile = null;
      // Get the now latest file from the DB and sets the variable.
      this.getLatestFile();
    }
  }

  public void removeVersion(final String fileId) {
    Iterator it = getFileVersions().iterator();
    ContentItemFile cif;
    while (it.hasNext()) {
      cif = (ContentItemFile) it.next();
      if (fileId.equalsIgnoreCase(cif.getId())) {
        removeVersion(cif);
        break;
      }
    }
  }

  /**
   * Removes all version files.
   */
  public void removeAllVersions() {
    Iterator it = getFileVersions().iterator();
    ContentItemFile cif;
    while (it.hasNext()) {
      cif = (ContentItemFile) it.next();
      cif.deleteOsFile();
      HibernateUtil.makeTransient(cif);
    }
    this.latestFile = null;
  }

  /*
   * Accessors
   */

  /**
   * @return Returns the parent.
   */
  public ContentLocation getParent() {
    return parent;
  }

  /**
   * @param theParent
   *            The parent to set.
   */
  public void setParent(final ContentLocation theParent) {
    this.parent = theParent;
  }

  /**
   * @return Returns the name.
   */
  public String getName() {
    return name;
  }

  /**
   * @param fName
   *            The name to set.
   */
  public void setName(final String fName) {
    this.name = fName;
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
   * @return Returns the mimeType.
   */
  public String getMimeType() {
    return mimeType;
  }

  /**
   * @param mimeType
   *            The mimeType to set.
   */
  public void setMimeType(final String mimeType) {
    this.mimeType = mimeType;
  }

  /**
   * @return Returns the url.
   */
  public String getUrl() {
    return url;
  }

  /**
   * @param url
   *            The url to set.
   */
  public void setUrl(final String url) {
    this.url = url;
  }

  /**
   * @return Returns the path.
   */
  public String getPath() {
    return path;
  }

  /**
   * @param path
   *            The path to set.
   */
  public void setPath(final String path) {
    this.path = path;
  }

  /**
   * @return Returns the fileVersions.
   */
  public List getFileVersions() {
    Session session = HibernateUtil.getSession();
    Query qry = session
        .createQuery("from ContentItemFile cif where cif.parent = :contentParent order by cif.fileDateTime"); //$NON-NLS-1$
    qry.setParameter("contentParent", this); //$NON-NLS-1$
    return qry.list();
  }

  /**
   * @return Returns the title.
   */
  public String getTitle() {
    return title;
  }

  /**
   * @param title
   *            The title to set.
   */
  public void setTitle(final String title) {
    this.title = title;
  }

  /**
   * @return Returns the log.
   */
  @Override
  public Log getLogger() {
    return ContentItem.logger;
  }

  /* ISearchable Needs */
  public String[] getSearchableColumns() {
    return ContentItem.SearchableColumns;
  }

  public String getSearchableTable() {
    return ContentItem.SearchableTable;
  }

  public String getPhraseSearchQueryName() {
    return ContentItem.SearchablePhraseNamedQuery;
  }

  public void makeTransient() {
    this.removeAllVersions();
    HibernateUtil.makeTransient(this);
  }

  @Override
  public String toString() {
    return MessageFormat.format("{0}, {1}, {2}", new Object[] { this.getTitle(), this.getPath(), this.getMimeType() }); //$NON-NLS-1$
  }

}
