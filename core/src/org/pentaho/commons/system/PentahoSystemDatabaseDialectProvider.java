/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.commons.system;

import org.pentaho.database.IDatabaseDialect;
import org.pentaho.database.IDatabaseDialectProvider;
import org.pentaho.database.IDriverLocator;
import org.pentaho.database.model.IDatabaseType;
import org.pentaho.database.util.ClassUtil;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by bryan on 5/6/16.
 */
public class PentahoSystemDatabaseDialectProvider implements IDatabaseDialectProvider {
  private final Function<Class<IDatabaseDialect>, List<IDatabaseDialect>> dialectGetter;

  public PentahoSystemDatabaseDialectProvider() {
    this.dialectGetter = PentahoSystem::getAll;
  }

  public PentahoSystemDatabaseDialectProvider( Function<Class<IDatabaseDialect>, List<IDatabaseDialect>> dialectGetter ) {
    this.dialectGetter = dialectGetter;
  }

  private Stream<IDatabaseDialect> getDialectStream( boolean usableOnly, IDatabaseType databaseType ) {
    Stream<IDatabaseDialect> stream = dialectGetter.apply( IDatabaseDialect.class ).stream();
    if ( databaseType != null ) {
      stream = stream.filter( dialect -> dialect.getDatabaseType().equals( databaseType ) );
    }
    if ( usableOnly ) {
      stream = stream.filter( dialect -> {
        if ( dialect instanceof IDriverLocator ) {
          return ( (IDriverLocator) dialect ).isUsable();
        } else {
          return ClassUtil.canLoadClass( dialect.getNativeDriver() );
        }
      } );
    }
    return stream;
  }

  @Override public Collection<IDatabaseDialect> getDialects( boolean usableOnly ) {
    return getDialectStream( usableOnly, null ).collect( Collectors.toList() );
  }

  @Override public IDatabaseDialect getDialect( boolean usableOnly, IDatabaseType iDatabaseType ) {
    return getDialectStream( usableOnly, iDatabaseType ).findFirst().orElse( null );
  }
}
