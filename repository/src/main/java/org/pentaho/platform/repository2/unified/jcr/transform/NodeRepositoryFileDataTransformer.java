/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.repository2.unified.jcr.transform;

import java.util.Calendar;

import javax.jcr.ItemNotFoundException;
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
import org.pentaho.platform.repository2.unified.jcr.ITransformer;
import org.pentaho.platform.repository2.unified.jcr.JcrStringHelper;
import org.pentaho.platform.repository2.unified.jcr.NodeHelper;
import org.pentaho.platform.repository2.unified.jcr.PentahoJcrConstants;
import org.springframework.util.Assert;

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
    // Not need to check the name if we encoded it
    // JcrRepositoryFileUtils.checkName( dataNode.getName() );

    nodeName = JcrStringHelper.fileNameEncode( nodeName );

    if ( NodeHelper.hasNode( jcrParentNode, prefix, nodeName ) ) {
      jcrNode = NodeHelper.getNode( jcrParentNode, prefix, nodeName );
    } else {
      jcrNode = jcrParentNode.addNode( prefix + nodeName, pentahoJcrConstants.getPHO_NT_INTERNALFOLDER() );
    }
    // set any properties represented by dataNode
    for ( DataProperty dataProp : dataNode.getProperties() ) {

      String propName = dataProp.getName();
      // Not need to check the name if we encoded it
      // JcrRepositoryFileUtils.checkName( propName );

      propName = prefix + JcrStringHelper.fileNameEncode( propName );

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

    String nodeName = JcrStringHelper.fileNameDecode( jcrNode.getName().substring( prefix.length() ) );

    DataNode dataNode = parentDataNode != null ? parentDataNode.addNode( nodeName ) : new DataNode( nodeName );
    dataNode.setId( jcrNode.getIdentifier() );

    PropertyIterator props = jcrNode.getProperties( pattern );
    while ( props.hasNext() ) {
      Property prop = props.nextProperty();
      String propName = JcrStringHelper.fileNameDecode( prop.getName().substring( prefix.length() ) );
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
