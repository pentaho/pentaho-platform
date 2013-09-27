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
 * Provides a simple interface for Portlet URL's (<tt>PortletURL</tt> objects).
 * 
 * @author mbatchel
 */

public interface IPentahoUrl {

  /**
   * Sets a URL parameter. Since the portlet URL handles all the URL parameter construction, talking to a generic
   * interface seems more extensible.
   * 
   * @param name
   *          Name of the parameter to set
   * @param value
   *          Value to set the parameter to
   */
  public void setParameter( String name, String value );

  /**
   * @return the full URL with the parameters incorporated
   */
  public String getUrl();

}
