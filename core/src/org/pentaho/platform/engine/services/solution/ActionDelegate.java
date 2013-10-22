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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.actionsequence.dom.ActionInputConstant;
import org.pentaho.actionsequence.dom.ActionSequenceDocument;
import org.pentaho.actionsequence.dom.IActionInput;
import org.pentaho.actionsequence.dom.IActionOutput;
import org.pentaho.actionsequence.dom.IActionResource;
import org.pentaho.actionsequence.dom.IActionSequenceOutput;
import org.pentaho.platform.api.action.ActionPreProcessingException;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.api.action.IDefinitionAwareAction;
import org.pentaho.platform.api.action.ILoggingAction;
import org.pentaho.platform.api.action.IPreProcessingAction;
import org.pentaho.platform.api.action.ISessionAwareAction;
import org.pentaho.platform.api.action.IStreamingAction;
import org.pentaho.platform.api.engine.ActionExecutionException;
import org.pentaho.platform.api.engine.ActionValidationException;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.engine.core.output.SimpleContentItem;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.util.beans.ActionHarness;
import org.pentaho.platform.util.beans.AlternateIndexFormatter;
import org.pentaho.platform.util.beans.PropertyNameFormatter;
import org.pentaho.platform.util.beans.SuffixAppenderFormatter;
import org.pentaho.platform.util.beans.ValueGenerator;
import org.pentaho.platform.util.beans.ValueSetErrorCallback;
import org.pentaho.platform.util.web.MimeHelper;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The purpose of the {@link ActionDelegate} is to represent an action object (which implements {@link IAction}) as
 * an {@link IComponent}.
 * 
 * @see IAction
 */
@SuppressWarnings( "serial" )
public class ActionDelegate extends ComponentBase {

  private ActionHarness actionHarness;

  protected static final PropertyNameFormatter ALTERNATE_INDEX_FORMATTER = new AlternateIndexFormatter();

  protected static final PropertyNameFormatter COMPATIBILITY_FORMATTER = new ActionSequenceCompatibilityFormatter();

  protected static final PropertyNameFormatter STREAM_APPENDER_FORMATTER = new SuffixAppenderFormatter( "Stream" ); //$NON-NLS-1$

  private Object actionBean;

  private IActionInput[] actionDefintionInputs;

  private IActionOutput[] actionDefintionOutputs;

  public ActionDelegate( Object actionBean ) {
    this.actionBean = actionBean;
    actionHarness = new ActionHarness( (IAction) actionBean );
  }

  public Object getActionBean() {
    return actionBean;
  }

  /**
   * Clean-up should happen in the {@link IAction#execute()}
   **/
  @Override
  public void done() {
  }

  /**
   * This method will tell you if an output in the action definition references an output stream that has a
   * global/public destination, such as "response", or "content". An action definition output is considered thusly,
   * if it has a counterpart of the same name in the action sequence outputs AND that output is of type "content"
   * AND it has declared one or more destinations.
   * 
   * @param contentOutput
   *          the action definition output to check
   * @return true if this output corresponds to a public destination-bound output
   */
  protected boolean hasPublicDestination( IActionOutput contentOutput ) {
    String resolvedName = contentOutput.getPublicName();
    IActionSequenceOutput publicOutput = getActionDefinition().getDocument().getOutput( resolvedName );
    if ( publicOutput == null ) {
      return false;
    }
    return ( publicOutput.getType().equals( ActionSequenceDocument.CONTENT_TYPE )
      && publicOutput.getDestinations().length > 0 );
  }

