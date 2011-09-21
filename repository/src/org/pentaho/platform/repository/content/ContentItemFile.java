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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository.ContentException;
import org.pentaho.platform.api.repository.IContentItemFile;
import org.pentaho.platform.engine.core.system.PentahoBase;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository.messages.Messages;
import org.pentaho.platform.util.messages.MessageUtil;

public class ContentItemFile extends PentahoBase implements IContentItemFile {
  private static final long serialVersionUID = 946969559555268447L;

  private static final Log logger = LogFactory.getLog(ContentItemFile.class);

  private String osFileName;

  private String osPath;

  private String actionName;

  private Date storedFileDate;

  private String id;

  private int revision = -1; // Hibernate Version #

  private ContentItem parent;

  private int initialized = -1;

  private File itemFile;

  private static final String PATH_BUILDER = "{0}/{1}"; //$NON-NLS-1$

  protected ContentItemFile() {
  }

  @Override
  public boolean equals(final Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof ContentItemFile)) {
      return false;
    }
    final ContentItemFile that = (ContentItemFile) other;
    return this.getId().equals(that.getId());
  }

  @Override
  public int hashCode() {
    return getId().hashCode();
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

  protected ContentItemFile(final ContentItem parent, final String guid, final String osPath, final String osFileName,
      final String actionName) {
    this.parent = parent;
    this.id = guid;
    this.osPath = osPath;
    this.osFileName = osFileName;
    this.actionName = actionName;
    this.itemFile = new File(getCompleteFileName());
    this.storedFileDate = new Date(System.currentTimeMillis());
  }

  public InputStream getInputStream() throws ContentException {
    String fName = getCompleteFileName();
    try {
      if (itemFile.exists()) {
        if (itemFile.canRead()) {
          return new FileInputStream(itemFile);
        }
        throw new ContentException(Messages.getInstance().getErrorString("CONTFILE.ERROR_0001_FILE_CANNOT_BE_READ", fName)); //$NON-NLS-1$
      }
      throw new ContentException(Messages.getInstance().getErrorString("CONTFILE.ERROR_0002_FILE_DOES_NOT_EXIST", fName)); //$NON-NLS-1$
    } catch (IOException e) {
      throw new ContentException(e.getMessage(), e);
    }
  }

  public Reader getReader() throws ContentException {
    InputStream is = getInputStream();
    return new BufferedReader(new InputStreamReader(is));
  }

  public OutputStream getOutputStream(final boolean overWriteOk) throws ContentException {
    return getOutputStream(overWriteOk, false);
  }

  public OutputStream getOutputStream(boolean overWriteOk, boolean append) throws ContentException {
    String fName = getCompleteFileName();
    if (itemFile.exists() && (!overWriteOk)) {
      // Not allowed to overwrite a file that is versioned.
      throw new ContentException(Messages.getInstance().getErrorString("CONTFILE.ERROR_0003_OVERWRITE_DISALLOWED", fName)); //$NON-NLS-1$
    }
    try {
      if (!append) {
        if (itemFile.exists()) {
          if (!itemFile.delete()) {
            throw new ContentException(Messages.getInstance().getErrorString("CONTFILE.ERROR_0004_CANNOT_DELETE_FOR_CREATE", fName)); //$NON-NLS-1$
          }
        }
        if (itemFile.createNewFile()) {
          return new BufferedOutputStream(new FileOutputStream(itemFile));
        }
        throw new ContentException(Messages.getInstance().getErrorString("CONTFILE.ERROR_0005_CANNOT_CREATE", fName)); //$NON-NLS-1$
      }
      return new BufferedOutputStream(new FileOutputStream(itemFile, append));
    } catch (IOException ex) {
      throw new ContentException(ex);
    }
  }

  public long copyToFile(final String newFileName) throws ContentException {
    try {
      InputStream is = getInputStream();
      try {
        OutputStream os = new BufferedOutputStream(new FileOutputStream(newFileName));
        try {
          long bytesCopied = 0;
          int size;
          byte[] copyBuffer = new byte[4096];
          while ((size = is.read(copyBuffer)) != -1) {
            os.write(copyBuffer, 0, size);
            bytesCopied += size;
          }
          return bytesCopied;
        } finally {
          os.flush();
          os.close();
        }
      } finally {
        is.close();
      }
    } catch (IOException ex) {
      throw new ContentException(Messages.getInstance().getErrorString(
          "CONTFILE.ERROR_0006_DURING_COPY", this.getCompleteFileName(), newFileName), ex); //$NON-NLS-1$
    }
  }

  public boolean deleteOsFile() {
    String fName = getCompleteFileName();
    File f = new File(fName);
    return f.delete();
  }

  protected String getCompleteFileName() {
    return MessageUtil.formatMessage(ContentItemFile.PATH_BUILDER, PentahoSystem.getApplicationContext()
        .getFileOutputPath("system/content") + "/" + getOsPath(), getOsFileName()); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * @return Returns the parent.
   */
  public ContentItem getParent() {
    return parent;
  }

  /**
   * @param parent
   *            The parent to set.
   */
  public void setParent(final ContentItem parent) {
    this.parent = parent;
  }

  /**
   * @return Returns the actionName.
   */
  public String getActionName() {
    return actionName;
  }

  /**
   * @param actionName
   *            The actionName to set.
   */
  public void setActionName(final String actionName) {
    this.actionName = actionName;
  }

  /**
   * @return Returns the fileDateTime.
   */
  public Date getFileDateTime() {
    return storedFileDate;
  }

  /**
   * @param fileDateTime
   *            The fileDateTime to set.
   */
  protected void setFileDateTime(final Date fileDateTime) {
    // Now, handle the case where the file is on another machine from this
    // one.
    storedFileDate = fileDateTime;
  }

  /**
   * @return Returns the fileSize.
   */
  public long getFileSize() {
    // Return the OS File Size
    return itemFile.length();
  }

  /**
   * @param fileSize
   *            The fileSize to set.
   */
  public void setFileSize(long fileSize) {
    // Do nothing because it'll be obtained by the OS
    // this line is here to prevent compiler warnings only
    fileSize++;
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
   * @return Returns the osFileName.
   */
  public String getOsFileName() {
    return osFileName;
  }

  /**
   * @param osFileName
   *            The osFileName to set.
   */
  public void setOsFileName(final String osFileName) {
    this.osFileName = osFileName;
  }

  /**
   * @return Returns the osPath.
   */
  public String getOsPath() {
    return osPath;
  }

  /**
   * @param osPath
   *            The osPath to set.
   */
  public void setOsPath(final String osPath) {
    this.osPath = osPath;
  }

  /**
   * @return Returns the initialized.
   */
  public int getInitialized() {
    return initialized;
  }

  /**
   * @param initialized
   *            The initialized to set.
   */
  public void setInitialized(final int initialized) {
    // This is a dummy property that gets filled after construction of the
    // object from
    // hibernate. There may be a better way of being called after all of an
    // objects'
    // properties get initialized, but I didn't see one (short of creating a
    // new user type).
    itemFile = new File(getCompleteFileName());
    this.initialized = initialized;
  }

  /**
   * @return Returns the itemFile.
   */
  public File getItemFile() {
    return itemFile;
  }

  /**
   * @param itemFile
   *            The itemFile to set.
   */
  public void setItemFile(final File itemFile) {
    this.itemFile = itemFile;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.core.system.PentahoBase#getLogger()
   */
  @Override
  public Log getLogger() {
    return ContentItemFile.logger;
  }

  @Override
  public String toString() {
    StringBuffer buf = new StringBuffer().append("[").append(this.getId()) //$NON-NLS-1$
        .append(",").append(this.getOsPath()) //$NON-NLS-1$
        .append(",").append(this.getOsFileName()) //$NON-NLS-1$
        .append(",").append(this.getActionName()) //$NON-NLS-1$
        .append("]"); //$NON-NLS-1$
    return buf.toString();
  }

}
