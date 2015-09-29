/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.api.engine;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.pentaho.commons.connection.IPentahoStreamSource;
import org.pentaho.platform.api.repository.IContentItem;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This interface defines methods and constants that are used during action execution to resolve parameters, inputs
 * and outputs, resources, and persist the runtime data. Think of the runtime context as working storage for the
 * execution of an action.
 * 
 * @author James Dixon
 */
public interface IRuntimeContext extends IAuditable, ILogger {
  /**
   * Unused
   */
  public static final String FEEDBACK_OUTPUT = "feedback-output"; //$NON-NLS-1$

  /**
   * Indicator of action validation failure
   */
  public static final int RUNTIME_CONTEXT_VALIDATE_FAIL = 0;

  /**
   * Indicator of action validation success
   */
  public static final int RUNTIME_CONTEXT_VALIDATE_OK = 1;

  /**
   * Indicates that the parameters for an action were able to be resolved.
   */
  public static final int RUNTIME_CONTEXT_RESOLVE_OK = 2;

  /**
   * Indicates that parameters for an action could not be resolved.
   */
  public static final int RUNTIME_CONTEXT_RESOLVE_FAIL = 9;

  /**
   * When beginning execution of an action sequence, the status starts off as not started.
   */
  public static final int RUNTIME_STATUS_NOT_STARTED = 3;

  /**
   * Indicator that initialization happened successfully
   */
  public static final int RUNTIME_STATUS_INITIALIZE_OK = 4;

  /**
   * Indicator of initialization failure
   */
  public static final int RUNTIME_STATUS_INITIALIZE_FAIL = 8;

  /**
   * As an action sequence begins execution, the status is set to running.
   */
  public static final int RUNTIME_STATUS_RUNNING = 5;

  /**
   * Indicates that the action executed successfully
   */
  public static final int RUNTIME_STATUS_SUCCESS = 6;

  /**
   * Indicator of action failure.
   */
  public static final int RUNTIME_STATUS_FAILURE = 7;

  /**
   * Indicates an invalid instance ID was passed into the execution of an action sequence
   */
  public static final int RUNTIME_STATUS_SETUP_FAIL = 10;

  /**
   * Currently only used as an indicator that BIRT report specification parameters could not be read.
   */
  public static final int PARAMETERS_FAIL = 1;

  /**
   * Currently only used as an indicator that BIRT report specification parameters could be read properly.
   */
  public static final int PARAMETERS_OK = 2;

  /**
   * Indicates that parameters are required by an action, and the parameters aren't available so they need to be
   * prompted for.
   */
  public static final int PARAMETERS_UI_NEEDED = 3;

  /**
   * Indicates that no parameter prompts are pending.
   */
  public static final int PROMPT_NO = 0;

  /**
   * Indicates that parameter prompts are pending.
   */
  public static final int PROMPT_WAITING = 1;

  /**
   * Indicates that we need to prompt immediately for parameters.
   */
  public static final int PROMPT_NOW = 2;

  /**
   * Returns the unique context identifier. The handle is created during construction of the
   * <code>RuntimeContext</code> object, and should be unique down to the date/time of construction. The default
   * form of this as implemented in <tt>RuntimeContext</tt> includes context- plus the hashcode and the date/time.
   * 
   * @return the unique handle.
   * @see RuntimeContext
   */
  public String getHandle();

  /**
   * During execution of an action sequence, returns the IActionSequence#getTitle().
   * 
   * @return Action sequence title
   * @see IActionSequence#getSequenceTitle()
   */
  public String getActionTitle();

  /** Forces the runtime to stop processing Actions and return to prompt */
  public void promptNow();

  /** Sets the prompt flag but continue processing Actions */
  public void promptNeeded();

  /**
   * Tells if a component is waiting for a prompt
   * 
   * @return true if a prompt is pending
   */
  public boolean isPromptPending();

  /**
   * Returns the unique execution instance. This is typically a GUID that can be used to track the entire execution
   * of an action sequence all the way from beginning to end.
   * 
   * @return unique instance Id
   */
  public String getInstanceId();

