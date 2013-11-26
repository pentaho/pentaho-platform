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
import org.pentaho.chart.AbstractChartThemeFactory;
import org.pentaho.chart.ChartBeanFactory;
import org.pentaho.chart.ChartBoot;
import org.pentaho.chart.IChartLinkGenerator;
import org.pentaho.chart.model.ChartModel;
import org.pentaho.chart.model.DialPlot;
import org.pentaho.chart.model.PiePlot;
import org.pentaho.chart.model.Theme;
import org.pentaho.chart.model.util.ChartSerializer;
import org.pentaho.chart.model.util.ChartSerializer.ChartSerializationFormat;
import org.pentaho.chart.plugin.ChartDataOverflowException;
import org.pentaho.chart.plugin.NoChartDataException;
import org.pentaho.chart.plugin.api.IOutput.OutputTypes;
import org.pentaho.chart.plugin.jfreechart.JFreeChartPlugin;
import org.pentaho.chart.plugin.openflashchart.OpenFlashChartPlugin;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.action.IStreamingAction;
import org.pentaho.platform.api.engine.IPentahoRequestContext;
import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.util.messages.LocaleHelper;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This class was adapted from {@link ChartComponent} by cboyden
 * <p/>
 * This is a bean that permits easy access to the ChartBeans functionality and was specifically designed to be run from
 * within the Pentaho Platform as an Action Sequence Action.
 *
 * @author cboyden
 * @author aphillips
 */
public class ChartAction implements IStreamingAction {
  protected static final int DEFAULT_CHART_WIDTH = 400;

  protected static final int DEFAULT_CHART_HEIGHT = 300;

  protected boolean convertNullsToZero = false;

  protected String seriesColumnName = null;

  protected int seriesColumn = -1;

  protected String categoryColumnName = null;

  protected int categoryColumn = -1;

  protected String valueColumnName = null;

  protected int valueColumn = -1;

  protected IPentahoResultSet chartData = null;

  protected String chartEngine;

  protected Exception bootException = null;

  protected String outputType = ""; //$NON-NLS-1$

  protected int chartWidth = -1;

  protected int chartHeight = -1;

  protected String contentLinkingTemplate;

  protected Number scalingFactor = new Double( 1 );

  protected OutputStream chartContentStream = null;

  protected String chartModelJson = null;

  protected String chartModelXml = null;

  protected ChartModel chartModel = null;

  protected String title = null;

  //  private String flashPath = "openflashchart"; //$NON-NLS-1$

  //  private String flashSwf = "open-flash-chart-full-embedded-font.swf"; //$NON-NLS-1$s

  /**
   * Initialize ChartBeans engine
   */ {
    synchronized ( ChartBoot.getInstance() ) {
      while ( !ChartBoot.getInstance().isBootDone() ) {
        if ( ChartBoot.getInstance().isBootInProgress() ) {
          // Wait 1 second
          try {
            java.lang.Thread.sleep( 1000 );
          } catch ( InterruptedException e ) {
            // Do nothing
          }
        } else {
          if ( !ChartBoot.getInstance().isBootFailed() ) {
            ChartBoot.getInstance().start();
          }
        }
      } // End while: boot is not done

      // Check for an error
      if ( ChartBoot.getInstance().isBootFailed() ) {
        bootException = ChartBoot.getInstance().getBootFailureReason();
      }
    } // End thread synchronization
  }

