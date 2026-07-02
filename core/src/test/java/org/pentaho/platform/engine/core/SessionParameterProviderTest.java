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


package org.pentaho.platform.engine.core;

import junit.framework.TestCase;
import org.pentaho.platform.engine.core.solution.PentahoSessionParameterProvider;
import org.pentaho.platform.engine.core.system.StandaloneSession;

@SuppressWarnings( { "all" } )
public class SessionParameterProviderTest extends TestCase {

  public void test1() {

    StandaloneSession session = new StandaloneSession( "test name" );

    PentahoSessionParameterProvider params = new PentahoSessionParameterProvider( session );

    session.setAttribute( "param1", "value1" );

    assertEquals( "Wrong param value", "test name", params.getParameter( "name" ) );
    assertEquals( "Wrong param value", "value1", params.getParameter( "param1" ) );
    assertEquals( "Wrong param value", "value1", params.getStringParameter( "param1", null ) );
    assertEquals( "Wrong param value", null, params.getStringParameter( "bogus", null ) );

  }

}
