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

package org.pentaho.test.platform.plugin.outputs;

import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.plugin.outputs.ApacheVFSOutputHandler;
import org.pentaho.test.platform.engine.core.BaseTest;

import java.io.File;

@SuppressWarnings( "nls" )
public class ApacheVFSOutputHandlerTest extends BaseTest {
  private static final String SOLUTION_PATH = "test-src/outputs-solution";
  private static final String ALT_SOLUTION_PATH = "test-src/outputs-solution";
  private static final String PENTAHO_XML_PATH = "/system/pentahoObjects.spring.xml";

  @Override
  public String getSolutionPath() {
    File file = new File( SOLUTION_PATH + PENTAHO_XML_PATH );
    if ( file.exists() ) {
      return SOLUTION_PATH;
    } else {
      return ALT_SOLUTION_PATH;
    }
  }

  public void testAudit() {
    startTest();

    ApacheVFSOutputHandler handler = new ApacheVFSOutputHandler();
    IContentItem contentItem = handler.getFileOutputContentItem();
    System.out.println( "Content Item for VFS" + contentItem ); //$NON-NLS-1$  

    assertTrue( true );
    finishTest();
  }

}