  /**
   * Sets the current action sequence
   * 
   * @param actionSequence
   *          The action sequence to validate
   */
  public void setActionSequence( IActionSequence actionSequence );

  public IActionSequence getActionSequence();

  /**
   * Validates the action sequence for consistency
   * 
   * @param sequenceName
   *          The name of the sequence to validate
   * @throws IllegalStateException
   * @throws ActionSequenceException
   */
  public void validateSequence( String sequenceName, IExecutionListener execListener ) throws ActionValidationException;

  /**
   * Executes the action sequence.
   * 
   * @param listener
   *          The listener to be notified when the sequence finishes
   * @param async
   *          Whether the action is synchronous or asynchronous.
   * @return <code>int</code> indicating success
   * @see IRuntimeContext#RUNTIME_STATUS_FAILURE
   * @see IRuntimeContext#RUNTIME_STATUS_SUCCESS
   */
  public void executeSequence( IActionCompleteListener listener, IExecutionListener execListener, boolean async )
    throws ActionSequenceException;

  /**
   * The Url Factory is used for building URL's that link to this or another application
   * 
   * @return the URL Factory
   */
  public IPentahoUrlFactory getUrlFactory();

  /**
   * @return The path within the solution holding the currently executing action sequence
   */
  public String getSolutionPath();

  /**
   * @return The component in the action seqeuence that's being executed
   */
  public String getCurrentComponentName();

  /**
   * @return The session that started execution of the current action sequence
   */
  public IPentahoSession getSession();

  /**
   * Writes a message to the audit log.
   * 
   * @param messageType
   *          Message type as defined in <tt>MessageTypes</tt>
   * @param message
   *          Message to appear in the log
   * @param value
   *          Value of an object to be logged.
   * @param duration
   *          For time-tracked execution paths, indicates the duration the task took
   * @see org.pentaho.platform.api.engine.IAuditEntry
   * @see IAuditable
   * @see org.pentaho.core.audit.MessageTypes
   */
  public void audit( String messageType, String message, String value, long duration );

  /**
   * Returns the named input parameter. This will search amongst all the input parameter providers (
   * <tt>IParameterProvider</tt>) in order until it finds the input parameter and returns it. Throws a
   * <tt>NullPointerException</tt> if the parameter is not found. This method never returns <code>null</code>
   * 
   * @param name
   *          The name of the parameter to get
   * @return The parameter
   * @see org.pentaho.platform.api.engine.services.IParameterProvider
   */
  public IActionParameter getInputParameter( String name );

  /**
   * Returns the named output parameter. This will search amongst all the output parameter providers (
   * <tt>IParameterProvider</tt>) in order until it finds the output parameter and returns it. Throws a
   * <tt>NullPointerException</tt> if the parameter is not found. This method never returns <code>null</code>
   * 
   * @param name
   *          The name of the parameter to get
   * @return The requested parameter
   * @see org.pentaho.platform.api.engine.services.IParameterProvider
   */
  public IActionParameter getOutputParameter( String name );

  /**
   * Gets the named resource definition from the executing action sequence. Throws a <tt>NullPointerException</tt>
   * if the resource is not found. This method never returns <code>null</code>
   * 
   * @param name
   *          The named resource to get
   * @return The resource if it exists.
   * @see IActionSequenceResource
   */
  public IActionSequenceResource getResourceDefintion( String name );

  /**
   * Gets the value of the specified input parameter. Throws a <tt>NullPointerException</tt> if the parameter is
   * not found. This method never returns <code>null</code>
   * 
   * @param name
   *          The named parameter to retrieve
   * @return The <tt>IActionParameter#getValue()</tt>
   * @see IActionParameter
   */
  public Object getInputParameterValue( String name );

  /**
   * Gets the value of the specified input parameter as a <code>String</code>. Throws a
   * <tt>NullPointerException</tt> if the parameter is not found. This method never returns <code>null</code>
   * 
   * @param name
   *          The named parameter to retrieve
   * @return The <tt>IActionParameter#getStringValue()</tt>
   * @see IActionParameter
   */
  public String getInputParameterStringValue( String name );

