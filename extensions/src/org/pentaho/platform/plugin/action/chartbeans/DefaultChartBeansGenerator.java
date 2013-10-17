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

package org.pentaho.platform.plugin.action.chartbeans;

import org.apache.commons.io.IOUtils;
import org.pentaho.actionsequence.dom.ActionSequenceDocument;
import org.pentaho.actionsequence.dom.IActionSequenceInput;
import org.pentaho.actionsequence.dom.IActionSequenceInputSource;
import org.pentaho.actionsequence.dom.IActionSequenceOutput;
import org.pentaho.actionsequence.dom.actions.ActionDefinition;
import org.pentaho.actionsequence.dom.actions.MQLAction;
import org.pentaho.chart.model.ChartDataDefinition;
import org.pentaho.chart.model.ChartModel;
import org.pentaho.chart.model.util.ChartSerializer;
import org.pentaho.chart.model.util.ChartSerializer.ChartSerializationFormat;
import org.pentaho.chart.plugin.jfreechart.JFreeChartPlugin;
import org.pentaho.chart.plugin.openflashchart.OpenFlashChartPlugin;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoRequestContext;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.config.messages.Messages;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.runtime.TemplateUtil;
import org.pentaho.platform.plugin.action.pentahometadata.ActionDefinitionEncoder;
import org.pentaho.platform.util.UUIDUtil;
import org.pentaho.platform.util.web.SimpleUrlFactory;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import static org.pentaho.actionsequence.dom.IActionSequenceDocument.*;

public class DefaultChartBeansGenerator implements IChartBeansGenerator {

  private Random random = new Random();
  private static final String DEFAULT_HTML_TEMPLATE =
      "<html><head><title>Command: doChart</title>{0}</head><body style='margin:0;' scroll='no'>{1}</body></html>";
  //$NON-NLS-1$

  private static final String JFREE_CHART_HTML_TEMPLATE =
      "<html><head><title>Pentaho Chart</title></head><body style='margin:0;' scroll='no'>{imageMap}<img border=\"0\" "
          + "width=\"{chartWidth}\" height=\"{chartHeight}\" src=\"{contextPath}getImage?image={imageFile}\" "
          + "{useMap}/></body></html>";
  //$NON-NLS-1$

  private static String DEFAULT_flashScriptFragment =
      "<input type=\"hidden\"/><script defer='defer' type=\"text/javascript\">function {dataFunction}() { return "
          + "/*JSON*/\"{chartJson}\"/*END_JSON*/;}</script>";
  //$NON-NLS-1$

  private static String DEFAULT_flashObjectFragment = "<object classid=\"clsid:D27CDB6E-AE6D-11cf-96B8-444553540000\" " //$NON-NLS-1$
      + "codebase=\"http://fpdownload.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=8,0,0,0\" "
      //$NON-NLS-1$
      + "width=\"{chart-width}\" height=\"{chart-height}\"  id=\"ofco{chartId}\" align=\"middle\"> " //$NON-NLS-1$
      + "<param name=\"allowScriptAccess\" value=\"sameDomain\" /> " //$NON-NLS-1$
      + "<param name=\"wmode\" value=\"opaque\">" //$NON-NLS-1$
      + "<param name=\"movie\" value=\"{ofc-url}?get-data={dataFunction}\" /> " //$NON-NLS-1$
      + "<param name=\"quality\" value=\"high\" /> " //$NON-NLS-1$
      + "<embed src=\"{ofc-url}?get-data={dataFunction}\" wmode=\"opaque\" quality=\"high\" bgcolor=\"#FFFFFF\" "
      //$NON-NLS-1$
      + "width=\"{chart-width}\" height=\"{chart-height}\"  id=\"ofce{chartId}\" align=\"middle\" "
      + "allowScriptAccess=\"sameDomain\" type=\"application/x-shockwave-flash\" "
      //$NON-NLS-1$
      + "pluginspage=\"http://www.macromedia.com/go/getflashplayer\" /></object>"; //$NON-NLS-1$

