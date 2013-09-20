/*
* This program is free software; you can redistribute it and/or modify it under the
* terms of the GNU General Public License, version 2 as published by the Free Software
* Foundation.
*
* You should have received a copy of the GNU General Public License along with this
* program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
* or from the Free Software Foundation, Inc.,
* 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*
* This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
*
* Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
*/

package org.pentaho.platform.engine.core.system.objfac.spring;

import java.util.Map;

import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.springframework.beans.factory.FactoryBean;

/**
 * Obtains a reference to the requested bean from the PentahoSystem
 *
 * {@code}
 * <pen:bean class="com.foo.Clazz"/>
 * {@code}
 *
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