  /**
   * Gets the named resource as an <tt>InputStream</tt>.
   * 
   * @param actionResource
   *          The resource to get from the <code>SolutionRepository</code>
   * @return The <code>InputStream</code> that contains the resource.
   * @throws FileNotFoundException
   */
  public InputStream getResourceInputStream( IActionSequenceResource actionResource ) throws FileNotFoundException;

  /**
   * Gets the named resource as a <tt>DataSource</tt>.
   * 
   * @param actionResource
   *          The resource to get from the <code>SolutionRepository</tt>
   * @return The <code>DataSource</code>
   * @throws FileNotFoundException
   */
  public IPentahoStreamSource getResourceDataSource( IActionSequenceResource actionResource )
    throws FileNotFoundException;

  /**
   * Gets the named resource as a <tt>String</tt>.
   * 
   * @param actionResource
   *          The resource to get from the <code>SolutionRepository</tt>
   * @return The <code>String</code>
   */
  public String getResourceAsString( IActionSequenceResource actionParameter ) throws IOException;

  /**
   * Gets the named resource as a <tt>Document</tt>.
   * 
   * @param actionResource
   *          The resource to get from the <code>SolutionRepository</tt>
   * @return The <code>DataSource</code>
   */
  public Document getResourceAsDocument( IActionSequenceResource actionParameter ) throws IOException,
    DocumentException;

  /**
   * Sets the value of a named output parameter
   * 
   * @param name
   *          The name of the output parameter
   * @param output
   *          The value to set the output parameter to
   */
  public void setOutputValue( String name, Object output );

  /**
   * Adds a parameter to the current inputs. A component can be use this to create parameters for internal use or
   * for new outputs.
   * 
   * @param name
   *          The name of the temporary parameter
   * @param output
   *          The value to set the temporary parameter to
   */
  public void addTempParameter( String name, IActionParameter output );

  /**
   * Returns an output stream for writing.
   * 
   * @param outputName
   *          The name of the output
   * @param mimeType
   *          The mime type of the output
   * @param extension
   *          The file extension of the output
   * @return OutputStream for writing to
   */
  public IContentItem getOutputItem( String outputName, String mimeType, String extension );

  /**
   * Returns an input stream from an input parameter, if the input parameter is a content item.
   * 
   * @param parameterName
   *          The name of the parameter
   * @return An InputStream from the content item
   */
  public InputStream getInputStream( String parameterName );

  /**
   * Get's the content item associated with the parameter, and returns the content item's datasource
   * 
   * @param parameterName
   *          The name of the parameter
   * @return The IPentahoStreamSource from the Content Item
   * @see IContentItem#getDataSource()
   */
  public IPentahoStreamSource getDataSource( String parameterName );

  /**
   * @return a <tt>Set</tt> containing all the inputs in the current action.
   */
  @SuppressWarnings( "rawtypes" )
  public Set getInputNames();

  /**
   * @return a <tt>Set</tt> containing the resource names in the action
   */
  @SuppressWarnings( "rawtypes" )
  public Set getResourceNames();

  /**
   * @return a <tt>Set</tt> containing the output names in the current action
   */
  @SuppressWarnings( "rawtypes" )
  public Set getOutputNames();

  /**
   * Does parameter substitution on the input string, searching for all parameter declarations in the input string,
   * and substituting the value from the matching input parameter. In other words, it replaces {REGION} with the
   * value of the input parameter called REGION.
   * 
   * @param format
   *          The string containing possible parameter references
   * @return String with parameters resolved.
   */
  public String applyInputsToFormat( String format );

  /**
   * Does parameter substitution on the input string, searching for all parameter declarations in the input string,
   * and substituting the value from the matching input parameter. In other words, it replaces {REGION} with the
   * value of the input parameter called REGION.
   * 
   * @param format
   *          The string containing possible parameter references
   * @param Resolver
   *          for parameters for overriding behavior
   * @return String with parameters resolved.
   */
  public String applyInputsToFormat( String format, IParameterResolver resolver );

  /**
   * Adds an input parameter to the list of all inputs for the action sequence
   * 
   * @param name
   *          The name of the parameter (the key to the parameter map)
   * @param param
   *          The parameter to add
   * @see IActionParameter
   */
  public void addInputParameter( String name, IActionParameter param );

