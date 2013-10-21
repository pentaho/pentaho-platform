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

public interface IPluginOperation {

  /**
   * Gets the id for this operation. There is a set of standard ids, e.g. RUN, EDIT, DELETE etc. The id is not an
   * enum so that the list of operations can be extended by plug-ins
   * 
   * @return The operation id
   */
  public String getId();

  /**
   * Gets the resource perspective to launch for this operation
   * 
   * @return The operation command
   */
  public String getPerspective();

}
