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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.pentaho.platform.api.engine.ActionSequenceException;
import org.pentaho.platform.api.engine.IActionCompleteListener;
import org.pentaho.platform.api.engine.IActionSequence;
import org.pentaho.platform.api.engine.ICreateFeedbackParameterCallback;
import org.pentaho.platform.api.engine.IExecutionListener;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoInitializer;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.repository.IRuntimeElement;
import org.pentaho.platform.api.repository.IRuntimeRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.engine.core.audit.AuditHelper;
import org.pentaho.platform.engine.core.audit.MessageTypes;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityParameterProvider;
import org.pentaho.platform.engine.services.ActionSequenceJCRHelper;
import org.pentaho.platform.engine.services.PentahoMessenger;
import org.pentaho.platform.engine.services.actionsequence.SequenceDefinition;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.engine.services.runtime.RuntimeContext;
import org.pentaho.platform.engine.services.runtime.SimpleRuntimeElement;
import org.pentaho.platform.util.JVMParameterProvider;
import org.pentaho.platform.util.UUIDUtil;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SolutionEngine extends PentahoMessenger implements ISolutionEngine, IPentahoInitializer {

  /**
   * 
   */
  private static final long serialVersionUID = -8957434833691831700L;

  private final boolean debug = PentahoSystem.debug;

  private Map<String, IParameterProvider> parameterProviders;

  private boolean persisted;

  private IActionCompleteListener doneListener;

  private IExecutionListener execListener;

  private IPentahoSession session;

  protected IRuntimeContext runtime = null;

  private int status;

  private boolean forcePrompt = false;

  private static final String LOG_NAME = "SOLUTION-ENGINE"; //$NON-NLS-1$

  private static final String JVM_PARAMETER_PROVIDER = "jvm"; //$NON-NLS-1$

  private static final Log logger = LogFactory.getLog( SolutionEngine.class );

  private String parameterXsl = null;

  private ICreateFeedbackParameterCallback createFeedbackParameterCallback;

  @Override
  public Log getLogger() {
    return SolutionEngine.logger;
  }

  public SolutionEngine() {
    status = IRuntimeContext.RUNTIME_STATUS_NOT_STARTED;
  }

  public void init( final IPentahoSession pSession ) {
    parameterProviders = new HashMap<String, IParameterProvider>();
    this.session = pSession;
    this.setParameterProvider( SolutionEngine.JVM_PARAMETER_PROVIDER, new JVMParameterProvider() );
    setForcePrompt( false );
    // Provide the security parameter provider to the parameter provider map in the super class
    SecurityParameterProvider provider = new SecurityParameterProvider( pSession );
    this.setParameterProvider( SecurityParameterProvider.SCOPE_SECURITY, provider );
  }

  public void setParameterProvider( final String name, final IParameterProvider parameterProvider ) {
    parameterProviders.put( name, parameterProvider );
  }

  protected Map getParameterProviders() {
    return parameterProviders;
  }

  public void setlistener( final IActionCompleteListener doneListener ) {
    this.doneListener = doneListener;
  }

  public void setlistener( final IExecutionListener execListener ) {
    this.execListener = execListener;
  }

  public void setSession( final IPentahoSession session ) {
    this.session = session;
  }

  protected boolean checkParameters( final String sequencePath, final String processId ) {
    if ( processId == null ) {
      // cannot allow this
      error( Messages.getInstance().getErrorString( "SolutionEngine.ERROR_0001_PROCESS_NOT_SPECIFIED" ) ); //$NON-NLS-1$
      return false;
    }
    if ( sequencePath == null ) {
      // cannot allow this
      error( Messages.getInstance().getErrorString( "SolutionEngine.ERROR_0003_PATH_NOT_SPECIFIED" ) ); //$NON-NLS-1$
      return false;
    }
    return true;

  }

  public int getStatus() {
    return status;
  }

  public IRuntimeContext getExecutionContext() {
    return runtime;
  }

  protected IPentahoSession getSession() {
    return session;
  }

  protected IRuntimeContext getRuntime() {
    return runtime;
  }

  protected void setRuntime( final IRuntimeContext runtime ) {
    this.runtime = runtime;
  }

  protected void auditStart( final String actionPath, final String instanceId ) {
    if ( debug ) {
      debug( Messages.getInstance().getString( "SolutionEngine.DEBUG_STARTING_EXECUTION", actionPath ) ); //$NON-NLS-1$
    }
    genLogIdFromInfo( instanceId, SolutionEngine.LOG_NAME, actionPath );
  }

  public IRuntimeContext execute( final String actionPath, final String processId, final boolean async,
      final boolean instanceEnds, final String instanceId, final boolean isPersisted, final Map parameterProviderMap,
      final IOutputHandler outputHandler, final IActionCompleteListener pListener, final IPentahoUrlFactory urlFactory,
      final List messages ) {
    applyParameterMappers( parameterProviderMap );
    return execute( actionPath, processId, async, instanceEnds, instanceId, isPersisted, parameterProviderMap,
        outputHandler, pListener, urlFactory, messages, null );
  }

  public IRuntimeContext execute( final String actionSequenceXML, final String sequencePath, final String processId,
      final boolean async, final boolean instanceEnds, final String instanceId, final boolean isPersisted,
      final Map parameterProviderMap, final IOutputHandler outputHandler, final IActionCompleteListener pListener,
      final IPentahoUrlFactory urlFactory, final List messages ) {
    applyParameterMappers( parameterProviderMap );
    return execute( sequencePath, processId, async, instanceEnds, instanceId, //$NON-NLS-1$ //$NON-NLS-2$
        isPersisted, parameterProviderMap, outputHandler, pListener, urlFactory, messages, actionSequenceXML );
  }

  protected IRuntimeContext execute( final String actionPath, final String processId, final boolean async,
      final boolean instanceEnds, String instanceId, final boolean isPersisted, final Map parameterProviderMap,
      final IOutputHandler outputHandler, final IActionCompleteListener pListener, final IPentahoUrlFactory urlFactory,
      final List messages, final String actionSequenceXML ) {

    this.persisted = isPersisted;
    setlistener( pListener );
    setSession( session );

    setMessages( messages );

    auditStart( actionPath, instanceId );

    if ( !checkParameters( actionPath, processId ) ) {
      return null;
    }

    session.setProcessId( processId );
    session.setActionName( actionPath );

    // create the runtime context object for this operation
    if ( debug ) {
      debug( Messages.getInstance().getString( "SolutionEngine.DEBUG_GETTING_RUNTIME_CONTEXT" ) ); //$NON-NLS-1$
    }
    boolean newInstance = instanceId == null;
    IRuntimeRepository runtimeRepository = null;
    if ( PentahoSystem.getObjectFactory().objectDefined( IRuntimeRepository.class.getSimpleName() ) ) {
      runtimeRepository = PentahoSystem.get( IRuntimeRepository.class, session );
    }

    IRuntimeElement runtimeData;
    if ( runtimeRepository == null ) {
      String id = UUIDUtil.getUUIDAsString();
      runtimeData = new SimpleRuntimeElement( id, session.getId(), IParameterProvider.SCOPE_SESSION );
    } else {
      runtimeRepository.setLoggingLevel( loggingLevel );
      if ( newInstance ) {
        // we need to create runtime data for this execution
        try {
          runtimeData =
              runtimeRepository.newRuntimeElement( session.getId(), IParameterProvider.SCOPE_SESSION, !persisted );
        } catch ( Throwable t ) {
          error( Messages.getInstance().getErrorString( "SolutionEngine.ERROR_0008_INVALID_INSTANCE", instanceId ), t ); //$NON-NLS-1$
          status = IRuntimeContext.RUNTIME_STATUS_SETUP_FAIL;
          return null;
        }
      } else {
        try {
          runtimeData = runtimeRepository.loadElementById( instanceId, null );
        } catch ( Throwable t ) {
          error( Messages.getInstance().getErrorString( "SolutionEngine.ERROR_0008_INVALID_INSTANCE", instanceId ), t ); //$NON-NLS-1$
          status = IRuntimeContext.RUNTIME_STATUS_SETUP_FAIL;
          return null;
        }
      }
    }
    if ( runtimeData == null ) {
      error( Messages.getInstance().getErrorString( "SolutionEngine.ERROR_0008_INVALID_INSTANCE", instanceId ) ); //$NON-NLS-1$
      status = IRuntimeContext.RUNTIME_STATUS_SETUP_FAIL;
      return null;
    }
    createRuntime( runtimeData, actionPath, outputHandler, processId, urlFactory );
    runtime.setLoggingLevel( loggingLevel );
    instanceId = runtime.getInstanceId();
    genLogIdFromInfo( instanceId, SolutionEngine.LOG_NAME, actionPath );

    if ( newInstance ) {
      // audit the creation of this against the session
      AuditHelper.audit( session.getId(), session.getName(), actionPath, getObjectName(), processId,
          MessageTypes.INSTANCE_START, instanceId, "", 0, this ); //$NON-NLS-1$
    }

    /*
     * IRuntimeElement runtimeData; if (instanceId == null) { // we need to create runtime data for this execution
     * try { runtimeRepository.setLoggingLevel(loggingLevel); runtimeData =
     * runtimeRepository.newRuntimeElement(session.getId(), IParameterProvider.SCOPE_SESSION, !persisted);
     * createRuntime(runtimeData, solutionName, outputHandler, processId, urlFactory);
     * runtime.setLoggingLevel(loggingLevel); instanceId = runtime.getInstanceId(); genLogIdFromInfo(instanceId,
     * SolutionEngine.LOG_NAME, sequenceName); // audit the creation of this against the session
     * AuditHelper.audit(session.getId(), session.getName(), sequenceName, getObjectName(), processId,
     * MessageTypes.INSTANCE_START, instanceId, "", 0, this); //$NON-NLS-1$ } catch (Throwable t) {
     * error(Messages.getInstance().getErrorString("SolutionEngine.ERROR_0008_INVALID_INSTANCE", instanceId), t);
     * //$NON-NLS-1$ status = IRuntimeContext.RUNTIME_STATUS_SETUP_FAIL; return null; } } else { try {
     * runtimeRepository.setLoggingLevel(loggingLevel); runtimeData = runtimeRepository.loadElementById(instanceId,
     * null); createRuntime(runtimeData, solutionName, outputHandler, processId, urlFactory);
     * runtime.setLoggingLevel(loggingLevel); instanceId = runtime.getInstanceId(); genLogIdFromInfo(instanceId,
     * SolutionEngine.LOG_NAME, sequenceName); } catch (Throwable t) {
     * error(Messages.getInstance().getErrorString("SolutionEngine.ERROR_0008_INVALID_INSTANCE", instanceId), t);
     * //$NON-NLS-1$ status = IRuntimeContext.RUNTIME_STATUS_SETUP_FAIL; return null; } }
     */
    return executeInternal( actionPath, processId, async, instanceEnds, parameterProviderMap, actionSequenceXML );
  }

  public IRuntimeContext execute( final IRuntimeContext pRuntime, String actionPath, final String processId,
      final boolean async, final boolean instanceEnds, final Map parameterProviderMap,
      final IOutputHandler outputHandler ) {
    applyParameterMappers( parameterProviderMap );

    runtime = pRuntime;
    runtime.setOutputHandler( outputHandler );
    auditStart( actionPath, runtime.getInstanceId() );

    if ( !checkParameters( actionPath, processId ) ) {
      status = IRuntimeContext.RUNTIME_STATUS_FAILURE;
      return null;
    }
    return executeInternal( actionPath, processId, async, instanceEnds, parameterProviderMap );

  }

  protected IRuntimeContext executeInternal( final String sequencePath, final String processId, final boolean async,
      final boolean instanceEnds, final Map parameterProviderMap ) {
    return executeInternal( sequencePath, processId, async, instanceEnds, parameterProviderMap, null );
  }

  @SuppressWarnings( { "unchecked" } )
  protected IRuntimeContext executeInternal( final String sequencePath, final String processId, final boolean async,
      final boolean instanceEnds, final Map parameterProviderMap, final String actionSequenceXML ) {

    long start = System.currentTimeMillis();

    parameterProviders.putAll( parameterProviderMap );
    parameterProviders.put( PentahoSystem.SCOPE_GLOBAL, PentahoSystem.getGlobalParameters() );

    // load the solution action document
    if ( debug ) {
      debug( Messages.getInstance().getString( "SolutionEngine.DEBUG_LOADING_ACTION_DEFINITION" ) ); //$NON-NLS-1$
    }

    IActionSequence actionSequence = null;
    if ( actionSequenceXML != null ) {
      actionSequence = createActionSequenceFromXml( actionSequenceXML );
    } else {
      actionSequence = createActionSequence( sequencePath );
    }
    if ( actionSequence == null ) {
      error( Messages.getInstance().getErrorString( "SolutionEngine.ERROR_0007_ACTION_EXECUTION_FAILED" ) ); //$NON-NLS-1$
      status = IRuntimeContext.RUNTIME_STATUS_FAILURE;
      long end = System.currentTimeMillis();
      AuditHelper.audit( session.getId(), session.getName(), sequencePath, getObjectName(), processId,
          MessageTypes.INSTANCE_FAILED, runtime.getInstanceId(), Messages.getInstance().getErrorString(
              "SolutionEngine.ERROR_0007_ACTION_EXECUTION_FAILED" ), ( (float) ( end - start ) / 1000 ), this ); //$NON-NLS-1$
      return runtime;
    }

    runtime.setActionSequence( actionSequence );
    if ( parameterXsl != null ) {
      runtime.setParameterXsl( parameterXsl );
    }

    if ( forcePrompt ) {
      runtime.setPromptStatus( IRuntimeContext.PROMPT_WAITING );
    } else {
      runtime.setPromptStatus( IRuntimeContext.PROMPT_NO );
    }

    boolean validating = true;
    try {
      runtime.validateSequence( FilenameUtils.getName( sequencePath ), execListener );
      validating = false;
      runtime.executeSequence( doneListener, execListener, async );
      if ( instanceEnds ) {
        long end = System.currentTimeMillis();
        AuditHelper.audit( session.getId(), session.getName(), sequencePath, getObjectName(), processId,
            MessageTypes.INSTANCE_END, runtime.getInstanceId(), "", ( (float) ( end - start ) / 1000 ), this ); //$NON-NLS-1$
      }
      status = runtime.getStatus();
    } catch ( ActionSequenceException ex ) {
      String errorMsg = null;
      status = validating ? IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_FAIL : IRuntimeContext.RUNTIME_STATUS_FAILURE;

      // This next line is a bit of a workaround, to make up for a deficiency in the SolutionEngine api.
      // What would be nice is to have the exception that is being caught here actually be thrown out of this
      // method. However, the ISolutionEngine interface that this class implements doesn't allow exceptions to be
      // thrown from this method. Since we can't change the signature of public API's with a minor release we need
      // a
      // workaround. We've created an new error method in PentahoMessenger that takes the exception and stuffs it
      // in
      // the messages list maintained within PentahoMessenger. Callers of this method that want to know if an
      // ActionSequenceException occurred should first call getStatus(). If the status does not
      // indicate success then call getMessages() and check if there is an exception in the list of messages.
      error( ex );

      long end = System.currentTimeMillis();
      AuditHelper.audit( session.getId(), session.getName(), sequencePath, getObjectName(), processId,
          MessageTypes.INSTANCE_FAILED, runtime.getInstanceId(), errorMsg, ( (float) ( end - start ) / 1000 ), this ); //$NON-NLS-1$
    }

    return runtime;
  }

  protected void createRuntime( final IRuntimeElement runtimeData, final String solutionName,
      final IOutputHandler outputHandler, final String processId, final IPentahoUrlFactory urlFactory ) {
    runtime =
        new RuntimeContext( runtimeData.getInstanceId(), this, solutionName, runtimeData, session, outputHandler,
            processId, urlFactory, parameterProviders, getMessages(), createFeedbackParameterCallback );
  }

  private IActionSequence createActionSequence( final String actionPath ) {
    return new ActionSequenceJCRHelper( session ).getActionSequence( actionPath, loggingLevel,
        RepositoryFilePermission.READ );
  }

  private IActionSequence createActionSequenceFromXml( final String actionDef ) {
    try {
      Document actionSequenceDocument = XmlDom4JHelper.getDocFromString( actionDef, null );
      if ( actionSequenceDocument == null ) {
        return null;
      }

      IActionSequence actionSequence =
          SequenceDefinition.ActionSequenceFactory( actionSequenceDocument,
              "", this, PentahoSystem.getApplicationContext(), loggingLevel ); //$NON-NLS-1$
      return ( actionSequence );
    } catch ( Exception e ) {
      //ignore
    }
    return null;
  }

  public void setForcePrompt( final boolean forcePrompt ) {
    this.forcePrompt = forcePrompt;
  }

  public void setParameterXsl( final String xsl ) {
    this.parameterXsl = xsl;
  }

  /**
   * FIXME: This is in dire need of fixing
   * 
   * @param parameterProviderMap
   */
  @SuppressWarnings( { "unchecked" } )
  protected void applyParameterMappers( final Map parameterProviderMap ) {
    IParameterProvider request = (IParameterProvider) parameterProviderMap.get( IParameterProvider.SCOPE_REQUEST );
    if ( request != null ) {
      IParameterProvider chartRequest = new FlashChartRequestMapper( request );
      parameterProviderMap.remove( IParameterProvider.SCOPE_REQUEST );
      parameterProviderMap.put( IParameterProvider.SCOPE_REQUEST, chartRequest );
    }
  }

  public void setCreateFeedbackParameterCallback( ICreateFeedbackParameterCallback callback ) {
    this.createFeedbackParameterCallback = callback;
  }

  protected ICreateFeedbackParameterCallback getCreateFeedbackParameterCallback() {
    return createFeedbackParameterCallback;
  }

}