  /**
   * @return true if the current output device allows user feedback (i.e. parameter input forms)
   */
  public boolean feedbackAllowed();

  /**
   * Interfaces to the current output handler to get the content item that is handling feedback (i.e. parameter
   * input forms)
   * 
   * @return the Content Item for user input
   * @see IContentItem
   * @see IOutputHandler
   */
  public IContentItem getFeedbackContentItem();

  /**
   * Interfaces to the current context to get the content items which was generated
   * 
   * @return The content item for output
   * @see IContentItem
   */
  public List<IContentItem> getOutputContentItems();

  /**
   * Interfaces to the current output handler to get the content item that describes the output from this request's
   * component execution.
   * 
   * @return The content item for output
   * @see IContentItem
   * @see IOutputHandler
   */
  public IContentItem getOutputContentItem( String mimeType );

  /**
   * Interfaces to the current output handler to get the named content item from this request's component
   * execution.
   * 
   * @param outputName
   *          the name of the output
   * @return The requested content item
   * @see IContentItem
   * @see IOutputHandler
   */
  public IContentItem getOutputContentItem( String outputName, String mimeType );

  /**
   * Generates a parameter acquisition form for required parameters. This writes directly to the output stream
   * provided by the output handler.
   * 
   * @throws ActionSequenceException
   * @see IOutputHandler
   */
  public void sendFeedbackForm() throws ActionSequencePromptException;

  public void setCreateFeedbackParameterCallback( ICreateFeedbackParameterCallback callback );

  /**
   * @deprecated
   * 
   *             Adds a feedback parameter for prompts based on an Action Parameter. Uses the Selections defined in
   *             the Action Parameter for the options and sets the default to the current value
   * 
   * @param actionParam
   *          The Action Parameter to use as the model for the prompt
   */
  @Deprecated
  public void createFeedbackParameter( IActionParameter actionParam );

  /**
   * Adds a feedback parameter (essentially a form input field) for a required input.
   * 
   * @param selMap
   *          Maps <code>IPentahoResultSet</code> objects to selection objects
   * @param fieldName
   *          Name of the form field
   * @param defaultValues
   *          default values for the input field
   * @see SelectionMapper
   */
  public void createFeedbackParameter( ISelectionMapper selMap, String fieldName, Object defaultValues );

  /**
   * Adds a feedback parameter (essentially a form input field) for a required input.
   * 
   * @param selMap
   *          Maps <code>IPentahoResultSet</code> objects to selection objects
   * @param fieldName
   *          Name of the form field
   * @param defaults
   *          default values for the input field
   * @param optional
   *          specifies if the feedback parameter is required or not
   * @see SelectionMapper
   */
  public void createFeedbackParameter( ISelectionMapper selMap, String fieldName, Object defaults, boolean optional );

  /**
   * Adds a scalar feedback parameter
   * 
   * @param fieldName
   *          Name of the input field
   * @param displayName
   *          display name of the input field
   * @param hint
   *          Fly-over hint for the input field
   * @param defaultValue
   *          Default value for the input field
   * @param visible
   *          Whether the input field is visible or not
   * @see XForm
   */
  public void createFeedbackParameter( String fieldName, String displayName, String hint, Object defaultValue,
      boolean visible );

  /**
   * Adds a scalar feedback parameter
   * 
   * @param fieldName
   *          Name of the input field
   * @param displayName
   *          display name of the input field
   * @param hint
   *          Fly-over hint for the input field
   * @param defaultValue
   *          Default value for the input field
   * @param visible
   *          Whether the input field is visible or not
   * @param optional
   *          specifies if the feedback parameter is required or not
   * @see XForm
   */
  public void createFeedbackParameter( String fieldName, String displayName, String hint, Object defaultValue,
      boolean visible, boolean optional );

  /**
   * Creates a feedback parameter that uses a list for the values
   * 
   * @param fieldName
   *          The name of the field
   * @param displayName
   *          Display name
   * @param hint
   *          Fly-over hint for the input field
   * @param defaultValues
   *          Default value of the input field
   * @param values
   *          List of values
   * @param dispNames
   *          Map of display names
   * @param displayStyle
   *          how to display the control
   * @see XForm
   */
  @SuppressWarnings( "rawtypes" )
  public void createFeedbackParameter( String fieldName, String displayName, String hint, Object defaultValues,
      List values, Map dispNames, String displayStyle );

