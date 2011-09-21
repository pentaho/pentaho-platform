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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jcr.Item;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.lock.Lock;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;

import org.apache.jackrabbit.JcrConstants;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.VersionSummary;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Class of static methods where the real JCR work takes place.
 * 
 * @author mlowery
 */
public class JcrRepositoryFileUtils {
  public static RepositoryFile getFileById(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final IOwnerLookupHelper ownerLookupHelper, final IPathConversionHelper pathConversionHelper,
      final Serializable fileId) throws RepositoryException {
    Node fileNode = session.getNodeByUUID(fileId.toString());
    Assert.notNull(fileNode);
    return nodeToFile(session, pentahoJcrConstants, ownerLookupHelper, pathConversionHelper, fileNode);
  }

  public static RepositoryFile nodeToFile(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final IOwnerLookupHelper ownerLookupHelper, final IPathConversionHelper pathConversionHelper, final Node node)
      throws RepositoryException {
    return nodeToFile(session, pentahoJcrConstants, ownerLookupHelper, pathConversionHelper, node, false);
  }

  private static RepositoryFile getRootFolder(final Session session) throws RepositoryException {
    Node node = session.getRootNode();
    RepositoryFile file = new RepositoryFile.Builder(node.getUUID(), "").folder(true).versioned(false).path( //$NON-NLS-1$
        node.getPath()).build();
    return file;
  }

  public static RepositoryFile nodeToFile(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final IOwnerLookupHelper ownerLookupHelper, final IPathConversionHelper pathConversionHelper, final Node node,
      final boolean loadMaps) throws RepositoryException {

    if (session.getRootNode().isSame(node)) {
      return getRootFolder(session);
    }

    Assert.isTrue(isSupportedNodeType(pentahoJcrConstants, node));

    Serializable id = null;
    String name = null;
    String path = null;
    long fileSize = 0;
    Date created = null;
    String creatorId = null;
    Boolean hidden = false;
    Date lastModified = null;
    boolean folder = false;
    boolean versioned = false;
    Serializable versionId = null;
    boolean locked = false;
    String lockOwner = null;
    Date lockDate = null;
    String lockMessage = null;
    RepositoryFileSid owner = null;
    String title = null;
    String description = null;
    Map<String, String> titleMap = null;
    Map<String, String> descriptionMap = null;
    
    String locale = null;

    id = getNodeId(session, pentahoJcrConstants, node);
    path = pathConversionHelper.absToRel((getAbsolutePath(session, pentahoJcrConstants, node)));
    // if the rel path is / then name the folder empty string instead of its true name (this hides the tenant name)
    name = RepositoryFile.SEPARATOR.equals(path) ? "" : getNodeName(session, pentahoJcrConstants, node); //$NON-NLS-1$

    if (isPentahoFolder(pentahoJcrConstants, node)) {
      folder = true;
    }

    // jcr:created nodes have OnParentVersion values of INITIALIZE
    if (node.hasProperty(pentahoJcrConstants.getJCR_CREATED())) {
      Calendar tmpCal = node.getProperty(pentahoJcrConstants.getJCR_CREATED()).getDate();
      if (tmpCal != null) {
        created = tmpCal.getTime();
      }
    }

    Map<String, Serializable> metadata = getFileMetadata(session, id);
    creatorId = (String) metadata.get(PentahoJcrConstants.PHO_CONTENTCREATOR);
    hidden = node.getProperty(pentahoJcrConstants.getPHO_HIDDEN()).getBoolean();
    if (isPentahoFile(pentahoJcrConstants, node)) {
      fileSize = node.getProperty(pentahoJcrConstants.getPHO_FILESIZE()).getLong();
    }
    if (isPentahoFile(pentahoJcrConstants, node)) {
      // pho:lastModified nodes have OnParentVersion values of IGNORE; i.e. they don't exist in frozen nodes
      if (!node.isNodeType(pentahoJcrConstants.getNT_FROZENNODE())) {
        Calendar tmpCal = node.getProperty(pentahoJcrConstants.getPHO_LASTMODIFIED()).getDate();
        if (tmpCal != null) {
          lastModified = tmpCal.getTime();
        }
      }
    }

    if (isPentahoHierarchyNode(session, pentahoJcrConstants, node)) {
      if (node.hasNode(pentahoJcrConstants.getPHO_TITLE())) {
        title = getLocalizedString(session, pentahoJcrConstants, node.getNode(pentahoJcrConstants.getPHO_TITLE()));
      }
      if (node.hasNode(pentahoJcrConstants.getPHO_DESCRIPTION())) {
        description = getLocalizedString(session, pentahoJcrConstants,
            node.getNode(pentahoJcrConstants.getPHO_DESCRIPTION()));
      }

      if (loadMaps) {
        if (node.hasNode(pentahoJcrConstants.getPHO_TITLE())) {
          titleMap = getLocalizedStringMap(session, pentahoJcrConstants,
              node.getNode(pentahoJcrConstants.getPHO_TITLE()));
        }
        if (node.hasNode(pentahoJcrConstants.getPHO_DESCRIPTION())) {
          descriptionMap = getLocalizedStringMap(session, pentahoJcrConstants,
              node.getNode(pentahoJcrConstants.getPHO_DESCRIPTION()));
        }
      }
    }

    locale = getLocale().toString();

    versioned = isVersioned(session, pentahoJcrConstants, node);
    if (versioned) {
      versionId = getVersionId(pentahoJcrConstants, node);
    }

    locked = isLocked(pentahoJcrConstants, node);
    if (locked) {
      Lock lock = node.getLock();
      lockOwner = lock.getLockOwner();
      lockDate = node.getProperty(pentahoJcrConstants.getPHO_LOCKDATE()).getDate().getTime();
      if (node.hasProperty(pentahoJcrConstants.getPHO_LOCKMESSAGE())) {
        lockMessage = node.getProperty(pentahoJcrConstants.getPHO_LOCKMESSAGE()).getString();
      }
    }

    owner = getRepositoryFileSid(session, pentahoJcrConstants, ownerLookupHelper, node);

    RepositoryFile file = new RepositoryFile.Builder(id, name).createdDate(created).creatorId(creatorId).lastModificationDate(lastModified)
        .folder(folder).versioned(versioned).path(path).versionId(versionId).fileSize(fileSize).locked(locked).lockDate(lockDate).hidden(hidden)
        .lockMessage(lockMessage).lockOwner(lockOwner).owner(owner).title(title).description(description).titleMap(
            titleMap).descriptionMap(descriptionMap).locale(locale).build();

    return file;
  }

