/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.api.engine;

import org.dom4j.Document;

import java.util.List;
import java.util.Properties;

/**
 * The SystemSettings manages the platform's overall configuration settings. These settings by default can be found
 * in the system tree in an xml file named "pentaho.xml."
 */

public interface ISystemSettings {

  /**
   * Gets the name of the source of the system configurations. For system configurations that are stored in a file,
   * it should return the filename. Other implementations should return a name that is relevant to the
   * implementation (possibly a URL, or a database sql query, etc.) Often this will be pentaho.xml
   * 
   * @return String containing a name that identifies the source of the system configuration
   */
  public String getSystemCfgSourceName();

  /**
   * Gets a system setting from the system path
   * 
   * @param path
   *          relative to the "system" directory, go to this document
   * @param settingName
   *          the setting name to get
   * @param defaultValue
   *          the value to use if the setting isn't specified in the setting document
   * @return the setting requested, or the default value if not found
   */
  public String getSystemSetting( String path, String settingName, String defaultValue );

  /**
   * Gets a system setting from the system configuration file
   * 
   * @param settingName
   *          the setting name to get
   * @param defaultValue
   *          the value to use if the setting isn't specified in the setting document
   * @return the setting requested, or the default value if not found
   */
  public String getSystemSetting( String settingName, String defaultValue );

  /**
   * Gets a section from the specified settings document
   * 
   * @param path
   *          relative to the system directory, go to this document
   * @param settingSection
   *          the section is the document to retrieve
   * @return the list of settings in the specified section of the document
   */
  @SuppressWarnings( "rawtypes" )
  public List getSystemSettings( String path, String settingSection );

  /**
   * Gets a section from the system system configuration file
   * 
   * @param settingSection
   *          the section to retrieve
   * @return the list of elements in the section of the document.
   */
  @SuppressWarnings( "rawtypes" )
  public List getSystemSettings( String settingSection );

  /**
   * The SystemSettings object caches each settings document once it's read in. If the system gets a refresh event,
   * this should be called to make sure that the system settings get refreshed.
   */
  public void resetSettingsCache();

  /**
   * Returns a Document object containing the settings document within the path specified by actionPath.
   * 
   * @param actionPath
   *          The XML document relative to the solution that contains the settings desired
   * @return Document Parsed XML document.
   */
  public Document getSystemSettingsDocument( String actionPath );

  /**
   * Gets a properties file from the solution.
   * 
   * @param path
   *          Relative path to the properties file within the solution
   * @return <tt>Properties</tt> object containing the properties.
   */
  public Properties getSystemSettingsProperties( String path );

}
