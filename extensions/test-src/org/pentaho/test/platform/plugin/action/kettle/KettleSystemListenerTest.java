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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.test.platform.plugin.action.kettle;

import org.apache.log4j.FileAppender;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.kettle.KettleSystemListener;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class KettleSystemListenerTest {
  private IApplicationContext mockApplicationContext;
  private FileAppender fileAppender = new FileAppender();

  @Before
  public void setup() {
    mockApplicationContext = mock( IApplicationContext.class );
    org.apache.log4j.Logger.getRootLogger().addAppender( fileAppender );
    PentahoSystem.setApplicationContext( mockApplicationContext );
  }

  @After
  public void teardown() {
    org.apache.log4j.Logger.getRootLogger().removeAppender( fileAppender );
  }

  @Test
  public void testStartup() {
    KettleSystemListener ksl = new KettleSystemListener();
    assertTrue( ksl.startup( null ) );
  }
}
