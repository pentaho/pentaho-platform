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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.database.IDatabaseDialect;
import org.pentaho.database.dialect.AbstractDatabaseDialect;
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
  @Mock AbstractDatabaseDialect unusableDatabaseDialect;
  @Mock IDatabaseType unusableDatabaseType;
  @Mock IDatabaseDialect unusableIDialect;
  @Mock IDatabaseType unusableIDialectType;
  @Mock AbstractDatabaseDialect usableDatabaseDialect;
  @Mock IDatabaseType usableDatabaseType;
  @Mock IDatabaseDialect usableIDialect;
  @Mock IDatabaseType usableIDialectType;
  List<IDatabaseDialect> databaseDialects;
  PentahoSystemDatabaseDialectProvider pentahoSystemDatabaseDialectProvider;

  @Before
  public void setup() {
    when( unusableDatabaseDialect.getDatabaseType() ).thenReturn( unusableDatabaseType );
    when( unusableDatabaseDialect.isUsable() ).thenReturn( false );
    when( usableDatabaseDialect.getDatabaseType() ).thenReturn( usableDatabaseType );
    when( usableDatabaseDialect.isUsable() ).thenReturn( true );
    when( unusableIDialect.getDatabaseType() ).thenReturn( unusableIDialectType );
    when( unusableIDialect.getNativeDriver() ).thenReturn( "fake.class" );
    when( usableIDialect.getDatabaseType() ).thenReturn( usableIDialectType );
    when( usableIDialect.getNativeDriver() ).thenReturn( Object.class.getCanonicalName() );
    Function<Class<IDatabaseDialect>, List<IDatabaseDialect>> dialectGetter = mock( Function.class );
    databaseDialects = new ArrayList<>(
      Arrays.asList( unusableDatabaseDialect, unusableIDialect, usableDatabaseDialect, usableIDialect ) );
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
    assertEquals( 2, dialects.size() );
    Iterator<IDatabaseDialect> iterator = dialects.iterator();
    assertEquals( usableDatabaseDialect, iterator.next() );
    assertEquals( usableIDialect, iterator.next() );
  }

  @Test
  public void testGetDialectsAll() {
    Collection<IDatabaseDialect> dialects = pentahoSystemDatabaseDialectProvider.getDialects( false );
    assertEquals( 4, dialects.size() );
    Iterator<IDatabaseDialect> iterator = dialects.iterator();
    assertEquals( unusableDatabaseDialect, iterator.next() );
    assertEquals( unusableIDialect, iterator.next() );
    assertEquals( usableDatabaseDialect, iterator.next() );
    assertEquals( usableIDialect, iterator.next() );
  }

  @Test
  public void testGetDialectUsableOnly() {
    assertEquals( usableDatabaseDialect, pentahoSystemDatabaseDialectProvider.getDialect( true, usableDatabaseType ) );
    assertEquals( usableIDialect, pentahoSystemDatabaseDialectProvider.getDialect( true, usableIDialectType ) );
    assertNull( pentahoSystemDatabaseDialectProvider.getDialect( true, unusableDatabaseType ) );
    assertNull( pentahoSystemDatabaseDialectProvider.getDialect( true, unusableIDialectType ) );
  }

  @Test
  public void testGetDialectAll() {
    assertEquals( usableDatabaseDialect, pentahoSystemDatabaseDialectProvider.getDialect( false, usableDatabaseType ) );
    assertEquals( usableIDialect, pentahoSystemDatabaseDialectProvider.getDialect( false, usableIDialectType ) );
    assertEquals( unusableDatabaseDialect,
      pentahoSystemDatabaseDialectProvider.getDialect( false, unusableDatabaseType ) );
    assertEquals( unusableIDialect,
      pentahoSystemDatabaseDialectProvider.getDialect( false, unusableIDialectType ) );
    assertNull( pentahoSystemDatabaseDialectProvider.getDialect( false, mock( IDatabaseType.class ) ) );
    assertNull( pentahoSystemDatabaseDialectProvider.getDialect( false, null ) );
  }
}