  public DefaultChartBeansGenerator() {

  }

  /**
   * The engine that processes the parameters from the specific interface methods and writes a chart to the output
   * stream or returns an input stream for reading.
   * 
   * @param pentahoSession
   * @param serializedChartDataDefinition
   * @param serializedChartModel
   * @param chartWidth
   * @param chartHeight
   * @param outputStream
   * @return
   * @throws IOException
   */

  private InputStream internalCreateChart( IPentahoSession pentahoSession, Map<String, Object> parameterMap,
      String serializedChartDataDefinition, String serializedChartModel, int chartWidth, int chartHeight,
      String contentLinkingTemplate, OutputStream outputStream ) throws IOException {
    InputStream result = null;
    ByteArrayOutputStream resultOutputStream = null;
    OutputStream out = null;

    // Make code more readable by defining the output result
    boolean returnInputStream = outputStream == null ? true : false;

    // If the caller sends a null OutputStream, then we will return an InputStream
    if ( returnInputStream ) {
      resultOutputStream = new ByteArrayOutputStream();
      out = new BufferedOutputStream( resultOutputStream );
    } else {
      out = outputStream;
    }

    // Setup parameters to be passed to the xaction
    Map<String, Object> params = new HashMap<String, Object>();

    params.put( "chart-model-json", serializedChartModel ); //$NON-NLS-1$
    params.put( "chart-width", chartWidth ); //$NON-NLS-1$
    params.put( "chart-height", chartHeight ); //$NON-NLS-1$

    // De-serialize the chartDataDefintion and extract relevant parts
    ChartDataDefinition chartDataDefinition =
        ChartSerializer.deSerializeDataDefinition( serializedChartDataDefinition, ChartSerializationFormat.JSON );

    if ( chartDataDefinition.getQuery() != null ) {
      params.put( "query", chartDataDefinition.getQuery() ); //$NON-NLS-1$
    }

    if ( chartDataDefinition.getDomainColumn() != null ) {
      params.put( "series-column", chartDataDefinition.getDomainColumn() ); //$NON-NLS-1$
    }

    if ( chartDataDefinition.getCategoryColumn() != null
      && !chartDataDefinition.getCategoryColumn().equals( "None" ) ) {
      params.put( "category-column", chartDataDefinition.getCategoryColumn() ); //$NON-NLS-1$
    }

    if ( chartDataDefinition.getRangeColumn() != null ) {
      params.put( "value-column", chartDataDefinition.getRangeColumn() ); //$NON-NLS-1$
    }

    if ( chartDataDefinition.getScalingFactor() != null ) {
      params.put( "scaling-factor", chartDataDefinition.getScalingFactor().toString() ); //$NON-NLS-1$
    }

    createAndRunActionSequence( pentahoSession, params, parameterMap, contentLinkingTemplate, out );

    if ( out instanceof BufferedOutputStream ) {
      out.flush();
    }

    if ( returnInputStream ) {
      result = new ByteArrayInputStream( resultOutputStream.toByteArray() );

      return ( result );
    }

    return null;
  }

  protected void createAndRunActionSequence( final IPentahoSession pentahoSession, final Map<String, Object> params,
      final Map<String, Object> defaultParameterMap, String contentLinkingTemplate, final OutputStream out ) {

    SimpleParameterProvider parameterProvider = new SimpleParameterProvider( params );

    // add the default parameter values
    for ( Map.Entry<String, Object> entry : defaultParameterMap.entrySet() ) {
      parameterProvider.setParameter( entry.getKey(), entry.getValue() );
    }

    Map<String, IParameterProvider> parameterProviders = new HashMap<String, IParameterProvider>();
    parameterProviders.put( IParameterProvider.SCOPE_REQUEST, parameterProvider );

    SimpleOutputHandler outputHandler = new SimpleOutputHandler( out, true );
    outputHandler.setOutputPreference( IOutputHandler.OUTPUT_TYPE_DEFAULT );

    ActionSequenceDocument doc = createActionSequenceDocument( defaultParameterMap.keySet(), contentLinkingTemplate );
    runActionSequence( pentahoSession, parameterProviders, outputHandler, doc );

    try {
      out.flush();
      out.close();
    } catch ( IOException e ) {
      e.printStackTrace();
    }
  }

