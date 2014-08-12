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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.api.metaverse;

/**
 * The IIdentifiable interface provides commonly used methods for identifying entities, such as name, ID, type
 * 
 */
public interface IIdentifiable {

  /**
   * Gets the name of this entity.
   * 
   * @return the String name of the entity
   */
  String getName();

  /**
   * Gets the metaverse-unique identifier for this entity.
   *
   * NOTE: This MUST return the same value as INamespace.getNamespaceId()
   *
   * @return the String ID of the entity.
   */
  String getStringID();

  /**
   * Gets the type of this entity.
   * 
   * @return the String type of the entity
   */
  String getType();

  /**
   * Sets the name.
   * 
   * @param name
   *          the new name
   */
  void setName( String name );

  /**
   * Sets the type.
   * 
   * @param type
   *          the new type
   */
  void setType( String type );

}
