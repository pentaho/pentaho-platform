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


package org.pentaho.platform.api.engine;

import java.util.Set;

/**
 * This interface makes certain that there is a mechanism to traverse the solution files from root to leaf. Note
 * that bi-directional traversal is not specified by this interface.
 *
 *
 * mlowery This interface is unrelated to security despite have the word ACL in its name.
 */

@Deprecated
public interface IAclSolutionFile extends ISolutionFile, IAclHolder {

  /**
   * Gets the set children IAclSolutionFiles from this <code>IAclSolutionFile</code>. Each child must be an
   * instance of IAclSolutionFile.
   *
   * @return <tt>Set</tt> of IAclSolutionFile objects
   */
  @SuppressWarnings( "rawtypes" )
  public Set getChildrenFiles();
}
