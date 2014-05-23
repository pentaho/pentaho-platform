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
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.repository2.unified.jcr.transform;

import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.DataNodeRef;
import org.pentaho.platform.api.repository2.unified.data.node.DataProperty;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.repository2.unified.jcr.ITransformer;
import org.pentaho.platform.repository2.unified.jcr.JcrRepositoryFileUtils;
import org.pentaho.platform.repository2.unified.jcr.NodeHelper;
import org.pentaho.platform.repository2.unified.jcr.PentahoJcrConstants;
import org.springframework.util.Assert;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.Calendar;

public class NodeRepositoryFileDataTransformer implements ITransformer<NodeRepositoryFileData> {

  // ~ Static fields/initializers
  // ======================================================================================

  // ~ Instance fields
  // =================================================================================================

  // ~ Constructors
  // ====================================================================================================

  public NodeRepositoryFileDataTransformer() {
    super();
  }

  // ~ Methods
  // =========================================================================================================

  protected void createOrUpdateContentNode( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final NodeRepositoryFileData data, final Node fileNode ) throws RepositoryException {
    Node unstructuredNode = null;
    if ( fileNode.hasNode( pentahoJcrConstants.getJCR_CONTENT() ) ) {
      unstructuredNode = fileNode.getNode( pentahoJcrConstants.getJCR_CONTENT() );
    } else {
      unstructuredNode =
          fileNode.addNode( pentahoJcrConstants.getJCR_CONTENT(), pentahoJcrConstants.getPHO_NT_INTERNALFOLDER() );
    }

    // clear out all nodes since it's the quickest way to guarantee that existing nodes that should be deleted are
    // removed
    final String pattern = session.getNamespacePrefix( PentahoJcrConstants.PHO_NS ) + ":" + "*"; //$NON-NLS-1$ //$NON-NLS-2$
    NodeIterator nodes = unstructuredNode.getNodes( pattern );
    while ( nodes.hasNext() ) {
      nodes.nextNode().remove();
    }

    internalCreateOrUpdate( session, pentahoJcrConstants, unstructuredNode, data.getNode() );
  }

  public void createContentNode( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final NodeRepositoryFileData data, final Node fileNode ) throws RepositoryException {
    createOrUpdateContentNode( session, pentahoJcrConstants, data, fileNode );
  }

  protected void internalCreateOrUpdate( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Node jcrParentNode, final DataNode dataNode ) throws RepositoryException {
    final String prefix = session.getNamespacePrefix( PentahoJcrConstants.PHO_NS ) + ":"; //$NON-NLS-1$
    // get or create the node represented by dataNode
    Node jcrNode = null;
    String nodeName = dataNode.getName();

    JcrRepositoryFileUtils.checkName( dataNode.getName() );

    if ( NodeHelper.hasNode( jcrParentNode, prefix, nodeName ) ) {
      jcrNode = NodeHelper.getNode( jcrParentNode, prefix, nodeName );
    } else {
      jcrNode = jcrParentNode.addNode( prefix + nodeName, pentahoJcrConstants.getPHO_NT_INTERNALFOLDER() );
    }
    // set any properties represented by dataNode
    for ( DataProperty dataProp : dataNode.getProperties() ) {

      JcrRepositoryFileUtils.checkName( dataProp.getName() );

      String propName = prefix + dataProp.getName();

      switch ( dataProp.getType() ) {
        case STRING: {
          jcrNode.setProperty( propName, dataProp.getString() );
          break;
        }
        case BOOLEAN: {
          jcrNode.setProperty( propName, dataProp.getBoolean() );
          break;
        }
        case DOUBLE: {
          jcrNode.setProperty( propName, dataProp.getDouble() );
          break;
        }
        case LONG: {
          jcrNode.setProperty( propName, dataProp.getLong() );
          break;
        }
        case DATE: {
          Calendar cal = Calendar.getInstance();
          cal.setTime( dataProp.getDate() );
          jcrNode.setProperty( propName, cal );
          break;
        }
        case REF: {
          jcrNode.setProperty( propName, session.getNodeByIdentifier( dataProp.getRef().getId().toString() ) );
          break;
        }
        default: {
          throw new IllegalArgumentException();
        }
      }
    }
    // now process any child nodes of dataNode
    for ( DataNode child : dataNode.getNodes() ) {
      internalCreateOrUpdate( session, pentahoJcrConstants, jcrNode, child );
    }
  }

