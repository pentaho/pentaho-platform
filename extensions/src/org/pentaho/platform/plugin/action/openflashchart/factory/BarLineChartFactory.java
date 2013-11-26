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

import ofc4j.model.axis.Axis;
import ofc4j.model.axis.YAxis;
import ofc4j.model.elements.Element;
import ofc4j.model.elements.LineChart;
import org.apache.commons.logging.Log;
import org.dom4j.Node;
import org.pentaho.commons.connection.IPentahoResultSet;

import java.util.ArrayList;
import java.util.List;

public class BarLineChartFactory extends BarChartFactory {

  // bar line related elements
  private static final String BAR_SERIES_SERIES_NODE_LOC = "bar-series/series"; //$NON-NLS-1$
  private static final String LINES_RANGE_STROKE_NODE_LOC = "lines-range-stroke"; //$NON-NLS-1$
  private static final String LINES_RANGE_GRID_COLOR_NODE_LOC = "lines-range-grid-color"; //$NON-NLS-1$
  private static final String LINES_RANGE_COLOR_NODE_LOC = "lines-range-color"; //$NON-NLS-1$
  private static final String LINE_SERIES_SERIES_NODE_LOC = "line-series/series"; //$NON-NLS-1$
  private static final String LINES_RANGE_MAXIMUM_NODE_LOC = "lines-range-maximum"; //$NON-NLS-1$
  private static final String LINES_RANGE_MINIMUM_NODE_LOC = "lines-range-minimum"; //$NON-NLS-1$
  private static final String LINE_RANGE_STEPS_NODE_LOC = "line-range-steps"; //$NON-NLS-1$

  LineChartFactory lineChartFactory = new LineChartFactory();

  @Override
  public void setupStyles() {
    super.setupStyles();
    lineChartFactory.setupStyles();
  }

  @Override
  public void setData( IPentahoResultSet data ) {
    super.setData( data );
    data.beforeFirst();
    lineChartFactory.setData( data );
  }

  @Override
  public void setChartNode( Node chartNode ) {
    super.setChartNode( chartNode );
    lineChartFactory.setChartNode( chartNode );
  }

  @Override
  public void setLog( Log log ) {
    super.setLog( log );
    lineChartFactory.setLog( log );
  }

  protected void setupColors() {
    super.setupColors();
    lineChartFactory.setupColors();
  }

  public Axis setupRange() {
    Axis axis = super.setupRange();
    setupLineRange();
    return axis;
  }

  @SuppressWarnings ( "unchecked" )
  public MinMax getRangeMinMax() {
    int rangeMin = 0;
    int rangeMax = 0;
    if ( isstacked ) {
      rangeMin = 0;
      rangeMax = getStackedMaxRange();
    } else {
      rangeMin = Integer.MAX_VALUE;
      rangeMax = Integer.MIN_VALUE;
      List nodes = chartNode.selectNodes( BAR_SERIES_SERIES_NODE_LOC );
      List<String> bars = new ArrayList<String>();
      for ( Object node : nodes ) {
        if ( getValue( (Node) node ) != null ) {
          bars.add( getValue( (Node) node ) );
        }
      }

      for ( int c = 0; c < getColumnCount(); c++ ) {
        String text = getColumnHeader( c );
        if ( bars.contains( text ) ) {
          for ( int r = 0; r < getRowCount(); r++ ) {
            if ( rangeMin > ( (Number) getValueAt( r, c ) ).intValue() ) {
              rangeMin = ( (Number) getValueAt( r, c ) ).intValue();
            }
            if ( rangeMax < ( (Number) getValueAt( r, c ) ).intValue() ) {
              rangeMax = ( (Number) getValueAt( r, c ) ).intValue();
            }
          }
        }
      }
    }

    if ( rangeMin > 0 ) {
      rangeMin = 0;
    }

    return new MinMax( rangeMin, rangeMax );
  }

