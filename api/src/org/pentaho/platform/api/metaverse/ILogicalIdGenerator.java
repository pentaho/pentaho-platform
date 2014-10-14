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

import java.util.Set;

public interface ILogicalIdGenerator {

  /**
   * Sets the property names that should be used in generating a logical id.
   *
   * @param propertyKeys property keys that indicate what makes this node logically unique
   */
  public void setLogicalIdPropertyKeys( String... propertyKeys );

  /**
   *
   * @return the Set of property keys that define logical equality for like nodes
   */
  public Set<String> getLogicalIdPropertyKeys();

  /**
   * Generates an ID based on properties that make it unique. It also will set that ID as a property on the node passed
   * in named 'logicalId'. It should reliably generate the same id for 2 nodes that are logically equal
   *
   * @param propertiesNode the object requiring a logical id
   * @return the logicalId generated
   */
  public String generateId( IHasProperties propertiesNode );

}
