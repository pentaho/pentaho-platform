/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

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
