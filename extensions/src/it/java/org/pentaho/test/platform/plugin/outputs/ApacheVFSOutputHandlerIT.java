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


package org.pentaho.test.platform.plugin.outputs;

import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.plugin.outputs.ApacheVFSOutputHandler;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.test.platform.utils.TestResourceLocation;

import java.io.File;

@SuppressWarnings( "nls" )
public class ApacheVFSOutputHandlerIT extends BaseTest {
  private static final String SOLUTION_PATH = TestResourceLocation.TEST_RESOURCES + "/outputs-solution";
  private static final String ALT_SOLUTION_PATH = TestResourceLocation.TEST_RESOURCES + "/outputs-solution";
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
