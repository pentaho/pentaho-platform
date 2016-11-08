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

import org.dom4j.Node;
import org.pentaho.actionsequence.dom.IActionDefinition;
import org.pentaho.commons.connection.IPentahoStreamSource;
import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.engine.IComponent;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IParameterResolver;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISelectionMapper;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.PentahoMessenger;
import org.pentaho.platform.engine.services.actionsequence.ActionParameter;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.util.messages.LocaleHelper;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

/**
 */
public abstract class ComponentBase extends PentahoMessenger implements IComponent, IParameterResolver {

  private static final long serialVersionUID = -1344533990604702214L;

  protected static final String UNKNOWN_COMPONENT_ID = "unknown"; //$NON-NLS-1$

  public static final String MISSING_SESSION = "session missing"; //$NON-NLS-1$

  public static final String COMPONENT_EXECUTE_FAIL = "component failed"; //$NON-NLS-1$

  protected static final boolean debug = PentahoSystem.debug;

  private IRuntimeContext runtimeContext;

  private IPentahoSession sessionContext;

  private String processId;

  private String actionName;

  private String instanceId;

  private String id;

  private boolean baseInitOk;

  private boolean componentInitOk;

  // private int loggingLevel = UNKNOWN;
  private String logId;

  private Node componentDefinition;

  private Map<String, String> componentDefinitionMap;

  private IActionDefinition actionDefinition;

  private final HashMap settings = new HashMap();

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

  public void setComponentDefinition( final Node componentDefinition ) {
    this.componentDefinition = componentDefinition;
  }

  public void setComponentDefinitionMap( final Map<String, String> componentDefinitionMap ) {
    this.componentDefinitionMap = componentDefinitionMap;
  }

  public Node getComponentDefinition() {
    return getComponentDefinition( false );
  }

  /**
   * Return the xml Node containing the component's definition. If <code>process</code> is true, visit every child
   * node in the tree, and if the child node's text is an action parameter convert it to it's value. (See doc for
   * applyInputsToFormat())
   * 
   * @param process
   *          if true, if the node's text represents a parameter, convert the parameter to it's value, and assign
   *          the value to the node's text.
   * 
   * @return Node containing this component's definition.
   */
  @SuppressWarnings( "unchecked" )
  public Node getComponentDefinition( final boolean process ) {
    if ( process ) {
      List nodes = componentDefinition.selectNodes( "//*" ); //$NON-NLS-1$
      Iterator it = nodes.iterator();
      while ( it.hasNext() ) {
        Node node = (Node) it.next();
        String txt = node.getText();
        if ( ( txt != null ) && !node.isReadOnly() ) {
          node.setText( applyInputsToFormat( txt ) );
        }
      }
    }
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

  protected abstract boolean validateAction();

  protected abstract boolean validateSystemSettings();

  public abstract void done();

  protected abstract boolean executeAction() throws Throwable;

  public abstract boolean init();

  @Override
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
    if ( ComponentBase.debug ) {
      debug( Messages.getInstance().getString( "Base.DEBUG_VALIDATING_COMPONENT", actionName ) ); //$NON-NLS-1$
      // grab the parameters first
    }

    id = Messages.getInstance().getString( "Base.CODE_COMPONENT_ID", processId, actionName ); //$NON-NLS-1$

    // now get picky about values
    baseInitOk =
        ( ( instanceId != null ) && ( sessionContext != null ) && ( processId != null ) && ( actionName != null ) );

    boolean systemSettingsValidate = validateSystemSettings();

    if ( baseInitOk && systemSettingsValidate ) {
      try {
        componentInitOk = validateAction();
      } catch ( Exception e ) {
        error( Messages.getInstance().getErrorString( "Base.ERROR_0004_VALIDATION_FAILED" ), e ); //$NON-NLS-1$                
      }
    }
    if ( getInitOk() ) {
      return IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_OK;
    }
    return IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_FAIL;
  }

  public int resolveParameter( final String template, final String parameterName, final Matcher parameterMatcher,
      final int copyStart, final StringBuffer result ) {
    // Overriding components should return non-negative value if they handle resolving the parameter
    return -1;
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
    return resource.getInputStream( RepositoryFilePermission.READ, LocaleHelper.getLocale() );
  }

