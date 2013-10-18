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

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.pentaho.platform.plugin.condition.javascript.RhinoScriptable;
import org.pentaho.test.platform.engine.core.BaseTest;

/**
 * @author Michael D'Amour
 * 
 */
@SuppressWarnings( "nls" )
public class RhinoTest extends BaseTest {
  private static final String SOLUTION_PATH = "test-src/solution";

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
    RhinoTest test = new RhinoTest();
    test.setUp();
    try {
      test.testRhino();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }
}
