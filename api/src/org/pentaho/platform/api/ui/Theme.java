package org.pentaho.platform.api.ui;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Theme encapsulates a collection of ThemeResources and a root directory to access them from.
 *
 * User: nbaker
 * Date: 5/15/11
 */
public class Theme {

  private Set<ThemeResource> resources = new LinkedHashSet<ThemeResource>();

  private String name;  
  private String themeRootDir;
  private boolean hidden;
  private String id;

  public Theme(String id, String name, String rootDir){
    this.id = id;
    this.name = name;
    this.themeRootDir = rootDir;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Set<ThemeResource> getResources() {
    return resources;
  }

  public void setResources(Set<ThemeResource> resources) {
    this.resources = resources;
  }

  public void addResource(ThemeResource themeResource) {
    resources.add(themeResource);
  }

  public String getThemeRootDir() {
    return themeRootDir;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Theme theme = (Theme) o;

    if (name != null ? !name.equals(theme.name) : theme.name != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return name != null ? name.hashCode() : 0;
  }

  public boolean isHidden(){
    return hidden;
  }

  public void setHidden(boolean hidden) {
    this.hidden = hidden;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
}