  /**
   * Wires up inputs outputs and resources to an Action and executes it.
   */
  @Override
  protected boolean executeAction() throws Throwable {
    //
    // Set inputs
    //
    InputErrorCallback errorCallback = new InputErrorCallback();
    for ( IActionInput input : getActionDefinition().getInputs() ) {

      Object inputValue = input.getValue();

      if ( input instanceof ActionInputConstant ) {
        // if the input is coming from the component definition section,
        // do parameter replacement on the string and the result of that
        // is the input value
        inputValue = input.getStringValue( true );
      }

      errorCallback.setValue( inputValue );
      actionHarness.setValue( input.getName(), inputValue, errorCallback, COMPATIBILITY_FORMATTER,
          ALTERNATE_INDEX_FORMATTER );
    }

    //
    // Set resources
    //
    ResourceCallback resourceCallback = new ResourceCallback();
    for ( IActionResource res : getActionDefinition().getResources() ) {
      actionHarness.setValue( res.getName(), res.getInputStream(), resourceCallback, COMPATIBILITY_FORMATTER,
          ALTERNATE_INDEX_FORMATTER );
    }

    //
    // Provide output stream for the streaming action. We are going to look for all outputs where
    // type = "content", and derive output streams to hand to the IStreamingAction.
    //
    Map<String, IContentItem> outputContentItems = new HashMap<String, IContentItem>();
    StreamOutputErrorCallback streamingOutputCallback = new StreamOutputErrorCallback();
    OuputStreamGenerator outputStreamGenerator = new OuputStreamGenerator( outputContentItems );

    IActionOutput[] contentOutputs = getActionDefinition().getOutputs( ActionSequenceDocument.CONTENT_TYPE );
    if ( contentOutputs.length > 0 ) {
      for ( IActionOutput contentOutput : contentOutputs ) {
        outputStreamGenerator.setContentOutput( contentOutput );
        actionHarness.setValue( contentOutput.getName(), outputStreamGenerator, streamingOutputCallback,
            STREAM_APPENDER_FORMATTER, COMPATIBILITY_FORMATTER, ALTERNATE_INDEX_FORMATTER );
      }
    }
    // else, This is not necessarily an error condition. Let the action bean decide.

    //
    // Execute the Action if the bean is executable
    //
    if ( actionBean instanceof IAction ) {
      ( (IAction) actionBean ).execute();
    }

    //
    // Get and store outputs
    //
    for ( IActionOutput output : actionDefintionOutputs ) {
      String outputName = output.getName();
      outputName = COMPATIBILITY_FORMATTER.format( outputName );

      // if streaming output, add it to the context and don't try to get it from the Action bean
      if ( outputContentItems.containsKey( outputName ) ) {
        IContentItem contentItem = outputContentItems.get( outputName );

        if ( !( contentItem instanceof SimpleContentItem ) ) {
          // this is a special output for streaming actions and does not require a bean accessor
          output.setValue( contentItem );
        }
      } else if ( actionHarness.isReadable( outputName ) ) {
        Object outputVal = actionHarness.getValue( outputName );
        output.setValue( outputVal );
      } else {
        if ( loggingLevel <= ILogger.WARN ) {
          warn( Messages.getInstance().getString( "ActionDelegate.WARN_OUTPUT_NOT_READABLE", //$NON-NLS-1$
              outputName, output.getType(), actionBean.getClass().getSimpleName() ) );
        }
      }
    }
    return true;
  }

  class StreamOutputErrorCallback implements ValueSetErrorCallback {
    public void failedToSetValue( Object bean, String name, Object value, String destPropertyType, Throwable cause )
      throws ActionExecutionException {
      throw new ActionExecutionException( Messages.getInstance().getErrorString(
          "ActionDelegate.ERROR_0008_FAILED_TO_SET_STREAM", name, OutputStream.class.getName(), //$NON-NLS-1$
          actionBean.getClass().getSimpleName(), destPropertyType ), cause );
    }

    public void propertyNotWritable( Object bean, String name ) {
      if ( loggingLevel <= ILogger.WARN ) {
        warn( Messages.getInstance().getString( "ActionDelegate.WARN_INPUT_NOT_WRITABLE", actionBean //$NON-NLS-1$
            .getClass().getSimpleName(), name, OutputStream.class.getName() ) );
      }
    }
  };

  class OuputStreamGenerator implements ValueGenerator {

