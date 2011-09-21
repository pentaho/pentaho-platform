package org.pentaho.platform.api.ui;

import java.util.ArrayList;
import java.util.List;

/**
 * ModuleThemeInfo holds theme information for a platform module such as a plugin or other area.
 *
 * User: nbaker
 * Date: 5/15/11
 */
public class ModuleThemeInfo{
  private List<Theme> moduleThemes = new ArrayList<Theme>();
  private List<Theme> systemThemes = new ArrayList<Theme>();
  private String module;

  /**
   * Constructs a new ModuleThemeInfo object parented to the given ThemeModule
   *
   * @param module name
   */
  public ModuleThemeInfo(String module){
    this.module = module;
  }

  public List<Theme> getModuleThemes() {
    return moduleThemes;
  }

  public String getModule() {
    return module;
  }

  public List<Theme> getSystemThemes() {
    return systemThemes;
  }

}