package org.pentaho.platform.engine.core.system.objfac.spring;

/**
 * User: nbaker
 * Date: 3/27/13
 */
public class Marker{
  private String id;
  public Marker(String id){
    this.id = id;
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    return id.equals(o);
  }
}