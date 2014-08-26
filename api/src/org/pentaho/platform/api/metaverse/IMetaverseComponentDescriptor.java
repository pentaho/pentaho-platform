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
 * IMetaverseComponentDescriptor is a contract for an object that can describe a metaverse component. For example, it
 * could contain name, type, and namespace information for a particular document. The metadata about the component and
 * the component itself are separated to allow for maximum flexibility.
 */
public interface IMetaverseComponentDescriptor extends IIdentifiable, INamespace {

  /**
   * Sets the namespace for the component described by this descriptor.
   *
   * @param namespace the namespace to set
   */
  void setNamespace( INamespace namespace );

  /**
   * Gets the namespace for the component described by this descriptor.
   *
   * @return the namespace of the described component
   */
  INamespace getNamespace();

  /**
   * Gets the context ("static", "runtime", e.g.) associated with the component described by this descriptor.
   *
   * @return A string containing a description of the context associated with the described component
   */
  String getContext();

  /**
   * Sets the context ("static", "runtime", e.g.) associated with the component described by this descriptor.
   *
   * @param context the context for the described component
   */
  void setContext( String context );
}