  /**
   * Executes an action sequence from an <code>ActionSequenceDocument</code>.
   * 
   * @param pentahoSession
   *          current <code>IPentahoSession</code>
   * @param parameterProviders
   *          map of parameter providers; there should a single entry with "request" as the key
   * @param outputHandler
   *          output handler
   * @param doc
   *          action sequence document
   * @throws RuntimeException
   *           if anything goes wrong
   */
  protected void runActionSequence( final IPentahoSession pentahoSession,
      final Map<String, IParameterProvider> parameterProviders, final IOutputHandler outputHandler,
      final ActionSequenceDocument doc ) throws RuntimeException {

    // Get the solution engine
    ISolutionEngine solutionEngine = PentahoSystem.get( ISolutionEngine.class, pentahoSession );
    if ( solutionEngine == null ) {
      throw new RuntimeException( "solutionEngine is null" ); //$NON-NLS-1$
    }
    solutionEngine.setLoggingLevel( ILogger.DEBUG );
    solutionEngine.init( pentahoSession );

    IPentahoRequestContext requestContext = PentahoRequestContextHolder.getRequestContext();
    String contextPath = requestContext.getContextPath();

    IPentahoUrlFactory urlFactory = new SimpleUrlFactory( contextPath ); //$NON-NLS-1$

    IRuntimeContext runtime;
    IParameterProvider requestParmProvider = parameterProviders.get( "request" );
    if ( requestParmProvider.hasParameter( "obj_id" ) ) {

      final String obj_id = (String) requestParmProvider.getParameter( "obj_id" );
      final String msg_name = (String) requestParmProvider.getParameter( "message_name" );
      final String job_id = (String) requestParmProvider.getParameter( "job_id" );

      runtime = solutionEngine.execute( doc.toString(), obj_id, job_id, false, true, //$NON-NLS-1$ //$NON-NLS-2$
          msg_name, true, parameterProviders, outputHandler, null, urlFactory, new ArrayList() ); //$NON-NLS-1$
    } else {
      runtime = solutionEngine.execute( doc.toString(), "chartbeans_mql", "myprocessid", false, true, //$NON-NLS-1$ //$NON-NLS-2$
          "myinstanceid", true, parameterProviders, outputHandler, null, urlFactory, new ArrayList() ); //$NON-NLS-1$
    }

    if ( ( runtime != null ) && ( runtime.getStatus() != IRuntimeContext.RUNTIME_STATUS_SUCCESS ) ) {
      StringBuilder buf = new StringBuilder();
      boolean firstIteration = true;
      for ( Object /* String */message : runtime.getMessages() ) {

        if ( message instanceof Exception ) {
          Exception ex = (Exception) message;
          if ( ex.getCause() instanceof RuntimeException ) {
            throw (RuntimeException) ex.getCause();
          }
        }

        if ( !firstIteration ) {
          buf.append( " \\\\ " ); //$NON-NLS-1$
        }
        buf.append( message );
      }
      String errorStr;
      if ( buf.indexOf( "action_sequence_failed" ) > -1 && buf.indexOf( "MQLRelationalDataComponent" ) > -1 ) {
        errorStr = Messages.getInstance().getString( "DefaultChartBeansGenerator.ERROR_0001_SECURITY_ERROR" );
      } else {
        errorStr = Messages.getInstance().getString( "DefaultChartBeansGenerator.ERROR_0002_UNKNOWN_ERROR" );
      }

      throw new RuntimeException( errorStr );
    }

  }

