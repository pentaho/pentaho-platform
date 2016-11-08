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

package org.pentaho.platform.api.repository;

import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.ISessionContainer;

import java.util.Collection;

public interface IRuntimeRepository extends ILogger, ISessionContainer {

  /**
   * Loads a runtimeElement by it's ID.
   * 
   * @param instanceId
   *          The instance ID for the Runtime Element
   * @param allowableReadAttributeNames
   *          The attribute names that are allowed to be read by this process
   * @return The RuntimeElement
   * @throws RepositoryException
   */
  @SuppressWarnings( "rawtypes" )
  public IRuntimeElement loadElementById( String instanceId, Collection allowableReadAttributeNames )
    throws RepositoryException;

  /**
   * Constructs a new Runtime Element.
   * 
   * @param parentId
   *          The parent of the Runtime Element
   * @param parentType
   *          The parent type of the Runtime Element
   * @param transientOnly
   *          Indicates whether this runtime element will be transient only (unsaved) or persistent. If true, then
   *          this will be in memory only, and will not be persisted when it goes out of scope.
   * @return The new Runtime element
   */
  public IRuntimeElement newRuntimeElement( String parentId, String parentType, boolean transientOnly );

  /**
   * Constructs a new Runtime Element.
   * 
   * @param parId
   *          The parent of the Runtime Element
   * @param parType
   *          The parent type of the Runtime Element
   * @param solutionId
   *          The ID of the Solution this Runtime Element is associated with
   * @param transientOnly
   *          Indicates whether this runtime element will be transient only (unsaved) or persistent. If true, then
   *          this will be in memory only, and will not be persisted when it goes out of scope.
   * @return The new Runtime element
   */
  public IRuntimeElement newRuntimeElement( String parId, String parType, String solutionId, boolean transientOnly );

  public boolean usesHibernate();

}