  private static Locale getLocale() {
    // TODO get locale from somewhere else
    return Locale.getDefault();
  }

  private static String getLocalizedString(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Node localizedStringNode) throws RepositoryException {
    Assert.isTrue(isLocalizedString(session, pentahoJcrConstants, localizedStringNode));

    Locale locale = getLocale();

    final String UNDERSCORE = "_"; //$NON-NLS-1$
    boolean hasLanguage = StringUtils.hasText(locale.getLanguage());
    boolean hasCountry = StringUtils.hasText(locale.getCountry());
    boolean hasVariant = StringUtils.hasText(locale.getVariant());

    List<String> candidatePropertyNames = new ArrayList<String>(3);

    if (hasVariant) {
      candidatePropertyNames.add(locale.getLanguage() + UNDERSCORE + locale.getCountry() + UNDERSCORE
          + locale.getVariant());
    }
    if (hasCountry) {
      candidatePropertyNames.add(locale.getLanguage() + UNDERSCORE + locale.getCountry());
    }
    if (hasLanguage) {
      candidatePropertyNames.add(locale.getLanguage());
    }

    for (String propertyName : candidatePropertyNames) {
      if (localizedStringNode.hasProperty(propertyName)) {
        return localizedStringNode.getProperty(propertyName).getString();
      }
    }
    return localizedStringNode.getProperty(pentahoJcrConstants.getPHO_ROOTLOCALE()).getString();
  }

  private static Map<String, String> getLocalizedStringMap(final Session session,
      final PentahoJcrConstants pentahoJcrConstants, final Node localizedStringNode) throws RepositoryException {
    Assert.isTrue(isLocalizedString(session, pentahoJcrConstants, localizedStringNode));
    String prefix = session.getNamespacePrefix(PentahoJcrConstants.PHO_NS);
    Assert.hasText(prefix);
    Map<String, String> localizedStringMap = new HashMap<String, String>();
    PropertyIterator propertyIter = localizedStringNode.getProperties();
    while (propertyIter.hasNext()) {
      Property property = propertyIter.nextProperty();
      localizedStringMap.put(property.getName().substring(prefix.length() + 1), property.getString());
    }
    return localizedStringMap;
  }

  /**
   * Sets localized string.
   */
  private static void setLocalizedStringMap(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Node localizedStringNode, final Map<String, String> map) throws RepositoryException {
    Assert.isTrue(isLocalizedString(session, pentahoJcrConstants, localizedStringNode));

    String prefix = session.getNamespacePrefix(PentahoJcrConstants.PHO_NS);
    Assert.hasText(prefix);
    PropertyIterator propertyIter = localizedStringNode.getProperties();
    while (propertyIter.hasNext()) {
      Property prop = propertyIter.nextProperty();
      if (prop.getName().startsWith(prefix)) {
        prop.remove();
      }
    }

    for (Map.Entry<String, String> entry : map.entrySet()) {
      localizedStringNode.setProperty(prefix + ":" + entry.getKey(), entry.getValue()); //$NON-NLS-1$
    }
  }

  private static RepositoryFileSid getRepositoryFileSid(final Session session,
      final PentahoJcrConstants pentahoJcrConstants, final IOwnerLookupHelper ownerLookupHelper, final Node node)
      throws RepositoryException {
    Node nonFrozenNode = null;
    if (node.isNodeType(pentahoJcrConstants.getNT_FROZENNODE())) {
      nonFrozenNode = session.getNodeByUUID(node.getProperty(pentahoJcrConstants.getJCR_FROZENUUID()).getString());
    } else {
      nonFrozenNode = node;
    }
    return ownerLookupHelper.getOwner(session, pentahoJcrConstants, nonFrozenNode);
  }

