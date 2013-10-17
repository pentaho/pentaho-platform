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

package org.pentaho.platform.plugin.action.jfreereport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.ui.RefineryUtilities;
import org.pentaho.actionsequence.dom.ActionInput;
import org.pentaho.actionsequence.dom.ActionResource;
import org.pentaho.actionsequence.dom.IActionInput;
import org.pentaho.actionsequence.dom.IActionOutput;
import org.pentaho.actionsequence.dom.IActionResource;
import org.pentaho.actionsequence.dom.actions.ActionDefinition;
import org.pentaho.actionsequence.dom.actions.ActionFactory;
import org.pentaho.actionsequence.dom.actions.JFreeReportAction;
import org.pentaho.commons.connection.ActivationHelper;
import org.pentaho.commons.connection.IPentahoMetaData;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.data.IDataComponent;
import org.pentaho.platform.api.data.IPreparedComponent;
import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.IComponent;
import org.pentaho.platform.api.engine.IPentahoRequestContext;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.jfreereport.components.JFreeReportValidateParametersComponent;
import org.pentaho.platform.plugin.action.jfreereport.helper.PentahoDataFactory;
import org.pentaho.platform.plugin.action.jfreereport.helper.PentahoResourceBundleFactory;
import org.pentaho.platform.plugin.action.jfreereport.helper.PentahoResourceLoader;
import org.pentaho.platform.plugin.action.jfreereport.helper.PentahoTableDataFactory;
import org.pentaho.platform.plugin.action.jfreereport.helper.PentahoTableModel;
import org.pentaho.platform.plugin.action.jfreereport.helper.PentahoURLRewriter;
import org.pentaho.platform.plugin.action.jfreereport.helper.ReportUtils;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.util.xml.XmlHelper;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.ParameterDataRow;
import org.pentaho.reporting.engine.classic.core.ReportProcessingException;
import org.pentaho.reporting.engine.classic.core.ResourceBundleFactory;
import org.pentaho.reporting.engine.classic.core.layout.output.YieldReportListener;
import org.pentaho.reporting.engine.classic.core.modules.gui.base.PreviewDialog;
import org.pentaho.reporting.engine.classic.core.modules.gui.base.ReportController;
import org.pentaho.reporting.engine.classic.core.modules.gui.print.PrintUtil;
import org.pentaho.reporting.engine.classic.core.modules.output.pageable.base.PageableReportProcessor;
import org.pentaho.reporting.engine.classic.core.modules.output.pageable.pdf.PdfOutputProcessor;
import org.pentaho.reporting.engine.classic.core.modules.output.table.base.FlowReportProcessor;
import org.pentaho.reporting.engine.classic.core.modules.output.table.base.StreamReportProcessor;
import org.pentaho.reporting.engine.classic.core.modules.output.table.csv.StreamCSVOutputProcessor;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.AllItemsHtmlPrinter;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.HtmlOutputProcessor;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.HtmlPrinter;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.StreamHtmlOutputProcessor;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.URLRewriter;
import org.pentaho.reporting.engine.classic.core.modules.output.table.rtf.StreamRTFOutputProcessor;
import org.pentaho.reporting.engine.classic.core.modules.output.table.xls.FlowExcelOutputProcessor;
import org.pentaho.reporting.engine.classic.core.modules.output.xml.XMLProcessor;
import org.pentaho.reporting.engine.classic.core.modules.parser.base.ReportGenerator;
import org.pentaho.reporting.engine.classic.core.util.ReportParameterValues;
import org.pentaho.reporting.engine.classic.extensions.modules.java14print.Java14PrintUtil;
import org.pentaho.reporting.libraries.base.config.Configuration;
import org.pentaho.reporting.libraries.base.config.ModifiableConfiguration;
import org.pentaho.reporting.libraries.base.util.IOUtils;
import org.pentaho.reporting.libraries.repository.ContentIOException;
import org.pentaho.reporting.libraries.repository.ContentLocation;
import org.pentaho.reporting.libraries.repository.DefaultNameGenerator;
import org.pentaho.reporting.libraries.repository.NameGenerator;
import org.pentaho.reporting.libraries.repository.file.FileRepository;
import org.pentaho.reporting.libraries.repository.stream.StreamRepository;
import org.pentaho.reporting.libraries.resourceloader.FactoryParameterKey;
import org.pentaho.reporting.libraries.resourceloader.ResourceException;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import org.xml.sax.InputSource;

import javax.activation.DataSource;
import javax.print.DocFlavor;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.swing.table.TableModel;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * The JFreeReportComponent provides a simple-to-use frontend for the reporting process.
 * <p/>
 * This component will execute JFreeReport reports in one of two different modes:
 * <p/>
 * <h3>sqlMode = true</h3>
 * <p/>
 * This means that the JFreeReport component is expected to execute an SQL query, wrap the resulting SQL Resultset in a
 * TableModel, and use that to execute a report whose definition is in the file system. In this mode, the
 * action-sequence definition must contain the following elements: In the resource-definition section, there must be a
 * resource called "report-definition" which defines the location of the jfreereport xml document.
 * <p/>
 * In the component-definition sction, there must be the following entries:
 * <ul>
 * <li>A "query" parameter which contains an SQL query.</li>
 * <li>Either for connecting to the SQL datasource:
 * <ul>
 * <li>A "jndi" parameter (with the jndi name of the datasource)</li>
 * <li>or The database parameters "driver", "user-id", "password" and "connection" so that a database connection can be
 * established for running the afore-mentioned "query".</li>
 * </ul>
 * </li>
 * </ul>
 * <h3>sqlMode = false</h3>
 * <p/>
 * This means that the JFreeReport component is expected to execute a report that exists in a .jar file (like the
 * reporting demo reports) along with the TableModel class that provides the data for the report. In this mode, the
 * action-sequence definition must contain the following elements: In the resource-definition section, there must be a
 * resource called "report-jar" that points to the .jar file that contains the report .xml file, and the TableModel
 * implementation.
 * <p/>
 * In the component-definition section, there must be two entries:
 * <ul>
 * <li>"report-location" - This is the location of the report .xml document (e.g. org/jfree/report/demo/report1.xml)
 * </li>
 * <li>"class-location" - This is the package-qualified class that implements TableModel (e.g.
 * org.jfree.report.demo.SampleData1).</li>
 * </ul>
 * 
 * @author mbatchel
 * @created Sep 8, 2005
 */
@SuppressWarnings( "deprecation" )
public class JFreeReportComponent extends AbstractJFreeReportComponent {
  protected static final Log logger = LogFactory.getLog( JFreeReportComponent.class );

  private static final long serialVersionUID = -4185151399689983507L;

  private static final int INIT_REPORT_PARAMS_STATUS_PASSED = 1;

  private static final int INIT_REPORT_PARAMS_STATUS_FAILED = 2;

  private static final int INIT_REPORT_PARAMS_STATUS_PROMPT_PENDING = 3;

  private static final String PROGRESS_DIALOG_ENABLED_KEY = "org.jfree.report.modules.gui.base.ProgressDialogEnabled"; //$NON-NLS-1$

  private static final String PROGRESS_BAR_ENABLED_KEY = "org.jfree.report.modules.gui.base.ProgressBarEnabled"; //$NON-NLS-1$

  private static final boolean DO_NOT_USE_THE_CONTENT_REPOSITORY = true;

  private JFreeReportValidateParametersComponent validateParametersComponent;

  public JFreeReportComponent() {
  }

  @Override
  public boolean init() {
    return true;
  }

