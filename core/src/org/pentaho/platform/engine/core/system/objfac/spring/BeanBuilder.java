package org.pentaho.platform.engine.core.system.objfac.spring;

import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.springframework.beans.factory.FactoryBean;

import java.util.List;
import java.util.Map;

/**
 * User: nbaker
 * Date: 3/2/13
 */
public class BeanBuilder implements FactoryBean {

  private String type;
  private Map<String, String> attributes;

  /*
    * (non-Javadoc)
    * @see org.springframework.beans.factory.FactoryBean#getObject()
    */
  public Object getObject() {

    try {
      Class cls = getClass().getClassLoader().loadClass(type.trim());
      Object val = PentahoSystem.get(cls, PentahoSessionHolder.getSession(), attributes);
      return val;
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }

  }

  /*
    * (non-Javadoc)
    * @see org.springframework.beans.factory.FactoryBean#getObjectType()
    */
  public Class<?> getObjectType() {
    return Object.class;
  }

  /*
    * (non-Javadoc)
    * @see org.springframework.beans.factory.FactoryBean#isSingleton()
    */
  public boolean isSingleton() {
    return true;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Map<String, String> getAttributes() {
    return attributes;
  }

  public void setAttributes(Map<String, String> attributes) {
    this.attributes = attributes;
  }
}
