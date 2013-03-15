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
 */
package org.pentaho.platform.repository2.unified.jcr;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.Lock;

import org.pentaho.platform.api.locale.IPentahoLocale;
import org.pentaho.platform.api.repository2.unified.IRepositoryDefaultAclHandler;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.VersionSummary;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.repository2.messages.Messages;
import org.pentaho.platform.repository2.unified.IRepositoryFileAclDao;
import org.pentaho.platform.repository2.unified.IRepositoryFileDao;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.pentaho.platform.util.VersionHelper;
import org.springframework.extensions.jcr.JcrCallback;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * CRUD operations against JCR. Note that there is no access control in this class (implicit or explicit).
 *
 * @author mlowery
 */
public class JcrRepositoryFileDao implements IRepositoryFileDao {

  // ~ Static fields/initializers ======================================================================================

  // ~ Instance fields =================================================================================================

  private JcrTemplate jcrTemplate;

  private List<ITransformer<IRepositoryFileData>> transformers;

  private ILockHelper lockHelper;

  private IDeleteHelper deleteHelper;

  private IPathConversionHelper pathConversionHelper;

  private IRepositoryFileAclDao aclDao;

  private IRepositoryDefaultAclHandler defaultAclHandler;

  // ~ Constructors ====================================================================================================

  public JcrRepositoryFileDao(final JcrTemplate jcrTemplate,
      final List<ITransformer<IRepositoryFileData>> transformers, final ILockHelper lockHelper,
      final IDeleteHelper deleteHelper, final IPathConversionHelper pathConversionHelper,
      final IRepositoryFileAclDao aclDao, final IRepositoryDefaultAclHandler defaultAclHandler) {
    super();
    Assert.notNull(jcrTemplate);
    Assert.notNull(transformers);
    this.jcrTemplate = jcrTemplate;
    this.transformers = transformers;
    this.lockHelper = lockHelper;
    this.deleteHelper = deleteHelper;
    this.pathConversionHelper = pathConversionHelper;
    this.aclDao = aclDao;
    this.defaultAclHandler = defaultAclHandler;
  }

  // ~ Methods =========================================================================================================

  private RepositoryFile internalCreateFolder(final Session session, final Serializable parentFolderId,
      final RepositoryFile folder, final RepositoryFileAcl acl, final String versionMessage) throws RepositoryException {
    PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
    JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary(session, pentahoJcrConstants, parentFolderId);
    Node folderNode = JcrRepositoryFileUtils.createFolderNode(session, pentahoJcrConstants, parentFolderId, folder);
    // we must create the acl during checkout
    JcrRepositoryFileAclUtils.createAcl(session, pentahoJcrConstants, folderNode.getIdentifier(),
        acl == null ? defaultAclHandler.createDefaultAcl(folder) : acl);
    session.save();
    if (folder.isVersioned()) {
      JcrRepositoryFileUtils.checkinNearestVersionableNodeIfNecessary(session, pentahoJcrConstants, folderNode,
          versionMessage);
    }
    JcrRepositoryFileUtils
        .checkinNearestVersionableFileIfNecessary(
            session,
            pentahoJcrConstants,
            parentFolderId,
            Messages
                .getInstance()
                .getString(
                    "JcrRepositoryFileDao.USER_0001_VER_COMMENT_ADD_FOLDER", folder.getName(), (parentFolderId == null ? "root" : parentFolderId.toString()))); //$NON-NLS-1$ //$NON-NLS-2$
    return JcrRepositoryFileUtils
        .nodeToFile(session, pentahoJcrConstants, pathConversionHelper, lockHelper, folderNode);
  }