  private static String getAbsolutePath(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Node node) throws RepositoryException {
    if (node.isNodeType(pentahoJcrConstants.getNT_FROZENNODE())) {
      return session.getNodeByUUID(node.getProperty(pentahoJcrConstants.getJCR_FROZENUUID()).getString()).getPath();
    } else {
      return node.getPath();
    }
  }

  private static Serializable getNodeId(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Node node) throws RepositoryException {
    if (node.isNodeType(pentahoJcrConstants.getNT_FROZENNODE())) {
      return node.getProperty(pentahoJcrConstants.getJCR_FROZENUUID()).getString();
    } else {
      return node.getUUID();
    }
  }

  private static String getNodeName(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Node node) throws RepositoryException {
    if (node.isNodeType(pentahoJcrConstants.getNT_FROZENNODE())) {
      return session.getNodeByUUID(node.getProperty(pentahoJcrConstants.getJCR_FROZENUUID()).getString()).getName();
    } else {
      return node.getName();
    }
  }

  private static String getVersionId(final PentahoJcrConstants pentahoJcrConstants, final Node node)
      throws RepositoryException {
    if (node.isNodeType(pentahoJcrConstants.getNT_FROZENNODE())) {
      return node.getParent().getName();
    } else {
      return node.getBaseVersion().getName();
    }

  }

  public static Node createFolderNode(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Serializable parentFolderId, final RepositoryFile folder) throws RepositoryException {
    Node parentFolderNode;
    if (parentFolderId != null) {
      parentFolderNode = session.getNodeByUUID(parentFolderId.toString());
    } else {
      parentFolderNode = session.getRootNode();
    }

    // guard against using a file retrieved from a more lenient session inside a more strict session
    Assert.notNull(parentFolderNode);

    Node folderNode = parentFolderNode.addNode(folder.getName(), pentahoJcrConstants.getPHO_NT_PENTAHOFOLDER());
    folderNode.setProperty(pentahoJcrConstants.getPHO_HIDDEN(), folder.isHidden());
    if (folder.isVersioned()) {
      //      folderNode.setProperty(addPentahoPrefix(session, PentahoJcrConstants.PENTAHO_VERSIONED), true);
      folderNode.addMixin(pentahoJcrConstants.getPHO_MIX_VERSIONABLE());
    }
    folderNode.addNode(pentahoJcrConstants.getPHO_METADATA(), JcrConstants.NT_UNSTRUCTURED);
    folderNode.addMixin(pentahoJcrConstants.getMIX_REFERENCEABLE());
    return folderNode;
  }

  public static Node createFileNode(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final IEscapeHelper escapeHelper, final Serializable parentFolderId, final RepositoryFile file,
      final IRepositoryFileData content, final ITransformer<IRepositoryFileData> transformer)
      throws RepositoryException {

    Node parentFolderNode;
    if (parentFolderId != null) {
      parentFolderNode = session.getNodeByUUID(parentFolderId.toString());
    } else {
      parentFolderNode = session.getRootNode();
    }

    // guard against using a file retrieved from a more lenient session inside a more strict session
    Assert.notNull(parentFolderNode);

    Node fileNode = parentFolderNode.addNode(file.getName(), pentahoJcrConstants.getPHO_NT_PENTAHOFILE());
    fileNode.setProperty(pentahoJcrConstants.getPHO_CONTENTTYPE(), transformer.getContentType());
    fileNode.setProperty(pentahoJcrConstants.getPHO_LASTMODIFIED(), Calendar.getInstance());
    fileNode.setProperty(pentahoJcrConstants.getPHO_HIDDEN(), file.isHidden());
    fileNode.setProperty(pentahoJcrConstants.getPHO_FILESIZE(), content.getDataSize());
    if (file.getTitleMap() != null && !file.getTitleMap().isEmpty()) {
      Node titleNode = fileNode.addNode(pentahoJcrConstants.getPHO_TITLE(),
          pentahoJcrConstants.getPHO_NT_LOCALIZEDSTRING());
      setLocalizedStringMap(session, pentahoJcrConstants, titleNode, file.getTitleMap());
    }
    if (file.getDescriptionMap() != null && !file.getDescriptionMap().isEmpty()) {
      Node descriptionNode = fileNode.addNode(pentahoJcrConstants.getPHO_DESCRIPTION(),
          pentahoJcrConstants.getPHO_NT_LOCALIZEDSTRING());
      setLocalizedStringMap(session, pentahoJcrConstants, descriptionNode, file.getDescriptionMap());
    }
    Node metaNode = fileNode.addNode(pentahoJcrConstants.getPHO_METADATA(), JcrConstants.NT_UNSTRUCTURED);
    setMetadataItemForFile(session, PentahoJcrConstants.PHO_CONTENTCREATOR, file.getCreatorId(), metaNode);
    fileNode.addMixin(pentahoJcrConstants.getPHO_MIX_LOCKABLE());
    fileNode.addMixin(pentahoJcrConstants.getMIX_REFERENCEABLE());

    if (file.isVersioned()) {
      //      fileNode.setProperty(addPentahoPrefix(session, PentahoJcrConstants.PENTAHO_VERSIONED), true);
      fileNode.addMixin(pentahoJcrConstants.getPHO_MIX_VERSIONABLE());
    }

    transformer.createContentNode(session, pentahoJcrConstants, escapeHelper, content, fileNode);
    return fileNode;
  }

