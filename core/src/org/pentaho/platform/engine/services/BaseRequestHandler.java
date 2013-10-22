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

package org.pentaho.platform.engine.services;

import org.pentaho.platform.api.engine.IActionCompleteListener;
import org.pentaho.platform.api.engine.IActionRequestHandler;
import org.pentaho.platform.api.engine.ICreateFeedbackParameterCallback;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.util.logging.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BaseRequestHandler implements IActionRequestHandler, IActionCompleteListener {

  public static final int ON_TIMEOUT_CANCEL = 0;

  public static final int ON_TIMEOUT_RETURN = 1;

  protected IPentahoUrlFactory urlFactory;

  private String actionPath;

  protected String processId;

  private String instanceId;

  protected IPentahoSession session;

  private IOutputHandler outputHandler;

  private HashMap parameterProviders;

  protected boolean instanceEnds;

  protected boolean forcePrompt = false;

  protected String parameterXsl = null;

  ArrayList messages;

  protected IRuntimeContext runtime = null;

  protected ICreateFeedbackParameterCallback createFeedbackParameterCallback;

  // IActionRequestHandler methods

  public BaseRequestHandler( final IPentahoSession session, final String instanceId,
      final IOutputHandler outputHandler, final IParameterProvider parameterProvider,
      final IPentahoUrlFactory urlFactory ) {
    this.session = session;
    this.outputHandler = outputHandler;
    this.urlFactory = urlFactory;
    this.instanceId = instanceId;
    parameterProviders = new HashMap();
    messages = new ArrayList();
    if ( parameterProvider != null ) {
      parameterProviders.put( IParameterProvider.SCOPE_REQUEST, parameterProvider );

    }
    instanceEnds = true;
  }

  public List getMessages() {
    return messages;
  }

  public void setInstanceEnds( final boolean instanceEnds ) {
    this.instanceEnds = instanceEnds;
  }

  public void setParameterProvider( final String name, final IParameterProvider parameterProvider ) {
    parameterProviders.put( name, parameterProvider );
  }

  public HashMap getParameterProviders() {
    return parameterProviders;
  }

  public void setOutputHandler( final IOutputHandler outputHandler ) {
    this.outputHandler = outputHandler;
  }

  public void setProcessId( final String processId ) {
    this.processId = processId;
  }

  public void setInstanceId( final String instanceId ) {
    this.instanceId = instanceId;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public IRuntimeContext handleActionRequest( final int timeout, final int timeoutType ) {

    // Get the solution engine
    ISolutionEngine solutionEngine = PentahoSystem.get( ISolutionEngine.class, session );
    if ( solutionEngine == null ) {
      Logger.error( this, Messages.getInstance().getErrorString( "BaseRequestHandler.ERROR_0001_NO_SOLUTION_ENGINE" ) ); //$NON-NLS-1$
      return null;
    }
    solutionEngine.setCreateFeedbackParameterCallback( createFeedbackParameterCallback );
    solutionEngine.setLoggingLevel( ILogger.DEBUG );
    solutionEngine.init( session );
    solutionEngine.setForcePrompt( forcePrompt );
    if ( parameterXsl != null ) {
      solutionEngine.setParameterXsl( parameterXsl );
    }

    dispose();
    runtime =
        solutionEngine.execute( actionPath, processId, false, instanceEnds, instanceId, true, parameterProviders,
            outputHandler, this, urlFactory, messages );

    // need to wait until this is complete
    // TODO

    // if this times out check the timeoutType before cancelling or
    // returning and leaving the component running

    return runtime;
  }

  public void dispose() {
    // execute the action
    if ( runtime != null ) {
      runtime.dispose();
    }
  }

  public IRuntimeContext handleActionAsyncRequest() {
    // TODO: handle asynchronous executions
    return null;
  }

  public IRuntimeContext getRuntime( final String requestHandle ) {
    return runtime;
  }

  // IActionCompleteListener methods
  public void actionComplete( final IRuntimeContext completedRuntime ) {
    // TODO

  }

  public IOutputHandler getOutputHandler() {
    return outputHandler;
  }

  public void setForcePrompt( final boolean forcePrompt ) {
    this.forcePrompt = forcePrompt;
  }

  public void setParameterXsl( final String xsl ) {
    this.parameterXsl = xsl;
  }

  public String getActionPath() {
    return actionPath;
  }

  public void setActionPath( final String actionPath ) {
    this.actionPath = actionPath;
  }

  public void setCreateFeedbackParameterCallback( ICreateFeedbackParameterCallback createFeedbackParameterCallback ) {
    this.createFeedbackParameterCallback = createFeedbackParameterCallback;
  }

}
