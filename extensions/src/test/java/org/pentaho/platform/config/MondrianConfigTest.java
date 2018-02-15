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

package org.pentaho.platform.config;

import org.junit.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by rfellows on 10/21/15.
 */
public class MondrianConfigTest {
  @Test
  public void testGettersAndSetters() throws Exception {
    assertThat( MondrianConfig.class, hasValidGettersAndSetters() );
  }

  @Test
  public void testConstructor() throws Exception {
    IMondrianConfig seed = mock( IMondrianConfig.class );
    when( seed.getLogFileLocation() ).thenReturn( "/home/users" );
    when( seed.getCacheHitCounters() ).thenReturn( true );
    when( seed.getQueryLimit() ).thenReturn( 39 );
    when( seed.getIgnoreInvalidMembers() ).thenReturn( false );
    when( seed.getResultLimit() ).thenReturn( 23 );
    when( seed.getTraceLevel() ).thenReturn( 1 );
    when( seed.getQueryTimeout() ).thenReturn( 400 );

    MondrianConfig config = new MondrianConfig( seed );
    assertEquals( seed.getCacheHitCounters(), config.getCacheHitCounters() );
    assertEquals( seed.getIgnoreInvalidMembers(), config.getIgnoreInvalidMembers() );
    assertEquals( seed.getLogFileLocation(), config.getLogFileLocation() );
    assertEquals( seed.getQueryLimit(), config.getQueryLimit() );
    assertEquals( seed.getQueryTimeout(), config.getQueryTimeout() );
    assertEquals( seed.getResultLimit(), config.getResultLimit() );
    assertEquals( seed.getTraceLevel(), config.getTraceLevel() );

  }
}