  /**
   * Called to process the chart definition and data set to produce a usable chart.
   *
   * @throws ChartBootException
   * @throws ChartProcessingException
   * @throws ResourceException
   * @throws InvalidChartDefinition
   * @throws IOException
   * @throws PersistenceException
   * @see org.pentaho.platform.api.action.IAction#execute()
   */
  public void execute() throws Exception {

    //
    // Runtime value validation is now part of the execute operation
    //
    validate();

    if ( bootException != null ) {
      throw new ChartBootException( bootException );
    }

    // Transform IPentahoResultSet to an object array

    Object[][] data = processChartData( chartData, valueColumn );

    if ( chartModel.getTheme() != null ) {
      AbstractChartThemeFactory chartThemeFactory = new AbstractChartThemeFactory() {
        protected List<File> getThemeFiles() {
          ArrayList<File> themeFiles = new ArrayList<File>();
          themeFiles.add( new File( PentahoSystem.getApplicationContext().getSolutionPath(
            "system/chartbeans/themes/Theme1.xml" ) ) ); //$NON-NLS-1$
          themeFiles.add( new File( PentahoSystem.getApplicationContext().getSolutionPath(
            "system/chartbeans/themes/Theme2.xml" ) ) ); //$NON-NLS-1$
          themeFiles.add( new File( PentahoSystem.getApplicationContext().getSolutionPath(
            "system/chartbeans/themes/Theme3.xml" ) ) ); //$NON-NLS-1$
          themeFiles.add( new File( PentahoSystem.getApplicationContext().getSolutionPath(
            "system/chartbeans/themes/Theme4.xml" ) ) ); //$NON-NLS-1$
          themeFiles.add( new File( PentahoSystem.getApplicationContext().getSolutionPath(
            "system/chartbeans/themes/Theme5.xml" ) ) ); //$NON-NLS-1$
          themeFiles.add( new File( PentahoSystem.getApplicationContext().getSolutionPath(
            "system/chartbeans/themes/Theme6.xml" ) ) ); //$NON-NLS-1$
          return themeFiles;
        }
      };

      if ( !( chartModel.getPlot() instanceof DialPlot ) ) {
        Theme chartTheme = chartThemeFactory.getTheme( chartModel.getTheme() );
        if ( chartTheme != null ) {
          chartTheme.applyTo( chartModel );
        }
      }
    }

    // Make sure chart engine is loaded
    loadChartEngine();
    // Set chart engine on chartModel for the ChartFactory to use
    chartModel.setChartEngineId( chartEngine );

    InputStream is = null;

    try {
      IChartLinkGenerator chartLinkGenerator =
        contentLinkingTemplate == null ? null : new ChartLinkGenerator( contentLinkingTemplate );
      is =
        ChartBeanFactory.createChart( data, scalingFactor, convertNullsToZero, valueColumn, seriesColumn,
          categoryColumn, chartModel, chartLinkGenerator, chartWidth, chartHeight, getOutputType() );
      // Wrap output as necessary
      if ( OpenFlashChartPlugin.PLUGIN_ID.equals( chartEngine ) ) {
        // Convert stream to string, insert into HTML fragment and re-stream it
        StringBuilder sb = new StringBuilder();
        int c = 0;

        // Build string
        while ( ( c = is.read() ) >= 0 ) {
          sb.append( (char) c );
        }

        IPentahoRequestContext requestContext = PentahoRequestContextHolder.getRequestContext();
        String contextPath = requestContext.getContextPath();

        String flashContent =
          ChartBeansGeneratorUtil
            .mergeOpenFlashChartHtmlTemplate( sb.toString().replaceAll( "\"", "\\\\\"" ), //$NON-NLS-1$ //$NON-NLS-2$
              contextPath + "/" + this.getSwfPath() + "/" + getSwfName() ); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$

        is = new ByteArrayInputStream( flashContent.getBytes( "utf-8" ) ); //$NON-NLS-1$
      }

      int val = 0;

      // TODO: Buffer for more efficiency
      while ( ( val = is.read() ) != -1 ) {
        chartContentStream.write( val );
      }
    } catch ( NoChartDataException ex ) {
      if ( JFreeChartPlugin.PLUGIN_ID.equals( chartEngine ) ) {
        BufferedImage image = new BufferedImage( chartWidth, chartHeight, BufferedImage.TYPE_INT_ARGB );
        Graphics2D graphics = image.createGraphics();
        graphics.setFont( new Font( "serif", Font.BOLD, 14 ) ); //$NON-NLS-1$
        graphics.setColor( Color.BLACK );
        graphics.drawString( "The chart data query returned no data.", 5, 5 ); //$NON-NLS-1$
        String outputType =
          getMimeType( null ).equals( "image/jpg" ) ? "jpeg" : "png"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        ImageIO.write( image, outputType, chartContentStream );
      } else {
        String flashContent =
          ChartBeansGeneratorUtil
            .buildEmptyOpenFlashChartHtmlFragment( "The chart data query returned no data." ); //$NON-NLS-1$
        is = new ByteArrayInputStream( flashContent.getBytes( "utf-8" ) ); //$NON-NLS-1$
        int val = 0;
        // TODO: Buffer for more efficiency
        while ( ( val = is.read() ) != -1 ) {
          chartContentStream.write( val );
        }
      }
    } catch ( ChartDataOverflowException ex ) {
      if ( JFreeChartPlugin.PLUGIN_ID.equals( chartEngine ) ) {
        BufferedImage image = new BufferedImage( chartWidth, chartHeight, BufferedImage.TYPE_INT_ARGB );
        Graphics2D graphics = image.createGraphics();
        graphics.setFont( new Font( "serif", Font.BOLD, 14 ) ); //$NON-NLS-1$
        graphics.setColor( Color.BLACK );
        graphics.drawString( Messages.getInstance().getErrorString( "ChartAction.TOO_MANY_DATA_POINTS" ), 5,
          5 ); //$NON-NLS-1$
        graphics.drawString( Messages.getInstance().getErrorString(
          "ChartAction.MAX_ALLOWED_DATA_POINTS", Integer.toString( ex.getMaxAllowedDataPoints() ) ), 5,
          25 ); //$NON-NLS-1$

        String outputType =
          getMimeType( null ).equals( "image/jpg" ) ? "jpeg" : "png"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        ImageIO.write( image, outputType, chartContentStream );
      } else {
        String flashContent =
          ChartBeansGeneratorUtil.buildEmptyOpenFlashChartHtmlFragment( Messages.getInstance().getErrorString(
            "ChartAction.TOO_MANY_DATA_POINTS_HTML", Integer.toString( ex.getMaxAllowedDataPoints() ) ) ); //$NON-NLS-1$
        is = new ByteArrayInputStream( flashContent.getBytes( "utf-8" ) ); //$NON-NLS-1$
        int val = 0;
        // TODO: Buffer for more efficiency
        while ( ( val = is.read() ) != -1 ) {
          chartContentStream.write( val );
        }
      }
    }
  }