  @SuppressWarnings ( "unchecked" )
  public void setupLineRange() {
    int rangeMin = 0;
    int rangeMax = 100;
    int steps = 9;

    String rangeColor = AXIS_COLOR_DEFAULT;
    String rangeGridColor = AXIS_GRID_COLOR_DEFAULT;
    int rangeStroke = 1;

    if ( CATEGORY_TYPE.equals( datasetType ) || XYZ_TYPE.equals( datasetType ) ) {
      rangeMin = Integer.MAX_VALUE;
      rangeMax = Integer.MIN_VALUE;
      List nodes = chartNode.selectNodes( LINE_SERIES_SERIES_NODE_LOC );
      List<String> bars = new ArrayList<String>();
      for ( Object node : nodes ) {
        if ( getValue( (Node) node ) != null ) {
          bars.add( getValue( (Node) node ) );
        }
      }

      for ( int c = 0; c < getColumnCount(); c++ ) {
        String text = getColumnHeader( c );
        if ( bars.contains( text ) ) {
          for ( int r = 0; r < getRowCount(); r++ ) {
            if ( rangeMin > ( (Number) getValueAt( r, c ) ).intValue() ) {
              rangeMin = ( (Number) getValueAt( r, c ) ).intValue();
            }
            if ( rangeMax < ( (Number) getValueAt( r, c ) ).intValue() ) {
              rangeMax = ( (Number) getValueAt( r, c ) ).intValue();
            }
          }
        }
      }
    }

    boolean minDefined = false;
    boolean maxDefined = false;

    Node temp = chartNode.selectSingleNode( LINES_RANGE_MINIMUM_NODE_LOC );
    if ( getValue( temp ) != null ) {
      rangeMin = new Integer( getValue( temp ) ).intValue();
      minDefined = true;
    }

    temp = chartNode.selectSingleNode( LINES_RANGE_MAXIMUM_NODE_LOC );
    if ( getValue( temp ) != null ) {
      rangeMax = new Integer( getValue( temp ) ).intValue();
      maxDefined = true;
    }

    temp = chartNode.selectSingleNode( LINES_RANGE_COLOR_NODE_LOC );
    if ( getValue( temp ) != null ) {
      rangeColor = getValue( temp );
    }

    temp = chartNode.selectSingleNode( LINES_RANGE_GRID_COLOR_NODE_LOC );
    if ( getValue( temp ) != null ) {
      rangeGridColor = getValue( temp );
    }

    temp = chartNode.selectSingleNode( LINES_RANGE_STROKE_NODE_LOC );
    if ( getValue( temp ) != null ) {
      rangeStroke = Integer.parseInt( getValue( temp ) );
    }

    temp = chartNode.selectSingleNode( LINE_RANGE_STEPS_NODE_LOC );
    if ( getValue( temp ) != null ) {
      steps = new Integer( getValue( temp ) ).intValue();
    }

    int diff = rangeMax - rangeMin;

    int chunksize = diff / steps;

    Integer stepforchart = null;
    if ( chunksize > 0 ) {
      stepforchart = new Integer( chunksize );
    }

    // Readjust mins/maxs only if they weren't specified
    if ( !minDefined ) {
      // If actual min is positive, don't go below ZERO
      if ( rangeMin >= 0 && rangeMin - chunksize < 0 ) {
        rangeMin = 0;
      } else {
        rangeMin = rangeMin - chunksize;
      }
    }
    if ( !maxDefined ) {
      rangeMax = rangeMin + ( chunksize * ( steps + 2 ) );
    }

    YAxis yaxis = new YAxis();
    yaxis.setRange( rangeMin, rangeMax, stepforchart );
    yaxis.setStroke( rangeStroke );
    yaxis.setColour( rangeColor );
    yaxis.setGridColour( rangeGridColor );
    chart.setYAxisRight( yaxis );
  }

  @Override
  public void createElements() {
    if ( CATEGORY_TYPE.equals( datasetType ) ) {
      int columnCount = getColumnCount();
      for ( int col = 0; col < columnCount; col++ ) {
        elements.add( getBarLineChartFromColumn( col ) );
      }
    }
  }

  @SuppressWarnings ( "unchecked" )
  public Element getBarLineChartFromColumn( int col ) {
    String text = getColumnHeader( col );
    // determine if this is a line or a bar
    List nodes = chartNode.selectNodes( BAR_SERIES_SERIES_NODE_LOC );
    List<String> bars = new ArrayList<String>();
    for ( Object node : nodes ) {
      if ( getValue( (Node) node ) != null ) {
        bars.add( getValue( (Node) node ) );
      }
    }
    if ( !bars.contains( text ) ) {
      LineChart lc = lineChartFactory.getLineChartFromColumn( col );
      lc.setRightYAxis();
      return lc;
    } else {
      return getVerticalBarChartFromColumn( col );
    }

  }

}
