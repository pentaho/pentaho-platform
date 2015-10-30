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

package org.pentaho.platform.uifoundation.chart;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Node;
import org.pentaho.commons.connection.DataUtilities;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IMessageFormatter;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoRequestContext;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.ActionSequenceJCRHelper;
import org.pentaho.platform.engine.services.connection.PentahoConnectionFactory;
import org.pentaho.platform.engine.services.runtime.TemplateUtil;
import org.pentaho.platform.uifoundation.messages.Messages;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.web.SimpleUrlFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * This class provides wrapper functions to make it easier to execute action sequences and generate a widget.
 */
public class ChartHelper {

  public static final String OUTER_PARAMS = "outer-params"; //$NON-NLS-1$
  public static final String INNER_PARAM = "inner-param"; //$NON-NLS-1$
  public static final String DRILL_URL = "drill-url"; //$NON-NLS-1$
  public static final String IMAGE_URL = "image-url"; //$NON-NLS-1$
  public static final String VALUE = "value"; //$NON-NLS-1$
  public static final String IMAGE_WIDTH = "image-width"; //$NON-NLS-1$
  public static final String IMAGE_HEIGHT = "image-height"; //$NON-NLS-1$
  public static final String SERIES_NAME = "series-name"; //$NON-NLS-1$
  public static final String TITLE = "title"; //$NON-NLS-1$

  public static final String CONNECTION = "connection"; //$NON-NLS-1$
  public static final String QUERY = "query"; //$NON-NLS-1$
  public static final String DATA_PROCESS = "data-process"; //$NON-NLS-1$

  public static final String CONTENT_TYPE = "text/html"; //$NON-NLS-1$

  public static final String nbsp = "&nbsp;"; //$NON-NLS-1$

  protected enum Error {
    ERROR_0001_IO_PROBLEM_GETTING_CHART_TYPE, //
    ERROR_0002_COULD_NOT_DETERMINE_CHART_TYPE, //
    ERROR_0003_INVALID_CHART_TYPE, //
    ERROR_0001_COULD_NOT_CREATE_WIDGET;
  }

  protected static class LogWriter {

    protected static final String KEY_PREFIX = ChartHelper.class.getSimpleName() + '.';

    private ILogger logger;

    protected IPentahoSession userSession;
    protected List<?> messages;

    private StringBuffer messageBuffer = new StringBuffer();

    public LogWriter( final IPentahoSession userSession, ILogger logger, List<?> messages ) {
      // No logger? The usersession extends ILogger, use it for loggin
      this.logger = logger == null ? userSession : logger;
      this.userSession = userSession;
      this.messages = messages;
    }

    public String logErrorAndGetContent( final Error errorKey, final Object... params ) {
      return logErrorAndGetContent( null, errorKey, params );
    }

    public String logErrorAndGetContent( final Throwable e, final Error errorKey, final Object... params ) {
      String errorStr = logError( e, errorKey, params );
      PentahoSystem.get( IMessageFormatter.class, userSession ).formatErrorMessage( CONTENT_TYPE, errorStr, messages,
          messageBuffer );
      return messageBuffer.toString();
    }

    public String logError( final Throwable e, final Error errorKey, final Object... params ) {
      String errorStr = Messages.getInstance().getString( KEY_PREFIX + errorKey.name(), params );
      if ( e == null ) {
        logger.error( errorStr );
      } else {
        logger.error( errorStr, e );
      }
      return errorStr;
    }

    public ILogger getLogger() {
      return logger;
    }
  }

  protected static class Builder {

    protected LogWriter logWriter;

    protected IParameterProvider parameterProvider;
    protected IPentahoSession userSession;
    protected String actionPath;
    protected List<?> messages;
    protected String chartTypeStr;
    protected String datasetType;
    protected String content;

    public Builder( final String actionPath, final IParameterProvider parameterProvider,
        final IPentahoSession userSession, final ILogger logger, final List<?> messages ) {
      logWriter = new LogWriter( userSession, logger, messages );
      this.parameterProvider = parameterProvider;
      this.userSession = userSession;
      this.actionPath = actionPath;
      this.messages = messages;
    }

