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
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.UnknownKeyException;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleEdge;
import org.pentaho.commons.connection.IPentahoDataTypes;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.commons.connection.PentahoDataTransmuter;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.uifoundation.messages.Messages;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Paint;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// ESCA-JAVA0136:
// ESCA-JAVA0100:
public class CategoryDatasetChartDefinition extends DefaultCategoryDataset implements ChartDefinition {

  private static final String STACKED_NODE_NAME = "is-stacked"; //$NON-NLS-1$

  private static final String ORIENTATION_NODE_NAME = "orientation"; //$NON-NLS-1$

  private static final String DOMAIN_TITLE_NODE_NAME = "domain-title"; //$NON-NLS-1$

  private static final String DOMAIN_TITLE_FONT_NODE_NAME = "domain-title-font"; //$NON-NLS-1$

  private static final String DOMAIN_TICK_FONT_NODE_NAME = "domain-tick-font"; //$NON-NLS-1$ 

  private static final String RANGE_TITLE_NODE_NAME = "range-title"; //$NON-NLS-1$

  private static final String RANGE_TITLE_FONT_NODE_NAME = "range-title-font"; //$NON-NLS-1$

  private static final String RANGE_TICK_FORMAT_NODE_NAME = "range-tick-format"; //$NON-NLS-1$

  private static final String RANGE_TICK_FONT_NODE_NAME = "range-tick-font"; //$NON-NLS-1$

  private static final String RANGE_MINIMUM_NODE_NAME = "range-minimum"; //$NON-NLS-1$

  private static final String RANGE_MAXIMUM_NODE_NAME = "range-maximum"; //$NON-NLS-1$

  private static final String RANGE_TICK_UNITS_NODE_NAME = "range-tick-units";

  private static final String DOMAIN_LABEL_ROTATION_ANGLE_NODE_NAME = "domain-label-rotation"; //$NON-NLS-1$

  private static final String DOMAIN_LABEL_ROTATION_DIRECTION_NODE_NAME = "domain-label-rotation-dir"; //$NON-NLS-1$

  private static final String MAX_BAR_WIDTH_NODE_NAME = "max-bar-width"; //$NON-NLS-1$

  private static final String INCLUDE_NULL_CATEGORIES_NODE_NAME = "include-null-categories"; //$NON-NLS-1$

  private int chartType = JFreeChartEngine.UNDEFINED_CHART_TYPE;

  private static final int DEFAULT_WIDTH = 200;

  private static final int DEFAULT_HEIGHT = 200;

  // JFreeChart Customizations
  private String title = ""; //$NON-NLS-1$

  protected String noDataMessage = null;

  private RectangleEdge titlePosition = RectangleEdge.TOP;

  private RectangleEdge legendPosition = RectangleEdge.BOTTOM;

  private Font titleFont = TextTitle.DEFAULT_FONT;

  private final List subTitles = new ArrayList();

  private Paint chartBackgroundPaint = Color.WHITE;

  private Image chartBackgroundImage = null;

  private boolean borderVisible = false;

  private Paint borderPaint = Color.BLACK;

  private int width = DEFAULT_WIDTH;

  private int height = DEFAULT_HEIGHT;

  // Plot Customizations
  private PlotOrientation orientation = PlotOrientation.VERTICAL;

  private Paint plotBackgroundPaint = Color.WHITE;

  private Image plotBackgroundImage = null;

  private boolean legendIncluded = true;

  private Font legendFont = null;

  private boolean legendBorderVisible = true;

  private boolean includeNullCategories = true;

  private boolean threeD = false;

  private boolean stacked = false;

  private Paint[] paintSequence = null;

  private CategoryLabelPositions domainLabelPositions = new CategoryLabelPositions();

  private String domainTitle = null;

  private Font domainTitleFont = TextTitle.DEFAULT_FONT;

  private Font domainTickFont = null;

  private String rangeTitle = null;

  private Font rangeTitleFont = TextTitle.DEFAULT_FONT;

  private NumberFormat rangeTickFormat = null;

  private Font rangeTickFont = null;

  private double rangeMinimum = ValueAxis.DEFAULT_LOWER_BOUND;

  private double rangeMaximum = ValueAxis.DEFAULT_UPPER_BOUND;

  private Double rangeTickUnits = null;

  // ADDED
  private Float backgroundAlpha;

  private Float foregroundAlpha;

