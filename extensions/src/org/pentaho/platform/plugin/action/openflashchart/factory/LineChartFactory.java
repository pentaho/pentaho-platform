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

package org.pentaho.platform.plugin.action.openflashchart.factory;

import ofc4j.model.elements.LineChart;
import org.dom4j.Node;

public class LineChartFactory extends AbstractChartFactory {

  // line related elements
  private static final String LINE_WIDTH_NODE_LOC = "line-width"; //$NON-NLS-1$
  private static final String DOTSTYLE_NODE_LOC = "dot-style"; //$NON-NLS-1$
  private static final String DOT_WIDTH_NODE_LOC = "dot-width"; //$NON-NLS-1$

  // defaults
  private static final LineChart.Style LINECHART_STYLE_DEFAULT = LineChart.Style.NORMAL;

  // line related members
  protected LineChart.Style linechartstyle;
  protected Integer linechartwidth;
  protected Integer dotwidth;

  @Override
  protected void createElements() {
    if ( CATEGORY_TYPE.equals( datasetType ) ) {
      int columnCount = getColumnCount();

      // Create a "series" or element for each column past the first
      for ( int col = 0; col < columnCount; col++ ) {
        elements.add( getLineChartFromColumn( col ) );
      }
    }
  }

  public LineChart getLineChartFromColumn( int col ) {
    LineChart lc = new LineChart( this.linechartstyle );
    for ( int row = 0; row < getRowCount(); row++ ) {
      double d = ( (Number) getValueAt( row, col ) ).doubleValue();
      LineChart.Dot dot = new LineChart.Dot( d );

      if ( dotwidth != null ) {
        dot.setDotSize( dotwidth );
      }
      lc.addDots( dot );
    }
    if ( linechartwidth != null ) {
      lc.setWidth( linechartwidth );
    }

    lc.setColour( getColor( col ) );

    if ( tooltipText != null ) {
      lc.setTooltip( tooltipText );
    }

    // set the title for this series
    lc.setText( getColumnHeader( col ) );

    // set the onclick event to the base url template
    if ( null != baseURLTemplate ) {
      lc.setOn_click( baseURLTemplate );
    }

    if ( alpha != null ) {
      lc.setAlpha( alpha );
    }

    return lc;
  }

  @Override
  protected void setupStyles() {
    super.setupStyles();

    Node temp = chartNode.selectSingleNode( DOTSTYLE_NODE_LOC );

    if ( getValue( temp ) != null ) {
      if ( "dot".equals( getValue( temp ) ) ) {
        linechartstyle = LineChart.Style.DOT;
      } else if ( "normal".equals( getValue( temp ) ) ) {
        linechartstyle = LineChart.Style.NORMAL;
      } else if ( "hollow".equals( getValue( temp ) ) ) {
        linechartstyle = LineChart.Style.HOLLOW;
      } else {
        linechartstyle = LINECHART_STYLE_DEFAULT;
      }
    } else {
      linechartstyle = LINECHART_STYLE_DEFAULT;
    }

    temp = chartNode.selectSingleNode( LINE_WIDTH_NODE_LOC );
    if ( getValue( temp ) != null ) {
      // parse with double so 1.0 is parsable
      linechartwidth = (int) Double.parseDouble( getValue( temp ) );
    }

    temp = chartNode.selectSingleNode( DOT_WIDTH_NODE_LOC );
    if ( getValue( temp ) != null ) {
      dotwidth = Integer.parseInt( getValue( temp ) );
    }
  }
}