    public boolean doChart( final StringBuffer outputStream ) {
      try {
        initChartType();
      } catch ( Exception e ) {
        outputStream.append( logWriter.logErrorAndGetContent( e, Error.ERROR_0001_IO_PROBLEM_GETTING_CHART_TYPE ) );
        return false;
      }
      if ( !StringUtils.isNumeric( chartTypeStr ) ) {
        // we have no idea what to try to generate
        outputStream.append( logWriter.logErrorAndGetContent( Error.ERROR_0002_COULD_NOT_DETERMINE_CHART_TYPE ) );
        return false;
      }

      int chartType = JFreeChartEngine.getChartType( chartTypeStr );
      int width = (int) parameterProvider.getLongParameter( IMAGE_WIDTH, 150 );
      int height = (int) parameterProvider.getLongParameter( IMAGE_HEIGHT, 150 );
      String urlDrillTemplate = parameterProvider.getStringParameter( DRILL_URL, "" );
      SimpleUrlFactory urlFactory = new SimpleUrlFactory( urlDrillTemplate );

      AbstractJFreeChartComponent chartComponent = null;
      try {
        chartComponent = determineChartByDataset( chartType, height, width, urlFactory );

        if ( chartComponent == null ) {
          chartComponent = determineChartByChartType( chartType, height, width, urlFactory );
        }

        if ( chartComponent != null ) {
          try {
            chartComponent.setLoggingLevel( logWriter.getLogger().getLoggingLevel() );
            chartComponent.validate( userSession, null );
            chartComponent.setUrlTemplate( urlDrillTemplate );

            initContent( chartComponent );
            content = chartComponent.getContent( CONTENT_TYPE );
            if ( content == null ) {
              outputStream.append( logWriter.logErrorAndGetContent( Error.ERROR_0001_COULD_NOT_CREATE_WIDGET ) );
              return false;
            }
          } catch ( Throwable e ) {
            logWriter.logError( e, Error.ERROR_0001_COULD_NOT_CREATE_WIDGET ); // TODO why not return false
          }
        }
      } finally {
        if ( chartComponent != null ) {
          chartComponent.dispose();
        }
      }

      return true;
    }

    protected void initChartType() {
      // Determine the type of chart we are building; these values can come from the chart xml definition, or
      // from the parameter provider. Try the parameter provider first, for performance reasons.
      chartTypeStr = parameterProvider.getStringParameter( ChartDefinition.TYPE_NODE_NAME, null );
      if ( StringUtils.isBlank( chartTypeStr ) ) {
        // attempt to get the chart type and possibly data type from the xml doc
        ActionSequenceJCRHelper jcrHelper = new ActionSequenceJCRHelper( userSession );
        Document chartDefinition = jcrHelper.getSolutionDocument( actionPath, RepositoryFilePermission.READ );
        Node chartAttributes = chartDefinition.selectSingleNode( "//" + AbstractChartComponent.CHART_NODE_NAME ); //$NON-NLS-1$
        chartTypeStr = chartAttributes.selectSingleNode( ChartDefinition.TYPE_NODE_NAME ).getText();
        Node datasetTypeNode = chartAttributes.selectSingleNode( ChartDefinition.DATASET_TYPE_NODE_NAME );
        if ( datasetTypeNode != null ) {
          datasetType = datasetTypeNode.getText();
        }
      } else {
        datasetType = ChartDefinition.CATEGORY_DATASET_STR;
      }
    }