  public NodeRepositoryFileData fromContentNode( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Node fileNode ) throws RepositoryException {
    Node unstructuredNode = fileNode.getNode( pentahoJcrConstants.getJCR_CONTENT() );
    final String pattern = session.getNamespacePrefix( PentahoJcrConstants.PHO_NS ) + ":" + "*"; //$NON-NLS-1$ //$NON-NLS-2$
    Assert.isTrue( unstructuredNode.getNodes( pattern ).getSize() == 1 );
    Node jcrNode = unstructuredNode.getNodes( pattern ).nextNode();
    return new NodeRepositoryFileData( internalRead( session, pentahoJcrConstants, jcrNode, null ) );
  }

  protected DataNode internalRead( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Node jcrNode, final DataNode parentDataNode ) throws RepositoryException {
    final String prefix = session.getNamespacePrefix( PentahoJcrConstants.PHO_NS ) + ":"; //$NON-NLS-1$
    final String pattern = prefix + "*"; //$NON-NLS-1$

    String nodeName = jcrNode.getName().substring( prefix.length() );

    DataNode dataNode = parentDataNode != null ? parentDataNode.addNode( nodeName ) : new DataNode( nodeName );
    dataNode.setId( jcrNode.getIdentifier() );

    PropertyIterator props = jcrNode.getProperties( pattern );
    while ( props.hasNext() ) {
      Property prop = props.nextProperty();
      String propName = prop.getName().substring( prefix.length() );
      switch ( prop.getType() ) {
        case PropertyType.STRING: {
          dataNode.setProperty( propName, prop.getString() );
          break;
        }
        case PropertyType.BOOLEAN: {
          dataNode.setProperty( propName, prop.getBoolean() );
          break;
        }
        case PropertyType.DOUBLE: {
          dataNode.setProperty( propName, prop.getDouble() );
          break;
        }
        case PropertyType.LONG: {
          dataNode.setProperty( propName, prop.getLong() );
          break;
        }
        case PropertyType.DATE: {
          dataNode.setProperty( propName, prop.getDate().getTime() );
          break;
        }
        case PropertyType.REFERENCE: {
          try {
            dataNode.setProperty( propName, new DataNodeRef( prop.getNode().getIdentifier() ) );
          } catch ( ItemNotFoundException e ) {
            // reference is missing, replace with missing data ref
            // this situation can occur if the user does not have permission to access the reference.
            dataNode.setProperty( propName, new DataNodeRef( DataNodeRef.REF_MISSING ) );
          }
          break;
        }
        default: {
          throw new IllegalArgumentException();
        }
      }
    }

    // iterate over children
    NodeIterator nodes = jcrNode.getNodes( pattern );
    while ( nodes.hasNext() ) {
      Node child = nodes.nextNode();
      internalRead( session, pentahoJcrConstants, child, dataNode );
    }

    return dataNode;
  }

  /**
   * {@inheritDoc}
   */
  public boolean canRead( final String contentType, final Class<? extends IRepositoryFileData> clazz ) {
    return IRepositoryFileData.NODE_CONTENT_TYPE.equals( contentType )
        && clazz.isAssignableFrom( NodeRepositoryFileData.class );
  }

  /**
   * {@inheritDoc}
   */
  public boolean canWrite( final Class<? extends IRepositoryFileData> clazz ) {
    return NodeRepositoryFileData.class.equals( clazz );
  }

  /**
   * {@inheritDoc}
   */
  public String getContentType() {
    return IRepositoryFileData.NODE_CONTENT_TYPE;
  }

  public void updateContentNode( Session session, PentahoJcrConstants pentahoJcrConstants, NodeRepositoryFileData data,
      Node fileNode ) throws RepositoryException {
    createOrUpdateContentNode( session, pentahoJcrConstants, data, fileNode );
  }

}
