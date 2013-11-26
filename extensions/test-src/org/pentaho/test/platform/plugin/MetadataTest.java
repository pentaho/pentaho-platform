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

import org.dom4j.Document;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.uifoundation.component.xml.PMDUIComponent;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.test.platform.engine.core.BaseTest;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;

@SuppressWarnings( "nls" )
public class MetadataTest extends BaseTest {
  private static final String SOLUTION_PATH = "test-src/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public Map getRequiredListeners() {
    Map listeners = super.getRequiredListeners();
    listeners.put( "metadata", "metadata" ); //$NON-NLS-1$ //$NON-NLS-2$
    return listeners;
  }

  public void testViewList() {
    startTest();

    IPentahoUrlFactory urlFactory = new SimpleUrlFactory( "" ); //$NON-NLS-1$

    PMDUIComponent component = new PMDUIComponent( urlFactory, new ArrayList() );
    StandaloneSession session =
        new StandaloneSession( Messages.getInstance().getString( "BaseTest.DEBUG_JUNIT_SESSION" ) ); //$NON-NLS-1$
    component.validate( session, null );
    component.setAction( PMDUIComponent.ACTION_LIST_MODELS );

    Document doc = component.getXmlContent();
    System.out.println( doc.asXML() );
    try {
      OutputStream outputStream = getOutputStream( "MetadataTest.testViewList", ".xml" ); //$NON-NLS-1$//$NON-NLS-2$
      outputStream.write( doc.asXML().getBytes() );
    } catch ( IOException e ) {
      //ignore
    }
    finishTest();
  }

  public void __testLoadView() {
    startTest();

    IPentahoUrlFactory urlFactory = new SimpleUrlFactory( "" ); //$NON-NLS-1$

    PMDUIComponent component = new PMDUIComponent( urlFactory, new ArrayList() );
    StandaloneSession session =
        new StandaloneSession( Messages.getInstance().getString( "BaseTest.DEBUG_JUNIT_SESSION" ) ); //$NON-NLS-1$
    component.validate( session, null );
    component.setAction( PMDUIComponent.ACTION_LOAD_MODEL );
    component.setDomainName( "test" ); //$NON-NLS-1$
    component.setModelId( "Orders" ); //$NON-NLS-1$

    Document doc = component.getXmlContent();
    System.out.println( doc.asXML() );
    try {
      OutputStream outputStream = getOutputStream( "MetadataTest.testLoadView", ".xml" ); //$NON-NLS-1$//$NON-NLS-2$
      outputStream.write( doc.asXML().getBytes() );
    } catch ( IOException e ) {
      //ignore
    }
    finishTest();
  }

  public void __testLookup() {
    startTest();

    IPentahoUrlFactory urlFactory = new SimpleUrlFactory( "" ); //$NON-NLS-1$

    PMDUIComponent component = new PMDUIComponent( urlFactory, new ArrayList() );
    StandaloneSession session =
        new StandaloneSession( Messages.getInstance().getString( "BaseTest.DEBUG_JUNIT_SESSION" ) ); //$NON-NLS-1$
    component.validate( session, null );
    component.setAction( PMDUIComponent.ACTION_LOOKUP );
    component.setDomainName( "test" ); //$NON-NLS-1$
    component.setModelId( "Orders" ); //$NON-NLS-1$
    component.setColumnId( "BC_CUSTOMERS_CUSTOMERNAME" ); //$NON-NLS-1$

    Document doc = component.getXmlContent();
    System.out.println( doc.asXML() );
    try {
      OutputStream outputStream = getOutputStream( "MetadataTest.testLoadView", ".xml" ); //$NON-NLS-1$//$NON-NLS-2$
      outputStream.write( doc.asXML().getBytes() );
    } catch ( IOException e ) {
      //ignore
    }
    finishTest();
  }

  public static void main( String[] args ) {
    MetadataTest test = new MetadataTest();
    test.setUp();
    try {
      test.testViewList();
      test.__testLoadView();
      test.__testLookup();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }

}