    // WARNING!!! This is an atypical way to access data for the chart... these parameters and their
    // usage are undocumented, and only left in here to support older solutions that may be using them.
    protected void initContent( AbstractChartComponent chartComponent ) throws Exception {
      String connectionName = parameterProvider.getStringParameter( CONNECTION, null );
      String queryTemplate = parameterProvider.getStringParameter( QUERY, null );
      if ( StringUtils.isNotBlank( connectionName ) && StringUtils.isNotBlank( queryTemplate ) ) {
        chartComponent.setDataAction( actionPath );
        IPentahoResultSet values = getValues( connectionName, queryTemplate );
        chartComponent.setValues( values );

        String outerParams = parameterProvider.getStringParameter( OUTER_PARAMS, null );
        if ( StringUtils.isNotBlank( outerParams ) ) {
          StringTokenizer tokenizer = new StringTokenizer( outerParams, ";" ); //$NON-NLS-1$
          while ( tokenizer.hasMoreTokens() ) {
            chartComponent.addOuterParamName( tokenizer.nextToken() );
          }
        }
      } else {
        String dataAction = parameterProvider.getStringParameter( DATA_PROCESS, null );
        if ( StringUtils.isNotBlank( dataAction ) ) {
          chartComponent.setDataAction( dataAction );
        }
      }
    }

    protected IPentahoResultSet getValues( final String connectionName, final String queryTemplate ) throws Exception {
      IPentahoConnection connection = null;
      try {
        // connection = new SQLConnection(connectionName, logger)
        // TODO support non-SQL data sources. Much easier now using the factory
        connection =
            PentahoConnectionFactory.getConnection( IPentahoConnection.SQL_DATASOURCE, connectionName, userSession,
                logWriter.getLogger() );
        String query =
            TemplateUtil.applyTemplate( queryTemplate, TemplateUtil.parametersToProperties( parameterProvider ), null );
        return connection.executeQuery( query );
      } finally {
        if ( connection != null ) {
          connection.close();
        }
      }
    }

    protected AbstractJFreeChartComponent determineChartByChartType( int chartType, int height, int width,
        IPentahoUrlFactory urlFactory ) {
      // Didn't find a dataset, so try to create the component based on chart type.
      switch ( chartType ) {
        case JFreeChartEngine.BAR_CHART_TYPE:
        case JFreeChartEngine.AREA_CHART_TYPE:
        case JFreeChartEngine.BAR_LINE_CHART_TYPE:
        case JFreeChartEngine.LINE_CHART_TYPE:
        case JFreeChartEngine.DIFFERENCE_CHART_TYPE:
        case JFreeChartEngine.DOT_CHART_TYPE:
        case JFreeChartEngine.STEP_AREA_CHART_TYPE:
        case JFreeChartEngine.STEP_CHART_TYPE:
        case JFreeChartEngine.PIE_GRID_CHART_TYPE:
          CategoryDatasetChartComponent datasetChartComponent =
              new CategoryDatasetChartComponent( chartType, actionPath, width, height, urlFactory, messages );
          String seriesName = parameterProvider.getStringParameter( SERIES_NAME, null ); //$NON-NLS-1$
          datasetChartComponent.setSeriesName( seriesName );
          return datasetChartComponent;
        case JFreeChartEngine.PIE_CHART_TYPE:

          return new PieDatasetChartComponent( chartType, actionPath, width, height, urlFactory, messages );

        case JFreeChartEngine.DIAL_CHART_TYPE:

          DialChartComponent dialChartComponent =
              new DialChartComponent( chartType, actionPath, width, height, urlFactory, messages );
          // Very likely null; allow API users to continue to pass the dial value via parameters
          String dialValue = parameterProvider.getStringParameter( VALUE, null );
          if ( dialValue != null ) {
            Number numericDialValue =
                DataUtilities.toNumber( dialValue, LocaleHelper.getCurrencyFormat(), LocaleHelper.getNumberFormat() );
            dialChartComponent.setValue( numericDialValue.doubleValue() );
          }
          return dialChartComponent;

        case JFreeChartEngine.BUBBLE_CHART_TYPE:

          return new XYZSeriesCollectionChartComponent( chartType, actionPath, width, height, urlFactory, messages );

        case JFreeChartEngine.UNDEFINED_CHART_TYPE:
        default:

          // Unsupported chart type, bail out
          content =
              logWriter.logErrorAndGetContent( Error.ERROR_0003_INVALID_CHART_TYPE, chartTypeStr, Integer
                  .toString( chartType ) );
          return null;
      }
    }

