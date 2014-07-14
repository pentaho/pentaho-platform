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

import java.util.Set;

/**
 * The IMetaverseNode interface represents a node/vertex in the graph model of the metaverse.
 */
public interface IMetaverseNode extends IIdentifiable {
  
  /**
     * Return the object value associated with the provided string key.
     * If no value exists for that key, return null.
     *
     * @param key the key of the key/value property
     * @return the object value related to the string key
     */
    public <T> T getProperty(String key);

    /**
     * Return all the keys associated with the element.
     *
     * @return the set of all string keys associated with the element
     */
    public Set<String> getPropertyKeys();

    /**
     * Assign a key/value property to the element.
     * If a value already exists for this key, then the previous key/value is overwritten.
     *
     * @param key   the string key of the property
     * @param value the object value o the property
     */
    public void setProperty(String key, Object value);

    /**
     * Un-assigns a key/value property from the element.
     * The object value of the removed property is returned.
     *
     * @param key the key of the property to remove from the element
     * @return the object value associated with that key prior to removal
     */
    public <T> T removeProperty(String key);

}