  private static void preventLostUpdate(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final RepositoryFile file) throws RepositoryException {
    Node fileNode = session.getNodeByUUID(file.getId().toString());
    // guard against using a file retrieved from a more lenient session inside a more strict session
    Assert.notNull(fileNode);
    if (isVersioned(session, pentahoJcrConstants, fileNode)) {
      Assert.notNull(file.getVersionId(), "updating a versioned file requires a non-null version id"); //$NON-NLS-1$
      Assert.state(fileNode.getBaseVersion().getName().equals(file.getVersionId().toString()),
          "update to this file has occurred since its last read"); //$NON-NLS-1$
    }
  }

  public static Node updateFileNode(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final IEscapeHelper escapeHelper, final RepositoryFile file, final IRepositoryFileData content,
      final ITransformer<IRepositoryFileData> transformer) throws RepositoryException {

    Node fileNode = session.getNodeByUUID(file.getId().toString());
    // guard against using a file retrieved from a more lenient session inside a more strict session
    Assert.notNull(fileNode);

    preventLostUpdate(session, pentahoJcrConstants, file);

    fileNode.setProperty(pentahoJcrConstants.getPHO_CONTENTTYPE(), transformer.getContentType());
    fileNode.setProperty(pentahoJcrConstants.getPHO_LASTMODIFIED(), Calendar.getInstance());
    fileNode.setProperty(pentahoJcrConstants.getPHO_HIDDEN(), file.isHidden());
    fileNode.setProperty(pentahoJcrConstants.getPHO_FILESIZE(), content.getDataSize());
    if (file.getTitleMap() != null && !file.getTitleMap().isEmpty()) {
      Node titleNode = null;
      if (!fileNode.hasNode(pentahoJcrConstants.getPHO_TITLE())) {
        titleNode = fileNode.addNode(pentahoJcrConstants.getPHO_TITLE(),
            pentahoJcrConstants.getPHO_NT_LOCALIZEDSTRING());
      } else {
        titleNode = fileNode.getNode(pentahoJcrConstants.getPHO_TITLE());
      }
      setLocalizedStringMap(session, pentahoJcrConstants, titleNode, file.getTitleMap());
    }
    if (file.getDescriptionMap() != null && !file.getDescriptionMap().isEmpty()) {
      Node descriptionNode = null;
      if (!fileNode.hasNode(pentahoJcrConstants.getPHO_DESCRIPTION())) {
        descriptionNode = fileNode.addNode(pentahoJcrConstants.getPHO_DESCRIPTION(),
            pentahoJcrConstants.getPHO_NT_LOCALIZEDSTRING());
      } else {
        descriptionNode = fileNode.getNode(pentahoJcrConstants.getPHO_DESCRIPTION());
      }
      setLocalizedStringMap(session, pentahoJcrConstants, descriptionNode, file.getDescriptionMap());
    }

    if (file.getCreatorId() != null) {
      Node metadataNode = null;
      if (!fileNode.hasNode(pentahoJcrConstants.getPHO_METADATA())) {
        metadataNode = fileNode.addNode(pentahoJcrConstants.getPHO_METADATA(), JcrConstants.NT_UNSTRUCTURED);
      } else {
        metadataNode = fileNode.getNode(pentahoJcrConstants.getPHO_METADATA());
      }
      setMetadataItemForFile(session, PentahoJcrConstants.PHO_CONTENTCREATOR, file.getCreatorId(), metadataNode);
    }
    transformer.updateContentNode(session, pentahoJcrConstants, escapeHelper, content, fileNode);
    return fileNode;
  }

  public static IRepositoryFileData getContent(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final IEscapeHelper escapeHelper, final Serializable fileId, final Serializable versionId,
      final ITransformer<IRepositoryFileData> transformer) throws RepositoryException {
    Node fileNode = session.getNodeByUUID(fileId.toString());
    if (isVersioned(session, pentahoJcrConstants, fileNode)) {
      Version version = null;
      if (versionId != null) {
        version = fileNode.getVersionHistory().getVersion(versionId.toString());
      } else {
        version = fileNode.getBaseVersion();
      }
      fileNode = getNodeAtVersion(pentahoJcrConstants, version);
    }
    Assert.isTrue(!isPentahoFolder(pentahoJcrConstants, fileNode));

    return transformer.fromContentNode(session, pentahoJcrConstants, escapeHelper, fileNode);
  }

