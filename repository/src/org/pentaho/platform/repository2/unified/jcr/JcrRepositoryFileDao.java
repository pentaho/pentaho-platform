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

import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.VersionSummary;
import org.pentaho.platform.repository2.messages.Messages;
import org.pentaho.platform.repository2.unified.IRepositoryFileAclDao;
import org.pentaho.platform.repository2.unified.IRepositoryFileDao;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.springframework.extensions.jcr.JcrCallback;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.Lock;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

  private IOwnerLookupHelper ownerLookupHelper;

  private IDeleteHelper deleteHelper;

  private IPathConversionHelper pathConversionHelper;

  private IEscapeHelper escapeHelper;

  private IRepositoryFileAclDao aclDao;

  // ~ Constructors ====================================================================================================

  public JcrRepositoryFileDao(final JcrTemplate jcrTemplate,
      final List<ITransformer<IRepositoryFileData>> transformers, final IOwnerLookupHelper ownerLookupHelper,
      final ILockHelper lockHelper, final IDeleteHelper deleteHelper, final IPathConversionHelper pathConversionHelper,
      final IEscapeHelper escapeHelper, final IRepositoryFileAclDao aclDao) {
    super();
    Assert.notNull(jcrTemplate);
    Assert.notNull(transformers);
    this.jcrTemplate = jcrTemplate;
    this.transformers = transformers;
    this.lockHelper = lockHelper;
    this.ownerLookupHelper = ownerLookupHelper;
    this.deleteHelper = deleteHelper;
    this.pathConversionHelper = pathConversionHelper;
    this.escapeHelper = escapeHelper;
    this.aclDao = aclDao;
  }

  // ~ Methods =========================================================================================================

  private RepositoryFile internalCreateFolder(final Session session, final Serializable parentFolderId, final RepositoryFile folder,
      final RepositoryFileAcl acl, final String versionMessage) throws RepositoryException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary(session, pentahoJcrConstants, parentFolderId);
        Node folderNode = JcrRepositoryFileUtils.createFolderNode(session, pentahoJcrConstants, parentFolderId, folder);
        // we must create the acl during checkout
        aclDao.createAcl(folderNode.getUUID(), acl);
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
        return JcrRepositoryFileUtils.nodeToFile(session, pentahoJcrConstants, ownerLookupHelper,
            pathConversionHelper, folderNode);
  }

  private RepositoryFile internalCreateFile(final Serializable parentFolderId, final RepositoryFile file,
      final IRepositoryFileData content, final RepositoryFileAcl acl, final String versionMessage) {
    Assert.notNull(file);
    Assert.hasText(file.getName());
    Assert.isTrue(!file.isFolder());
    Assert.notNull(content);

    return (RepositoryFile) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary(session, pentahoJcrConstants, parentFolderId);
        Node fileNode = JcrRepositoryFileUtils.createFileNode(session, pentahoJcrConstants, escapeHelper,
            parentFolderId, file, content, findTransformerForWrite(content.getClass()));
        // we must create the acl during checkout
        aclDao.createAcl(fileNode.getUUID(), acl);
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
        return JcrRepositoryFileUtils.nodeToFile(session, pentahoJcrConstants, ownerLookupHelper,
            pathConversionHelper, fileNode);
      }
    });
  }

  private RepositoryFile internalUpdateFile(final RepositoryFile file, final IRepositoryFileData content,
      final String versionMessage) {
    Assert.notNull(file);
    Assert.hasText(file.getName());
    Assert.isTrue(!file.isFolder());
    Assert.notNull(content);

    return (RepositoryFile) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        lockHelper.addLockTokenToSessionIfNecessary(session, pentahoJcrConstants, file.getId());
        JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary(session, pentahoJcrConstants, file.getId());
        JcrRepositoryFileUtils.updateFileNode(session, pentahoJcrConstants, escapeHelper, file, content,
            findTransformerForWrite(content.getClass()));
        session.save();
        JcrRepositoryFileUtils.checkinNearestVersionableFileIfNecessary(session, pentahoJcrConstants, file.getId(),
            versionMessage);
        lockHelper.removeLockTokenFromSessionIfNecessary(session, pentahoJcrConstants, file.getId());
        return JcrRepositoryFileUtils.nodeIdToFile(session, pentahoJcrConstants, ownerLookupHelper,
            pathConversionHelper, file.getId());
      }
    });
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
  public RepositoryFile createFile(final Serializable parentFolderId, final RepositoryFile file,
      final IRepositoryFileData content, final RepositoryFileAcl acl, final String versionMessage) {
    Assert.notNull(file);
    Assert.isTrue(!file.isFolder());
    return internalCreateFile(parentFolderId, file, content, acl, versionMessage);
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFile createFolder(final Serializable parentFolderId, final RepositoryFile folder,
      final RepositoryFileAcl acl, final String versionMessage) {
    Assert.notNull(folder);
    Assert.hasText(folder.getName());
    Assert.isTrue(!folder.getName().contains(RepositoryFile.SEPARATOR));
    Assert.isTrue(folder.isFolder());

    return (RepositoryFile) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        return internalCreateFolder(session, parentFolderId, folder, acl, versionMessage);
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFile getFileById(final Serializable fileId) {
    return internalGetFileById(fileId, false);
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFile getFileById(final Serializable fileId, final boolean loadMaps) {
    return internalGetFileById(fileId, loadMaps);
  }

  private RepositoryFile internalGetFileById(final Serializable fileId, final boolean loadMaps) {
    Assert.notNull(fileId);
    return (RepositoryFile) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        Node fileNode = session.getNodeByUUID(fileId.toString());
        return fileNode != null ? JcrRepositoryFileUtils.nodeToFile(session, pentahoJcrConstants, ownerLookupHelper,
            pathConversionHelper, fileNode, loadMaps) : null;
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFile getFile(final String relPath) {
    Assert.hasText(relPath);
    Assert.isTrue(relPath.startsWith(RepositoryFile.SEPARATOR));
    return (RepositoryFile) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
    String absPath = pathConversionHelper.relToAbs(relPath);
    return internalGetFile(session, absPath, false);
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFile getFileByAbsolutePath(final String absPath) {
    Assert.hasText(absPath);
    Assert.isTrue(absPath.startsWith(RepositoryFile.SEPARATOR));
    return (RepositoryFile) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
    return internalGetFile(session, absPath, false);
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFile getFile(final String relPath, final boolean loadMaps) {
    Assert.hasText(relPath);
    Assert.isTrue(relPath.startsWith(RepositoryFile.SEPARATOR));
    return (RepositoryFile) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
    String absPath = pathConversionHelper.relToAbs(relPath);
    return internalGetFile(session, absPath, loadMaps);
    }
  });
  }

  private RepositoryFile internalGetFile(final Session session, final String absPath, final boolean loadMaps) throws RepositoryException {

        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        Item fileNode;
        try {
          fileNode = session.getItem(absPath);
          // items are nodes or properties; this must be a node
          Assert.isTrue(fileNode.isNode());
        } catch (PathNotFoundException e) {
          fileNode = null;
        }
        return fileNode != null ? JcrRepositoryFileUtils.nodeToFile(session, pentahoJcrConstants, ownerLookupHelper,
            pathConversionHelper, (Node) fileNode, loadMaps) : null;
   
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  public <T extends IRepositoryFileData> T getData(final Serializable fileId, final Serializable versionId,
      final Class<T> contentClass) {
    Assert.notNull(fileId);
    return (T) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        return JcrRepositoryFileUtils.getContent(
            session,
            pentahoJcrConstants,
            escapeHelper,
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
  @SuppressWarnings("unchecked")
  public List<RepositoryFile> getChildren(final Serializable folderId, final String filter) {
    Assert.notNull(folderId);
    return (List<RepositoryFile>) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        return JcrRepositoryFileUtils.getChildren(session, pentahoJcrConstants, ownerLookupHelper,
            pathConversionHelper, folderId, filter);
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFile updateFile(final RepositoryFile file, final IRepositoryFileData content,
      final String versionMessage) {
    Assert.notNull(file);
    Assert.isTrue(!file.isFolder());
    return internalUpdateFile(file, content, versionMessage);
  }

  /**
   * {@inheritDoc}
   */
  public void lockFile(final Serializable fileId, final String message) {
    Assert.notNull(fileId);
    jcrTemplate.execute(new JcrCallback() {
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
  public void unlockFile(final Serializable fileId) {
    Assert.notNull(fileId);
    jcrTemplate.execute(new JcrCallback() {
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
  @SuppressWarnings("unchecked")
  public List<VersionSummary> getVersionSummaries(final Serializable fileId) {
    Assert.notNull(fileId);
    return (List<VersionSummary>) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        return JcrRepositoryFileUtils.getVersionSummaries(session, pentahoJcrConstants, fileId);
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFile getFile(final Serializable fileId, final Serializable versionId) {
    Assert.notNull(fileId);
    Assert.notNull(versionId);
    return (RepositoryFile) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        return JcrRepositoryFileUtils.getFileAtVersion(session, pentahoJcrConstants, ownerLookupHelper,
            pathConversionHelper, fileId, versionId);
      }
    });
  }

  public void setLockTokenHelper(final ILockHelper lockTokenHelper) {
    Assert.notNull(lockTokenHelper);
    this.lockHelper = lockTokenHelper;
  }

  public void setOwnerLookupHelper(final IOwnerLookupHelper ownerLookupHelper) {
    Assert.notNull(ownerLookupHelper);
    this.ownerLookupHelper = ownerLookupHelper;
  }

  /**
   * {@inheritDoc}
   */
  public void deleteFile(final Serializable fileId, final String versionMessage) {
    Assert.notNull(fileId);
    jcrTemplate.execute(new JcrCallback() {
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
  public void deleteFileAtVersion(final Serializable fileId, final Serializable versionId) {
    Assert.notNull(fileId);
    Assert.notNull(versionId);
    jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        Node fileToDeleteNode = session.getNodeByUUID(fileId.toString());
        fileToDeleteNode.getVersionHistory().removeVersion(versionId.toString());
        session.save();
        return null;
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  public List<RepositoryFile> getDeletedFiles(final String origParentFolderPath, final String filter) {
    Assert.hasLength(origParentFolderPath);
    return (List<RepositoryFile>) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        return deleteHelper.getDeletedFiles(session, pentahoJcrConstants, origParentFolderPath, filter);
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  public List<RepositoryFile> getDeletedFiles() {
    return (List<RepositoryFile>) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        return deleteHelper.getDeletedFiles(session, pentahoJcrConstants);
      }
    });
  }

  /**
   * {@inheritDoc}
   * 
   * <p>
   * No checkout needed as .trash is not versioned.
   * </p>
   */
  public void permanentlyDeleteFile(final Serializable fileId, final String versionMessage) {
    Assert.notNull(fileId);
    jcrTemplate.execute(new JcrCallback() {
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
  public void undeleteFile(final Serializable fileId, final String versionMessage) {
    Assert.notNull(fileId);
    jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        String absOrigParentFolderPath = deleteHelper.getOriginalParentFolderPath(session, pentahoJcrConstants, fileId);
        Serializable origParentFolderId = null;
        // original parent folder path may no longer exist!
        if (session.itemExists(absOrigParentFolderPath)) {
          origParentFolderId = ((Node) session.getItem(absOrigParentFolderPath)).getUUID();
        } else {
          // go through each of the segments of the original parent folder path, creating as necessary
          String[] segments = pathConversionHelper.absToRel(absOrigParentFolderPath).split(RepositoryFile.SEPARATOR);
          RepositoryFile lastParentFolder = internalGetFile(session, ServerRepositoryPaths.getTenantRootFolderPath(), false);
          for (String segment : segments) {
            if (StringUtils.hasLength(segment)) {
              RepositoryFile tmp = internalGetFile(session, pathConversionHelper.relToAbs((lastParentFolder.getPath().equals(RepositoryFile.SEPARATOR) ? "" : lastParentFolder.getPath()) + RepositoryFile.SEPARATOR + segment), false); //$NON-NLS-1$
              if (tmp == null) {
                lastParentFolder = internalCreateFolder(session, lastParentFolder.getId(), new RepositoryFile.Builder(segment).folder(true).build(), aclDao.createDefaultAcl(), null);
              } else {
                lastParentFolder = tmp;
              }
            }
          }
          origParentFolderId = lastParentFolder.getId();
        }
        JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary(session, pentahoJcrConstants, origParentFolderId);
        deleteHelper.undeleteFile(session, pentahoJcrConstants, fileId);
        session.save();
        JcrRepositoryFileUtils.checkinNearestVersionableFileIfNecessary(session, pentahoJcrConstants, origParentFolderId,
            versionMessage);
        return null;
      }
    });
  }
  
  private void internalCopyOrMove(final Serializable fileId, final String destRelPath, final String versionMessage, final boolean copy) {
    Assert.notNull(fileId);
    jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        String destAbsPath = pathConversionHelper.relToAbs(destRelPath);
        String cleanDestAbsPath = destAbsPath;
        if (cleanDestAbsPath.endsWith(RepositoryFile.SEPARATOR)) {
          cleanDestAbsPath.substring(0, cleanDestAbsPath.length() - 1);
        }
        Node srcFileNode = session.getNodeByUUID(fileId.toString());
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
        String finalDestAbsPath = appendFileName ? cleanDestAbsPath + RepositoryFile.SEPARATOR + srcFileNode.getName() : cleanDestAbsPath; 
        if (copy) {
          session.getWorkspace().copy(finalSrcAbsPath, finalDestAbsPath);
        } else {
          session.getWorkspace().move(finalSrcAbsPath, finalDestAbsPath);
        }
        JcrRepositoryFileUtils.checkinNearestVersionableNodeIfNecessary(session, pentahoJcrConstants,
            destParentFolderNode, versionMessage);
        // if it's a move within the same folder, then the next checkin is unnecessary
        if (!copy && !destParentFolderNode.getUUID().equals(srcParentFolderId.toString())) {
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
  public void moveFile(final Serializable fileId, final String destRelPath, final String versionMessage) {
    internalCopyOrMove(fileId, destRelPath, versionMessage, false);
  }

  /**
   * {@inheritDoc}
   */
  public void copyFile(final Serializable fileId, final String destRelPath, final String versionMessage) {
    internalCopyOrMove(fileId, destRelPath, versionMessage, true);
  }
  
  /**
   * {@inheritDoc}
   */
  public VersionSummary getVersionSummary(final Serializable fileId, final Serializable versionId) {
    Assert.notNull(fileId);
    return (VersionSummary) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        return JcrRepositoryFileUtils.getVersionSummary(session, pentahoJcrConstants, fileId, versionId);
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  public void restoreFileAtVersion(final Serializable fileId, final Serializable versionId, final String versionMessage) {
    Assert.notNull(fileId);
    Assert.notNull(versionId);
    jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        RepositoryFile file = getFile(fileId, versionId);
        // fool the preventLostUpdate check by setting this file's version to the base version
        Serializable baseVersionId = JcrRepositoryFileUtils.getBaseVersionId(session, fileId);
        RepositoryFile.Builder builder = new RepositoryFile.Builder(file).versionId(baseVersionId);
        Assert.isTrue(!file.isFolder());
        IRepositoryFileData fileData = getData(fileId, versionId, IRepositoryFileData.class);
        updateFile(builder.build(), fileData, versionMessage);
        return null;
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  public boolean canUnlockFile(final Serializable fileId) {
    return (Boolean) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        Node fileNode = session.getNodeByUUID(fileId.toString());
        Lock lock = fileNode.getLock();
        return lockHelper.canUnlock(session, pentahoJcrConstants, lock);
      }
    });
  }

 /**
   * {@inheritDoc}
   */

  public RepositoryFileTree getTree(final String relPath, final int depth, final String filter, final boolean showHidden) {
    Assert.hasText(relPath);
    return (RepositoryFileTree) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        String absPath = pathConversionHelper.relToAbs(relPath);
        return JcrRepositoryFileUtils.getTree(session, pentahoJcrConstants, ownerLookupHelper, pathConversionHelper,
            absPath, depth, filter, showHidden);
      }
    });
  }

  @SuppressWarnings("unchecked")
  public List<RepositoryFile> getReferrers(final Serializable fileId) {
    if (fileId == null) {
      return new ArrayList<RepositoryFile>();
    }

    return (List<RepositoryFile>) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);

        Node fileNode = session.getNodeByUUID(fileId.toString());
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
    } else {
      return JcrRepositoryFileUtils.nodeToFile(session, pentahoJcrConstants, ownerLookupHelper, pathConversionHelper,
          currentNode);
    }
  }
  
  public void setFileMetadata(final Serializable fileId, final Map<String, Serializable> metadataMap) {
    Assert.notNull(fileId);
    jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        JcrRepositoryFileUtils.setFileMetadata(session, fileId, metadataMap);
        return null;
      }
    });
  }
  
  public Map<String, Serializable> getFileMetadata(final Serializable fileId) {
    Assert.notNull(fileId);
    return (Map<String, Serializable>) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(Session session) throws IOException, RepositoryException {
        return JcrRepositoryFileUtils.getFileMetadata(session, fileId);
      }     
    });
  }
}
