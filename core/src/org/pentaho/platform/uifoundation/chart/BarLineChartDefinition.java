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

import org.dom4j.Node;
import org.jfree.chart.axis.ValueAxis;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.IPentahoSession;

import java.awt.Font;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

public class BarLineChartDefinition extends CategoryDatasetChartDefinition {

  private static final String SERIES_NODE_NAME = "series"; //$NON-NLS-1$

  private static final String BAR_SERIES_NODE_NAME = "bar-series"; //$NON-NLS-1$

  private static final String LINE_SERIES_NODE_NAME = "line-series"; //$NON-NLS-1$

  // private static final String CATEGORY_NODE_NAME = "category-column"; //$NON-NLS-1$

  private static final String LINES_RANGE_TITLE_NODE_NAME = "lines-range-title"; //$NON-NLS-1$

  private static final String LINES_RANGE_TITLE_FONT_NODE_NAME = "lines-range-title-font"; //$NON-NLS-1$

  private static final String LINES_RANGE_TICK_FORMAT_NODE_NAME = "lines-range-tick-format"; //$NON-NLS-1$

  private static final String LINES_RANGE_TICK_FONT_NODE_NAME = "lines-range-tick-font"; //$NON-NLS-1$

  private static final String LINES_RANGE_MINIMUM_NODE_NAME = "lines-range-minimum"; //$NON-NLS-1$

  private static final String LINES_RANGE_MAXIMUM_NODE_NAME = "lines-range-maximum"; //$NON-NLS-1$

  private String[] barColumns;

  private String[] lineColumns;

  // not used
  // private String categoryColumn;

  private Node chartAttributes;

  private String linesRangeTitle = null;

  private Font linesRangeTitleFont = null;

  private NumberFormat linesRangeTickFormat = null;

  private Font linesRangeTickFont = null;

  private double linesRangeMinimum = ValueAxis.DEFAULT_LOWER_BOUND;

  private double linesRangeMaximum = ValueAxis.DEFAULT_UPPER_BOUND;

  private static final long serialVersionUID = 1955883428823312855L;

  public BarLineChartDefinition( final IPentahoResultSet data, final boolean byRow, final Node chartAttributes,
      final IPentahoSession session ) {
    super( data, byRow, chartAttributes, session );

    // setting standard category chart definition attriutes
    setChartAttributes( chartAttributes );

    // Setting bar-line-chart attributes
    // setCategoryColumn(chartAttributes.selectSingleNode(CATEGORY_NODE_NAME));
    setBarSeries( chartAttributes.selectSingleNode( BarLineChartDefinition.BAR_SERIES_NODE_NAME ) );
    setLineSeries( chartAttributes.selectSingleNode( BarLineChartDefinition.LINE_SERIES_NODE_NAME ) );
    setLinesRangeTitle( chartAttributes.selectSingleNode( BarLineChartDefinition.LINES_RANGE_TITLE_NODE_NAME ) );
    setLinesRangeTitleFont( chartAttributes.selectSingleNode( BarLineChartDefinition
      .LINES_RANGE_TITLE_FONT_NODE_NAME ) );
    setLinesRangeTickFormat( chartAttributes
        .selectSingleNode( BarLineChartDefinition.LINES_RANGE_TICK_FORMAT_NODE_NAME ) );
    setLinesRangeTickFont( chartAttributes.selectSingleNode( BarLineChartDefinition.LINES_RANGE_TICK_FONT_NODE_NAME ) );
    setLinesRangeMinimum( chartAttributes.selectSingleNode( BarLineChartDefinition.LINES_RANGE_MINIMUM_NODE_NAME ) );
    setLinesRangeMaximum( chartAttributes.selectSingleNode( BarLineChartDefinition.LINES_RANGE_MAXIMUM_NODE_NAME ) );
  }

  public String[] getBarColumns() {
    return barColumns;
  }

  public void setBarColumns( final String[] barColumns ) {
    this.barColumns = barColumns;
  }

  // public String getCategoryColumn() {
  // return categoryColumn;
  // }
  //
  // public void setCategoryColumn(Node categoryNode) {
  // if (categoryNode != null) {
  // this.categoryColumn = categoryNode.getText();
  // }
  // }

  public String[] getLineColumns() {
    return lineColumns;
  }

  public void setLineColumns( final String[] lineColumns ) {
    this.lineColumns = lineColumns;
  }