  public static List<RepositoryFile> getChildren(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final IOwnerLookupHelper ownerLookupHelper, final IPathConversionHelper pathConversionHelper,
      final Serializable folderId, final String filter) throws RepositoryException {
    Node folderNode = session.getNodeByUUID(folderId.toString());
    Assert.isTrue(isPentahoFolder(pentahoJcrConstants, folderNode));

    List<RepositoryFile> children = new ArrayList<RepositoryFile>();
    // get all immediate child nodes that are of type PHO_NT_PENTAHOFOLDER or PHO_NT_PENTAHOFILE
    NodeIterator nodeIterator = null;
    if (filter != null) {
      nodeIterator = folderNode.getNodes(filter);
    } else {
      nodeIterator = folderNode.getNodes();
    }

    while (nodeIterator.hasNext()) {
      Node node = nodeIterator.nextNode();
      if (isSupportedNodeType(pentahoJcrConstants, node)) {
        children.add(nodeToFile(session, pentahoJcrConstants, ownerLookupHelper, pathConversionHelper, node));
      }
    }
    Collections.sort(children);
    return children;
  }

  public static boolean isPentahoFolder(final PentahoJcrConstants pentahoJcrConstants, final Node node)
      throws RepositoryException {
    Assert.notNull(node);
    if (node.isNodeType(pentahoJcrConstants.getNT_FROZENNODE())) {
      String nodeTypeName = node.getProperty(pentahoJcrConstants.getJCR_FROZENPRIMARYTYPE()).getString();
      return pentahoJcrConstants.getPHO_NT_PENTAHOFOLDER().equals(nodeTypeName);
    } else {
      return node.isNodeType(pentahoJcrConstants.getPHO_NT_PENTAHOFOLDER());
    }
  }

  private static boolean isPentahoHierarchyNode(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Node node) throws RepositoryException {
    Assert.notNull(node);
    if (node.isNodeType(pentahoJcrConstants.getNT_FROZENNODE())) {
      String nodeTypeName = node.getProperty(pentahoJcrConstants.getJCR_FROZENPRIMARYTYPE()).getString();
      // TODO mlowery add PENTAHOLINKEDFILE here when it is available
      return pentahoJcrConstants.getPHO_NT_PENTAHOFOLDER().equals(nodeTypeName)
          || pentahoJcrConstants.getPHO_NT_PENTAHOFILE().equals(nodeTypeName);
    } else {
      return node.isNodeType(pentahoJcrConstants.getPHO_NT_PENTAHOHIERARCHYNODE());
    }
  }

  private static boolean isLocked(final PentahoJcrConstants pentahoJcrConstants, final Node node)
      throws RepositoryException {
    Assert.notNull(node);
    if (node.isNodeType(pentahoJcrConstants.getNT_FROZENNODE())) {
      // frozen nodes are never locked
      return false;
    }
    boolean locked = node.isLocked();
    if (locked) {
      Assert.isTrue(node.isNodeType(pentahoJcrConstants.getPHO_MIX_LOCKABLE()));
    }
    return locked;
  }

  public static boolean isPentahoFile(final PentahoJcrConstants pentahoJcrConstants, final Node node)
      throws RepositoryException {
    Assert.notNull(node);
    if (node.isNodeType(pentahoJcrConstants.getNT_FROZENNODE())) {
      String primaryTypeName = node.getProperty(pentahoJcrConstants.getJCR_FROZENPRIMARYTYPE()).getString();
      if (pentahoJcrConstants.getPHO_NT_PENTAHOFILE().equals(primaryTypeName)) {
        return true;
      }
      return false;
    } else {
      return node.isNodeType(pentahoJcrConstants.getPHO_NT_PENTAHOFILE());
    }
  }

  private static boolean isLocalizedString(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Node node) throws RepositoryException {
    Assert.notNull(node);
    if (node.isNodeType(pentahoJcrConstants.getNT_FROZENNODE())) {
      String frozenPrimaryType = node.getProperty(pentahoJcrConstants.getJCR_FROZENPRIMARYTYPE()).getString();
      if (pentahoJcrConstants.getPHO_NT_LOCALIZEDSTRING().equals(frozenPrimaryType)) {
        return true;
      }
      return false;
    } else {
      return node.isNodeType(pentahoJcrConstants.getPHO_NT_LOCALIZEDSTRING());
    }
  }

  private static boolean isVersioned(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Node node) throws RepositoryException {
    Assert.notNull(node);
    if (node.isNodeType(pentahoJcrConstants.getNT_FROZENNODE())) {
      // frozen nodes represent the nodes at a particular version; so yes, they are versioned!
      return true;
    } else {
      return node.isNodeType(pentahoJcrConstants.getPHO_MIX_VERSIONABLE());
    }
  }

