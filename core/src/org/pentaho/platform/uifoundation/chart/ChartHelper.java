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
import java.util.StringTokenizer;

/**
 * This class provides wrapper functions to make it easier to execute action sequences and generate a widget.
 */
public class ChartHelper {

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
      final StringBuffer outputStream, final IPentahoSession userSession, final ArrayList messages, ILogger logger ) {

    boolean result = true;
    String content = null;
    StringBuffer messageBuffer = new StringBuffer();

    if ( logger == null ) {
      // No logger? The usersession extends ILogger, use it for logging
      logger = userSession;
    }

    // Retrieve all parameters from parameter provider

    String outerParams = parameterProvider.getStringParameter( "outer-params", null ); //$NON-NLS-1$
    String innerParam = parameterProvider.getStringParameter( "inner-param", null ); //$NON-NLS-1$

    String urlDrillTemplate = parameterProvider.getStringParameter( "drill-url", null ); //$NON-NLS-1$
    String imageUrl = parameterProvider.getStringParameter( "image-url", null ); //$NON-NLS-1$

    // Very likely null; allow API users to continue to pass the dial value via parameters
    String dialValue = parameterProvider.getStringParameter( "value", null ); //$NON-NLS-1$
    IPentahoRequestContext requestContext = PentahoRequestContextHolder.getRequestContext();
    if ( imageUrl == null ) {
      imageUrl = requestContext.getContextPath(); //$NON-NLS-1$
    }

    if ( urlDrillTemplate == null ) {
      urlDrillTemplate = ""; //$NON-NLS-1$
    }

    int width = (int) parameterProvider.getLongParameter( "image-width", 150 ); //$NON-NLS-1$
    int height = (int) parameterProvider.getLongParameter( "image-height", 150 ); //$NON-NLS-1$

    SimpleUrlFactory urlFactory = new SimpleUrlFactory( urlDrillTemplate );

    // Determine the type of chart we are building; these values can come from the chart xml definition, or
    // from the parameter provider. Try the parameter provider first, for performance reasons.

    String chartTypeStr = parameterProvider.getStringParameter( ChartDefinition.TYPE_NODE_NAME, null );
    String datasetType = ChartDefinition.CATEGORY_DATASET_STR;
    if ( ( chartTypeStr == null ) || ( chartTypeStr.length() == 0 ) ) {

      try {
        // attempt to get the chart type and possibly data type from the xml doc
        ActionSequenceJCRHelper jcrHelper = new ActionSequenceJCRHelper( userSession );
        Document chartDefinition = jcrHelper.getSolutionDocument( actionPath, RepositoryFilePermission.READ );
        Node chartAttributes = chartDefinition.selectSingleNode( "//" + AbstractChartComponent.CHART_NODE_NAME ); //$NON-NLS-1$
        chartTypeStr = chartAttributes.selectSingleNode( ChartDefinition.TYPE_NODE_NAME ).getText();
        Node datasetTypeNode = chartAttributes.selectSingleNode( ChartDefinition.DATASET_TYPE_NODE_NAME );
        if ( datasetTypeNode != null ) {
          datasetType = datasetTypeNode.getText();
        }

      } catch ( Exception e ) {

        logger.error(
            Messages.getInstance().getErrorString( "ChartHelper.ERROR_0001_IO_PROBLEM_GETTING_CHART_TYPE" ), e ); //$NON-NLS-1$
        PentahoSystem
            .get( IMessageFormatter.class, userSession )
            .formatErrorMessage(
                "text/html", Messages.getInstance().getString( "ChartHelper.ERROR_0001_IO_PROBLEM_GETTING_CHART_TYPE" ), messages, messageBuffer ); //$NON-NLS-1$ //$NON-NLS-2$
        content = messageBuffer.toString();
        result = false;
      }
    }

    // Check again - do we have a chart type now? If not, bail out, we have no idea what to try to generate
    if ( ( chartTypeStr == null ) || ( chartTypeStr.length() == 0 ) ) {

      logger.error( Messages.getInstance().getString( "ChartHelper.ERROR_0002_COULD_NOT_DETERMINE_CHART_TYPE" ) ); //$NON-NLS-1$
      PentahoSystem
          .get( IMessageFormatter.class, userSession )
          .formatErrorMessage(
              "text/html", Messages.getInstance().getString( "ChartHelper.ERROR_0002_COULD_NOT_DETERMINE_CHART_TYPE" ), messages, messageBuffer ); //$NON-NLS-1$ //$NON-NLS-2$
      content = messageBuffer.toString();
      result = false;
    }

    if ( !result ) {
      outputStream.append( content );
      return result;
    }

    int chartType = JFreeChartEngine.getChartType( chartTypeStr );
    AbstractJFreeChartComponent chartComponent = null;

    try {
      // Some charts are determined by the dataset that is passed in; check these first...
      if ( datasetType.equalsIgnoreCase( ChartDefinition.TIME_SERIES_COLLECTION_STR ) ) {
        chartComponent =
            new TimeSeriesCollectionChartComponent( chartType, actionPath, width, height, urlFactory, messages );
      } else if ( datasetType.equalsIgnoreCase( ChartDefinition.XY_SERIES_COLLECTION_STR ) ) {
        chartComponent =
            new XYSeriesCollectionChartComponent( chartType, actionPath, width, height, urlFactory, messages );
      } else if ( datasetType.equalsIgnoreCase( ChartDefinition.XYZ_SERIES_COLLECTION_STR ) ) {
        chartComponent =
            new XYZSeriesCollectionChartComponent( chartType, actionPath, width, height, urlFactory, messages );
      }

      // Didn't find a dataset, so try to create the component based on chart type.
      if ( chartComponent == null ) {
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

            chartComponent =
                new CategoryDatasetChartComponent( chartType, actionPath, width, height, urlFactory, messages );
            break;

          case JFreeChartEngine.PIE_CHART_TYPE:

            chartComponent = new PieDatasetChartComponent( chartType, actionPath, width, height, urlFactory, messages );
            break;

          case JFreeChartEngine.DIAL_CHART_TYPE:

            chartComponent = new DialChartComponent( chartType, actionPath, width, height, urlFactory, messages );
            if ( dialValue != null ) {
              Number numericDialValue =
                  DataUtilities.toNumber( dialValue, LocaleHelper.getCurrencyFormat(), LocaleHelper.getNumberFormat() );
              ( (DialChartComponent) chartComponent ).setValue( numericDialValue.doubleValue() );
            }
            break;

          case JFreeChartEngine.BUBBLE_CHART_TYPE:

            chartComponent =
                new XYZSeriesCollectionChartComponent( chartType, actionPath, width, height, urlFactory, messages );
            break;

          case JFreeChartEngine.UNDEFINED_CHART_TYPE:
          default:
            // Unsupported chart type, bail out
            logger.error( Messages.getInstance().getString(
                "ChartHelper.ERROR_0003_INVALID_CHART_TYPE", chartTypeStr, Integer.toString( chartType ) ) ); //$NON-NLS-1$
            PentahoSystem.get( IMessageFormatter.class, userSession ).formatErrorMessage(
                "text/html", Messages.getInstance().getString( "ChartHelper.ERROR_0003_INVALID_CHART_TYPE", //$NON-NLS-1$ //$NON-NLS-2$
                    chartTypeStr, Integer.toString( chartType ) ), messages, messageBuffer );
            content = messageBuffer.toString();
            result = false;

        }
      }

      if ( result && ( chartComponent != null ) ) {
        try {

          chartComponent.setLoggingLevel( logger.getLoggingLevel() );
          chartComponent.validate( userSession, null );
          chartComponent.setDataAction( actionPath );
          chartComponent.setUrlTemplate( urlDrillTemplate );

          String seriesName = parameterProvider.getStringParameter( "series-name", null ); //$NON-NLS-1$
          if ( chartComponent instanceof CategoryDatasetChartComponent ) {
            ( (CategoryDatasetChartComponent) chartComponent ).setSeriesName( seriesName );
          }

          // WARNING!!! This is an atypical way to access data for the chart... these parameters and their
          // usage are undocumented, and only left in here to support older solutions that may be using them.
          // *************** START QUESTIONABLE CODE ********************************************************

          String connectionName = parameterProvider.getStringParameter( "connection", null ); //$NON-NLS-1$
          String query = parameterProvider.getStringParameter( "query", null ); //$NON-NLS-1$
          String dataAction = parameterProvider.getStringParameter( "data-process", null ); //$NON-NLS-1$

          IPentahoConnection connection = null;
          try {
            chartComponent.setParamName( innerParam );
            chartComponent.setParameterProvider( IParameterProvider.SCOPE_REQUEST, parameterProvider );
            if ( ( connectionName != null ) && ( query != null ) ) {
              // connection = new SQLConnection(connectionName, logger)
              // TODO support non-SQL data sources. Much easier now using the factory
              connection =
                  PentahoConnectionFactory.getConnection( IPentahoConnection.SQL_DATASOURCE, connectionName,
                      userSession, logger );

              try {
                query =
                    TemplateUtil.applyTemplate( query, TemplateUtil.parametersToProperties( parameterProvider ), null );
                IPentahoResultSet results = connection.executeQuery( query );
                chartComponent.setValues( results );
              } finally {
                boolean ignored = true;
              }

              chartComponent.setUrlTemplate( urlDrillTemplate );
              if ( outerParams != null ) {
                StringTokenizer tokenizer = new StringTokenizer( outerParams, ";" ); //$NON-NLS-1$
                while ( tokenizer.hasMoreTokens() ) {
                  chartComponent.addOuterParamName( tokenizer.nextToken() );
                }
              }
            } else if ( dataAction != null ) {
              chartComponent.setDataAction( dataAction );
            }
            // ***************** END QUESTIONABLE CODE ********************************************************

            content = chartComponent.getContent( "text/html" ); //$NON-NLS-1$

          } finally {
            if ( connection != null ) {
              connection.close();
            }
          }

        } catch ( Throwable e ) {
          logger.error( Messages.getInstance().getErrorString( "Widget.ERROR_0001_COULD_NOT_CREATE_WIDGET" ), e ); //$NON-NLS-1$
        }
      } // end of if(result)

      try {
        if ( content == null ) {
          PentahoSystem
              .get( IMessageFormatter.class, userSession )
              .formatErrorMessage(
                  "text/html", Messages.getInstance().getErrorString( "Widget.ERROR_0001_COULD_NOT_CREATE_WIDGET" ), messages, messageBuffer ); //$NON-NLS-1$ //$NON-NLS-2$
          content = messageBuffer.toString();
          result = false;
        }
        outputStream.append( content );
      } catch ( Exception e ) {
        logger.error( Messages.getInstance().getErrorString( "Widget.ERROR_0001_COULD_NOT_CREATE_WIDGET" ), e ); //$NON-NLS-1$
      }

    } finally {
      if ( chartComponent != null ) {
        chartComponent.dispose();
      }
    }
    return result;
  }

  /**
   * doPieChart generates the images and html necessary to render pie charts. It provides a simple wrapper around
   * the class org.pentaho.ui.component.charting.PieDatasetChartComponent
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
  public static boolean
  doPieChart( final String actionPath, final IParameterProvider parameterProvider, final StringBuffer outputStream,
        final IPentahoSession userSession, final ArrayList messages, final ILogger logger ) {

    boolean result = true;
    String outerParams = parameterProvider.getStringParameter( "outer-params", null ); //$NON-NLS-1$
    String innerParam = parameterProvider.getStringParameter( "inner-param", null ); //$NON-NLS-1$

    String urlDrillTemplate = parameterProvider.getStringParameter( "drill-url", null ); //$NON-NLS-1$
    String imageUrl = parameterProvider.getStringParameter( "image-url", null ); //$NON-NLS-1$
    IPentahoRequestContext requestContext = PentahoRequestContextHolder.getRequestContext();
    if ( imageUrl == null ) {
      imageUrl = requestContext.getContextPath(); //$NON-NLS-1$
    }

    if ( urlDrillTemplate == null ) {
      urlDrillTemplate = ""; //$NON-NLS-1$
    }

    int width = (int) parameterProvider.getLongParameter( "image-width", 150 ); //$NON-NLS-1$
    int height = (int) parameterProvider.getLongParameter( "image-height", 150 ); //$NON-NLS-1$

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
    String imageUrl = parameterProvider.getStringParameter( "image-url", null ); //$NON-NLS-1$
    IPentahoRequestContext requestContext = PentahoRequestContextHolder.getRequestContext();
    if ( imageUrl == null ) {
      imageUrl = requestContext.getContextPath(); //$NON-NLS-1$
    }

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
