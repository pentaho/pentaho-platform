package org.pentaho.platform.api.ui;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class responsible for collecting information about all available system module local
 * themes. All implementations should utilize a caching mechanism to prevent unnecessary
 * overhead.
 *
 * User: nbaker
 * Date: 5/15/11
 */
public interface IThemeManager{

  /**
   * Set the collection of IThemeResolver objects used to find available themes
   * 
   * @param resolvers
   */
  void setThemeResolvers(List<IThemeResolver> resolvers);

  /**
   * Return a list of available system themes.
   *
   * @return List of themes
   */
  public List<String> getSystemThemeIds();

  /**
   * Returns the named theme or null if not found.
   * @param themeName
   * @return Theme for the supplied name
   */
  Theme getSystemTheme(String themeName);

  /**
   * Returns the named local theme for the given named module or null if either is not found
   * 
   * @param moduleName
   * @param themeName
   * @return
   */
  Theme getModuleTheme(String moduleName, String themeName);


  /**
   * Returns the MoudleThemeInfo object containing all themes for the given module name or null if not found.
   *
   * @param moduleName
   * @return
   */
  ModuleThemeInfo getModuleThemeInfo(String moduleName);

  /**
   * Force the Theme Manager to recalculate system and module themes
   */
  void refresh();
}
