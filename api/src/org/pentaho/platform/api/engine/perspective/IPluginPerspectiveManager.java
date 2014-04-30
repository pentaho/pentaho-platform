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

package org.pentaho.platform.api.engine.perspective;

import org.pentaho.platform.api.engine.perspective.pojo.IPluginPerspective;

import java.util.List;

public interface IPluginPerspectiveManager {

  public List<IPluginPerspective> getPluginPerspectives();

  /**
   * @param pluginPerspective
   * @deprecated No longer needed as IPluginPerspective objects are registered with PentahoSystem. Existing calls will
   * do nothing. Replace calls to this method with:
   * <pre>
   *   PentahoSystem.registerObject( pluginPerspective );
   * </pre>
   */
  @Deprecated
  public void addPluginPerspective( IPluginPerspective pluginPerspective );

  /**
   * @param pluginPerspective
   * @deprecated No longer needed as IPluginPerspective objects are registered with PentahoSystem. Existing calls will
   * do nothing. Replace calls to this method with {@link org.pentaho.platform.api.engine
   * .IPentahoObjectRegistration#remove()}
   * <pre>
   *   IPentahoObjectRegistration handle = PentahoSystem.registerObject( pluginPerspective );
   *   handle.remove(); // replaces call to removePluginPerspective
   * </pre>
   */
  @Deprecated
  public void removePluginPerspective( IPluginPerspective pluginPerspective );

  /**
   * @deprecated No longer needed as IPluginPerspective objects are registered with PentahoSystem. Existing calls will
   * do nothing. Replace calls to this method with {@link org.pentaho.platform.api.engine
   * .IPentahoObjectRegistration#remove()}
   * <pre>
   *   IPentahoObjectRegistration handle = PentahoSystem.registerObject( pluginPerspective );
   *   handle.remove(); // replaces call to removePluginPerspective
   * </pre>
   */
  @Deprecated
  public void clearPluginPerspectives();

}