  private RepositoryFile internalCreateFile(final Serializable parentFolderId, final RepositoryFile file,
      final IRepositoryFileData content, final RepositoryFileAcl acl, final String versionMessage) {

    /*
     * PPP-3049: Changed the Assert.notNull(content) to code that creates a file with a single blank when
     * the assert WOULD have been triggered.
     */
    Assert.notNull(file);
    Assert.isTrue(!file.isFolder());
    //    Assert.notNull(content);
    DataNode emptyDataNode = new DataNode(file.getName());
    emptyDataNode.setProperty(" ", "content"); //$NON-NLS-1$ //$NON-NLS-2$
    final IRepositoryFileData emptyContent = new NodeRepositoryFileData(emptyDataNode);

    return (RepositoryFile) jcrTemplate.execute(new JcrCallback() {
      @Override
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary(session, pentahoJcrConstants, parentFolderId);
        Node fileNode = JcrRepositoryFileUtils.createFileNode(session, pentahoJcrConstants, parentFolderId, file,
            content == null ? emptyContent : content, findTransformerForWrite(content == null ? emptyContent.getClass()
                : content.getClass()));
        // we must create the acl during checkout
        aclDao.createAcl(fileNode.getIdentifier(), acl == null ? defaultAclHandler.createDefaultAcl(file) : acl);
        session.save();
        if (file.isVersioned()) {
          JcrRepositoryFileUtils.checkinNearestVersionableNodeIfNecessary(session, pentahoJcrConstants, fileNode,
              versionMessage);
        }
        JcrRepositoryFileUtils
            .checkinNearestVersionableFileIfNecessary(
                session,
                pentahoJcrConstants,
                parentFolderId,
                Messages
                    .getInstance()
                    .getString(
                        "JcrRepositoryFileDao.USER_0002_VER_COMMENT_ADD_FILE", file.getName(), (parentFolderId == null ? "root" : parentFolderId.toString()))); //$NON-NLS-1$ //$NON-NLS-2$
        return JcrRepositoryFileUtils.nodeToFile(session, pentahoJcrConstants, pathConversionHelper, lockHelper,
            fileNode);
      }
    });
  }

  private RepositoryFile internalUpdateFile(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final RepositoryFile file, final IRepositoryFileData content, final String versionMessage)
      throws RepositoryException {
    Assert.notNull(file);
    Assert.isTrue(!file.isFolder());
    Assert.notNull(content);
    lockHelper.addLockTokenToSessionIfNecessary(session, pentahoJcrConstants, file.getId());
    JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary(session, pentahoJcrConstants, file.getId());
    JcrRepositoryFileUtils.updateFileNode(session, pentahoJcrConstants, file, content,
        findTransformerForWrite(content.getClass()));
    session.save();
    JcrRepositoryFileUtils.checkinNearestVersionableFileIfNecessary(session, pentahoJcrConstants, file.getId(),
        versionMessage);
    lockHelper.removeLockTokenFromSessionIfNecessary(session, pentahoJcrConstants, file.getId());
    return JcrRepositoryFileUtils.nodeIdToFile(session, pentahoJcrConstants, pathConversionHelper, lockHelper,
        file.getId());
  }

  protected ITransformer<IRepositoryFileData> findTransformerForRead(final String contentType,
      final Class<? extends IRepositoryFileData> clazz) {
    for (ITransformer<IRepositoryFileData> transformer : transformers) {
      if (transformer.canRead(contentType, clazz)) {
        return transformer;
      }
    }
    throw new IllegalArgumentException(Messages.getInstance().getString(
        "JcrRepositoryFileDao.ERROR_0001_NO_TRANSFORMER")); //$NON-NLS-1$
  }

  protected ITransformer<IRepositoryFileData> findTransformerForWrite(final Class<? extends IRepositoryFileData> clazz) {
    for (ITransformer<IRepositoryFileData> transformer : transformers) {
      if (transformer.canWrite(clazz)) {
        return transformer;
      }
    }
    throw new IllegalArgumentException(Messages.getInstance().getString(
        "JcrRepositoryFileDao.ERROR_0001_NO_TRANSFORMER")); //$NON-NLS-1$
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RepositoryFile createFile(final Serializable parentFolderId, final RepositoryFile file,
      final IRepositoryFileData content, final RepositoryFileAcl acl, final String versionMessage) {
    Assert.notNull(file);
    Assert.isTrue(!file.isFolder());
    return internalCreateFile(parentFolderId, file, content, acl, versionMessage);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RepositoryFile createFolder(final Serializable parentFolderId, final RepositoryFile folder,
      final RepositoryFileAcl acl, final String versionMessage) {
    Assert.notNull(folder);
    Assert.isTrue(folder.isFolder());

    return (RepositoryFile) jcrTemplate.execute(new JcrCallback() {
      @Override
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        return internalCreateFolder(session, parentFolderId, folder, acl, versionMessage);
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RepositoryFile getFileById(final Serializable fileId) {
    return internalGetFileById(fileId, false, null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RepositoryFile getFileById(final Serializable fileId, final boolean loadMaps) {
    return internalGetFileById(fileId, loadMaps, null);
  }

  @Override
  public RepositoryFile getFile(final String relPath, final IPentahoLocale locale) {
    return getFile(relPath, false, locale);
  }

  @Override
  public RepositoryFile getFileById(Serializable fileId, IPentahoLocale locale) {
    return internalGetFileById(fileId, false, locale);
  }

  @Override
  public RepositoryFile getFile(final String relPath, final boolean loadLocaleMaps, final IPentahoLocale locale) {
    Assert.hasText(relPath);
    Assert.isTrue(relPath.startsWith(RepositoryFile.SEPARATOR));

    return (RepositoryFile) jcrTemplate.execute(new JcrCallback() {
      @Override
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        String absPath = pathConversionHelper.relToAbs(relPath);
        return internalGetFile(session, absPath, loadLocaleMaps, locale);
      }
    });
  }

  @Override
  public RepositoryFile getFileById(Serializable fileId, boolean loadLocaleMaps, IPentahoLocale locale) {
    return internalGetFileById(fileId, loadLocaleMaps, locale);
  }

  private RepositoryFile internalGetFileById(final Serializable fileId, final boolean loadMaps,
      final IPentahoLocale locale) {
    Assert.notNull(fileId);
    return (RepositoryFile) jcrTemplate.execute(new JcrCallback() {
      @Override
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        Node fileNode = session.getNodeByIdentifier(fileId.toString());
        return fileNode != null ? JcrRepositoryFileUtils.nodeToFile(session, pentahoJcrConstants, pathConversionHelper,
            lockHelper, fileNode, loadMaps, locale) : null;
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RepositoryFile getFile(final String relPath) {
    return getFile(relPath, false, null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RepositoryFile getFileByAbsolutePath(final String absPath) {
    Assert.hasText(absPath);
    Assert.isTrue(absPath.startsWith(RepositoryFile.SEPARATOR));
    return (RepositoryFile) jcrTemplate.execute(new JcrCallback() {
      @Override
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        return internalGetFile(session, absPath, false, null);
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RepositoryFile getFile(final String relPath, final boolean loadMaps) {
    Assert.hasText(relPath);
    Assert.isTrue(relPath.startsWith(RepositoryFile.SEPARATOR));
    return (RepositoryFile) jcrTemplate.execute(new JcrCallback() {
      @Override
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        String absPath = pathConversionHelper.relToAbs(relPath);
        return internalGetFile(session, absPath, loadMaps, null);
      }
    });
  }

  private RepositoryFile internalGetFile(final Session session, final String absPath, final boolean loadMaps,
      final IPentahoLocale locale) throws RepositoryException {

    PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
    Item fileNode;
    try {
      fileNode = session.getItem(absPath);
      // items are nodes or properties; this must be a node
      Assert.isTrue(fileNode.isNode());
    } catch (PathNotFoundException e) {
      fileNode = null;
    }
    return fileNode != null ? JcrRepositoryFileUtils.nodeToFile(session, pentahoJcrConstants, pathConversionHelper,
        lockHelper, (Node) fileNode, loadMaps, locale) : null;

  }

  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings("unchecked")
  public <T extends IRepositoryFileData> T getData(final Serializable fileId, final Serializable versionId,
      final Class<T> contentClass) {
    Assert.notNull(fileId);
    return (T) jcrTemplate.execute(new JcrCallback() {
      @Override
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        return JcrRepositoryFileUtils.getContent(
            session,
            pentahoJcrConstants,
            fileId,
            versionId,
            findTransformerForRead(
                JcrRepositoryFileUtils.getFileContentType(session, pentahoJcrConstants, fileId, versionId),
                contentClass));
      }
    });

  }

  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings("unchecked")
  public List<RepositoryFile> getChildren(final Serializable folderId, final String filter) {
    Assert.notNull(folderId);
    return (List<RepositoryFile>) jcrTemplate.execute(new JcrCallback() {
      @Override
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        return JcrRepositoryFileUtils.getChildren(session, pentahoJcrConstants, pathConversionHelper, lockHelper,
            folderId, filter);
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RepositoryFile updateFile(final RepositoryFile file, final IRepositoryFileData content,
      final String versionMessage) {
    Assert.notNull(file);
    Assert.isTrue(!file.isFolder());
    return (RepositoryFile) jcrTemplate.execute(new JcrCallback() {
      @Override
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        return internalUpdateFile(session, pentahoJcrConstants, file, content, versionMessage);
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void lockFile(final Serializable fileId, final String message) {
    Assert.notNull(fileId);
    jcrTemplate.execute(new JcrCallback() {
      @Override
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        lockHelper.lockFile(session, pentahoJcrConstants, fileId, message);
        return null;
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void unlockFile(final Serializable fileId) {
    Assert.notNull(fileId);
    jcrTemplate.execute(new JcrCallback() {
      @Override
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        lockHelper.unlockFile(session, pentahoJcrConstants, fileId);
        return null;
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings("unchecked")
  public List<VersionSummary> getVersionSummaries(final Serializable fileId) {
    Assert.notNull(fileId);
    return (List<VersionSummary>) jcrTemplate.execute(new JcrCallback() {
      @Override
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        return JcrRepositoryFileUtils.getVersionSummaries(session, pentahoJcrConstants, fileId, true);
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RepositoryFile getFile(final Serializable fileId, final Serializable versionId) {
    Assert.notNull(fileId);
    Assert.notNull(versionId);
    return (RepositoryFile) jcrTemplate.execute(new JcrCallback() {
      @Override
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        return JcrRepositoryFileUtils.getFileAtVersion(session, pentahoJcrConstants, pathConversionHelper, lockHelper,
           fileId, versionId);
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void deleteFile(final Serializable fileId, final String versionMessage) {
    Assert.notNull(fileId);
    jcrTemplate.execute(new JcrCallback() {
      @Override
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        Serializable parentFolderId = JcrRepositoryFileUtils.getParentId(session, fileId);
        JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary(session, pentahoJcrConstants, parentFolderId);
        deleteHelper.deleteFile(session, pentahoJcrConstants, fileId);
        session.save();
        JcrRepositoryFileUtils.checkinNearestVersionableFileIfNecessary(session, pentahoJcrConstants, parentFolderId,
           versionMessage);
        return null;
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void deleteFileAtVersion(final Serializable fileId, final Serializable versionId) {
    Assert.notNull(fileId);
    Assert.notNull(versionId);
    jcrTemplate.execute(new JcrCallback() {
      @Override
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        Node fileToDeleteNode = session.getNodeByIdentifier(fileId.toString());
        session.getWorkspace().getVersionManager().getVersionHistory(fileToDeleteNode.getPath())
            .removeVersion(versionId.toString());
        session.save();
        return null;
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings("unchecked")
  public List<RepositoryFile> getDeletedFiles(final String origParentFolderPath, final String filter) {
    Assert.hasLength(origParentFolderPath);
    return (List<RepositoryFile>) jcrTemplate.execute(new JcrCallback() {
      @Override
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        return deleteHelper.getDeletedFiles(session, pentahoJcrConstants, origParentFolderPath, filter);
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings("unchecked")
  public List<RepositoryFile> getDeletedFiles() {
    return (List<RepositoryFile>) jcrTemplate.execute(new JcrCallback() {
      @Override
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        return deleteHelper.getDeletedFiles(session, pentahoJcrConstants);
      }
    });
  }

  /**
   * {@inheritDoc}
   * <p/>
   * <p>
   * No checkout needed as .trash is not versioned.
   * </p>
   */
  @Override
  public void permanentlyDeleteFile(final Serializable fileId, final String versionMessage) {
    Assert.notNull(fileId);
    jcrTemplate.execute(new JcrCallback() {
      @Override
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        deleteHelper.permanentlyDeleteFile(session, pentahoJcrConstants, fileId);
        session.save();
        return null;
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void undeleteFile(final Serializable fileId, final String versionMessage) {
    Assert.notNull(fileId);
    jcrTemplate.execute(new JcrCallback() {
      @Override
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        String absOrigParentFolderPath = deleteHelper.getOriginalParentFolderPath(session, pentahoJcrConstants, fileId);
        Serializable origParentFolderId = null;
        // original parent folder path may no longer exist!
        if (session.itemExists(absOrigParentFolderPath)) {
          origParentFolderId = ((Node) session.getItem(absOrigParentFolderPath)).getIdentifier();
        } else {
          // go through each of the segments of the original parent folder path, creating as necessary
          String[] segments = pathConversionHelper.absToRel(absOrigParentFolderPath).split(RepositoryFile.SEPARATOR);
          RepositoryFile lastParentFolder = internalGetFile(session, ServerRepositoryPaths.getTenantRootFolderPath(),
              false, null);
          for (String segment : segments) {
            if (StringUtils.hasLength(segment)) {
              RepositoryFile tmp = internalGetFile(
                  session,
                  pathConversionHelper
                      .relToAbs((lastParentFolder.getPath().equals(RepositoryFile.SEPARATOR) ? "" : lastParentFolder.getPath()) + RepositoryFile.SEPARATOR + segment), false, null); //$NON-NLS-1$
              if (tmp == null) {
                lastParentFolder = internalCreateFolder(session, lastParentFolder.getId(), new RepositoryFile.Builder(
                    segment).folder(true).build(), defaultAclHandler.createDefaultAcl(lastParentFolder), null);
              } else {
                lastParentFolder = tmp;
              }
            }
          }
          origParentFolderId = lastParentFolder.getId();
        }
        JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary(session, pentahoJcrConstants,
            origParentFolderId);
        deleteHelper.undeleteFile(session, pentahoJcrConstants, fileId);
        session.save();
        JcrRepositoryFileUtils.checkinNearestVersionableFileIfNecessary(session, pentahoJcrConstants,
           origParentFolderId, versionMessage);
        return null;
      }
    });
  }

  private void internalCopyOrMove(final Serializable fileId, final String destRelPath, final String versionMessage,
      final boolean copy) {
    Assert.notNull(fileId);
    jcrTemplate.execute(new JcrCallback() {
      @Override
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        String destAbsPath = pathConversionHelper.relToAbs(destRelPath);
        String cleanDestAbsPath = destAbsPath;
        if (cleanDestAbsPath.endsWith(RepositoryFile.SEPARATOR)) {
          cleanDestAbsPath.substring(0, cleanDestAbsPath.length() - 1);
        }
        Node srcFileNode = session.getNodeByIdentifier(fileId.toString());
        Serializable srcParentFolderId = JcrRepositoryFileUtils.getParentId(session, fileId);
        boolean appendFileName = false;
        boolean destExists = true;
        Node destFileNode = null;
        Node destParentFolderNode = null;
        try {
          destFileNode = (Node) session.getItem(cleanDestAbsPath);
        } catch (PathNotFoundException e) {
          destExists = false;
        }
        if (destExists) {
          // make sure it's a file or folder
          Assert.isTrue(JcrRepositoryFileUtils.isSupportedNodeType(pentahoJcrConstants, destFileNode));
          // existing item; make sure src is not a folder if dest is a file
          Assert.isTrue(
              !(JcrRepositoryFileUtils.isPentahoFolder(pentahoJcrConstants, srcFileNode) && JcrRepositoryFileUtils
                  .isPentahoFile(pentahoJcrConstants, destFileNode)),
              Messages.getInstance().getString("JcrRepositoryFileDao.ERROR_0002_CANNOT_OVERWRITE_FILE_WITH_FOLDER")); //$NON-NLS-1$
          if (JcrRepositoryFileUtils.isPentahoFolder(pentahoJcrConstants, destFileNode)) {
            // existing item; caller is not renaming file, only moving it
            appendFileName = true;
            destParentFolderNode = destFileNode;
          } else {
            // get parent of existing dest item
            int lastSlashIndex = cleanDestAbsPath.lastIndexOf(RepositoryFile.SEPARATOR);
            Assert.isTrue(lastSlashIndex > 1,
                Messages.getInstance().getString("JcrRepositoryFileDao.ERROR_0003_ILLEGAL_DEST_PATH")); //$NON-NLS-1$
            String absPathToDestParentFolder = cleanDestAbsPath.substring(0, lastSlashIndex);
            destParentFolderNode = (Node) session.getItem(absPathToDestParentFolder);
          }
        } else {
          // destination doesn't exist; go up one level to a folder that does exist
          int lastSlashIndex = cleanDestAbsPath.lastIndexOf(RepositoryFile.SEPARATOR);
          Assert.isTrue(lastSlashIndex > 1,
              Messages.getInstance().getString("JcrRepositoryFileDao.ERROR_0003_ILLEGAL_DEST_PATH")); //$NON-NLS-1$
          String absPathToDestParentFolder = cleanDestAbsPath.substring(0, lastSlashIndex);
          JcrRepositoryFileUtils.checkName(cleanDestAbsPath.substring(lastSlashIndex + 1));
          try {
            destParentFolderNode = (Node) session.getItem(absPathToDestParentFolder);
          } catch (PathNotFoundException e1) {
            Assert.isTrue(false, Messages.getInstance().getString("JcrRepositoryFileDao.ERROR_0004_PARENT_MUST_EXIST")); //$NON-NLS-1$
          }
          Assert.isTrue(JcrRepositoryFileUtils.isPentahoFolder(pentahoJcrConstants, destParentFolderNode), Messages
              .getInstance().getString("JcrRepositoryFileDao.ERROR_0005_PARENT_MUST_BE_FOLDER")); //$NON-NLS-1$
        }
        if (!copy) {
          JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary(session, pentahoJcrConstants,
              srcParentFolderId);
        }
        JcrRepositoryFileUtils.checkoutNearestVersionableNodeIfNecessary(session, pentahoJcrConstants,
            destParentFolderNode);
        String finalSrcAbsPath = srcFileNode.getPath();
        String finalDestAbsPath = appendFileName ? cleanDestAbsPath + RepositoryFile.SEPARATOR + srcFileNode.getName()
            : cleanDestAbsPath;
        if (copy) {
          session.getWorkspace().copy(finalSrcAbsPath, finalDestAbsPath);
        } else {
          session.getWorkspace().move(finalSrcAbsPath, finalDestAbsPath);
        }
        JcrRepositoryFileUtils.checkinNearestVersionableNodeIfNecessary(session, pentahoJcrConstants,
            destParentFolderNode, versionMessage);
        // if it's a move within the same folder, then the next checkin is unnecessary
        if (!copy && !destParentFolderNode.getIdentifier().equals(srcParentFolderId.toString())) {
          JcrRepositoryFileUtils.checkinNearestVersionableFileIfNecessary(session, pentahoJcrConstants,
              srcParentFolderId, versionMessage);
        }
        return null;
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void moveFile(final Serializable fileId, final String destRelPath, final String versionMessage) {
    internalCopyOrMove(fileId, destRelPath, versionMessage, false);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void copyFile(final Serializable fileId, final String destRelPath, final String versionMessage) {
    internalCopyOrMove(fileId, destRelPath, versionMessage, true);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public VersionSummary getVersionSummary(final Serializable fileId, final Serializable versionId) {
    Assert.notNull(fileId);
    return (VersionSummary) jcrTemplate.execute(new JcrCallback() {
      @Override
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        return JcrRepositoryFileUtils.getVersionSummary(session, pentahoJcrConstants, fileId, versionId);
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void restoreFileAtVersion(final Serializable fileId, final Serializable versionId, final String versionMessage) {
    Assert.notNull(fileId);
    Assert.notNull(versionId);
    jcrTemplate.execute(new JcrCallback() {
      @Override
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        Node fileNode = session.getNodeByIdentifier(fileId.toString());
        session.getWorkspace().getVersionManager().restore(fileNode.getPath(), versionId.toString(), true);
        return null;
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean canUnlockFile(final Serializable fileId) {
    return (Boolean) jcrTemplate.execute(new JcrCallback() {
      @Override
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        Node fileNode = session.getNodeByIdentifier(fileId.toString());
        Lock lock = session.getWorkspace().getLockManager().getLock(fileNode.getPath());
        return lockHelper.canUnlock(session, pentahoJcrConstants, lock);
      }
    });
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public RepositoryFileTree getTree(final String relPath, final int depth, final String filter, final boolean showHidden) {
    Assert.hasText(relPath);
    return (RepositoryFileTree) jcrTemplate.execute(new JcrCallback() {
      @Override
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        String absPath = pathConversionHelper.relToAbs(relPath);
        return JcrRepositoryFileUtils.getTree(session, pentahoJcrConstants, pathConversionHelper, lockHelper, absPath,
           depth, filter, showHidden);
      }
    });
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<RepositoryFile> getReferrers(final Serializable fileId) {
    if (fileId == null) {
      return new ArrayList<RepositoryFile>();
    }

    return (List<RepositoryFile>) jcrTemplate.execute(new JcrCallback() {
      @Override
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);

        Node fileNode = session.getNodeByIdentifier(fileId.toString());
        // guard against using a file retrieved from a more lenient session inside a more strict session
        Assert.notNull(fileNode);

        Set<RepositoryFile> referrers = new HashSet<RepositoryFile>();
        PropertyIterator refIter = fileNode.getReferences();
        if (refIter.hasNext()) {
          while (refIter.hasNext()) {
            // for each referrer property, march up the tree until we find the file node to which the property belongs
            RepositoryFile referrer = getReferrerFile(session, pentahoJcrConstants, refIter.nextProperty());
            if (referrer != null) {
              referrers.add(referrer);
            }
          }
        }
        session.save();
        return Arrays.asList(referrers.toArray());
      }
    });
  }

  protected RepositoryFile getReferrerFile(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Property referrerProperty) throws RepositoryException {
    Node currentNode = referrerProperty.getParent();
    while (!currentNode.isNodeType(pentahoJcrConstants.getPHO_NT_PENTAHOHIERARCHYNODE())) {
      currentNode = currentNode.getParent();
    }
    // if folder, then referrer is a lock token record (under the user's home folder) which will be cleaned up by 
    // lockHelper; ignore it
    if (currentNode.isNodeType(pentahoJcrConstants.getPHO_NT_PENTAHOFOLDER())) {
      return null;
    }

    return JcrRepositoryFileUtils.nodeToFile(session, pentahoJcrConstants, pathConversionHelper, lockHelper,
        currentNode);
  }

  @Override
  public void setFileMetadata(final Serializable fileId, final Map<String, Serializable> metadataMap) {
    Assert.notNull(fileId);
    jcrTemplate.execute(new JcrCallback() {
      @Override
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        JcrRepositoryFileUtils.setFileMetadata(session, fileId, metadataMap);
        return null;
      }
    });
  }

  @Override
  @SuppressWarnings("unchecked")
  public Map<String, Serializable> getFileMetadata(final Serializable fileId) {
    Assert.notNull(fileId);
    return (Map<String, Serializable>) jcrTemplate.execute(new JcrCallback() {
      @Override
      public Object doInJcr(Session session) throws IOException, RepositoryException {
        return JcrRepositoryFileUtils.getFileMetadata(session, fileId);
      }
    });
  }

  @Override
  public List<Character> getReservedChars() {
    return JcrRepositoryFileUtils.getReservedChars();
  }

  public IRepositoryDefaultAclHandler getDefaultAclHandler() {
    return defaultAclHandler;
  }

  public void setDefaultAclHandler(IRepositoryDefaultAclHandler defaultAclHandler) {
    this.defaultAclHandler = defaultAclHandler;
  }

  @Override
  public String getProductID() {
    return VersionHelper.getVersionInfo(this.getClass()).getProductID();
  }

  @Override
  public List<Locale> getAvailableLocalesForFileById(Serializable fileId) {
    RepositoryFile repositoryFile = getFileById(fileId, true);
    return getAvailableLocalesForFile(repositoryFile);
  }

  @Override
  public List<Locale> getAvailableLocalesForFileByPath(String relPath) {
    RepositoryFile repositoryFile = getFileById(relPath, true);
    return getAvailableLocalesForFile(repositoryFile);
  }

  @Override
  public List<Locale> getAvailableLocalesForFile(RepositoryFile repositoryFile){
    List<Locale> localeList = new ArrayList<Locale>();
    if(repositoryFile != null){
      for(String localeName : repositoryFile.getLocalePropertiesMap().keySet()){
        Locale locale = new Locale(localeName);
        localeList.add(locale);
      }
    }

    return localeList;
  }

  @Override
  public Properties getLocalePropertiesForFileById(Serializable fileId, String locale) {
    RepositoryFile repositoryFile = getFileById(fileId, true);
    return getLocalePropertiesForFile(repositoryFile, locale);
  }

  @Override
  public Properties getLocalePropertiesForFileByPath(String relPath, String locale) {

    RepositoryFile repositoryFile = getFileById(relPath, true);
    return getLocalePropertiesForFile(repositoryFile, locale);
  }

  @Override
  public Properties getLocalePropertiesForFile(RepositoryFile repositoryFile, String locale) {
    if(repositoryFile != null){
      Properties properties = repositoryFile.getLocalePropertiesMap().get(locale);
      return properties;
    }

    return null;
  }

  @Override
  public void setLocalePropertiesForFileById(Serializable fileId, String locale, Properties properties) {
    RepositoryFile repositoryFile = getFileById(fileId, true);
    setLocalePropertiesForFile(repositoryFile, locale, properties);
  }

  @Override
  public void setLocalePropertiesForFileByPath(String relPath, String locale, Properties properties) {
    RepositoryFile repositoryFile = getFileById(relPath, true);
    setLocalePropertiesForFile(repositoryFile, locale, properties);
  }

  @Override
  public void setLocalePropertiesForFile(final RepositoryFile repositoryFile, final String locale, final Properties properties) {
    Assert.notNull(repositoryFile);
    Assert.notNull(locale);
    Assert.notNull(properties);
    jcrTemplate.execute(new JcrCallback() {
      @Override
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        JcrRepositoryFileUtils.setFileLocaleProperties(session, repositoryFile.getId(), locale, properties);
        return null;
      }
    });
  }

  @Override
  public void deleteLocalePropertiesForFile(final RepositoryFile repositoryFile, final String locale) {
    Assert.notNull(repositoryFile);
    Assert.notNull(locale);
    jcrTemplate.execute(new JcrCallback() {
      @Override
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        JcrRepositoryFileUtils.deleteFileLocaleProperties(session, repositoryFile.getId(), locale);
        return null;
      }
    });
  }

}
