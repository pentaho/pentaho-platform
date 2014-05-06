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

/*
 * Created on Jun 17, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

package org.pentaho.platform.engine.services.runtime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.pentaho.actionsequence.dom.actions.ActionDefinition;
import org.pentaho.actionsequence.dom.actions.ActionFactory;
import org.pentaho.commons.connection.IPeekable;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.commons.connection.IPentahoStreamSource;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.api.engine.ActionExecutionException;
import org.pentaho.platform.api.engine.ActionInitializationException;
import org.pentaho.platform.api.engine.ActionSequenceException;
import org.pentaho.platform.api.engine.ActionSequencePromptException;
import org.pentaho.platform.api.engine.ActionValidationException;
import org.pentaho.platform.api.engine.IActionCompleteListener;
import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IActionSequence;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.engine.IComponent;
import org.pentaho.platform.api.engine.IConditionalExecution;
import org.pentaho.platform.api.engine.IContentOutputHandler;
import org.pentaho.platform.api.engine.ICreateFeedbackParameterCallback;
import org.pentaho.platform.api.engine.IExecutionListener;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterManager;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IParameterResolver;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISelectionMapper;
import org.pentaho.platform.api.engine.ISolutionActionDefinition;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.engine.InvalidParameterException;
import org.pentaho.platform.api.engine.PluginBeanException;
import org.pentaho.platform.api.engine.UnresolvedParameterException;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.api.repository.IRuntimeElement;
import org.pentaho.platform.api.repository.IRuntimeRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.util.XmlParseException;
import org.pentaho.platform.engine.core.audit.AuditHelper;
import org.pentaho.platform.engine.core.audit.MessageTypes;
import org.pentaho.platform.engine.core.output.MultiContentItem;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.PentahoMessenger;
import org.pentaho.platform.engine.services.SolutionURIResolver;
import org.pentaho.platform.engine.services.actionsequence.ActionParameter;
import org.pentaho.platform.engine.services.actionsequence.ActionParameterSource;
import org.pentaho.platform.engine.services.actionsequence.ActionSequenceParameterMgr;
import org.pentaho.platform.engine.services.actionsequence.ActionSequenceResourceWrapper;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.engine.services.solution.ActionDelegate;
import org.pentaho.platform.engine.services.solution.PojoComponent;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.xml.XForm;
import org.pentaho.platform.util.xml.XmlHelper;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

/**
 * @author James Dixon
 * 
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code
 *         Style - Code Templates
 */
@SuppressWarnings( "deprecation" )
public class RuntimeContext extends PentahoMessenger implements IRuntimeContext {

  /**
   * 
   */
  private static final long serialVersionUID = -1179016850860938879L;

  private IRuntimeElement runtimeData;

  private static final String LOG_NAME = "RUNTIME"; //$NON-NLS-1$

  private static final String PLUGIN_BUNDLE_NAME = "org.pentaho.platform.engine.services.runtime.plugins"; //$NON-NLS-1$

  protected static final String PARAMETER_FORM = "actionparam"; //$NON-NLS-1$

  private String logId;

  private IPentahoSession session;

  protected ISolutionEngine solutionEngine;

  protected StringBuffer xformHeader;

  protected StringBuffer xformBody;

  protected Map<String, String> xformFields;

  private static final String DEFAULT_PARAMETER_XSL = "DefaultParameterForm.xsl"; //$NON-NLS-1$

  protected String parameterXsl = RuntimeContext.DEFAULT_PARAMETER_XSL;

  protected String parameterTemplate = null;

  protected String parameterTarget;

  private String instanceId;

  private String processId;

  private String handle;

  protected IPentahoUrlFactory urlFactory;

  protected Map parameterProviders;

  protected static Map componentClassMap;

  protected IActionSequence actionSequence;

  public static final boolean debug = PentahoSystem.debug;

  private boolean audit = true;

  private int status;

  protected IOutputHandler outputHandler;

  protected IParameterManager paramManager;

  private String currentComponent;

  private int promptStatus = IRuntimeContext.PROMPT_NO;

  private int contentSequenceNumber; // = 0

  // Normally shouldn't need to synchronize. But, a bug in
  // pattern compilation results in the need to synchronize
  // a small block of code. If/when this problem is fixed, we
  // can remove this synchronization lock.
  private static final byte[] PATTERN_COMPILE_LOCK = new byte[0];

  private static final Log logger = LogFactory.getLog( RuntimeContext.class );

  private ICreateFeedbackParameterCallback createFeedbackParameterCallback;

  private IPluginManager pluginManager;

  static {
    RuntimeContext.getComponentClassMap();
  }

  @Override
  public Log getLogger() {
    return RuntimeContext.logger;
  }

  /*
   * public RuntimeContext( IApplicationContext applicationContext, String solutionName ) { this( null,
   * solutionName, applicationContext, null, null, null, null ); }
   */
  public RuntimeContext( final String instanceId, final ISolutionEngine solutionEngine, final String solutionName,
      final IRuntimeElement runtimeData, final IPentahoSession session, final IOutputHandler outputHandler,
      final String processId, final IPentahoUrlFactory urlFactory, final Map parameterProviders, final List messages,
      ICreateFeedbackParameterCallback createFeedbackParameterCallback ) {
    this.createFeedbackParameterCallback = createFeedbackParameterCallback;
    this.instanceId = instanceId;
    this.solutionEngine = solutionEngine;
    this.session = session;
    this.outputHandler = outputHandler;
    this.processId = processId;
    this.urlFactory = urlFactory;
    this.parameterProviders = parameterProviders;
    setMessages( messages );
    xformHeader = new StringBuffer();
    xformBody = new StringBuffer();
    xformFields = new HashMap<String, String>();
    // TODO - Throw invalid parameter error if these babies are null

    this.currentComponent = ""; //$NON-NLS-1$
    status = IRuntimeContext.RUNTIME_STATUS_NOT_STARTED;

    this.runtimeData = runtimeData;
    if ( runtimeData != null ) {
      this.instanceId = runtimeData.getInstanceId();
    }

    handle = "context-" + this.hashCode() + "-" + new Date().getTime(); //$NON-NLS-1$ //$NON-NLS-2$

    logId = ( ( instanceId != null ) ? instanceId : solutionName ) + ":" + RuntimeContext.LOG_NAME + ":" + handle + " "; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    paramManager = new ParameterManager();

    // Set the default XSL for parameter forms
    // Proposed fix for bug BISERVER-97 by Ezequiel Cuellar
    // If the component-definition's action-definition does not have an xsl element it reuses the one already
    // set by its previous component-definition's action-definition peer.
    // If the xsl element is not present for the component-definition then reset to the default xsl value
    // specified in the Pentaho.xml tag "default-parameter-xsl"

    // Proposed fix for bug BISERVER-238 by Ezequiel Cuellar
    // Added a default value of DefaultParameterForm.xsl when getting the value of default-parameter-xsl
    String defaultParameterXsl = PentahoSystem.getSystemSetting( "default-parameter-xsl", null ); //$NON-NLS-1$
    if ( ( defaultParameterXsl != null ) && ( defaultParameterXsl.length() > 0 ) ) {
      setParameterXsl( defaultParameterXsl );
    }

    // Gets the plugin manager if it's there.
    if ( PentahoSystem.getObjectFactory().objectDefined( IPluginManager.class.getSimpleName() ) ) {
      pluginManager = PentahoSystem.get( IPluginManager.class, session );
    }

  }

  private IRuntimeElement createChild( boolean persisted ) {
    IRuntimeElement childRuntimeData = null;
    IRuntimeRepository runtimeRepository = PentahoSystem.get( IRuntimeRepository.class, session );
    // the runtime repository is optional
    if ( runtimeRepository != null ) {
      runtimeRepository.setLoggingLevel( loggingLevel );
      childRuntimeData = runtimeRepository.newRuntimeElement( instanceId, "instance", !persisted ); //$NON-NLS-1$
      String childInstanceId = childRuntimeData.getInstanceId();
      // audit the creation of this against the parent instance
      AuditHelper.audit( instanceId, session.getName(), getActionName(), getObjectName(), processId,
          MessageTypes.INSTANCE_START, childInstanceId, "", 0, this ); //$NON-NLS-1$
    }
    return childRuntimeData;
  }

  public String createNewInstance( final boolean persisted ) {
    String childInstanceId = null;
    IRuntimeElement childRuntimeData = createChild( persisted );
    if ( childRuntimeData != null ) {
      childInstanceId = childRuntimeData.getInstanceId();
    }
    return childInstanceId;
  }

  public String createNewInstance( final boolean persisted, final Map parameters ) {
    return createNewInstance( persisted, parameters, false );
  }

  public String createNewInstance( final boolean persisted, final Map parameters, final boolean forceImmediateWrite ) {
    String childInstanceId = null;
    IRuntimeElement childRuntimeData = createChild( persisted );
    if ( childRuntimeData != null ) {

      if ( parameters != null ) {
        Iterator parameterIterator = parameters.keySet().iterator();
        while ( parameterIterator.hasNext() ) {
          String parameterName = (String) parameterIterator.next();
          Object parameterValue = parameters.get( parameterName );
          if ( parameterValue instanceof String ) {
            childRuntimeData.setStringProperty( parameterName, (String) parameterValue );
          } else if ( parameterValue instanceof BigDecimal ) {
            childRuntimeData.setBigDecimalProperty( parameterName, (BigDecimal) parameterValue );
          } else if ( parameterValue instanceof Date ) {
            childRuntimeData.setDateProperty( parameterName, (Date) parameterValue );
          } else if ( parameterValue instanceof List ) {
            childRuntimeData.setListProperty( parameterName, (List) parameterValue );
          } else if ( parameterValue instanceof Long ) {
            childRuntimeData.setLongProperty( parameterName, (Long) parameterValue );
          }
        }
      }
      childInstanceId = childRuntimeData.getInstanceId();
      if ( forceImmediateWrite ) {
        childRuntimeData.forceSave();
      }
    }
    return childInstanceId;
  }

  public int getStatus() {
    return status;
  }

  public void promptNow() {
    promptStatus = IRuntimeContext.PROMPT_NOW;
  }

  /** Sets the prompt flag but continue processing Actions */
  public void promptNeeded() {
    if ( promptStatus < IRuntimeContext.PROMPT_WAITING ) { // Don't mask a Prompt_Now
      promptStatus = IRuntimeContext.PROMPT_WAITING;
    }
  }

  /**
   * Tells if a component is waiting for a prompt
   * 
   * @return true if a prompt is pending
   */
  public boolean isPromptPending() {
    return ( promptStatus != IRuntimeContext.PROMPT_NO );
  }