    protected AbstractJFreeChartComponent determineChartByDataset( int chartType, int height, int width,
        IPentahoUrlFactory urlFactory ) {
      // Some charts are determined by the dataset that is passed in; check these first...
      if ( ChartDefinition.TIME_SERIES_COLLECTION_STR.equalsIgnoreCase( datasetType ) ) {
        return new TimeSeriesCollectionChartComponent( chartType, actionPath, width, height, urlFactory, messages );
      } else if ( ChartDefinition.XY_SERIES_COLLECTION_STR.equalsIgnoreCase( datasetType ) ) {
        return new XYSeriesCollectionChartComponent( chartType, actionPath, width, height, urlFactory, messages );
      } else if ( ChartDefinition.XYZ_SERIES_COLLECTION_STR.equalsIgnoreCase( datasetType ) ) {
        return new XYZSeriesCollectionChartComponent( chartType, actionPath, width, height, urlFactory, messages );
      }
      return null;
    }
  }

  /**
   * doChart generates the images and html necessary to render various charts within a web page.
   * 
   * @param actionPath
   *          full path including the name of the action sequence or resource
   * @param parameterProvider
   *          the collection of parameters to customize the chart
   * @param outputStream
   *          the output string buffer for the content
   * @param userSession
   *          the user session object
   * @param messages
   *          a collection to store error and logging messages
   * @param logger
   *          logging object
   * 
   * @return true if successful
   */
  public static boolean doChart( final String actionPath, final IParameterProvider parameterProvider,
      final StringBuffer outputStream, final IPentahoSession userSession, final List<?> messages, ILogger logger ) {
    Builder builder = new Builder( actionPath, parameterProvider, userSession, logger, messages );
    return builder.doChart( outputStream );
  }

  /**
   * doPieChart generates the images and html necessary to render pie charts. It provides a simple wrapper around the
   * class org.pentaho.ui.component.charting.PieDatasetChartComponent
   * 
   * @param solutionName
   *          the solution name
   * @param actionPath
   *          the action path
   * @param chartName
   *          the xml file describing the chart
   * @param parameterProvider
   *          the collection of parameters to customize the chart
   * @param outputStream
   *          the output string buffer for the content
   * @param userSession
   *          the user session object
   * @param messages
   *          a collection to store error and logging messages
   * @param logger
   *          logging object
   * 
   * @return true if successful
   * @deprecated use doChart instead
   */
  @Deprecated
  public static boolean doPieChart( final String actionPath, final IParameterProvider parameterProvider,
      final StringBuffer outputStream, final IPentahoSession userSession, final List<?> messages, final ILogger logger ) {
    boolean result = true;
    String outerParams = parameterProvider.getStringParameter( OUTER_PARAMS, null );
    String innerParam = parameterProvider.getStringParameter( INNER_PARAM, null );
    int width = (int) parameterProvider.getLongParameter( IMAGE_WIDTH, 150 );
    int height = (int) parameterProvider.getLongParameter( IMAGE_HEIGHT, 150 );
    String urlDrillTemplate = parameterProvider.getStringParameter( DRILL_URL, "" ); //$NON-NLS-1$

    String imageUrl = parameterProvider.getStringParameter( IMAGE_URL, null );
    if ( StringUtils.isBlank( imageUrl ) ) {
      imageUrl = PentahoRequestContextHolder.getRequestContext().getContextPath();
    }

    SimpleUrlFactory urlFactory = new SimpleUrlFactory( urlDrillTemplate );
    PieDatasetChartComponent chartComponent = null;
    try {
      chartComponent =
          new PieDatasetChartComponent( JFreeChartEngine.PIE_CHART_TYPE, actionPath, width, height, urlFactory,
              messages );
      if ( logger != null ) {
        chartComponent.setLoggingLevel( logger.getLoggingLevel() );
      }
      chartComponent.validate( userSession, null );
      chartComponent.setUrlTemplate( urlDrillTemplate );
      if ( outerParams != null ) {
        StringTokenizer tokenizer = new StringTokenizer( outerParams, ";" ); //$NON-NLS-1$
        while ( tokenizer.hasMoreTokens() ) {
          chartComponent.addOuterParamName( tokenizer.nextToken() );
        }
      }
      chartComponent.setParamName( innerParam );

      chartComponent.setDataAction( actionPath );

      chartComponent.setParameterProvider( IParameterProvider.SCOPE_REQUEST, parameterProvider );

      String content = chartComponent.getContent( "text/html" ); //$NON-NLS-1$

      if ( ( content == null ) || content.equals( "" ) ) { //$NON-NLS-1$
        content = "&nbsp;"; //$NON-NLS-1$
      }
      outputStream.append( content );

    } finally {
      if ( chartComponent != null ) {
        chartComponent.dispose();
      }
    }
    return result;

  }

