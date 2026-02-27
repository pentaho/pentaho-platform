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