  protected InputStream getInputStream( final String inputName ) {
    return runtimeContext.getInputStream( inputName );
  }

  protected int getOutputPreference() {
    return runtimeContext.getOutputPreference();
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
    return getInputStringValue( inputName, true );
  }

  protected String getInputStringValue( final String inputName, final boolean applyTemplates ) {
    // first check to see if we have an input parameter that we can use for
    // this.
    String value = null;
    if ( runtimeContext.getInputNames().contains( inputName ) ) {
      value = runtimeContext.getInputParameterStringValue( inputName );
    } else {
      value = getComponentSetting( inputName );
    }
    if ( value != null ) {
      if ( applyTemplates ) {
        // TODO make the format appliation configurable
        value = this.applyInputsToFormat( value );
      }
    }
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
    if ( componentDefinitionMap != null && componentDefinitionMap.containsKey( path ) ) {
      return componentDefinitionMap.get( path );
    } else {
      // now check the component node from the action definition.
      Node node = componentDefinition.selectSingleNode( path );
      if ( node == null ) {
        return null;
      }
      return node.getText();
    }
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

  /*
   * protected IRuntimeContext getRuntimeContextX() { return runtimeContext; }
   */
  public String getInitFailMessage() {
    // TODO: return a meaningful message here
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

    // Fix regression issue - BISERVER-3004 (MB) --- Start
    String xsl = null;
    // see if we have a custom XSL for the parameter page, if required
    if ( isDefinedInput( "xsl" ) ) { //$NON-NLS-1$
      xsl = getComponentSetting( "xsl" ); //$NON-NLS-1$
    }
    if ( xsl != null ) {
      runtimeContext.setParameterXsl( xsl );
    } else {
      // Fix for bug BISERVER-97 by Ezequiel Cuellar (and MB)
      // If the component-definition's action-definition does not have an xsl element it reuses the one already
      // set by its previous component-definition's action-definition peer.
      // If the xsl element is not present for the component-definition then reset to the default xsl value
      // specified in the Pentaho.xml tag "default-parameter-xsl"

      // Fix for bug BISERVER-238 by Ezequiel Cuellar (and MB)
      // Added a default value of DefaultParameterForm.xsl when getting the value of default-parameter-xsl
      runtimeContext.setParameterXsl( PentahoSystem.getSystemSetting(
          "default-parameter-xsl", "DefaultParameterForm.xsl" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    }
    // Fix regression issue - BISERVER-3004 (MB) --- End

    if ( loggingLevel == ILogger.UNKNOWN ) {
      warn( Messages.getInstance().getString( "Base.WARNING_LOGGING_LEVEL_UNKNOWN" ) ); //$NON-NLS-1$
      loggingLevel = ILogger.DEBUG;
    }
    int result = IRuntimeContext.RUNTIME_STATUS_FAILURE;

    if ( sessionContext == null ) {
      error( Messages.getInstance().getErrorString( "Base.ERROR_0001_INVALID_SESSION" ) ); //$NON-NLS-1$
      return result;
    }

    if ( ComponentBase.debug ) {
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
      if ( e instanceof RuntimeException ) {
        throw (RuntimeException) e;
      } else {
        // Since we want all exceptions including checked exceptions to propogate to the solution engine,
        // and we cannot change IComponent API on a minor release, we have to wrap all checked exceptions
        // in a RuntimeException.
        throw new RuntimeException( Messages.getInstance().getErrorString( "Base.ERROR_0002_EXECUTION_FAILED" ), e ); //$NON-NLS-1$
      }
    }
    return result;
  }

  @Override
  public String getObjectName() {
    return this.getClass().getName();
  }

  public String getId() {
    return id;
  }

  public String getActionTitle() {
    return runtimeContext.getActionTitle();
  }

  @Deprecated
  protected IContentItem getOutputContentItem( final String mimeType ) {
    return runtimeContext.getOutputContentItem( mimeType );
  }

  protected IContentItem getOutputContentItem( final String outputName, final String mimeType ) {
    return runtimeContext.getOutputContentItem( outputName, mimeType );
  }

  protected IContentItem getContentOutputItem( final String outputName, final String mimeType ) {
    return runtimeContext.getOutputContentItem( outputName, mimeType );
  }

  protected IContentItem getOutputItem( final String outputName, final String mimeType, final String extension ) {
    return runtimeContext.getOutputItem( outputName, mimeType, extension );
  }

  protected void setOutputValue( final String outputName, final Object value ) {
    runtimeContext.setOutputValue( outputName, value );
  }

  protected void addTempParameter( final String name, final IActionParameter param ) {
    runtimeContext.addTempParameter( name, param );
  }

  protected void addTempParameterObject( final String name, final Object paramObject ) {
    String pType = "object"; //$NON-NLS-1$
    IActionParameter actionParameter = new ActionParameter( name, pType, paramObject, null, null );
    addTempParameter( name, actionParameter );
  }

  /**
   * 
   * @deprecated
   * @return
   */
  @Deprecated
  protected OutputStream getDefaultOutputStream( final String mimeType ) {
    IContentItem contentItem = runtimeContext.getOutputContentItem( mimeType );
    if ( contentItem != null ) {
      try {
        return contentItem.getOutputStream( getActionName() );
      } catch ( Exception e ) {
        getLogger().error( e );
      }
    }
    return null;
  }

  protected String applyInputsToFormat( final String format ) {
    return runtimeContext.applyInputsToFormat( format, this );
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

  /**
   * 
   * @deprecated
   * @return
   */
  @Deprecated
  protected void setOutputMimeType( final String mimeType ) {
    IContentItem outputContentItem = runtimeContext.getOutputContentItem( mimeType );
    outputContentItem.setMimeType( mimeType );
  }

  protected void setOutputMimeType( final String outputName, final String mimeType ) {
    IContentItem outputContentItem = runtimeContext.getOutputContentItem( outputName );
    outputContentItem.setMimeType( mimeType );
  }

  protected OutputStream getFeedbackOutputStream() {
    IContentItem feedbackContentItem = runtimeContext.getFeedbackContentItem();
    if ( feedbackContentItem != null ) {
      try {
        return feedbackContentItem.getOutputStream( getActionName() );
      } catch ( Exception e ) {
        //ignored
      }
    }
    return null;
  }

  /**
   * @deprecated
   * @param actionParam
   */
  @Deprecated
  protected void createFeedbackParameter( final IActionParameter actionParam ) {
    runtimeContext.createFeedbackParameter( actionParam );
    runtimeContext.promptNeeded();
  }

  protected void createFeedbackParameter( final ISelectionMapper selMap, final String fieldName,
      final Object defaultValues ) {
    runtimeContext.createFeedbackParameter( selMap, fieldName, defaultValues );
    runtimeContext.promptNeeded();
  }

  protected void createFeedbackParameter( final ISelectionMapper selMap, final String fieldName,
      final Object defaultValues, boolean optional ) {
    runtimeContext.createFeedbackParameter( selMap, fieldName, defaultValues, optional );
    if ( !optional ) {
      runtimeContext.promptNeeded();
    }
  }

  protected void createFeedbackParameter( final String fieldName, final String displayName, final String hint,
      final String defaultValue, final boolean visible ) {
    runtimeContext.createFeedbackParameter( fieldName, displayName, hint, defaultValue, visible );
    runtimeContext.promptNeeded();
  }

  protected void createFeedbackParameter( final String fieldName, final String displayName, final String hint,
      final String defaultValue, final boolean visible, boolean optional ) {
    runtimeContext.createFeedbackParameter( fieldName, displayName, hint, defaultValue, visible, optional );
    if ( !optional ) {
      runtimeContext.promptNeeded();
    }
  }

  public void createFeedbackParameter( final String fieldName, final String displayName, final String hint,
      final Object defaultValues, final List values, final Map dispNames, final String displayStyle ) {
    runtimeContext.createFeedbackParameter( fieldName, displayName, hint, defaultValues, values, dispNames,
        displayStyle );
    runtimeContext.promptNeeded();
  }

  public void createFeedbackParameter( final String fieldName, final String displayName,
                                       final String hint,
      final Object defaultValues, final List values, final Map dispNames, final String displayStyle,
      boolean optional ) {
    runtimeContext.createFeedbackParameter( fieldName, displayName, hint, defaultValues, values, dispNames,
        displayStyle, optional );
    if ( !optional ) {
      runtimeContext.promptNeeded();
    }
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
