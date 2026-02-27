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


package org.pentaho.platform.core.mt;

import org.junit.Test;

import static org.junit.Assert.*;

public class TenantTest {

  @Test
  public void tenantTest() {
    Tenant t = new Tenant();
    assertNull( t.getRootFolderAbsolutePath() );
    assertNull( t.getId() );
    assertNull( t.getName() );
    assertTrue( t.isEnabled() );
    assertEquals( t.toString(), "TENANT ID = null" );

    t.setRootFolderAbsolutePath( "C:/TestPath" );
    assertEquals( t.getRootFolderAbsolutePath(), "C:/TestPath" );
    assertEquals( t.getId(), "C:/TestPath" );
    assertEquals( t.getName(), "TestPath" );
    t.setEnabled( false );
    assertFalse( t.isEnabled() );
    assertEquals( t.toString(), "TENANT ID = C:/TestPath" );
    assertEquals( t.hashCode(), 31 * "C:/TestPath".hashCode() );
  }

  @Test
  public void tenantTestEquals() {
    Tenant t = new Tenant( "C:/TestPath", true );
    assertFalse( t.equals( null ) );
    assertFalse( t.equals( "C:/TestPath" ) );
    Tenant t2 = new Tenant( "C:/TestPath2", true );
    assertFalse( t.equals( t2 ) );
    t2.setRootFolderAbsolutePath( t.getRootFolderAbsolutePath() );
    assertTrue( t.equals( t2 ) );
    assertTrue( t.equals( t ) );
  }
}
