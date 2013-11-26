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

package org.pentaho.platform.plugin.action.examples;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Node;
import org.pentaho.actionsequence.dom.IActionDefinition;
import org.pentaho.commons.connection.IPentahoStreamSource;
import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.engine.IComponent;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISelectionMapper;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.api.util.IVersionHelper;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.solution.StandardSettings;
import org.pentaho.platform.plugin.action.messages.Messages;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ComponentImplementationExample extends Object implements IComponent {

  protected int loggingLevel = ILogger.UNKNOWN;

  public static final String LOGID_MASK1 = "{0}:{1}:{2}: "; //$NON-NLS-1$

  public static final String LOGID_MASK2 = "{0}:{1}:{2}:{3} "; //$NON-NLS-1$

  public static final String LOGID_SEPARATOR = ":"; //$NON-NLS-1$

  public String EMPTYLOGID = "::: "; //$NON-NLS-1$

  private String logId = EMPTYLOGID;

  protected static final boolean debug = PentahoSystem.debug;

  private IRuntimeContext runtimeContext;

  private IPentahoSession sessionContext;

  private String processId;

  private String actionName;

  private String instanceId;

  private String id;

  private boolean baseInitOk;

  private boolean componentInitOk;

  private Node componentDefinition;

  private Map<String, String> componentDefinitionMap;

  private HashMap settings;

  private IActionDefinition actionDefinition;

  protected boolean executeAction() throws Throwable {
    // TODO add your execute code here
    return false;
  }

  public boolean validateAction() {
    // TODO add any validation code in here and return true
    return false;
  }

  protected boolean validateSystemSettings() {
    // TODO add any validation of system settings here and return true
    return false;
  }

  public void done() {
    // TODO add any cleanup code here
  }

  public boolean init() {
    // TODO add any initialization code and return true
    return false;
  }

  public void setLogId( final String lId ) {
    logId = lId;
  }

  public Log getLogger() {
    return LogFactory.getLog( this.getClass() );
  }

  public void genLogIdFromSession( final IPentahoSession sess ) {
    genLogIdFromInfo( sess.getId() != null ? sess.getId() : "", //$NON-NLS-1$
        sess.getProcessId() != null ? sess.getProcessId() : "", //$NON-NLS-1$
        sess.getActionName() != null ? sess.getActionName() : "" //$NON-NLS-1$
    );
  }

  public void genLogIdFromInfo( final String sessId, final String procId, final String actName ) {
    Object[] args = { sessId, procId, actName };
    setLogId( MessageFormat.format( ComponentImplementationExample.LOGID_MASK1, args ) );
  }

  public void genLogIdFromInfo( final String sessId, final String procId, final String actName, final String instId ) {
    Object[] args = { sessId, procId, actName, instId };
    setLogId( MessageFormat.format( ComponentImplementationExample.LOGID_MASK2, args ) );
  }

  /* ILogger Implementation */

  public String getObjectName() {
    return this.getClass().getName();
  }

  public int getLoggingLevel() {
    return loggingLevel;
  }

  public void setLoggingLevel( final int logLevel ) {
    this.loggingLevel = logLevel;
  }

  private List messages;

  public List getMessages() {
    return messages;
  }

  public void setMessages( final List messages ) {
    this.messages = messages;
  }

  public void trace( final String message ) {
    if ( loggingLevel <= ILogger.TRACE ) {
      if ( messages != null ) {
        messages.add( Messages.getInstance().getString( "Message.USER_DEBUG", message, getClass().getName() ) ); //$NON-NLS-1$
      }
      getLogger().trace( getLogId() + message );
    }
  }

  public void debug( final String message ) {
    if ( loggingLevel <= ILogger.DEBUG ) {
      if ( messages != null ) {
        messages.add( Messages.getInstance().getString( "Message.USER_DEBUG", message, getClass().getName() ) ); //$NON-NLS-1$
      }
      getLogger().debug( getLogId() + message );
    }
  }

  public void info( final String message ) {
    if ( loggingLevel <= ILogger.INFO ) {
      if ( messages != null ) {
        messages.add( Messages.getInstance().getString( "Message.USER_INFO", message, getClass().getName() ) ); //$NON-NLS-1$
      }
      getLogger().info( getLogId() + message );
    }
  }

  public void warn( final String message ) {
    if ( loggingLevel <= ILogger.WARN ) {
      if ( messages != null ) {
        messages.add( Messages.getInstance().getString( "Message.USER_WARNING", message, getClass().getName() ) ); //$NON-NLS-1$
      }
      getLogger().warn( getLogId() + message );
    }
  }

  public void error( final String message ) {
    if ( loggingLevel <= ILogger.ERROR ) {
      if ( messages != null ) {
        messages.add( Messages.getInstance().getString( "Message.USER_ERROR", message, getClass().getName() ) ); //$NON-NLS-1$
      }
      getLogger().error( getLogId() + message );
    }
  }

  public void fatal( final String message ) {
    if ( loggingLevel <= ILogger.FATAL ) {
      if ( messages != null ) {
        messages.add( Messages.getInstance().getString( "Message.USER_ERROR", message, getClass().getName() ) ); //$NON-NLS-1$
      }
      getLogger().fatal( getLogId() + message );
    }
  }

  public void trace( final String message, final Throwable error ) {
    if ( loggingLevel <= ILogger.TRACE ) {
      if ( messages != null ) {
        messages.add( Messages.getInstance().getString( "Message.USER_DEBUG", message, getClass().getName() ) ); //$NON-NLS-1$
      }
      getLogger().trace( getLogId() + message, error );
    }
  }

  public void debug( final String message, final Throwable error ) {
    if ( loggingLevel <= ILogger.DEBUG ) {
      if ( messages != null ) {
        messages.add( Messages.getInstance().getString( "Message.USER_DEBUG", message, getClass().getName() ) ); //$NON-NLS-1$
      }
      getLogger().debug( getLogId() + message, error );
    }
  }

  public void info( final String message, final Throwable error ) {
    if ( loggingLevel <= ILogger.INFO ) {
      if ( messages != null ) {
        messages.add( Messages.getInstance().getString( "Message.USER_INFO", message, getClass().getName() ) ); //$NON-NLS-1$
      }
      getLogger().info( getLogId() + message, error );
    }
  }

  public void warn( final String message, final Throwable error ) {
    if ( loggingLevel <= ILogger.WARN ) {
      if ( messages != null ) {
        messages.add( Messages.getInstance().getString( "Message.USER_WARNING", message, getClass().getName() ) ); //$NON-NLS-1$
      }
      getLogger().warn( getLogId() + message, error );
    }
  }

  public void error( final String message, final Throwable error ) {
    if ( loggingLevel <= ILogger.ERROR ) {
      if ( messages != null ) {
        messages.add( Messages.getInstance().getString( "Message.USER_ERROR", message, getClass().getName() ) ); //$NON-NLS-1$
      }
      IVersionHelper versionHelper = PentahoSystem.get( IVersionHelper.class, null );
      getLogger().error( "Error Start: Pentaho " + versionHelper.getVersionInformation( this.getClass() ) ); //$NON-NLS-1$
      getLogger().error( getLogId() + message, error );
      getLogger().error( "Error end:" ); //$NON-NLS-1$ 
    }
  }

  public void fatal( final String message, final Throwable error ) {
    if ( loggingLevel <= ILogger.FATAL ) {
      if ( messages != null ) {
        messages.add( Messages.getInstance().getString( "Message.USER_ERROR", message, getClass().getName() ) ); //$NON-NLS-1$
      }
      IVersionHelper versionHelper = PentahoSystem.get( IVersionHelper.class, null );
      getLogger().error( "Error: Pentaho " + versionHelper.getVersionInformation( this.getClass() ) ); //$NON-NLS-1$
      getLogger().fatal( getLogId() + message, error );
      getLogger().error( "Error end:" ); //$NON-NLS-1$ 
    }
  }

  public static String getUserString( final String type ) {
    return Messages.getInstance().getString( "Message.USER_" + type ); //$NON-NLS-1$
  }

  public void setInstanceId( final String instanceId ) {
    this.instanceId = instanceId;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setActionName( final String actionName ) {
    this.actionName = actionName;
  }

  public String getActionName() {
    return actionName;
  }

  public void setProcessId( final String processId ) {
    this.processId = processId;
  }

  public String getProcessId() {
    return processId;
  }

  public void setComponentDefinitionMap( final Map<String, String> componentDefinitionMap ) {
    this.componentDefinitionMap = componentDefinitionMap;
  }

  public void setComponentDefinition( final Node componentDefinition ) {
    this.componentDefinition = componentDefinition;
  }

  public Node getComponentDefinition() {
    return componentDefinition;
  }

  public void setRuntimeContext( final IRuntimeContext runtimeContext ) {
    this.runtimeContext = runtimeContext;
  }

  public IRuntimeContext getRuntimeContext() {
    return runtimeContext;
  }

  public void setSession( final IPentahoSession session ) {
    this.sessionContext = session;
  }

  public IPentahoSession getSession() {
    return sessionContext;
  }

  protected void saveSetting( final String name, final Object value ) {
    settings.put( name, value );
  }

  protected Object getSetting( final String name ) {
    return settings.get( name );
  }

  protected String getStringSetting( final String name ) {
    Object value = settings.get( name );
    if ( value == null ) {
      return null;
    } else if ( value instanceof String ) {
      return (String) value;
    } else {
      return value.toString();
    }
  }

  public String getLogId() {
    return logId;
  }

  protected boolean isDefinedInput( final String inputName ) {

    if ( runtimeContext.getInputNames().contains( inputName ) ) {
      return true;
    } else {
      return getComponentSetting( inputName ) != null;
    }
  }

  protected boolean isDefinedOutput( final String outputName ) {
    return runtimeContext.getOutputNames().contains( outputName );
  }

  protected boolean isDefinedResource( final String resourceName ) {
    return runtimeContext.getResourceNames().contains( resourceName );
  }

  public final int validate() {

    logId = Messages.getInstance().getString( "Base.CODE_LOG_ID", instanceId, runtimeContext.getHandle(), actionName ); //$NON-NLS-1$
    if ( ComponentImplementationExample.debug ) {
      debug( Messages.getInstance().getString( "Base.DEBUG_VALIDATING_COMPONENT", actionName ) ); //$NON-NLS-1$
      // grab the parameters first
    }

    id = Messages.getInstance().getString( "Base.CODE_COMPONENT_ID", processId, actionName ); //$NON-NLS-1$

    // now get picky about values
    baseInitOk =
        ( ( instanceId != null ) && ( sessionContext != null ) && ( processId != null ) && ( actionName != null ) );

    boolean systemSettingsValidate = validateSystemSettings();

    if ( baseInitOk && systemSettingsValidate ) {
      componentInitOk = validateAction();
    }
    if ( getInitOk() ) {
      return IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_OK;
    }
    return IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_FAIL;
  }

  public boolean getInitOk() {
    return baseInitOk && componentInitOk;
  }

  protected Set getOutputNames() {
    return runtimeContext.getOutputNames();
  }

  protected Set getInputNames() {
    return runtimeContext.getInputNames();
  }

  protected Set getResourceNames() {
    return runtimeContext.getResourceNames();
  }

  protected boolean feedbackAllowed() {
    return runtimeContext.feedbackAllowed();
  }

  protected IActionSequenceResource getResource( final String resourceName ) {
    return runtimeContext.getResourceDefintion( resourceName );
  }

  protected InputStream getResourceInputStream( final IActionSequenceResource resource ) throws FileNotFoundException {
    return runtimeContext.getResourceInputStream( resource );
  }

  protected InputStream getInputStream( final String inputName ) {
    return runtimeContext.getInputStream( inputName );
  }

  protected int getOutputPreference() {
    return runtimeContext.getOutputPreference();
  }

  protected IContentItem getOutputItem( final String outputName, final String mimeType, final String extension ) {
    return runtimeContext.getOutputItem( outputName, mimeType, extension );
  }

  protected void audit( final String messageType, final String message, final String value, final int duration ) {
    runtimeContext.audit( messageType, message, value, duration );
  }

  protected boolean getInputBooleanValue( final String inputName, final boolean defaultValue ) {
    String strValue = getInputStringValue( inputName );
    if ( strValue == null ) {
      return defaultValue;
    } else if ( "true".equalsIgnoreCase( strValue ) ) { //$NON-NLS-1$
      return true;
    } else if ( "false".equalsIgnoreCase( strValue ) ) { //$NON-NLS-1$
      return false;
    } else {
      return defaultValue;
    }

  }

  protected long getInputLongValue( final String inputName, final long defaultValue ) {
    String strValue = getInputStringValue( inputName );
    if ( strValue == null ) {
      return defaultValue;
    }
    try {
      return Long.parseLong( strValue );
    } catch ( Exception e ) {
      return defaultValue;
    }

  }

  protected String getInputStringValue( final String inputName ) {
    // first check to see if we have an input parameter that we can use for
    // this.
    String value = null;
    if ( runtimeContext.getInputNames().contains( inputName ) ) {
      value = runtimeContext.getInputParameterStringValue( inputName );
    } else if ( componentDefinitionMap.containsKey( inputName ) ) {
      value = componentDefinitionMap.get( inputName );
    } else {
      // now check the component node from the action definition.
      Node node = componentDefinition.selectSingleNode( inputName );
      if ( node == null ) {
        return null;
      }
      value = node.getText();
    }
    value = this.applyInputsToFormat( value );
    return value;
  }

  protected Object getInputValue( final String inputName ) {
    // first check to see if we have an input parameter that we can use for
    // this.
    if ( runtimeContext.getInputNames().contains( inputName ) ) {
      return runtimeContext.getInputParameterValue( inputName );
    }
    // now check the component node from the action definition.
    Node node = componentDefinition.selectSingleNode( inputName );
    if ( node == null ) {
      return null;
    }
    return node.getText();
  }

  private String getComponentSetting( final String path ) {
    // first check to see if we have an input parameter that we can use for
    // this.
    if ( runtimeContext.getInputNames().contains( path ) ) {
      return runtimeContext.getInputParameterStringValue( path );
    }
    // now check the component node from the action definition.
    Node node = componentDefinition.selectSingleNode( path );
    if ( node == null ) {
      return null;
    }
    return node.getText();
  }

  public void promptNeeded() {
    runtimeContext.promptNeeded();
  }

  public void promptNow() {
    runtimeContext.promptNow();
  }

  public String getResourceAsString( final IActionSequenceResource resource ) {
    try {
      return runtimeContext.getResourceAsString( resource );
    } catch ( Exception e ) {
      return null;
    }
  }

  public String getInitFailMessage() {
    return null;
  }

  public String createNewInstance( final boolean persisted, final Map parameters, final boolean forceImmediateWrite ) {
    return runtimeContext.createNewInstance( persisted, parameters, forceImmediateWrite );
  }

  public void inputMissingError( final String paramName ) {
    error( Messages.getInstance().getErrorString( "ComponentBase.ERROR_0003_INPUT_PARAM_MISSING", paramName ) ); //$NON-NLS-1$
  }

  public void outputMissingError( final String paramName ) {
    error( Messages.getInstance().getErrorString( "ComponentBase.ERROR_0004_OUTPUT_PARAM_MISSING", paramName ) ); //$NON-NLS-1$
  }

  public void resourceMissingError( final String paramName ) {
    error( Messages.getInstance().getErrorString( "ComponentBase.ERROR_0005_RESOURCE_PARAM_MISSING", paramName ) ); //$NON-NLS-1$
  }

  public void resourceComponentSettingError( final String paramName ) {
    error( Messages.getInstance()
        .getErrorString( "ComponentBase.ERROR_0006_COMPONENT_SETTING_PARAM_MISSING", paramName ) ); //$NON-NLS-1$
  }

  public int execute() {

    // see if we have a custom XSL for the parameter page, if required
    String xsl = getComponentSetting( "xsl" ); //$NON-NLS-1$
    if ( xsl != null ) {
      runtimeContext.setParameterXsl( xsl );
    }

    // see if we have a target window for the output
    String target = getComponentSetting( "target" ); //$NON-NLS-1$
    if ( target != null ) {
      runtimeContext.setParameterTarget( target );
    }

    if ( loggingLevel == ILogger.UNKNOWN ) {
      warn( Messages.getInstance().getString( "Base.WARNING_LOGGING_LEVEL_UNKNOWN" ) ); //$NON-NLS-1$
      loggingLevel = ILogger.DEBUG;
    }
    int result = IRuntimeContext.RUNTIME_STATUS_FAILURE;

    if ( sessionContext == null ) {
      error( Messages.getInstance().getErrorString( "Base.ERROR_0001_INVALID_SESSION" ) ); //$NON-NLS-1$
      return result;
    }

    if ( ComponentImplementationExample.debug ) {
      debug( Messages.getInstance().getString( "Base.DEBUG_VALIDATION_RESULT" ) + getInitOk() ); //$NON-NLS-1$
    }
    if ( !getInitOk() ) {
      return result;
    }

    try {
      result = ( executeAction() ? IRuntimeContext.RUNTIME_STATUS_SUCCESS : IRuntimeContext.RUNTIME_STATUS_FAILURE );
      if ( ( result == IRuntimeContext.RUNTIME_STATUS_SUCCESS ) && runtimeContext.isPromptPending() ) {
        // see if we need to prevent further components from executing
        if ( isDefinedInput( StandardSettings.HANDLE_ALL_PROMPTS ) ) {
          runtimeContext.promptNow();
        }
      }
    } catch ( Throwable e ) {
      error( Messages.getInstance().getErrorString( "Base.ERROR_0002_EXECUTION_FAILED" ), e ); //$NON-NLS-1$
    }
    return result;
  }

  public String getId() {
    return id;
  }

  public String getActionTitle() {
    return runtimeContext.getActionTitle();
  }

  protected IContentItem getOutputContentItem( final String mimeType ) {
    return runtimeContext.getOutputContentItem( mimeType );
  }

  protected IContentItem getOutputContentItem( final String outputName, final String mimeType ) {
    return runtimeContext.getOutputContentItem( outputName, mimeType );
  }

  protected void setOutputValue( final String outputName, final Object value ) {
    runtimeContext.setOutputValue( outputName, value );
  }

  protected OutputStream getDefaultOutputStream( final String mimeType ) {
    IContentItem contentItem = runtimeContext.getOutputContentItem( mimeType );
    if ( contentItem != null ) {
      try {
        return contentItem.getOutputStream( getActionName() );
      } catch ( Exception e ) {
        //ignore
      }
    }
    return null;
  }

  protected String applyInputsToFormat( final String format ) {
    return runtimeContext.applyInputsToFormat( format );
  }

  protected IActionParameter getOutputItem( final String outputName ) {
    return runtimeContext.getOutputParameter( outputName );
  }

  protected String getSolutionName() {
    return "";
  }

  protected String getSolutionPath() {
    return runtimeContext.getSolutionPath();
  }

  protected IActionParameter getInputParameter( final String parameterName ) {
    return runtimeContext.getInputParameter( parameterName );
  }

  protected boolean isPromptPending() {
    return runtimeContext.isPromptPending();
  }

  protected void setFeedbackMimeType( final String mimeType ) {
    IContentItem feedbackContentItem = runtimeContext.getFeedbackContentItem();
    feedbackContentItem.setMimeType( mimeType );
  }

  protected void setOutputMimeType( final String mimeType ) {
    IContentItem outputContentItem = runtimeContext.getOutputContentItem( mimeType );
    outputContentItem.setMimeType( mimeType );
  }

  protected OutputStream getFeedbackOutputStream() {
    IContentItem feedbackContentItem = runtimeContext.getFeedbackContentItem();
    if ( feedbackContentItem != null ) {
      try {
        return feedbackContentItem.getOutputStream( getActionName() );
      } catch ( Exception e ) {
        //ignore
      }
    }
    return null;
  }

  @SuppressWarnings( "deprecation" )
  protected void createFeedbackParameter( final IActionParameter actionParam ) {
    runtimeContext.createFeedbackParameter( actionParam );
    runtimeContext.promptNeeded();
  }

  protected void createFeedbackParameter( final ISelectionMapper selMap, final String fieldName,
      final Object defaultValues ) {
    runtimeContext.createFeedbackParameter( selMap, fieldName, defaultValues );
    runtimeContext.promptNeeded();
  }

  protected void createFeedbackParameter( final String fieldName, final String displayName, final String hint,
      final String defaultValue, final boolean visible ) {
    runtimeContext.createFeedbackParameter( fieldName, displayName, hint, defaultValue, visible );
    runtimeContext.promptNeeded();
  }

  public void createFeedbackParameter( final String fieldName, final String displayName, final String hint,
      final Object defaultValues, final List values, final Map dispNames, final String displayStyle ) {
    runtimeContext.createFeedbackParameter( fieldName, displayName, hint, defaultValues, values, dispNames,
      displayStyle );
    runtimeContext.promptNeeded();
  }

  protected IPentahoStreamSource getResourceDataSource( final IActionSequenceResource resource )
    throws FileNotFoundException {
    return runtimeContext.getResourceDataSource( resource );
  }

  public void setActionDefinition( final IActionDefinition actionDefinition ) {
    this.actionDefinition = actionDefinition;
  }

  public IActionDefinition getActionDefinition() {
    return actionDefinition;
  }
}