  /**
   * Creates an <code>ActionSequenceDocument</code> that will run an MQL query and pipe the results in the ChartBeans
   * <code>ChartComponent</code>.
   * 
   * @param parameterNameSet
   *          set of parameter names that appear in the MQL query
   * @return doc
   */
  protected ActionSequenceDocument createActionSequenceDocument( final Set<String> parameterNameSet,
      String contentLinkingTemplate ) {
    ActionSequenceDocument actionSequenceDocument = new ActionSequenceDocument();
    actionSequenceDocument.setTitle( "chartbeans_mql.xaction" ); //$NON-NLS-1$
    actionSequenceDocument.setVersion( "1" ); //$NON-NLS-1$
    actionSequenceDocument.setLoggingLevel( "debug" ); //$NON-NLS-1$
    actionSequenceDocument.setAuthor( "Dashboard" ); //$NON-NLS-1$
    actionSequenceDocument.setDescription( "Generate a chart through ChartBeans from an MQL statement." ); //$NON-NLS-1$
    actionSequenceDocument.setHelp( "Pass in an MQL statement that returns a table of three columns. The first column " //$NON-NLS-1$
        + "is the series, the second is the category and the third is the data." ); //$NON-NLS-1$
    actionSequenceDocument.setHelp( "" ); //$NON-NLS-1$
    actionSequenceDocument.setResultType( "rule" ); //$NON-NLS-1$

    IActionSequenceInput queryInput = actionSequenceDocument.createInput( "query", STRING_TYPE ); //$NON-NLS-1$
    IActionSequenceInput chartModelJsonInput = actionSequenceDocument.createInput( "chart-model-json", STRING_TYPE ); //$NON-NLS-1$
    IActionSequenceInput chartWidthInput = actionSequenceDocument.createInput( "chart-width", INTEGER_TYPE ); //$NON-NLS-1$
    chartWidthInput.addSource( REQUEST_INPUT_SOURCE, "chart-width" ); //$NON-NLS-1$
    chartWidthInput.setDefaultValue( "1" ); //$NON-NLS-1$
    IActionSequenceInput chartHeightInput = actionSequenceDocument.createInput( "chart-height", INTEGER_TYPE ); //$NON-NLS-1$
    chartHeightInput.addSource( REQUEST_INPUT_SOURCE, "chart-height" ); //$NON-NLS-1$
    chartHeightInput.setDefaultValue( "1" ); //$NON-NLS-1$
    IActionSequenceInput seriesColumnInput = actionSequenceDocument.createInput( "series-column", STRING_TYPE ); //$NON-NLS-1$
    seriesColumnInput.setDefaultValue( "1" ); //$NON-NLS-1$
    IActionSequenceInput categoryColumnInput = actionSequenceDocument.createInput( "category-column", STRING_TYPE ); //$NON-NLS-1$
    // set a default value of empty string to avoid an error when rendering pie charts (which don't have a category
    // column
    categoryColumnInput.setDefaultValue( "2" ); //$NON-NLS-1$
    IActionSequenceInput valueColumnInput = actionSequenceDocument.createInput( "value-column", STRING_TYPE ); //$NON-NLS-1$
    valueColumnInput.setDefaultValue( "0" ); //$NON-NLS-1$
    IActionSequenceInput scalingFactorInput = actionSequenceDocument.createInput( "scaling-factor", STRING_TYPE ); //$NON-NLS-1$

    // add inputs from parameterNameSet; these parameters will appear as placeholders in the query input
    for ( String parameterName : parameterNameSet ) {
      IActionSequenceInput input =
          actionSequenceDocument.createInput( ActionDefinitionEncoder.encodeBlankSpaces( parameterName ), STRING_TYPE );
      IActionSequenceInputSource[] sources = input.getSources();
      if ( sources.length > 0 ) {
        input.getSources()[0].setName( parameterName );
      } else {
        input.addSource( "request", parameterName );
      }
    }

    IActionSequenceOutput outputStreamOutput = actionSequenceDocument.createOutput( "outputstream", CONTENT_TYPE ); //$NON-NLS-1$
    outputStreamOutput.addDestination( RESPONSE_OUTPUT_DESTINATION, "content" ); //$NON-NLS-1$

    MQLAction mqlAction = (MQLAction) actionSequenceDocument.addAction( MQLAction.class );
    mqlAction.setActionInputValue( "query", queryInput ); //$NON-NLS-1$

    // add inputs from parameterNameSet to this action
    for ( String parameterName : parameterNameSet ) {
      mqlAction.addInput( ActionDefinitionEncoder.encodeBlankSpaces( parameterName ), STRING_TYPE );
    }

    mqlAction.setOutputResultSet( "chartdata" ); //$NON-NLS-1$
    mqlAction.setComponentDefinition( "live", Boolean.TRUE.toString() ); //$NON-NLS-1$
    mqlAction.setComponentDefinition( "display-names", Boolean.FALSE.toString() ); //$NON-NLS-1$

    ActionDefinition pojoAction = (ActionDefinition) actionSequenceDocument.addAction( ActionDefinition.class );
    pojoAction.setComponentName( "ChartBeansComponent" ); //$NON-NLS-1$
    if ( contentLinkingTemplate != null ) {
      pojoAction.setComponentDefinition( "contentLinkingTemplate", contentLinkingTemplate );
    }
    pojoAction.setActionInputValue( "chart-model-json", chartModelJsonInput ); //$NON-NLS-1$
    pojoAction.addInput( "chartdata", RESULTSET_TYPE ); //$NON-NLS-1$
    pojoAction.setActionInputValue( "chart-width", chartWidthInput ); //$NON-NLS-1$
    pojoAction.setActionInputValue( "chart-height", chartHeightInput ); //$NON-NLS-1$
    pojoAction.setActionInputValue( "series-column", seriesColumnInput ); //$NON-NLS-1$
    pojoAction.setActionInputValue( "category-column", categoryColumnInput ); //$NON-NLS-1$
    pojoAction.setActionInputValue( "value-column", valueColumnInput ); //$NON-NLS-1$
    pojoAction.setActionInputValue( "scaling-factor", scalingFactorInput ); //$NON-NLS-1$
    pojoAction.addOutput( "outputstream", CONTENT_TYPE ); //$NON-NLS-1$

    return actionSequenceDocument;
  }

