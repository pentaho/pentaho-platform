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
 * The IMetaverseBuilder is a Builder that creates and maintains a metaverse model, which contains nodes and links.
 */
public interface IMetaverseBuilder {

  /**
   * Adds the specified node to the metaverse model.
   *
   * @param node the node to add
   * @return the metaverse builder (for chaining)
   */
  IMetaverseBuilder addNode( IMetaverseNode node );

  /**
   * Adds the specified link to the model. If the link refers to nodes that do not yet exist in the model, then
   * placeholder node(s) should also be inserted to maintain a valid graph model.
   *
   * @param link the link to add
   * @return the metaverse builder (for chaining)
   */
  IMetaverseBuilder addLink( IMetaverseLink link );

  /**
   * Adds the link.
   *
   * @param fromNode the from node
   * @param label    the label
   * @param toNode   the to node
   * @return the i metaverse builder
   */
  IMetaverseBuilder addLink( IMetaverseNode fromNode, String label, IMetaverseNode toNode );

  /**
   * Deletes the specified node from the metaverse model.
   *
   * @param node the node to remove
   * @return the metaverse builder (for chaining)
   */
  IMetaverseBuilder deleteNode( IMetaverseNode node );

  /**
   * Deletes the specified link from the metaverse model.
   *
   * @param link the link to remove
   * @return the metaverse builder (for chaining)
   */
  IMetaverseBuilder deleteLink( IMetaverseLink link );

  /**
   * Updates the specified node to have the provided attributes.
   *
   * @param updatedNode the node with updated attributes
   * @return the metaverse builder (for chaining)
   */
  IMetaverseBuilder updateNode( IMetaverseNode updatedNode );

  /**
   * Updates the specified link to have the provided attributes.
   *
   * @param link     the link
   * @param newLabel the new label
   * @return the metaverse builder (for chaining)
   */
  IMetaverseBuilder updateLinkLabel( IMetaverseLink link, String newLabel );

  /**
   * Returns a metaverse object factory for creating metaverse components (nodes, links, e.g.)
   *
   * @return a metaverse object factory
   */
  IMetaverseObjectFactory getMetaverseObjectFactory();

  /**
   * Sets the metaverse object factory for this builder. Most classes that need a metaverse object
   * factory will get it from their builder (if they have implemented IRequiresMetaverseBuilder
   *
   * @param metaverseObjectFactory the metaverse object factory to set
   */
  void setMetaverseObjectFactory( IMetaverseObjectFactory metaverseObjectFactory );

}
