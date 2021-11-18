/*!
 *
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
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.engine.services;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.pentaho.platform.api.engine.ActionSequenceException;
import org.pentaho.platform.api.engine.ActionValidationException;
import org.pentaho.platform.api.engine.IActionParameter;
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
import org.pentaho.platform.engine.core.system.objfac.AggregateObjectFactory;
import org.pentaho.platform.engine.security.SecurityParameterProvider;
import org.pentaho.platform.engine.services.actionsequence.ActionParameter;
import org.pentaho.platform.engine.services.runtime.RuntimeContext;
import org.pentaho.platform.engine.services.runtime.SimpleRuntimeElement;
import org.pentaho.platform.util.JVMParameterProvider;
import org.pentaho.platform.util.UUIDUtil;
import org.pentaho.platform.util.web.SimpleUrlFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author Andrei Abramov
 */
public class MessageFormatterTest {

  IRuntimeRepository mockedRuntimeRepository;
  ISolutionEngine mockedSolutionEngine;
  StandaloneApplicationContext applicationContext;
  AggregateObjectFactory aggregateObjectFactory;

  private final IPentahoUrlFactory urlFactory = new SimpleUrlFactory( "" );
  private final IPentahoSession session = new StandaloneSession( "system" );

  private IRuntimeContext runtimeCtx;

  @Before
  public void before() {
    applicationContext = new StandaloneApplicationContext( ".", "" );
    aggregateObjectFactory = new AggregateObjectFactory();
    mockedRuntimeRepository = mock( IRuntimeRepository.class );
    mockedSolutionEngine = mock( ISolutionEngine.class );
  }

  @Test
  public void formatSuccessMessage() {
    try ( MockedStatic<PentahoSystem> pentahoSystem = Mockito.mockStatic( PentahoSystem.class ) ) {
      pentahoSystem.when( PentahoSystem::getApplicationContext ).thenReturn( applicationContext );
      pentahoSystem.when( PentahoSystem::getObjectFactory ).thenReturn( aggregateObjectFactory );

      runtimeCtx = spy( new RuntimeContext( "id", mockedSolutionEngine, "solutionName",
        makeRuntimeData( session ), session, null, "processId", urlFactory,
        makeParameterProviders( session ), new ArrayList<String>(), null ) );

      Set<String> inputNames = new HashSet<>();
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
          + "size=\"1\"/></td></tr><tr><td class=\"portlet-font\" valign=\"top\">Test=<img%20src=\"http://www"
          + ".pentaho.com/sites/all/themes/pentaho_resp/logo.svg\"%20/><br/></td></tr></table></body></html>",
        messageBuffer.toString() );
    }
  }

  @Test
  public void shouldAddStacktraceWhenShowStacktraceIsTrue() {
    try ( MockedStatic<PentahoSystem> mockedPentahoSystem = Mockito.mockStatic( PentahoSystem.class ) ) {
      mockedPentahoSystem.when( PentahoSystem::getApplicationContext ).thenReturn( applicationContext );
      mockedPentahoSystem.when( PentahoSystem::getObjectFactory ).thenReturn( aggregateObjectFactory );

      runtimeCtx = spy( new RuntimeContext( "id", mockedSolutionEngine, "solutionName",
        makeRuntimeData( session ), session, null, "processId", urlFactory,
        makeParameterProviders( session ), new ArrayList<String>(), null ) );

      MessageFormatter mf = new MessageFormatter() {
        @Override
        String getTemplate( StringBuffer messageBuffer ) {
          try {
            return IOUtils.toString( this.getClass()
              .getResourceAsStream( "viewActionErrorTestTemplate.html" ), "UTF-8" );
          } catch ( IOException e ) {
            return null;
          }
        }

        @Override
        String getStacktrace( ActionSequenceException exception ) {
          return "Error stacktrace";
        }
      };
      StringBuffer messageBuffer = new StringBuffer();
      mf.formatExceptionMessage( MessageFormatter.HTML_MIME_TYPE, new ActionValidationException( "Test Error" ),
        messageBuffer, true );
      // details controls are not hidden
      // stacktrace is added
      assertEquals( "<div id=\"controls\">"
          + "<a href=\"#\" id=\"details-show\" class=\"showLink\" onclick=\"showHide('details');return false;\">View "
          + "Details</a>"
          + "<a href=\"#\" id=\"details-hide\" class=\"hideLink\" onclick=\"showHide('details');return false;\">Hide "
          + "Details</a>"
          + "</div>"
          + "<div id=\"details\" class=\"details\"><span class=\"label\">Stack Trace:</span><pre "
          + "class=\"stackTrace\">Error stacktrace<pre></div>",
        messageBuffer.toString() );
    }
  }

  @Test
  public void shouldNotAddStacktraceWhenShowStacktraceIsFalse() {
    try ( MockedStatic<PentahoSystem> mockedPentahoSystem = Mockito.mockStatic( PentahoSystem.class ) ) {
      mockedPentahoSystem.when( PentahoSystem::getApplicationContext ).thenReturn( applicationContext );
      mockedPentahoSystem.when( PentahoSystem::getObjectFactory ).thenReturn( aggregateObjectFactory );

      runtimeCtx = spy( new RuntimeContext( "id", mockedSolutionEngine, "solutionName",
        makeRuntimeData( session ), session, null, "processId", urlFactory,
        makeParameterProviders( session ), new ArrayList<String>(), null ) );
      MessageFormatter mf = new MessageFormatter() {
        @Override
        String getTemplate( StringBuffer messageBuffer ) {
          try {
            return IOUtils.toString( this.getClass()
              .getResourceAsStream( "viewActionErrorTestTemplate.html" ), "UTF-8" );
          } catch ( IOException e ) {
            return null;
          }
        }
      };
      StringBuffer messageBuffer = new StringBuffer();
      mf.formatExceptionMessage( MessageFormatter.HTML_MIME_TYPE, new ActionValidationException( "Test Error" ),
        messageBuffer, false );
      // details controls are hidden
      // stacktrace is not added
      assertEquals( "<div id=\"controls\" hidden>"
          + "<a href=\"#\" id=\"details-show\" class=\"showLink\" onclick=\"showHide('details');return false;\">View "
          + "Details</a>"
          + "<a href=\"#\" id=\"details-hide\" class=\"hideLink\" onclick=\"showHide('details');return false;\">Hide "
          + "Details</a>"
          + "</div>"
          + "<div id=\"details\" class=\"details\"><span class=\"label\">%STACK_TRACE_LABEL%</span><pre "
          + "class=\"stackTrace\">%STACK_TRACE%<pre></div>",
        messageBuffer.toString() );
    }
  }

  private Map<String, IParameterProvider> makeParameterProviders( final IPentahoSession session ) {
    final Map<String, IParameterProvider> res = new HashMap<>();

    res.put( "jvm", new JVMParameterProvider() );
    res.put( SecurityParameterProvider.SCOPE_SECURITY, new SecurityParameterProvider( session ) );

    return res;
  }

  private IRuntimeElement makeRuntimeData( final IPentahoSession session ) {
    return new SimpleRuntimeElement( UUIDUtil.getUUIDAsString(), session.getId(), IParameterProvider.SCOPE_SESSION );
  }

}