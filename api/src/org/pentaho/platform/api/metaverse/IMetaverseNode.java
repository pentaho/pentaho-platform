/*!
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
 * Copyright (c) 2002-2014 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.api.metaverse;

/**
 * The IMetaverseNode interface represents a node/vertex in the graph model of the metaverse.
 */
public interface IMetaverseNode {

  /**
   * Gets the name of this node.
   *
   * @return the String name of the node
   */
  String getName();

  /**
   * Gets the metaverse-unique identifier for this node.
   *
   * @return the String ID of the node
   */
  String getID();

  /**
   * Gets the type of this node.
   *
   * @return the String type of the node
   */
  String getType();

}