  public void setContentLinkingTemplate( String template ) {
    contentLinkingTemplate = template;
  }

  /**
   * Transform the IPentahoResultSet into the data format suitable for chart creation.
   *
   * @return Row / Column data table or null
   */
  protected Object[][] processChartData( IPentahoResultSet resultSet, int valueColumnIndex ) {
    if ( resultSet == null ) {
      return null;
    }

    Object[][] result = null;

    result = new Object[ resultSet.getRowCount() ][ resultSet.getMetaData().getColumnCount() ];

    for ( int r = 0; r < resultSet.getRowCount(); r++ ) {
      for ( int c = 0; c < resultSet.getMetaData().getColumnCount(); c++ ) {
        result[ r ][ c ] = resultSet.getValueAt( r, c );
      }
    }

    return ( result );
  }

  public void setOutputStream( OutputStream outputStream ) {
    setChartContentStream( outputStream );
  }

  /**
   * Define the OutputStream to which the resulting chart shall be written
   *
   * @param outStream Stream to receive the chart
   */
  public void setChartContentStream( OutputStream outStream ) {
    chartContentStream = outStream;
  }

  /**
   * Define the data set that will populate the chart
   *
   * @param chartDataSet data set for charting
   */
  public void setChartData( IPentahoResultSet chartDataSet ) {
    chartData = chartDataSet.memoryCopy();
  }