  public static boolean isSupportedNodeType(final PentahoJcrConstants pentahoJcrConstants, final Node node)
      throws RepositoryException {
    Assert.notNull(node);
    if (node.isNodeType(pentahoJcrConstants.getNT_FROZENNODE())) {
      String nodeTypeName = node.getProperty(pentahoJcrConstants.getJCR_FROZENPRIMARYTYPE()).getString();
      return pentahoJcrConstants.getPHO_NT_PENTAHOFILE().equals(nodeTypeName)
          || pentahoJcrConstants.getPHO_NT_PENTAHOFOLDER().equals(nodeTypeName);
    } else {
      return node.isNodeType(pentahoJcrConstants.getPHO_NT_PENTAHOFILE())
          || node.isNodeType(pentahoJcrConstants.getPHO_NT_PENTAHOFOLDER());
    }
  }

  /**
   * Conditionally checks out node representing file if node is versionable.
   */
  public static void checkoutNearestVersionableFileIfNecessary(final Session session,
      final PentahoJcrConstants pentahoJcrConstants, final Serializable fileId) throws RepositoryException {
    // file could be null meaning the caller is using null as the parent folder; that's OK; in this case the node in
    // question would be the repository root node and that is never versioned
    if (fileId != null) {
      Node node = session.getNodeByUUID(fileId.toString());
      checkoutNearestVersionableNodeIfNecessary(session, pentahoJcrConstants, node);
    }
  }

  /**
   * Conditionally checks out node if node is versionable.
   */
  public static void checkoutNearestVersionableNodeIfNecessary(final Session session,
      final PentahoJcrConstants pentahoJcrConstants, final Node node) throws RepositoryException {
    Assert.notNull(node);

    Node versionableNode = findNearestVersionableNode(session, pentahoJcrConstants, node);

    if (versionableNode != null) {
      versionableNode.checkout();
    }
  }

  /**
   * Conditionally checks in node representing file if node is versionable.
   */
  public static void checkinNearestVersionableFileIfNecessary(final Session session,
      final PentahoJcrConstants pentahoJcrConstants, final Serializable fileId, final String versionMessage)
      throws RepositoryException {
    // file could be null meaning the caller is using null as the parent folder; that's OK; in this case the node in
    // question would be the repository root node and that is never versioned
    if (fileId != null) {
      Node node = session.getNodeByUUID(fileId.toString());
      checkinNearestVersionableNodeIfNecessary(session, pentahoJcrConstants, node, versionMessage);
    }
  }

  /**
   * Conditionally checks in node if node is versionable.
   * 
   * TODO mlowery move commented out version labeling to its own method
   */
  public static void checkinNearestVersionableNodeIfNecessary(final Session session,
      final PentahoJcrConstants pentahoJcrConstants, final Node node, final String versionMessage)
      throws RepositoryException {
    Assert.notNull(node);
    session.save();
    /*
      session.save must be called inside the versionable node block and outside to ensure user changes are made when
      a file is not versioned.
    */
    Node versionableNode = findNearestVersionableNode(session, pentahoJcrConstants, node);

    if (versionableNode != null) {
      versionableNode.setProperty(pentahoJcrConstants.getPHO_VERSIONAUTHOR(), getUsername());
      if (StringUtils.hasText(versionMessage)) {
        versionableNode.setProperty(pentahoJcrConstants.getPHO_VERSIONMESSAGE(), versionMessage);
      } else {
        versionableNode.setProperty(pentahoJcrConstants.getPHO_VERSIONMESSAGE(), (String) null);
      }
      session.save(); // required before checkin since we set some properties above
      versionableNode.checkin();
      // Version newVersion = versionableNode.checkin();
      // if (versionMessageAndLabel.length > 1 && StringUtils.hasText(versionMessageAndLabel[1])) {
      //   newVersion.getContainingHistory().addVersionLabel(newVersion.getName(), versionMessageAndLabel[1], true);
      // }
    }
  }

  private static String getUsername() {
    IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
    Assert.state(pentahoSession != null);
    return pentahoSession.getName();
  }

  /**
   * Returns the nearest versionable node (possibly the node itself) or {@code null} if the root is reached.
   */
  private static Node findNearestVersionableNode(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Node node) throws RepositoryException {
    Node currentNode = node;
    while (!currentNode.isNodeType(pentahoJcrConstants.getPHO_MIX_VERSIONABLE())) {
      try {
        currentNode = currentNode.getParent();
      } catch (ItemNotFoundException e) {
        // at the root
        return null;
      }
    }
    return currentNode;
  }

  public static void deleteFile(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Serializable fileId, final ILockHelper lockTokenHelper) throws RepositoryException {
    Node fileNode = session.getNodeByUUID(fileId.toString());
    // guard against using a file retrieved from a more lenient session inside a more strict session
    Assert.notNull(fileNode);
    // technically, the node can be locked when it is deleted; however, we want to avoid an orphaned lock token; delete
    // it first
    if (fileNode.isLocked()) {
      Lock lock = fileNode.getLock();
      // don't need lock token anymore
      lockTokenHelper.removeLockToken(session, pentahoJcrConstants, lock);
    }
    fileNode.remove();
  }

