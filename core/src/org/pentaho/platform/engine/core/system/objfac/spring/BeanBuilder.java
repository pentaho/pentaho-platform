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

import org.pentaho.platform.api.engine.IPentahoObjectReference;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

import java.util.List;
import java.util.Map;

/**
 * Obtains a reference to the requested bean from the PentahoSystem
 * 
 * {@code} <pen:bean class="com.foo.Clazz"/> {@code}
 * 
 * User: nbaker Date: 3/2/13
 */
public class BeanBuilder implements FactoryBean {

  private String type;
  private Map<String, String> attributes;
  private static ThreadLocal<BeanBuilder> resolvingBean = new ThreadLocal<BeanBuilder>();
  private static Logger log = LoggerFactory.getLogger( BeanBuilder.class );

  /*
   * (non-Javadoc)
   * 
   * @see org.springframework.beans.factory.FactoryBean#getObject()
   */
  public Object getObject() {

    try {
      if ( resolvingBean.get() == this ) {
        log.warn( "Circular Reference detected in bean creation ( "
            + type
            + " : "
            + attributes
            + "). Very likely a published "
            + "pentaho bean is resolving itself. Ensure that the published attributes do not match that of the Pentaho "
            + "bean query. The system will attempt to find the next highest available bean, but at a performance "
            + "penilty" );
        // attempt to find a lower priority bean for them
        Class cls = getClass().getClassLoader().loadClass( type.trim() );
        resolvingBean.set( this );
        List<IPentahoObjectReference<?>> objectReferences =
            PentahoSystem.getObjectFactory().getObjectReferences( cls, PentahoSessionHolder.getSession(), attributes );
        resolvingBean.set( null );
        if ( objectReferences.size() > 1 ) {
          // we have more than one, return the second highest
          return objectReferences.get( 1 ).getObject();
        } else {
          // there's only one bean, this is a fatal situation
          throw new IllegalStateException( "Fatal Circular reference in Pentaho Bean ( " + type + " : " + attributes
              + ")" );
        }

      } else {
        Class cls = getClass().getClassLoader().loadClass( type.trim() );
        resolvingBean.set( this );
        Object val = PentahoSystem.get( cls, PentahoSessionHolder.getSession(), attributes );
        resolvingBean.set( null );
        return val;
      }
    } catch ( ClassNotFoundException e ) {
      throw new RuntimeException( e );
    } catch ( ObjectFactoryException e ) {
      throw new RuntimeException( e );
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.springframework.beans.factory.FactoryBean#getObjectType()
   */
  public Class<?> getObjectType() {
    return Object.class;
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
