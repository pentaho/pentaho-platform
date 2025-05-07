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

package org.pentaho.platform.engine.core.system.objfac.spring;

import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.springframework.beans.factory.FactoryBean;

import java.util.List;
import java.util.Map;

/**
 * Obtains references registered in the PentahoSystem for the given class, exposed as an ordered list by priority
 *
 * {@code} <pen:list class="com.foo.Clazz"/> {@code}
 *
 * User: nbaker Date: 3/2/13
 */
public class BeanListBuilder implements FactoryBean {

  private String type;
  private Map<String, String> attributes;

  /*
   * (non-Javadoc)
   *
   * @see org.springframework.beans.factory.FactoryBean#getObject()
   */
  public List getObject() {
    try {
      Class cls = getClass().getClassLoader().loadClass( type.trim() );
      List<?> vals = PentahoSystem.getAll( cls, PentahoSessionHolder.getSession(), attributes );
      return vals;
    } catch ( ClassNotFoundException e ) {
      throw new RuntimeException( e );
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.springframework.beans.factory.FactoryBean#getObjectType()
   */
  public Class<?> getObjectType() {
    return List.class;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.springframework.beans.factory.FactoryBean#isSingleton()
   */
  public boolean isSingleton() {
    return true;
  }

  public String getType() {
    return type;
  }

  public void setType( String type ) {
    this.type = type;
  }

  public Map<String, String> getAttributes() {
    return attributes;
  }

  public void setAttributes( Map<String, String> attributes ) {
    this.attributes = attributes;
  }
}
