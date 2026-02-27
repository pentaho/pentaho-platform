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

@SuppressWarnings( "nls" )
public class ClassToOverride {

  public String originalClass;

  @Override
  public String toString() {
    return "I am the original class from the parent class loader";
  }

}
