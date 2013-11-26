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

import ofc4j.model.Tooltip;
import ofc4j.model.axis.Axis;
import ofc4j.model.elements.BarChart;
import ofc4j.model.elements.BarChart.Style;
import ofc4j.model.elements.Element;
import ofc4j.model.elements.HorizontalBarChart;
import ofc4j.model.elements.SketchBarChart;
import ofc4j.model.elements.StackedBarChart;
import ofc4j.model.elements.StackedBarChart.Stack;
import ofc4j.model.elements.StackedBarChart.StackKey;
import ofc4j.model.elements.StackedBarChart.StackValue;
import org.dom4j.Node;

import java.util.ArrayList;
import java.util.List;

public class BarChartFactory extends AbstractChartFactory {

  // bar related elements
  private static final String HEIGHT_3D_NODE_LOC = "height-3d"; //$NON-NLS-1$
  private static final String FUN_FACTOR_NODE_LOC = "fun-factor"; //$NON-NLS-1$
  private static final String IS3D_NODE_LOC = "is-3D"; //$NON-NLS-1$
  private static final String ISGLASS_NODE_LOC = "is-glass"; //$NON-NLS-1$
  private static final String ISSKETCH_NODE_LOC = "is-sketch"; //$NON-NLS-1$
  private static final String ISSTACKED_NODE_LOC = "is-stacked"; //$NON-NLS-1$
  private static final String OUTLINE_COLOR_PALETTE_NODE_LOC = "outline-color-palette"; //$NON-NLS-1$

  // defaults
  private static final Style BARCHART_STYLE_DEFAULT = BarChart.Style.NORMAL;
  private static final int SKETCH_FUNFACTOR_DEFAULT = 5;

  // bar related members
  private ArrayList<String> outlineColors = new ArrayList<String>();
  private BarChart.Style barchartstyle;
  private boolean issketch;
  private Integer sketchBarFunFactor;
  private Integer threedheight;
  private StackedBarChart sbc;

  protected boolean isstacked;

  @Override
  public MinMax getRangeMinMax() {
    if ( isstacked ) {
      return new MinMax( 0, getStackedMaxRange() );
    } else {
      MinMax minmax = super.getRangeMinMax();
      if ( minmax.min > 0 ) {
        minmax.min = 0;
      }
      return minmax;
    }
  }

  public Axis setupDomain() {
    Axis axis = super.setupDomain();
    axis.setOffset( true );
    return axis;
  }

  @SuppressWarnings( "unchecked" )
  public int getStackedMaxRange() {
    int maxRange = 0;
    for ( int i = 0; i < sbc.getStackCount(); i++ ) {
      int currRange = 0;
      List<Object> vals = (List<Object>) sbc.getValues().get( i );
      for ( Object val : vals ) {
        currRange += ( (StackValue) val ).getValue().intValue();
      }
      if ( currRange > maxRange ) {
        maxRange = currRange;
      }
    }
    return maxRange;
  }

  @Override
  public void setupStyles() {
    super.setupStyles();
    barchartstyle = BARCHART_STYLE_DEFAULT;

    // 3d
    Node temp = chartNode.selectSingleNode( IS3D_NODE_LOC );
    if ( getValue( temp ) != null && "true".equals( getValue( temp ) ) ) { //$NON-NLS-1$
      barchartstyle = BarChart.Style.THREED;

      // also load 3d height
      temp = chartNode.selectSingleNode( HEIGHT_3D_NODE_LOC );
      if ( getValue( temp ) != null ) {
        threedheight = Integer.parseInt( getValue( temp ) );
      }
    }
    // Glass
    temp = chartNode.selectSingleNode( ISGLASS_NODE_LOC );
    if ( getValue( temp ) != null && "true".equals( getValue( temp ) ) ) { //$NON-NLS-1$
      barchartstyle = BarChart.Style.GLASS;
    }
    // Sketch
    temp = chartNode.selectSingleNode( ISSKETCH_NODE_LOC );
    if ( getValue( temp ) != null && "true".equals( getValue( temp ) ) ) { //$NON-NLS-1$
      issketch = true;
      // Also load fun factor
      temp = chartNode.selectSingleNode( FUN_FACTOR_NODE_LOC );
      if ( getValue( temp ) != null ) {
        sketchBarFunFactor = Integer.parseInt( getValue( temp ) );
      } else {
        sketchBarFunFactor = SKETCH_FUNFACTOR_DEFAULT;
      }
    } else {
      issketch = false;
    }

    // Stacked
    temp = chartNode.selectSingleNode( ISSTACKED_NODE_LOC );
    if ( getValue( temp ) != null ) {
      isstacked = "true".equals( getValue( temp ) ); //$NON-NLS-1$
    }

    temp = chartNode.selectSingleNode( OUTLINE_COLOR_PALETTE_NODE_LOC );
    if ( temp != null ) {
      Object[] colorNodes = temp.selectNodes( COLOR_NODE_LOC ).toArray();
      for ( int j = 0; j < colorNodes.length; j++ ) {
        outlineColors.add( getValue( (Node) colorNodes[j] ) );
      }
    } else {
      for ( int i = 0; i < COLORS_DEFAULT.length; i++ ) {
        outlineColors.add( COLORS_DEFAULT[i] );
      }
    }
  }

