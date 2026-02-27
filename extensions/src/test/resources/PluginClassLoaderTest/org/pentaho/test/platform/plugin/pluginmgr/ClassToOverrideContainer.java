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


package org.pentaho.test.platform.plugin.pluginmgr;

public class ClassToOverrideContainer {
  public static final ClassToOverride CLASS_TO_OVERRIDE = new ClassToOverride();
  
  @Override
  public String toString() {
    return new ClassToOverride().toString();
  }
}
