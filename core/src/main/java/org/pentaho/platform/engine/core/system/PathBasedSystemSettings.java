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
import org.dom4j.DocumentException;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Behavior is identical to <code>SystemSettings</code> except that the settings for <code>PentahoSystem</code> can
 * live in an arbitrary location in the file system (i.e. the file does not have to exist in the
 * <repository>/system). The location of the file used to initialize <code>PentahoSystem</code> is specified by the
 * <code>System</code> property whose key is <code>SYSTEM_CFG_PATH_KEY</code>. If this key does not exist in the
 * <code>System</code> properties, then this class falls back on the behavior of SystemSettings (i.e. it gets the
 * system settings from <repository>/system/pentaho.xml).
 * 
 * @author Steven Barkdull
 * 
 */
public class PathBasedSystemSettings extends SystemSettings {

  private static final long serialVersionUID = 666L;

  /**
   * key into System.property containing the path to the file containing settings information for
   * <code>PentahoSystem</code>.
   */
  public static final String SYSTEM_CFG_PATH_KEY = "PENTAHO_SYS_CFG_PATH"; //$NON-NLS-1$

  /**
   * the path to the file containing settings information for <code>PentahoSystem</code>. if this is null, the
   * system settings will be in <repository>/system/pentaho.xml
   */
  private String systemCfgPath;

  public PathBasedSystemSettings() {
    super();
    // NOTE: null is an acceptable value, if null, fall back to default
    systemCfgPath = System.getProperty( PathBasedSystemSettings.SYSTEM_CFG_PATH_KEY );
  }

  @Override
  public String getSystemSetting( final String settingName, final String defaultValue ) {
    if ( null != systemCfgPath ) {
      return getSystemSetting( systemCfgPath, settingName, defaultValue );
    } else {
      // use default
      return super.getSystemSetting( settingName, defaultValue );
    }
  }

  @Override
  public List getSystemSettings( final String settingName ) {
    if ( null != systemCfgPath ) {
      return getSystemSettings( systemCfgPath, settingName );
    } else {
      // use default
      return super.getSystemSettings( settingName );
    }
  }

  @Override
  protected String getAbsolutePath( final String path ) {
    if ( path.equals( systemCfgPath ) ) {
      return systemCfgPath;
    } else {
      // use default
      return super.getAbsolutePath( path );
    }
  }

  @Override
  public String getSystemCfgSourceName() {
    return null != systemCfgPath ? systemCfgPath : super.getSystemCfgSourceName();
  }

  @Override
  public Document getSettingsDocumentFromFile( File f ) throws IOException, DocumentException {
    return XmlDom4JHelper.getDocFromFile( f, new PentahoDtdEntityResolver() );
  }

}
