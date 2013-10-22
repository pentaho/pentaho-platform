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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Node;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.Minute;
import org.jfree.data.time.Month;
import org.jfree.data.time.Quarter;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.time.Week;
import org.jfree.data.time.Year;
import org.jfree.ui.RectangleEdge;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.uifoundation.messages.Messages;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Paint;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class TimeSeriesCollectionChartDefinition extends TimeSeriesCollection implements XYChartDefinition {

  public static final String DATE_MINIMUM_NODE_NAME = "date-minimum"; //$NON-NLS-1$

  public static final String DATE_MAXIMUM_NODE_NAME = "date-maximum"; //$NON-NLS-1$

  private int chartType = JFreeChartEngine.UNDEFINED_CHART_TYPE;

  private String noDataMessage = null;

  // JFreeChart Customizations
  private String title = ""; //$NON-NLS-1$

  private RectangleEdge titlePosition = RectangleEdge.TOP;

  private RectangleEdge legendPosition = RectangleEdge.BOTTOM;

  private Font titleFont = TextTitle.DEFAULT_FONT;

  private final List subTitles = new ArrayList();

  private Paint chartBackgroundPaint = Color.WHITE;

  private Image chartBackgroundImage = null;

  private boolean borderVisible = false;

  private Paint borderPaint = Color.BLACK;

  private int width = 200;

  private int height = 200;

  private int dotWidth = 5;

  private int dotHeight = 5;

  // Plot Customizations
  private PlotOrientation orientation = PlotOrientation.VERTICAL;

  private Paint plotBackgroundPaint = Color.WHITE;

  private Image plotBackgroundImage = null;

  private boolean legendIncluded = true;

  private boolean threeD = false;

  private boolean stacked = false;

  private boolean domainVerticalTickLabels = false;

  private String domainPeriodType = "Millisecond"; //$NON-NLS-1$

  private Paint[] paintSequence = null;

  private String domainTitle = null;

  private Font domainTitleFont = TextTitle.DEFAULT_FONT;

  private String rangeTitle = null;

  private Font rangeTitleFont = TextTitle.DEFAULT_FONT;

  private double rangeMinimum = ValueAxis.DEFAULT_LOWER_BOUND;

  private double rangeMaximum = ValueAxis.DEFAULT_UPPER_BOUND;

  private Date dateMinimum = null;

  private Date dateMaximum = null;

  private Font legendFont = null;

  private boolean legendBorderVisible = true;

  private String lineStyle = ChartDefinition.LINE_STYLE_SOLID_STR;

  private float lineWidth = 1.0f;

  private Float backgroundAlpha;

  private Float foregroundAlpha;

  private boolean markersVisible = false;

  // in JFreeChart, the tokens stand for:
  // {0} - the series name
  // {1} - the preformatted x-value
  // {2} - the preformatted y-value
  private String tooltipContent = "{1}, {2}"; //$NON-NLS-1$

  private String tooltipYFormat = "0.0"; //$NON-NLS-1$

  private String tooltipXFormat = "d-MMM-yyyy"; //$NON-NLS-1$

  // Other stuff
  private IPentahoSession session;

  private static final long serialVersionUID = 1717509132920946530L;

  public TimeSeriesCollectionChartDefinition( final IPentahoSession session ) {
    super();
    this.session = session;
  }

  public TimeSeriesCollectionChartDefinition( final int chartType, final IPentahoResultSet data, final boolean byRow,
      final Node chartAttributes, final IPentahoSession session ) {
    this( session );
    this.chartType = chartType;
    setChartAttributes( chartAttributes );
    if ( byRow ) {
      setDataByRow( data );
    } else {
      setDataByColumn( data );
    }
  }

  public TimeSeriesCollectionChartDefinition( final IPentahoResultSet data, final boolean byRow,
      final Node chartAttributes, final IPentahoSession session ) {
    this( JFreeChartEngine.UNDEFINED_CHART_TYPE, data, byRow, chartAttributes, session );
  }

  public Log getLogger() {
    return LogFactory.getLog( TimeSeriesCollectionChartDefinition.class );
  }

  private void setChartAttributes( final Node chartAttributes ) {
    if ( chartAttributes == null ) {
      return;
    }
    // get the chart type from the chart node -- this overrides the current
    // chart type
    setChartType( chartAttributes.selectSingleNode( ChartDefinition.TYPE_NODE_NAME ) );

    // set the chart background
    setChartBackground( chartAttributes.selectSingleNode( ChartDefinition.CHART_BACKGROUND_NODE_NAME ) );

    // set the plot background
    setPlotBackground( chartAttributes.selectSingleNode( ChartDefinition.PLOT_BACKGROUND_NODE_NAME ) );

    // set the orientation
    setOrientation( chartAttributes.selectSingleNode( XYChartDefinition.ORIENTATION_NODE_NAME ) );

    // do we want a legend
    setLegendIncluded( chartAttributes.selectSingleNode( ChartDefinition.INCLUDE_LEGEND_NODE_NAME ) );

    // get the chart title
    setTitle( chartAttributes.selectSingleNode( ChartDefinition.TITLE_NODE_NAME ) );

    // A list of <subtitle> nodes should not be allowed to exist as a child of the main XML element (for XML schema
    // to
    // be well constructed and validate the XML .
    // We have deprecated <subtitle> as a child of the main node , and now require a <subtitles> parent node
    // under which <subtitle> can exist.

    List subtitles = chartAttributes.selectNodes( ChartDefinition.SUBTITLE_NODE_NAME );

    if ( ( subtitles == null ) || ( subtitles.isEmpty() ) ) {
      Node subTitlesNode = chartAttributes.selectSingleNode( ChartDefinition.SUBTITLES_NODE_NAME );
      if ( subTitlesNode != null ) {
        subtitles = subTitlesNode.selectNodes( ChartDefinition.SUBTITLE_NODE_NAME );
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
      addSubTitles( subtitles );
    }

    // get the paint sequence
    setPaintSequence( chartAttributes.selectSingleNode( ChartDefinition.PALETTE_NODE_NAME ) );
    Node backgroundAlphaNode = chartAttributes.selectSingleNode( ChartDefinition.BACKGROUND_ALPHA_NODE_NAME );
    Node foregroundAlphaNode = chartAttributes.selectSingleNode( ChartDefinition.FOREGROUND_ALPHA_NODE_NAME );

    if ( backgroundAlphaNode != null ) {
      setBackgroundAlpha( chartAttributes.selectSingleNode( ChartDefinition.BACKGROUND_ALPHA_NODE_NAME ) );
    }
    if ( foregroundAlphaNode != null ) {
      setForegroundAlpha( chartAttributes.selectSingleNode( ChartDefinition.FOREGROUND_ALPHA_NODE_NAME ) );
    }
    // get the stacked value
    setStacked( chartAttributes.selectSingleNode( ChartDefinition.STACKED_NODE_NAME ) );

    // get the 3D value
    setThreeD( chartAttributes.selectSingleNode( ChartDefinition.THREED_NODE_NAME ) );

    // set the width
    setWidth( chartAttributes.selectSingleNode( ChartDefinition.WIDTH_NODE_NAME ) );

    // set the height
    setHeight( chartAttributes.selectSingleNode( ChartDefinition.HEIGHT_NODE_NAME ) );

    // set the dot width
    setDotWidth( chartAttributes.selectSingleNode( ChartDefinition.DOT_WIDTH_NODE_NAME ) );

    // set the dot height
    setDotHeight( chartAttributes.selectSingleNode( ChartDefinition.DOT_HEIGHT_NODE_NAME ) );

    // set vertical tick labels flag
    setDomainVerticalTickLabels( chartAttributes
        .selectSingleNode( XYChartDefinition.DOMAIN_VERTICAL_TICK_LABELS_NODE_NAME ) );

    // set the border on or off
    setBorderVisible( chartAttributes.selectSingleNode( ChartDefinition.CHART_BORDER_VISIBLE_NODE_NAME ) );

    // set the border Paint
    setBorderPaint( JFreeChartEngine.getPaint( chartAttributes
        .selectSingleNode( XYChartDefinition.CHART_BORDER_PAINT_NODE_NAME ) ) );

    // set the title location
    setTitlePosition( chartAttributes.selectSingleNode( ChartDefinition.TITLE_POSITION_NODE_NAME ) );

    // set the legend location
    setLegendPosition( chartAttributes.selectSingleNode( ChartDefinition.LEGEND_POSITION_NODE_NAME ) );

    // set the title font
    setTitleFont( chartAttributes.selectSingleNode( ChartDefinition.TITLE_FONT_NODE_NAME ) );

    // set the domain title
    setDomainTitle( chartAttributes.selectSingleNode( XYChartDefinition.DOMAIN_TITLE_NODE_NAME ) );

    // set the domain font
    setDomainTitleFont( chartAttributes.selectSingleNode( XYChartDefinition.DOMAIN_TITLE_FONT_NODE_NAME ) );

    // set the range title
    setRangeTitle( chartAttributes.selectSingleNode( XYChartDefinition.RANGE_TITLE_NODE_NAME ) );

    // the the range font
    setRangeTitleFont( chartAttributes.selectSingleNode( XYChartDefinition.RANGE_TITLE_FONT_NODE_NAME ) );

    // set the range minimum
    setRangeMinimum( chartAttributes.selectSingleNode( XYChartDefinition.RANGE_MINIMUM_NODE_NAME ) );

    // set the range minimum
    setRangeMaximum( chartAttributes.selectSingleNode( XYChartDefinition.RANGE_MAXIMUM_NODE_NAME ) );

    // set the date minimum
    setDateMinimum( chartAttributes.selectSingleNode( TimeSeriesCollectionChartDefinition.DATE_MINIMUM_NODE_NAME ) );

    // set the date minimum
    setDateMaximum( chartAttributes.selectSingleNode( TimeSeriesCollectionChartDefinition.DATE_MAXIMUM_NODE_NAME ) );

    // set the Period type
    setDomainPeriodType( chartAttributes.selectSingleNode( XYChartDefinition.DOMAIN_PERIOD_TYPE_NODE_NAME ) );

    // set the line style
    setLineStyle( chartAttributes.selectSingleNode( ChartDefinition.LINE_STYLE_NODE_NAME ) );

    // set the line width
    setLineWidth( chartAttributes.selectSingleNode( ChartDefinition.LINE_WIDTH_NODE_NAME ) );

    // set the marker visibility
    setMarkersVisible( chartAttributes.selectSingleNode( ChartDefinition.MARKER_VISIBLE_NODE_NAME ) );

    // set legend font
    setLegendFont( chartAttributes.selectSingleNode( ChartDefinition.LEGEND_FONT_NODE_NAME ) );

    // set legend border visible
    setLegendBorderVisible( chartAttributes.selectSingleNode( ChartDefinition.DISPLAY_LEGEND_BORDER_NODE_NAME ) );

    setTooltipContent( chartAttributes.selectSingleNode( XYChartDefinition.TOOLTIP_CONTENT_NODE_NAME ) );

    setTooltipYFormat( chartAttributes.selectSingleNode( XYChartDefinition.TOOLTIP_Y_FORMAT_NODE_NAME ) );

    setTooltipXFormat( chartAttributes.selectSingleNode( XYChartDefinition.TOOLTIP_X_FORMAT_NODE_NAME ) );
  }

  private void setDataByColumn( final IPentahoResultSet data ) {
    // TODO Make this routine MDX friendly
    if ( data == null ) {
      noDataMessage = Messages.getInstance().getString( "CHART.USER_NO_DATA_AVAILABLE" ); //$NON-NLS-1$
      return; // No data so we've got nothing to set
      // TODO come up with some sort of error strategy here.
    }
    boolean firstPass = true;
    String lastSeries = ""; //$NON-NLS-1$
    String seriesName = ""; //$NON-NLS-1$
    Class timePeriodClass = TimeSeriesCollectionChartDefinition.getTimePeriodClass( getDomainPeriodType() );
    Object[] rowData = data.next();
    TimeSeries wrkSeries = null;
    while ( rowData != null ) {
      seriesName = (String) rowData[0];
      if ( firstPass || !seriesName.equalsIgnoreCase( lastSeries ) ) {
        if ( !firstPass ) {
          addSeries( wrkSeries );
        }
        wrkSeries = new TimeSeries( seriesName, timePeriodClass );
        lastSeries = seriesName;
        firstPass = false;
      }
      Date keyDate = getValidDate( rowData[1] );
      RegularTimePeriod regularTimePeriod =
          RegularTimePeriod.createInstance( timePeriodClass, keyDate, RegularTimePeriod.DEFAULT_TIME_ZONE );
      TimeSeriesDataItem timeSeriesDataItem =
          new TimeSeriesDataItem( regularTimePeriod, ( (Number) rowData[2] ).doubleValue() );
      if ( wrkSeries != null ) {
        wrkSeries.add( timeSeriesDataItem );
      }
      rowData = data.next();
    }
    if ( !firstPass ) {
      addSeries( wrkSeries );
    }
    if ( ( data.getRowCount() > 0 ) && ( this.getSeriesCount() <= 0 ) ) {
      noDataMessage = Messages.getInstance().getString( "CHART.USER_INCORRECT_DATA_FORMAT" ); //$NON-NLS-1$
    }

  }

  private void setDataByRow( final IPentahoResultSet data ) {
    // TODO Make this routine MDX friendly
    if ( data == null ) {
      noDataMessage = Messages.getInstance().getString( "CHART.USER_NO_DATA_AVAILABLE" ); //$NON-NLS-1$
      return; // No data so we've got nothing to set
      // TODO come up with some sort of error strategy here.
    }
    Class timePeriodClass = TimeSeriesCollectionChartDefinition.getTimePeriodClass( getDomainPeriodType() );
    Object[] rowData = data.next();
    while ( rowData != null ) {
      String seriesName = (String) rowData[0];
      TimeSeries wrkSeries = new TimeSeries( seriesName, timePeriodClass );
      for ( int column = 1; column < rowData.length - 1; column = column + 2 ) {
        Date keyDate = getValidDate( rowData[column] );
        TimeSeriesDataItem timeSeriesDataItem =
            new TimeSeriesDataItem( RegularTimePeriod.createInstance( timePeriodClass, keyDate,
                RegularTimePeriod.DEFAULT_TIME_ZONE ), ( (Number) rowData[column + 1] ).doubleValue() );
        wrkSeries.add( timeSeriesDataItem );
      }
      addSeries( wrkSeries );
      rowData = data.next();
    }

    if ( ( data.getRowCount() > 0 ) && ( this.getSeriesCount() <= 0 ) ) {
      noDataMessage = Messages.getInstance().getString( "CHART.USER_INCORRECT_DATA_FORMAT" ); //$NON-NLS-1$
    }

  }

  SimpleDateFormat formatter1 = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" ); //$NON-NLS-1$
  SimpleDateFormat formatter2 = new SimpleDateFormat( "yyyy-MM-dd" ); //$NON-NLS-1$

  private Date getValidDate( Object raw ) {

    if ( raw instanceof String ) {
      try {
        try {
          return formatter1.parse( (String) raw );
        } catch ( ParseException e ) {
          // try w/o hours
        }
        return formatter2.parse( (String) raw );
      } catch ( ParseException e ) {
        getLogger().error(
            Messages.getInstance().getString( "TimeSeriesCollectionChartDefinition.ERROR_0001_INVALID_DATE", //$NON-NLS-1$
                (String) raw ), e );
        return null;
      }
    } else {
      // This was the original code; if we have an unknown object
      // it will throw an exception, but would anyway.
      // It's a small atempt to make MDX queries work here
      return (Date) raw;
    }
  }

  public static Class getTimePeriodClass( final String timePeriodStr ) {
    Class retClass = Millisecond.class;
    if ( timePeriodStr.equalsIgnoreCase( ChartDefinition.SECOND_PERIOD_TYPE_STR ) ) {
      retClass = Second.class;
    } else if ( timePeriodStr.equalsIgnoreCase( ChartDefinition.MINUTE_PERIOD_TYPE_STR ) ) {
      retClass = Minute.class;
    } else if ( timePeriodStr.equalsIgnoreCase( ChartDefinition.HOUR_PERIOD_TYPE_STR ) ) {
      retClass = Hour.class;
    } else if ( timePeriodStr.equalsIgnoreCase( ChartDefinition.DAY_PERIOD_TYPE_STR ) ) {
      retClass = Day.class;
    } else if ( timePeriodStr.equalsIgnoreCase( ChartDefinition.WEEK_PERIOD_TYPE_STR ) ) {
      retClass = Week.class;
    } else if ( timePeriodStr.equalsIgnoreCase( ChartDefinition.MONTH_PERIOD_TYPE_STR ) ) {
      retClass = Month.class;
    } else if ( timePeriodStr.equalsIgnoreCase( ChartDefinition.QUARTER_PERIOD_TYPE_STR ) ) {
      retClass = Quarter.class;
    } else if ( timePeriodStr.equalsIgnoreCase( ChartDefinition.YEAR_PERIOD_TYPE_STR ) ) {
      retClass = Year.class;
    }
    return retClass;
  }

  /**
   * @param backgroundPaint
   *          The backgroundPaint to set.
   */
  public void setChartBackgroundPaint( final Paint chartBackgroundPaint ) {
    if ( chartBackgroundPaint != null ) {
      this.chartBackgroundPaint = chartBackgroundPaint;
    }
  }

  /**
   * Return the java.awt.Font to be used to display the dial title
   * 
   * @return Font The Font for the title of this Pie
   */
  public Font getTitleFont() {
    return titleFont;
  }

  public void setTitleFont( final Font titleFont ) {
    this.titleFont = titleFont;
  }

  public void setTitleFont( final Node titleFontNode ) {
    Font font = JFreeChartEngine.getFont( titleFontNode );
    if ( font != null ) {
      setTitleFont( font );
    }
  }

  /**
   * @return Returns the backgroundPaint.
   */
  public Paint getChartBackgroundPaint() {
    return chartBackgroundPaint;
  }

  /**
   * @return Returns the chartType.
   */
  public int getChartType() {
    return chartType;
  }

  public static int getChartType( final String typeStr ) {
    if ( typeStr != null ) {
      if ( ChartDefinition.LINE_CHART_STR.equalsIgnoreCase( typeStr ) ) {
        return JFreeChartEngine.LINE_CHART_TYPE;
      } else if ( ChartDefinition.AREA_CHART_STR.equalsIgnoreCase( typeStr ) ) {
        return JFreeChartEngine.AREA_CHART_TYPE;
      } else if ( ChartDefinition.STEP_CHART_STR.equalsIgnoreCase( typeStr ) ) {
        return JFreeChartEngine.STEP_CHART_TYPE;
      } else if ( ChartDefinition.STEP_AREA_CHART_STR.equalsIgnoreCase( typeStr ) ) {
        return JFreeChartEngine.STEP_AREA_CHART_TYPE;
      } else if ( ChartDefinition.DIFFERENCE_CHART_STR.equalsIgnoreCase( typeStr ) ) {
        return JFreeChartEngine.DIFFERENCE_CHART_TYPE;
      } else if ( ChartDefinition.DOT_CHART_STR.equalsIgnoreCase( typeStr ) ) {
        return JFreeChartEngine.DOT_CHART_TYPE;
      }
    }
    return JFreeChartEngine.UNDEFINED_CHART_TYPE;
  }

  public void setChartType( final Node chartTypeNode ) {
    if ( chartTypeNode != null ) {
      String typeStr = chartTypeNode.getText();
      setChartType( TimeSeriesCollectionChartDefinition.getChartType( typeStr ) );
    }
  }

  /**
   * @param chartType
   *          The chartType to set.
   */
  public void setChartType( final int chartType ) {
    this.chartType = chartType;
  }

  /**
   * @return Returns the threeD.
   */
  public boolean isThreeD() {
    return threeD;
  }

  public void setThreeD( final Node threeDNode ) {
    if ( threeDNode != null ) {
      String boolStr = threeDNode.getText();
      Boolean booleanValue = new Boolean( boolStr );
      setThreeD( booleanValue.booleanValue() );
    }
  }

  /**
   * @param threeD
   *          The threeD to set.
   */
  public void setThreeD( final boolean threeD ) {
    this.threeD = threeD;
  }

  /**
   * @return Returns the stacked.
   */
  public boolean isStacked() {
    return stacked;
  }

  public void setStacked( final Node stackedNode ) {
    if ( stackedNode != null ) {
      String boolStr = stackedNode.getText();
      Boolean booleanValue = new Boolean( boolStr );
      setStacked( booleanValue.booleanValue() );
    }
  }

  /**
   * @param stacked
   *          The stacked to set.
   */
  public void setStacked( final boolean stacked ) {
    this.stacked = stacked;
  }

  /**
   * @return Returns the verticalTickLabels.
   */
  public boolean isDomainVerticalTickLabels() {
    return domainVerticalTickLabels;
  }

  public void setDomainVerticalTickLabels( final Node domainVerticalTickLabelsNode ) {
    if ( domainVerticalTickLabelsNode != null ) {
      String boolStr = domainVerticalTickLabelsNode.getText();
      Boolean booleanValue = new Boolean( boolStr );
      setDomainVerticalTickLabels( booleanValue.booleanValue() );
    }
  }

  /**
   * @param domainVerticalTickLabels
   *          The domainVerticalLabels to set.
   */
  public void setDomainVerticalTickLabels( final boolean domainVerticalTickLabels ) {
    this.domainVerticalTickLabels = domainVerticalTickLabels;
  }

  /**
   * @return Returns the domainPeriodType.
   */
  public String getDomainPeriodType() {
    return domainPeriodType;
  }

  public void setDomainPeriodType( final Node domainPeriodTypeNode ) {
    if ( domainPeriodTypeNode != null ) {
      setDomainPeriodType( domainPeriodTypeNode.getText() );
    }
  }

  /**
   * @param domainPeriodType
   *          The domainPeriodType to set.
   */
  public void setDomainPeriodType( final String domainPeriodType ) {
    this.domainPeriodType = domainPeriodType;
  }

  /**
   * @return Returns the height.
   */
  public int getHeight() {
    return height;
  }

  public void setHeight( final Node heightNode ) {
    if ( heightNode != null ) {
      setHeight( Integer.parseInt( heightNode.getText() ) );
    }
  }

  /**
   * @param height
   *          The height to set.
   */
  public void setHeight( final int height ) {
    this.height = height;
  }

  /**
   * @return Returns the width.
   */
  public int getWidth() {
    return width;
  }

  public void setWidth( final Node widthNode ) {
    if ( widthNode != null ) {
      setWidth( Integer.parseInt( widthNode.getText() ) );
    }
  }

  /**
   * @param width
   *          The width to set.
   */
  public void setWidth( final int width ) {
    this.width = width;
  }

  /**
   * @return Returns the title.
   */
  public String getTitle() {
    return title;
  }

  public void setTitle( final Node chartTitleNode ) {
    if ( chartTitleNode != null ) {
      setTitle( chartTitleNode.getText() );
    }
  }

  /**
   * @param title
   *          The title to set.
   */
  public void setTitle( final String title ) {
    this.title = title;
  }

  /**
   * @return Returns the paintSequence.
   */
  public Paint[] getPaintSequence() {
    return paintSequence;
  }

  public void setPaintSequence( final Node paletteNode ) {
    if ( paletteNode != null ) {
      List colorNodes = paletteNode.selectNodes( ChartDefinition.COLOR_NODE_NAME );
      Paint[] paints = new Paint[colorNodes.size()];
      for ( int i = 0; i < colorNodes.size(); i++ ) {
        paints[i] = JFreeChartEngine.getPaint( (Node) colorNodes.get( i ) );
      }
      setPaintSequence( paints );
    }
  }

  /**
   * @param paintSequence
   *          The paintSequence to set.
   */
  public void setPaintSequence( final Paint[] paintSequence ) {
    this.paintSequence = paintSequence;
  }

  /**
   * @return Returns the subTitles.
   */
  public List getSubtitles() {
    return subTitles;
  }

  public void addSubTitles( final List subTitleNodes ) {
    if ( subTitleNodes != null ) {
      Iterator iter = subTitleNodes.iterator();
      while ( iter.hasNext() ) {
        addSubTitle( ( (Node) iter.next() ).getText() );
      }
    }
  }

  public void addSubTitle( final String subTitle ) {
    subTitles.add( subTitle );
  }

  /**
   * @return Returns the chartBackgroundImage.
   */
  public Image getChartBackgroundImage() {
    return chartBackgroundImage;
  }

  public void setChartBackgroundImage( final Node chartBackgroundImageNode ) {
    setChartBackgroundImage( JFreeChartEngine.getImage( chartBackgroundImageNode, getSession() ) );
  }

  /**
   * @param chartBackgroundImage
   *          The chartBackgroundImage to set.
   */
  public void setChartBackgroundImage( final Image chartBackgroundImage ) {
    this.chartBackgroundImage = chartBackgroundImage;
  }

  /**
   * @return Returns the legendIncluded.
   */
  public boolean isLegendIncluded() {
    return legendIncluded;
  }

  public void setLegendIncluded( final Node legendNode ) {
    if ( legendNode != null ) {
      String boolStr = legendNode.getText();
      Boolean booleanValue = new Boolean( boolStr );
      setLegendIncluded( booleanValue.booleanValue() );
    }
  }

  /**
   * @param legendIncluded
   *          The legendIncluded to set.
   */
  public void setLegendIncluded( final boolean legendIncluded ) {
    this.legendIncluded = legendIncluded;
  }

  public void setPlotBackgroundPaint( final Paint plotBackgroundPaint ) {
    if ( plotBackgroundPaint != null ) {
      this.plotBackgroundPaint = plotBackgroundPaint;
    }
  }

  public Paint getPlotBackgroundPaint() {
    return plotBackgroundPaint;
  }

  /**
   * @return Returns the plotBackgroundImage.
   */
  public Image getPlotBackgroundImage() {
    return plotBackgroundImage;
  }

  public void setPlotBackgroundImage( final Node plotBackgroundImageNode ) {
    setPlotBackgroundImage( JFreeChartEngine.getImage( plotBackgroundImageNode, getSession() ) );
  }

  /**
   * @param plotBackgroundImage
   *          The plotBackgroundImage to set.
   */
  public void setPlotBackgroundImage( final Image plotBackgroundImage ) {
    this.plotBackgroundImage = plotBackgroundImage;
  }

  /**
   * @return Returns the orientation.
   */
  public PlotOrientation getOrientation() {
    return orientation;
  }

  public void setOrientation( final Node orientationNode ) {
    if ( orientationNode != null ) {
      String orientationStr = orientationNode.getText();
      if ( ChartDefinition.VERTICAL_ORIENTATION.equalsIgnoreCase( orientationStr ) ) {
        setOrientation( PlotOrientation.VERTICAL );
      } else if ( ChartDefinition.HORIZONTAL_ORIENTATION.equalsIgnoreCase( orientationStr ) ) {
        setOrientation( PlotOrientation.HORIZONTAL );
      }
    }
  }

  /**
   * @param orientation
   *          The orientation to set.
   */
  public void setOrientation( final PlotOrientation orientation ) {
    this.orientation = orientation;
  }

  /**
   * @return Returns the borderVisible.
   */
  public boolean isBorderVisible() {
    return borderVisible;
  }

  public void setBorderVisible( final Node borderVisibleNode ) {
    if ( borderVisibleNode != null ) {
      String boolStr = borderVisibleNode.getText();
      Boolean booleanValue = new Boolean( boolStr );
      setBorderVisible( booleanValue.booleanValue() );
    }
  }

  /**
   * @param borderVisible
   *          The borderVisible to set.
   */
  public void setBorderVisible( final boolean borderVisible ) {
    this.borderVisible = borderVisible;
  }

  /**
   * @return Returns the borderPaint.
   */
  public Paint getBorderPaint() {
    return borderPaint;
  }

  /**
   * @param borderPaint
   *          The borderPaint to set.
   */
  public void setBorderPaint( final Paint borderPaint ) {
    this.borderPaint = borderPaint;
  }

  private RectangleEdge getPosition( final Node positionNode ) {
    if ( positionNode != null ) {
      String positionStr = positionNode.getText();
      if ( "top".equalsIgnoreCase( positionStr ) ) { //$NON-NLS-1$
        return RectangleEdge.TOP;
      } else if ( "left".equalsIgnoreCase( positionStr ) ) { //$NON-NLS-1$
        return RectangleEdge.LEFT;
      } else if ( "bottom".equalsIgnoreCase( positionStr ) ) { //$NON-NLS-1$
        return RectangleEdge.BOTTOM;
      } else if ( "right".equalsIgnoreCase( positionStr ) ) { //$NON-NLS-1$
        return RectangleEdge.RIGHT;
      }
    }
    return null;
  }

  public void setTitlePosition( final Node titlePositionNode ) {
    RectangleEdge position = getPosition( titlePositionNode );
    if ( position != null ) {
      setTitlePosition( position );
    }
  }

  /**
   * @return Returns the titlePosition.
   */
  public RectangleEdge getTitlePosition() {
    return titlePosition;
  }

  /**
   * @param titlePosition
   *          The titlePosition to set.
   */
  public void setTitlePosition( final RectangleEdge titlePosition ) {
    this.titlePosition = titlePosition;
  }

  public void setLegendPosition( final Node legendPositionNode ) {
    RectangleEdge position = getPosition( legendPositionNode );
    if ( position != null ) {
      setLegendPosition( position );
    }
  }

  public RectangleEdge getLegendPosition() {
    return legendPosition;
  }

  public void setLegendPosition( final RectangleEdge legendPosition ) {
    this.legendPosition = legendPosition;
  }

  public void setChartBackground( final Node chartBackgroundNode ) {
    if ( chartBackgroundNode != null ) {
      Node backgroundTypeNode = chartBackgroundNode.selectSingleNode( ChartDefinition.BACKGROUND_TYPE_ATTRIBUTE_NAME );
      if ( backgroundTypeNode != null ) {
        String backgroundTypeStr = backgroundTypeNode.getText();
        if ( ChartDefinition.COLOR_TYPE_NAME.equalsIgnoreCase( backgroundTypeStr ) ) {
          setChartBackgroundPaint( JFreeChartEngine.getPaint( chartBackgroundNode ) );
          setChartBackgroundImage( (Image) null );
        } else if ( ChartDefinition.IMAGE_TYPE_NAME.equalsIgnoreCase( backgroundTypeStr ) ) {
          setChartBackgroundImage( chartBackgroundNode );
          setChartBackgroundPaint( null );
        } else if ( ChartDefinition.TEXTURE_TYPE_NAME.equalsIgnoreCase( backgroundTypeStr ) ) {
          setChartBackgroundPaint( JFreeChartEngine.getTexturePaint( chartBackgroundNode, getWidth(), getHeight(),
              getSession() ) );
          setChartBackgroundImage( (Image) null );
        } else if ( ChartDefinition.GRADIENT_TYPE_NAME.equalsIgnoreCase( backgroundTypeStr ) ) {
          setChartBackgroundPaint( JFreeChartEngine.getGradientPaint( chartBackgroundNode, getWidth(), getHeight() ) );
          setChartBackgroundImage( (Image) null );
        }
      }
    }
  }

  public void setPlotBackground( final Node plotBackgroundNode ) {
    if ( plotBackgroundNode != null ) {
      Node backgroundTypeNode = plotBackgroundNode.selectSingleNode( ChartDefinition.BACKGROUND_TYPE_ATTRIBUTE_NAME );
      if ( backgroundTypeNode != null ) {
        String backgroundTypeStr = backgroundTypeNode.getText();
        if ( ChartDefinition.COLOR_TYPE_NAME.equalsIgnoreCase( backgroundTypeStr ) ) {
          setPlotBackgroundPaint( JFreeChartEngine.getPaint( plotBackgroundNode ) );
          setPlotBackgroundImage( (Image) null );
        } else if ( ChartDefinition.IMAGE_TYPE_NAME.equalsIgnoreCase( backgroundTypeStr ) ) {
          setPlotBackgroundImage( plotBackgroundNode );
          setPlotBackgroundPaint( null );
        } else if ( ChartDefinition.TEXTURE_TYPE_NAME.equalsIgnoreCase( backgroundTypeStr ) ) {
          setPlotBackgroundPaint( JFreeChartEngine.getTexturePaint( plotBackgroundNode, getWidth(), getHeight(),
              getSession() ) );
          setPlotBackgroundImage( (Image) null );
        } else if ( ChartDefinition.GRADIENT_TYPE_NAME.equalsIgnoreCase( backgroundTypeStr ) ) {
          setPlotBackgroundPaint( JFreeChartEngine.getGradientPaint( plotBackgroundNode, getWidth(), getHeight() ) );
          setPlotBackgroundImage( (Image) null );
        }
      }
    }
  }

  public void setDomainTitle( final Node titleNode ) {
    if ( titleNode != null ) {
      setDomainTitle( titleNode.getText() );
    }
  }

  /**
   * @return Returns the domainTitle.
   */
  public String getDomainTitle() {
    return domainTitle;
  }

  /**
   * @param domainTitle
   *          The domainTitle to set.
   */
  public void setDomainTitle( final String domainTitle ) {
    this.domainTitle = domainTitle;
  }

  public void setRangeTitle( final Node titleNode ) {
    if ( titleNode != null ) {
      setRangeTitle( titleNode.getText() );
    }
  }

  /**
   * @return Returns the rangeTitle.
   */
  public String getRangeTitle() {
    return rangeTitle;
  }

  /**
   * @param rangeTitle
   *          The rangeTitle to set.
   */
  public void setRangeTitle( final String rangeTitle ) {
    this.rangeTitle = rangeTitle;
  }

  public void setDomainTitleFont( final Node titleFontNode ) {
    Font font = JFreeChartEngine.getFont( titleFontNode );
    if ( font != null ) {
      setDomainTitleFont( font );
    }
  }

  /**
   * @return Returns the domainTitleFont.
   */
  public Font getDomainTitleFont() {
    return domainTitleFont;
  }

  /**
   * @param domainTitleFont
   *          The domainTitleFont to set.
   */
  public void setDomainTitleFont( final Font domainTitleFont ) {
    this.domainTitleFont = domainTitleFont;
  }

  public void setRangeTitleFont( final Node titleFontNode ) {
    Font font = JFreeChartEngine.getFont( titleFontNode );
    if ( font != null ) {
      setRangeTitleFont( font );
    }
  }

  /**
   * @return Returns the rangeTitleFont.
   */
  public Font getRangeTitleFont() {
    return rangeTitleFont;
  }

  /**
   * @param rangeTitleFont
   *          The rangeTitleFont to set.
   */
  public void setRangeTitleFont( final Font rangeTitleFont ) {
    this.rangeTitleFont = rangeTitleFont;
  }

  public boolean isDisplayLabels() {
    // TODO Auto-generated method stub
    return false;
  }

  public IPentahoSession getSession() {
    return session;
  }

  public void setSession( final IPentahoSession session ) {
    this.session = session;
  }

  // --------------------------------------------------
  /**
   * @return Returns the dot height.
   */
  public int getDotHeight() {
    return dotHeight;
  }

  public void setDotHeight( final Node heightNode ) {
    if ( heightNode != null ) {
      setDotHeight( Integer.parseInt( heightNode.getText() ) );
    }
  }

  /**
   * @param height
   *          The dot height to set.
   */
  public void setDotHeight( final int height ) {
    this.dotHeight = height;
  }

  /**
   * @return Returns the dot width.
   */
  public int getDotWidth() {
    return dotWidth;
  }

  public void setDotWidth( final Node widthNode ) {
    if ( widthNode != null ) {
      setDotWidth( Integer.parseInt( widthNode.getText() ) );
    }
  }

  /**
   * @param width
   *          The dot width to set.
   */
  public void setDotWidth( final int width ) {
    this.dotWidth = width;
  }

  /**
   * 
   * @return returns the style set for the lines
   * @see ChartDefinition.LINE_STYLE_SOLID_STR
   * @see ChartDefinition.LINE_STYLE_DASH_STR
   * @see ChartDefinition.LINE_STYLE_DOT_STR
   * @see ChartDefinition.LINE_STYLE_DASHDOT_STR
   * @see ChartDefinition.LINE_STYLE_DASHDOTDOT_STR
   */
  public String getLineStyle() {
    return lineStyle;
  }

  /**
   * 
   * @param lineStyle
   *          set the style for all line series
   * @see ChartDefinition.LINE_STYLE_SOLID_STR
   * @see ChartDefinition.LINE_STYLE_DASH_STR
   * @see ChartDefinition.LINE_STYLE_DOT_STR
   * @see ChartDefinition.LINE_STYLE_DASHDOT_STR
   * @see ChartDefinition.LINE_STYLE_DASHDOTDOT_STR
   */
  public void setLineStyle( final String lineStyle ) {
    this.lineStyle = lineStyle;
  }

  /**
   * 
   * @param lineStyleNode
   *          set the style from an XML node
   */
  public void setLineStyle( final Node lineStyleNode ) {
    if ( lineStyleNode != null ) {
      setLineStyle( lineStyleNode.getText() );
    }
  }

  /**
   * 
   * @return the width of all line series Valid values are float numbers zero or greater
   */
  public float getLineWidth() {
    return lineWidth;
  }

  /**
   * 
   * @param lineWidth
   *          set the width of all line series Valid values are float numbers zero or greater
   */
  public void setLineWidth( final float lineWidth ) {
    this.lineWidth = lineWidth;
  }

  /**
   * 
   * @param lineWidthNode
   *          set the line width from an XML node
   */
  public void setLineWidth( final Node lineWidthNode ) {
    if ( lineWidthNode != null ) {
      setLineWidth( Float.parseFloat( lineWidthNode.getText() ) );
    }
  }

  /**
   * 
   * @return boolean whether the markers (data points) for all series are displayed
   */
  public boolean isMarkersVisible() {
    return markersVisible;
  }

  /**
   * 
   * @param markersVisible
   *          set whether the markers (data points) for all series should be displayed
   */
  public void setMarkersVisible( final boolean markersVisible ) {
    this.markersVisible = markersVisible;
  }

  /**
   * 
   * @param markersVisibleNode
   *          set the markers visibility from an XML node
   */
  public void setMarkersVisible( final Node markersVisibleNode ) {
    if ( markersVisibleNode != null ) {
      String boolStr = markersVisibleNode.getText();
      Boolean booleanValue = new Boolean( boolStr );
      setMarkersVisible( booleanValue.booleanValue() );
    }
  }

  /**
   * Return the java.awt.Font to be used to display the legend items
   * 
   * @return Font The font for the legend items
   */
  public Font getLegendFont() {
    // TODO Auto-generated method stub
    return legendFont;
  }

  /**
   * Set java.awt.Font to be used to display the legend items
   * 
   * @param Font
   *          The java.awt.Font for the legend items
   */
  public void setLegendFont( final Font legendFont ) {
    this.legendFont = legendFont;
  }

  public void setLegendFont( final Node legendFontNode ) {
    Font font = JFreeChartEngine.getFont( legendFontNode );
    if ( font != null ) {
      setLegendFont( font );
    }
  }

  public void setLegendBorderVisible( final Node legendBorderVisibleNode ) {
    if ( legendBorderVisibleNode != null ) {
      boolean legBorderVisible = ( new Boolean( legendBorderVisibleNode.getText() ) ).booleanValue();
      setLegendBorderVisible( legBorderVisible );
    }
  }

  /**
   * @param boolean legendBorderVisible Set the visibility of the legend border.
   */
  public void setLegendBorderVisible( final boolean legendBorderVisible ) {
    this.legendBorderVisible = legendBorderVisible;
  }

  /**
   * Return the boolen that states if the legend border is visible
   * 
   * @return boolean Is the legend border visible
   */
  public boolean isLegendBorderVisible() {
    // TODO Auto-generated method stub
    return legendBorderVisible;
  }

  /**
   * Return the range axis' minimum value
   * 
   * @return double Range axis' minimum value
   */
  public double getRangeMinimum() {
    return rangeMinimum;
  }

  public void setRangeMinimum( final Node rangeMinimumNode ) {
    if ( rangeMinimumNode != null ) {
      setRangeMinimum( Double.parseDouble( rangeMinimumNode.getText() ) );
    }

  }

  /**
   * @param double rangeMinimum Set the minimum value of the range axis.
   */
  public void setRangeMinimum( final double rangeMinimum ) {
    this.rangeMinimum = rangeMinimum;
  }

  /**
   * Return the range axis' maximum value
   * 
   * @return double Range axis' maximum value
   */
  public double getRangeMaximum() {
    return rangeMaximum;
  }

  public void setRangeMaximum( final Node rangeMaximumNode ) {
    if ( rangeMaximumNode != null ) {
      setRangeMaximum( Double.parseDouble( rangeMaximumNode.getText() ) );
    }

  }

  public void setDateMaximum( final Date dateMaximum ) {
    this.dateMaximum = dateMaximum;
  }

  public Date getDateMaximum() {
    return dateMaximum;
  }

  public void setDateMaximum( final Node dateMaximumNode ) {
    if ( dateMaximumNode != null ) {
      try {
        setDateMaximum( DateFormat.getDateInstance().parse( dateMaximumNode.getText() ) );
      } catch ( ParseException e ) {
        getLogger().error(
            Messages.getInstance().getString( "TimeSeriesCollectionChartDefinition.ERROR_0001_INVALID_DATE", //$NON-NLS-1$
                dateMaximumNode.getText() ), e );
      }
    }
  }

  public void setDateMinimum( final Date dateMinimum ) {
    this.dateMinimum = dateMinimum;
  }

  public Date getDateMinimum() {
    return dateMinimum;
  }

  public void setDateMinimum( final Node dateMinimumNode ) {
    if ( dateMinimumNode != null ) {
      try {
        setDateMinimum( DateFormat.getDateInstance().parse( dateMinimumNode.getText() ) );
      } catch ( ParseException e ) {
        getLogger().error(
            Messages.getInstance().getString( "TimeSeriesCollectionChartDefinition.ERROR_0001_INVALID_DATE", //$NON-NLS-1$
                dateMinimumNode.getText() ), e );
      }
    }
  }

  /**
   * @param double rangeMaximum Set the maximum value of the range axis.
   */
  public void setRangeMaximum( final double rangeMaximum ) {
    this.rangeMaximum = rangeMaximum;
  }

  public String getNoDataMessage() {
    return noDataMessage;
  }

  public String getTooltipContent() {
    return tooltipContent;
  }

  public void setTooltipContent( final String tooltipContent ) {
    this.tooltipContent = tooltipContent;
  }

  public String getTooltipXFormat() {
    return tooltipXFormat;
  }

  public void setTooltipXFormat( final String tooltipXFormat ) {
    this.tooltipXFormat = tooltipXFormat;
  }

  public String getTooltipYFormat() {
    return tooltipYFormat;
  }

  public void setTooltipYFormat( final String tooltipYFormat ) {
    this.tooltipYFormat = tooltipYFormat;
  }

  public void setTooltipContent( final Node node ) {
    if ( node != null ) {
      setTooltipContent( node.getText() );
    }
  }

  public void setTooltipXFormat( final Node node ) {
    if ( node != null ) {
      setTooltipXFormat( node.getText() );
    }
  }

  public void setTooltipYFormat( final Node node ) {
    if ( node != null ) {
      setTooltipYFormat( node.getText() );
    }
  }

  public Float getBackgroundAlpha() {
    return backgroundAlpha;
  }

  public void setBackgroundAlpha( Node backgroundAlphaNode ) {
    if ( backgroundAlphaNode != null ) {
      Float backgroundAlphaValue = new Float( backgroundAlphaNode.getText() );
      this.backgroundAlpha = backgroundAlphaValue;
    }

  }

  public Float getForegroundAlpha() {
    return foregroundAlpha;
  }

  public void setForegroundAlpha( Node foregroundAlphaNode ) {
    if ( foregroundAlphaNode != null ) {
      Float foregroundAlphaValue = new Float( foregroundAlphaNode.getText() );
      this.foregroundAlpha = foregroundAlphaValue;
    }

  }

}
