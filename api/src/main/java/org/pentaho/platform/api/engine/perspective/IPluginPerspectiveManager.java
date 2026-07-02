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