  /**
   * Validate the current settings of the ChartAction. If validate() returns true, then execute may be called. If
   * validate() returns false, a call to execute() is guaranteed to fail.
   *
   * @return state of validation
   * @throws Exception
   */
  public void validate() throws Exception {
    // Must have a valid result set
    if ( chartData == null ) {
      throw new IllegalArgumentException( Messages.getInstance().getErrorString(
        "ChartComponent.ERROR_0006_PARAM_NOT_SET", "chartData" ) ); //$NON-NLS-1$//$NON-NLS-2$
    }

    if ( chartContentStream == null ) {
      throw new IllegalArgumentException( Messages.getInstance().getErrorString(
        "ChartComponent.ERROR_0006_PARAM_NOT_SET", "chartContentStream" ) ); //$NON-NLS-1$//$NON-NLS-2$
    }

    // Default to the first three columns if no others are explicitly specified
    // Resolve column name to column ordinal if present
    if ( seriesColumnName != null ) {
      // Leave it at -1 if it is specified as blank (The charting engine will handle this properly)
      if ( !seriesColumnName.equals( "" ) ) { //$NON-NLS-1$
        seriesColumn = chartData.getMetaData().getColumnIndex( seriesColumnName );
      }
    } else {
      // Set default ordering as no ordinal has been defined
      if ( seriesColumn < 0 ) {
        seriesColumn = 0;
      }
    }

    if ( categoryColumnName != null ) {
      // Leave it at -1 if it is specified as blank (The charting engine will handle this properly)
      if ( !categoryColumnName.equals( "" ) ) { //$NON-NLS-1$
        categoryColumn = chartData.getMetaData().getColumnIndex( categoryColumnName );
      }
    } else {
      // Set default ordering as no ordinal has been defined
      if ( categoryColumn < 0 ) {
        categoryColumn = 1;
      }
    }
    if ( valueColumnName != null ) {
      // Leave it at -1 if it is specified as blank (The charting engine will handle this properly)
      if ( !valueColumnName.equals( "" ) ) { //$NON-NLS-1$
        valueColumn = chartData.getMetaData().getColumnIndex( valueColumnName );
      }
    } else {
      // Set default ordering as no ordinal has been defined
      if ( valueColumn < 0 ) {
        valueColumn = 2;
      }
    }

    loadChartEngine();

    if ( chartModel == null ) {
      throw new IllegalArgumentException( Messages.getInstance().getErrorString(
        "ChartComponent.ERROR_0006_PARAM_NOT_SET", "chartModel" ) ); //$NON-NLS-1$//$NON-NLS-2$
    }

    // Verify that all columns required for a given chart type are present
    if ( chartModel.getPlot() instanceof DialPlot ) {
      if ( valueColumn < 0 ) {
        throw new IllegalArgumentException( Messages.getInstance().getErrorString(
          "ChartComponent.ERROR_0007_PARAM_VALUE_CANNOT_BE_NEGATIVE", "valueColumn",
          DialPlot.class.getSimpleName() ) ); //$NON-NLS-1$//$NON-NLS-2$
      }
    } else if ( chartModel.getPlot() instanceof PiePlot ) {
      if ( ( seriesColumn < 0 ) || ( valueColumn < 0 ) ) {
        throw new IllegalArgumentException(
          Messages
            .getInstance()
            .getErrorString(
              "ChartComponent.ERROR_0007_PARAM_VALUE_CANNOT_BE_NEGATIVE", "valueColumn or seriesColumn",
              PiePlot.class.getSimpleName() ) ); //$NON-NLS-1$//$NON-NLS-2$
      }
    } else {
      if ( ( seriesColumn < 0 ) || ( categoryColumn < 0 ) || ( valueColumn < 0 ) ) {
        throw new IllegalArgumentException(
          Messages
            .getInstance()
            .getErrorString(
              "ChartComponent.ERROR_0007_PARAM_VALUE_CANNOT_BE_NEGATIVE",
              "seriesColumn, categoryColumn, or valueColumn", "All" ) ); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
      }
    }

    if ( chartWidth <= 0 ) {
      chartWidth = DEFAULT_CHART_WIDTH;
    }

    if ( chartHeight <= 0 ) {
      chartHeight = DEFAULT_CHART_HEIGHT;
    }
  }

  /**
   * Define the column in the data set that contains the Series/Domain data
   *
   * @param seriesCol name of column that contains the Series/Domain for the chart
   */
  public void setSeriesColumn( String seriesCol ) {
    seriesColumnName = seriesCol;
  }

  public void setConvertNullsToZero( boolean convert ) {
    this.convertNullsToZero = convert;
  }

  public boolean getConvertNullsToZero() {
    return convertNullsToZero;
  }

  /**
   * Define the column in the data set that contains the Category data
   *
   * @param seriesCol name of column that contains the Category for the chart
   */
  public void setCategoryColumn( String categoryCol ) {
    categoryColumnName = categoryCol;
  }

  /**
   * Define the column in the data set that contains the Value/Range data
   *
   * @param seriesCol name of column that contains the Value/Range for the chart
   */
  public void setValueColumn( String valueCol ) {
    valueColumnName = valueCol;
  }

