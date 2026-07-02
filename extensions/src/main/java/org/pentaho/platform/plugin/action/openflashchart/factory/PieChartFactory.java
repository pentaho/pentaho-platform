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
