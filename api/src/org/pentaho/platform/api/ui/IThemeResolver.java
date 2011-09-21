package org.pentaho.platform.api.ui;

import java.util.Map;

/**
 * Theme Resolvers provide information on available themes and their resources.
 *
 * User: nbaker
 * Date: 5/15/11
 */
public interface IThemeResolver {

  /**
   * Return a map of ModuleThemeInfo objects keyed by module name.
   *
   * @return map of ModuleThemeInfo by module name
   */
  Map<String, ModuleThemeInfo> getModuleThemes();

  /**
   * Return a map of system themes keyed by theme name.
   *
   * @return map of System themes
   */
  Map<String, Theme> getSystemThemes();

}
