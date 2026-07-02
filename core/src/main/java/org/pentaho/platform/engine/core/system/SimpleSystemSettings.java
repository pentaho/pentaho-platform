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