    private Map<String, IContentItem> outputContentItems;

    private IActionOutput curActionOutput;

    private boolean streamingCheckPerformed = false;

    public OuputStreamGenerator( Map<String, IContentItem> outputContentItems ) {
      this.outputContentItems = outputContentItems;
    }

    public void setContentOutput( IActionOutput actionOutput ) throws Exception {
      curActionOutput = actionOutput;
    }

    public Object getValue( String name ) throws Exception {
      // fail early if we cannot handle stream outputs
      if ( !streamingCheckPerformed && !( actionBean instanceof IStreamingAction ) ) {
        throw new ActionExecutionException( Messages.getInstance().getErrorString(
            "ActionDelegate.ERROR_0002_ACTION_CANNOT_ACCEPT_STREAM", //$NON-NLS-1$
            name, actionBean.getClass().getSimpleName() ) );
      }
      streamingCheckPerformed = true;

      String mimeType = ( (IStreamingAction) actionBean ).getMimeType( name );
      if ( StringUtils.isEmpty( mimeType ) ) {
        throw new ActionValidationException( Messages.getInstance().getErrorString(
            "ActionDelegate.ERROR_0001_MIMETYPE_NOT_DECLARED" ) ); //$NON-NLS-1$
      }

      IContentItem contentItem = null;

      //
      // If the output is mapped publicly and has a destination associated with it, then we will be asking
      // the current IOuputHandler to create an IContentItem (OuputStream) for us. Otherwise, we will asking the
      // IContentOutputHandler impl registered to handle content destined for "contentrepo" to create
      // an IContentItem (OutputStream) for us.
      //
      if ( hasPublicDestination( curActionOutput ) ) {
        // most output handlers will manage multiple destinations for us and hand us back a MultiContentItem
        contentItem = getRuntimeContext().getOutputContentItem( curActionOutput.getPublicName(), mimeType );
      } else {
        String extension = MimeHelper.getExtension( mimeType );
        if ( extension == null ) {
          extension = ".bin"; //$NON-NLS-1$
        }
        contentItem = getRuntimeContext().getOutputItem( curActionOutput.getName(), mimeType, extension );
      }

      if ( contentItem == null ) {
        // this is the best I can do here to point users to a tangible problem without unwrapping code in
        // RuntimeEngine
        // - AP
        throw new ActionValidationException( Messages.getInstance().getErrorString(
            "ActionDelegate.ERROR_0003_OUTPUT_STREAM_NOT_AVAILABLE_1", //$NON-NLS-1$
            curActionOutput.getPublicName() ) );
      }

      // this will be a MultiOutputStream in the case where there is more than one destination for the content
      // output
      OutputStream contentOutputStream = contentItem.getOutputStream( getActionName() );
      if ( contentOutputStream == null ) {
        throw new ActionExecutionException( Messages.getInstance().getErrorString(
            "ActionDelegate.ERROR_0004_OUTPUT_STREAM_NOT_AVAILABLE_2", //$NON-NLS-1$
            actionBean.getClass().getSimpleName() ) );
      }

      // save this for later when we set the action outputs
      outputContentItems.put( curActionOutput.getName(), contentItem );

      return contentOutputStream;
    }
  }

  class ResourceCallback implements ValueSetErrorCallback {
    public void failedToSetValue( Object bean, String name, Object value, String destPropertyType, Throwable cause )
      throws ActionExecutionException {
      String className = ( value != null ) ? value.getClass().getName() : "ClassNameNotAvailable"; //$NON-NLS-1$
      throw new ActionExecutionException( Messages.getInstance().getErrorString(
          "ActionDelegate.ERROR_0006_FAILED_TO_SET_RESOURCE", //$NON-NLS-1$
          name, className, actionBean.getClass().getSimpleName(), destPropertyType ), cause );
    }

