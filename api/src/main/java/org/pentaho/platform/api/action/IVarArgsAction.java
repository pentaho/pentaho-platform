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
