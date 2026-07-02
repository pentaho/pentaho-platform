/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


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
