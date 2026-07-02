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

import org.dom4j.Document;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.uifoundation.component.xml.PMDUIComponent;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.test.platform.utils.TestResourceLocation;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;

@SuppressWarnings( "nls" )
public class MetadataIT extends BaseTest {
  private static final String SOLUTION_PATH = TestResourceLocation.TEST_RESOURCES + "/solution";

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
    MetadataIT test = new MetadataIT();
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
