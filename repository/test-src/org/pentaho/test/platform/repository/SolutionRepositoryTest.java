/*
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
 * Copyright 2005-2008 Pentaho Corporation.  All rights reserved. 
 * 
 * Created Jan 25, 2006 
 * @author wseyler
 */

package org.pentaho.test.platform.repository;

import java.io.File;
//import java.io.OutputStream;
import java.util.Properties;

//import org.pentaho.platform.api.repository.ISolutionRepository;
//import org.pentaho.platform.engine.core.system.PentahoSystem;

@SuppressWarnings("nls")
public class SolutionRepositoryTest extends RepositoryTestCase {
  private StringBuffer longString = new StringBuffer();

  private static final String SOLUTION_PATH = "projects/actions/test-src/solution";

  private static final String ALT_SOLUTION_PATH = "test-src/solution";

  private static final String PENTAHO_XML_PATH = "/system/pentaho.xml";

  public String getSolutionPath() {
    File file = new File(SOLUTION_PATH + PENTAHO_XML_PATH);
    if (file.exists()) {
      System.out.println("File exist returning " + SOLUTION_PATH);
      return SOLUTION_PATH;
    } else {
      System.out.println("File does not exist returning " + ALT_SOLUTION_PATH);
      return ALT_SOLUTION_PATH;
    }
  }
  
  public SolutionRepositoryTest(String arg0) {
    super(arg0);
    Properties props = System.getProperties();
    longString.append(props.getProperty("java.home")).append(props.getProperty("sun.cpu.isalist")). //$NON-NLS-1$ //$NON-NLS-2$
        append(props.getProperty("java.vm.version")).append(props.getProperty("user.home")). //$NON-NLS-1$ //$NON-NLS-2$
        append(props.getProperty("java.class.path")); //$NON-NLS-1$
  }

//  public void testSolutionRepository() {
//    ISolutionRepository repository = PentahoSystem.getSolutionRepository(getPentahoSession());
//
//    repository.removeSolutionFile("samples", "", "charts"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
//
//    OutputStream output = getOutputStream("SolutionRepositoryTest.testSolutionRepository", ".txt"); //$NON-NLS-1$ //$NON-NLS-2$
//    try {
//      output.write(repository.getSolutions(ISolutionRepository.ACTION_EXECUTE).asXML().getBytes());
//      output.write('\n');
//      output.write(repository.getSolutionStructure(ISolutionRepository.ACTION_EXECUTE).asXML().getBytes());
//      output.write('\n');
//      output.write(repository.getSolutionTree(ISolutionRepository.ACTION_EXECUTE).asXML().getBytes());
//      output.write('\n');
//    } catch (Exception e) {
//    }
//
//  }

// TODO: remove once tests are running  
  public void setUp() {}
  public void tearDown() {}
  public void testDummyTest() {}
    
  
  public static void main(String[] args) {
    SolutionRepositoryTest test = new SolutionRepositoryTest("testSolutionRepository"); //$NON-NLS-1$
    junit.textui.TestRunner.run(test);
    System.exit(0);
  }

}