  public String buildEmptyOpenFlashChartHtmlFragment( String msg ) {
    // populate the flash html template
    Properties props = new Properties();
    props.setProperty( "dataFunction", "getChartData" + Integer.toString( random.nextInt( 100000 ) ) ); // + chartId); //$NON-NLS-1$ //$NON-NLS-2$
    props.setProperty( "chartJson", "{}" ); //$NON-NLS-1$ //$NON-NLS-2$

    String flashHtml =
        MessageFormat.format( getHtmlTemplate(), new String[] {
          TemplateUtil.applyTemplate( getFlashScriptFragment(), props, null ), msg } );

    return flashHtml;
  }

  /**
   * Does this method belong in ChartBeansGeneratorUtil? ChartBeansGeneratorUtil may be more of a convenience for
   * executing the default ActionSequence, if this is to hold true, this method probably needs a new home more central
   * to the ChartBeans code. Returns a complete HTML document that references an Open Flash Chart SWF resource that
   * resides on the server along with the data that should be displayed in the chart (via a JavaScript function that
   * returns a JSON string).
   * <p>
   * Only exposed for debugging (i.e. hosted mode) purposes.
   * </p>
   */
  public String buildOpenFlashChartHtmlFragment( String openFlashChartJson, String swfUrl, String chartWidth,
      String chartHeight ) {
    // generate a unique name for the function
    String chartId = UUIDUtil.getUUIDAsString().replaceAll( "[^\\w]", "" ); //$NON-NLS-1$ //$NON-NLS-2$

    // populate the flash html template
    Properties props = new Properties();
    props.setProperty( "chartId", chartId ); //$NON-NLS-1$
    props.setProperty( "dataFunction", "getChartData" + Integer.toString( random.nextInt( 100000 ) ) ); // + chartId); //$NON-NLS-1$ //$NON-NLS-2$
    props.setProperty( "chart-width", chartWidth ); //$NON-NLS-1$
    props.setProperty( "chart-height", chartHeight ); //$NON-NLS-1$
    props.setProperty( "ofc-url", swfUrl ); //$NON-NLS-1$
    props.setProperty( "chartJson", openFlashChartJson ); //$NON-NLS-1$

    String flashHtml =
        MessageFormat.format( getHtmlTemplate(), new String[] {
          TemplateUtil.applyTemplate( getFlashScriptFragment(), props, null ),
          TemplateUtil.applyTemplate( getFlashObjectFragment(), props, null ) } );

    return flashHtml;
  }

