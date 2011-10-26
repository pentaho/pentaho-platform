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
 * Copyright 2008 Pentaho Corporation.  All rights reserved. 
 * 
 */
package org.pentaho.platform.engine.services.solution;

import java.io.OutputStream;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.actionsequence.dom.IActionDefinition;
import org.pentaho.platform.api.engine.IActionSequence;
import org.pentaho.platform.api.engine.ICreateFeedbackParameterCallback;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IMessageFormatter;
import org.pentaho.platform.api.engine.IMimeTypeListener;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.solution.ActionInfo;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.util.messages.LocaleHelper;

public class ActionSequenceContentGenerator extends BaseContentGenerator {

	private static final long serialVersionUID = 458870144807597675L;

	public Log getLogger() {
		return LogFactory.getLog(ActionSequenceContentGenerator.class);
	}

	protected void setupListeners( ISolutionEngine solutionEngine ) {
	    // setup any listeners
	    ICreateFeedbackParameterCallback feedbackParameterCallback = (ICreateFeedbackParameterCallback) getCallback( ICreateFeedbackParameterCallback.class );
	    if( feedbackParameterCallback != null ) {
		    solutionEngine.setCreateFeedbackParameterCallback(feedbackParameterCallback);
	    }
	}
	
	protected ISolutionEngine getSolutionEngine() {
	    return PentahoSystem.get(ISolutionEngine.class, userSession);
	}
		
	public void createContent( ) throws Exception {

		// get the solution engine
	    ISolutionEngine solutionEngine = getSolutionEngine();
	    if (solutionEngine == null) {
	    	String message = Messages.getInstance().getErrorString("BaseRequestHandler.ERROR_0001_NO_SOLUTION_ENGINE"); //$NON-NLS-1$
	    	error( message );
	    	throw new ObjectFactoryException( message );
	    }

	    setupListeners( solutionEngine );
	    
	    IParameterProvider requestParams = parameterProviders.get( IParameterProvider.SCOPE_REQUEST );

//	    setup( solutionEngine );
	    
	    IRuntimeContext runtime = null;
	    try {

		    ISystemSettings systemSettings = PentahoSystem.getSystemSettings();
		    String processId = this.getClass().getName();
		    boolean instanceEnds = "true".equalsIgnoreCase( requestParams.getStringParameter( "instanceends" ,"true" ) );  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$

		    String parameterXsl = systemSettings.getSystemSetting("default-parameter-xsl", "DefaultParameterForm.xsl"); //$NON-NLS-1$ //$NON-NLS-2$
		    boolean forcePrompt = "true".equalsIgnoreCase( requestParams.getStringParameter( "prompt" ,"false" ) );  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		    boolean doSubscribe = "yes".equalsIgnoreCase( requestParams.getStringParameter("subscribepage", "no") ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		    String solutionName = requestParams.getStringParameter("solution", null); //$NON-NLS-1$
		    String actionPath = requestParams.getStringParameter("path", null); //$NON-NLS-1$
		    String actionName = requestParams.getStringParameter("action2", null); //$NON-NLS-1$

        String actionSeqPath = ActionInfo.buildSolutionPath(solutionName, actionPath, actionName);
		    
		    createOutputFileName( actionSeqPath );
		    
		    if (actionSeqPath == null) {
		      // now look for a primary action
		      actionSeqPath = requestParams.getStringParameter("action", null); //$NON-NLS-1$
		    }

		    int outputPreference = IOutputHandler.OUTPUT_TYPE_DEFAULT;
		    if ( doSubscribe ) {
		    	forcePrompt = true;
		        parameterProviders.put("PRO_EDIT_SUBSCRIPTION", requestParams); //$NON-NLS-1$ 
		        outputPreference = IOutputHandler.OUTPUT_TYPE_PARAMETERS;
		    }
		    outputHandler.setOutputPreference(outputPreference);

		    solutionEngine.setLoggingLevel(ILogger.DEBUG);
		    solutionEngine.init(userSession);
		    solutionEngine.setForcePrompt(forcePrompt);
		    if (parameterXsl != null) {
		      solutionEngine.setParameterXsl(parameterXsl);
		    }
		    
		    runtime = solutionEngine.execute(actionSeqPath, processId, false, instanceEnds, instanceId, false, parameterProviders, outputHandler, null, urlFactory, messages);

	        boolean doMessages = "true".equalsIgnoreCase(requestParams.getStringParameter("debug", "false")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      	    boolean doWrapper = "true".equalsIgnoreCase( requestParams.getStringParameter( "wrapper" ,"true" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	    	postExecute( runtime, doMessages, doWrapper );
	    } finally {
		    if( runtime != null ) {
		    	runtime.dispose();
		    }
	    }
		
	}
	
	protected void createOutputFileName( String actionPath ) {
        ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, userSession);
        ActionInfo info = ActionInfo.parseActionString(actionPath);
        IActionSequence actionSequence = repository.getActionSequence(info.getSolutionName(), info.getPath(), info.getActionName(),
                PentahoSystem.loggingLevel, ISolutionRepository.ACTION_EXECUTE);
        String fileName = "content"; //$NON-NLS-1$
        if (actionSequence != null) {
          String title = actionSequence.getTitle();
          if ((title != null) && (title.length() > 0)) {
            fileName = title;
          } else {
            String sequenceName = actionSequence.getSequenceName();

            if ((sequenceName != null) && (sequenceName.length() > 0)) {
              fileName = sequenceName;
            } else {
              List<?> actionDefinitionsList = actionSequence.getActionDefinitionsAndSequences();
              int i = 0;
              boolean done = false;

              while ((actionDefinitionsList.size() > i) && !done) {
                IActionDefinition actionDefinition = (IActionDefinition) actionDefinitionsList.get(i);
                String componentName = actionDefinition.getComponentName();
                if ((componentName != null) && (componentName.length() > 0)) {
                  fileName = componentName;
                  done = true;
                } else {
                  i++;
                }
              }
            }
          }
        }
	    IMimeTypeListener mimeTypeListener = outputHandler.getMimeTypeListener();
	    if (mimeTypeListener != null) {
	      mimeTypeListener.setName(fileName);
	    }
	}
	
	protected void postExecute( IRuntimeContext runtime, boolean doMessages, boolean doWrapper ) throws Exception {
    // see if we need to provide feedback to the caller
    if (!outputHandler.contentDone() || doMessages ) {
      IParameterProvider requestParams = parameterProviders.get( IParameterProvider.SCOPE_REQUEST );
      String actionName = requestParams.getStringParameter("action", null); //$NON-NLS-1$
      
      IContentItem contentItem = outputHandler.getOutputContentItem( IOutputHandler.RESPONSE, IOutputHandler.CONTENT, null, "text/html" );//$NON-NLS-1$
      OutputStream outputStream = contentItem.getOutputStream(actionName);

      if (outputStream != null) {
        StringBuffer buffer = new StringBuffer();
        if ((runtime != null) && (runtime.getStatus() == IRuntimeContext.RUNTIME_STATUS_SUCCESS)) {
          PentahoSystem.get( IMessageFormatter.class, userSession).formatSuccessMessage(
              "text/html", runtime, buffer, doMessages, doWrapper); //$NON-NLS-1$
        } else {
          // we need an error message...
          PentahoSystem.get( IMessageFormatter.class, userSession).formatFailureMessage(
              "text/html", runtime, buffer, messages); //$NON-NLS-1$
        }
        outputStream.write(buffer.toString().getBytes(LocaleHelper.getSystemEncoding()));
        outputStream.close();
      }
    }
	}
	
}
