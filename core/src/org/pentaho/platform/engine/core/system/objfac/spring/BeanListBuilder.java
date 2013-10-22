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
