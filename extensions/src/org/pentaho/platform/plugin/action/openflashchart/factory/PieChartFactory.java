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

import ofc4j.model.elements.Element;
import ofc4j.model.elements.PieChart;
import org.dom4j.Node;

public class PieChartFactory extends AbstractChartFactory {

  // pie related elements
  private static final String START_ANGLE_NODE_LOC = "start-angle"; //$NON-NLS-1$
  private static final String ANIMATE_NODE_LOC = "animate"; //$NON-NLS-1$

  // pie related members
  private boolean animate;
  private Integer startAngle;

  @Override
  protected void createElements() {
    elements.add( getPieChartFromColumn( 0 ) );
  }

  public Element getPieChartFromColumn( int col ) {
    PieChart pc = new PieChart();
    PieChart.Slice[] slices = new PieChart.Slice[getRowCount()];
    for ( int row = 0; row < getRowCount(); row++ ) {
      double d = ( (Number) getValueAt( row, col ) ).doubleValue();
      // Labels are already set - use them
      String label = (String) chart.getXAxis().getLabels().getLabels().get( row );

      slices[row] = new PieChart.Slice( d, label, label );
      if ( tooltipText != null ) {
        slices[row].setTooltip( tooltipText );
      }
    }

    pc.addSlices( slices );
    pc.setColours( colors );
    pc.setStartAngle( startAngle );
    pc.setAnimate( animate );
    if ( alpha != null ) {
      pc.setAlpha( alpha );
    }
    return pc;
  }

  @Override
  protected void setupStyles() {
    super.setupStyles();

    Node temp = chartNode.selectSingleNode( ANIMATE_NODE_LOC );
    if ( getValue( temp ) != null ) {
      animate = "true".equals( getValue( temp ) ); //$NON-NLS-1$
    }

    temp = chartNode.selectSingleNode( START_ANGLE_NODE_LOC );
    if ( getValue( temp ) != null ) {
      startAngle = Integer.parseInt( getValue( temp ) );
    }
  }

}
