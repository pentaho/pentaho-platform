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