  // line attributes are duplicated in 3 classes:
  // CategoryDatasetChartDefinition.java
  // XYSeriesCollecitonChartDefinition.java
  // TimeSeriesCollecitonChartdefinition.java
  // This is a tactical implementation, to get additional
  // critical attributes of the chart exposed. The architecture
  // for managing these attributes needs to be addressed
  // strategically at some point in the future.
  private String lineStyle = ChartDefinition.LINE_STYLE_SOLID_STR;

  private float lineWidth = 1.0f;

  private boolean markersVisible = false;

  private Double maxBarWidth;

  private IPentahoSession session;

  /**
   * version info
   */
  private static final long serialVersionUID = 1717509132920946530L;

  public CategoryDatasetChartDefinition( final IPentahoSession session ) {
    super();
    this.session = session;
  }

  public CategoryDatasetChartDefinition( final IPentahoSession session, final Node chartAttributes ) {
    super();
    this.session = session;
    setChartAttributes( chartAttributes );
  }

  public CategoryDatasetChartDefinition( final int chartType, final IPentahoResultSet data, final boolean byRow,
      final IPentahoSession session ) {
    this( session );
    this.chartType = chartType;
    if ( byRow ) {
      setDataByRow( data );
    } else {
      setDataByColumn( data );
    }
  }

  public CategoryDatasetChartDefinition( final IPentahoResultSet data, final boolean byRow, final Node chartAttributes,
      final IPentahoSession session ) {
    this( JFreeChartEngine.UNDEFINED_CHART_TYPE, data, byRow, session );
    setChartAttributes( chartAttributes );
  }

  public static Log getLogger() {
    return LogFactory.getLog( CategoryDatasetChartDefinition.class );
  }