    public void propertyNotWritable( Object bean, String name ) {
      if ( loggingLevel <= ILogger.WARN ) {
        warn( Messages.getInstance().getString( "ActionDelegate.WARN_RESOURCE_NOT_WRITABLE", actionBean //$NON-NLS-1$
            .getClass().getSimpleName(), name, InputStream.class.getName() ) );
      }
    }
  }

  class InputErrorCallback implements ValueSetErrorCallback {
    private Object curValue;

    public void setValue( Object value ) throws Exception {
      curValue = value;
    }

    public void failedToSetValue( Object bean, String name, Object value, String destPropertyType, Throwable cause )
      throws ActionExecutionException {
      String className = ( value != null ) ? value.getClass().getName() : "ClassNameNotAvailable"; //$NON-NLS-1$
      throw new ActionExecutionException( Messages.getInstance().getErrorString(
          "ActionDelegate.ERROR_0005_FAILED_TO_SET_INPUT", //$NON-NLS-1$
          name, className, actionBean.getClass().getSimpleName(), destPropertyType ), cause );
    }

    public void propertyNotWritable( Object bean, String name ) throws Exception {
      if ( loggingLevel <= ILogger.WARN ) {
        // log a warning if there is no way to get this input to the Action
        String valueType = ( curValue == null ) ? null : curValue.getClass().getName();
        warn( Messages.getInstance().getString(
            "ActionDelegate.WARN_INPUT_NOT_WRITABLE", actionBean.getClass().getSimpleName(), //$NON-NLS-1$
            name, valueType ) );
      }
    }
  }

  /**
   * Any initialization can be done in the {@link IPreProcessingAction#doPreExecution()}
   */
  @Override
  public boolean init() {
    return true;
  }

  /**
   * Validation of Action input values should happen in the {@link IAction#execute()} This method is used as a pre
   * execution hook where we setup as much runtime information as possible prior to the actual execute call.
   **/
  @Override
  protected boolean validateAction() {
    if ( actionBean == null ) {
      throw new IllegalArgumentException( Messages.getInstance().getErrorString(
          "ActionDelegate.ERROR_0007_NO_ACTION_BEAN_SPECIFIED" ) ); //$NON-NLS-1$
    }

    //
    // Provide a commons logging logger for logging actions
    // The log name will be the name of the Action class
    //
    if ( actionBean instanceof ILoggingAction ) {
      ( (ILoggingAction) actionBean ).setLogger( LogFactory.getLog( actionBean.getClass() ) );
    }

    //
    // Provide a session to the Action if an ISessionAwareAction
    //
    if ( actionBean instanceof ISessionAwareAction ) {
      ( (ISessionAwareAction) actionBean ).setSession( getSession() );
    }

    actionDefintionInputs = getActionDefinition().getInputs();
    actionDefintionOutputs = getActionDefinition().getOutputs();

    //
    // If an Action is action-definition aware, then here is the place (prior to
    // execution) to tell it about the action definition.
    //
    List<String> inputNames = new ArrayList<String>();
    for ( IActionInput input : actionDefintionInputs ) {
      inputNames.add( input.getName() );
    }
    List<String> outputNames = new ArrayList<String>();
    for ( IActionOutput output : actionDefintionOutputs ) {
      outputNames.add( output.getName() );
    }
    if ( actionBean instanceof IDefinitionAwareAction ) {
      IDefinitionAwareAction definitionAwareAction = (IDefinitionAwareAction) actionBean;
      definitionAwareAction.setInputNames( inputNames );
      definitionAwareAction.setOutputNames( outputNames );
    }

    //
    // Invoke any pre-execution processing if the Action requires it.
    //
    if ( actionBean instanceof IPreProcessingAction ) {
      try {
        ( (IPreProcessingAction) actionBean ).doPreExecution();
      } catch ( ActionPreProcessingException e ) {
        throw new RuntimeException( e );
      }
    }
    // we do not use the return value to indicate failure.
    return true;
  }

  @Override
  protected boolean validateSystemSettings() {
    return true;
  }

  @Override
  public Log getLogger() {
    return LogFactory.getLog( ActionDelegate.class );
  }
}