  @Override
  public void createElements() {
    if ( CATEGORY_TYPE.equals( datasetType ) ) {
      int columnCount = getColumnCount();
      for ( int col = 0; col < columnCount; col++ ) {
        Element e = null;
        if ( VERTICAL_ORIENTATION.equals( orientation ) ) {
          e = getVerticalBarChartFromColumn( col );
        } else if ( HORIZONTAL_ORIENTATION.equals( orientation ) ) {
          e = getHorizontalBarChartFromColumn( col );

          // Fix for BISERVER-3027, incorrect hover tip placement on hbar
          chart.setTooltip( new Tooltip() );
        }
        elements.add( e );
      }
    }
  }

  public Element getStackedBarChartFromColumn( int col ) {
    if ( sbc == null ) {
      sbc = new StackedBarChart();

      // set the onclick event to the base url template
      if ( null != baseURLTemplate ) {
        sbc.setOn_click( baseURLTemplate );
      }

      if ( alpha != null ) {
        sbc.setAlpha( alpha );
      }
    }

    StackKey key = new StackKey();
    String text = getColumnHeader( col );
    key.setText( text );
    key.setColour( getColor( col ) );
    sbc.addKeys( key );

    for ( int row = 0; row < getRowCount(); row++ ) {
      Stack stack = null;
      if ( sbc.getStackCount() > row ) {
        stack = sbc.stack( row );
      } else {
        stack = sbc.newStack();
      }
      double d = ( (Number) getValueAt( row, col ) ).doubleValue();
      stack.addStackValues( new StackValue( d, getColor( col ) ) );
    }

    return sbc;
  }

  public Element getVerticalBarChartFromColumn( int col ) {
    if ( isstacked ) {
      return getStackedBarChartFromColumn( col );
    } else {
      BarChart bc;
      if ( issketch ) {
        bc = new SketchBarChart();
        ( (SketchBarChart) bc ).setFunFactor( sketchBarFunFactor );
        ( (SketchBarChart) bc ).setOutlineColour( getOutlineColor( col ) );
      } else {
        bc = new BarChart( this.barchartstyle );
        if ( this.barchartstyle == Style.THREED && threedheight != null ) {
          chart.getXAxis().set3D( threedheight );
        }
      }

      for ( int row = 0; row < getRowCount(); row++ ) {
        double d = ( (Number) getValueAt( row, col ) ).doubleValue();
        bc.addBars( new BarChart.Bar( d ) );
      }

      bc.setColour( getColor( col ) );

      if ( tooltipText != null ) {
        bc.setTooltip( tooltipText );
      }

      // set the title for this series
      bc.setText( getColumnHeader( col ) );

      // set the onclick event to the base url template
      if ( null != baseURLTemplate ) {
        bc.setOn_click( baseURLTemplate );
      }

      if ( alpha != null ) {
        bc.setAlpha( alpha );
      }

      return bc;
    }
  }

  public Element getHorizontalBarChartFromColumn( int col ) {
    HorizontalBarChart hbc = new HorizontalBarChart();
    for ( int row = 0; row < getRowCount(); row++ ) {
      double d = ( (Number) getValueAt( row, col ) ).doubleValue();
      hbc.addBars( new HorizontalBarChart.Bar( d ) );
    }
    hbc.setColour( getColor( col ) );
    if ( tooltipText != null ) {
      hbc.setTooltip( tooltipText );
    }

    // set the title for this series
    hbc.setText( getColumnHeader( col ) );

    // set the onclick event to the base url template
    if ( null != baseURLTemplate ) {
      hbc.setOn_click( baseURLTemplate );
    }

    if ( alpha != null ) {
      hbc.setAlpha( alpha );
    }

    return hbc;
  }

  // Utility Methods

  public String getOutlineColor( int i ) {
    return outlineColors.get( i % outlineColors.size() );
  }

}
