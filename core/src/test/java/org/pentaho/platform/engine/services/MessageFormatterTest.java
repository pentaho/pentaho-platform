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
 * Copyright 2017 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.services;

import mockit.Mock;
import mockit.MockUp;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.repository.IRuntimeElement;
import org.pentaho.platform.api.repository.IRuntimeRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.security.SecurityParameterProvider;
import org.pentaho.platform.engine.services.actionsequence.ActionParameter;
import org.pentaho.platform.engine.services.runtime.RuntimeContext;
import org.pentaho.platform.engine.services.runtime.SimpleRuntimeElement;
import org.pentaho.platform.util.JVMParameterProvider;
import org.pentaho.platform.util.UUIDUtil;
import org.pentaho.platform.util.web.SimpleUrlFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Andrei Abramov
 */
public class MessageFormatterTest {

  IRuntimeRepository mockedRuntimeRepository;
  ISolutionEngine mockedSolutionEngine;
  MockUp<PentahoSystem> mockedPentahoSystem;

  private final IPentahoUrlFactory urlFactory = new SimpleUrlFactory( "" );
  private final IPentahoSession session = new StandaloneSession( "system" );

  private IRuntimeContext runtimeCtx;

  @Before
  public void before() {
    mockedRuntimeRepository = mock( IRuntimeRepository.class );
    mockedSolutionEngine = mock( ISolutionEngine.class );
    mockedPentahoSystem = new MockUp<PentahoSystem>() {

      @Mock
      public IApplicationContext getApplicationContext() {
        final String solutionPath = ".";
        final String applicationPath = "";
        return new StandaloneApplicationContext( solutionPath, applicationPath );
      }
    };

    runtimeCtx = spy( new RuntimeContext( "id", mockedSolutionEngine, "solutionName",
      makeRuntimeData( session ), session, null, "processId", urlFactory,
      makeParameterProviders( session ), new ArrayList<String>(), null ) );
  }

  @Test
  public void formatSuccessMessage() throws Exception {

    Set inputNames = new HashSet<String>(  );
    inputNames.add( "Test" );

    IActionParameter actionParameter = new ActionParameter( "Test", "Test", "<img%20src=\"http://www.pentaho"
      + ".com/sites/all/themes/pentaho_resp/logo.svg\"%20/>", null, "" );

    when( runtimeCtx.getOutputNames() ).thenReturn( inputNames );
    doReturn( actionParameter ).when( runtimeCtx ).getOutputParameter( anyString() );

    MessageFormatter mf = new MessageFormatter();
    StringBuffer messageBuffer = new StringBuffer();
    mf.formatSuccessMessage( MessageFormatter.HTML_MIME_TYPE, runtimeCtx, messageBuffer, false );
    assertEquals( "<html><head><title>Pentaho BI Platform - Start Action</title><link rel=\"stylesheet\" "
      + "type=\"text/css\" href=\"/pentaho-style/active/default.css\"></head><body dir=\"LTR\"><table "
      + "cellspacing=\"10\"><tr><td class=\"portlet-section\" colspan=\"3\">Action Successful<hr "
      + "size=\"1\"/></td></tr><tr><td class=\"portlet-font\" valign=\"top\">Test=&lt;img%20src=&quot;http://www"
      + ".pentaho.com/sites/all/themes/pentaho_resp/logo.svg&quot;%20/&gt;<br/></td></tr></table></body></html>",
      messageBuffer.toString() );
  }

  private Map<String, IParameterProvider> makeParameterProviders( final IPentahoSession session ) {
    final Map<String, IParameterProvider> res = new HashMap<>();

    res.put( "jvm", new JVMParameterProvider() );
    res.put( SecurityParameterProvider.SCOPE_SECURITY, new SecurityParameterProvider( session ) );

    return res;
  }

  private IRuntimeElement makeRuntimeData( final IPentahoSession session ) {
    return new SimpleRuntimeElement( UUIDUtil.getUUIDAsString(), session.getId( ), IParameterProvider.SCOPE_SESSION );
  }

}