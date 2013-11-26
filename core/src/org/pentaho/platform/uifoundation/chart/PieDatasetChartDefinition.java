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
import org.jfree.chart.title.TextTitle;
import org.jfree.data.UnknownKeyException;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.ui.RectangleEdge;
import org.pentaho.commons.connection.IPentahoDataTypes;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.commons.connection.PentahoDataTransmuter;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.uifoundation.messages.Messages;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PieDatasetChartDefinition extends DefaultPieDataset implements ChartDefinition {
  /**
   *
   */
  protected static final Log logger = LogFactory.getLog( PieDatasetChartDefinition.class );

  private static final long serialVersionUID = 1L;

  private static final String INTERIOR_GAP_NODE_NAME = "interior-gap"; //$NON-NLS-1$

  private static final String START_ANGLE_NODE_NAME = "start-angle"; //$NON-NLS-1$

  private static final String LABEL_FONT_NODE_NAME = "label-font"; //$NON-NLS-1$

  private static final String LABEL_PAINT_NODE_NAME = "label-paint"; //$NON-NLS-1$

  private static final String LABEL_BACKGROUND_PAINT_NODE_NAME = "label-background-paint"; //$NON-NLS-1$

  private static final String LABEL_GAP_NODE_NAME = "label-gap"; //$NON-NLS-1$

  private static final String SLICE_NODE_NAME = "slice"; //$NON-NLS-1$

  private static final String EXPLODE_SLICE_NODE_NAME = "explode-slices"; //$NON-NLS-1$

  // JFreeChart Customizations
  private String title = ""; //$NON-NLS-1$

  private String noDataMessage = null;

  private RectangleEdge titlePosition = RectangleEdge.TOP;

  private Font titleFont = TextTitle.DEFAULT_FONT;

  private final List subTitles = new ArrayList();

  private final List explodeSlices = new ArrayList();

  private Paint chartBackgroundPaint = Color.WHITE;

  private Image chartBackgroundImage = null;

  private boolean borderVisible = false;

  private Paint borderPaint = Color.BLACK;

  private int width = 200;

  private int height = 200;

  // Plot Customizations
  private Paint plotBackgroundPaint = Color.WHITE;

  private Image plotBackgroundImage = null;

  private Paint[] paintSequence = null;

  private boolean legendIncluded = true;

  private boolean threeD = false;

  private boolean displayLabels = true;

  // PiePlot Customizations
  private double interiorGap = 0.085;

  private double startAngle = 90.0;

  private Font labelFont = new Font( "SansSerif", Font.PLAIN, 10 ); //$NON-NLS-1$

  private Paint labelPaint = Color.BLACK;

  private Paint labelBackgroundPaint = new Color( 255, 255, 204 );

  private double labelGap = 0.10;

  // Other stuff
  private IPentahoSession session;

  private Font legendFont = null;

  private boolean legendBorderVisible = true;

  private RectangleEdge legendPosition = RectangleEdge.BOTTOM;

  private Float backgroundAlpha;

  private Float foregroundAlpha;

  /**
   *
   */
  public PieDatasetChartDefinition( final IPentahoSession session ) {
    super();
    this.session = session;
  }

  /**
   * @param data
   */
  public PieDatasetChartDefinition( final IPentahoResultSet data, final boolean byRow, final IPentahoSession session ) {
    this( session );
    if ( byRow ) {
      setDataByRow( data );
    } else {
      setDataByColumn( data );
    }
  }

  public PieDatasetChartDefinition( final IPentahoResultSet data, final boolean byRow, final Node chartAttributes,
      final IPentahoSession session ) {
    this( data, byRow, session );
    setChartAttributes( chartAttributes );
  }

  public static Log getLogger() {
    return LogFactory.getLog( PieDatasetChartDefinition.class );
  }

  private void setChartAttributes( final Node chartAttributes ) {
    if ( chartAttributes == null ) {
      return;
    }
    // set the chart background
    setChartBackground( chartAttributes.selectSingleNode( ChartDefinition.CHART_BACKGROUND_NODE_NAME ) );

    // set the plot background
    setPlotBackground( chartAttributes.selectSingleNode( ChartDefinition.PLOT_BACKGROUND_NODE_NAME ) );

    // do we want a legend
    setLegendIncluded( chartAttributes.selectSingleNode( ChartDefinition.INCLUDE_LEGEND_NODE_NAME ) );

    // get the chart title
    setTitle( chartAttributes.selectSingleNode( ChartDefinition.TITLE_NODE_NAME ) );

    Node backgroundAlphaNode = chartAttributes.selectSingleNode( ChartDefinition.BACKGROUND_ALPHA_NODE_NAME );
    Node foregroundAlphaNode = chartAttributes.selectSingleNode( ChartDefinition.FOREGROUND_ALPHA_NODE_NAME );

    if ( backgroundAlphaNode != null ) {
      setBackgroundAlpha( chartAttributes.selectSingleNode( ChartDefinition.BACKGROUND_ALPHA_NODE_NAME ) );
    }
    if ( foregroundAlphaNode != null ) {
      setForegroundAlpha( chartAttributes.selectSingleNode( ChartDefinition.FOREGROUND_ALPHA_NODE_NAME ) );
    }
    // get the chart subtitles

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
      PieDatasetChartDefinition.getLogger().warn(
          Messages.getInstance().getString(
              "CHART.WARN_DEPRECATED_CHILD", ChartDefinition.SUBTITLE_NODE_NAME, ChartDefinition.SUBTITLES_NODE_NAME ) ); //$NON-NLS-1$ 
      PieDatasetChartDefinition.getLogger().warn(
          Messages.getInstance()
              .getString( "CHART.WARN_PROPERTY_WILL_NOT_VALIDATE", ChartDefinition.SUBTITLE_NODE_NAME ) ); //$NON-NLS-1$  
    }

    if ( subtitles != null ) {
      addSubTitles( subtitles );
    }

    // get the chart's exploded sections

    List slicesNodes = null;
    Node slicesNode = chartAttributes.selectSingleNode( PieDatasetChartDefinition.EXPLODE_SLICE_NODE_NAME );
    if ( slicesNode != null ) {
      slicesNodes = slicesNode.selectNodes( PieDatasetChartDefinition.SLICE_NODE_NAME );
    }

    if ( slicesNodes != null ) {
      addExplodedSlices( slicesNodes );
    }

    // get the paint sequence
    setPaintSequence( chartAttributes.selectSingleNode( ChartDefinition.PALETTE_NODE_NAME ) );

    // get the 3D value
    setThreeD( chartAttributes.selectSingleNode( ChartDefinition.THREED_NODE_NAME ) );

    // set the width
    setWidth( chartAttributes.selectSingleNode( ChartDefinition.WIDTH_NODE_NAME ) );

    // set the height
    setHeight( chartAttributes.selectSingleNode( ChartDefinition.HEIGHT_NODE_NAME ) );

    // set the border on or off
    setBorderVisible( chartAttributes.selectSingleNode( ChartDefinition.CHART_BORDER_VISIBLE_NODE_NAME ) );

    // set the border Paint
    setBorderPaint( JFreeChartEngine.getPaint( chartAttributes
        .selectSingleNode( ChartDefinition.CHART_BORDER_PAINT_NODE_NAME ) ) );

    // set the title location
    setTitlePosition( chartAttributes.selectSingleNode( ChartDefinition.TITLE_POSITION_NODE_NAME ) );

    // set the title font
    setTitleFont( chartAttributes.selectSingleNode( ChartDefinition.TITLE_FONT_NODE_NAME ) );

    // set the interior gap
    setInteriorGap( chartAttributes.selectSingleNode( PieDatasetChartDefinition.INTERIOR_GAP_NODE_NAME ) );

    // set the start angle
    setStartAngle( chartAttributes.selectSingleNode( PieDatasetChartDefinition.START_ANGLE_NODE_NAME ) );

    // set if we want labels
    setDisplayLabels( chartAttributes.selectSingleNode( ChartDefinition.DISPLAY_LABELS_NODE_NAME ) );

    // set the label font
    setLabelFont( chartAttributes.selectSingleNode( PieDatasetChartDefinition.LABEL_FONT_NODE_NAME ) );

    // set the label paint
    setLabelPaint( JFreeChartEngine.getPaint( chartAttributes
        .selectSingleNode( PieDatasetChartDefinition.LABEL_PAINT_NODE_NAME ) ) );

    // set the label background paint
    setLabelBackgroundPaint( JFreeChartEngine.getPaint( chartAttributes
        .selectSingleNode( PieDatasetChartDefinition.LABEL_BACKGROUND_PAINT_NODE_NAME ) ) );

    // set the label gap
    setLabelGap( chartAttributes.selectSingleNode( PieDatasetChartDefinition.LABEL_GAP_NODE_NAME ) );

    // set legend font
    setLegendFont( chartAttributes.selectSingleNode( ChartDefinition.LEGEND_FONT_NODE_NAME ) );

    // set legend border visible
    setLegendBorderVisible( chartAttributes.selectSingleNode( ChartDefinition.DISPLAY_LEGEND_BORDER_NODE_NAME ) );

    setLegendPosition( chartAttributes.selectSingleNode( ChartDefinition.LEGEND_POSITION_NODE_NAME ) );
  }

  private void setDataByColumn( final IPentahoResultSet data ) {
    setDataByRow( PentahoDataTransmuter.pivot( data ) );
  }

  private void setDataByRow( IPentahoResultSet data ) {
    if ( data == null ) {
      noDataMessage = Messages.getInstance().getString( "CHART.USER_NO_DATA_AVAILABLE" ); //$NON-NLS-1$
      return; // No data so we've got nothing to set
      // TODO come up with some sort of error strategy here.
    }
    boolean hasColumnHeaders = data.getMetaData().getColumnHeaders() != null;
    if ( !hasColumnHeaders ) {
      data = PentahoDataTransmuter.transmute( data, false );
    }
    String[] columnHeaders = null;
    try {
      columnHeaders = PentahoDataTransmuter.getCollapsedHeaders( IPentahoDataTypes.AXIS_COLUMN, data, '|' );
    } catch ( Exception e ) {
      // should really NEVER get here
      PieDatasetChartDefinition.logger.error( null, e );
    }
    int row = 0;
    if ( !hasColumnHeaders ) {
      data.next();
      row = 1;
    }
    Object[] rowData = data.next();
    while ( ( rowData != null ) && ( row < data.getRowCount() + 1 ) ) {
      for ( int column = 0; column < rowData.length; column++ ) {
        if ( rowData[column] instanceof Number ) {

          Number currentNumber = null;
          try { // If value has been set then we get it
            if ( columnHeaders != null && column < columnHeaders.length ) {
              currentNumber = getValue( columnHeaders[column] );
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
          if ( columnHeaders != null && column < columnHeaders.length ) {
            setValue( columnHeaders[column], new Double( newValue + currentValue ) );
          }
        }
      }
      rowData = data.next();
      row++;
    }

    if ( ( data.getRowCount() > 0 ) && ( this.getItemCount() <= 0 ) ) {
      noDataMessage = Messages.getInstance().getString( "CHART.USER_INCORRECT_DATA_FORMAT" ); //$NON-NLS-1$
    }

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

  public int getHeight() {
    return height;
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

  public int getWidth() {
    return width;
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

  public String getTitle() {
    return title;
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

  public Font getTitleFont() {
    return titleFont;
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

  public List getSubtitles() {
    return subTitles;
  }

  public void addExplodedSlices( final List nodes ) {
    if ( nodes != null ) {
      Iterator iter = nodes.iterator();
      while ( iter.hasNext() ) {
        addExplodedSlice( ( (Node) iter.next() ).getText() );
      }
    }
  }

  public void addExplodedSlice( final String slice ) {
    explodeSlices.add( slice );
  }

  public List getExplodedSlices() {
    return explodeSlices;
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

  /**
   * @param backgroundPaint
   *          The backgroundPaint to set.
   */
  public void setChartBackgroundPaint( final Paint chartBackgroundPaint ) {
    if ( chartBackgroundPaint != null ) {
      this.chartBackgroundPaint = chartBackgroundPaint;
    }
  }

  public Paint getChartBackgroundPaint() {
    return chartBackgroundPaint;
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

  public Image getChartBackgroundImage() {
    return chartBackgroundImage;
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

  public boolean isBorderVisible() {
    return borderVisible;
  }

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

  public Paint[] getPaintSequence() {
    return paintSequence;
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

  public void setPlotBackgroundPaint( final Paint plotBackgroundPaint ) {
    if ( plotBackgroundPaint != null ) {
      this.plotBackgroundPaint = plotBackgroundPaint;
    }
  }

  public Paint getPlotBackgroundPaint() {
    return plotBackgroundPaint;
  }

  /**
   * @param plotBackgroundImage
   *          The plotBackgroundImage to set.
   */
  public void setPlotBackgroundImage( final Image plotBackgroundImage ) {
    this.plotBackgroundImage = plotBackgroundImage;
  }

  public void setPlotBackgroundImage( final Node plotBackgroundImageNode ) {
    setPlotBackgroundImage( JFreeChartEngine.getImage( plotBackgroundImageNode, getSession() ) );
  }

  public Image getPlotBackgroundImage() {
    return plotBackgroundImage;
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

  public boolean isLegendIncluded() {
    return legendIncluded;
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

  public boolean isThreeD() {
    return threeD;
  }

  private void setInteriorGap( final Node interiorGapNode ) {
    if ( interiorGapNode != null ) {
      String gapNodeStr = interiorGapNode.getText();
      Double doubleValue = new Double( gapNodeStr );
      setInteriorGap( doubleValue.doubleValue() );
    }
  }

  /**
   * @param interiorGap
   *          The interiorGap to set.
   */
  public void setInteriorGap( final double interiorGap ) {
    this.interiorGap = interiorGap;
  }

  public double getInteriorGap() {
    return interiorGap;
  }

  private void setStartAngle( final Node startAngleNode ) {
    if ( startAngleNode != null ) {
      String gapNodeStr = startAngleNode.getText();
      Double doubleValue = new Double( gapNodeStr );
      setStartAngle( doubleValue.doubleValue() );
    }
  }

  /**
   * @param startAngle
   *          The startAngle to set.
   */
  public void setStartAngle( final double startAngle ) {
    this.startAngle = startAngle;
  }

  public double getStartAngle() {
    return startAngle;
  }

  private void setLabelFont( final Node labelFontNode ) {
    Font font = JFreeChartEngine.getFont( labelFontNode );
    if ( font != null ) {
      setLabelFont( font );
    }
  }

  public void setLabelFont( final Font font ) {
    labelFont = font;
  }

  public Font getLabelFont() {
    // TODO Auto-generated method stub
    return labelFont;
  }

  /**
   * @param labelPaint
   *          The labelPaint to set.
   */
  public void setLabelPaint( final Paint labelPaint ) {
    if ( labelPaint != null ) {
      this.labelPaint = labelPaint;
    }
  }

  /**
   * @return Returns the labelPaint.
   */
  public Paint getLabelPaint() {
    return labelPaint;
  }

  public Paint getLabelBackgroundPaint() {
    // TODO Auto-generated method stub
    return labelBackgroundPaint;
  }

  /**
   * @param labelBackgroundPaint
   *          The labelBackgroundPaint to set.
   */
  public void setLabelBackgroundPaint( final Paint labelBackgroundPaint ) {
    if ( labelBackgroundPaint != null ) {
      this.labelBackgroundPaint = labelBackgroundPaint;
    }
  }

  public double getLabelGap() {
    return labelGap;
  }

  /**
   * @param node
   *          The labelGap to set.
   */
  public void setLabelGap( final Node labelGapNode ) {
    if ( labelGapNode != null ) {
      String gapNodeStr = labelGapNode.getText();
      Double doubleValue = new Double( gapNodeStr );
      setLabelGap( doubleValue.doubleValue() );
    }
  }

  public void setLabelGap( final double labelGap ) {
    this.labelGap = labelGap;
  }

  public boolean isDisplayLabels() {
    return displayLabels;
  }

  public void setDisplayLabels( final Node threeDNode ) {
    if ( threeDNode != null ) {
      String boolStr = threeDNode.getText();
      Boolean booleanValue = new Boolean( boolStr );
      setDisplayLabels( booleanValue.booleanValue() );
    }
  }

  public void setDisplayLabels( final boolean displayLabels ) {
    this.displayLabels = displayLabels;
  }

  public IPentahoSession getSession() {
    return session;
  }

  public void setSession( final IPentahoSession session ) {
    this.session = session;
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

  public String getNoDataMessage() {
    return noDataMessage;
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
