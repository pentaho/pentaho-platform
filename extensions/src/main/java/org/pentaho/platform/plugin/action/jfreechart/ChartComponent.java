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

package org.pentaho.platform.plugin.action.jfreechart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.dom4j.Node;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.imagemap.ImageMapUtilities;
import org.jfree.chart.imagemap.StandardToolTipTagFragmentGenerator;
import org.jfree.data.general.Dataset;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.IPentahoRequestContext;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.api.util.XmlParseException;
import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.solution.ComponentBase;
import org.pentaho.platform.engine.services.solution.PentahoEntityResolver;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.uifoundation.chart.BarLineChartDefinition;
import org.pentaho.platform.uifoundation.chart.CategoryDatasetChartDefinition;
import org.pentaho.platform.uifoundation.chart.ChartDefinition;
import org.pentaho.platform.uifoundation.chart.DialWidgetDefinition;
import org.pentaho.platform.uifoundation.chart.JFreeChartEngine;
import org.pentaho.platform.uifoundation.chart.PentahoChartURLTagFragmentGenerator;
import org.pentaho.platform.uifoundation.chart.PieDatasetChartDefinition;
import org.pentaho.platform.uifoundation.chart.TimeSeriesCollectionChartDefinition;
import org.pentaho.platform.uifoundation.chart.TimeTableXYDatasetChartDefinition;
import org.pentaho.platform.uifoundation.chart.XYSeriesCollectionChartDefinition;
import org.pentaho.platform.uifoundation.chart.XYZSeriesCollectionChartDefinition;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ChartComponent extends ComponentBase {
  private static final long serialVersionUID = 9050456842938084174L;

  private static final String CHART_NAME_PROP = "chart-name"; //$NON-NLS-1$

  private static final String CHART_OUTPUT = "chart-object"; //$NON-NLS-1$

  private static final String CHART_TYPE = "CHART-OBJECT"; //$NON-NLS-1$

  private static final String CHART_DATA_PROP = "chart-data"; //$NON-NLS-1$

  // Root xml tag for chart attributes when chart is generated via an action sequence
  private static final String CHART_ATTRIBUTES_PROP = "chart-attributes"; //$NON-NLS-1$

  // This member added for compatibility to dashboard chart definitions
  // Root xml tag for chart attributes when chart is generated via a dashboard
  private static final String ALTERNATIVE_CHART_ATTRIBUTES_PROP = "chart"; //$NON-NLS-1$

  private static final String BY_ROW_PROP = "by-row"; //$NON-NLS-1$

  private static final String URL_TEMPLATE = "url-template"; //$NON-NLS-1$

  private static final String PARAMETER_NAME = "paramName"; //$NON-NLS-1$

  private static final String OUTER_PARAMETER_NAME = "series-name"; //$NON-NLS-1$

  private static final String OUTPUT_TYPE_PROP = "output-type"; //$NON-NLS-1$

  private static final String CHART_FILE_NAME_OUTPUT = "chart-filename"; //$NON-NLS-1$

  private static final String HTML_MAPPING_OUTPUT = "chart-mapping"; //$NON-NLS-1$

  private static final String HTML_MAPPING_HTML = "chart-map-html"; //$NON-NLS-1$

  private static final String BASE_URL_OUTPUT = "base-url"; //$NON-NLS-1$

  private static final String CONTEXT_PATH_OUTPUT = "context-path"; //$NON-NLS-1$

  private static final String FULLY_QUALIFIED_SERVER_URL_OUTPUT = "fully-qualified-server-url"; //$NON-NLS-1$

  private static final String HTML_IMG_TAG = "image-tag"; //$NON-NLS-1$

  private static final String SVG_TYPE = "SVG"; //$NON-NLS-1$

  private static final String PNG_BYTES_TYPE = "png-bytes"; //$NON-NLS-1$

  private static final String TEMP_DIRECTORY = "system/tmp/"; //$NON-NLS-1$

  private static final String FILENAME_PREFIX = "tmp_chart_"; //$NON-NLS-1$

  private static final String USE_BASE_URL_TAG = "use-base-url"; //$NON-NLS-1$

  private static final String URL_TARGET_TAG = "url-target"; //$NON-NLS-1$

  private static final String PNG_EXTENSION = ".png"; //$NON-NLS-1$

  private static final String SVG_EXTENSION = ".svg"; //$NON-NLS-1$

  private static final String MAP_EXTENSION = ".map"; //$NON-NLS-1$

  private static final String KEEP_TEMP_FILE_PROP = "keep_temp_file"; //$NON-NLS-1$

  private static final int FILE_NAME = 0;

  private static final int MAP_NAME = 1;

  public static final String FOREGROUND_ALPHA = "foreground-alpha"; //$NON-NLS-1$

  public static final String BACKGROUND_ALPHA = "background-alpha"; //$NON-NLS-1$

  @Override
  public Log getLogger() {
    return LogFactory.getLog( ChartComponent.class );
  }

  @Override
  protected boolean validateSystemSettings() {
    // This component does not have any system settings to validate
    return true;
  }

  @Override
  protected boolean validateAction() {
    // See if we have chart data
    if ( !isDefinedInput( ChartComponent.CHART_DATA_PROP ) ) {
      inputMissingError( ChartComponent.CHART_DATA_PROP );
      return false;
    }
    // See if we have chart attributes
    if ( !isDefinedInput( ChartComponent.CHART_ATTRIBUTES_PROP ) ) {
      if ( !isDefinedResource( ChartComponent.CHART_ATTRIBUTES_PROP ) ) {
        inputMissingError( ChartComponent.CHART_ATTRIBUTES_PROP );
        return false;
      }
    }

    // Anything else should be optional
    return true;
  }

  @Override
  public void done() {
  }

  @Override
  protected boolean executeAction() {
    int height = -1;
    int width = -1;
    String title = ""; //$NON-NLS-1$
    Node chartDocument = null;
    IPentahoResultSet data = (IPentahoResultSet) getInputValue( ChartComponent.CHART_DATA_PROP );
    if ( !data.isScrollable() ) {
      getLogger().debug( "ResultSet is not scrollable. Copying into memory" ); //$NON-NLS-1$
      IPentahoResultSet memSet = data.memoryCopy();
      data.close();
      data = memSet;
    }

    String urlTemplate = (String) getInputValue( ChartComponent.URL_TEMPLATE );

    Node chartAttributes = null;
    String chartAttributeString = null;

    // Attempt to get chart attributes as an input string or as a resource file
    // If these don't trip, then we assume the chart attributes are defined in
    // the component-definition of the chart action.

    if ( getInputNames().contains( ChartComponent.CHART_ATTRIBUTES_PROP ) ) {
      chartAttributeString = getInputStringValue( ChartComponent.CHART_ATTRIBUTES_PROP );
    } else if ( isDefinedResource( ChartComponent.CHART_ATTRIBUTES_PROP ) ) {
      IActionSequenceResource resource = getResource( ChartComponent.CHART_ATTRIBUTES_PROP );
      chartAttributeString = getResourceAsString( resource );
    }

    // Realize chart attributes as an XML document
    if ( chartAttributeString != null ) {
      try {
        chartDocument = XmlDom4JHelper.getDocFromString( chartAttributeString, new PentahoEntityResolver() );
      } catch ( XmlParseException e ) {
        getLogger()
            .error( Messages.getInstance().getString( "ChartComponent.ERROR_0005_CANT_DOCUMENT_FROM_STRING" ), e ); //$NON-NLS-1$
        return false;
      }

      chartAttributes = chartDocument.selectSingleNode( ChartComponent.CHART_ATTRIBUTES_PROP );

      // This line of code handles a discrepancy between the schema of a chart definition
      // handed to a dashboard versus a ChartComponent schema. The top level node for the dashboard charts
      // is <chart>, whereas the ChartComponent expects <chart-attributes>.

      // TODO:
      // This discrepancy should be resolved when we have ONE chart solution.

      if ( chartAttributes == null ) {
        chartAttributes = chartDocument.selectSingleNode( ChartComponent.ALTERNATIVE_CHART_ATTRIBUTES_PROP );
      }
    }

    // Default chart attributes are in the component-definition section of the action definition.
    if ( chartAttributes == null ) {
      chartAttributes = getComponentDefinition( true ).selectSingleNode( ChartComponent.CHART_ATTRIBUTES_PROP );
    }

    // URL click-through attributes (useBaseURL, target) are only processed IF we
    // have an urlTemplate attribute
    if ( ( urlTemplate == null ) || ( urlTemplate.length() == 0 ) ) {
      if ( chartAttributes.selectSingleNode( ChartComponent.URL_TEMPLATE ) != null ) {
        urlTemplate = chartAttributes.selectSingleNode( ChartComponent.URL_TEMPLATE ).getText();
      }
    }

    // These parameters are replacement variables parsed into the
    // urlTemplate specifically when we have a URL that is a drill-through
    // link in a chart intended to drill down into the chart data.
    String parameterName = (String) getInputValue( ChartComponent.PARAMETER_NAME );
    if ( ( parameterName == null ) || ( parameterName.length() == 0 ) ) {
      if ( chartAttributes.selectSingleNode( ChartComponent.PARAMETER_NAME ) != null ) {
        parameterName = chartAttributes.selectSingleNode( ChartComponent.PARAMETER_NAME ).getText();
      }
    }

    // These parameters are replacement variables parsed into the
    // urlTemplate specifically when we have a URL that is a drill-through
    // link in a chart intended to drill down into the chart data.
    String outerParameterName = (String) getInputValue( ChartComponent.OUTER_PARAMETER_NAME );
    if ( ( outerParameterName == null ) || ( outerParameterName.length() == 0 ) ) {
      if ( chartAttributes.selectSingleNode( ChartComponent.OUTER_PARAMETER_NAME ) != null ) {
        outerParameterName = chartAttributes.selectSingleNode( ChartComponent.OUTER_PARAMETER_NAME ).getText();
      }
    }

    String chartType = chartAttributes.selectSingleNode( ChartDefinition.TYPE_NODE_NAME ).getText();

    // --------------- This code allows inputs to override the chartAttributes
    // of width, height, and title
    Object widthObj = getInputValue( ChartDefinition.WIDTH_NODE_NAME );
    if ( widthObj != null ) {
      width = Integer.parseInt( widthObj.toString() );
      if ( width != -1 ) {
        if ( chartAttributes.selectSingleNode( ChartDefinition.WIDTH_NODE_NAME ) == null ) {
          ( (Element) chartAttributes ).addElement( ChartDefinition.WIDTH_NODE_NAME );
        }
        chartAttributes.selectSingleNode( ChartDefinition.WIDTH_NODE_NAME ).setText( Integer.toString( width ) );
      }
    }
    Object heightObj = getInputValue( ChartDefinition.HEIGHT_NODE_NAME );
    if ( heightObj != null ) {
      height = Integer.parseInt( heightObj.toString() );
      if ( height != -1 ) {
        if ( chartAttributes.selectSingleNode( ChartDefinition.HEIGHT_NODE_NAME ) == null ) {
          ( (Element) chartAttributes ).addElement( ChartDefinition.HEIGHT_NODE_NAME );
        }
        chartAttributes.selectSingleNode( ChartDefinition.HEIGHT_NODE_NAME ).setText( Integer.toString( height ) );
      }
    }
    Object titleObj = getInputValue( ChartDefinition.TITLE_NODE_NAME );
    if ( titleObj != null ) {
      if ( chartAttributes.selectSingleNode( ChartDefinition.TITLE_NODE_NAME ) == null ) {
        ( (Element) chartAttributes ).addElement( ChartDefinition.TITLE_NODE_NAME );
      }
      chartAttributes.selectSingleNode( ChartDefinition.TITLE_NODE_NAME ).setText( titleObj.toString() );
    }
    // ----------------End of Override

    // ---------------Feed the Title and Subtitle information through the input substitution
    Node titleNode = chartAttributes.selectSingleNode( ChartDefinition.TITLE_NODE_NAME );
    if ( titleNode != null ) {
      String titleStr = titleNode.getText();
      if ( titleStr != null ) {
        title = titleStr;
        String newTitle = applyInputsToFormat( titleStr );
        titleNode.setText( newTitle );
      }
    }

    List subtitles = chartAttributes.selectNodes( ChartDefinition.SUBTITLE_NODE_NAME );

    if ( ( subtitles == null ) || ( subtitles.isEmpty() ) ) {
      Node subTitlesNode = chartAttributes.selectSingleNode( ChartDefinition.SUBTITLES_NODE_NAME );
      if ( subTitlesNode != null ) {
        subtitles = chartAttributes.selectNodes( ChartDefinition.SUBTITLE_NODE_NAME );
      }
    } else {
      // log a deprecation warning for this property...
      getLogger().warn(
          Messages.getInstance().getString(
              "CHART.WARN_DEPRECATED_CHILD", ChartDefinition.SUBTITLE_NODE_NAME, ChartDefinition.SUBTITLES_NODE_NAME ) ); //$NON-NLS-1$
      getLogger().warn(
          Messages.getInstance()
              .getString( "CHART.WARN_PROPERTY_WILL_NOT_VALIDATE", ChartDefinition.SUBTITLE_NODE_NAME ) ); //$NON-NLS-1$
    }

    if ( subtitles != null ) {
      for ( Iterator iter = subtitles.iterator(); iter.hasNext(); ) {
        Node subtitleNode = (Node) iter.next();
        if ( subtitleNode != null ) {
          String subtitleStr = subtitleNode.getText();
          if ( subtitleStr != null ) {
            String newSubtitle = applyInputsToFormat( subtitleStr );
            subtitleNode.setText( newSubtitle );
          }
        }
      }
    }

    // ----------------End of Format

    // Determine if we are going to read the chart data set by row or by column
    boolean byRow = false;
    if ( getInputStringValue( ChartComponent.BY_ROW_PROP ) != null ) {
      byRow = Boolean.valueOf( getInputStringValue( ChartComponent.BY_ROW_PROP ) ).booleanValue();
    }

    // TODO Figure out why these overrides are called here. Seems like we are doing the same thing we just did above,
    // but
    // could possibly step on the height and width values set previously.

    if ( height == -1 ) {
      height =
          (int) getInputLongValue( ChartComponent.CHART_ATTRIBUTES_PROP + "/" + ChartDefinition.HEIGHT_NODE_NAME, 50 ); //$NON-NLS-1$
    }
    if ( width == -1 ) {
      width =
          (int) getInputLongValue( ChartComponent.CHART_ATTRIBUTES_PROP + "/" + ChartDefinition.WIDTH_NODE_NAME, 100 ); //$NON-NLS-1$      
    }

    if ( title.length() <= 0 ) {
      title = getInputStringValue( ChartComponent.CHART_ATTRIBUTES_PROP + "/" + ChartDefinition.TITLE_NODE_NAME ); //$NON-NLS-1$
    }

    // Select the right dataset to use based on the chart type
    // Default to category dataset
    String datasetType = ChartDefinition.CATEGORY_DATASET_STR;
    boolean isStacked = false;
    Node datasetTypeNode = chartAttributes.selectSingleNode( ChartDefinition.DATASET_TYPE_NODE_NAME );
    if ( datasetTypeNode != null ) {
      datasetType = datasetTypeNode.getText();
    }
    Dataset dataDefinition = null;
    if ( ChartDefinition.XY_SERIES_COLLECTION_STR.equalsIgnoreCase( datasetType ) ) {
      dataDefinition = new XYSeriesCollectionChartDefinition( data, byRow, chartAttributes, getSession() );
    } else if ( ChartDefinition.TIME_SERIES_COLLECTION_STR.equalsIgnoreCase( datasetType ) ) {

      Node stackedNode = chartAttributes.selectSingleNode( ChartDefinition.STACKED_NODE_NAME );
      if ( stackedNode != null ) {
        isStacked = Boolean.valueOf( stackedNode.getText() ).booleanValue();
      }
      if ( ( isStacked ) && ( ChartDefinition.AREA_CHART_STR.equalsIgnoreCase( chartType ) ) ) {
        dataDefinition = new TimeTableXYDatasetChartDefinition( data, byRow, chartAttributes, getSession() );
      } else {
        dataDefinition = new TimeSeriesCollectionChartDefinition( data, byRow, chartAttributes, getSession() );
      }
    } else if ( ChartDefinition.PIE_CHART_STR.equalsIgnoreCase( chartType ) ) {
      dataDefinition = new PieDatasetChartDefinition( data, byRow, chartAttributes, getSession() );
    } else if ( ChartDefinition.DIAL_CHART_STR.equalsIgnoreCase( chartType ) ) {
      dataDefinition = new DialWidgetDefinition( data, byRow, chartAttributes, width, height, getSession() );
    } else if ( ChartDefinition.BAR_LINE_CHART_STR.equalsIgnoreCase( chartType ) ) {
      dataDefinition = new BarLineChartDefinition( data, byRow, chartAttributes, getSession() );
    } else if ( ChartDefinition.BUBBLE_CHART_STR.equalsIgnoreCase( chartType ) ) {
      dataDefinition = new XYZSeriesCollectionChartDefinition( data, byRow, chartAttributes, getSession() );
    } else {
      dataDefinition = new CategoryDatasetChartDefinition( data, byRow, chartAttributes, getSession() );
    }

    // Determine what we are sending back - Default to OUTPUT_PNG output
    // OUTPUT_PNG = the chart gets written to a file in .png format
    // OUTPUT_SVG = the chart gets written to a file in .svg (XML) format
    // OUTPUT_CHART = the chart in a byte stream gets stored as as an IContentItem
    // OUTPUT_PNG_BYTES = the chart gets sent as a byte stream in .png format

    int outputType = JFreeChartEngine.OUTPUT_PNG;

    if ( getInputStringValue( ChartComponent.OUTPUT_TYPE_PROP ) != null ) {
      if ( ChartComponent.SVG_TYPE.equalsIgnoreCase( getInputStringValue( ChartComponent.OUTPUT_TYPE_PROP ) ) ) {
        outputType = JFreeChartEngine.OUTPUT_SVG;
      } else if ( ChartComponent.CHART_TYPE.equalsIgnoreCase( getInputStringValue( ChartComponent.
        OUTPUT_TYPE_PROP ) ) ) {
        outputType = JFreeChartEngine.OUTPUT_CHART;
      } else if ( ChartComponent.PNG_BYTES_TYPE
          .equalsIgnoreCase( getInputStringValue( ChartComponent.OUTPUT_TYPE_PROP ) ) ) {
        outputType = JFreeChartEngine.OUTPUT_PNG_BYTES;
      }
    }

    boolean keepTempFile = false;
    if ( isDefinedInput( KEEP_TEMP_FILE_PROP ) ) {
      keepTempFile = getInputBooleanValue( KEEP_TEMP_FILE_PROP, false );
    }

    JFreeChart chart = null;

    switch ( outputType ) {

    /**************************** OUTPUT_PNG_BYTES *********************************************/
      case JFreeChartEngine.OUTPUT_PNG_BYTES:

        chart = JFreeChartEngine.getChart( dataDefinition, title, "", width, height, this ); //$NON-NLS-1$

        // TODO Shouldn't the mime types and other strings here be constant somewhere? Where do we
        // put this type of general info ?

        String mimeType = "image/png"; //$NON-NLS-1$
        IContentItem contentItem = getOutputItem( "chartdata", mimeType, ".png" ); //$NON-NLS-1$ //$NON-NLS-2$
        contentItem.setMimeType( mimeType );
        try {

          OutputStream output = contentItem.getOutputStream( getActionName() );
          ChartUtilities.writeChartAsPNG( output, chart, width, height );

        } catch ( Exception e ) {
          error( Messages.getInstance().getErrorString( "ChartComponent.ERROR_0004_CANT_CREATE_IMAGE" ), e ); //$NON-NLS-1$
          return false;
        }

        break;

      /**************************** OUTPUT_SVG && OUTPUT_PNG *************************************/
      case JFreeChartEngine.OUTPUT_SVG:
        // intentionally fall through to PNG

      case JFreeChartEngine.OUTPUT_PNG:

        // Don't include the map in a file if HTML_MAPPING_HTML is specified, as that
        // param sends the map back on the outputstream as a string
        boolean createMapFile = !isDefinedOutput( ChartComponent.HTML_MAPPING_HTML );
        boolean hasTemplate = urlTemplate != null && urlTemplate.length() > 0;

        File[] fileResults = createTempFile( outputType, hasTemplate, !keepTempFile );

        if ( fileResults == null ) {
          error( Messages.getInstance().getErrorString( "ChartComponent.ERROR_0003_CANT_CREATE_TEMP_FILES" ) ); //$NON-NLS-1$
          return false;
        }

        String chartId =
            fileResults[ChartComponent.FILE_NAME].getName().substring( 0,
                fileResults[ChartComponent.FILE_NAME].getName().indexOf( '.' ) );
        String filePathWithoutExtension = ChartComponent.TEMP_DIRECTORY + chartId;
        PrintWriter printWriter = new PrintWriter( new StringWriter() );
        ChartRenderingInfo info = new ChartRenderingInfo( new StandardEntityCollection() );

        JFreeChartEngine.saveChart( dataDefinition, title,
            "", filePathWithoutExtension, width, height, outputType, printWriter, info, this ); //$NON-NLS-1$

        // Creating the image map
        boolean useBaseUrl = true;
        String urlTarget = "pentaho_popup"; //$NON-NLS-1$

        // Prepend the base url to the front of every drill through link
        if ( chartAttributes.selectSingleNode( ChartComponent.USE_BASE_URL_TAG ) != null ) {
          Boolean booleanValue =
              new Boolean( chartAttributes.selectSingleNode( ChartComponent.USE_BASE_URL_TAG ).getText() );
          useBaseUrl = booleanValue.booleanValue();
        }

        // What target for link? _parent, _blank, etc.
        if ( chartAttributes.selectSingleNode( ChartComponent.URL_TARGET_TAG ) != null ) {
          urlTarget = chartAttributes.selectSingleNode( ChartComponent.URL_TARGET_TAG ).getText();
        }

        String mapString = null;
        if ( hasTemplate ) {
          try {
            String mapId =
                fileResults[ChartComponent.MAP_NAME].getName().substring( 0,
                    fileResults[ChartComponent.MAP_NAME].getName().indexOf( '.' ) );
            mapString =
                ImageMapUtilities.getImageMap( mapId, info, new StandardToolTipTagFragmentGenerator(),
                    new PentahoChartURLTagFragmentGenerator( urlTemplate, urlTarget, useBaseUrl, dataDefinition,
                        parameterName, outerParameterName ) );

            if ( createMapFile ) {
              BufferedWriter out = new BufferedWriter( new FileWriter( fileResults[ChartComponent.MAP_NAME] ) );
              out.write( mapString );
              out.flush();
              out.close();
            }
          } catch ( IOException e ) {
            error( Messages.getInstance().getErrorString(
                "ChartComponent.ERROR_0001_CANT_WRITE_MAP", fileResults[ChartComponent.MAP_NAME].getPath() ) ); //$NON-NLS-1$
            return false;
          } catch ( Exception e ) {
            error( e.getLocalizedMessage(), e );
            return false;
          }

        }

        /*******************************************************************************************************
         * Legitimate outputs for the ChartComponent in an action sequence:
         * 
         * CHART_OUTPUT (chart-output) Stores the chart in the content repository as an IContentItem.
         * 
         * CHART_FILE_NAME_OUTPUT (chart-filename) Returns the name of the chart file, including the file extension
         * (with no path information) as a String.
         * 
         * HTML_MAPPING_OUTPUT (chart-mapping) Returns the name of the file that the map has been saved to, including
         * the file extension (with no path information) as a String. Will be empty if url-template is undefined
         * 
         * HTML_MAPPING_HTML (chart-map-html) Returns the chart image map HTML as a String. Will be empty if
         * url-template is undefined
         * 
         * BASE_URL_OUTPUT (base-url) Returns the web app's base URL (ie., http://localhost:8080/pentaho) as a String.
         * 
         * HTML_IMG_TAG (image-tag) Returns the HTML snippet including the image map, image (<IMG />) tag for the chart
         * image with src, width, height and usemap attributes defined. Usemap will not be included if url-template is
         * undefined.
         * 
         *******************************************************************************************************/

        // Now set the outputs
        Set outputs = getOutputNames();

        if ( ( outputs != null ) && ( outputs.size() > 0 ) ) {

          Iterator iter = outputs.iterator();
          while ( iter.hasNext() ) {

            String outputName = (String) iter.next();
            String outputValue = null;

            if ( outputName.equals( ChartComponent.CHART_FILE_NAME_OUTPUT ) ) {

              outputValue = fileResults[ChartComponent.FILE_NAME].getName();

            } else if ( outputName.equals( ChartComponent.HTML_MAPPING_OUTPUT ) ) {
              if ( hasTemplate ) {
                outputValue = fileResults[ChartComponent.MAP_NAME].getName();
              }
            } else if ( outputName.equals( ChartComponent.HTML_MAPPING_HTML ) ) {

              outputValue = mapString;

            } else if ( outputName.equals( ChartComponent.BASE_URL_OUTPUT ) ) {
              IPentahoRequestContext requestContext = PentahoRequestContextHolder.getRequestContext();
              outputValue = requestContext.getContextPath();

            } else if ( outputName.equals( ChartComponent.CONTEXT_PATH_OUTPUT ) ) {
              IPentahoRequestContext requestContext = PentahoRequestContextHolder.getRequestContext();
              outputValue = requestContext.getContextPath();
            } else if ( outputName.equals( ChartComponent.FULLY_QUALIFIED_SERVER_URL_OUTPUT ) ) {

              IApplicationContext applicationContext = PentahoSystem.getApplicationContext();
              if ( applicationContext != null ) {
                outputValue = applicationContext.getFullyQualifiedServerURL();
              } else {
                IPentahoRequestContext requestContext = PentahoRequestContextHolder.getRequestContext();
                outputValue = requestContext.getContextPath();
              }
            } else if ( outputName.equals( ChartComponent.HTML_IMG_TAG ) ) {

              outputValue = hasTemplate ? mapString : ""; //$NON-NLS-1$

              outputValue += "<img border=\"0\" "; //$NON-NLS-1$
              outputValue += "width=\"" + width + "\" "; //$NON-NLS-1$//$NON-NLS-2$
              outputValue += "height=\"" + height + "\" "; //$NON-NLS-1$//$NON-NLS-2$
              if ( hasTemplate ) {
                outputValue +=
                    "usemap=\"#" + fileResults[ChartComponent.MAP_NAME].getName().substring( 0,
                      fileResults[ ChartComponent.MAP_NAME ].getName().indexOf( '.' ) ) + "\" "; //$NON-NLS-1$//$NON-NLS-2$
              }
              IPentahoRequestContext requestContext = PentahoRequestContextHolder.getRequestContext();
              String contextPath = requestContext.getContextPath();
              outputValue +=
                  "src=\"" + contextPath + "getImage?image=" + fileResults[ChartComponent.FILE_NAME].getName() + "\"/>"; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$

            }

            if ( outputValue != null ) {
              setOutputValue( outputName, outputValue );
            }
          }
        }

        break;

      /************************** OUTPUT_CHART && DEFAULT *************************************/
      case JFreeChartEngine.OUTPUT_CHART:
        // intentionally fall through to default

      default:

        String chartName = ChartComponent.CHART_OUTPUT;
        if ( isDefinedInput( ChartComponent.CHART_NAME_PROP ) ) {
          chartName = getInputStringValue( ChartComponent.CHART_NAME_PROP );
        }
        chart = JFreeChartEngine.getChart( dataDefinition, title, "", width, height, this ); //$NON-NLS-1$
        setOutputValue( chartName, chart );

        break;
    }

    return true;
  }

  @Override
  public boolean init() {
    // nothing to do here really
    return true;
  }

  /**
   * @return String that represents the file path to a temporary file
   */
  protected File[] createTempFile( final int outputType, final boolean includeMapFile, boolean trackFile ) {
    File[] results;
    if ( includeMapFile ) {
      results = new File[2];
    } else {
      results = new File[1];
    }

    String extension =
        outputType == JFreeChartEngine.OUTPUT_SVG ? ChartComponent.SVG_EXTENSION : ChartComponent.PNG_EXTENSION;

    try {
      File file =
          PentahoSystem.getApplicationContext().createTempFile( getSession(), ChartComponent.FILENAME_PREFIX,
            extension, trackFile );
      results[ChartComponent.FILE_NAME] = file;
      if ( includeMapFile ) {
        file =
            PentahoSystem.getApplicationContext().createTempFile( getSession(), ChartComponent.FILENAME_PREFIX,
              ChartComponent.MAP_EXTENSION, trackFile );
        results[ChartComponent.MAP_NAME] = file;
      }
    } catch ( IOException e ) {
      return null;
    }
    return results;
  }
}
