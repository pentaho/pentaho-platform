/*!
 *
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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.engine.core.system.objfac.references;

import org.pentaho.platform.api.engine.IObjectCreator;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Session-aware Reference. An internal weak-map is maintained to keep track of instances by IPentahoSession.
 * <p/>
 * Created by nbaker on 4/15/14.
 */
public class SessionBoundPentahoObjectReference<T> extends AbstractPentahoObjectReference<T> {

  private final IObjectCreator<T> creator;
  private static WeakHashMap<IPentahoSession, Map<Class<?>, Object>> cache =
    new WeakHashMap<IPentahoSession, Map<Class<?>, Object>>();


  public SessionBoundPentahoObjectReference( Class<T> type, IObjectCreator<T> creator, Map<String, Object> attributes,
                                             Integer priority ) {
    super( type, attributes, priority );
    this.creator = creator;
  }

  @SuppressWarnings( "unchecked" )
  @Override protected T createObject() throws ObjectFactoryException {
    final IPentahoSession session = PentahoSessionHolder.getSession();

    Map<Class<?>, Object> classObjectMap = cache.get( session );
    if ( classObjectMap == null ) {
      classObjectMap = new WeakHashMap<Class<?>, Object>();
      cache.put( session, classObjectMap );
    }
    if ( classObjectMap.containsKey( this.getObjectClass() ) ) {
      return (T) classObjectMap.get( this.getObjectClass() );
    }
    T newObject = creator.create( session );
    classObjectMap.put( this.getObjectClass(), newObject );
    return newObject;
  }


  /**
   * workaround for inheritance in Builders. Ideas taken from: https://weblogs.java
   * .net/blog/emcmanus/archive/2010/10/25/using-builder-pattern-subclasses
   */
  private abstract static class BuilderBase<T, B extends BuilderBase<T, B>>
    extends AbstractPentahoObjectReference.Builder<T, B> {
    IObjectCreator<T> creator;

    public B creator( IObjectCreator<T> creator ) {
      this.creator = creator;
      return self();
    }

    @Override public SessionBoundPentahoObjectReference<T> build() {
      return new SessionBoundPentahoObjectReference<T>( this.type, this.creator, this.attributes, this.priority );
    }
  }

  /**
   * Public builder, implementation specific methods should be in BuilderBase, only self() should be defined here
   */
  public static class Builder<T> extends BuilderBase<T, Builder<T>> {
    public Builder( Class<T> type ) {
      type( type );
    }

    @Override public Builder<T> self() {
      return this;
    }
  }

}
