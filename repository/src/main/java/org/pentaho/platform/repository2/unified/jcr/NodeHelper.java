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


package org.pentaho.platform.repository2.unified.jcr;

import org.pentaho.platform.api.repository2.unified.data.node.DataNode;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

public class NodeHelper {

  /**
   * Encapsulate hasNode calls here to ensure we are encoding the parameter
   * @param parentNode
   * @param nodeName
   * @return
   * @throws javax.jcr.RepositoryException
   */
  protected static boolean checkHasNode( Node parentNode, String nodeName ) throws RepositoryException {
    return parentNode.hasNode( nodeName );
  }

  /**
   * Encapsulate hasNode calls here to ensure we are encoding the parameter
   * @param parentNode
   * @param nodeNamePrefix
   * @param nodeName
   * @return
   * @throws RepositoryException
   */
  public static boolean hasNode( Node parentNode, String nodeNamePrefix, String nodeName ) throws RepositoryException {
    return checkHasNode( parentNode, nodeNamePrefix + JcrStringHelper.fileNameEncode( nodeName ));
  }

  /**
   * Encapsulate addNode calls here to ensure we are encoding the parameter
   * @param parentNode
   * @param nodeName
   * @return
   * @throws RepositoryException
   */
  protected static Node checkAddNode( Node parentNode, String nodeName ) throws RepositoryException {
    return parentNode.addNode( nodeName );
  }

  /**
   * Encapsulate addNode calls here to ensure we are encoding the parameter
   * @param parentNode
   * @param nodeNamePrefix
   * @param nodeName
   * @return
   * @throws RepositoryException
   */
  public static Node addNode( Node parentNode, String nodeNamePrefix, String nodeName ) throws RepositoryException {
    return checkAddNode( parentNode, nodeNamePrefix + JcrStringHelper.fileNameEncode( nodeName ) );
  }

  /**
   * Encapsulate addNode calls here to ensure we are encoding the parameter
   * @param parentNode
   * @param nodeNamePrefix
   * @param nodeName
   * @return
   * @throws RepositoryException
   */
  public static Node addNode( Node parentNode, String nodeNamePrefix, String nodeName, String nodeParameter ) throws RepositoryException {
    return checkAddNode( parentNode, nodeNamePrefix + JcrStringHelper.fileNameEncode( nodeName ), nodeParameter );
  }

  /**
   * Encapsulate addNode calls here to ensure we are encoding the parameter
   * @param parentNode
   * @param nodeName
   * @return
   * @throws RepositoryException
   */
  protected static Node checkAddNode( Node parentNode, String nodeName, String nodeParameter ) throws RepositoryException {
    return parentNode.addNode( nodeName, nodeParameter );
  }

  /**
   * Encapsulate getNode calls here to ensure we are encoding the parameter
   * @param parentNode
   * @param nodeName
   * @return
   * @throws RepositoryException
   */
  protected static Node checkGetNode( Node parentNode, String nodeName ) throws RepositoryException {
    return parentNode.getNode( nodeName );
  }

  /**
   * Encapsulate getNode calls here to ensure we are encoding the parameter
   * @param parentNode
   * @param nodeNamePrefix
   * @param nodeName
   * @return
   * @throws RepositoryException
   */
  public static Node getNode( Node parentNode, String nodeNamePrefix, String nodeName ) throws RepositoryException {
    return checkGetNode( parentNode, nodeNamePrefix + JcrStringHelper.fileNameEncode( nodeName ) );
  }

  /**
   * Safely create data node with jcr encoded name
   * @param name
   * @return
   */
  public static DataNode createDataNode( String name ){
    return new DataNode( JcrStringHelper.fileNameEncode( name ));
  }
}
