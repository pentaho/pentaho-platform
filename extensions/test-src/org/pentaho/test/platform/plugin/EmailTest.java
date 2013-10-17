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

package org.pentaho.test.platform.plugin;

import com.dumbster.smtp.SimpleSmtpServer;
import org.junit.Ignore;
import org.pentaho.test.platform.engine.core.BaseTestCase;

@SuppressWarnings( "nls" )
@Ignore
public class EmailTest extends BaseTestCase {

  private SimpleSmtpServer smtpServer;

  public EmailTest() {
    super( SOLUTION_PATH );
  }

  private static final String SOLUTION_PATH = "test-src/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public void setUp() throws Exception {
    super.setUp();
    smtpServer = SimpleSmtpServer.start();
  }

  public void tearDown() throws Exception {
    try {
      smtpServer.stop();
    } catch ( NullPointerException ex ) {
      // ignored.
    }
    super.tearDown();
  }
}
