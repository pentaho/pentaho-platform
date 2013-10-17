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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings( { "all" } )
public class BootTest extends TestCase {

  public void testBoot() throws PlatformInitializationException {
    PentahoBoot boot = new PentahoBoot();
    boot.setFilePath( "test-src/solution" );

    boot.enableReporting();

    // create a user session
    IPentahoSession session = new StandaloneSession( "test" );
    PentahoSessionHolder.setSession( session );

    FileSystemBackedUnifiedRepository repo =
        (FileSystemBackedUnifiedRepository) PentahoSystem.get( IUnifiedRepository.class );
    repo.setRootDir( new File( "test-src/solution" ) );

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
