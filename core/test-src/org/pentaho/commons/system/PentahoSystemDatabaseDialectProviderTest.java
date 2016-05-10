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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.database.IDatabaseDialect;
import org.pentaho.database.model.IDatabaseType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bryan on 5/10/16.
 */
@RunWith( MockitoJUnitRunner.class )
public class PentahoSystemDatabaseDialectProviderTest {
  @Mock IDatabaseDialect unusableDatabaseDialect;
  @Mock IDatabaseType unusableDatabaseType;
  @Mock IDatabaseDialect usableDatabaseDialect;
  @Mock IDatabaseType usableDatabaseType;
  List<IDatabaseDialect> databaseDialects;
  PentahoSystemDatabaseDialectProvider pentahoSystemDatabaseDialectProvider;

  @Before
  public void setup() {
    when( unusableDatabaseDialect.getDatabaseType() ).thenReturn( unusableDatabaseType );
    when( unusableDatabaseDialect.isUsable() ).thenReturn( false );
    when( usableDatabaseDialect.getDatabaseType() ).thenReturn( usableDatabaseType );
    when( usableDatabaseDialect.isUsable() ).thenReturn( true );
    Function<Class<IDatabaseDialect>, List<IDatabaseDialect>> dialectGetter = mock( Function.class );
    databaseDialects = new ArrayList<>( Arrays.asList( unusableDatabaseDialect, usableDatabaseDialect ) );
    when( dialectGetter.apply( IDatabaseDialect.class ) ).thenReturn( databaseDialects );
    pentahoSystemDatabaseDialectProvider = new PentahoSystemDatabaseDialectProvider( dialectGetter );
  }

  @Test
  public void testNoArgConstructor() {
    assertNotNull( new PentahoSystemDatabaseDialectProvider() );
  }

  @Test
  public void testGetDialectsUsableOnly() {
    Collection<IDatabaseDialect> dialects = pentahoSystemDatabaseDialectProvider.getDialects( true );
    assertEquals( 1, dialects.size() );
    assertEquals( usableDatabaseDialect, dialects.iterator().next() );
  }

  @Test
  public void testGetDialectsAll() {
    Collection<IDatabaseDialect> dialects = pentahoSystemDatabaseDialectProvider.getDialects( false );
    assertEquals( 2, dialects.size() );
    Iterator<IDatabaseDialect> iterator = dialects.iterator();
    assertEquals( unusableDatabaseDialect, iterator.next() );
    assertEquals( usableDatabaseDialect, iterator.next() );
  }

  @Test
  public void testGetDialectUsableOnly() {
    assertEquals( usableDatabaseDialect, pentahoSystemDatabaseDialectProvider.getDialect( true, usableDatabaseType ) );
    assertNull( pentahoSystemDatabaseDialectProvider.getDialect( true, unusableDatabaseType ) );
  }

  @Test
  public void testGetDialectAll() {
    assertEquals( usableDatabaseDialect, pentahoSystemDatabaseDialectProvider.getDialect( false, usableDatabaseType ) );
    assertEquals( unusableDatabaseDialect,
      pentahoSystemDatabaseDialectProvider.getDialect( false, unusableDatabaseType ) );
    assertNull( pentahoSystemDatabaseDialectProvider.getDialect( false, mock( IDatabaseType.class ) ) );
  }
}
