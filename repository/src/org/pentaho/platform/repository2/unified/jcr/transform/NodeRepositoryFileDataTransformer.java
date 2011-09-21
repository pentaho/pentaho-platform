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
package org.pentaho.platform.repository2.unified.jcr.transform;

import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.DataNodeRef;
import org.pentaho.platform.api.repository2.unified.data.node.DataProperty;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.repository2.unified.jcr.IEscapeHelper;
import org.pentaho.platform.repository2.unified.jcr.ITransformer;
import org.pentaho.platform.repository2.unified.jcr.PentahoJcrConstants;
import org.springframework.util.Assert;

public class NodeRepositoryFileDataTransformer implements ITransformer<NodeRepositoryFileData> {

  // ~ Static fields/initializers ======================================================================================

  // ~ Instance fields =================================================================================================

  private static final String CONTENT_TYPE = "node"; //$NON-NLS-1$

  // ~ Constructors ====================================================================================================

  public NodeRepositoryFileDataTransformer() {
    super();
  }

  // ~ Methods =========================================================================================================

  protected void createOrUpdateContentNode(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final IEscapeHelper escapeHelper, final NodeRepositoryFileData data, final Node fileNode)
      throws RepositoryException {
    Node unstructuredNode = null;
    if (fileNode.hasNode(pentahoJcrConstants.getJCR_CONTENT())) {
      unstructuredNode = fileNode.getNode(pentahoJcrConstants.getJCR_CONTENT());
    } else {
      unstructuredNode = fileNode.addNode(pentahoJcrConstants.getJCR_CONTENT(),
          pentahoJcrConstants.getPHO_NT_INTERNALFOLDER());
    }

    // clear out all nodes since it's the quickest way to guarantee that existing nodes that should be deleted are 
    // removed
    final String pattern = session.getNamespacePrefix(PentahoJcrConstants.PHO_NS) + ":" + "*"; //$NON-NLS-1$ //$NON-NLS-2$
    NodeIterator nodes = unstructuredNode.getNodes(pattern);
    while (nodes.hasNext()) {
      nodes.nextNode().remove();
    }

    internalCreateOrUpdate(session, pentahoJcrConstants, escapeHelper, unstructuredNode, data.getNode());
  }

  public void createContentNode(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final IEscapeHelper escapeHelper, final NodeRepositoryFileData data, final Node fileNode)
      throws RepositoryException {
    createOrUpdateContentNode(session, pentahoJcrConstants, escapeHelper, data, fileNode);
  }

  protected void internalCreateOrUpdate(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final IEscapeHelper escapeHelper, final Node jcrParentNode, final DataNode dataNode) throws RepositoryException {
    final String prefix = session.getNamespacePrefix(PentahoJcrConstants.PHO_NS) + ":"; //$NON-NLS-1$
    // get or create the node represented by dataNode
    Node jcrNode = null;
    String escapedNodeName = prefix + escapeHelper.escapeIllegalJcrChars(dataNode.getName());
    
    if (jcrParentNode.hasNode(escapedNodeName)) {
      jcrNode = jcrParentNode.getNode(escapedNodeName);
    } else {
      jcrNode = jcrParentNode.addNode(escapedNodeName, pentahoJcrConstants.getPHO_NT_INTERNALFOLDER());
    }
    // set any properties represented by dataNode
    for (DataProperty dataProp : dataNode.getProperties()) {
      String escapedPropName = prefix + escapeHelper.escapeIllegalJcrChars(dataProp.getName());
      
      switch (dataProp.getType()) {
        case STRING: {
          jcrNode.setProperty(escapedPropName, dataProp.getString());
          break;
        }
        case BOOLEAN: {
          jcrNode.setProperty(escapedPropName, dataProp.getBoolean());
          break;
        }
        case DOUBLE: {
          jcrNode.setProperty(escapedPropName, dataProp.getDouble());
          break;
        }
        case LONG: {
          jcrNode.setProperty(escapedPropName, dataProp.getLong());
          break;
        }
        case DATE: {
          Calendar cal = Calendar.getInstance();
          cal.setTime(dataProp.getDate());
          jcrNode.setProperty(escapedPropName, cal);
          break;
        }
        case REF: {
          jcrNode.setProperty(escapedPropName, session.getNodeByUUID(dataProp.getRef().getId().toString()));
          break;
        }
        default: {
          throw new IllegalArgumentException();
        }
      }
    }
    // now process any child nodes of dataNode
    for (DataNode child : dataNode.getNodes()) {
      internalCreateOrUpdate(session, pentahoJcrConstants, escapeHelper, jcrNode, child);
    }
  }

