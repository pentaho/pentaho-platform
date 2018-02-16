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

package org.pentaho.platform.engine.services;

import java.util.ArrayList;
import java.util.HashMap;

import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.test.platform.engine.core.BaseTest;

@SuppressWarnings( { "all" } )
public class MultiOutputTest extends BaseTest {
  private static final String SOLUTION_PATH = "src/test/resources/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public void testMultiOutput() {

    startTest();
    ISolutionEngine solutionEngine = ServiceTestHelper.getSolutionEngine();
    String xactionStr = ServiceTestHelper.getXAction( SOLUTION_PATH, "services/MultiOutputTest.xaction" );
    IRuntimeContext runtimeContext =
        solutionEngine.execute( xactionStr, "test1a.xaction", "empty action sequence test", false, true, null, false, //$NON-NLS-1$ //$NON-NLS-2$
            new HashMap(), null, null, new SimpleUrlFactory( "" ), new ArrayList() );
    finishTest();

  }

}