  private void setChartAttributes( final Node chartAttributes ) {
    if ( chartAttributes == null ) {
      return;
    }

    // set the alfa layers
    Node backgroundAlphaNode = chartAttributes.selectSingleNode( ChartDefinition.BACKGROUND_ALPHA_NODE_NAME );
    Node foregroundAlphaNode = chartAttributes.selectSingleNode( ChartDefinition.FOREGROUND_ALPHA_NODE_NAME );

    if ( backgroundAlphaNode != null ) {
      setBackgroundAlpha( chartAttributes.selectSingleNode( ChartDefinition.BACKGROUND_ALPHA_NODE_NAME ) );
    }
    if ( foregroundAlphaNode != null ) {
      setForegroundAlpha( chartAttributes.selectSingleNode( ChartDefinition.FOREGROUND_ALPHA_NODE_NAME ) );
    }

    // get the chart type from the chart node -- this overrides the current
    // chart type
    setChartType( chartAttributes.selectSingleNode( ChartDefinition.TYPE_NODE_NAME ) );

    // set the chart background
    setChartBackground( chartAttributes.selectSingleNode( ChartDefinition.CHART_BACKGROUND_NODE_NAME ) );

    // set the plot background
    setPlotBackground( chartAttributes.selectSingleNode( ChartDefinition.PLOT_BACKGROUND_NODE_NAME ) );

    // set the orientation
    setOrientation( chartAttributes.selectSingleNode( CategoryDatasetChartDefinition.ORIENTATION_NODE_NAME ) );

    // do we want a legend
    setLegendIncluded( chartAttributes.selectSingleNode( ChartDefinition.INCLUDE_LEGEND_NODE_NAME ) );

    // get the chart title
    setTitle( chartAttributes.selectSingleNode( ChartDefinition.TITLE_NODE_NAME ) );

    // get the chart subtitles

    // A list of <subtitle> nodes should not be allowed to exist as a child of the main XML element (for XML schema
    // to
    // be well constructed and validate the XML .
    // We have deprecated <subtitle> as a child of the main node , and now require a <subtitles> parent node
    // under which <subtitle> can exist.

    List subTitleNodes = chartAttributes.selectNodes( ChartDefinition.SUBTITLE_NODE_NAME );

    if ( ( subTitleNodes == null ) || ( subTitleNodes.isEmpty() ) ) {
      Node subTitlesNode = chartAttributes.selectSingleNode( ChartDefinition.SUBTITLES_NODE_NAME );
      if ( subTitlesNode != null ) {
        subTitleNodes = subTitlesNode.selectNodes( ChartDefinition.SUBTITLE_NODE_NAME );
      }
    } else {
      // log a deprecation warning for this property...
      CategoryDatasetChartDefinition.getLogger().warn(
          Messages.getInstance().getString(
              "CHART.WARN_DEPRECATED_CHILD", ChartDefinition.SUBTITLE_NODE_NAME, ChartDefinition.SUBTITLES_NODE_NAME ) ); //$NON-NLS-1$ 
      CategoryDatasetChartDefinition.getLogger().warn(
          Messages.getInstance()
              .getString( "CHART.WARN_PROPERTY_WILL_NOT_VALIDATE", ChartDefinition.SUBTITLE_NODE_NAME ) ); //$NON-NLS-1$  
    }

    if ( subTitleNodes != null ) {
      addSubTitles( subTitleNodes );
    }

    // get the paint sequence
    setPaintSequence( chartAttributes.selectSingleNode( ChartDefinition.PALETTE_NODE_NAME ) );

    // get the stacked value
    setStacked( chartAttributes.selectSingleNode( CategoryDatasetChartDefinition.STACKED_NODE_NAME ) );

    // get the 3D value
    setThreeD( chartAttributes.selectSingleNode( ChartDefinition.THREED_NODE_NAME ) );

    // set the width
    setWidth( chartAttributes.selectSingleNode( ChartDefinition.WIDTH_NODE_NAME ) );

    // set the height
    setHeight( chartAttributes.selectSingleNode( ChartDefinition.HEIGHT_NODE_NAME ) );

    // set category rotation direction
    setCategoryLabelRotation( chartAttributes
        .selectSingleNode( CategoryDatasetChartDefinition.DOMAIN_LABEL_ROTATION_DIRECTION_NODE_NAME ), chartAttributes
        .selectSingleNode( CategoryDatasetChartDefinition.DOMAIN_LABEL_ROTATION_ANGLE_NODE_NAME ) );

    // set the border on or off
    setBorderVisible( chartAttributes.selectSingleNode( ChartDefinition.CHART_BORDER_VISIBLE_NODE_NAME ) );

    // set the border Paint
    setBorderPaint( JFreeChartEngine.getPaint( chartAttributes
        .selectSingleNode( ChartDefinition.CHART_BORDER_PAINT_NODE_NAME ) ) );

    // set the title location
    setTitlePosition( chartAttributes.selectSingleNode( ChartDefinition.TITLE_POSITION_NODE_NAME ) );

    // set the legend position
    setLegendPosition( chartAttributes.selectSingleNode( ChartDefinition.LEGEND_POSITION_NODE_NAME ) );

    // set the title font
    setTitleFont( chartAttributes.selectSingleNode( ChartDefinition.TITLE_FONT_NODE_NAME ) );

    // set the domain title
    setDomainTitle( chartAttributes.selectSingleNode( CategoryDatasetChartDefinition
      .DOMAIN_TITLE_NODE_NAME ) );

    // set the domain font
    setDomainTitleFont( chartAttributes.selectSingleNode( CategoryDatasetChartDefinition
      .DOMAIN_TITLE_FONT_NODE_NAME ) );

    // set the range minimum
    setRangeMinimum( chartAttributes.selectSingleNode( CategoryDatasetChartDefinition
      .RANGE_MINIMUM_NODE_NAME ) );

    // set the range minimum
    setRangeMaximum( chartAttributes.selectSingleNode( CategoryDatasetChartDefinition
      .RANGE_MAXIMUM_NODE_NAME ) );

    // set the range tick units
    setRangeTickUnits( chartAttributes.selectSingleNode( CategoryDatasetChartDefinition
      .RANGE_TICK_UNITS_NODE_NAME ) );

    // the the range tick label number format
    setDomainTickFont( chartAttributes.selectSingleNode( CategoryDatasetChartDefinition
      .DOMAIN_TICK_FONT_NODE_NAME ) );

    // set the range title
    setRangeTitle( chartAttributes.selectSingleNode( CategoryDatasetChartDefinition
      .RANGE_TITLE_NODE_NAME ) );

    // the the range font
    setRangeTitleFont( chartAttributes.selectSingleNode( CategoryDatasetChartDefinition
      .RANGE_TITLE_FONT_NODE_NAME ) );

    // the the range tick label number format
    setRangeTickFormat( chartAttributes.selectSingleNode( CategoryDatasetChartDefinition
      .RANGE_TICK_FORMAT_NODE_NAME ) );

    // the the range ticklabel font
    setRangeTickFont( chartAttributes.selectSingleNode( CategoryDatasetChartDefinition
      .RANGE_TICK_FONT_NODE_NAME ) );

    // set the line style
    setLineStyle( chartAttributes.selectSingleNode( ChartDefinition.LINE_STYLE_NODE_NAME ) );

    // set the line width
    setLineWidth( chartAttributes.selectSingleNode( ChartDefinition.LINE_WIDTH_NODE_NAME ) );

    // set the marker visibility
    setMarkersVisible( chartAttributes.selectSingleNode( ChartDefinition.MARKER_VISIBLE_NODE_NAME ) );

    // set the marker visibility
    setMaxBarWidth( chartAttributes.selectSingleNode( CategoryDatasetChartDefinition.MAX_BAR_WIDTH_NODE_NAME ) );

    // set legend font
    setLegendFont( chartAttributes.selectSingleNode( ChartDefinition.LEGEND_FONT_NODE_NAME ) );

    // set legend border visible
    setLegendBorderVisible( chartAttributes.selectSingleNode( ChartDefinition.DISPLAY_LEGEND_BORDER_NODE_NAME ) );

    // first see if Pentaho System sets this property
    String defaultIncludeNullCategories =
        PentahoSystem.getSystemSetting( "Charting/" + INCLUDE_NULL_CATEGORIES_NODE_NAME, "true" ); //$NON-NLS-1$ //$NON-NLS-2$

    setIncludeNullCategories( "true".equals( defaultIncludeNullCategories ) ); //$NON-NLS-1$

    // set whether to include null categories
    setIncludeNullCategories( chartAttributes.selectSingleNode( INCLUDE_NULL_CATEGORIES_NODE_NAME ) );
  }