  public NodeRepositoryFileData fromContentNode(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final IEscapeHelper escapeHelper, final Node fileNode) throws RepositoryException {
    Node unstructuredNode = fileNode.getNode(pentahoJcrConstants.getJCR_CONTENT());
    final String pattern = session.getNamespacePrefix(PentahoJcrConstants.PHO_NS) + ":" + "*"; //$NON-NLS-1$ //$NON-NLS-2$
    Assert.isTrue(unstructuredNode.getNodes(pattern).getSize() == 1);
    Node jcrNode = unstructuredNode.getNodes(pattern).nextNode();
    return new NodeRepositoryFileData(internalRead(session, pentahoJcrConstants, escapeHelper, jcrNode, null));
  }

  protected DataNode internalRead(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      IEscapeHelper escapeHelper, final Node jcrNode, final DataNode parentDataNode) throws RepositoryException {
    final String prefix = session.getNamespacePrefix(PentahoJcrConstants.PHO_NS) + ":"; //$NON-NLS-1$
    final String pattern = prefix + "*"; //$NON-NLS-1$

    String unescapedNodeName = escapeHelper.unescapeIllegalJcrChars(jcrNode.getName().substring(prefix.length()));
    
    DataNode dataNode = parentDataNode != null ? parentDataNode.addNode(unescapedNodeName)
        : new DataNode(unescapedNodeName);
    dataNode.setId(jcrNode.getUUID());

    PropertyIterator props = jcrNode.getProperties(pattern);
    while (props.hasNext()) {
      Property prop = props.nextProperty();
      String unescapedPropName = escapeHelper.unescapeIllegalJcrChars(prop.getName().substring(prefix.length()));
      switch (prop.getType()) {
        case PropertyType.STRING: {
          dataNode.setProperty(unescapedPropName, prop.getString());
          break;
        }
        case PropertyType.BOOLEAN: {
          dataNode.setProperty(unescapedPropName, prop.getBoolean());
          break;
        }
        case PropertyType.DOUBLE: {
          dataNode.setProperty(unescapedPropName, prop.getDouble());
          break;
        }
        case PropertyType.LONG: {
          dataNode.setProperty(unescapedPropName, prop.getLong());
          break;
        }
        case PropertyType.DATE: {
          dataNode.setProperty(unescapedPropName, prop.getDate().getTime());
          break;
        }
        case PropertyType.REFERENCE: {
          dataNode.setProperty(unescapedPropName, new DataNodeRef(prop.getNode().getUUID()));
          break;
        }
        default: {
          throw new IllegalArgumentException();
        }
      }
    }

    // iterate over children
    NodeIterator nodes = jcrNode.getNodes(pattern);
    while (nodes.hasNext()) {
      Node child = nodes.nextNode();
      internalRead(session, pentahoJcrConstants, escapeHelper, child, dataNode);
    }

    return dataNode;
  }

  /**
   * {@inheritDoc}
   */
  public boolean canRead(final String contentType, final Class<? extends IRepositoryFileData> clazz) {
    return CONTENT_TYPE.equals(contentType) && clazz.isAssignableFrom(NodeRepositoryFileData.class);
  }

  /**
   * {@inheritDoc}
   */
  public boolean canWrite(final Class<? extends IRepositoryFileData> clazz) {
    return NodeRepositoryFileData.class.equals(clazz);
  }

  /**
   * {@inheritDoc}
   */
  public String getContentType() {
    return CONTENT_TYPE;
  }

  public void updateContentNode(Session session, PentahoJcrConstants pentahoJcrConstants,
      final IEscapeHelper escapeHelper, NodeRepositoryFileData data, Node fileNode) throws RepositoryException {
    createOrUpdateContentNode(session, pentahoJcrConstants, escapeHelper, data, fileNode);
  }

}
