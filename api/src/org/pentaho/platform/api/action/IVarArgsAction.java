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

package org.pentaho.platform.api.action;

import java.util.Map;

/**
 * Allows an Action to accept inputs from the action sequence that are unspecified by the Action itself. In other
 * words, if there is no bean property for a particular input, it will be passed to the Action through this API.
 * 
 * @see IAction
 * @author aphillips
 * @since 3.6
 */
public interface IVarArgsAction extends IAction {

  /**
   * Inputs from an action sequence that cannot be set on an Action by Java bean convention will be passed in
   * through this map.
   * 
   * @param args
   *          a map of unspecified inputs
   */
  public void setVarArgs( Map<String, Object> args );

}