  @Override
  public void done() {
  }

  @Override
  public boolean validateSystemSettings() {
    return true;
  }

  @Override
  public Log getLogger() {
    return LogFactory.getLog( JFreeReportComponent.class );
  }

  protected boolean initAndValidate( final IComponent component ) {
    component.setInstanceId( getInstanceId() );
    component.setActionName( getActionName() );
    component.setProcessId( getProcessId() );
    component.setComponentDefinition( getComponentDefinition() );
    component.setRuntimeContext( getRuntimeContext() );
    component.setSession( getSession() );
    component.setLoggingLevel( getLoggingLevel() );
    component.setMessages( getMessages() );
    return ( component.validate() == IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_OK );
  }

  /**
   * We cannot validate the parameters of all components, as the required parameters might not have been created.
   * 
   * @return
   */
  @Override
  public boolean validateAction() {

    boolean result = true;
    if ( !( getActionDefinition() instanceof JFreeReportAction ) ) {
      error( Messages.getInstance().getErrorString(
          "ComponentBase.ERROR_0001_UNKNOWN_ACTION_TYPE", getActionDefinition().getElement().asXML() ) ); //$NON-NLS-1$
      result = false;
    } else {
      validateParametersComponent = new JFreeReportValidateParametersComponent();
      if ( initAndValidate( validateParametersComponent ) == false ) {
        error( Messages.getInstance().getString( "JFreeReportComponent.ERROR_0025_COULD_NOT_VALIDATE" ) ); //$NON-NLS-1$
        result = false;
      }
    }
    return result;
  }

  private boolean isParameterUIAvailable() {
    /*
     * See if we are allowed to generate a parameter selection user interface. If we are being called as part of a
     * process, this will not be allowed.
     */
    if ( !feedbackAllowed() ) {
      // We could not get an output stream for the feedback, but we are
      // allowed
      // to generate UI, so return an error
      error( Messages.getInstance().getErrorString( "JFreeReport.ERROR_0020_INVALID_FEEDBACK_STREAM" ) ); //$NON-NLS-1$
      return false;
    }
    // We need input from the user, we have delivered an input form into the
    // feeback stream
    setFeedbackMimeType( "text/html" ); //$NON-NLS-1$
    return true;
  }

  private int initReportParams() {
    JFreeReportAction jFreeReportAction = (JFreeReportAction) getActionDefinition();
    int result = JFreeReportComponent.INIT_REPORT_PARAMS_STATUS_PASSED;
    final String defaultValue = ""; //$NON-NLS-1$

    IActionInput[] actionInputs = jFreeReportAction.getInputs();
    for ( IActionInput element : actionInputs ) {
      Object paramValue = element.getValue();
      String inputName = element.getName();
      if ( ( paramValue == null ) || ( "".equals( paramValue ) ) ) //$NON-NLS-1$
      {
        IActionParameter paramParameter = getInputParameter( inputName );
        if ( paramParameter.getPromptStatus() == IActionParameter.PROMPT_PENDING ) {
          result = JFreeReportComponent.INIT_REPORT_PARAMS_STATUS_PROMPT_PENDING;
          continue;
        }
        if ( isParameterUIAvailable() ) {
          // The parameter value was not provided, and we are allowed
          // to
          // create user interface forms
          createFeedbackParameter( inputName, inputName, "", defaultValue, true ); //$NON-NLS-1$
          result = JFreeReportComponent.INIT_REPORT_PARAMS_STATUS_PROMPT_PENDING;
        } else {
          result = JFreeReportComponent.INIT_REPORT_PARAMS_STATUS_FAILED;
        }
      }
    }
    return result;
  }

  /**
   * This method gets called from the outside. Based upon our mode call the correct function.
   */
  @Override
  public boolean executeAction() {
    int initParamsResult = initReportParams();
    boolean result = false;
    if ( initParamsResult == JFreeReportComponent.INIT_REPORT_PARAMS_STATUS_FAILED ) {
      result = false;
    } else if ( ( initParamsResult == JFreeReportComponent.INIT_REPORT_PARAMS_STATUS_PROMPT_PENDING )
        || isPromptPending() ) {
      result = true;
    } else if ( initParamsResult == JFreeReportComponent.INIT_REPORT_PARAMS_STATUS_PASSED ) {
      result = executeReportAction();
    }
    return result;
  }

  protected boolean executeReportAction() {
    boolean result = false;
    try {
      MasterReport report = getReport();
      if ( report != null ) {
        addTempParameterObject( AbstractJFreeReportComponent.DATACOMPONENT_REPORTTEMP_OBJINPUT, report );
        if ( initReportConfigParameters( report ) && ( initReportInputs( report ) ) ) {
          result = generateReport( report, getDataFactory() );
        }
      }
    } catch ( ClassNotFoundException ex ) {
      error( Messages.getInstance().getErrorString( "JFreeReport.ERROR_0021_DATA_COMPONENT_FAILED" ), ex ); //$NON-NLS-1$
    } catch ( InstantiationException ex ) {
      error( Messages.getInstance().getErrorString( "JFreeReport.ERROR_0021_DATA_COMPONENT_FAILED" ), ex ); //$NON-NLS-1$
    } catch ( IllegalAccessException ex ) {
      error( Messages.getInstance().getErrorString( "JFreeReport.ERROR_0021_DATA_COMPONENT_FAILED" ), ex ); //$NON-NLS-1$
    } catch ( IOException ex ) {
      error( Messages.getInstance().getErrorString( "JFreeReport.ERROR_0008_INVALID_OUTPUT_STREAM" ), ex ); //$NON-NLS-1$
    } catch ( Exception ex ) {
      error( ex.getMessage() );
    }
    return result;
  }

  protected PentahoTableDataFactory getDataFactory() throws ClassNotFoundException, InstantiationException,
    IllegalAccessException, Exception {
    PentahoTableDataFactory factory = null;
    factory = getQueryComponentDataFactory();
    if ( factory == null ) {
      factory = getInputParamDataFactory();
    }
    if ( factory == null ) {
      factory = getJarDataFactory();
    }
    if ( factory == null ) {
      throw new Exception( Messages.getInstance().getString( "JFreeReport.ERROR_0022_DATA_INPUT_INVALID_OBJECT" ) ); //$NON-NLS-1$
    }
    return factory;
  }