  public static Object nodeIdToFile(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final IOwnerLookupHelper ownerLookupHelper, final IPathConversionHelper pathConversionHelper,
      final Serializable fileId) throws RepositoryException {
    Node fileNode = session.getNodeByUUID(fileId.toString());
    return nodeToFile(session, pentahoJcrConstants, ownerLookupHelper, pathConversionHelper, fileNode);
  }

  public static Object getVersionSummaries(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Serializable fileId) throws RepositoryException {
    Node fileNode = session.getNodeByUUID(fileId.toString());
    VersionHistory versionHistory = fileNode.getVersionHistory();
    // get root version but don't include it in version summaries; from JSR-170 specification section 8.2.5:
    // [root version] is a dummy version that serves as the starting point of the version graph. Like all version nodes, 
    // it has a subnode called jcr:frozenNode. But, in this case that frozen node does not contain any state information 
    // about N
    Version version = versionHistory.getRootVersion();
    Version[] successors = version.getSuccessors();
    List<VersionSummary> versionSummaries = new ArrayList<VersionSummary>();
    while (successors != null && successors.length > 0) {
      version = successors[0]; // branching not supported
      versionSummaries.add(toVersionSummary(pentahoJcrConstants, versionHistory, version));
      successors = version.getSuccessors();
    }
    return versionSummaries;
  }

  private static VersionSummary toVersionSummary(final PentahoJcrConstants pentahoJcrConstants,
      final VersionHistory versionHistory, final Version version) throws RepositoryException {
    List<String> labels = Arrays.asList(versionHistory.getVersionLabels(version));
    // get custom Pentaho properties (i.e. author and message)
    Node nodeAtVersion = getNodeAtVersion(pentahoJcrConstants, version);
    String author = nodeAtVersion.getProperty(pentahoJcrConstants.getPHO_VERSIONAUTHOR()).getString();
    String message = null;
    if (nodeAtVersion.hasProperty(pentahoJcrConstants.getPHO_VERSIONMESSAGE())) {
      message = nodeAtVersion.getProperty(pentahoJcrConstants.getPHO_VERSIONMESSAGE()).getString();
    }
    return new VersionSummary(version.getName(), versionHistory.getVersionableUUID(), version.getCreated().getTime(),
        author, message, labels);
  }

  /**
   * Returns the node as it was at the given version.
   
   * @param version version to get
   * @return node at version
   */
  private static Node getNodeAtVersion(final PentahoJcrConstants pentahoJcrConstants, final Version version)
      throws RepositoryException {
    return version.getNode(pentahoJcrConstants.getJCR_FROZENNODE());
  }

  public static RepositoryFile getFileAtVersion(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final IOwnerLookupHelper ownerLookupHelper, final IPathConversionHelper pathConversionHelper,
      final Serializable fileId, final Serializable versionId) throws RepositoryException {
    Node fileNode = session.getNodeByUUID(fileId.toString());
    Version version = fileNode.getVersionHistory().getVersion(versionId.toString());
    return nodeToFile(session, pentahoJcrConstants, ownerLookupHelper, pathConversionHelper,
        getNodeAtVersion(pentahoJcrConstants, version));
  }

  /**
   * Returns the metadata regarding that identifies what transformer wrote this file's data.
   */
  public static String getFileContentType(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Serializable fileId, final Serializable versionId) throws RepositoryException {
    Assert.notNull(fileId);
    Node fileNode = session.getNodeByUUID(fileId.toString());
    if (versionId != null) {
      Version version = fileNode.getVersionHistory().getVersion(versionId.toString());
      Node nodeAtVersion = getNodeAtVersion(pentahoJcrConstants, version);
      return nodeAtVersion.getProperty(pentahoJcrConstants.getPHO_CONTENTTYPE()).getString();
    } else {
      return fileNode.getProperty(pentahoJcrConstants.getPHO_CONTENTTYPE()).getString();
    }
  }

  public static Serializable getParentId(final Session session, final Serializable fileId) throws RepositoryException {
    Node node = session.getNodeByUUID(fileId.toString());
    return node.getParent().getUUID();
  }

  public static Serializable getBaseVersionId(final Session session, final Serializable fileId)
      throws RepositoryException {
    Node node = session.getNodeByUUID(fileId.toString());
    return node.getBaseVersion().getName();
  }

  public static Object getVersionSummary(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Serializable fileId, final Serializable versionId) throws RepositoryException {
    Node fileNode = session.getNodeByUUID(fileId.toString());
    VersionHistory versionHistory = fileNode.getVersionHistory();
    Version version = null;
    if (versionId != null) {
      version = versionHistory.getVersion(versionId.toString());
    } else {
      version = fileNode.getBaseVersion();
    }
    return toVersionSummary(pentahoJcrConstants, versionHistory, version);
  }

