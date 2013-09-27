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

package org.pentaho.platform.api.engine;

import java.util.Set;

/**
 * This interface makes certain that there is a mechanism to traverse the solution files from root to leaf. Note that
 * bi-directional traversal is not specified by this interface.
 * 
 * 
 * mlowery This interface is unrelated to security despite have the word ACL in its name.
 */

public interface IAclSolutionFile extends ISolutionFile, IAclHolder {

  /**
   * Gets the set children IAclSolutionFiles from this <code>IAclSolutionFile</code>. Each child must be an instance of
   * IAclSolutionFile.
   * 
   * @return <tt>Set</tt> of IAclSolutionFile objects
   */
  @SuppressWarnings( "unchecked" )
  public Set getChildrenFiles();
}