  /**
   * Creates a feedback parameter that uses a list for the values
   * 
   * @param fieldName
   *          The name of the field
   * @param displayName
   *          Display name
   * @param hint
   *          Fly-over hint for the input field
   * @param defaultValues
   *          Default value of the input field
   * @param values
   *          List of values
   * @param dispNames
   *          Map of display names
   * @param displayStyle
   *          how to display the control
   * @parm optional specifies if the feedback parameter is required or not
   * @see XForm
   */
  @SuppressWarnings( "rawtypes" )
  public void createFeedbackParameter( String fieldName, String displayName, String hint, Object defaultValues,
      List values, Map dispNames, String displayStyle, boolean optional );

  /**
   * @return the current status of execution
   */
  public int getStatus();

  /**
   * @return List of messages saved up during execution. This is used to provide failure feedback to the user.
   */
  @SuppressWarnings( "rawtypes" )
  public List getMessages();

  /**
   * Creates a new runtime context that is a child of this instance
   * 
   * @param persisted
   *          Should the runtime data be persisted
   * @return Instance id of the new RuntimeContext
   */
  public String createNewInstance( boolean persisted );

  /**
   * Creates a new runtime context that is a child of this instance and sets attributes of the runtime data from
   * the parameter Map
   * 
   * @param persisted
   *          Should the runtime data be persisted
   * @param parameters
   *          parameters for the new instance
   * @return Instance id of the new RuntimeContext
   */
  @SuppressWarnings( "rawtypes" )
  public String createNewInstance( boolean persisted, Map parameters );

  /**
   * Creates a new runtime context that is a child of this instance and sets attributes of the runtime data from
   * the parameter Map, and can optionally cause the new instance to be forcibly written to the underlying
   * persistence mechanism.
   * 
   * @param persisted
   *          Should the runtime data be persisted
   * @param parameters
   *          parameters for the new instance
   * @param forceImmediateWrite
   *          if true, will call the new runtime element's forceSave method before returning.
   * @return Instance id of the new RuntimeContext
   */
  @SuppressWarnings( "rawtypes" )
  public String createNewInstance( boolean persisted, Map parameters, boolean forceImmediateWrite );

  public void dispose();

  @SuppressWarnings( "rawtypes" )
  public void dispose( List exceptParameters );

  /**
   * Sets the xsl file to be used to generate the parameter page for the current component. The parameter should be
   * a full path from the solution root starting with a /, or it should be a path relative to the directory of the
   * current action sequence.
   * 
   * @param xsl
   *          The name of the XSL file
   */
  public void setParameterXsl( String xsl );

  /**
   * Sets the target window that the content will be displayed in. This name is used at the target in an
   * Window.open() javascript call made when the submit button on the parameter page is clicked.
   * 
   * @param target
   *          Window name
   */
  public void setParameterTarget( String target );

  /**
   * Forces the immediate write of runtime data to underlying persistence mechanism. In the case of using Hibernate
   * for the runtime data persistence, this works out to a call to HibernateUtil.flush().
   */
  public void forceSaveRuntimeData();

  /**
   * Gets the output type prefered by the handler. Values are defined in
   * org.pentaho.platform.api.engine.IOutputHandler and are OUTPUT_TYPE_PARAMETERS, OUTPUT_TYPE_CONTENT, or
   * OUTPUT_TYPE_DEFAULT
   * 
   * @return Output type
   */
  public int getOutputPreference();

  /**
   * Sets the output handler for the runtime context
   * 
   * @param outputHandler
   *          The output handler
   * 
   */
  public void setOutputHandler( IOutputHandler outputHandler );

  /**
   * Sets the default prompt status PROMPT_NO, PROMPT_WAITING, PROMPT_NOW
   * 
   * @param status
   */
  public void setPromptStatus( int status );

  public IParameterManager getParameterManager();

  @SuppressWarnings( "rawtypes" )
  public Map getParameterProviders();

}
