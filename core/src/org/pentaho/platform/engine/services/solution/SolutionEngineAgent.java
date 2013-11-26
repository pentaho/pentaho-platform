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
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.services.solution;

import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoRequestContext;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.util.web.SimpleUrlFactory;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SolutionEngineAgent {

  private HashMap<String, String> parameters;

  private String userId;

  private String actionSequence;

  private String description;

  private ByteArrayOutputStream outputStream;

  private ISolutionEngine solutionEngine = null;

  public SolutionEngineAgent() {
    parameters = new HashMap<String, String>();
  }

  public void setUserId( final String userId ) {
    this.userId = userId;
  }

  public String getUserId() {
    return userId;
  }

  public void setParamter( final String name, final String value ) {
    parameters.put( name, value );
  }

  public void setActionSequence( final String actionSequence ) {
    this.actionSequence = actionSequence;
  }

  public String getActionSequence() {
    return actionSequence;
  }

  public void setDescription( final String description ) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public String getOutput( final String name ) {
    if ( ( name == null ) || "default".equals( name ) || "".equals( name ) ) { //$NON-NLS-1$ //$NON-NLS-2$
      return outputStream.toString();
    } else {
      IActionParameter output = solutionEngine.getExecutionContext().getOutputParameter( name );
      return output.getStringValue();
    }
  }

  public int execute() {
    PentahoSystem.systemEntryPoint();
    try {
      // create a generic session object
      StandaloneSession session = new StandaloneSession( userId );

      solutionEngine = PentahoSystem.get( SolutionEngine.class, session );
      solutionEngine.init( session );

      SimpleParameterProvider parameterProvider = new SimpleParameterProvider( parameters );

      HashMap<String, IParameterProvider> parameterProviderMap = new HashMap<String, IParameterProvider>();
      parameterProviderMap.put( IParameterProvider.SCOPE_REQUEST, parameterProvider );

      IPentahoRequestContext requestContext = PentahoRequestContextHolder.getRequestContext();
      IPentahoUrlFactory urlFactory = new SimpleUrlFactory( requestContext.getContextPath() ); //$NON-NLS-1$

      String processName = description;
      boolean persisted = false;
      List messages = new ArrayList();

      outputStream = new ByteArrayOutputStream( 0 );
      SimpleOutputHandler outputHandler = null;
      if ( outputStream != null ) {
        outputHandler = new SimpleOutputHandler( outputStream, false );
        outputHandler.setOutputPreference( IOutputHandler.OUTPUT_TYPE_DEFAULT );
      }
      solutionEngine.execute( actionSequence, processName, false, true, null, persisted, parameterProviderMap,
          outputHandler, null, urlFactory, messages );

    } finally {
      PentahoSystem.systemExitPoint();
    }
    return solutionEngine.getStatus();
  }

}
