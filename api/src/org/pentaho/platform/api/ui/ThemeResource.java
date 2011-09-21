package org.pentaho.platform.api.ui;

/**
 * User: nbaker
 * Date: 5/15/11
 */
public class ThemeResource{
  Theme theme;
  String location;
  public ThemeResource(Theme theme, String resource){
    this.theme = theme;
    location = resource;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public Theme getTheme() {
    return theme;
  }

  public void setTheme(Theme theme) {
    this.theme = theme;
  }
}