  /**
   * doDial generates the images and html necessary to render dials. It provides a simple wrapper around the class
   * org.pentaho.ui.component.DashboardWidgetComponent
   * 
   * @param solutionName
   *          the solution name
   * @param actionPath
   *          the action path
   * @param chartName
   *          the xml file describing the chart
   * @param parameterProvider
   *          the collection of parameters to customize the chart
   * @param outputStream
   *          the output string buffer for the content
   * @param userSession
   *          the user session object
   * @param messages
   *          a collection to store error and logging messages
   * @param logger
   *          logging object
   * 
   * @return true if successful
   * @deprecated use doChart() instead
   */
  @Deprecated
  public static boolean doDial( final String solutionName, final String actionPath, final String chartName,
      final IParameterProvider parameterProvider, final StringBuffer outputStream, final IPentahoSession userSession,
      final ArrayList messages, final ILogger logger ) {

    boolean result = true;
    String linkUrl = parameterProvider.getStringParameter( "drill-url", null ); //$NON-NLS-1$
    IPentahoRequestContext requestContext = PentahoRequestContextHolder.getRequestContext();

    if ( linkUrl == null ) {
      linkUrl = ""; //$NON-NLS-1$
    }

    int width = (int) parameterProvider.getLongParameter( "image-width", 150 ); //$NON-NLS-1$
    int height = (int) parameterProvider.getLongParameter( "image-height", 150 ); //$NON-NLS-1$

    SimpleUrlFactory urlFactory = new SimpleUrlFactory( linkUrl );

    DashboardWidgetComponent widget = null;
    try {
      widget =
          new DashboardWidgetComponent( DashboardWidgetComponent.TYPE_DIAL, solutionName + File.separator + actionPath
              + File.separator + chartName, width, height, urlFactory, messages );
      if ( logger != null ) {
        widget.setLoggingLevel( logger.getLoggingLevel() );
      }
      widget.validate( userSession, null );

      widget.setParameterProvider( IParameterProvider.SCOPE_REQUEST, parameterProvider );

      String value = parameterProvider.getStringParameter( "value", "0" ); //$NON-NLS-1$//$NON-NLS-2$
      Number numericValue =
          DataUtilities.toNumber( value, LocaleHelper.getCurrencyFormat(), LocaleHelper.getNumberFormat() );

      widget.setValue( numericValue.doubleValue() );

      String title = parameterProvider.getStringParameter( "title", "" ); //$NON-NLS-1$ //$NON-NLS-2$
      widget.setTitle( title );

      String content = widget.getContent( "text/html" ); //$NON-NLS-1$

      if ( content == null ) {
        StringBuffer buffer = new StringBuffer();
        PentahoSystem
            .get( IMessageFormatter.class, userSession )
            .formatErrorMessage(
                "text/html", Messages.getInstance().getString( "Widget.ERROR_0001_COULD_NOT_CREATE_WIDGET" ), messages, buffer ); //$NON-NLS-1$ //$NON-NLS-2$
        content = buffer.toString();
        result = false;
      }

      if ( ( content == null ) || content.equals( "" ) ) { //$NON-NLS-1$
        content = "&nbsp;"; //$NON-NLS-1$
      }
      outputStream.append( content );

    } finally {
      if ( widget != null ) {
        widget.dispose();
      }
    }
    return result;

  }

}
