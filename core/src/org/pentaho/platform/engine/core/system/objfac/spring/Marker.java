package org.pentaho.platform.engine.core.system.objfac.spring;

/**
 * Used by the {@link PublishedBeanRegistry} to identify a particular beanFactory. An instance is registered inside
 * the BeanFactory with a UUID. This is later extracted to find published beans from that factory.
 *
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