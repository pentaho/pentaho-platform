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


package org.pentaho.test.platform.engine.core;

import org.pentaho.platform.engine.core.system.SystemSettings;

import java.util.ArrayList;
import java.util.List;

/**
 * This settings class allows you to initialize the MicroPlatform with the xml based SystemSettings class, and
 * bootstrap successfully.
 * 
 * @author GMoran
 * 
 */
public class XmlSimpleSystemSettings extends SystemSettings {

  private static final long serialVersionUID = -2399565415836826106L;

  @SuppressWarnings( "unchecked" )
  @Override
  public List getSystemSettings( String path, String settingName ) {
    // TODO Auto-generated method stub
    List empty = super.getSystemSettings( path, settingName );
    if ( empty == null ) {
      return new ArrayList();
    }
    return empty;
  }

  @Override
  public String getSystemSetting( String settingName, String defaultValue ) {
    String empty = super.getSystemSetting( settingName, defaultValue );
    if ( empty == null ) {
      return "";
    }
    return empty;
  }

}
