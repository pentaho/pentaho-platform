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


package org.pentaho.platform.plugin.condition.javascript;

import org.mozilla.javascript.ScriptableObject;

public class RhinoScriptable extends ScriptableObject {

  /**
   * 
   */
  private static final long serialVersionUID = 6876272459770131778L;

  /*
   * (non-Javadoc)
   * 
   * @see org.mozilla.javascript.Scriptable#getClassName()
   */
  @Override
  public String getClassName() {
    // TODO Auto-generated method stub
    return "org.pentaho.platform.plugin.javascript.RhinoScriptable"; //$NON-NLS-1$
  }

}
