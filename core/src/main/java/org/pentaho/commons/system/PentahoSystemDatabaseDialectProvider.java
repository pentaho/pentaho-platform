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

  private Stream<IDatabaseDialect> getDialectStream() {
    return dialectGetter.apply( IDatabaseDialect.class ).stream();
  }

  private Stream<IDatabaseDialect> filterUsableDialects( Stream<IDatabaseDialect> stream, boolean usableOnly ) {
    if ( usableOnly ) {
      return stream.filter( dialect -> {
        if ( dialect instanceof IDriverLocator ) {
          return ( (IDriverLocator) dialect ).isUsable();
        } else {
          return ClassUtil.canLoadClass( dialect.getNativeDriver() );
        }
      } );
    }
    return stream;
  }

  /**
   * Returns collection of database dialects registered to the Hitachi Vantara System.
   *
   * @param usableOnly
   * @return dialects collection
   */
  @Override public Collection<IDatabaseDialect> getDialects( boolean usableOnly ) {
    return filterUsableDialects( getDialectStream(), usableOnly ).collect( Collectors.toList() );
  }

  /**
   * Returns database dialect registered to the Pentaho System for specified database type.
   *
   * @param usableOnly
   * @param databaseType
   * @return database dialect or null.
   */
  @Override public IDatabaseDialect getDialect( boolean usableOnly, IDatabaseType databaseType ) {
    if ( databaseType == null ) {
      return null;
    }

    Stream<IDatabaseDialect> dialects = getDialectStream().filter( dialect -> dialect.getDatabaseType().equals( databaseType ) );
    return filterUsableDialects( dialects, usableOnly ).findFirst().orElse( null );
  }
}
