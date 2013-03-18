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
import java.util.*;
import java.util.regex.Pattern;

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
import javax.jcr.lock.Lock;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.JcrConstants;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.locale.IPentahoLocale;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.VersionSummary;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.repository2.locale.PentahoLocale;
import org.pentaho.platform.repository2.messages.Messages;
import org.pentaho.platform.repository2.unified.exception.RepositoryFileDaoMalformedNameException;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Class of static methods where the real JCR work takes place.
 * 
 * @author mlowery
 */
public class JcrRepositoryFileUtils {

  private static final Log logger = LogFactory.getLog(JcrRepositoryFileUtils.class);

  /**
   * See section 4.6 "Path Syntax" of JCR 1.0 spec. Note that this list is only characters that can never appear in a
   * "simplename". It does not include '.' because, while "." and ".." are illegal, any other string containing '.' is 
   * legal. It is up to this implementation to prohibit permutations of legal characters.
   */
  private static final List<Character> reservedChars = Collections.unmodifiableList(Arrays.asList(new Character[] {
      '/', ':', '[', ']', '*', '\'', '"', '|', '\t', '\r', '\n' }));

  private static final Pattern containsReservedCharsPattern = makePattern();

  private static Pattern makePattern() {
    // escape all reserved characters as they may have special meaning to regex engine
    StringBuilder buf = new StringBuilder();
    buf.append(".*"); //$NON-NLS-1$
    buf.append("["); //$NON-NLS-1$
    for (Character ch : reservedChars) {
      buf.append("\\"); //$NON-NLS-1$
      buf.append(ch);
    }
    buf.append("]"); //$NON-NLS-1$
    buf.append("+"); //$NON-NLS-1$
    buf.append(".*"); //$NON-NLS-1$
    return Pattern.compile(buf.toString());
  }

  public static RepositoryFile getFileById(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final IPathConversionHelper pathConversionHelper, final ILockHelper lockHelper, final Serializable fileId)
      throws RepositoryException {
    Node fileNode = session.getNodeByIdentifier(fileId.toString());
    Assert.notNull(fileNode);
    return nodeToFile(session, pentahoJcrConstants, pathConversionHelper, lockHelper, fileNode);
  }

  public static RepositoryFile nodeToFile(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final IPathConversionHelper pathConversionHelper, final ILockHelper lockHelper, final Node node)
      throws RepositoryException {
    return nodeToFile(session, pentahoJcrConstants, pathConversionHelper, lockHelper, node, false, null);
  }

  private static RepositoryFile getRootFolder(final Session session) throws RepositoryException {
    Node node = session.getRootNode();
    RepositoryFile file = new RepositoryFile.Builder(node.getIdentifier(), "").folder(true).versioned(false).path( //$NON-NLS-1$
        node.getPath()).build();
    return file;
  }

  public static RepositoryFile nodeToFile(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final IPathConversionHelper pathConversionHelper, final ILockHelper lockHelper, final Node node,
      final boolean loadMaps, IPentahoLocale pentahoLocale) throws RepositoryException {

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
    String title = null;
    String description = null;
    Map<String, String> titleMap = null;
    Map<String, String> descriptionMap = null;
    Map<String, Properties> localePropertiesMap = null;

    id = getNodeId(session, pentahoJcrConstants, node);

    if (logger.isDebugEnabled()) {
      logger.debug(String.format("reading file with id '%s' and path '%s'", id, node.getPath())); //$NON-NLS-1$
    }

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
    if (node.hasProperty(pentahoJcrConstants.getPHO_HIDDEN())) {
      hidden = node.getProperty(pentahoJcrConstants.getPHO_HIDDEN()).getBoolean();
    }
    if (node.hasProperty(pentahoJcrConstants.getPHO_FILESIZE())) {
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
        title = getLocalizedString(session, pentahoJcrConstants, node.getNode(pentahoJcrConstants.getPHO_TITLE()),
            pentahoLocale);
      }
      if (node.hasNode(pentahoJcrConstants.getPHO_DESCRIPTION())) {
        description = getLocalizedString(session, pentahoJcrConstants,
            node.getNode(pentahoJcrConstants.getPHO_DESCRIPTION()), pentahoLocale);
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

        if (node.hasNode(pentahoJcrConstants.getPHO_LOCALES())) {
          localePropertiesMap = getLocalePropertiesMap(session, pentahoJcrConstants,
             node.getNode(pentahoJcrConstants.getPHO_LOCALES()));
        }
      }
    }

