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


package org.pentaho.test.platform.plugin;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.pentaho.platform.plugin.condition.javascript.RhinoScriptable;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.test.platform.utils.TestResourceLocation;

/**
 * @author Michael D'Amour
 * 
 */
@SuppressWarnings( "nls" )
public class RhinoIT extends BaseTest {
  private static final String SOLUTION_PATH = TestResourceLocation.TEST_RESOURCES + "/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public void testRhino() {
    // Creates and enters a Context. The Context stores information
    // about the execution environment of a script.
    Context cx = ContextFactory.getGlobal().enterContext();
    try {
      // Initialize the standard objects (Object, Function, etc.)
      // This must be done before scripts can be executed. Returns
      // a scope object that we use in later calls.
      Scriptable scope = new RhinoScriptable();
      scope.getClassName();
      // Collect the arguments into a single string.
      String[] args = { "var s=5;", "s++;", "s+=2;", "s = \'" + scope.getClassName() + "\'" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
      String s = ""; //$NON-NLS-1$
      for ( int i = 0; i < args.length; i++ ) {
        s += args[i];
      }
      // Now evaluate the string we've colected.
      Object result = cx.evaluateString( scope, s, "<cmd>", 1, null ); //$NON-NLS-1$
      // Convert the result to a string and print it.
      System.err.println( Context.toString( result ) );
    } finally {
      // Exit from the context.
      Context.exit();
    }
  }

  public static void main( String[] args ) {
    RhinoIT test = new RhinoIT();
    test.setUp();
    try {
      test.testRhino();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }
}
