/**
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2006 - 2014 Pentaho Corporation.  All rights reserved.
 *
 */

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
