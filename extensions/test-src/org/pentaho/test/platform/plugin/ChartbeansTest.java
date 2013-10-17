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

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory.Scope;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.services.solution.SolutionEngine;
import org.pentaho.platform.plugin.action.chartbeans.DefaultChartBeansGenerator;
import org.pentaho.platform.plugin.action.chartbeans.IChartBeansGenerator;
import org.pentaho.platform.repository2.unified.fs.FileSystemBackedUnifiedRepository;
import org.pentaho.platform.uifoundation.component.ActionComponent;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.test.platform.engine.core.MicroPlatform;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/* This test required SampleData to be running
 * 
 */

@SuppressWarnings( "nls" )
public class ChartbeansTest {
  @SuppressWarnings( "serial" )
  protected class ChartbeansActionComponent extends ActionComponent {

    public ChartbeansActionComponent( String actionString, String instanceId, int outputPreference,
        IPentahoUrlFactory urlFactory, List messages ) {
      super( actionString, instanceId, outputPreference, urlFactory, messages );
    }

    public byte[] getContentBytes() {
      return super.getContentAsStream( null ).toByteArray();
    }
  }

  private MicroPlatform microPlatform;

  StandaloneSession session;

  @Before
  public void init0() {
    microPlatform = new MicroPlatform( "test-src/solution/" );
    microPlatform.define( ISolutionEngine.class, SolutionEngine.class );
    microPlatform.define( IChartBeansGenerator.class, DefaultChartBeansGenerator.class );
    microPlatform.define( IUnifiedRepository.class, FileSystemBackedUnifiedRepository.class, Scope.GLOBAL );
    FileSystemBackedUnifiedRepository repo =
        (FileSystemBackedUnifiedRepository) PentahoSystem.get( IUnifiedRepository.class );
    repo.setRootDir( new File( "test-src/solution" ) );

    session = new StandaloneSession();
  }

  @SuppressWarnings( "deprecation" )
  @Test
  public void testChartbeansActionComponentOFC() {
    microPlatform.init();

    IPentahoUrlFactory urlFactory = new SimpleUrlFactory( "" );
    ArrayList messages = new ArrayList();
    SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
    parameterProvider.setParameter( "solution", "test" ); //$NON-NLS-1$ //$NON-NLS-2$
    parameterProvider.setParameter( "path", "chartbeans" ); //$NON-NLS-1$ //$NON-NLS-2$
    parameterProvider.setParameter( "action", "Chartbeans_All_Inclusive_OFC.xaction" ); //$NON-NLS-1$ //$NON-NLS-2$
    ActionComponent component =
        new ActionComponent(
            "test/chartbeans/Chartbeans_All_Inclusive_OFC.xaction", null, IOutputHandler.OUTPUT_TYPE_DEFAULT, urlFactory, messages ); //$NON-NLS-1$
    component.setParameterProvider( IParameterProvider.SCOPE_REQUEST, parameterProvider );
    component.validate( session, null );
    OutputStream outputStream = getOutputStream( "Chartbeans.testChartbeansAllInclusive_OFC", ".html" ); //$NON-NLS-1$ //$NON-NLS-2$

    String result = component.getContent( "text/html" ); //$NON-NLS-1$

    try {
      outputStream.write( result.getBytes() );

      assertTrue( result.startsWith( "<html><head>" ) ); //$NON-NLS-1$
      assertTrue( result.indexOf( "function getChartData" ) > 0 ); //$NON-NLS-1$
      assertTrue( result.indexOf( "/*JSON*/" ) > 0 ); //$NON-NLS-1$
      assertTrue( result.indexOf( "/*END_JSON*/" ) > 0 ); //$NON-NLS-1$
      assertTrue( result.indexOf( "</body></html>" ) > 0 ); //$NON-NLS-1$
    } catch ( Exception e ) {
      assertTrue( "An exception has been thrown: " + e.getMessage(), false ); //$NON-NLS-1$
    }
  }

  protected OutputStream getOutputStream( String testName, String extension ) {
    OutputStream outputStream = null;
    try {
      String tmpDir = PentahoSystem.getApplicationContext().getFileOutputPath( "test/tmp" ); //$NON-NLS-1$
      File file = new File( tmpDir );
      file.mkdirs();
      String path = PentahoSystem.getApplicationContext().getFileOutputPath( "test/tmp/" + testName + extension ); //$NON-NLS-1$
      outputStream = new FileOutputStream( path );
    } catch ( FileNotFoundException e ) {
      //ignore
    }
    return outputStream;
  }

  protected File getOriginalFile( String fileName ) {
    return new File(
        System.getProperty( "user.dir" ) + File.separator + "test-src" + File.separator + "solution" + File.separator + "test" + File.separator + "chartbeans" + //$NON-NLS-1$//$NON-NLS-2$
            File.separator + "results" + File.separator + fileName ); //$NON-NLS-1$
  }

}