  private PentahoTableDataFactory getQueryComponentDataFactory() throws ClassNotFoundException, InstantiationException,
    IllegalAccessException, Exception {
    JFreeReportAction jFreeReportAction = (JFreeReportAction) getActionDefinition();
    PentahoTableDataFactory factory = null;
    String dataComponentName = jFreeReportAction.getDataComponent().getStringValue();
    String origComponentName = jFreeReportAction.getComponentName();
    if ( dataComponentName != null ) {
      if ( JFreeReportAction.SQL_DATA_SOURCE.equalsIgnoreCase( dataComponentName ) ) {
        dataComponentName = AbstractJFreeReportComponent.DATACOMPONENT_SQLCLASS;
      } else if ( JFreeReportAction.MDX_DATA_SOURCE.equalsIgnoreCase( dataComponentName ) ) {
        dataComponentName = AbstractJFreeReportComponent.DATACOMPONENT_MDXCLASS;
      }
      try {
        // This is a giant hack and a big no, no. Basically we're going to transform the JFreeReportAction into a
        // SQL or MDX lookup action, by changing its component name. Then we create the appropriate component to run the
        // transformed action.
        // All this to support the DB and Query info being embedded in the JFreeReport action. This is definitely
        // deprecated functionality
        // that should not be relied upon. The correct way to do this is to create an SQL or MDX action prior to the
        // JFreeReport
        // action in the action sequence. That action performs the desired query, then pass the results of that query to
        // the JFreeReport
        // action.
        jFreeReportAction.setComponentName( dataComponentName );
        ActionDefinition tmpActionDefinition =
            ActionFactory.getActionDefinition( jFreeReportAction.getElement(), jFreeReportAction
                .getActionParameterMgr() );
        final Class componentClass = Class.forName( dataComponentName );
        IDataComponent dataComponent = (IDataComponent) componentClass.newInstance();
        dataComponent.setInstanceId( getInstanceId() );
        dataComponent.setActionName( getActionName() );
        dataComponent.setProcessId( getProcessId() );
        dataComponent.setActionDefinition( tmpActionDefinition );
        dataComponent.setComponentDefinition( getComponentDefinition() );
        dataComponent.setRuntimeContext( getRuntimeContext() );
        dataComponent.setSession( getSession() );
        dataComponent.setLoggingLevel( getLoggingLevel() );
        dataComponent.setMessages( getMessages() );
        // if that fails, then we know we messed up again.
        // Abort, we cant continue anyway.
        if ( ( dataComponent.validate() == IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_OK ) && dataComponent.init()
            && ( dataComponent.execute() == IRuntimeContext.RUNTIME_STATUS_SUCCESS ) ) {
          final IPentahoResultSet resultset = dataComponent.getResultSet();
          factory =
              new PentahoTableDataFactory( AbstractJFreeReportComponent.DATACOMPONENT_DEFAULTINPUT,
                  new PentahoTableModel( resultset ) );
        } else {
          throw new IllegalArgumentException( Messages.getInstance().getErrorString(
              "JFreeReport.ERROR_0021_DATA_COMPONENT_FAILED" ) ); //$NON-NLS-1$
        }
      } catch ( ClassNotFoundException e ) {
        JFreeReportComponent.logger.error( null, e );
      } catch ( InstantiationException e ) {
        JFreeReportComponent.logger.error( null, e );
      } catch ( IllegalAccessException e ) {
        JFreeReportComponent.logger.error( null, e );
      } finally {
        jFreeReportAction.setComponentName( origComponentName );
      }
    }
    return factory;
  }

  private PentahoTableDataFactory getJarDataFactory() throws Exception {
    PentahoTableDataFactory factory = null;
    JFreeReportAction jFreeReportAction = (JFreeReportAction) getActionDefinition();
    try {
      org.pentaho.actionsequence.dom.IActionResource actionResource = jFreeReportAction.getDataJar().getJar();
      if ( actionResource != null ) {
        DataSource dataSource = new ActivationHelper.PentahoStreamSourceWrapper( actionResource.getDataSource() );
        InputStream in = dataSource.getInputStream();
        try {
          // not being able to read a single char is definitly a big boo ..
          if ( in.read() == -1 ) {
            throw new Exception( Messages.getInstance().getErrorString( "JFreeReport.ERROR_0009_REPORT_JAR_UNREADABLE" ) ); //$NON-NLS-1$
          } else {
            final ClassLoader loader =
                ReportUtils.createJarLoader( getSession(), getResource( actionResource.getName() ) );
            if ( loader == null ) {
              throw new Exception( Messages.getInstance().getString(
                "JFreeReportDataComponent.ERROR_0035_COULD_NOT_CREATE_CLASSLOADER" ) ); //$NON-NLS-1$
            } else if ( !isDefinedInput( AbstractJFreeReportComponent.DATACOMPONENT_CLASSLOCINPUT ) ) {
              throw new Exception( Messages.getInstance().getErrorString(
                "JFreeReport.ERROR_0012_CLASS_LOCATION_MISSING" ) ); //$NON-NLS-1$
            } else {
              // Get input parameters, and set them as properties in the report
              // object.
              final ReportParameterValues reportProperties = new ReportParameterValues();
              IActionInput[] actionInputs = jFreeReportAction.getInputs();
              for ( IActionInput element : actionInputs ) {
                final Object paramValue = element.getValue();
                if ( paramValue instanceof Object[] ) {
                  final Object[] values = (Object[]) paramValue;
                  final StringBuffer valuesBuffer = new StringBuffer();
                  // TODO support non-string items
                  for ( int z = 0; z < values.length; z++ ) {
                    if ( z == 0 ) {
                      valuesBuffer.append( values[z].toString() );
                    } else {
                      valuesBuffer.append( ',' ).append( values[z].toString() );
                    }
                  }
                  reportProperties.put( element.getName(), valuesBuffer.toString() );
                } else {
                  reportProperties.put( element.getName(), paramValue );
                }
              }

              final DataFactory dataFactory = new PentahoDataFactory( loader );
              final TableModel model =
                  dataFactory.queryData( jFreeReportAction.getDataJar().getDataClass(), new ParameterDataRow(
                      reportProperties ) );

              factory = new PentahoTableDataFactory( AbstractJFreeReportComponent.DATACOMPONENT_DEFAULTINPUT, model );
            }
          }
        } catch ( Exception e ) {
          throw new Exception( Messages.getInstance().getErrorString( "JFreeReport.ERROR_0009_REPORT_JAR_UNREADABLE" ) ); //$NON-NLS-1$
        }
      }
    } catch ( FileNotFoundException e1 ) {
      throw new Exception( Messages.getInstance().getErrorString(
        "JFreeReport.ERROR_0010_REPORT_JAR_MISSING", jFreeReportAction //$NON-NLS-1$
        .getDataJar().toString() ) );
    }
    return factory;
  }

  private PentahoTableDataFactory getInputParamDataFactory() {
    PentahoTableDataFactory factory = null;
    JFreeReportAction jFreeReportAction = (JFreeReportAction) getActionDefinition();

    ActionInput reportDataParam = (ActionInput) jFreeReportAction.getData();
    Object dataObject = reportDataParam != null ? reportDataParam.getValue() : null;
    if ( ( dataObject instanceof IPentahoResultSet ) || ( dataObject instanceof TableModel ) ) {
      factory = new PentahoTableDataFactory();
      if ( dataObject instanceof IPentahoResultSet ) {
        IPentahoResultSet resultset = (IPentahoResultSet) dataObject;
        if ( resultset.isScrollable() ) {
          resultset.beforeFirst();
        } else {
          debug( "ResultSet is not scrollable. Copying into memory" ); //$NON-NLS-1$
          IPentahoResultSet memSet = resultset.memoryCopy();
          resultset.close();
          resultset = memSet;
        }
        factory.addTable( AbstractJFreeReportComponent.DATACOMPONENT_DEFAULTINPUT, new PentahoTableModel( resultset ) );
      } else if ( dataObject instanceof TableModel ) {
        factory.addTable( AbstractJFreeReportComponent.DATACOMPONENT_DEFAULTINPUT, (TableModel) dataObject );
      }

      IActionInput[] subreportQueries = jFreeReportAction.getSubreportQueryParams();
      for ( IActionInput element : subreportQueries ) {
        dataObject = element.getValue();
        if ( dataObject instanceof IPreparedComponent ) {
          factory.addPreparedComponent( element.getName(), (IPreparedComponent) dataObject );
        } else if ( dataObject instanceof IPentahoResultSet ) {
          final IPentahoResultSet resultset = (IPentahoResultSet) dataObject;
          resultset.beforeFirst();
          factory.addTable( element.getName(), new PentahoTableModel( resultset ) );
        } else if ( dataObject instanceof TableModel ) {
          factory.addTable( element.getName(), (TableModel) dataObject );
        }
      }
    }
    return factory;
  }

