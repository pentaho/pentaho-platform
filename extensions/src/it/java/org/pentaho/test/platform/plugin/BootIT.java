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

import junit.framework.TestCase;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.boot.PlatformInitializationException;
import org.pentaho.platform.engine.services.solution.SolutionHelper;
import org.pentaho.platform.plugin.boot.PentahoBoot;
import org.pentaho.platform.repository2.unified.fs.FileSystemBackedUnifiedRepository;
import org.pentaho.test.platform.utils.TestResourceLocation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings( { "all" } )
public class BootIT extends TestCase {

  public void testBoot() throws PlatformInitializationException {
    PentahoBoot boot = new PentahoBoot();
    boot.setFilePath( TestResourceLocation.TEST_RESOURCES + "/solution" );

    boot.enableReporting();

    // create a user session
    IPentahoSession session = new StandaloneSession( "test" );
    PentahoSessionHolder.setSession( session );

    FileSystemBackedUnifiedRepository repo =
        (FileSystemBackedUnifiedRepository) PentahoSystem.get( IUnifiedRepository.class );
    repo.setRootDir( new File( TestResourceLocation.TEST_RESOURCES + "/solution" ) );

    boolean ok = boot.start();
    assertTrue( ok );

    String outputType = "pdf";
    OutputStream outputStream = null;
    try {
      // create an output stream to write the report into
      File outputFile = new File( "report." + outputType );
      outputStream = new FileOutputStream( outputFile );

      // pass the outputType parameter
      Map parameters = new HashMap();
      parameters.put( "output-type", outputType );
      SolutionHelper.execute( "test report", session, "boot/report.xaction", parameters, outputStream );
    } catch ( FileNotFoundException e ) {
      e.printStackTrace();
      return;
    } finally {
      if ( outputStream != null ) {
        try {
          // close the output stream
          outputStream.close();
        } catch ( IOException e ) {
          e.printStackTrace();
        }
      }
    }

    boot.stop();
  }

}