  public IPentahoUrlFactory getUrlFactory() {
    return urlFactory;
  }

  public boolean feedbackAllowed() {
    return ( outputHandler != null ) && outputHandler.allowFeedback();
  }

  public IContentItem getFeedbackContentItem() {
    return outputHandler.getFeedbackContentItem();
  }

  @SuppressWarnings( "unused" )
  private int getContentSequenceNumber() {
    return contentSequenceNumber++;
  }

  public IContentItem getOutputItem( final String outputName, final String mimeType, final String extension ) {

    // TODO support content output versions in the action definition

    IActionParameter outputParameter = getOutputParameter( outputName );
    if ( outputParameter == null ) {
      warn( Messages.getInstance().getErrorString(
          "RuntimeContext.ERROR_0021_INVALID_OUTPUT_REQUEST", outputName, actionSequence.getSequenceName() ) ); //$NON-NLS-1$
      return null;
    }

    String filePath = "~/workspace/" + FilenameUtils.getBaseName( getSolutionPath() ) + extension; //$NON-NLS-1$
    String contentName = "contentrepo:" + filePath; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

    if ( !IActionParameter.TYPE_CONTENT.equals( outputParameter.getType() ) ) {
      warn( Messages.getInstance().getErrorString( "RuntimeContext.ERROR_0023_INVALID_OUTPUT_STREAM", outputName ) ); //$NON-NLS-1$
      return null;
    }

    try {
      IContentOutputHandler output = PentahoSystem.getOutputDestinationFromContentRef( contentName, session );
      if ( output != null ) {
        // TODO get this info
        output.setInstanceId( instanceId );
        output.setSolutionPath( filePath );
        output.setMimeType( mimeType );
        output.setSession( session );
        IContentItem contentItem = output.getFileOutputContentItem();
        setOutputValue( outputName, contentItem );
        return contentItem;
      }
    } catch ( Exception e ) {
      //ignored
    }
    return null;
  }

  public IContentItem getOutputContentItem( final String mimeType ) {
    // TODO check the sequence definition to see where this should come from
    return outputHandler.getOutputContentItem( IOutputHandler.RESPONSE, IOutputHandler.CONTENT, instanceId, mimeType );
  }

  public IContentItem getOutputContentItem( final String outputName, final String mimeType ) {

    IContentItem contentItem = null;
    IActionParameter parameter = (IActionParameter) actionSequence.getOutputDefinitions().get( outputName );
    if ( parameter == null ) {
      warn( Messages.getInstance().getErrorString(
          "RuntimeContext.ERROR_0021_INVALID_OUTPUT_REQUEST", outputName, actionSequence.getSequenceName() ) ); //$NON-NLS-1$
    } else {
      List destinationsList = parameter.getVariables();
      Iterator destinationsIterator = destinationsList.iterator();
      if ( destinationsList.size() > 1 ) {
        contentItem = new MultiContentItem();
      }
      while ( destinationsIterator.hasNext() ) {
        ActionParameterSource destination = (ActionParameterSource) destinationsIterator.next();

        String objectName = destination.getSourceName();
        String contentName = destination.getValue();
        contentName = TemplateUtil.applyTemplate( contentName, this );
        outputHandler.setSession( session );
        IContentItem tmpContentItem =
            outputHandler.getOutputContentItem( objectName, contentName, instanceId, mimeType );
        if ( contentItem instanceof MultiContentItem ) {
          ( (MultiContentItem) contentItem ).addContentItem( tmpContentItem );
        } else {
          contentItem = tmpContentItem;
          break;
        }
      }
    }

    return contentItem;
  }

  public String getHandle() {
    return handle;
  }

  public IPentahoSession getSession() {
    return session;
  }

  public String getSolutionPath() {
    return ( ( actionSequence != null ) ? actionSequence.getSolutionPath() : null );
  }

