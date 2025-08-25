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
