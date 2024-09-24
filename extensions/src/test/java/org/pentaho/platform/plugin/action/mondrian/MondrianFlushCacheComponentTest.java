/*!
 *
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
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

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
