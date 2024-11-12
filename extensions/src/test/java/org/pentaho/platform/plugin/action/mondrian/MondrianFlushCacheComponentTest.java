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


package org.pentaho.platform.plugin.action.mondrian;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.plugin.action.mdx.MDXLookupRule;
import org.pentaho.platform.plugin.services.connections.mondrian.MDXConnection;

import mondrian.olap.CacheControl;
import mondrian.olap.Connection;
import mondrian.olap.Cube;
import mondrian.olap.Schema;

/**
 * @author William E. Seyler
 */

public class MondrianFlushCacheComponentTest {

  MondrianFlushCacheComponent mfcc;
  MDXLookupRule rule;
  MDXConnection mdxConn;
  Connection conn;
  CacheControl cacheControl;
  Schema schema;
  Cube[] cubes = new Cube[2];

  @Before
  public void setUp() throws Exception {
    mfcc = spy( new MondrianFlushCacheComponent() );
    rule = spy( new MDXLookupRule() );
    mdxConn = spy( new MDXConnection() );
    doReturn( rule ).when( mfcc ).getLookupRule();
    conn = mock( Connection.class );
    doReturn( mdxConn ).when( rule ).shareConnection();
    doReturn( conn ).when( mdxConn ).getConnection();
    cacheControl = mock( CacheControl.class );
    doReturn( cacheControl ).when( conn ).getCacheControl( null );
    schema = mock( Schema.class );
    doReturn( schema ).when( conn ).getSchema();
    cubes[0] = mock( Cube.class );
    cubes[1] = mock( Cube.class );
    doReturn( cubes ).when( schema ).getCubes();
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testGetLogger() {
    assertNotNull( mfcc.getLogger() );
  }

  @Test
  public void testExecuteAction() {
    assertEquals( true, mfcc.executeAction() );
  }

  @Test
  public void testValidateAction() {
    assertEquals( true, mfcc.validateAction() );
  }

  @Test
  public void testValidateSystemSettings() {
    assertEquals( true, mfcc.validateSystemSettings() );
  }

  @Test
  public void testInit() {
    assertEquals( true, mfcc.init() );
  }

  @Test
  public void testGetLookupRule() {
    assertEquals( rule, mfcc.getLookupRule() );
  }
}
