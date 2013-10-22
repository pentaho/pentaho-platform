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

package org.pentaho.platform.engine.core.system;

import org.dom4j.Document;
import org.pentaho.platform.api.engine.ISystemSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class SimpleSystemSettings implements ISystemSettings {

  private Map<String, String> settings = new HashMap<String, String>();

  public SimpleSystemSettings() {
  }

  public void addSetting( String name, String value ) {
    settings.put( "pentaho-root|||" + name, value ); //$NON-NLS-1$
  }

  public String getSystemCfgSourceName() {
    return ""; //$NON-NLS-1$
  }

  public String getSystemSetting( String path, String settingName, String defaultValue ) {
    String value = settings.get( path + "|||" + settingName ); //$NON-NLS-1$
    if ( value == null ) {
      return defaultValue;
    }
    return value;
  }

  public String getSystemSetting( String settingName, String defaultValue ) {
    return getSystemSetting( "pentaho-root", settingName, defaultValue ); //$NON-NLS-1$
  }

  public List getSystemSettings( String path, String settingSection ) {
    String keyPrefix = path + "|||" + settingSection; //$NON-NLS-1$
    Set<String> keys = settings.keySet();
    Iterator<String> keyIterator = keys.iterator();
    List<String[]> results = new ArrayList<String[]>();
    while ( keyIterator.hasNext() ) {
      String key = keyIterator.next();
      if ( key.startsWith( keyPrefix ) ) {
        results.add( new String[] { key, settings.get( key ) } );
      }
    }
    return results;
  }

  public List getSystemSettings( String settingSection ) {
    // we don't support this
    return new ArrayList<String>();
  }

  public Document getSystemSettingsDocument( String actionPath ) {
    // we don't support this
    return null;
  }

  public Properties getSystemSettingsProperties( String path ) {
    // we don't support this
    return null;
  }

  public void resetSettingsCache() {
    // nothing to do
  }

}
