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


package org.pentaho.platform.api.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * ModuleThemeInfo holds theme information for a platform module such as a plugin or other area.
 * 
 * User: nbaker Date: 5/15/11
 */
public class ModuleThemeInfo implements Serializable {

  /**
   * for Serializable
   */
  private static final long serialVersionUID = 7878856976816551482L;

  private List<Theme> moduleThemes = new ArrayList<Theme>();
  private List<Theme> systemThemes = new ArrayList<Theme>();
  private String module;

  /**
   * Constructs a new ModuleThemeInfo object parented to the given ThemeModule
   * 
   * @param module
   *          name
   */
  public ModuleThemeInfo( String module ) {
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
