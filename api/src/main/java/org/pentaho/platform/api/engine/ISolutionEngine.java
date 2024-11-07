/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.api.engine;

import java.util.List;
import java.util.Map;

/**
 * The Solution Engine handles loading and launching execution of solutions and action sequences. There is one
 * solution engine per request, which can launch one or more action sequences.
 */
public interface ISolutionEngine extends ILogger {

  public static final String RUNTIME_SOLUTION_NAME = "RuntimeSolution"; //$NON-NLS-1$

  /**
   * Sets the source for input parameters.
   * 
   * @param name
   *          name to give to this provider
   * @param parameterProvider
   *          instance of a provider
   * @see org.pentaho.platform.api.engine.services.IParameterProvider
   */
  public void setParameterProvider( String name, IParameterProvider parameterProvider );

  public void setCreateFeedbackParameterCallback( ICreateFeedbackParameterCallback callback );

  /**
   * Executes the action sequence specified in the manner described in the parameters
   * 
   * @param actionPath
   *          the path to the solution action that will lead to the requested action
   * @param processId
   *          id for the given action sequence document
   * @param async
   *          synchronous(false) or asynchronous(true) execution (not currently used)
   * @param instanceId
   *          id to be handed to the runtime repository
   * @param persisted
   *          if true, store runtime data, otherwise do not
   * @param parameterProviderMap
   *          group of ParameterProviders, sources for inout parameters
   * @param outputHandler
   *          handler used to query for addition parameters
   * @param listener
   *          object notified on completion of action sequences
   * @param urlFactory
   *          factory for building urls
   * @param messages
   *          list into which debug, info, warning, and errors messages will be added
   * @return IRuntimeContext the RuntimeContext associated with this action sequence execution
   * 
   * @see org.pentaho.platform.api.engine.IRuntimeContext
   */
  @SuppressWarnings( "rawtypes" )
  public IRuntimeContext execute( String actionPath, String processId, boolean async, boolean instanceEnds,
      String instanceId, boolean persisted, Map parameterProviderMap, IOutputHandler outputHandler,
      IActionCompleteListener listener, IPentahoUrlFactory urlFactory, List messages );

  /**
   * Executes the action sequence specified
   * 
   * @param runtime
   *          The runtime context for the execution
   * @param actionPath
   *          path to the action
   * @param processId
   *          id for the given process, typically a GUID or unique id for this execution
   * @param async
   *          true if the execution should be asynchronous.
   * @param instanceEnds
   *          currently true indicating that the process ends with this execution
   * @param parameterProviderMap
   *          Map of parameter providers to use for the execution
   * @param outputHandler
   *          The output handler for dealing with user feedback
   * @return The runtime context for the execution
   * @see IRuntimeContext
   * @see IParameterProvider
   * @see IActionSequence
   */
  @SuppressWarnings( "rawtypes" )
  public IRuntimeContext execute( IRuntimeContext runtime, String actionPath, String processId, boolean async,
      boolean instanceEnds, Map parameterProviderMap, IOutputHandler outputHandler );

  /**
   * Executes the in memory action sequence specified
   * 
   * @param actionSequenceXML
   *          the in memory action sequence string
   * @param sequenceName
   *          name of the action sequence
   * @param processId
   *          id for the given process, typically a GUID or unique id for this execution
   * @param async
   *          true if the execution should be asynchronous.
   * @param instanceEnds
   *          currently true indicating that the process ends with this execution
   * @param parameterProviderMap
   *          Map of parameter providers to use for the execution
   * @param outputHandler
   *          The output handler for dealing with user feedback
   * @return The runtime context for the execution
   * @see IRuntimeContext
   * @see IParameterProvider
   * @see IActionSequence
   */
  @SuppressWarnings( "rawtypes" )
  public IRuntimeContext execute( String actionSequenceXML, String sequenceName, String processId, boolean async,
      boolean instanceEnds, String instanceId, boolean persisted, Map parameterProviderMap,
      IOutputHandler outputHandler, IActionCompleteListener pListener, IPentahoUrlFactory urlFactory, List messages );

  /**
   * Sets the action complete listener which will be called when the action is complete
   * 
   * @param listener
   *          Listener to call back when execution is complete.
   */
  public void setlistener( IActionCompleteListener listener );

  public void setlistener( IExecutionListener execListener );

  /**
   * Sets the session in the solution engine
   * 
   * @param session
   *          The session for this execution
   */
  public void setSession( IPentahoSession session );

  /**
   * @return the runtime context being used for this execution.
   */
  public IRuntimeContext getExecutionContext();

  /**
   * @return Gets the current status from this execution
   */
  public int getStatus();

  /**
   * Initialize the SolutionEngine. This method should be called immediately after object construction, and if
   * solution engines are re-used among different IPentahoSessions to bind the solution engine to the session.
   * 
   * @param session
   *          the session context for this SolutionEngine
   */
  public void init( IPentahoSession session );

  /**
   * Sets if the promp page should be forced
   * 
   * @param status
   */
  public void setForcePrompt( boolean forcePrompt );

  /**
   * Sets the xsl file to be used to generate the parameter page for the current component. The parameter should be
   * a full path from the solution root starting with a /, or it should be a path relative to the directory of the
   * current action sequence.
   * 
   * @param xsl
   *          The name of the XSL file
   */
  public void setParameterXsl( String xsl );

}