  private String getHostColonPort( final String pentahoBaseURL ) {
    try {
      URL url = new URL( pentahoBaseURL );
      return url.getHost() + ":" + url.getPort(); //$NON-NLS-1$
    } catch ( Exception e ) {
      //ignore
    }
    return pentahoBaseURL;
  }

  private String getBaseServerURL( final String pentahoBaseURL ) {
    try {
      URL url = new URL( pentahoBaseURL );
      return url.getProtocol() + "://" + url.getHost() + ":" + url.getPort(); //$NON-NLS-1$ //$NON-NLS-2$
    } catch ( Exception e ) {
      //ignore
    }
    return pentahoBaseURL;
  }

  private MasterReport parseReport( final IActionSequenceResource resource ) {
    try {
      // define the resource url so that PentahoResourceLoader recognizes the path.
      String resourceUrl =
          PentahoResourceLoader.SOLUTION_SCHEMA_NAME + PentahoResourceLoader.SCHEMA_SEPARATOR + resource.getAddress();

      String fullyQualifiedServerURL = PentahoSystem.getApplicationContext().getFullyQualifiedServerURL();

      HashMap<FactoryParameterKey, Object> helperObjects = new HashMap<FactoryParameterKey, Object>();

      helperObjects.put( new FactoryParameterKey( "pentahoBaseURL" ), fullyQualifiedServerURL ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

      // trim out the server and port
      helperObjects.put( new FactoryParameterKey( "serverBaseURL" ), getBaseServerURL( fullyQualifiedServerURL ) );
      //$NON-NLS-1$

      helperObjects.put(
          new FactoryParameterKey( "solutionRoot" ), PentahoSystem.getApplicationContext().getSolutionPath( "" ) ); //$NON-NLS-1$ //$NON-NLS-2$

      // get the host:port portion only
      helperObjects.put( new FactoryParameterKey( "hostColonPort" ), getHostColonPort( fullyQualifiedServerURL ) ); //$NON-NLS-1$

      // get the requestContextPath
      helperObjects
          .put(
            new FactoryParameterKey( "requestContextPath" ),
            PentahoRequestContextHolder.getRequestContext().getContextPath() ); //$NON-NLS-1$

      Iterator it = getInputNames().iterator();
      while ( it.hasNext() ) {
        try {
          String inputName = (String) it.next();

          // do not store the data as it would always force a cache refresh and it has no bearing
          // on how the report definition would be parsed
          if ( AbstractJFreeReportComponent.DATACOMPONENT_DATAINPUT.equals( inputName ) ) {
            continue;
          }

          String inputValue = getInputStringValue( inputName );
          helperObjects.put( new FactoryParameterKey( inputName ), inputValue );
        } catch ( Exception e ) {
          //ignore
        }
      }

      ResourceManager resourceManager = new ResourceManager();
      resourceManager.registerDefaults();

      ResourceKey contextKey = resourceManager.createKey( resourceUrl, helperObjects );
      ResourceKey key = resourceManager.createKey( resourceUrl, helperObjects );

      return ReportGenerator.getInstance().parseReport( resourceManager, key, contextKey );

    } catch ( Exception ex ) {
      error(
          Messages.getInstance().getErrorString( "JFreeReport.ERROR_0007_COULD_NOT_PARSE", resource.getAddress() ), ex ); //$NON-NLS-1$
      return null;
    }
  }

  public MasterReport getReport() throws Exception {
    JFreeReportAction jFreeReportAction = (JFreeReportAction) getActionDefinition();
    MasterReport report = getReportFromResource();
    if ( report == null ) {
      report = getReportFromInputParam();
      if ( report == null ) {
        report = getReportFromJar();
      }
    }
    if ( ( report != null ) && jFreeReportAction.getCreatePrivateCopy().getBooleanValue( false ) ) {
      report = (MasterReport) report.clone();
    }
    return report;
  }

  private MasterReport getReportFromResource() throws ResourceException, IOException {
    JFreeReportAction jFreeReportAction = (JFreeReportAction) getActionDefinition();
    MasterReport report = null;
    Object reportDefinition = jFreeReportAction.getReportDefinition();
    IActionSequenceResource resource = null;
    if ( reportDefinition instanceof ActionResource ) {
      resource = getResource( ( (ActionResource) reportDefinition ).getName() );
    }
    if ( resource != null ) {
      if ( resource.getSourceType() == IActionResource.XML ) {
        String repDef = resource.getAddress();
        ReportGenerator generator = ReportGenerator.createInstance();

        // add the runtime context so that PentahoResourceData class can get access to the solution repo
        // generator.setObject(PentahoResourceData.PENTAHO_RUNTIME_CONTEXT_KEY, getRuntimeContext());

        // Read the encoding from the XML file - see BISERVER-895
        final String encoding = XmlHelper.getEncoding( repDef, null );
        ByteArrayInputStream inStream = new ByteArrayInputStream( repDef.getBytes( encoding ) );
        InputSource repDefInputSource = new InputSource( inStream );
        repDefInputSource.setEncoding( encoding );
        report = generator.parseReport( repDefInputSource, getDefinedResourceURL( null ) );
      } else {
        report = parseReport( resource );
      }
    }
    return report;
  }

  private MasterReport getReportFromInputParam() throws ResourceException, UnsupportedEncodingException, IOException {
    MasterReport report = null;
    JFreeReportAction jFreeReportAction = (JFreeReportAction) getActionDefinition();

    Object reportDefinition = jFreeReportAction.getReportDefinition();
    if ( reportDefinition instanceof ActionInput ) {
      String repDef = ( (ActionInput) reportDefinition ).getStringValue();
      report = createReport( repDef );
    }

    return report;
  }

  protected MasterReport createReport( final String reportDefinition ) throws ResourceException, IOException {
    ReportGenerator generator = ReportGenerator.createInstance();

    // add the runtime context so that PentahoResourceData class can get access to the solution repo
    // generator.setObject(PentahoResourceData.PENTAHO_RUNTIME_CONTEXT_KEY, getRuntimeContext());

    URL url = null;
    IPentahoRequestContext requestContext = PentahoRequestContextHolder.getRequestContext();
    try {
      url = new URL( requestContext.getContextPath() ); //$NON-NLS-1$ //$NON-NLS-2$ 
    } catch ( Exception e ) {
      // a null URL is ok
      warn( Messages.getInstance().getString( "JFreeReportLoadComponent.WARN_COULD_NOT_CREATE_URL" ) ); //$NON-NLS-1$
    }

    // Read the encoding from the XML file - see BISERVER-895
    final String encoding = XmlHelper.getEncoding( reportDefinition, null );
    ByteArrayInputStream inStream = new ByteArrayInputStream( reportDefinition.getBytes( encoding ) );
    InputSource reportDefinitionInputSource = new InputSource( inStream );
    reportDefinitionInputSource.setEncoding( encoding );
    return generator.parseReport( reportDefinitionInputSource, getDefinedResourceURL( url ) );
  }

  private MasterReport getReportFromJar() throws Exception {
    JFreeReportAction jFreeReportAction = (JFreeReportAction) getActionDefinition();
    MasterReport report;
    org.pentaho.actionsequence.dom.IActionResource reportJar = jFreeReportAction.getReportDefinitionJar().getJar();
    final IActionSequenceResource resource = getResource( reportJar.getName() );
    final ClassLoader loader = ReportUtils.createJarLoader( getSession(), resource );
    if ( loader == null ) {
      throw new Exception( Messages.getInstance().getString(
        "JFreeReportLoadComponent.ERROR_0035_COULD_NOT_CREATE_CLASSLOADER" ) ); //$NON-NLS-1$
    }

    String reportLocation = jFreeReportAction.getReportDefinitionJar().getReportLocation();
    URL resourceUrl = loader.getResource( reportLocation );
    if ( resourceUrl == null ) {
      throw new Exception( Messages.getInstance().getErrorString( "JFreeReport.ERROR_0016_REPORT_RESOURCE_INVALID", //$NON-NLS-1$
          reportLocation, resource.getAddress() ) );
    }

    try {
      ReportGenerator generator = ReportGenerator.getInstance();

      // add the runtime context so that PentahoResourceData class can get access to the solution repo
      // generator.setObject(PentahoResourceData.PENTAHO_RUNTIME_CONTEXT_KEY, getRuntimeContext());

      report = generator.parseReport( resourceUrl, getDefinedResourceURL( resourceUrl ) );
    } catch ( Exception ex ) {
      throw new Exception( Messages.getInstance().getErrorString(
          "JFreeReport.ERROR_0007_COULD_NOT_PARSE", reportLocation ), ex ); //$NON-NLS-1$
    }
    return report;
  }

  private URL getDefinedResourceURL( final URL defaultValue ) {
    if ( isDefinedInput( AbstractJFreeReportComponent.REPORTLOAD_RESURL ) == false ) {
      return defaultValue;
    }

    try {
      final String inputStringValue =
          getInputStringValue( Messages.getInstance().getString( AbstractJFreeReportComponent.REPORTLOAD_RESURL ) );
      return new URL( inputStringValue );
    } catch ( Exception e ) {
      return defaultValue;
    }
  }

  private boolean initReportConfigParameters( final MasterReport report ) {
    JFreeReportAction jFreeReportAction = (JFreeReportAction) getActionDefinition();
    boolean result = true;
    if ( isDefinedInput( AbstractJFreeReportComponent.DATACOMPONENT_REPORTTEMP_OBJINPUT ) ) {
      Object reportConfigParams = jFreeReportAction.getReportConfig().getValue();
      if ( reportConfigParams != null ) {
        if ( reportConfigParams instanceof IPentahoResultSet ) {
          setReportConfigParameters( report, (IPentahoResultSet) reportConfigParams );
        } else if ( reportConfigParams instanceof Map ) {
          setReportConfigParameters( report, (Map) reportConfigParams );
        } else if ( reportConfigParams instanceof JFreeReportAction.StaticReportConfig ) {
          setReportConfigParameters( report, (JFreeReportAction.StaticReportConfig) reportConfigParams );
        } else {
          error( Messages.getInstance().getErrorString(
            "JFreeReport.ERROR_0026_UNKNOWN_REPORT_CONFIGURATION_PARAMETERS" ) ); //$NON-NLS-1$
          result = false;
        }
      }
    }
    return result;
  }

  private void setReportConfigParameters( final MasterReport report,
      final JFreeReportAction.StaticReportConfig reportConfig ) {
    // We have some configuration parameters in the component definition
    for ( int i = 0; i < reportConfig.size(); i++ ) {
      JFreeReportAction.StaticReportConfigItem staticReportConfigItem =
          (JFreeReportAction.StaticReportConfigItem) reportConfig.get( i );
      String parmName = staticReportConfigItem.getName();
      String parmValue = staticReportConfigItem.getValue();
      if ( ( parmName == null ) || ( parmName.length() == 0 ) ) {
        // Ignore configuration settings without name=
        error( Messages.getInstance().getErrorString( "JFreeReport.ERROR_0027_REPORT_CONFIGURATION_PARAMETER_IGNORED" ) ); //$NON-NLS-1$
        continue;
      }
      if ( parmValue != null ) {
        parmValue = parmValue.trim();
        if ( parmValue.length() > 0 ) {
          report.getReportConfiguration().setConfigProperty( parmName, applyInputsToFormat( parmValue ) );
        } else {
          error( Messages.getInstance()
              .getErrorString( "JFreeReport.ERROR_0027_REPORT_CONFIGURATION_PARAMETER_IGNORED" ) ); //$NON-NLS-1$            
        }
      } else {
        error( Messages.getInstance().getErrorString( "JFreeReport.ERROR_0027_REPORT_CONFIGURATION_PARAMETER_IGNORED" ) ); //$NON-NLS-1$          
      }
    }

  }

  private void setReportConfigParameters( final MasterReport report, final Map values ) {
    Map.Entry ent;
    ModifiableConfiguration config = report.getReportConfiguration();
    Iterator it = values.entrySet().iterator();
    while ( it.hasNext() ) {
      ent = (Map.Entry) it.next();
      if ( ( ent.getKey() != null ) && ( ent.getValue() != null ) ) {
        config.setConfigProperty( ent.getKey().toString(), applyInputsToFormat( ent.getValue().toString() ) );
      }
    }
  }

  private void setReportConfigParameters( final MasterReport report, final IPentahoResultSet values ) {
    int rowCount = values.getRowCount();
    int colCount = values.getColumnCount();
    ModifiableConfiguration config = report.getReportConfiguration();
    if ( colCount >= 2 ) {
      IPentahoMetaData md = values.getMetaData();
      int nameIdx = md.getColumnIndex( "name" ); //$NON-NLS-1$
      int valIdx = md.getColumnIndex( "value" ); //$NON-NLS-1$
      if ( nameIdx < 0 ) {
        nameIdx = 0;
      }
      if ( valIdx < 0 ) {
        valIdx = 1;
      }
      for ( int i = 0; i < rowCount; i++ ) {
        Object[] aRow = values.getDataRow( i );
        if ( ( aRow[nameIdx] != null ) && ( aRow[valIdx] != null ) ) {
          config.setConfigProperty( aRow[nameIdx].toString(), applyInputsToFormat( aRow[valIdx].toString() ) );
        }
      }
    } else {
      error( Messages.getInstance().getErrorString( "JFreeReport.ERROR_0025_INVALID_REPORT_CONFIGURATION_PARAMETERS" ) ); //$NON-NLS-1$
    }
  }

  private boolean initReportInputs( final MasterReport report ) throws CloneNotSupportedException {

    JFreeReportAction jFreeReportAction = (JFreeReportAction) getActionDefinition();
    // Get input parameters, and set them as properties in the report
    // object.
    IActionInput[] actionInputs = jFreeReportAction.getInputs();
    for ( IActionInput element : actionInputs ) {
      String paramName = element.getName();
      Object paramValue = element.getValue();
      if ( ( paramValue == null ) || "".equals( paramValue ) ) { //$NON-NLS-1$
        continue;
      }

      // we filter some well-known bad-guys. It is dangerous to have the
      // report-object (the parsed JFreeReport object), the "report-data"
      // (the tablemodel) or the "data" reference copied to the report.
      // also dangerous are result sets and table models.

      if ( paramValue instanceof IPentahoResultSet ) {
        continue;
      }
      if ( paramValue instanceof TableModel ) {
        continue;
      }
      if ( AbstractJFreeReportComponent.DATACOMPONENT_REPORTTEMP_OBJINPUT.equals( paramName ) ) {
        continue;
      }
      if ( AbstractJFreeReportComponent.DATACOMPONENT_REPORTTEMP_DATAINPUT.equals( paramName ) ) {
        continue;
      }
      if ( AbstractJFreeReportComponent.DATACOMPONENT_DATAINPUT.equals( paramName ) ) {
        continue;
      }

      /*
       * WG: Commenting out because this change (SVN: 44880) breaks bi-developers / reporting / subreport.xaction we'll
       * need to revisit this when reving to the 4.0 reporting engine.
       * 
       * final ParameterDefinitionEntry[] parameterDefinitions =
       * report.getParameterDefinition().getParameterDefinitions(); boolean foundParameter = false; for (int j = 0; j <
       * parameterDefinitions.length; j++) { final ParameterDefinitionEntry definition = parameterDefinitions[j]; if
       * (paramName.equals(definition.getName())) { foundParameter = true; break; } } if (foundParameter == false) { if
       * (report.getParameterDefinition() instanceof ModifiableReportParameterDefinition) { final
       * ModifiableReportParameterDefinition parameterDefinition = (ModifiableReportParameterDefinition)
       * report.getParameterDefinition(); parameterDefinition.addParameterDefinition(new PlainParameter(paramName)); } }
       */
      if ( paramValue instanceof Object[] ) {
        Object[] values = (Object[]) paramValue;
        StringBuffer valuesBuffer = new StringBuffer();
        // TODO support non-string items
        for ( int j = 0; j < values.length; j++ ) {
          if ( j == 0 ) {
            valuesBuffer.append( values[j].toString() );
          } else {
            valuesBuffer.append( ',' ).append( values[j].toString() );
          }
        }
        report.getParameterValues().put( paramName, valuesBuffer.toString() );
        // report.setProperty(paramName, valuesBuffer.toString());
      } else {
        report.getParameterValues().put( paramName, paramValue );
        // report.setProperty(paramName, paramValue);
      }
    }
    return true;
  }

  private String getMimeType( final String outputFormat ) {
    String mimeType = null;
    if ( AbstractJFreeReportComponent.REPORTALLCONTENT_OUTPUTTYPE_HTML.equals( outputFormat ) ) {
      mimeType = "text/html"; //$NON-NLS-1$
    } else if ( AbstractJFreeReportComponent.REPORTALLCONTENT_OUTPUTTYPE_PDF.equals( outputFormat ) ) {
      mimeType = "application/pdf"; //$NON-NLS-1$
    } else if ( AbstractJFreeReportComponent.REPORTALLCONTENT_OUTPUTTYPE_XLS.equals( outputFormat ) ) {
      mimeType = "application/vnd.ms-excel"; //$NON-NLS-1$
    } else if ( AbstractJFreeReportComponent.REPORTALLCONTENT_OUTPUTTYPE_CSV.equals( outputFormat ) ) {
      mimeType = "text/csv"; //$NON-NLS-1$
    } else if ( AbstractJFreeReportComponent.REPORTALLCONTENT_OUTPUTTYPE_RTF.equals( outputFormat ) ) {
      mimeType = "application/rtf"; //$NON-NLS-1$
    } else if ( AbstractJFreeReportComponent.REPORTALLCONTENT_OUTPUTTYPE_XML.equals( outputFormat ) ) {
      mimeType = "text/xml"; //$NON-NLS-1$
    }
    return mimeType;
  }

  private String getFileExtension( final String outputFormat ) {
    String fileExtension = null;
    if ( AbstractJFreeReportComponent.REPORTALLCONTENT_OUTPUTTYPE_HTML.equals( outputFormat ) ) {
      fileExtension = ".html"; //$NON-NLS-1$
    } else if ( AbstractJFreeReportComponent.REPORTALLCONTENT_OUTPUTTYPE_PDF.equals( outputFormat ) ) {
      fileExtension = ".pdf"; //$NON-NLS-1$
    } else if ( AbstractJFreeReportComponent.REPORTALLCONTENT_OUTPUTTYPE_XLS.equals( outputFormat ) ) {
      fileExtension = ".xls"; //$NON-NLS-1$
    } else if ( AbstractJFreeReportComponent.REPORTALLCONTENT_OUTPUTTYPE_CSV.equals( outputFormat ) ) {
      fileExtension = ".csv"; //$NON-NLS-1$
    } else if ( AbstractJFreeReportComponent.REPORTALLCONTENT_OUTPUTTYPE_RTF.equals( outputFormat ) ) {
      fileExtension = ".rtf"; //$NON-NLS-1$
    } else if ( AbstractJFreeReportComponent.REPORTALLCONTENT_OUTPUTTYPE_XML.equals( outputFormat ) ) {
      fileExtension = ".xml"; //$NON-NLS-1$
    }
    return fileExtension;
  }

  private boolean generateReport( final MasterReport report,
                                  final PentahoTableDataFactory factory ) throws IOException {
    JFreeReportAction jFreeReportAction = (JFreeReportAction) getActionDefinition();
    boolean result = false;
    try {
      applyThreadPriority();

      // this might be invalid in case the action is contained in a sub-directory.
      final String baseName = IOUtils.getInstance().stripFileExtension( getActionName() );
      final String path = getSolutionPath();
      final PentahoResourceBundleFactory bundleFactory =
          new PentahoResourceBundleFactory( path, baseName, getSession() );
      report.setResourceBundleFactory( bundleFactory );
      // set the default resourcebundle. This allows users to override the
      // resource-bundle in case they want to keep common strings in a common
      // collection.
      report.getReportConfiguration().setConfigProperty( ResourceBundleFactory.DEFAULT_RESOURCE_BUNDLE_CONFIG_KEY,
          baseName );

      if ( factory != null ) {
        report.setDataFactory( factory );
      }

      String printerName = jFreeReportAction.getPrinterName().getStringValue();
      String outputFormat = jFreeReportAction.getOutputType().getStringValue();

      if ( printerName != null ) {
        result = print( report, getActionTitle(), printerName );
      } else if ( AbstractJFreeReportComponent.REPORTALLCONTENT_OUTPUTTYPE_SWING.equals( outputFormat ) ) {
        if ( GraphicsEnvironment.isHeadless() ) {
          result = writeSwingPreview( report );
        }
        warn( Messages.getInstance().getString( "JFreeReportAllContentComponent.WARN_HEADLESSMODE_ACTIVE" ) ); //$NON-NLS-1$
      } else if ( AbstractJFreeReportComponent.REPORTALLCONTENT_OUTPUTTYPE_HTML.equals( outputFormat )
          || AbstractJFreeReportComponent.REPORTALLCONTENT_OUTPUTTYPE_PDF.equals( outputFormat )
          || AbstractJFreeReportComponent.REPORTALLCONTENT_OUTPUTTYPE_XLS.equals( outputFormat )
          || AbstractJFreeReportComponent.REPORTALLCONTENT_OUTPUTTYPE_CSV.equals( outputFormat )
          || AbstractJFreeReportComponent.REPORTALLCONTENT_OUTPUTTYPE_RTF.equals( outputFormat )
          || AbstractJFreeReportComponent.REPORTALLCONTENT_OUTPUTTYPE_XML.equals( outputFormat ) ) {
        String mimeType = getMimeType( outputFormat );
        String fileExtention = getFileExtension( outputFormat );
        IContentItem contentItem = getContentItem( mimeType, fileExtention );
        OutputStream outputStream = null;
        if ( contentItem != null ) {
          outputStream = contentItem.getOutputStream( getActionName() );
        } else {
          outputStream = getDefaultOutputStream( mimeType );
        }
        result =
            writeReport( outputFormat, report, outputStream, jFreeReportAction.getReportGenerationYieldRate()
                .getIntValue( 0 ), jFreeReportAction.getHtmlContentHandlerUrlPattern().getStringValue() );
        if ( contentItem != null ) {
          contentItem.closeOutputStream();
        }
      } else {
        warn( Messages.getInstance().getString( "JFreeReportAllContentComponent.WARN_NO_PRINTER_GIVEN" ) ); //$NON-NLS-1$
      }
    } finally {
      if ( factory != null ) {
        // force close the factory
        factory.closeTables();
      }
    }
    return result;
  }

  private void applyThreadPriority() {
    String priority = ( (JFreeReportAction) getActionDefinition() ).getReportGenerationPriority().getStringValue();
    try {
      if ( AbstractJFreeReportComponent.REPORTGENERATE_PRIORITYLOWER.equals( priority ) ) {
        Thread.currentThread().setPriority( Math.max( Thread.currentThread().getPriority() - 1, 1 ) );
      } else if ( AbstractJFreeReportComponent.REPORTGENERATE_PRIORITYLOWEST.equals( priority ) ) {
        Thread.currentThread().setPriority( 1 );
      }
    } catch ( Exception e ) {
      // Non fatal exception.
      warn( Messages.getInstance().getString(
          "AbstractGenerateContentComponent.JFreeReport.ERROR_0044_UNABLE_T0_SET_THREAD_PRIORITY" ) ); //$NON-NLS-1$
    }
  }

  protected IContentItem getContentItem( final String mimeType, final String extension ) {
    JFreeReportAction jFreeReportAction = (JFreeReportAction) getActionDefinition();
    // Try to get the output from the action-sequence document.
    IContentItem contentItem = null;
    IActionOutput actionOutput = jFreeReportAction.getOutputReport();
    if ( actionOutput != null ) {
      contentItem = getOutputItem( actionOutput.getName(), mimeType, extension );
      contentItem.setMimeType( mimeType );
    } else if ( getOutputNames().size() == 1 ) {
      String outputName = (String) getOutputNames().iterator().next();
      contentItem = getOutputContentItem( outputName, mimeType );
      contentItem.setMimeType( mimeType );
    }
    return contentItem;
  }

  private boolean writeReport( final String outputFormat, final MasterReport report, final OutputStream outputStream,
      final int yieldRate, final String htmlContentHandlerUrlPattern ) {
    boolean result = false;
    if ( AbstractJFreeReportComponent.REPORTALLCONTENT_OUTPUTTYPE_HTML.equals( outputFormat ) ) {
      result = writeHtml( report, outputStream, yieldRate, htmlContentHandlerUrlPattern );
    } else if ( AbstractJFreeReportComponent.REPORTALLCONTENT_OUTPUTTYPE_PDF.equals( outputFormat ) ) {
      result = writePdf( report, outputStream, yieldRate );
    } else if ( AbstractJFreeReportComponent.REPORTALLCONTENT_OUTPUTTYPE_XLS.equals( outputFormat ) ) {
      result = writeXls( report, outputStream, yieldRate );
    } else if ( AbstractJFreeReportComponent.REPORTALLCONTENT_OUTPUTTYPE_CSV.equals( outputFormat ) ) {
      result = writeCsv( report, outputStream, yieldRate );
    } else if ( AbstractJFreeReportComponent.REPORTALLCONTENT_OUTPUTTYPE_RTF.equals( outputFormat ) ) {
      result = writeRtf( report, outputStream, yieldRate );
    } else if ( AbstractJFreeReportComponent.REPORTALLCONTENT_OUTPUTTYPE_XML.equals( outputFormat ) ) {
      result = writeXml( report, outputStream );
    }
    return result;
  }

  public boolean writeHtml( final MasterReport report, final OutputStream outputStream, final int yieldRate,
      String htmlContentHandlerUrlPattern ) {
    try {

      if ( htmlContentHandlerUrlPattern == null ) {
        final Configuration globalConfig = ClassicEngineBoot.getInstance().getGlobalConfig();
        htmlContentHandlerUrlPattern = PentahoRequestContextHolder.getRequestContext().getContextPath();
        htmlContentHandlerUrlPattern += globalConfig.getConfigProperty( "org.pentaho.web.ContentHandler" ); //$NON-NLS-1$
      }

      final IApplicationContext ctx = PentahoSystem.getApplicationContext();

      final URLRewriter rewriter;
      final ContentLocation dataLocation;
      final NameGenerator dataNameGenerator;
      if ( ctx != null ) {
        File dataDirectory = new File( ctx.getFileOutputPath( "system/tmp/" ) ); //$NON-NLS-1$
        if ( dataDirectory.exists() && ( dataDirectory.isDirectory() == false ) ) {
          dataDirectory = dataDirectory.getParentFile();
          if ( dataDirectory.isDirectory() == false ) {
            throw new ReportProcessingException( Messages.getInstance().getErrorString(
                "JFreeReportDirectoryComponent.ERROR_0001_INVALID_DIR", dataDirectory.getPath() ) ); //$NON-NLS-1$
          }
        } else if ( dataDirectory.exists() == false ) {
          dataDirectory.mkdirs();
        }

        final FileRepository dataRepository = new FileRepository( dataDirectory );
        dataLocation = dataRepository.getRoot();
        dataNameGenerator = new DefaultNameGenerator( dataLocation );
        rewriter = new PentahoURLRewriter( htmlContentHandlerUrlPattern );
      } else {
        dataLocation = null;
        dataNameGenerator = null;
        rewriter = new PentahoURLRewriter( htmlContentHandlerUrlPattern );
      }

      final StreamRepository targetRepository = new StreamRepository( null, outputStream );
      final ContentLocation targetRoot = targetRepository.getRoot();

      final HtmlOutputProcessor outputProcessor = new StreamHtmlOutputProcessor( report.getConfiguration() );
      final HtmlPrinter printer = new AllItemsHtmlPrinter( report.getResourceManager() );
      printer.setContentWriter( targetRoot, new DefaultNameGenerator( targetRoot, "index", "html" ) ); //$NON-NLS-1$//$NON-NLS-2$
      printer.setDataWriter( dataLocation, dataNameGenerator );
      printer.setUrlRewriter( rewriter );
      outputProcessor.setPrinter( printer );

      final StreamReportProcessor sp = new StreamReportProcessor( report, outputProcessor );
      if ( yieldRate > 0 ) {
        sp.addReportProgressListener( new YieldReportListener( yieldRate ) );
      }
      sp.processReport();
      sp.close();

      outputStream.flush();
      return true;
    } catch ( ReportProcessingException e ) {
      error( Messages.getInstance().getString( "JFreeReportHtmlComponent.ERROR_0046_FAILED_TO_PROCESS_REPORT" ), e ); //$NON-NLS-1$
      return false;
    } catch ( IOException e ) {
      error( Messages.getInstance().getString( "JFreeReportHtmlComponent.ERROR_0046_FAILED_TO_PROCESS_REPORT" ), e ); //$NON-NLS-1$
      return false;
    } catch ( ContentIOException e ) {
      error( Messages.getInstance().getString( "JFreeReportHtmlComponent.ERROR_0046_FAILED_TO_PROCESS_REPORT" ), e ); //$NON-NLS-1$
      return false;
    }
  }

  protected boolean writeXls( final MasterReport report, final OutputStream outputStream, final int yieldRate ) {
    boolean result = false;
    try {
      final FlowExcelOutputProcessor target =
          new FlowExcelOutputProcessor( report.getConfiguration(), outputStream, report.getResourceManager() );
      final FlowReportProcessor reportProcessor = new FlowReportProcessor( report, target );

      if ( isDefinedInput( AbstractJFreeReportComponent.WORKBOOK_PARAM ) ) {
        target.setTemplateInputStream( getInputStream( AbstractJFreeReportComponent.WORKBOOK_PARAM ) );
      }
      if ( yieldRate > 0 ) {
        reportProcessor.addReportProgressListener( new YieldReportListener( yieldRate ) );
      }
      reportProcessor.processReport();
      reportProcessor.close();
      outputStream.flush();
      result = true;
    } catch ( Exception e ) {
      error( Messages.getInstance().getString( "JFreeReportExcelComponent.ERROR_0037_ERROR_READING_REPORT_INPUT" ), e ); //$NON-NLS-1$
    }
    return result;
  }

  protected boolean writePdf( final MasterReport report, final OutputStream outputStream, final int yieldRate ) {
    PageableReportProcessor proc = null;
    boolean result = false;
    try {

      final PdfOutputProcessor outputProcessor = new PdfOutputProcessor( report.getConfiguration(), outputStream );
      proc = new PageableReportProcessor( report, outputProcessor );
      if ( yieldRate > 0 ) {
        proc.addReportProgressListener( new YieldReportListener( yieldRate ) );
      }
      proc.processReport();
      proc.close();
      proc = null;
      result = true;
    } catch ( Exception e ) {
      error( Messages.getInstance().getErrorString( "JFreeReportPdfComponent.ERROR_0001_WRITING_PDF_FAILED", //$NON-NLS-1$
          e.getLocalizedMessage() ), e );
    } finally {
      if ( proc != null ) {
        proc.close();
      }
    }
    return result;
  }

  public boolean print( final MasterReport report, final String jobName, final String printerName ) {
    boolean result = false;
    if ( jobName != null ) {
      report.getReportConfiguration().setConfigProperty( PrintUtil.PRINTER_JOB_NAME_KEY, String.valueOf( jobName ) );
    }

    PrintService printer = null;
    PrintService[] services = PrintServiceLookup.lookupPrintServices( DocFlavor.SERVICE_FORMATTED.PAGEABLE, null );
    for ( final PrintService service : services ) {
      if ( service.getName().equals( printerName ) ) {
        printer = service;
      }
    }
    if ( ( printer == null ) && ( services.length > 0 ) ) {
      printer = services[0];
    }

    try {
      Java14PrintUtil.printDirectly( report, printer );
      result = true;
    } catch ( PrintException e ) {
      //ignore
    } catch ( ReportProcessingException e ) {
      //ignore
    }
    return result;
  }

  protected boolean writeCsv( final MasterReport report, final OutputStream outputStream, final int yieldRate ) {
    boolean result = false;
    try {
      final StreamCSVOutputProcessor target = new StreamCSVOutputProcessor( outputStream );
      final StreamReportProcessor reportProcessor = new StreamReportProcessor( report, target );
      if ( yieldRate > 0 ) {
        reportProcessor.addReportProgressListener( new YieldReportListener( yieldRate ) );
      }
      reportProcessor.processReport();
      reportProcessor.close();
      outputStream.flush();
      result = true;
    } catch ( ReportProcessingException e ) {
      //ignore
    } catch ( IOException e ) {
      //ignore
    }
    return result;
  }

  protected boolean writeRtf( final MasterReport report, final OutputStream outputStream, final int yieldRate ) {
    boolean result = false;
    try {
      final StreamRTFOutputProcessor target =
          new StreamRTFOutputProcessor( report.getConfiguration(), outputStream, report.getResourceManager() );
      final StreamReportProcessor proc = new StreamReportProcessor( report, target );
      if ( yieldRate > 0 ) {
        proc.addReportProgressListener( new YieldReportListener( yieldRate ) );
      }
      proc.processReport();
      proc.close();
      outputStream.close();
      result = true;
    } catch ( ReportProcessingException e ) {
      //ignore
    } catch ( IOException e ) {
      //ignore
    }
    return result;
  }

  protected boolean writeXml( final MasterReport report, final OutputStream outputStream ) {
    boolean result = false;
    try {
      final XMLProcessor processor = new XMLProcessor( report );
      final OutputStreamWriter writer = new OutputStreamWriter( outputStream );
      processor.setWriter( writer );
      processor.processReport();

      writer.close();
      result = true;
    } catch ( ReportProcessingException e ) {
      error( Messages.getInstance().getString( "JFreeReportXmlComponent.ERROR_0046_FAILED_TO_PROCESS_REPORT" ), e ); //$NON-NLS-1$
    } catch ( IOException e ) {
      error( Messages.getInstance().getString( "JFreeReportXmlComponent.ERROR_0046_FAILED_TO_PROCESS_REPORT" ), e ); //$NON-NLS-1$
    }
    return result;
  }

  protected boolean writeSwingPreview( final MasterReport report ) {
    final ModifiableConfiguration reportConfiguration = report.getReportConfiguration();

    final boolean progressBar =
        getInputBooleanValue( AbstractJFreeReportComponent.REPORTSWING_PROGRESSBAR,
            "true".equals( reportConfiguration.getConfigProperty( JFreeReportComponent.PROGRESS_BAR_ENABLED_KEY ) ) ); //$NON-NLS-1$
    final boolean progressDialog =
        getInputBooleanValue( AbstractJFreeReportComponent.REPORTSWING_PROGRESSDIALOG,
            "true".equals( reportConfiguration.getConfigProperty( JFreeReportComponent.PROGRESS_DIALOG_ENABLED_KEY ) ) ); //$NON-NLS-1$
    reportConfiguration.setConfigProperty( JFreeReportComponent.PROGRESS_DIALOG_ENABLED_KEY, String
        .valueOf( progressDialog ) );
    reportConfiguration
        .setConfigProperty( JFreeReportComponent.PROGRESS_BAR_ENABLED_KEY, String.valueOf( progressBar ) );

    final PreviewDialog dialog = createDialog( report );
    final ReportController reportController = getReportController();
    if ( reportController != null ) {
      dialog.setReportController( reportController );
    }
    dialog.pack();
    if ( dialog.getParent() != null ) {
      RefineryUtilities.centerDialogInParent( dialog );
    } else {
      RefineryUtilities.centerFrameOnScreen( dialog );
    }

    dialog.setVisible( true );
    return true;
  }

  private ReportController getReportController() {
    if ( isDefinedInput( AbstractJFreeReportComponent.REPORTSWING_REPORTCONTROLLER ) ) {
      final Object controller = getInputValue( AbstractJFreeReportComponent.REPORTSWING_REPORTCONTROLLER );
      if ( controller instanceof ReportController ) {
        return (ReportController) controller;
      }
    }
    return null;
  }

  private PreviewDialog createDialog( final MasterReport report ) {
    final boolean modal = getInputBooleanValue( AbstractJFreeReportComponent.REPORTSWING_MODAL, true );

    if ( isDefinedInput( AbstractJFreeReportComponent.REPORTSWING_PARENTDIALOG ) ) {
      final Object parent = getInputValue( AbstractJFreeReportComponent.REPORTSWING_PARENTDIALOG );
      if ( parent instanceof Dialog ) {
        return new PreviewDialog( report, (Dialog) parent, modal );
      } else if ( parent instanceof Frame ) {
        return new PreviewDialog( report, (Frame) parent, modal );
      }
    }

    final PreviewDialog previewDialog = new PreviewDialog( report );
    previewDialog.setModal( modal );
    return previewDialog;
  }
}