  private void setDataByColumn( final IPentahoResultSet data ) {
    setDataByRow( PentahoDataTransmuter.pivot( data ) );
  }

  @SuppressWarnings( "deprecation" )
  private void setDataByRow( IPentahoResultSet data ) {
    if ( data == null ) {
      noDataMessage = Messages.getInstance().getString( "CHART.USER_NO_DATA_AVAILABLE" ); //$NON-NLS-1$
      return; // No data so we've got nothing to set
      // TODO come up with some sort of error strategy here.
    }
    boolean hasRowHeaders = data.getMetaData().getRowHeaders() != null;
    boolean hasColumnHeaders = data.getMetaData().getColumnHeaders() != null;
    if ( !hasRowHeaders || !hasColumnHeaders ) {
      data = PentahoDataTransmuter.transmute( data, false );
    }
    String[] rowHeaders = null;
    String[] columnHeaders = null;
    try {
      rowHeaders = PentahoDataTransmuter.getCollapsedHeaders( IPentahoDataTypes.AXIS_ROW, data, '|' );
      columnHeaders = PentahoDataTransmuter.getCollapsedHeaders( IPentahoDataTypes.AXIS_COLUMN, data, '|' );
    } catch ( Exception e ) {
      // should really NEVER get here
      CategoryDatasetChartDefinition.getLogger().error( null, e );
    }
    int row = 0;
    Object[] rowData = data.next();
    while ( rowData != null ) {
      for ( int column = 0; column < rowData.length; column++ ) {
        if ( rowData[column] instanceof Number ) {
          Number currentNumber = null;
          try { // If value has been set then we get it
            if ( rowHeaders != null && row < rowHeaders.length && columnHeaders != null
                && column < columnHeaders.length ) {
              currentNumber = getValue( rowHeaders[row], columnHeaders[column] );
            }
          } catch ( UnknownKeyException uke ) { // else we just set it
            // to zero
            currentNumber = new Double( 0.0 );
          }
          if ( currentNumber == null ) {
            currentNumber = new Double( 0.0 );
          }
          double currentValue = currentNumber.doubleValue();
          double newValue = ( (Number) rowData[column] ).doubleValue();
          if ( rowHeaders != null && row < rowHeaders.length && columnHeaders
            != null && column < columnHeaders.length ) {
            setValue( new Double( currentValue + newValue ), rowHeaders[row], columnHeaders[column] );
          }
        } else if ( includeNullCategories && rowData[column] == null ) {
          Number currentNumber = null;
          try { // If value has been set then we get it
            if ( rowHeaders != null && row < rowHeaders.length && columnHeaders != null
                && column < columnHeaders.length ) {
              currentNumber = getValue( rowHeaders[row], columnHeaders[column] );
            }
          } catch ( UnknownKeyException uke ) { // else we just set it
            currentNumber = null;
          }
          if ( rowHeaders != null && row < rowHeaders.length && columnHeaders
            != null && column < columnHeaders.length ) {
            setValue( currentNumber, rowHeaders[row], columnHeaders[column] );
          }
        }
      }
      row++;
      rowData = data.next();
    }

    if ( ( data.getRowCount() > 0 ) && ( this.getRowCount() <= 0 ) ) {
      noDataMessage = Messages.getInstance().getString( "CHART.USER_INCORRECT_DATA_FORMAT" ); //$NON-NLS-1$
    }
  }

