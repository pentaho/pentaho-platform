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

package org.pentaho.test.platform.web.ui.component;

import org.dom4j.Document;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.commons.connection.memory.MemoryResultSet;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.uifoundation.component.FilterDefinition;
import org.pentaho.platform.uifoundation.component.xml.FilterPanel;
import org.pentaho.platform.uifoundation.component.xml.FilterPanelException;
import org.pentaho.platform.util.logging.SimpleLogger;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
import org.pentaho.platform.web.http.request.HttpRequestParameterProvider;
import org.pentaho.platform.web.http.session.HttpSessionParameterProvider;
import org.pentaho.test.platform.engine.core.BaseTest;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@SuppressWarnings( "nls" )
public class FilterDefinitionTest extends BaseTest {
  private static final String SOLUTION_PATH = "test-src/web-solution";

  private static final String ALT_SOLUTION_PATH = "test-src/web-solution";

  private static final String PENTAHO_XML_PATH = "/system/pentaho.xml";

  public String getSolutionPath() {
    File file = new File( SOLUTION_PATH + PENTAHO_XML_PATH );
    if ( file.exists() ) {
      return SOLUTION_PATH;
    } else {
      return ALT_SOLUTION_PATH;
    }
  }

  public void testFilterCreation() {
    // IApplicationContext appCtx = PentahoSystem.getApplicationContext();

    try {
      Class.forName( "net.sf.cglib.transform.ClassFilter" ); //$NON-NLS-1$
    } catch ( ClassNotFoundException e1 ) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

    ILogger l = new SimpleLogger( this );
    IPentahoSession session = getSession();
    setGlobalParams();
    this.sessionStartup( session );
    Document doc = null;
    try {
      doc = XmlDom4JHelper.getDocFromFile( new File( SOLUTION_PATH + "/test/filterPanel/test.filterpanel.xml" ), null ); //$NON-NLS-1$
    } catch ( Exception ee ) {
      ee.printStackTrace();
      assertTrue( "Failed to get the document from a file.", false ); //$NON-NLS-1$
    }
    FilterPanel fp = null;
    try {
      fp = new FilterPanel( session, doc, l );
    } catch ( FilterPanelException e ) {
      e.printStackTrace();
      assertTrue( "Failed to create stream from document.", false ); //$NON-NLS-1$
    }

    Map parameterProviders = getParameterProviders();
    boolean success = false;
    List filters = fp.getFilters();
    FilterDefinition fd = null;
    fd = (FilterDefinition) filters.get( 0 );
    success = fd.populate( parameterProviders, new String[] { "huh" } ); //$NON-NLS-1$
    assertTrue( "Populate on filter session-attribute failed", success ); //$NON-NLS-1$

    fd = (FilterDefinition) filters.get( 1 );
    success = fd.populate( parameterProviders, new String[] { "huh" } ); //$NON-NLS-1$
    assertTrue( "Populate on filter global-attribute failed", success ); //$NON-NLS-1$

    fd = (FilterDefinition) filters.get( 2 );
    success = fd.populate( parameterProviders, new String[] { "huh" } ); //$NON-NLS-1$
    assertTrue( "Populate on filter static-lov failed", success ); //$NON-NLS-1$

    // TODO sbarkdull, figure out why we die in the HibernateUtil code, and re-enable this
    /*
     * fd = (FilterDefinition)filters.get( 3 ); success = fd.populate(parameterProviders, new String[]{"huh"} );
     * assertTrue( "Populate on filter action sequence failed", success );
     */
  }

  private IPentahoResultSet getFakeResultSet() {
    List hdr = new LinkedList();
    hdr.add( "customername" ); //$NON-NLS-1$
    hdr.add( "customernumber" ); //$NON-NLS-1$
    List data = new LinkedList();
    List row = new LinkedList();
    row.add( "contents of 0,0" ); //$NON-NLS-1$
    row.add( "contents of 0,1" ); //$NON-NLS-1$
    data.add( row );

    row = new LinkedList();
    row.add( "contents of 1,0" ); //$NON-NLS-1$
    row.add( "contents of 1,1" ); //$NON-NLS-1$
    data.add( row );

    return MemoryResultSet.createFromLists( hdr, data );
  }

  private IPentahoSession getSession() {
    IPentahoResultSet rs = getFakeResultSet();
    IPentahoSession session = new StandaloneSession( "REPOSTEST.JUNIT_TEST_SESSION" ); //$NON-NLS-1$
    session.setAttribute( "customerNamesList", rs ); //$NON-NLS-1$

    // IPentahoResultSet data = (IPentahoResultSet) PentahoSystem.getGlobalParameters().getParameter(listSource);

    return session;
  }

  private void setGlobalParams() {
    IPentahoResultSet rs = getFakeResultSet();
    SimpleParameterProvider spp = (SimpleParameterProvider) PentahoSystem.getGlobalParameters();
    spp.setParameter( "customerNamesList", rs ); //$NON-NLS-1$
  }

  private Map getParameterProviders() {
    SimpleParameterProvider simplePP = new SimpleParameterProvider();
    simplePP.setParameter( "empty", "empty" ); //$NON-NLS-1$ //$NON-NLS-2$
    Map parameterProviders = new HashMap();
    parameterProviders.put( HttpSessionParameterProvider.SCOPE_SESSION, simplePP );

    simplePP = new SimpleParameterProvider();
    simplePP.setParameter( "empty", "empty" ); //$NON-NLS-1$ //$NON-NLS-2$
    parameterProviders.put( HttpRequestParameterProvider.SCOPE_REQUEST, simplePP );

    return parameterProviders;
  }
}