  public static RepositoryFileTree getTree(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final IOwnerLookupHelper ownerLookupHelper, final IPathConversionHelper pathConversionHelper,
      final String absPath, final int depth, final String filter) throws RepositoryException {

    Item fileItem = session.getItem(absPath);
    // items are nodes or properties; this must be a node
    Assert.isTrue(fileItem.isNode());
    Node fileNode = (Node) fileItem;

    RepositoryFile rootFile = JcrRepositoryFileUtils.nodeToFile(session, pentahoJcrConstants, ownerLookupHelper,
        pathConversionHelper, fileNode, false);

    List<RepositoryFileTree> children;
    // if depth is neither negative (indicating unlimited depth) nor positive (indicating at least one more level to go)
    if (depth != 0) {
      children = new ArrayList<RepositoryFileTree>();
      if (isPentahoFolder(pentahoJcrConstants, fileNode)) {
        NodeIterator childNodes = filter != null ? fileNode.getNodes(filter) : fileNode.getNodes();
        while (childNodes.hasNext()) {
          Node childNode = childNodes.nextNode();
          if (isSupportedNodeType(pentahoJcrConstants, childNode)) {
            children.add(getTree(session, pentahoJcrConstants, ownerLookupHelper, pathConversionHelper,
                childNode.getPath(), depth - 1, filter));
          }
        }
      }
      Collections.sort(children);
    } else {
      children = null;
    }
    return new RepositoryFileTree(rootFile, children);
  }
  
  
  public static void setFileMetadata(final Session session, final Serializable fileId, Map<String, Serializable> metadataMap) throws ItemNotFoundException, RepositoryException {
    PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);

    Node fileNode = session.getNodeByUUID(fileId.toString());
    String prefix = session.getNamespacePrefix(PentahoJcrConstants.PHO_NS);
    Assert.hasText(prefix);
    Node metadataNode = fileNode.getNode(pentahoJcrConstants.getPHO_METADATA());
    checkoutNearestVersionableNodeIfNecessary(session, pentahoJcrConstants, metadataNode);
    
    PropertyIterator propertyIter = metadataNode.getProperties(prefix + ":*");
    while (propertyIter.hasNext()) {
      propertyIter.nextProperty().remove();
    }

    for (String key : metadataMap.keySet()) {
      setMetadataItemForFile(session, key, metadataMap.get(key), metadataNode);
    }
    
    checkinNearestVersionableNodeIfNecessary(session, pentahoJcrConstants, metadataNode, null);
  }
    
  private static void setMetadataItemForFile(final Session session, final String metadataKey, final Serializable metadataObj, final Node metadataNode) throws ItemNotFoundException, RepositoryException {
    Assert.notNull(metadataNode);
    String prefix = session.getNamespacePrefix(PentahoJcrConstants.PHO_NS);
    Assert.hasText(prefix);
    if (metadataObj instanceof String) {
      metadataNode.setProperty(prefix + ":" + metadataKey, (String)metadataObj); //$NON-NLS-1$
    } else if (metadataObj instanceof Calendar) {
      metadataNode.setProperty(prefix + ":" + metadataKey, (Calendar)metadataObj); //$NON-NLS-1$      
    } else if (metadataObj instanceof Double) {
      metadataNode.setProperty(prefix + ":" + metadataKey, (Double)metadataObj); //$NON-NLS-1$
    } else if (metadataObj instanceof Long) {
      metadataNode.setProperty(prefix + ":" + metadataKey, (Long)metadataObj); //$NON-NLS-1$
    } else if (metadataObj instanceof Boolean) {
      metadataNode.setProperty(prefix + ":" + metadataKey, (Boolean)metadataObj); //$NON-NLS-1$
    }
  }

  public static Map<String, Serializable> getFileMetadata(final Session session, final Serializable fileId) throws ItemNotFoundException, RepositoryException {
    Map<String, Serializable> values = new HashMap<String, Serializable>();
    String prefix = session.getNamespacePrefix(PentahoJcrConstants.PHO_NS);
    Node fileNode = session.getNodeByUUID(fileId.toString());
    PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
    String metadataNodeName = pentahoJcrConstants.getPHO_METADATA();
    Node metadataNode = null;
    try {
      metadataNode = fileNode.getNode(metadataNodeName);
    } catch (PathNotFoundException pathNotFound) {  // No meta on this return an empty Map
      return values;
    }
    PropertyIterator iter = metadataNode.getProperties(prefix + ":*");
    while( iter.hasNext()) {
      Property property = iter.nextProperty();
      String key = property.getName().substring(property.getName().indexOf(':') + 1);
      Serializable value = null;
      switch( property.getType() ) {
        case PropertyType.STRING:
          value = property.getString();
          break;
        case PropertyType.DATE:
          value = property.getDate();
          break;
        case PropertyType.DOUBLE:
          value = property.getDouble();
          break;
        case PropertyType.LONG:
          value = property.getLong();
          break;
        case PropertyType.BOOLEAN:
          value = property.getBoolean();
          break;
      }
      if (value != null) {
        values.put(key, value);
      }
    }

    return values;
  }
}