  /**
   * @param chartBackgroundPaint
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

  /**
   * @deprecated use JFreeChartEngine.getChartType(typeStr), which has all of the chart name options for
   *             conversion.
   * 
   * @param typeStr
   * @return
   */
  @Deprecated
  public static int getChartType( final String typeStr ) {
    if ( typeStr != null ) {
      if ( ChartDefinition.PIE_CHART_STR.equalsIgnoreCase( typeStr ) ) {
        return JFreeChartEngine.PIE_CHART_TYPE;
      } else if ( ChartDefinition.PIE_GRID_CHART_STR.equalsIgnoreCase( typeStr ) ) {
        return JFreeChartEngine.PIE_GRID_CHART_TYPE;
      } else if ( ChartDefinition.BAR_CHART_STR.equalsIgnoreCase( typeStr ) ) {
        return JFreeChartEngine.BAR_CHART_TYPE;
      } else if ( ChartDefinition.LINE_CHART_STR.equalsIgnoreCase( typeStr ) ) {
        return JFreeChartEngine.LINE_CHART_TYPE;
      } else if ( ChartDefinition.AREA_CHART_STR.equalsIgnoreCase( typeStr ) ) {
        return JFreeChartEngine.AREA_CHART_TYPE;
      } else if ( ChartDefinition.BAR_LINE_CHART_STR.equalsIgnoreCase( typeStr ) ) {
        return JFreeChartEngine.BAR_LINE_CHART_TYPE;
      }
    }
    return JFreeChartEngine.UNDEFINED_CHART_TYPE;
  }