  /**
   * Fetch the desired output type
   *
   * @return output type
   */
  protected OutputTypes getOutputType() {
    if ( outputType.equals( "jpg" ) ) { //$NON-NLS-1$
      return OutputTypes.FILE_TYPE_JPEG;
    } else if ( outputType.equals( "png" ) ) { //$NON-NLS-1$
      return OutputTypes.FILE_TYPE_PNG;
    }

    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.api.action.IStreamingAction#getMimeType(java.lang.String)
   */
  public String getMimeType( String streamPropertyName ) {
    loadChartEngine();

    if ( JFreeChartPlugin.PLUGIN_ID.equals( chartEngine ) ) {
      if ( outputType.equalsIgnoreCase( "jpg" ) ) { //$NON-NLS-1$
        return "image/jpg"; //$NON-NLS-1$
      } else if ( outputType.equalsIgnoreCase( "png" ) ) { //$NON-NLS-1$
        return "image/png"; //$NON-NLS-1$
      }

      // Default JFREE action
      outputType = "png"; //$NON-NLS-1$
      return "image/png"; //$NON-NLS-1$
    } else if ( OpenFlashChartPlugin.PLUGIN_ID.equals( chartEngine ) ) {
      outputType = "html"; //$NON-NLS-1$
      return "text/html"; //$NON-NLS-1$
    }

    // Final component default is OFC
    return "text/html"; //$NON-NLS-1$
  }

  /**
   * Sets the chart engine based on the order of precedence: 1) Chart Definition 2) Action Sequence 3) System Setting 4)
   * Hard Coded
   */
  protected void loadChartEngine() {
    loadChartModel();

    if ( chartModel != null ) {
      if ( chartModel.getChartEngineId() != null ) {
        this.chartEngine = chartModel.getChartEngineId();
        // Defined in ChartModel, escape
        return;
      }
    }

    if ( this.chartEngine != null ) {
      // Engine set on Action Sequence, escape
      return;
    }

    // Load default value from system setting or take hard coded

    // Hard coded final fall back is Open Flash Chart
    String defaultChartEngine =
      PentahoSystem.getSystemSetting(
        "chartbeans/chartbeans_config.xml", "default-chart-engine",
        OpenFlashChartPlugin.PLUGIN_ID ); //$NON-NLS-1$ //$NON-NLS-2$

    if ( defaultChartEngine == null ) {
      defaultChartEngine = OpenFlashChartPlugin.PLUGIN_ID;
    }

    this.chartEngine = defaultChartEngine;
  }

  protected void loadChartModel() {
    if ( chartModel == null ) {
      if ( chartModelJson != null ) {
        chartModel = ChartSerializer.deSerialize( chartModelJson, ChartSerializationFormat.JSON );
      } else {
        if ( chartModelXml != null ) {
          chartModel = ChartSerializer.deSerialize( chartModelXml, ChartSerializationFormat.XML );
        }
      }
    }
  }

  /**
   * Set the JSON representation of the ChartModel
   *
   * @param chartModelJson JSON serialized representation of the ChartModel
   */
  public void setChartModelJson( String chartModelJson ) {
    this.chartModelJson = chartModelJson;
  }

  /**
   * Set the XML representation of the ChartModel as an InputStream
   *
   * @param chartModelStream XML serialized representation of the ChartModel
   * @throws IOException if there is a problem converting the input stream to an encoded string
   */
  public void setChartModelXmlStream( InputStream chartModelStream ) throws IOException {
    chartModelXml = IOUtils.toString( chartModelStream, LocaleHelper.getSystemEncoding() );
  }

  /**
   * Set the XML representation of the ChartModel
   *
   * @param chartStyleXml XML serialized representation of the ChartModel
   */
  public void setChartModelXml( String chartModelXml ) {
    this.chartModelXml = chartModelXml;
  }

  /**
   * Set the ChartModel
   *
   * @param chartModel model of the chart to be generated
   */
  public void setChartModel( ChartModel chartModel ) {
    this.chartModel = chartModel;
  }

  /**
   * Set the width of the chart in units specific to the ChartPlugin
   *
   * @param chartWidth width of the chart
   */
  public void setChartWidth( int chartWidth ) {
    this.chartWidth = chartWidth;
  }

  /**
   * Set the height of the chart in units specific to the ChartPlugin
   *
   * @param chartHeight height of the chart
   */
  public void setChartHeight( int chartHeight ) {
    this.chartHeight = chartHeight;
  }

  /**
   * Set the width of the chart in units specific to the ChartPlugin
   *
   * @param chartWidth width of the chart
   */
  public void setChartWidth( String chartWidth ) {
    this.chartWidth = Integer.valueOf( chartWidth );
  }

  /**
   * Set the height of the chart in units specific to the ChartPlugin
   *
   * @param chartHeight height of the chart
   */
  public void setChartHeight( String chartHeight ) {
    this.chartHeight = Integer.valueOf( chartHeight );
  }

  /**
   * Get the chart engine that the resulting chart was created through
   *
   * @return
   */
  public String getChartEngine() {
    loadChartEngine();
    return chartEngine;
  }

  /**
   * Set the chart engine to render the chart
   *
   * @param chartEngine Value of "JFreeChart" or "OpenFlashChart"
   */
  public void setChartEngine( String chartEngine ) {
    this.chartEngine = chartEngine;
  }

  public void setOutputType( String outputType ) {
    this.outputType = outputType;
  }

  public void setScalingFactor( Double scalingFactor ) {
    this.scalingFactor = scalingFactor;
  }

  public String getSwfPath() {
    return "openflashchart"; //$NON-NLS-1$
  }

  public String getSwfName() {
    return "open-flash-chart-full-embedded-font.swf"; //$NON-NLS-1$
  }
}