  public void setBarSeries( final Node barSeriesNode ) {
    if ( barSeriesNode != null ) {
      List barNodes = barSeriesNode.selectNodes( BarLineChartDefinition.SERIES_NODE_NAME );
      String[] bars = new String[barNodes.size()];
      for ( int i = 0; i < barNodes.size(); i++ ) {
        Node barNode = (Node) barNodes.get( i );
        bars[i] = barNode.getText();
      }
      setBarColumns( bars );
    }
  }

  public void setLineSeries( final Node lineSeriesNode ) {
    if ( lineSeriesNode != null ) {
      List lineNodes = lineSeriesNode.selectNodes( BarLineChartDefinition.SERIES_NODE_NAME );
      String[] lines = new String[lineNodes.size()];
      for ( int i = 0; i < lineNodes.size(); i++ ) {
        Node lineNode = (Node) lineNodes.get( i );
        lines[i] = lineNode.getText();
      }
      setLineColumns( lines );
    }
  }

  public Node getChartAttributes() {
    return chartAttributes;
  }

  public void setChartAttributes( final Node chartAttributes ) {
    this.chartAttributes = chartAttributes;
  }

  public String getLinesRangeTitle() {
    return linesRangeTitle;
  }

  public void setLinesRangeTitle( final Node titleNode ) {
    if ( titleNode != null ) {
      setLinesRangeTitle( titleNode.getText() );
    }
  }

  public void setLinesRangeTitle( final String linesRangeTitle ) {
    this.linesRangeTitle = linesRangeTitle;
  }

  public Font getLinesRangeTitleFont() {
    return linesRangeTitleFont;
  }

  public void setLinesRangeTitleFont( final Node titleFontNode ) {
    Font font = JFreeChartEngine.getFont( titleFontNode );
    if ( font != null ) {
      setLinesRangeTitleFont( font );
    }
  }

  public void setLinesRangeTitleFont( final Font linesRangeTitleFont ) {
    this.linesRangeTitleFont = linesRangeTitleFont;
  }

  public NumberFormat getLinesRangeTickFormat() {
    return linesRangeTickFormat;
  }

  public void setLinesRangeTickFormat( final NumberFormat linesRangeTickFormat ) {
    this.linesRangeTickFormat = linesRangeTickFormat;
  }

  public void setLinesRangeTickFormat( final Node tickFormatFontNode ) {
    if ( tickFormatFontNode != null ) {
      NumberFormat format = new DecimalFormat( tickFormatFontNode.getText() );
      if ( format != null ) {
        setLinesRangeTickFormat( format );
      }
    }
  }

  /**
   * Return the java.awt.Font to be used to display the range axis tick labels
   * 
   * @return Font The Font for the range axis tick labels
   */
  public Font getLinesRangeTickFont() {
    return linesRangeTickFont;
  }

  public void setLinesRangeTickFont( final Font linesRangeTickFont ) {
    this.linesRangeTickFont = linesRangeTickFont;
  }

  public void setLinesRangeTickFont( final Node linesRangeTickFontNode ) {
    Font font = JFreeChartEngine.getFont( linesRangeTickFontNode );
    if ( font != null ) {
      setLinesRangeTickFont( font );
    }
  }

  /**
   * Return the range axis' minimum value
   * 
   * @return double Range axis' minimum value
   */
  public double getLinesRangeMinimum() {
    return linesRangeMinimum;
  }

  public void setLinesRangeMinimum( final Node linesRangeMinimumNode ) {
    if ( linesRangeMinimumNode != null ) {
      setLinesRangeMinimum( Double.parseDouble( linesRangeMinimumNode.getText() ) );
    }

  }

  /**
   * @param double rangeMinimum Set the minimum value of the range axis.
   */
  public void setLinesRangeMinimum( final double linesRangeMinimum ) {
    this.linesRangeMinimum = linesRangeMinimum;
  }

  /**
   * Return the range axis' maximum value
   * 
   * @return double Range axis' maximum value
   */
  public double getLinesRangeMaximum() {
    return linesRangeMaximum;
  }

  public void setLinesRangeMaximum( final Node linesRangeMaximumNode ) {
    if ( linesRangeMaximumNode != null ) {
      setLinesRangeMaximum( Double.parseDouble( linesRangeMaximumNode.getText() ) );
    }

  }

  /**
   * @param double rangeMinimum Set the minimum value of the range axis.
   */
  public void setLinesRangeMaximum( final double linesRangeMaximum ) {
    this.linesRangeMaximum = linesRangeMaximum;
  }

  public void setNoDataMessage( final String msg ) {
    noDataMessage = msg;
  }

}
