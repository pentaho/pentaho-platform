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

import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.actionsequence.OutputDef;
import org.pentaho.platform.engine.services.solution.SolutionCompare;
import org.pentaho.test.platform.engine.core.BaseTest;

import java.io.FileOutputStream;
import java.util.ArrayList;

//import java.math.BigDecimal;
//import java.util.Date;
//import java.util.Map;
//import org.pentaho.platform.engine.services.solution.SolutionEngineAgent;

@SuppressWarnings( "nls" )
public class SolutionTest extends BaseTest {
  private static final String SOLUTION_PATH = "test-src/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public void testSolutionCompare() {
    startTest();

    SolutionCompare compare = new SolutionCompare();
    String path1 = PentahoSystem.getApplicationContext().getSolutionPath( "test/platform" ); //$NON-NLS-1$ 
    String path2 = PentahoSystem.getApplicationContext().getSolutionPath( "test/dashboard" ); //$NON-NLS-1$
    compare.compare( path1, path2 );

    assertTrue( true );
    finishTest();
  }

  public void testSolutionCompare2() {
    startTest();

    SolutionCompare compare = new SolutionCompare();
    String path1 = PentahoSystem.getApplicationContext().getSolutionPath( "test/charts/areachart_data.xaction" ); //$NON-NLS-1$ 
    String path2 = PentahoSystem.getApplicationContext().getSolutionPath( "test/dashboard" ); //$NON-NLS-1$
    compare.compare( path1, path2 );

    assertTrue( true );
    finishTest();
  }

  public void testOutputDef() {
    startTest();
    try {
      FileOutputStream outputStream = new FileOutputStream( "c:/test.txt" ); //$NON-NLS-1$
      OutputDef outputDef1 = new OutputDef( "FirstOutputName", outputStream ); //$NON-NLS-1$
      outputDef1.setValue( "MyOutputDefinition" ); //$NON-NLS-1$
      assertEquals( outputDef1.getName(), "FirstOutputName" ); //$NON-NLS-1$
      OutputDef outputDef2 = new OutputDef( "SecondOutputName", new ArrayList() ); //$NON-NLS-1$
      assertEquals( outputDef2.getName(), "SecondOutputName" ); //$NON-NLS-1$      
      OutputDef outputDef3 = new OutputDef( "ThirdOutputName", "MyOutputType" ); //$NON-NLS-1$ //$NON-NLS-2$
      assertEquals( outputDef3.getName(), "ThirdOutputName" ); //$NON-NLS-1$

    } catch ( Exception e ) {
      e.printStackTrace();
    }
    assertTrue( true );
    finishTest();
  }
}
