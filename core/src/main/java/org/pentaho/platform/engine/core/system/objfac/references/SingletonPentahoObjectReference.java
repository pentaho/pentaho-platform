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

import org.pentaho.platform.api.engine.ObjectFactoryException;

import java.util.Collections;
import java.util.Map;

/**
 * A simple wrapper Reference around an object. It will always return the same object that it was created with.
 * <p/>
 * Created by nbaker on 4/15/14.
 */
public class SingletonPentahoObjectReference<T> extends AbstractPentahoObjectReference<T> {

  private final T obj;

  public SingletonPentahoObjectReference( Class<T> type, T obj ) {
    this( type, obj, Collections.<String, Object>emptyMap(), 0 );

  }

  public SingletonPentahoObjectReference( Class<T> type, T obj, Map<String, Object> attributes, int priority ) {
    super( type, attributes, priority );
    this.obj = obj;
  }

  @Override protected T createObject() throws ObjectFactoryException {
    return this.obj;
  }

  /**
   * workaround for inheritance in Builders. Ideas taken from: https://weblogs.java
   * .net/blog/emcmanus/archive/2010/10/25/using-builder-pattern-subclasses
   */
  private abstract static class BuilderBase<T, B extends BuilderBase<T, B>>
    extends AbstractPentahoObjectReference.Builder<T, B> {
    T object;

    public B object( T object ) {
      this.object = object;
      return self();
    }

    @Override public SingletonPentahoObjectReference<T> build() {
      return new SingletonPentahoObjectReference<T>( this.type, this.object, this.attributes, this.priority );
    }
  }

  /**
   * Public builder, implementation specific methods should be in BuilderBase, only self() should be defined here
   */
  public static class Builder<T> extends BuilderBase<T, Builder<T>> {
    public Builder( Class<T> type ) {
      type( type );
    }

    @Override protected Builder<T> self() {
      return this;
    }
  }
}
