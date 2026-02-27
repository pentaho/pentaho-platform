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