  public String getCurrentComponentName() {
    if ( "".equals( currentComponent ) ) { //$NON-NLS-1$
      return this.getClass().getName();
    }
    return currentComponent;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public int getErrorLevel() {
    return status;
  }

  public void setActionSequence( final IActionSequence sequence ) {
    this.actionSequence = sequence;
    paramManager = new ParameterManager( sequence );
  }

  public void validateSequence( final String sequenceName, final IExecutionListener execListener )
    throws ActionValidationException {
    paramManager.resetParameters();

    logId = instanceId + ":" + RuntimeContext.LOG_NAME + ":" + handle + ":" + sequenceName + " "; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    if ( audit ) {
      audit( MessageTypes.ACTION_SEQUENCE_START, MessageTypes.START, "", 0 ); //$NON-NLS-1$
    }

    if ( status != IRuntimeContext.RUNTIME_STATUS_NOT_STARTED ) {
      throw new IllegalStateException( Messages.getInstance().getErrorString(
          "RuntimeContext.ERROR_0001_RUNTIME_RUNNING" ) ); //$NON-NLS-1$
    }

    initFromActionSequenceDefinition();

    // validate component
    try {
      validateComponents( actionSequence, execListener );
      status = IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_OK;
    } catch ( ActionValidationException ex ) {
      status = IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_FAIL;
      throw ex;
    }
  }

  private void validateComponents( final IActionSequence sequence, final IExecutionListener execListener )
    throws ActionValidationException {
    List defList = sequence.getActionDefinitionsAndSequences();

    Object listItem;
    for ( Iterator it = defList.iterator(); it.hasNext(); ) {
      listItem = it.next();

      if ( listItem instanceof IActionSequence ) {
        validateComponents( (IActionSequence) listItem, execListener );
      } else if ( listItem instanceof ISolutionActionDefinition ) {

        ISolutionActionDefinition actionDef = (ISolutionActionDefinition) listItem;
        if ( RuntimeContext.debug ) {
          debug( Messages.getInstance().getString(
              "RuntimeContext.DEBUG_VALIDATING_COMPONENT", actionDef.getComponentName() ) ); //$NON-NLS-1$
        }

        IComponent component = null;
        try {
          component = resolveComponent( actionDef, instanceId, processId, session );
          component.setLoggingLevel( loggingLevel );

          // allow the ActionDefinition to cache the component
          actionDef.setComponent( component );
          paramManager.setCurrentParameters( actionDef );
          /*
           * We need to catch checked and unchecked exceptions here so we can create an ActionSequeceException with
           * contextual information, including the root cause. Allowing unchecked exceptions to pass through would
           * prevent valuable feedback in the log or response.
           */
        } catch ( Throwable ex ) {
          ActionDefinition actionDefinition = new ActionDefinition( (Element) actionDef.getNode(), null );
          throw new ActionValidationException( Messages.getInstance().getErrorString(
              "RuntimeContext.ERROR_0009_COULD_NOT_CREATE_COMPONENT", actionDef.getComponentName().trim() ), ex, //$NON-NLS-1$
              session.getName(), instanceId, getActionSequence().getSequenceName(), actionDefinition.getDescription(),
              actionDefinition.getComponentName() );
        }

        int validateResult = IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_OK;
        try {
          validateResult = component.validate();
          /*
           * We need to catch checked and unchecked exceptions here so we can create an ActionSequeceException with
           * contextual information, including the root cause. Allowing unchecked exceptions to pass through would
           * prevent valuable feedback in the log or response.
           */
        } catch ( Throwable t ) {
          throw new ActionValidationException( Messages.getInstance().getErrorString(
              "RuntimeContext.ERROR_0035_ACTION_VALIDATION_FAILED" ), t, //$NON-NLS-1$
              session.getName(), instanceId, getActionSequence().getSequenceName(), component.getActionDefinition() );
        }

        if ( validateResult != IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_OK ) {
          throw new ActionValidationException( Messages.getInstance().getErrorString(
              "RuntimeContext.ERROR_0035_ACTION_VALIDATION_FAILED" ), //$NON-NLS-1$
              session.getName(), instanceId, getActionSequence().getSequenceName(), component.getActionDefinition() );
        }

        paramManager.addOutputParameters( actionDef );
        setCurrentComponent( "" ); //$NON-NLS-1$
        setCurrentActionDef( null );
      }
    }
    if ( execListener != null ) {
      execListener.validated( this );
    }
  }

  public IPentahoStreamSource getDataSource( final String parameterName ) {
    IPentahoStreamSource dataSource = null;

    // TODO Temp workaround for content repos bug
    IActionParameter actionParameter = paramManager.getCurrentInput( parameterName );
    if ( actionParameter == null ) {
      throw new InvalidParameterException( Messages.getInstance().getErrorString(
          "RuntimeContext.ERROR_0019_INVALID_INPUT_REQUEST", parameterName, actionSequence.getSequenceName() ) ); //$NON-NLS-1$
    }

    Object locObj = actionParameter.getValue();
    if ( locObj != null ) {
      if ( locObj instanceof IContentItem ) { // At this point we have an IContentItem so why do anything else?
        dataSource = ( (IContentItem) locObj ).getDataSource();
      }
    }
    // This will return null if the locObj is null
    return dataSource;
  }

  @SuppressWarnings( { "unchecked" } )
  protected static Map getComponentClassMap() {
    if ( RuntimeContext.componentClassMap == null ) {
      RuntimeContext.componentClassMap = Collections.synchronizedMap( RuntimeContext.createComponentClassMap() );
    }
    return RuntimeContext.componentClassMap;
  }

  private static Map createComponentClassMap() {
    Properties knownComponents = new Properties();
    // First, get known plugin names...
    try {
      ResourceBundle pluginBundle = ResourceBundle.getBundle( RuntimeContext.PLUGIN_BUNDLE_NAME );
      if ( pluginBundle != null ) { // Copy the bundle here...
        Enumeration keyEnum = pluginBundle.getKeys();
        String bundleKey = null;
        while ( keyEnum.hasMoreElements() ) {
          bundleKey = (String) keyEnum.nextElement();
          knownComponents.put( bundleKey, pluginBundle.getString( bundleKey ) );
        }
      }
    } catch ( Exception ex ) {
      RuntimeContext.logger
          .warn( Messages.getInstance().getString( "RuntimeContext.WARN_NO_PLUGIN_PROPERTIES_BUNDLE" ) ); //$NON-NLS-1$
    }
    // Get overrides...
    //
    // Note - If the override wants to remove an existing "known" plugin,
    // simply adding an empty value will cause the "known" plugin to be removed.
    //
    InputStream is = null;
    try {
      File f = new File( PentahoSystem.getApplicationContext().getSolutionPath( "system/plugin.properties" ) );
      if ( f.exists() ) {
        is = new FileInputStream( f );
        Properties overrideComponents = new Properties();
        overrideComponents.load( is );
        knownComponents.putAll( overrideComponents ); // load over the top of the known properties
      }
    } catch ( FileNotFoundException ignored ) {
      RuntimeContext.logger.warn( Messages.getInstance().getString( "RuntimeContext.WARN_NO_PLUGIN_PROPERTIES" ) ); //$NON-NLS-1$
    } catch ( IOException ignored ) {
      RuntimeContext.logger.warn(
          Messages.getInstance().getString( "RuntimeContext.WARN_BAD_PLUGIN_PROPERTIES" ), ignored ); //$NON-NLS-1$
    } finally {
      try {
        if ( is != null ) {
          is.close();
        }
      } catch ( IOException e ) {
        //ignored
      }
    }
    return knownComponents;
  }

  protected void setCurrentComponent( final String componentClassName ) {
    currentComponent = componentClassName;
  }

  protected void setCurrentActionDef( final ISolutionActionDefinition actionDefinition ) {
  }

  protected static String getComponentClassName( final String rawClassName, final IRuntimeContext runtime ) {
    String mappedClassName = (String) RuntimeContext.getComponentClassMap().get( rawClassName );
    if ( mappedClassName != null ) {
      if ( mappedClassName.charAt( 0 ) == '!' ) {
        // this is deprecated, log a warning
        mappedClassName = mappedClassName.substring( 1 );
        runtime.warn( Messages.getInstance().getString(
            "RuntimeContext.WARN_DEPRECATED_COMPONENT_CLASS", rawClassName, mappedClassName ) ); //$NON-NLS-1$
        runtime.audit( MessageTypes.DEPRECATION_WARNING, rawClassName, mappedClassName, 0 );
      }
      return mappedClassName;
    }
    return rawClassName;

  }

  protected IComponent resolveComponent( final ISolutionActionDefinition actionDefinition,
      final String currentInstanceId, final String currentProcessId, final IPentahoSession currentSession )
    throws ClassNotFoundException, PluginBeanException, InstantiationException, IllegalAccessException {

    // try to create an instance of the component class specified in the
    // action document

    String componentAlias = actionDefinition.getComponentName().trim();

    String componentClassName = RuntimeContext.getComponentClassName( componentAlias, this );

    Element componentDefinition = (Element) actionDefinition.getComponentSection();
    setCurrentComponent( componentClassName );
    setCurrentActionDef( actionDefinition );

    IComponent component = null;
    Class componentClass = null;
    Object componentTmp = null;

    // Explicitly using the short name instead of the fully layed out class name
    if ( ( pluginManager != null ) && ( pluginManager.isBeanRegistered( componentAlias ) ) ) {
      if ( RuntimeContext.debug ) {
        this.debug( "Component alias " + componentAlias + " will be resolved by the plugin manager." ); //$NON-NLS-1$ //$NON-NLS-2$
      }
      componentTmp = pluginManager.getBean( componentAlias );
      if ( RuntimeContext.debug ) {
        this.debug( "Component found in a plugin, class is: " + componentTmp.getClass().getName() ); //$NON-NLS-1$
      }
    }

    if ( RuntimeContext.debug ) {
      this.debug( "Component alias " + componentAlias + " will be resolved by the platform" ); //$NON-NLS-1$ //$NON-NLS-2$
    }
    // Ok - the plugin didn't load - try the old route
    if ( componentTmp == null ) {
      componentClass = Class.forName( componentClassName );
      componentTmp = componentClass.newInstance();
    }
    if ( componentTmp instanceof IComponent ) {
      component = (IComponent) componentTmp;
    } else if ( componentTmp instanceof IAction ) {
      component = new ActionDelegate( componentTmp );
    } else {
      // Try this out...
      PojoComponent pc = new PojoComponent();
      pc.setPojo( componentTmp );
      component = pc;
    }

    component.setInstanceId( currentInstanceId );
    component.setActionName( getActionName() );
    component.setProcessId( currentProcessId );

    // This next conditional is used to allow components to use the new action sequence dom commons project. The
    // ActionFactory should return an object that wraps the action definition element to be processed by the component.
    // The component can then use the wrappers API to access the action definition rather than make explicit references
    // to the dom nodes.
    if ( component instanceof IParameterResolver ) {
      component.setActionDefinition( ActionFactory.getActionDefinition( (Element) actionDefinition.getNode(),
          new ActionSequenceParameterMgr( this, currentSession, (IParameterResolver) component ) ) );
    } else {
      component.setActionDefinition( ActionFactory.getActionDefinition( (Element) actionDefinition.getNode(),
          new ActionSequenceParameterMgr( this, currentSession ) ) );
    }

    // create a map of the top level component definition nodes and their text
    Map<String, String> componentDefinitionMap = new HashMap<String, String>();
    List elements = componentDefinition.elements();
    Element element;
    String name;
    String value;
    String customXsl = null;
    for ( int idx = 0; idx < elements.size(); idx++ ) {
      element = (Element) elements.get( idx );
      name = element.getName();
      value = element.getText();
      // see if we have a target window for the output
      if ( "target".equals( name ) ) { //$NON-NLS-1$
        setParameterTarget( value );
      } else if ( "xsl".equals( name ) ) { //$NON-NLS-1$
        customXsl = value; // setParameterXsl(value);
      }

      componentDefinitionMap.put( element.getName(), element.getText() );
    }

    if ( customXsl != null ) {
      setParameterXsl( customXsl );
    }

    component.setComponentDefinitionMap( componentDefinitionMap );
    component.setComponentDefinition( componentDefinition );
    component.setRuntimeContext( this );
    component.setSession( currentSession );
    component.setLoggingLevel( getLoggingLevel() );
    component.setMessages( getMessages() );
    return component;
  }

  public void executeSequence( final IActionCompleteListener doneListener, final IExecutionListener execListener,
      final boolean async ) throws ActionSequenceException {
    paramManager.resetParameters();
    long start = new Date().getTime();

    status = IRuntimeContext.RUNTIME_STATUS_RUNNING;

    // create an IActionDef object
    List actionDefinitions = actionSequence.getActionDefinitionsAndSequences();
    if ( actionDefinitions == null ) {
      audit( MessageTypes.ACTION_SEQUENCE_FAILED, MessageTypes.VALIDATION, Messages.getInstance().getErrorString(
          "RuntimeContext.ERROR_0011_NO_VALID_ACTIONS" ), 0 ); //$NON-NLS-1$
      status = IRuntimeContext.RUNTIME_STATUS_FAILURE;
      throw new ActionValidationException( Messages.getInstance().getErrorString(
          "RuntimeContext.ERROR_0011_NO_VALID_ACTIONS" ), //$NON-NLS-1$
          session.getName(), instanceId, getActionSequence().getSequenceName(), null );
    }

    setLoggingLevel( loggingLevel );

    if ( RuntimeContext.debug ) {
      debug( Messages.getInstance().getString( "RuntimeContext.DEBUG_EXECUTING_ACTIONS" ) ); //$NON-NLS-1$
    }

    paramManager.setCurrentParameters( null );
    try {
      resolveParameters();
      if ( execListener != null ) {
        execListener.loaded( this );
      }
      executeSequence( actionSequence, doneListener, execListener, async );

      if ( this.feedbackAllowed()
          && ( ( promptStatus != IRuntimeContext.PROMPT_NO ) || ( xformBody.length() > 0 )
        || ( parameterTemplate != null ) ) ) {
        sendFeedbackForm();
      }

      paramManager.setCurrentParameters( null );

      if ( audit ) {
        audit( MessageTypes.ACTION_SEQUENCE_END, MessageTypes.END, "", (int) ( new Date().getTime() - start ) ); //$NON-NLS-1$
      }

      if ( !isPromptPending() ) {
        Map returnParamMap = paramManager.getReturnParameters();

        for ( Iterator it = returnParamMap.entrySet().iterator(); it.hasNext(); ) {
          Map.Entry mapEntry = (Map.Entry) it.next();

          String paramName = (String) mapEntry.getKey();
          ParameterManager.ReturnParameter returnParam = (ParameterManager.ReturnParameter) mapEntry.getValue();

          if ( returnParam == null ) {
            error( Messages.getInstance().getErrorString( "RuntimeContext.ERROR_0029_SAVE_PARAM_NOT_FOUND", paramName ) ); //$NON-NLS-1$
          } else {
            if ( IParameterProvider.SCOPE_SESSION.equals( returnParam.destinationName ) ) {
              session.setAttribute( returnParam.destinationParameter, returnParam.value );
              if ( RuntimeContext.debug ) {
                debug( paramName + " - session - " + returnParam.destinationParameter ); //$NON-NLS-1$
              }
            } else if ( "response".equals( returnParam.destinationName ) ) { //$NON-NLS-1$
              if ( outputHandler != null ) {
                outputHandler.setOutput( returnParam.destinationParameter, returnParam.value );
              } else {
                info( Messages.getInstance().getString( "RuntimeContext.INFO_NO_OUTPUT_HANDLER" ) ); //$NON-NLS-1$
              }
              if ( RuntimeContext.debug ) {
                debug( paramName + " - response - " + returnParam.destinationParameter ); //$NON-NLS-1$
              }
            } else if ( PentahoSystem.SCOPE_GLOBAL.equals( returnParam.destinationName ) ) {
              PentahoSystem.putInGlobalAttributesMap( returnParam.destinationParameter, returnParam.value );
              if ( RuntimeContext.debug ) {
                debug( paramName + " - global - " + returnParam.destinationParameter ); //$NON-NLS-1$
              }
            } else { // Unrecognized scope
              warn( Messages
                  .getInstance()
                  .getString(
                      "RuntimeContext.WARN_UNRECOGNIZED_SCOPE", returnParam.destinationName, returnParam.destinationParameter ) ); //$NON-NLS-1$
            }
          }
        }
      }
    } catch ( UnresolvedParameterException ex ) {
      status = IRuntimeContext.RUNTIME_STATUS_FAILURE;
      audit( MessageTypes.ACTION_SEQUENCE_FAILED, MessageTypes.VALIDATION, Messages.getInstance().getErrorString(
          "RuntimeContext.ERROR_0013_BAD_PARAMETERS" ), 0 ); //$NON-NLS-1$
      throw ex;
    } catch ( ActionSequenceException ex ) {
      status = IRuntimeContext.RUNTIME_STATUS_FAILURE;
      audit( MessageTypes.ACTION_SEQUENCE_FAILED, MessageTypes.EXECUTION, "", (int) ( new Date().getTime() - start ) ); //$NON-NLS-1$
      throw ex;
    } catch ( IOException ex ) {
      status = IRuntimeContext.RUNTIME_STATUS_FAILURE;
      audit( MessageTypes.ACTION_SEQUENCE_FAILED, MessageTypes.EXECUTION, "", (int) ( new Date().getTime() - start ) ); //$NON-NLS-1$
      throw new ActionSequenceException( ex );
    }
  }

  public void setPromptStatus( final int status ) {
    promptStatus = status;
  }

  @SuppressWarnings( { "unchecked" } )
  public void executeSequence( final IActionSequence sequence, final IActionCompleteListener doneListener,
      final IExecutionListener execListener, final boolean async ) throws ActionSequenceException {
    String loopParamName = sequence.getLoopParameter();

    boolean peekOnly = sequence.getLoopUsingPeek();
    Object loopList;
    IActionParameter loopParm = null;

    if ( loopParamName == null ) {
      loopList = new ArrayList<Integer>();
      ( (ArrayList) loopList ).add( new Integer( 0 ) );
    } else {
      loopParm = getLoopParameter( loopParamName );
      loopList = loopParm.getValue();

      // If the loop list is an array, convert it to an array list for processing
      if ( loopList instanceof Object[] ) {
        loopList = Arrays.asList( (Object[]) loopList );
      }

    }

    if ( loopList instanceof List ) {
      executeLoop( loopParm, (List) loopList, sequence, doneListener, execListener, async );
      if ( loopParm != null ) {
        addInputParameter( loopParm.getName(), loopParm ); // replace the loop param in case the last loop muggled
                                                           // it
      }
    } else if ( loopList instanceof IPentahoResultSet ) {
      executeLoop( loopParm, (IPentahoResultSet) loopList, sequence, doneListener, execListener, async, peekOnly );
    }
  }

  private void executeLoop( final IActionParameter loopParm, final IPentahoResultSet loopSet,
      final IActionSequence sequence, final IActionCompleteListener doneListener,
      final IExecutionListener execListener, final boolean async, boolean peekOnly ) throws ActionSequenceException {

    // execute the actions
    int loopCount = -1;

    // TODO handle results sets directly instead of using Properties maps

    // Only if the result set is scrollable that is we CAN go back to the first record should we reset
    // to the first record. This is to resolve multiple levels of looping on resultset.
    if ( loopSet.isScrollable() ) {
      loopSet.beforeFirst();
    }
    if ( peekOnly && !( loopSet instanceof IPeekable ) ) {
      throw new ActionExecutionException( Messages.getInstance().getErrorString(
          "RuntimeContext.ERROR_0033_NOT_PEEKABLE" ), //$NON-NLS-1$
          session.getName(), instanceId, getActionSequence().getSequenceName(), null );
    }
    Object[] row = peekOnly ? ( (IPeekable) loopSet ).peek() : loopSet.next();
    Object[][] headerSet = loopSet.getMetaData().getColumnHeaders();
    // TODO handle OLAP result sets
    Object[] headers = headerSet[0];
    while ( row != null ) {
      loopCount++;
      if ( RuntimeContext.debug ) {
        debug( Messages.getInstance()
            .getString( "RuntimeContext.DEBUG_EXECUTING_ACTION", Integer.toString( loopCount ) ) ); //$NON-NLS-1$
      }

      if ( execListener != null ) {
        execListener.loop( this, loopCount );
      }
      if ( loopParm != null ) {
        IActionParameter ap;
        for ( int columnNo = 0; columnNo < headers.length; columnNo++ ) {
          String name = headers[columnNo].toString();
          Object value = row[columnNo];
          String type = null;
          if ( value instanceof String ) {
            type = IActionParameter.TYPE_STRING;
          } else if ( value instanceof Date ) {
            type = IActionParameter.TYPE_DATE;
          } else if ( ( value instanceof Long ) || ( value instanceof Integer ) ) {
            type = IActionParameter.TYPE_INTEGER;
          } else if ( ( value instanceof BigDecimal ) || ( value instanceof Double ) || ( value instanceof Float ) ) {
            type = IActionParameter.TYPE_DECIMAL;
          } else if ( value instanceof String[] ) {
            type = IActionParameter.TYPE_STRING;
          } else if ( value == null ) {
            warn( Messages.getInstance().getString( "RuntimeContext.WARN_VARIABLE_IN_LOOP_IS_NULL", name ) ); //$NON-NLS-1$
          } else {
            type = IActionParameter.TYPE_OBJECT;
            warn( Messages.getInstance().getString(
                "RuntimeContext.WARN_VARIABLE_IN_LOOP_NOT_RECOGNIZED", name, value.getClass().toString() ) ); //$NON-NLS-1$
          }
          // TODO make sure any previous loop values are removed
          ap = paramManager.getInput( name );
          if ( ap == null ) {
            ap = new ActionParameter( name, type, value, null, null );
            addInputParameter( name, ap );
          } else {
            ap.dispose();
            ap.setValue( value );
          }
        }
      }
      try {
        performActions( sequence, doneListener, execListener, async );
      } catch ( ActionSequenceException e ) {
        e.setLoopIndex( loopCount );
        throw e;
      }
      row = peekOnly ? ( (IPeekable) loopSet ).peek() : loopSet.next();
    }

    status = IRuntimeContext.RUNTIME_STATUS_SUCCESS;
  }

  private void executeLoop( final IActionParameter loopParm, final List loopList, final IActionSequence sequence,
      final IActionCompleteListener doneListener, final IExecutionListener execListener, final boolean async )
    throws ActionSequenceException {

    // execute the actions
    int loopCount = -1;
    for ( Iterator it = loopList.iterator(); it.hasNext(); ) {
      loopCount++;
      if ( RuntimeContext.debug ) {
        debug( Messages.getInstance()
            .getString( "RuntimeContext.DEBUG_EXECUTING_ACTION", Integer.toString( loopCount ) ) ); //$NON-NLS-1$
      }

      if ( execListener != null ) {
        execListener.loop( this, loopCount );
      }
      Object loopVar = it.next();
      if ( loopParm != null ) {
        IActionParameter ap;
        if ( loopVar instanceof Map ) {
          ap = new ActionParameter( loopParm.getName(), "property-map", loopVar, null, null ); //$NON-NLS-1$
        } else {
          ap = new ActionParameter( loopParm.getName(), "string", loopVar, null, null ); //$NON-NLS-1$
        }

        addInputParameter( loopParm.getName(), ap );
      }

      try {
        performActions( sequence, doneListener, execListener, async );
      } catch ( ActionSequenceException e ) {
        e.setLoopIndex( loopCount );
        throw e;
      }
      if ( promptStatus == IRuntimeContext.PROMPT_NOW ) {
        return;
      }
    }
    status = IRuntimeContext.RUNTIME_STATUS_SUCCESS;
  }

  private void performActions( final IActionSequence sequence, final IActionCompleteListener doneListener,
      final IExecutionListener execListener, final boolean async ) throws ActionSequenceException {
    IConditionalExecution conditional = sequence.getConditionalExecution();
    if ( conditional != null ) {
      try {
        if ( !conditional.shouldExecute( paramManager.getAllParameters(), RuntimeContext.logger ) ) {
          //audit(MessageTypes.ACTION_SEQUENCE_EXECUTE_CONDITIONAL, MessageTypes.NOT_EXECUTED, "", 0); //$NON-NLS-1$ //$NON-NLS-2$
          if ( RuntimeContext.debug ) {
            this.debug( Messages.getInstance().getString( "RuntimeContext.INFO_ACTION_NOT_EXECUTED" ) ); //$NON-NLS-1$
          }
          status = IRuntimeContext.RUNTIME_STATUS_SUCCESS;
          return;
        }
      } catch ( Exception ex ) {
        currentComponent = ""; //$NON-NLS-1$
        status = IRuntimeContext.RUNTIME_STATUS_FAILURE;
        throw new ActionExecutionException( Messages.getInstance().getErrorString(
            "RuntimeContext.ERROR_0032_CONDITIONAL_EXECUTION_FAILED" ), ex, //$NON-NLS-1$
            session.getName(), instanceId, getActionSequence().getSequenceName(), null );

      }
    }

    List defList = sequence.getActionDefinitionsAndSequences();

    Object listItem;

    for ( Iterator actIt = defList.iterator(); actIt.hasNext(); ) {
      listItem = actIt.next();

      if ( listItem instanceof IActionSequence ) {
        executeSequence( (IActionSequence) listItem, doneListener, execListener, async );
      } else if ( listItem instanceof ISolutionActionDefinition ) {
        ISolutionActionDefinition actionDef = (ISolutionActionDefinition) listItem;
        currentComponent = actionDef.getComponentName();
        paramManager.setCurrentParameters( actionDef );

        try {
          executeAction( actionDef, parameterProviders, doneListener, execListener, async );
          paramManager.addOutputParameters( actionDef );
        } catch ( ActionSequenceException ex ) {
          currentComponent = ""; //$NON-NLS-1$
          status = IRuntimeContext.RUNTIME_STATUS_FAILURE;
          throw ex;
        }
      }
      if ( promptStatus == IRuntimeContext.PROMPT_NOW ) {
        break;
      }
      currentComponent = ""; //$NON-NLS-1$
    }
    status = IRuntimeContext.RUNTIME_STATUS_SUCCESS;
  }

  private void executeAction( final ISolutionActionDefinition actionDefinition, final Map pParameterProviders,
      final IActionCompleteListener doneListener, final IExecutionListener execListener, final boolean async )
    throws ActionInitializationException, ActionExecutionException, UnresolvedParameterException {

    this.parameterProviders = pParameterProviders;
    // TODO get audit setting from action definition

    long start = new Date().getTime();
    if ( audit ) {
      audit( MessageTypes.COMPONENT_EXECUTE_START, MessageTypes.START, "", 0 ); //$NON-NLS-1$
    }

    try {
      // resolve the parameters
      resolveParameters();
    } catch ( UnresolvedParameterException ex ) {
      audit( MessageTypes.COMPONENT_EXECUTE_FAILED, MessageTypes.VALIDATION, Messages.getInstance().getErrorString(
          "RuntimeContext.ERROR_0013_BAD_PARAMETERS" ), 0 ); //$NON-NLS-1$
      if ( doneListener != null ) {
        doneListener.actionComplete( this );
      }
      status = IRuntimeContext.RUNTIME_STATUS_FAILURE;
      ex.setActionClass( actionDefinition.getComponentName() );
      ex.setStepDescription( actionDefinition.getDescription() );
      throw ex;
    }
    status = IRuntimeContext.RUNTIME_CONTEXT_RESOLVE_OK;

    if ( RuntimeContext.debug ) {
      debug( Messages.getInstance().getString( "RuntimeContext.DEBUG_PRE-EXECUTE_AUDIT" ) ); //$NON-NLS-1$
    }
    List auditPre = actionDefinition.getPreExecuteAuditList();
    audit( auditPre );

    // initialize the component
    IComponent component = actionDefinition.getComponent();

    if ( RuntimeContext.debug ) {
      debug( Messages.getInstance().getString(
          "RuntimeContext.DEBUG_SETTING_LOGGING", Logger.getLogLevelName( loggingLevel ) ) ); //$NON-NLS-1$
    }
    component.setLoggingLevel( loggingLevel );
    if ( RuntimeContext.debug ) {
      debug( Messages.getInstance().getString( "RuntimeContext.DEBUG_INITIALIZING_COMPONENT" ) ); //$NON-NLS-1$
    }
    boolean initResult = false;
    try {
      initResult = component.init();
      /*
       * We need to catch checked and unchecked exceptions here so we can create an ActionSequeceException with
       * contextual information, including the root cause. Allowing unchecked exceptions to pass through would
       * prevent valuable feedback in the log or response.
       */
    } catch ( Throwable t ) {
      throw new ActionInitializationException( Messages.getInstance().getErrorString(
          "RuntimeContext.ERROR_0016_COMPONENT_INITIALIZE_FAILED" ), t, //$NON-NLS-1$
          session.getName(), instanceId, getActionSequence().getSequenceName(), component.getActionDefinition() );
    }

    if ( !initResult ) {
      status = IRuntimeContext.RUNTIME_STATUS_INITIALIZE_FAIL;
      audit( MessageTypes.COMPONENT_EXECUTE_FAILED, MessageTypes.VALIDATION, Messages.getInstance().getErrorString(
          "RuntimeContext.ERROR_0016_COMPONENT_INITIALIZE_FAILED" ), 0 ); //$NON-NLS-1$
      if ( doneListener != null ) {
        doneListener.actionComplete( this );
      }
      throw new ActionInitializationException( Messages.getInstance().getErrorString(
          "RuntimeContext.ERROR_0016_COMPONENT_INITIALIZE_FAILED" ), //$NON-NLS-1$
          session.getName(), instanceId, getActionSequence().getSequenceName(), component.getActionDefinition() );
    }

    try {
      executeComponent( actionDefinition );
    } catch ( ActionExecutionException ex ) {
      if ( doneListener != null ) {
        doneListener.actionComplete( this );
      }
      throw ex;
    }

    if ( RuntimeContext.debug ) {
      debug( Messages.getInstance().getString( "RuntimeContext.DEBUG_POST-EXECUTE_AUDIT" ) ); //$NON-NLS-1$
    }
    List auditPost = actionDefinition.getPostExecuteAuditList();
    audit( auditPost );
    if ( audit ) {
      long end = new Date().getTime();
      audit( MessageTypes.COMPONENT_EXECUTE_END, MessageTypes.END, "", (int) ( end - start ) ); //$NON-NLS-1$
    }

    if ( doneListener != null ) {
      doneListener.actionComplete( this );
    }
    if ( execListener != null ) {
      execListener.action( this, actionDefinition );
    }
  }

  protected void executeComponent( final ISolutionActionDefinition actionDefinition ) throws ActionExecutionException {
    if ( RuntimeContext.debug ) {
      debug( Messages.getInstance().getString( "RuntimeContext.DEBUG_STARTING_COMPONENT_EXECUTE" ) ); //$NON-NLS-1$
    }
    try {
      if ( getOutputPreference() == IOutputHandler.OUTPUT_TYPE_PARAMETERS
          && actionDefinition.getComponentName().contains( "SecureFilterComponent" ) ) {
        status = actionDefinition.getComponent().execute();
      } else if ( getOutputPreference() != IOutputHandler.OUTPUT_TYPE_PARAMETERS ) {
        status = actionDefinition.getComponent().execute();
      } else {
        status = IRuntimeContext.RUNTIME_STATUS_SUCCESS;
      }
      actionDefinition.getComponent().done();
      if ( RuntimeContext.debug ) {
        debug( Messages.getInstance().getString( "RuntimeContext.DEBUG_FINISHED_COMPONENT_EXECUTE" ) ); //$NON-NLS-1$
      }
      /*
       * We need to catch checked and unchecked exceptions here so we can create an ActionSequeceException with
       * contextual information, including the root cause. Allowing unchecked exceptions to pass through would
       * prevent valuable feedback in the log or response. Once the IComponent API changes to throw
       * ActionSequenceException from execute(), we may want to handle those specially here by allowing them to
       * pass through without a wrapping exception.
       */
    } catch ( Throwable e ) {
      status = IRuntimeContext.RUNTIME_STATUS_FAILURE;
      audit( MessageTypes.COMPONENT_EXECUTE_FAILED, MessageTypes.FAILED, e.getLocalizedMessage(), 0 );
      throw new ActionExecutionException( Messages.getInstance().getErrorString(
          "RuntimeContext.ERROR_0017_COMPONENT_EXECUTE_FAILED" ), e, //$NON-NLS-1$
          session.getName(), instanceId, getActionSequence().getSequenceName(), actionDefinition.getComponent()
              .getActionDefinition() );
    }

    if ( status != IRuntimeContext.RUNTIME_STATUS_SUCCESS ) {
      throw new ActionExecutionException( Messages.getInstance().getErrorString(
          "RuntimeContext.ERROR_0017_COMPONENT_EXECUTE_FAILED" ), //$NON-NLS-1$
          session.getName(), instanceId, getActionSequence().getSequenceName(), actionDefinition.getComponent()
              .getActionDefinition() );
    }
  }

  private void initFromActionSequenceDefinition() {

    // TODO get audit setting from action sequence

    int actionLogLevel = actionSequence.getLoggingLevel();
    int instanceLogLevel = runtimeData.getLoggingLevel();
    int actionSequenceLoggingLevel =
        ( instanceLogLevel != ILogger.UNKNOWN ) ? instanceLogLevel : ( ( actionLogLevel != ILogger.UNKNOWN )
            ? actionLogLevel : solutionEngine.getLoggingLevel() );

    setLoggingLevel( actionSequenceLoggingLevel );
  }

  private void resolveParameters() throws UnresolvedParameterException {

    Set inputNames = getInputNames();
    Iterator inputNamesIterator = inputNames.iterator();
    IActionParameter actionParameter;
    List variables;
    Iterator variablesIterator;
    ActionParameterSource variable;
    String sourceName;
    String sourceValue;
    Object variableValue = null;
    IParameterProvider parameterProvider;
    while ( inputNamesIterator.hasNext() ) {
      variableValue = null;

      String inputName = (String) inputNamesIterator.next();
      actionParameter = paramManager.getCurrentInput( inputName );
      if ( actionParameter == null ) {
        throw new UnresolvedParameterException( Messages.getInstance().getErrorString(
            "RuntimeContext.ERROR_0031_INPUT_NOT_FOUND", inputName ), //$NON-NLS-1$
            session.getName(), instanceId, getActionSequence().getSequenceName(), null );
      }

      variables = actionParameter.getVariables();
      variablesIterator = variables.iterator();
      while ( variablesIterator.hasNext() ) {
        variable = (ActionParameterSource) variablesIterator.next();
        sourceName = variable.getSourceName();
        sourceValue = variable.getValue();
        variableValue = null;
        // TODO support accessing the ancestors of the current instance,
        // e.g. runtme.parent
        if ( "runtime".equals( sourceName ) ) { //$NON-NLS-1$
          // first check the standard variables
          variableValue = getStringParameter( sourceValue, null );
          if ( variableValue == null ) {
            // now check the runtime data
            variableValue = runtimeData.getStringProperty( sourceValue, null );
          }
          if ( variableValue != null ) {
            break;
          }
        } else {
          parameterProvider = (IParameterProvider) parameterProviders.get( sourceName );
          if ( parameterProvider == null ) {
            warn( Messages.getInstance().getString(
                "RuntimeContext.WARN_REQUESTED_PARAMETER_SOURCE_NOT_AVAILABLE", sourceName, inputName ) ); //$NON-NLS-1$
          } else {
            variableValue = parameterProvider.getParameter( sourceValue );
            if ( variableValue != null ) {
              break;
            }
          }
        }
      } // while

      if ( variableValue == null ) {

        if ( actionParameter.getValue() != null ) {
          if ( actionParameter.hasDefaultValue() ) {
            if ( PentahoSystem.trace ) {
              trace( Messages.getInstance().getString( "RuntimeContext.TRACE_USING_DEFAULT_PARAMETER_VALUE", inputName ) ); //$NON-NLS-1$
            }
          } else {
            if ( PentahoSystem.trace ) {
              trace( Messages.getInstance().getString(
                  "RuntimeContext.TRACE_INFO_USING_CURRENT_PARAMETER_VALUE" + inputName ) ); //$NON-NLS-1$
            }
          }
        } else if ( "content".equals( actionParameter.getType() ) ) { //$NON-NLS-1$
          variableValue = ""; //$NON-NLS-1$
        }
      } else {
        actionParameter.setValue( variableValue );
      }
    } // while
  }

  public void dispose() {
    paramManager.dispose();
  }

  public void dispose( final List actionParameters ) {
    paramManager.dispose( actionParameters );
  }

  // IParameterProvider methods
  public String getStringParameter( final String name, final String defaultValue ) {
    if ( "instance-id".equals( name ) ) { //$NON-NLS-1$
      return instanceId;
    } else if ( "solution-id".equals( name ) ) { //$NON-NLS-1$
      return "";
    }
    return defaultValue;
    // return runtimeData.getStringProperty( name, defaultValue );
  }

  // IRuntimeContext input and output methods

  public Object getInputParameterValue( final String name ) {
    Object value = null;
    IActionParameter actionParameter = paramManager.getCurrentInput( name );
    if ( actionParameter == null ) {
      // TODO need to know from the action definition if this is ok or not
      warn( Messages.getInstance().getErrorString(
          "RuntimeContext.ERROR_0019_INVALID_INPUT_REQUEST", name, actionSequence.getSequenceName() ) ); //$NON-NLS-1$
    } else {
      value = actionParameter.getValue();
    }
    return value;
  }

  public String getInputParameterStringValue( final String name ) {
    String value = null;
    IActionParameter actionParameter = paramManager.getCurrentInput( name );
    if ( actionParameter == null ) {
      // TODO need to know from the action definition if this is ok or not
      warn( Messages.getInstance().getErrorString(
          "RuntimeContext.ERROR_0019_INVALID_INPUT_REQUEST", name, actionSequence.getSequenceName() ) ); //$NON-NLS-1$
    } else {
      value = actionParameter.getStringValue();
    }
    return value;
  }

  // TODO Add to Param Manager - Need spcial case to grab loop param only from sequence inputs
  private IActionParameter getLoopParameter( final String name ) {
    IActionParameter actionParameter = paramManager.getLoopParameter( name );
    if ( actionParameter == null ) {
      // TODO need to know from the action definition if this is ok or not
      warn( Messages.getInstance().getErrorString(
          "RuntimeContext.ERROR_0020_INVALID_LOOP_PARAMETER", name, actionSequence.getSequenceName() ) ); //$NON-NLS-1$
    }
    return actionParameter;
  }

  public IActionParameter getInputParameter( final String name ) {
    IActionParameter actionParameter = paramManager.getCurrentInput( name );
    if ( actionParameter == null ) {
      // TODO need to know from the action definition if this is ok or not
      warn( Messages.getInstance().getErrorString(
          "RuntimeContext.ERROR_0019_INVALID_INPUT_REQUEST", name, actionSequence.getSequenceName() ) ); //$NON-NLS-1$
    }
    return actionParameter;
  }

  public IActionParameter getOutputParameter( final String name ) {
    IActionParameter actionParameter = paramManager.getCurrentOutput( name );
    if ( actionParameter == null ) {
      // TODO need to know from the action definition if this is ok or not
      warn( Messages.getInstance().getErrorString(
          "RuntimeContext.ERROR_0021_INVALID_OUTPUT_REQUEST", name, actionSequence.getSequenceName() ) ); //$NON-NLS-1$
    }
    return actionParameter;
  }

  public IActionSequenceResource getResourceDefintion( final String name ) {
    IActionSequenceResource actionResource = paramManager.getCurrentResource( name );

    if ( actionResource == null ) {
      // TODO need to know from the action definition if this is ok or not
      warn( Messages.getInstance().getErrorString(
          "RuntimeContext.ERROR_0022_INVALID_RESOURCE_REQUEST", name, actionSequence.getSequenceName() ) ); //$NON-NLS-1$
    }
    return actionResource;
  }

  public Set getInputNames() {
    return paramManager.getCurrentInputNames();
  }

  public void addTempParameter( final String name, final IActionParameter param ) {
    paramManager.addToCurrentInputs( name, param );
  }

  public void setOutputValue( final String name, final Object output ) {
    IActionParameter actionParameter = paramManager.getCurrentOutput( name );
    if ( actionParameter == null ) {
      throw new InvalidParameterException( Messages.getInstance().getErrorString(
          "RuntimeContext.ERROR_0021_INVALID_OUTPUT_REQUEST", name, actionSequence.getSequenceName() ) ); //$NON-NLS-1$
    }
    actionParameter.setValue( output );

    if ( output instanceof String ) {
      runtimeData.setStringProperty( name, (String) output );
    } else if ( output instanceof Date ) {
      runtimeData.setDateProperty( name, (Date) output );
    } else if ( output instanceof Long ) {
      runtimeData.setLongProperty( name, (Long) output );
    } else if ( output instanceof List ) {
      runtimeData.setListProperty( name, (List) output );
    } else if ( output instanceof Map ) {
      runtimeData.setMapProperty( name, (Map) output );
    } else if ( output instanceof IContentItem ) {
      runtimeData.setStringProperty( name, ( (IContentItem) output ).getPath() );
    }

  }

  public InputStream getInputStream( final String parameterName ) {

    InputStream inputStream = null;
    IActionParameter inputParameter = getInputParameter( parameterName );
    if ( inputParameter == null ) {
      throw new InvalidParameterException( Messages.getInstance().getErrorString(
          "RuntimeContext.ERROR_0019_INVALID_INPUT_REQUEST", parameterName, actionSequence.getSequenceName() ) ); //$NON-NLS-1$
    }
    Object value = inputParameter.getValue();
    if ( value instanceof IContentItem ) {
      IContentItem contentItem = (IContentItem) value;
      inputStream = contentItem.getInputStream();
    }
    return inputStream;
  }

  public Set getOutputNames() {
    return paramManager.getCurrentOutputNames();
  }

  public Set getResourceNames() {
    return paramManager.getCurrentResourceNames();
  }

  public InputStream getResourceInputStream( final IActionSequenceResource actionResource )
    throws FileNotFoundException {
    return actionResource.getInputStream( RepositoryFilePermission.READ, LocaleHelper.getLocale() );
  }

  public String getResourceAsString( final IActionSequenceResource actionResource ) throws IOException {
    if ( isEmbeddedResource( actionResource ) ) {
      return ( getEmbeddedResource( actionResource ) );
    }
    byte[] bytes =
        IOUtils.toByteArray( actionResource.getInputStream( RepositoryFilePermission.READ, LocaleHelper.getLocale() ) );
    return new String( bytes, LocaleHelper.getSystemEncoding() );
  }

  public Document getResourceAsDocument( final IActionSequenceResource actionResource ) throws IOException {
    if ( isEmbeddedResource( actionResource ) ) {
      try {
        return XmlDom4JHelper.getDocFromString( getEmbeddedResource( actionResource ), null );
      } catch ( XmlParseException e ) {
        error( Messages.getInstance().getString( "RuntimeContext.ERROR_UNABLE_TO_GET_RESOURCE_AS_DOCUMENT" ), e ); //$NON-NLS-1$
        return null;
      }
    }
    Document document = null;
    try {
      org.dom4j.io.SAXReader reader = new org.dom4j.io.SAXReader();
      reader.setEntityResolver( new SolutionURIResolver() );
      document = reader.read( actionResource.getInputStream(
        RepositoryFilePermission.READ, LocaleHelper.getLocale() ) );
    } catch ( Throwable t ) {
      // XML document can't be read. We'll just return a null document.
    }
    return document;
  }

  public IPentahoStreamSource getResourceDataSource( final IActionSequenceResource actionResource )
    throws FileNotFoundException {
    return new ActionSequenceResourceWrapper( actionResource, actionResource.getInputStream(
        RepositoryFilePermission.READ, LocaleHelper.getLocale() ) );
  }

  private boolean isEmbeddedResource( final IActionSequenceResource actionResource ) {
    int type = actionResource.getSourceType();
    return ( ( type == IActionSequenceResource.STRING ) || ( type == IActionSequenceResource.XML ) );
  }

  private String getEmbeddedResource( final IActionSequenceResource actionResource ) {
    String s = actionResource.getAddress();
    return ( ( s == null ) ? "" : s ); //$NON-NLS-1$
  }

  // IAuditable methods

  public String getId() {
    return handle;
  }

  public String getProcessId() {
    return processId;
  }

  public String getActionName() {
    return ( ( actionSequence != null ) ? actionSequence.getSequenceName() : Messages.getInstance().getString(
        "RuntimeContext.DEBUG_NO_ACTION" ) ); //$NON-NLS-1$
  }

  public String getActionTitle() {
    return ( ( actionSequence != null ) ? actionSequence.getTitle() : Messages.getInstance().getString(
        "RuntimeContext.DEBUG_NO_ACTION" ) ); //$NON-NLS-1$
  }

  // Audit methods

  public void audit( final List auditList ) {

    if ( ( auditList == null ) || ( auditList.size() == 0 ) ) {
      return;
    }

    // TODO pass in a list of parameter objects instead of parameter names
    Iterator it = auditList.iterator();
    while ( it.hasNext() ) {
      Element auditNode = (Element) it.next();
      String name = auditNode.getText();
      String value = getStringParameter( name, "" ); //$NON-NLS-1$
      AuditHelper.audit( this, session, MessageTypes.INSTANCE_ATTRIBUTE, name, value, 0, this );
    }

  }

  public void audit( final String messageType, final String message, final String value, final long duration ) {
    if ( !audit ) {
      return;
    }

    if ( RuntimeContext.debug ) {
      debug( Messages.getInstance().getString(
          "RuntimeContext.DEBUG_AUDIT", instanceId, getCurrentComponentName(), messageType ) ); //$NON-NLS-1$
    }
    AuditHelper.audit( this, session, messageType, message, value, (float) duration / 1000, this );
  }

  public void addInputParameter( final String name, final IActionParameter param ) {
    paramManager.addToAllInputs( name, param );
  }

  public String applyInputsToFormat( final String format ) {
    return TemplateUtil.applyTemplate( format, this );
  }

  public String applyInputsToFormat( final String format, final IParameterResolver resolver ) {
    return TemplateUtil.applyTemplate( format, this, resolver );
  }

  // Feebdack form handling

  public void sendFeedbackForm() throws ActionSequencePromptException {
    try {
      if ( !feedbackAllowed() ) {
        return;
      }
      // add the standard parameters that we need
      createFeedbackParameter( "path", "path", "", getSolutionPath(), false ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      // ProSolutionEngine proSolutionEngine = (ProSolutionEngine) solutionEngine;
      IParameterProvider parameterProvider = (IParameterProvider) parameterProviders.get( "PRO_EDIT_SUBSCRIPTION" ); //$NON-NLS-1$
      if ( parameterProvider == null ) { // Then we are not editing subscriptions
        parameterProvider = (IParameterProvider) parameterProviders.get( IParameterProvider.SCOPE_REQUEST );
      } else {
        parameterProvider.getStringParameter( "subscribe-id", null ); //$NON-NLS-1$
      }
      Iterator parameterNameIterator = parameterProvider.getParameterNames();
      while ( parameterNameIterator.hasNext() ) {
        String name = (String) parameterNameIterator.next();
        if ( !"path".equals( name ) && ( xformFields.get( name ) == null ) ) {
          // TODO we need to check to see if this has been handled as
          // a control before adding a hidden field
          Object value = parameterProvider.getParameter( name );
          if ( value != null ) {
            createFeedbackParameter( name, name, "", value, false ); //$NON-NLS-1$
          }
        }
      }
      SolutionURIResolver resolver = new SolutionURIResolver();
      if ( parameterXsl == null ) {
        // Generate XForm for the parameters needed, transform into
        // HTML, and float it down the feedback stream
        xformBody.append( "<tr><td>" ); //$NON-NLS-1$
        XForm.createXFormSubmit( RuntimeContext.PARAMETER_FORM, xformBody, Messages.getInstance().getString(
            "RuntimeContext.USER_PARAMETER_FORM_SUBMIT" ) ); //$NON-NLS-1$
        xformBody.append( "</td></tr></table></body>" ); //$NON-NLS-1$
        String html =
            XForm.completeXForm( XForm.OUTPUT_HTML_PAGE, RuntimeContext.PARAMETER_FORM, xformHeader, xformBody,
                getSession(), resolver );
        if ( RuntimeContext.debug ) {
          debug( Messages.getInstance().getString( "RuntimeContext.DEBUG_PARAMETER_HTML", html ) ); //$NON-NLS-1$
        }
        outputHandler.getFeedbackContentItem().setMimeType( "text/html" ); //$NON-NLS-1$ 
        OutputStream os = outputHandler.getFeedbackContentItem().getOutputStream( getActionName() );
        os.write( html.getBytes() );
      } else if ( parameterTemplate != null ) {
        String html =
            XForm.completeXForm( XForm.OUTPUT_HTML_PAGE, RuntimeContext.PARAMETER_FORM, xformHeader, new StringBuffer(
                parameterTemplate ), getSession(), resolver );
        if ( RuntimeContext.debug ) {
          debug( Messages.getInstance().getString( "RuntimeContext.DEBUG_PARAMETER_HTML", html ) ); //$NON-NLS-1$
        }
        IContentItem contentItem = outputHandler.getFeedbackContentItem();
        contentItem.setMimeType( "text/html" ); //$NON-NLS-1$ 
        OutputStream os = contentItem.getOutputStream( getActionName() );
        os.write( html.getBytes( LocaleHelper.getSystemEncoding() ) );
        os.close();
      } else if ( parameterXsl.endsWith( ".xsl" ) ) { //$NON-NLS-1$
        String id = actionSequence.getSequenceName();
        int pos = id.indexOf( '.' );
        if ( pos > -1 ) {
          id = id.substring( 0, pos );
        }
        // make sure the id can form a valid javascript variable or
        // function name
        id = id.replace( '-', '_' );
        id = id.replace( ' ', '_' );
        String actionUrl = urlFactory.getActionUrlBuilder().getUrl();
        String displayUrl = urlFactory.getDisplayUrlBuilder().getUrl();
        // String target = (parameterTarget == null) ? "" : parameterTarget; //$NON-NLS-1$
        XForm.completeXFormHeader( RuntimeContext.PARAMETER_FORM, xformHeader );
        Document document =
            XmlDom4JHelper
                .getDocFromString(
                    "<?xml version=\"1.0\" encoding=\"" + LocaleHelper.getSystemEncoding() + "\" ?><filters xmlns:xf=\"http://www.w3.org/2002/xforms\">" + //$NON-NLS-1$ //$NON-NLS-2$
                        xformHeader
                        + "<id><![CDATA[" + //$NON-NLS-1$
                        id
                        + "]]></id><title><![CDATA[" + //$NON-NLS-1$
                        Messages.getInstance().getEncodedString( actionSequence.getTitle() )
                        + "]]></title><description><![CDATA[" + //$NON-NLS-1$
                        Messages.getInstance().getEncodedString( actionSequence.getDescription() )
                        + "]]></description><icon><![CDATA[" + //$NON-NLS-1$
                        actionSequence.getIcon() + "]]></icon><help><![CDATA[" + //$NON-NLS-1$
                        Messages.getInstance().getEncodedString( actionSequence.getHelp() ) + "]]></help>" + //$NON-NLS-1$
                        "<action><![CDATA[" + actionUrl + "]]></action>" + //$NON-NLS-1$ //$NON-NLS-2$
                        "<display><![CDATA[" + displayUrl + "]]></display>" + //$NON-NLS-1$ //$NON-NLS-2$
                        ( ( parameterTarget != null ) ? "<target>" + parameterTarget + "</target>" : "" ) + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        xformBody.toString() + "</filters>", null ); //$NON-NLS-1$ 
        // add any subscription information here
        Element root = document.getRootElement();

        // notify the xsl whether we're in parameter view or not.
        root.addAttribute(
            "parameterView", ( getOutputPreference() == IOutputHandler.OUTPUT_TYPE_PARAMETERS ) ? "true" : "false" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put( "baseUrl", PentahoSystem.getApplicationContext().getBaseUrl() ); //$NON-NLS-1$
        parameters.put( "actionUrl", this.getUrlFactory().getActionUrlBuilder().getUrl() ); //$NON-NLS-1$
        parameters.put( "displayUrl", this.getUrlFactory().getDisplayUrlBuilder().getUrl() ); //$NON-NLS-1$
        // Uncomment this line for troubleshooting the XSL.
        StringBuffer content =
            XmlHelper.transformXml( parameterXsl, getSolutionPath(), document.asXML(), parameters, resolver );

        IContentItem contentItem = outputHandler.getFeedbackContentItem();
        contentItem.setMimeType( "text/html" ); //$NON-NLS-1$ 
        OutputStream os = contentItem.getOutputStream( getActionName() );
        try {
          os.write( content.toString().getBytes( LocaleHelper.getSystemEncoding() ) );
        } finally {
          if ( os != null ) {
            os.close();
          }
        }
      }
      /*
       * We need to catch checked and unchecked exceptions here so we can create an ActionSequeceException with
       * contextual information, including the root cause. Allowing unchecked exceptions to pass through would
       * prevent valuable feedback in the log or response.
       */
    } catch ( Throwable e ) {
      throw new ActionSequencePromptException( Messages.getInstance().getErrorString(
          "RuntimeContext.ERROR_0030_SEND_FEEDBACKFORM" ), e, //$NON-NLS-1$
          session.getName(), instanceId, getActionSequence().getSequenceName(), null );
    }
  }

  private void addXFormHeader() {

    XForm.createXFormHeader( RuntimeContext.PARAMETER_FORM, xformHeader );

    IActionSequenceResource resource = paramManager.getCurrentResource( parameterXsl );

    if ( !parameterXsl.endsWith( ".xsl" ) && ( resource != null ) ) { //$NON-NLS-1$
      // load the parameter page template
      try {
        parameterTemplate = getResourceAsString( resource );
      } catch ( Exception e ) {
        // TODO log this
      }
    }

  }

  /**
   * @deprecated Unused
   */
  @Deprecated
  public void createFeedbackParameter( final IActionParameter actionParam ) {
    if ( actionParam.hasSelections() ) {
      // TODO support display styles
      // TODO support help hints
      createFeedbackParameter( actionParam.getName(), actionParam.getSelectionDisplayName(),
          "", actionParam.getStringValue(), actionParam.getSelectionValues(), actionParam.getSelectionNameMap(), null ); //$NON-NLS-1$
    }
  }

  //CHECKSTYLE IGNORE Indentation FOR NEXT 2 LINES
  public void
  createFeedbackParameter( final ISelectionMapper selMap, final String fieldName,
                             final Object defaultValues ) {
    createFeedbackParameter( selMap, fieldName, defaultValues, false );
  }

  public void createFeedbackParameter( final ISelectionMapper selMap, final String fieldName,
      final Object defaultValues, final boolean optional ) {
    if ( selMap != null ) {
      createFeedbackParameter(
          fieldName,
          selMap.getSelectionDisplayName(),
          "", defaultValues, selMap.getSelectionValues(), selMap.getSelectionNameMap(), selMap.getDisplayStyle(), optional ); //$NON-NLS-1$
    }
  }

  public void createFeedbackParameter( final String fieldName, final String displayName, final String hint,
      final Object defaultValues, final List values, final Map dispNames, final String displayStyle ) {
    createFeedbackParameter( fieldName, displayName, hint, defaultValues, values, dispNames, displayStyle, false );
  }

  public void createFeedbackParameter( String fieldName, final String displayName, String hint, Object defaultValues,
      final List values, final Map dispNames, final String displayStyle, final boolean optional ) {

    if ( createFeedbackParameterCallback != null ) {
      createFeedbackParameterCallback.createFeedbackParameter( this, fieldName, displayName, hint, defaultValues,
          values, dispNames, displayStyle, optional, true );
    }

    // If there is a "PRO_EDIT_SUBSCRIPTION" param provider, then we must be editing a subscription so use its
    // values
    IParameterProvider parameterProvider = (IParameterProvider) parameterProviders.get( "PRO_EDIT_SUBSCRIPTION" ); //$NON-NLS-1$
    if ( parameterProvider != null ) {
      defaultValues = parameterProvider.getParameter( paramManager.getActualRequestParameterName( fieldName ) );
    }

    if ( values == null ) {
      return;
    }
    if ( ( xformHeader == null ) || ( xformHeader.length() == 0 ) ) {
      // this is the first parameter, need to create the header...
      addXFormHeader();
    }

    // See if the parameter is defined in the template. If so, then
    // don't add it to the XForm.
    if ( checkForFieldInTemplate( fieldName ) ) {
      return;
    }

    int type = ( values.size() < 6 ) ? XForm.TYPE_RADIO : XForm.TYPE_SELECT;
    if ( displayStyle != null ) {
      if ( "text-box".equals( displayStyle ) ) { //$NON-NLS-1$
        type = XForm.TYPE_TEXT;
      } else if ( "radio".equals( displayStyle ) ) { //$NON-NLS-1$
        type = XForm.TYPE_RADIO;
      } else if ( "select".equals( displayStyle ) ) { //$NON-NLS-1$
        type = XForm.TYPE_SELECT;
      } else if ( "list".equals( displayStyle ) ) { //$NON-NLS-1$
        type = XForm.TYPE_LIST;
      } else if ( "list-multi".equals( displayStyle ) ) { //$NON-NLS-1$
        type = XForm.TYPE_LIST_MULTI;
      } else if ( "check-multi".equals( displayStyle ) ) { //$NON-NLS-1$
        type = XForm.TYPE_CHECK_MULTI;
      } else if ( "check-multi-scroll".equals( displayStyle ) ) { //$NON-NLS-1$
        type = XForm.TYPE_CHECK_MULTI_SCROLL;
      } else if ( "check-multi-scroll-2-column".equals( displayStyle ) ) { //$NON-NLS-1$
        type = XForm.TYPE_CHECK_MULTI_SCROLL_2_COLUMN;
      } else if ( "check-multi-scroll-3-column".equals( displayStyle ) ) { //$NON-NLS-1$
        type = XForm.TYPE_CHECK_MULTI_SCROLL_3_COLUMN;
      } else if ( "check-multi-scroll-4-column".equals( displayStyle ) ) { //$NON-NLS-1$
        type = XForm.TYPE_CHECK_MULTI_SCROLL_4_COLUMN;
      }

    }
    fieldName = paramManager.getActualRequestParameterName( fieldName );
    if ( hint == null ) {
      hint = ""; //$NON-NLS-1$
    }
    if ( parameterXsl == null ) {
      // create some xform to represent this parameter...
      xformBody
          .append( Messages.getInstance().getString( "RuntimeContext.CODE_XFORM_CONTROL_LABEL_START", displayName ) ); //$NON-NLS-1$
      XForm.createXFormControl( type, fieldName, defaultValues, values, dispNames, RuntimeContext.PARAMETER_FORM,
          xformHeader, xformBody );
      xformBody.append( Messages.getInstance().getString( "RuntimeContext.CODE_XFORM_CONTROL_LABEL_END" ) ); //$NON-NLS-1$
    } else if ( parameterTemplate != null ) {
      StringBuffer body = new StringBuffer();
      XForm.createXFormControl( type, fieldName, defaultValues, values, dispNames, RuntimeContext.PARAMETER_FORM,
          xformHeader, body );
      parameterTemplate = parameterTemplate.replaceAll( "\\{" + fieldName + "\\}", body.toString() ); //$NON-NLS-1$ //$NON-NLS-2$
    } else if ( parameterXsl.endsWith( ".xsl" ) ) { //$NON-NLS-1$
      StringBuffer body = new StringBuffer();
      XForm.createXFormControl( type, fieldName, defaultValues, values, dispNames, RuntimeContext.PARAMETER_FORM,
          xformHeader, body );
      xformBody.append( "<filter" ); //$NON-NLS-1$
      if ( optional ) {
        xformBody.append( " optional=\"true\"" ); //$NON-NLS-1$
      }
      xformBody.append( "><id><![CDATA[" + fieldName + "]]></id>" ) //$NON-NLS-1$ //$NON-NLS-2$
          .append( "<title><![CDATA[" + displayName + "]]></title>" ) //$NON-NLS-1$ //$NON-NLS-2$
          .append( "<help><![CDATA[" + hint + "]]></help><control>" ) //$NON-NLS-1$ //$NON-NLS-2$
          .append( body ).append( "</control></filter>" ); //$NON-NLS-1$
    }

    xformFields.put( fieldName, fieldName );

  }

  public boolean checkForFieldInTemplate( final String fieldName ) {
    //
    // This pattern looks for:
    //
    // id="fieldname"
    // iD="fieldname"
    // Id="fieldname"
    // ID="fieldname"
    // id='fieldname'
    // iD='fieldname'
    // Id='fieldname'
    // ID='fieldname'
    //
    // TODO: This is actually optimistic searching as it's not looking for the
    // string within the form portion of the template. IMO, to be more robust,
    // this needs to at least look for something only within a form and only
    // within a control on a form.
    if ( ( parameterTemplate == null ) || ( parameterTemplate.length() == 0 ) ) {
      return false;
    }
    String regex = "[iI][dD]=[\'\"]" + fieldName + "[\'\"]"; //$NON-NLS-1$ //$NON-NLS-2$
    Pattern pattern = null;
    // Normally shouldn't need to synchronize. But, a Java bug in
    // pattern compilation on multi-processor machines results in the
    // need to synchronize a small block of code. If/when this problem
    // is fixed, we can remove this synchronization lock.
    // See: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6238699
    synchronized ( RuntimeContext.PATTERN_COMPILE_LOCK ) {
      pattern = Pattern.compile( regex );
    }
    Matcher matcher = pattern.matcher( parameterTemplate );
    if ( matcher.find() ) {
      return true;
    }
    return false;
  }

  public void createFeedbackParameter( final String fieldName, final String displayName, final String hint,
      final Object defaultValue, final boolean visible ) {
    createFeedbackParameter( fieldName, displayName, hint, defaultValue, visible, false );
  }

  public void createFeedbackParameter( String fieldName, final String displayName, String hint, Object defaultValue,
      final boolean visible, final boolean optional ) {

    // If there is a "PRO_EDIT_SUBSCRIPTION" param provider, then we must be editing a subscription so use its
    // values
    IParameterProvider parameterProvider = (IParameterProvider) parameterProviders.get( "PRO_EDIT_SUBSCRIPTION" ); //$NON-NLS-1$
    if ( parameterProvider != null ) {
      Object newValue = parameterProvider.getParameter( paramManager.getActualRequestParameterName( fieldName ) );
      defaultValue = newValue == null ? defaultValue : newValue;
    }

    if ( createFeedbackParameterCallback != null ) {
      createFeedbackParameterCallback.createFeedbackParameter( this, fieldName, displayName, hint, defaultValue, null,
          null, null, optional, visible );
    }

    if ( ( xformHeader == null ) || ( xformHeader.length() == 0 ) ) {
      // this is the first parameter, need to create the header...
      addXFormHeader();
    }
    if ( parameterTemplate != null ) {
      // see if the parameter is defined in the HTML template
      if ( checkForFieldInTemplate( fieldName ) ) {
        return;
      }
    }
    if ( hint == null ) {
      hint = ""; //$NON-NLS-1$
    }
    fieldName = paramManager.getActualRequestParameterName( fieldName );
    if ( parameterXsl == null ) {
      // create some xform to represent this parameter...

      if ( visible ) {
        xformBody.append( Messages.getInstance().getString(
            "RuntimeContext.CODE_XFORM_CONTROL_LABEL_START", displayName ) ); //$NON-NLS-1$
      }
      XForm
          .createXFormControl( fieldName, defaultValue, RuntimeContext.PARAMETER_FORM,
            xformHeader, xformBody, visible );
      if ( visible ) {
        xformBody.append( Messages.getInstance().getString( "RuntimeContext.CODE_XFORM_CONTROL_LABEL_END" ) ); //$NON-NLS-1$
      }
    } else if ( parameterTemplate != null ) {
      StringBuffer body = new StringBuffer();
      if ( visible ) {
        XForm.createXFormControl( fieldName, defaultValue, RuntimeContext.PARAMETER_FORM, xformHeader, body, visible );
      } else {
        try {
          if ( defaultValue instanceof Object[] ) {
            setObjectArrayParameters( fieldName, (Object[]) defaultValue );
          }
          String value = StringEscapeUtils.escapeXml( defaultValue.toString() );
          body.append( "<input type=\"hidden\" name=\"" + fieldName + "\" value=\"" + value + "\"></input>" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        } catch ( Exception e ) {
          body.append( "<input type=\"hidden\" name=\"" + fieldName + "\" value=\"" + defaultValue + "\"></input>" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
      }
      parameterTemplate = parameterTemplate.replaceAll( "\\{" + fieldName + "\\}", Matcher.quoteReplacement( body.toString() ) ); //$NON-NLS-1$ //$NON-NLS-2$
    } else {
      if ( visible ) {
        StringBuffer body = new StringBuffer();
        XForm.createXFormControl( fieldName, defaultValue, RuntimeContext.PARAMETER_FORM, xformHeader, body, visible );
        xformBody.append( "<filter" ); //$NON-NLS-1$
        if ( optional ) {
          xformBody.append( " optional=\"true\"" ); //$NON-NLS-1$
        }
        xformBody.append( "><id><![CDATA[" + fieldName + "]]></id>" ) //$NON-NLS-1$ //$NON-NLS-2$
            .append( "<title><![CDATA[" + displayName + "]]></title>" ) //$NON-NLS-1$ //$NON-NLS-2$
            .append( "<help><![CDATA[" + hint + "]]></help><control>" ) //$NON-NLS-1$ //$NON-NLS-2$
            .append( body ).append( "</control></filter>" ); //$NON-NLS-1$

      } else {
        try {
          if ( defaultValue instanceof Object[] ) {
            setObjectArrayParameters( fieldName, (Object[]) defaultValue );
          } else {
            String value = defaultValue.toString().replaceAll( "&", "&amp;" ); //$NON-NLS-1$//$NON-NLS-2$
            value = value.replaceAll( "\"", "''" ); //$NON-NLS-1$ //$NON-NLS-2$
            xformBody.append( "<input type=\"hidden\" name=\"" + fieldName + "\" value=\"" + value + "\"></input>" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
          }
        } catch ( Exception e ) {
          xformBody
              .append( "<input type=\"hidden\" name=\"" + fieldName + "\" value=\"" + defaultValue + "\"></input>" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
      }
    }
    xformFields.put( fieldName, fieldName );
  }

  private void setObjectArrayParameters( final String fieldName, final Object[] values ) {
    for ( Object element : values ) {
      String value = element.toString().replaceAll( "&", "&amp;" ); //$NON-NLS-1$//$NON-NLS-2$
      value = value.replaceAll( "\"", "''" ); //$NON-NLS-1$ //$NON-NLS-2$
      xformBody.append( "<input type=\"hidden\" name=\"" + fieldName + "\" value=\"" + value + "\"></input>" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
  }

  public void setParameterXsl( final String xsl ) {
    this.parameterXsl = xsl;
  }

  public void setParameterTarget( final String target ) {
    this.parameterTarget = target;
  }

  @Override
  public String getLogId() {
    return logId;
  }

  /**
   * Forces the immediate write of runtime data to underlying persistence mechanism. In the case of using Hibernate
   * for the runtime data persistence, this works out to a call to HibernateUtil.flush().
   */
  public void forceSaveRuntimeData() {
    if ( runtimeData != null ) {
      runtimeData.forceSave();
    }
  }

  /**
   * Gets the output type preferred by the handler. Values are defined in org.pentaho.core.solution.IOutputHander
   * and are OUTPUT_TYPE_PARAMETERS, OUTPUT_TYPE_CONTENT, or OUTPUT_TYPE_DEFAULT
   * 
   * @return Output type
   */
  public int getOutputPreference() {
    if ( outputHandler != null ) {
      return outputHandler.getOutputPreference();
    } else {
      return IOutputHandler.OUTPUT_TYPE_DEFAULT;
    }
  }

  public void setOutputHandler( final IOutputHandler outputHandler ) {
    this.outputHandler = outputHandler;
  }

  public IActionSequence getActionSequence() {
    return actionSequence;
  }

  public IParameterManager getParameterManager() {
    return paramManager;
  }

  public Map getParameterProviders() {
    // TODO Auto-generated method stub
    return null;
  }

  public void setCreateFeedbackParameterCallback( ICreateFeedbackParameterCallback callback ) {
    createFeedbackParameterCallback = callback;
  }
}