  public String buildJFreeChartHtmlFragment( File imageFile, String imageMap, String imageMapName, int chartWidth,
      int chartHeight, String contextPath ) {

    Properties props = new Properties();
    props.setProperty( "imageMap", imageMap != null ? imageMap : "" ); //$NON-NLS-1$ //$NON-NLS-2$
    props.setProperty( "useMap", imageMapName != null ? "usemap=\"#" + imageMapName + "\"" : "" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    props.setProperty( "contextPath", contextPath ); //$NON-NLS-1$
    props.setProperty( "chartWidth", Integer.toString( chartWidth ) ); //$NON-NLS-1$
    props.setProperty( "chartHeight", Integer.toString( chartHeight ) ); //$NON-NLS-1$
    props.setProperty( "imageFile", imageFile.getName() ); //$NON-NLS-1$

    return TemplateUtil.applyTemplate( JFREE_CHART_HTML_TEMPLATE, props, null );
  }

  public String createChartAsHtml( IPentahoSession userSession, Map<String, Object> parameterMap,
      String serializedChartDataDefinition, String serializedChartModel, int chartWidth, int chartHeight,
      String contentLinkingTemplate ) throws IOException {

    ChartModel chartModel = ChartSerializer.deSerialize( serializedChartModel, ChartSerializationFormat.JSON );

    String html = null;

    if ( chartModel.getChartEngineId() == null ) {
      // Load default value from system setting or take hard coded
      // Hard coded final fall back is Open Flash Chart
      String defaultChartEngine =
          PentahoSystem.getSystemSetting( "chartbeans/chartbeans_config.xml", "default-chart-engine",
              OpenFlashChartPlugin.PLUGIN_ID ); //$NON-NLS-1$ //$NON-NLS-2$
      if ( defaultChartEngine == null ) {
        defaultChartEngine = OpenFlashChartPlugin.PLUGIN_ID;
      }
      chartModel.setChartEngineId( defaultChartEngine );
    }
    // Check for render engine override
    String override = (String) parameterMap.get( "renderEngine" );
    if ( override != null ) {
      chartModel.setChartEngineId( override );
    }

    serializedChartModel = ChartSerializer.serialize( chartModel, ChartSerializationFormat.JSON );

    if ( contentLinkingTemplate == null ) { // use old version
      html =
          createChartAsHtml( userSession, parameterMap, serializedChartDataDefinition, serializedChartModel,
              chartWidth, chartHeight );
    } else { // with content linking
      final String ENCODING = "UTF-8"; //$NON-NLS-1$

      InputStream is =
          this.internalCreateChart( userSession, parameterMap, serializedChartDataDefinition, serializedChartModel,
              chartWidth, chartHeight, contentLinkingTemplate, null );
      html = IOUtils.toString( is, ENCODING ); // copy(is, writer);
    }

    return html;
  }

  public String createChartAsHtml( IPentahoSession userSession, Map<String, Object> parameterMap,
      String serializedChartDataDefinition, String serializedChartModel, int chartWidth, int chartHeight )
    throws IOException {

    ChartModel chartModel = ChartSerializer.deSerialize( serializedChartModel, ChartSerializationFormat.JSON );

    String html = null;

    if ( chartModel.getChartEngineId() == null ) {
      // Load default value from system setting or take hard coded

      // Hard coded final fall back is Open Flash Chart
      String defaultChartEngine =
          PentahoSystem.getSystemSetting( "chartbeans/chartbeans_config.xml", "default-chart-engine",
              OpenFlashChartPlugin.PLUGIN_ID ); //$NON-NLS-1$ //$NON-NLS-2$

      if ( defaultChartEngine == null ) {
        defaultChartEngine = OpenFlashChartPlugin.PLUGIN_ID;
      }

      chartModel.setChartEngineId( defaultChartEngine );

    }

    // Check for render engine override
    String override = (String) parameterMap.get( "renderEngine" );
    if ( override != null ) {
      chartModel.setChartEngineId( override );
    }

    serializedChartModel = ChartSerializer.serialize( chartModel, ChartSerializationFormat.JSON );

    if ( JFreeChartPlugin.PLUGIN_ID.equals( chartModel.getChartEngineId() ) ) {
      final String SOLUTION_TMP_DIR = "system/tmp/"; //$NON-NLS-1$
      File chartFileOnServer =
          new File( new File( PentahoSystem.getApplicationContext().getFileOutputPath( SOLUTION_TMP_DIR ) ),
              java.util.UUID.randomUUID().toString() );

      BufferedOutputStream bos = null;
      try {
        bos = new BufferedOutputStream( new FileOutputStream( chartFileOnServer ) );
        this.internalCreateChart( userSession, parameterMap, serializedChartDataDefinition, serializedChartModel,
            chartWidth, chartHeight, null, bos );
      } finally {
        IOUtils.closeQuietly( bos );
      }

      IPentahoRequestContext requestContext = PentahoRequestContextHolder.getRequestContext();
      String contextPath = requestContext.getContextPath();
      String url = contextPath + "/"; //$NON-NLS-1$ //$NON-NLS-2$ 
      final String IMAGE_URL_TEMPLATE = "{0}getImage?image={1}"; //$NON-NLS-1$
      final String imageUrl =
          MessageFormat.format( IMAGE_URL_TEMPLATE, new String[] { url, chartFileOnServer.getName() } );
      html = this.mergeStaticImageHtmlTemplate( imageUrl );

    } else if ( OpenFlashChartPlugin.PLUGIN_ID.equals( chartModel.getChartEngineId() ) ) {

      ByteArrayOutputStream tmpOut = new ByteArrayOutputStream();
      this.internalCreateChart( userSession, parameterMap, serializedChartDataDefinition, serializedChartModel,
          chartWidth, chartHeight, null, tmpOut );
      final String ENCODING = "UTF-8"; //$NON-NLS-1$
      ByteArrayInputStream in = new ByteArrayInputStream( tmpOut.toByteArray() );
      IOUtils.closeQuietly( tmpOut );
      html = IOUtils.toString( in, ENCODING );
      IOUtils.closeQuietly( in );

    } else {
      throw new IllegalArgumentException( "unrecognized chart engine" ); //$NON-NLS-1$
    }

    return html;
  }

  public String mergeOpenFlashChartHtmlTemplate( String openFlashChartJson, String swfUrl ) {
    return buildOpenFlashChartHtmlFragment( openFlashChartJson, swfUrl, "100%", "100%" ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  public String mergeJFreeChartHtmlTemplate( File imageFile, String imageMap, String imageMapName, int chartWidth,
      int chartHeight, String contextPath ) {
    return buildJFreeChartHtmlFragment( imageFile, imageMap, imageMapName, chartWidth, chartHeight, contextPath );
  }

  public String mergeStaticImageHtmlTemplate( String imageUrl ) {
    final String BODY_TEMPLATE = "<img src=\"{0}\" />"; //$NON-NLS-1$
    final String body = MessageFormat.format( BODY_TEMPLATE, new String[] { imageUrl } );
    return MessageFormat.format( getHtmlTemplate(), new String[] { "", body } ); //$NON-NLS-1$
  }

  public String getHtmlTemplate() {
    return DEFAULT_HTML_TEMPLATE;
  }

  public String getFlashScriptFragment() {
    return DEFAULT_flashScriptFragment;
  }

  public String getFlashObjectFragment() {
    return DEFAULT_flashObjectFragment;
  }

}
