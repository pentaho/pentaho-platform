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

/**
 * Manages Pentaho Request Context
 * <p>
 * For now the only information stored in the IPentahoRequestContext is teh context path. In future more
 * information can be added to this
 * 
 */
public interface IPentahoRequestContext {
  /**
   * Returns the portion of the request URI that indicates the context of the request. The context path always
   * comes first in a request URI. The path starts with a "/" character and end with a "/" character. For servlets
   * in the default (root) context, this method returns "/". The container does not decode this string. It is
   * possible that a servlet container may match a context by more than one context path. In such cases this method
   * will return the actual context path used by the request
   */
  public String getContextPath();
}