  public void setChartType( final Node chartTypeNode ) {
    if ( chartTypeNode != null ) {
      String typeStr = chartTypeNode.getText();
      setChartType( JFreeChartEngine.getChartType( typeStr ) );
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
      setThreeD( Boolean.parseBoolean( boolStr ) );
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
      setStacked( Boolean.parseBoolean( boolStr ) );
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

  public CategoryLabelPositions getCategoryLabelPositions() {
    return domainLabelPositions;
  }

  public void setCategoryLabelRotation( final Node rotationDirection, final Node rotationAngle ) {
    // down is the default
    String direction = "down"; //$NON-NLS-1$
    if ( rotationDirection != null ) {
      direction = rotationDirection.getText();
    }

    if ( rotationAngle != null ) {
      if ( "up".equalsIgnoreCase( direction ) ) { //$NON-NLS-1$
        setCategoryLabelPositions( CategoryLabelPositions.createUpRotationLabelPositions( Double
            .parseDouble( rotationAngle.getText() ) ) );
      } else {
        setCategoryLabelPositions( CategoryLabelPositions.createDownRotationLabelPositions( Double
            .parseDouble( rotationAngle.getText() ) ) );
      }
    }
  }

  /**
   * @param categoryLabelPositions
   *          The categoryLabelPositions to set.
   */
  public void setCategoryLabelPositions( final CategoryLabelPositions categoryLabelPositions ) {
    this.domainLabelPositions = categoryLabelPositions;
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
    if ( borderPaint != null ) {
      this.borderPaint = borderPaint;
    }
  }

  public void setTitlePosition( final Node titlePositionNode ) {
    RectangleEdge position = getPosition( titlePositionNode );
    if ( position != null ) {
      setTitlePosition( position );
    }
  }

  public void setLegendPosition( final Node legendPositionNode ) {
    RectangleEdge position = getPosition( legendPositionNode );
    if ( position != null ) {
      setLegendPosition( position );
    }
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

  public RectangleEdge getLegendPosition() {
    return legendPosition;
  }

  public void setLegendPosition( RectangleEdge legendPosition ) {
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

  /**
   * Return the java.awt.Font to be used to display the range axis tick labels
   * 
   * @return Font The Font for the range axis tick labels
   */
  public Font getDomainTickFont() {
    return domainTickFont;
  }

  public void setDomainTickFont( final Font domainTickFont ) {
    this.domainTickFont = domainTickFont;
  }

  public void setDomainTickFont( final Node rangeTickFontNode ) {
    Font font = JFreeChartEngine.getFont( rangeTickFontNode );
    if ( font != null ) {
      setDomainTickFont( font );
    }
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

  /**
   * @return Returns the rangeTickFormat.
   */
  public NumberFormat getRangeTickFormat() {
    return rangeTickFormat;
  }

  /**
   * @param rangeTickFormat
   *          The range tick number format to set.
   */
  public void setRangeTickFormat( final NumberFormat rangeTickFormat ) {
    this.rangeTickFormat = rangeTickFormat;
  }

  public void setRangeTickFormat( final Node tickFormatFontNode ) {
    if ( tickFormatFontNode != null ) {
      NumberFormat format = new DecimalFormat( tickFormatFontNode.getText() );
      if ( format != null ) {
        setRangeTickFormat( format );
      }
    }
  }

  /**
   * Return the java.awt.Font to be used to display the range axis tick labels
   * 
   * @return Font The Font for the range axis tick labels
   */
  public Font getRangeTickFont() {
    return rangeTickFont;
  }

  public void setRangeTickFont( final Font rangeTickFont ) {
    this.rangeTickFont = rangeTickFont;
  }

  public void setRangeTickFont( final Node rangeTickFontNode ) {
    Font font = JFreeChartEngine.getFont( rangeTickFontNode );
    if ( font != null ) {
      setRangeTickFont( font );
    }
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
   * 
   * @return the max width of all bar series Valid values are double numbers zero or greater
   */
  public Double getMaxBarWidth() {
    return maxBarWidth;
  }

  /**
   * 
   * @param maxBarWidth
   *          set the width of all bar series Valid values are double numbers zero or greater
   */
  public void setMaxBarWidth( final Double maxBarWidth ) {
    this.maxBarWidth = maxBarWidth;
  }

  /**
   * 
   * @param maxBarWidthNode
   *          set the maxBarWidth width from an XML node
   */
  public void setMaxBarWidth( final Node maxBarWidthNode ) {
    if ( maxBarWidthNode != null ) {
      setMaxBarWidth( new Double( maxBarWidthNode.getText() ) );
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

  public void setIncludeNullCategories( final Node includeNullCategoriesNode ) {
    if ( includeNullCategoriesNode != null ) {
      boolean locIncludeNullCategories = ( new Boolean( includeNullCategoriesNode.getText() ) ).booleanValue();
      setIncludeNullCategories( locIncludeNullCategories );
    }
  }

  /**
   * Return the boolen that states if null categories should be included
   * 
   * @return boolean Is the legend border visible
   */
  public boolean isIncludeNullCategories() {
    return includeNullCategories;
  }

  /**
   * @param boolean includeNullCategories Set whether the dataset should include null categories
   */
  public void setIncludeNullCategories( final boolean includeNullCategories ) {
    this.includeNullCategories = includeNullCategories;
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
   * Return the range axis' minimum value
   * 
   * @return double Range axis' minimum value
   */
  public double getRangeMaximum() {
    return rangeMaximum;
  }

  public void setRangeMaximum( final Node rangeMaximumNode ) {
    if ( rangeMaximumNode != null ) {
      setRangeMaximum( Double.parseDouble( rangeMaximumNode.getText() ) );
    }

  }

  /**
   * @param double rangeMinimum Set the minimum value of the range axis.
   */
  public void setRangeMaximum( final double rangeMaximum ) {
    this.rangeMaximum = rangeMaximum;
  }

  public void setRangeTickUnits( final Node rangeTickUnitsNode ) {
    if ( rangeTickUnitsNode != null ) {
      String rangeTickVal = rangeTickUnitsNode.getText();
      String dotLess = rangeTickVal.replace( ".", "" );
      if ( Integer.parseInt( dotLess ) > 0 ) {
        setRangeTickUnits( Double.parseDouble( rangeTickVal ) );
      }
    }
  }

  public void setRangeTickUnits( final double rangeTickUnits ) {
    this.rangeTickUnits = rangeTickUnits;
  }

  public Double getRangeTickUnits() {
    return this.rangeTickUnits;
  }

  public String getNoDataMessage() {
    return noDataMessage;
  }

  // ADDED
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
