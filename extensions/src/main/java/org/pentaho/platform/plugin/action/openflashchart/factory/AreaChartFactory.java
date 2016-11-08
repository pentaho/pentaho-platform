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

import ofc4j.model.elements.AreaHollowChart;
import ofc4j.model.elements.AreaLineChart;
import ofc4j.model.elements.LineChart;

public class AreaChartFactory extends LineChartFactory {

  @Override
  public LineChart getLineChartFromColumn( int col ) {
    LineChart ac = null;
    if ( linechartstyle != LineChart.Style.HOLLOW ) {
      AreaLineChart ahc = new AreaLineChart();
      ahc.setFill( getColor( col ) );
      ac = ahc;
    } else {
      AreaHollowChart ahc = new AreaHollowChart();
      ahc.setFill( getColor( col ) );
      ac = ahc;
    }

    Number[] numbers = new Number[getRowCount()];
    for ( int row = 0; row < getRowCount(); row++ ) {
      numbers[row] = ( (Number) getValueAt( row, col ) ).doubleValue();
    }

    ac.addValues( numbers );
    ac.setColour( getColor( col ) );

    if ( linechartwidth != null ) {
      ac.setWidth( linechartwidth );
    }
    if ( tooltipText != null ) {
      ac.setTooltip( tooltipText );
    }

    // set the title for this series
    ac.setText( getColumnHeader( col ) );

    // set the onclick event to the base url template
    if ( null != baseURLTemplate ) {
      ac.setOn_click( baseURLTemplate );
    }
    if ( alpha != null ) {
      ac.setAlpha( alpha );
    }

    return ac;
  }
}