    // Get default locale if null
    if (pentahoLocale == null) {
      pentahoLocale = new PentahoLocale();
    }

    versioned = isVersioned(session, pentahoJcrConstants, node);
    if (versioned) {
      versionId = getVersionId(session, pentahoJcrConstants, node);
    }

    locked = isLocked(pentahoJcrConstants, node);
    if (locked) {
      Lock lock = session.getWorkspace().getLockManager().getLock(node.getPath());
      lockOwner = lockHelper.getLockOwner(session, pentahoJcrConstants, lock);
      lockDate = lockHelper.getLockDate(session, pentahoJcrConstants, lock);
      lockMessage = lockHelper.getLockMessage(session, pentahoJcrConstants, lock);
    }

    RepositoryFile file = new RepositoryFile.Builder(id, name).createdDate(created).creatorId(creatorId)
        .lastModificationDate(lastModified).folder(folder).versioned(versioned).path(path).versionId(versionId)
        .fileSize(fileSize).locked(locked).lockDate(lockDate).hidden(hidden).lockMessage(lockMessage)
        .lockOwner(lockOwner).title(title).description(description).titleMap(titleMap).descriptionMap(descriptionMap)
        .locale(pentahoLocale.toString()).localePropertiesMap(localePropertiesMap).build();

    return file;
  }

  private static String getLocalizedString(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Node localizedStringNode, IPentahoLocale pentahoLocale) throws RepositoryException {
    Assert.isTrue(isLocalizedString(session, pentahoJcrConstants, localizedStringNode));

    boolean isLocaleNull = pentahoLocale == null;

    if (pentahoLocale == null) {
      pentahoLocale = new PentahoLocale();
    }

    Locale locale = pentahoLocale.getLocale();

    final String UNDERSCORE = "_"; //$NON-NLS-1$
    final String COLON = ":"; //$NON-NLS-1$
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

    String prefix = session.getNamespacePrefix(PentahoJcrConstants.PHO_NS);
    Assert.hasText(prefix);

    String propertyStr = isLocaleNull ? pentahoJcrConstants.getPHO_ROOTLOCALE() : prefix + COLON + locale.getLanguage();

    return localizedStringNode.getProperty(propertyStr).getString();
  }

  private static Map<String, Properties> getLocalePropertiesMap(
     final Session session, final PentahoJcrConstants pentahoJcrConstants,
     final Node localeNode) throws RepositoryException {

    String prefix = session.getNamespacePrefix(PentahoJcrConstants.PHO_NS);
    Assert.hasText(prefix);

    Map<String, Properties> localePropertiesMap = new HashMap<String, Properties>();

    NodeIterator nodeItr = localeNode.getNodes();
    while(nodeItr.hasNext()){
      Node node = nodeItr.nextNode();

      String locale = node.getName();
      Properties properties = new Properties();
      PropertyIterator propertyIterator = node.getProperties();
      while(propertyIterator.hasNext()){
        Property property = propertyIterator.nextProperty();
        properties.put(property.getName(), property.getValue().getString());
      }

      localePropertiesMap.put(locale, properties);
    }
    return localePropertiesMap;
  }

  private static void setLocalePropertiesMap(final Session session, final PentahoJcrConstants pentahoJcrConstants,
                                             final Node localeRootNode, final Map<String, Properties> localePropertiesMap) throws RepositoryException {
    String prefix = session.getNamespacePrefix(PentahoJcrConstants.PHO_NS);
    Assert.hasText(prefix);

    if(localePropertiesMap != null && !localePropertiesMap.isEmpty()){
      for(String locale : localePropertiesMap.keySet()){
        Properties properties = localePropertiesMap.get(locale);
        if(properties != null){
          // create node and set properties for each locale
          Node localeNode = localeRootNode.addNode(locale, pentahoJcrConstants.getNT_UNSTRUCTURED());
          for(String propertyName : properties.stringPropertyNames()){
            localeNode.setProperty(propertyName, properties.getProperty(propertyName));
          }
        }
      }
    }
  }

  private static Map<String, String> getLocalizedStringMap(final Session session,
      final PentahoJcrConstants pentahoJcrConstants, final Node localizedStringNode) throws RepositoryException {
    Assert.isTrue(isLocalizedString(session, pentahoJcrConstants, localizedStringNode));

    String prefix = session.getNamespacePrefix(PentahoJcrConstants.PHO_NS);
    Assert.hasText(prefix);

    Map<String, String> localizedStringMap = new HashMap<String, String>();
    PropertyIterator propertyIter = localizedStringNode.getProperties();

    // Loop through properties and append the appropriate values in the map
    while (propertyIter.hasNext()) {
      Property property = propertyIter.nextProperty();
      String propertyKey = property.getName().substring(prefix.length() + 1);

      localizedStringMap.put(propertyKey, property.getString());
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

  private static String getAbsolutePath(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Node node) throws RepositoryException {
    if (node.isNodeType(pentahoJcrConstants.getNT_FROZENNODE())) {
      return session.getNodeByIdentifier(node.getProperty(pentahoJcrConstants.getJCR_FROZENUUID()).getString())
          .getPath();
    }

    return node.getPath();
  }

  private static Serializable getNodeId(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Node node) throws RepositoryException {
    if (node.isNodeType(pentahoJcrConstants.getNT_FROZENNODE())) {
      return node.getProperty(pentahoJcrConstants.getJCR_FROZENUUID()).getString();
    }

    return node.getIdentifier();
  }

  private static String getNodeName(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Node node) throws RepositoryException {
    if (node.isNodeType(pentahoJcrConstants.getNT_FROZENNODE())) {
      return session.getNodeByIdentifier(node.getProperty(pentahoJcrConstants.getJCR_FROZENUUID()).getString())
          .getName();
    }

    return node.getName();
  }

  private static String getVersionId(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Node node) throws RepositoryException {
    if (node.isNodeType(pentahoJcrConstants.getNT_FROZENNODE())) {
      return node.getParent().getName();
    }

    return session.getWorkspace().getVersionManager().getBaseVersion(node.getPath()).getName();
  }

  public static Node createFolderNode(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Serializable parentFolderId, final RepositoryFile folder) throws RepositoryException {
    checkName(folder.getName());
    Node parentFolderNode;
    if (parentFolderId != null) {
      parentFolderNode = session.getNodeByIdentifier(parentFolderId.toString());
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
      final Serializable parentFolderId, final RepositoryFile file, final IRepositoryFileData content,
      final ITransformer<IRepositoryFileData> transformer) throws RepositoryException {

    checkName(file.getName());
    Node parentFolderNode;
    if (parentFolderId != null) {
      parentFolderNode = session.getNodeByIdentifier(parentFolderId.toString());
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
    if (file.getLocalePropertiesMap() != null && !file.getLocalePropertiesMap().isEmpty()) {
      Node localeNodes = fileNode.addNode(pentahoJcrConstants.getPHO_LOCALES(), pentahoJcrConstants.getPHO_NT_LOCALE());
      setLocalePropertiesMap(session, pentahoJcrConstants, localeNodes, file.getLocalePropertiesMap());
    }
    Node metaNode = fileNode.addNode(pentahoJcrConstants.getPHO_METADATA(), JcrConstants.NT_UNSTRUCTURED);
    setMetadataItemForFile(session, PentahoJcrConstants.PHO_CONTENTCREATOR, file.getCreatorId(), metaNode);
    fileNode.addMixin(pentahoJcrConstants.getMIX_LOCKABLE());
    fileNode.addMixin(pentahoJcrConstants.getMIX_REFERENCEABLE());

    if (file.isVersioned()) {
      //      fileNode.setProperty(addPentahoPrefix(session, PentahoJcrConstants.PENTAHO_VERSIONED), true);
      fileNode.addMixin(pentahoJcrConstants.getPHO_MIX_VERSIONABLE());
    }

    transformer.createContentNode(session, pentahoJcrConstants, content, fileNode);
    return fileNode;
  }

  private static void preventLostUpdate(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final RepositoryFile file) throws RepositoryException {
    Node fileNode = session.getNodeByIdentifier(file.getId().toString());
    // guard against using a file retrieved from a more lenient session inside a more strict session
    Assert.notNull(fileNode);
    if (isVersioned(session, pentahoJcrConstants, fileNode)) {
      Assert.notNull(file.getVersionId(), "updating a versioned file requires a non-null version id"); //$NON-NLS-1$
      Assert.state(
          session.getWorkspace().getVersionManager().getBaseVersion(fileNode.getPath()).getName()
              .equals(file.getVersionId().toString()), "update to this file has occurred since its last read"); //$NON-NLS-1$
    }
  }

  public static Node updateFileNode(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final RepositoryFile file, final IRepositoryFileData content, final ITransformer<IRepositoryFileData> transformer)
      throws RepositoryException {

    Node fileNode = session.getNodeByIdentifier(file.getId().toString());
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
    if (file.getLocalePropertiesMap() != null && !file.getLocalePropertiesMap().isEmpty()) {
      Node localePropertiesMapNode = null;
      if (!fileNode.hasNode(pentahoJcrConstants.getPHO_LOCALES())) {
        localePropertiesMapNode = fileNode.addNode(pentahoJcrConstants.getPHO_LOCALES(),
           pentahoJcrConstants.getPHO_NT_LOCALE());
      } else {
        localePropertiesMapNode = fileNode.getNode(pentahoJcrConstants.getPHO_LOCALES());
      }
      setLocalePropertiesMap(session, pentahoJcrConstants, localePropertiesMapNode, file.getLocalePropertiesMap());
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
    transformer.updateContentNode(session, pentahoJcrConstants, content, fileNode);
    return fileNode;
  }

  public static IRepositoryFileData getContent(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Serializable fileId, final Serializable versionId, final ITransformer<IRepositoryFileData> transformer)
      throws RepositoryException {
    Node fileNode = session.getNodeByIdentifier(fileId.toString());
    if (isVersioned(session, pentahoJcrConstants, fileNode)) {
      VersionManager vMgr = session.getWorkspace().getVersionManager();
      Version version = null;
      if (versionId != null) {
        version = vMgr.getVersionHistory(fileNode.getPath()).getVersion(versionId.toString());
      } else {
        version = vMgr.getBaseVersion(fileNode.getPath());
      }
      fileNode = getNodeAtVersion(pentahoJcrConstants, version);
    }
    Assert.isTrue(!isPentahoFolder(pentahoJcrConstants, fileNode));

    return transformer.fromContentNode(session, pentahoJcrConstants, fileNode);
  }

  public static List<RepositoryFile> getChildren(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final IPathConversionHelper pathConversionHelper, final ILockHelper lockHelper, final Serializable folderId,
      final String filter) throws RepositoryException {
    Node folderNode = session.getNodeByIdentifier(folderId.toString());
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
        children.add(nodeToFile(session, pentahoJcrConstants, pathConversionHelper, lockHelper, node));
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
    }

    return node.isNodeType(pentahoJcrConstants.getPHO_NT_PENTAHOFOLDER());
  }

  private static boolean isPentahoHierarchyNode(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Node node) throws RepositoryException {
    Assert.notNull(node);
    if (node.isNodeType(pentahoJcrConstants.getNT_FROZENNODE())) {
      String nodeTypeName = node.getProperty(pentahoJcrConstants.getJCR_FROZENPRIMARYTYPE()).getString();
      // TODO mlowery add PENTAHOLINKEDFILE here when it is available
      return pentahoJcrConstants.getPHO_NT_PENTAHOFOLDER().equals(nodeTypeName)
          || pentahoJcrConstants.getPHO_NT_PENTAHOFILE().equals(nodeTypeName);
    }

    return node.isNodeType(pentahoJcrConstants.getPHO_NT_PENTAHOHIERARCHYNODE());
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
      Assert.isTrue(node.isNodeType(pentahoJcrConstants.getMIX_LOCKABLE()));
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
    }

    return node.isNodeType(pentahoJcrConstants.getPHO_NT_PENTAHOFILE());
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
    }

    return node.isNodeType(pentahoJcrConstants.getPHO_NT_LOCALIZEDSTRING());
  }

  private static boolean isVersioned(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Node node) throws RepositoryException {
    Assert.notNull(node);
    if (node.isNodeType(pentahoJcrConstants.getNT_FROZENNODE())) {
      // frozen nodes represent the nodes at a particular version; so yes, they are versioned!
      return true;
    }

    return node.isNodeType(pentahoJcrConstants.getPHO_MIX_VERSIONABLE());
  }

  public static boolean isSupportedNodeType(final PentahoJcrConstants pentahoJcrConstants, final Node node)
      throws RepositoryException {
    Assert.notNull(node);
    if (node.isNodeType(pentahoJcrConstants.getNT_FROZENNODE())) {
      String nodeTypeName = node.getProperty(pentahoJcrConstants.getJCR_FROZENPRIMARYTYPE()).getString();
      return pentahoJcrConstants.getPHO_NT_PENTAHOFILE().equals(nodeTypeName)
          || pentahoJcrConstants.getPHO_NT_PENTAHOFOLDER().equals(nodeTypeName);
    }

    return node.isNodeType(pentahoJcrConstants.getPHO_NT_PENTAHOFILE())
        || node.isNodeType(pentahoJcrConstants.getPHO_NT_PENTAHOFOLDER());
  }

  /**
   * Conditionally checks out node representing file if node is versionable.
   */
  public static void checkoutNearestVersionableFileIfNecessary(final Session session,
      final PentahoJcrConstants pentahoJcrConstants, final Serializable fileId) throws RepositoryException {
    // file could be null meaning the caller is using null as the parent folder; that's OK; in this case the node in
    // question would be the repository root node and that is never versioned
    if (fileId != null) {
      Node node = session.getNodeByIdentifier(fileId.toString());
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
      session.getWorkspace().getVersionManager().checkout(versionableNode.getPath());
    }
  }

  public static void checkinNearestVersionableFileIfNecessary(final Session session,
      final PentahoJcrConstants pentahoJcrConstants, final Serializable fileId, final String versionMessage)
      throws RepositoryException {
    checkinNearestVersionableFileIfNecessary(session, pentahoJcrConstants, fileId, versionMessage, false);
  }

  /**
   * Conditionally checks in node representing file if node is versionable.
   */
  public static void checkinNearestVersionableFileIfNecessary(final Session session,
      final PentahoJcrConstants pentahoJcrConstants, final Serializable fileId, final String versionMessage,
      final boolean aclOnlyChange) throws RepositoryException {
    // file could be null meaning the caller is using null as the parent folder; that's OK; in this case the node in
    // question would be the repository root node and that is never versioned
    if (fileId != null) {
      Node node = session.getNodeByIdentifier(fileId.toString());
      checkinNearestVersionableNodeIfNecessary(session, pentahoJcrConstants, node, versionMessage, aclOnlyChange);
    }
  }

  public static void checkinNearestVersionableNodeIfNecessary(final Session session,
      final PentahoJcrConstants pentahoJcrConstants, final Node node, final String versionMessage)
      throws RepositoryException {
    checkinNearestVersionableNodeIfNecessary(session, pentahoJcrConstants, node, versionMessage, false);
  }

  /**
   * Conditionally checks in node if node is versionable.
   * 
   * TODO mlowery move commented out version labeling to its own method
   */
  public static void checkinNearestVersionableNodeIfNecessary(final Session session,
      final PentahoJcrConstants pentahoJcrConstants, final Node node, final String versionMessage,
      final boolean aclOnlyChange) throws RepositoryException {
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
        // TODO mlowery why do I need to check for hasProperty here? in JR 1.6, I didn't need to
        if (versionableNode.hasProperty(pentahoJcrConstants.getPHO_VERSIONMESSAGE())) {
          versionableNode.setProperty(pentahoJcrConstants.getPHO_VERSIONMESSAGE(), (String) null);
        }
      }
      if (aclOnlyChange) {
        versionableNode.setProperty(pentahoJcrConstants.getPHO_ACLONLYCHANGE(), true);
      } else {
        // TODO mlowery why do I need to check for hasProperty here? in JR 1.6, I didn't need to
        if (versionableNode.hasProperty(pentahoJcrConstants.getPHO_ACLONLYCHANGE())) {
          versionableNode.getProperty(pentahoJcrConstants.getPHO_ACLONLYCHANGE()).remove();
        }
      }
      session.save(); // required before checkin since we set some properties above
      session.getWorkspace().getVersionManager().checkin(versionableNode.getPath());
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
    Node fileNode = session.getNodeByIdentifier(fileId.toString());
    // guard against using a file retrieved from a more lenient session inside a more strict session
    Assert.notNull(fileNode);
    // technically, the node can be locked when it is deleted; however, we want to avoid an orphaned lock token; delete
    // it first
    if (fileNode.isLocked()) {
      Lock lock = session.getWorkspace().getLockManager().getLock(fileNode.getPath());
      // don't need lock token anymore
      lockTokenHelper.removeLockToken(session, pentahoJcrConstants, lock);
    }
    fileNode.remove();
  }

  public static RepositoryFile nodeIdToFile(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final IPathConversionHelper pathConversionHelper, final ILockHelper lockHelper, final Serializable fileId)
      throws RepositoryException {
    Node fileNode = session.getNodeByIdentifier(fileId.toString());
    return nodeToFile(session, pentahoJcrConstants, pathConversionHelper, lockHelper, fileNode);
  }

  public static Object getVersionSummaries(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Serializable fileId, final boolean includeAclOnlyChanges) throws RepositoryException {
    Node fileNode = session.getNodeByIdentifier(fileId.toString());
    VersionHistory versionHistory = session.getWorkspace().getVersionManager().getVersionHistory(fileNode.getPath());
    // get root version but don't include it in version summaries; from JSR-170 specification section 8.2.5:
    // [root version] is a dummy version that serves as the starting point of the version graph. Like all version nodes, 
    // it has a subnode called jcr:frozenNode. But, in this case that frozen node does not contain any state information 
    // about N
    Version version = versionHistory.getRootVersion();
    Version[] successors = version.getSuccessors();
    List<VersionSummary> versionSummaries = new ArrayList<VersionSummary>();
    while (successors != null && successors.length > 0) {
      version = successors[0]; // branching not supported
      VersionSummary sum = toVersionSummary(pentahoJcrConstants, versionHistory, version);
      if (!sum.isAclOnlyChange() || (includeAclOnlyChanges && sum.isAclOnlyChange())) {
        versionSummaries.add(sum);
      }
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
    boolean aclOnlyChange = false;
    if (nodeAtVersion.hasProperty(pentahoJcrConstants.getPHO_ACLONLYCHANGE())
        && nodeAtVersion.getProperty(pentahoJcrConstants.getPHO_ACLONLYCHANGE()).getBoolean()) {
      aclOnlyChange = true;
    }
    return new VersionSummary(version.getName(), versionHistory.getVersionableIdentifier(), aclOnlyChange, version
        .getCreated().getTime(), author, message, labels);
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
      final IPathConversionHelper pathConversionHelper, final ILockHelper lockHelper, final Serializable fileId,
      final Serializable versionId) throws RepositoryException {
    Node fileNode = session.getNodeByIdentifier(fileId.toString());
    Version version = session.getWorkspace().getVersionManager().getVersionHistory(fileNode.getPath())
        .getVersion(versionId.toString());
    return nodeToFile(session, pentahoJcrConstants, pathConversionHelper, lockHelper,
        getNodeAtVersion(pentahoJcrConstants, version));
  }

  /**
   * Returns the metadata regarding that identifies what transformer wrote this file's data.
   */
  public static String getFileContentType(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Serializable fileId, final Serializable versionId) throws RepositoryException {
    Assert.notNull(fileId);
    Node fileNode = session.getNodeByIdentifier(fileId.toString());
    if (versionId != null) {
      Version version = session.getWorkspace().getVersionManager().getVersionHistory(fileNode.getPath())
          .getVersion(versionId.toString());
      Node nodeAtVersion = getNodeAtVersion(pentahoJcrConstants, version);
      return nodeAtVersion.getProperty(pentahoJcrConstants.getPHO_CONTENTTYPE()).getString();
    }

    return fileNode.getProperty(pentahoJcrConstants.getPHO_CONTENTTYPE()).getString();
  }

  public static Serializable getParentId(final Session session, final Serializable fileId) throws RepositoryException {
    Node node = session.getNodeByIdentifier(fileId.toString());
    return node.getParent().getIdentifier();
  }

  public static Serializable getBaseVersionId(final Session session, final Serializable fileId)
      throws RepositoryException {
    Node node = session.getNodeByIdentifier(fileId.toString());
    return session.getWorkspace().getVersionManager().getBaseVersion(node.getPath()).getName();
  }

  public static Object getVersionSummary(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Serializable fileId, final Serializable versionId) throws RepositoryException {
    VersionManager vMgr = session.getWorkspace().getVersionManager();
    Node fileNode = session.getNodeByIdentifier(fileId.toString());
    VersionHistory versionHistory = vMgr.getVersionHistory(fileNode.getPath());
    Version version = null;
    if (versionId != null) {
      version = versionHistory.getVersion(versionId.toString());
    } else {
      version = vMgr.getBaseVersion(fileNode.getPath());
    }
    return toVersionSummary(pentahoJcrConstants, versionHistory, version);
  }

  public static RepositoryFileTree getTree(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final IPathConversionHelper pathConversionHelper, final ILockHelper lockHelper, final String absPath,
      final int depth, final String filter, final boolean showHidden) throws RepositoryException {

    Item fileItem = session.getItem(absPath);
    // items are nodes or properties; this must be a node
    Assert.isTrue(fileItem.isNode());
    Node fileNode = (Node) fileItem;

    RepositoryFile rootFile = JcrRepositoryFileUtils.nodeToFile(session, pentahoJcrConstants, pathConversionHelper,
        lockHelper, fileNode, false, null);
    if (!showHidden && rootFile.isHidden()) {
      return null;
    }
    List<RepositoryFileTree> children;
    // if depth is neither negative (indicating unlimited depth) nor positive (indicating at least one more level to go)
    if (depth != 0) {
      children = new ArrayList<RepositoryFileTree>();
      if (isPentahoFolder(pentahoJcrConstants, fileNode)) {
        NodeIterator childNodes = filter != null ? fileNode.getNodes(filter) : fileNode.getNodes();
        while (childNodes.hasNext()) {
          Node childNode = childNodes.nextNode();
          if (isSupportedNodeType(pentahoJcrConstants, childNode)) {
            RepositoryFileTree repositoryFileTree = getTree(session, pentahoJcrConstants, pathConversionHelper,
                lockHelper, childNode.getPath(), depth - 1, filter, showHidden);
            if (repositoryFileTree != null) {
              children.add(repositoryFileTree);
            }
          }
        }
      }
      Collections.sort(children);
    } else {
      children = null;
    }
    return new RepositoryFileTree(rootFile, children);
  }

  public static void setFileLocaleProperties(final Session session, final Serializable fileId,
    String locale, Properties properties) throws RepositoryException {

    PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
    Node fileNode = session.getNodeByIdentifier(fileId.toString());
    String prefix = session.getNamespacePrefix(PentahoJcrConstants.PHO_NS);
    Assert.hasText(prefix);

    Node localesNode = null;
    if (!fileNode.hasNode(pentahoJcrConstants.getPHO_LOCALES())) {
      // Auto-create pho:locales node if doesn't exist
      localesNode = fileNode.addNode(pentahoJcrConstants.getPHO_LOCALES(), pentahoJcrConstants.getPHO_NT_LOCALE());
    } else {
      localesNode = fileNode.getNode(pentahoJcrConstants.getPHO_LOCALES());
    }

    try{
      Node localeNode = localesNode.getNode(locale);
      for(String propertyName : properties.stringPropertyNames()){
        localeNode.setProperty(propertyName, properties.getProperty(propertyName));
      }
    }
    catch(PathNotFoundException pnfe){
      // locale doesn't exist, create a new locale node
      Map<String, Properties> propertiesMap = new HashMap<String, Properties>();
      propertiesMap.put(locale, properties);
      setLocalePropertiesMap(session, pentahoJcrConstants, localesNode, propertiesMap);
    }

    checkinNearestVersionableNodeIfNecessary(session, pentahoJcrConstants, localesNode, null);
  }

  public static void deleteFileLocaleProperties(final Session session, final Serializable fileId,
    String locale) throws RepositoryException {

    PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
    Node fileNode = session.getNodeByIdentifier(fileId.toString());
    String prefix = session.getNamespacePrefix(PentahoJcrConstants.PHO_NS);
    Assert.hasText(prefix);

    Node localesNode = fileNode.getNode(pentahoJcrConstants.getPHO_LOCALES());
    Assert.notNull(localesNode);

    try{
      // remove locale node
      Node localeNode = localesNode.getNode(locale);
      localeNode.remove();
    }
    catch(PathNotFoundException pnfe){
      // nothing to delete
    }

    checkinNearestVersionableNodeIfNecessary(session, pentahoJcrConstants, localesNode, null);
  }


  public static void setFileMetadata(final Session session, final Serializable fileId,
      Map<String, Serializable> metadataMap) throws ItemNotFoundException, RepositoryException {
    PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);

    Node fileNode = session.getNodeByIdentifier(fileId.toString());
    String prefix = session.getNamespacePrefix(PentahoJcrConstants.PHO_NS);
    Assert.hasText(prefix);
    Node metadataNode = fileNode.getNode(pentahoJcrConstants.getPHO_METADATA());
    checkoutNearestVersionableNodeIfNecessary(session, pentahoJcrConstants, metadataNode);

    PropertyIterator propertyIter = metadataNode.getProperties(prefix + ":*"); //$NON-NLS-1$
    while (propertyIter.hasNext()) {
      propertyIter.nextProperty().remove();
    }

    for (String key : metadataMap.keySet()) {
      setMetadataItemForFile(session, key, metadataMap.get(key), metadataNode);
    }

    checkinNearestVersionableNodeIfNecessary(session, pentahoJcrConstants, metadataNode, null);
  }

  private static void setMetadataItemForFile(final Session session, final String metadataKey,
      final Serializable metadataObj, final Node metadataNode) throws ItemNotFoundException, RepositoryException {
    checkName(metadataKey);
    Assert.notNull(metadataNode);
    String prefix = session.getNamespacePrefix(PentahoJcrConstants.PHO_NS);
    Assert.hasText(prefix);
    if (metadataObj instanceof String) {
      metadataNode.setProperty(prefix + ":" + metadataKey, (String) metadataObj); //$NON-NLS-1$
    } else if (metadataObj instanceof Calendar) {
      metadataNode.setProperty(prefix + ":" + metadataKey, (Calendar) metadataObj); //$NON-NLS-1$      
    } else if (metadataObj instanceof Double) {
      metadataNode.setProperty(prefix + ":" + metadataKey, (Double) metadataObj); //$NON-NLS-1$
    } else if (metadataObj instanceof Long) {
      metadataNode.setProperty(prefix + ":" + metadataKey, (Long) metadataObj); //$NON-NLS-1$
    } else if (metadataObj instanceof Boolean) {
      metadataNode.setProperty(prefix + ":" + metadataKey, (Boolean) metadataObj); //$NON-NLS-1$
    }
  }

  public static Map<String, Serializable> getFileMetadata(final Session session, final Serializable fileId)
      throws ItemNotFoundException, RepositoryException {
    Map<String, Serializable> values = new HashMap<String, Serializable>();
    String prefix = session.getNamespacePrefix(PentahoJcrConstants.PHO_NS);
    Node fileNode = session.getNodeByIdentifier(fileId.toString());
    PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
    String metadataNodeName = pentahoJcrConstants.getPHO_METADATA();
    Node metadataNode = null;
    try {
      metadataNode = fileNode.getNode(metadataNodeName);
    } catch (PathNotFoundException pathNotFound) { // No meta on this return an empty Map
      return values;
    }
    PropertyIterator iter = metadataNode.getProperties(prefix + ":*"); //$NON-NLS-1$
    while (iter.hasNext()) {
      Property property = iter.nextProperty();
      String key = property.getName().substring(property.getName().indexOf(':') + 1);
      Serializable value = null;
      switch (property.getType()) {
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

  public static List<Character> getReservedChars() {
    return reservedChars;
  }

  /**
   * Checks for presence of reserved chars as well as illegal permutations of legal chars.
   */
  public static void checkName(final String name) {
    if (!StringUtils.hasLength(name) || // not null, not empty, and not all whitespace
        !name.trim().equals(name) || // no leading or trailing whitespace
        containsReservedCharsPattern.matcher(name).matches() || // no reserved characters
        ".".equals(name) || // no . //$NON-NLS-1$
        "..".equals(name)) { // no .. //$NON-NLS-1$
      throw new RepositoryFileDaoMalformedNameException(name);
    }
  }

  public static RepositoryFile createFolder(final Session session, final RepositoryFile parentFolder,
      final RepositoryFile folder, final boolean inheritAces, final RepositoryFileSid ownerSid,
      final IPathConversionHelper pathConversionHelper, final String versionMessage) throws RepositoryException {
    Serializable parentFolderId = parentFolder == null ? null : parentFolder.getId();
    PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
    JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary(session, pentahoJcrConstants, parentFolderId);
    Node folderNode = JcrRepositoryFileUtils.createFolderNode(session, pentahoJcrConstants, parentFolderId, folder);
    session.save();
    JcrRepositoryFileAclUtils.createAcl(session, pentahoJcrConstants, folderNode.getIdentifier(),
        new RepositoryFileAcl.Builder(ownerSid).entriesInheriting(inheritAces).build());
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
    return JcrRepositoryFileUtils.getFileById(session, pentahoJcrConstants, pathConversionHelper, null,
        folderNode.getIdentifier());
  }

  public static RepositoryFile getFileByAbsolutePath(final Session session, final String absPath,
      final IPathConversionHelper pathConversionHelper, final ILockHelper lockHelper, final boolean loadMaps,
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

}