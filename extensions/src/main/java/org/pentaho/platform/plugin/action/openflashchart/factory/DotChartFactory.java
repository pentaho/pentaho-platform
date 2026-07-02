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
import ofc4j.model.elements.ScatterChart;
import org.dom4j.Node;
import org.pentaho.platform.plugin.action.messages.Messages;

import java.text.MessageFormat;
import java.text.NumberFormat;

public class DotChartFactory extends AbstractChartFactory {

  private static final String DOT_WIDTH_NODE_LOC = "dot-width"; //$NON-NLS-1$
  private static final String DOT_LABEL_CONTENT_NODE_LOC = "dot-label-content"; //$NON-NLS-1$

  private Integer dotwidth;

  public void validateData() {
    super.validateData();
    if ( getColumnCount() < 2 ) {
      throw new RuntimeException( Messages.getInstance().getErrorString( "DotChartFactory.ERROR_0001_XY_COLUMN_COUNT" ) ); //$NON-NLS-1$
    }
  }

  @Override
  protected void createElements() {
    for ( int row = 0; row < getRowCount(); row++ ) {
      Element e = null;
      String text = getRowHeader( row );
      ScatterChart sc = new ScatterChart( "" ); //$NON-NLS-1$
      sc.setColour( getColor( row ) );
      Number x = (Number) getValueAt( row, 0 );
      Number y = (Number) getValueAt( row, 1 );

      if ( dotwidth != null ) {
        sc.setDotSize( dotwidth );
      }

      Node temp = chartNode.selectSingleNode( DOT_LABEL_CONTENT_NODE_LOC );
      if ( getValue( temp ) != null ) {
        sc.setTooltip( MessageFormat.format( getValue( temp ), text, NumberFormat.getInstance().format( x ),
            NumberFormat.getInstance().format( y ) ) );
      } else {
        sc.setTooltip( MessageFormat.format( "{0}: {1}, {2}", text, //$NON-NLS-1$
            NumberFormat.getInstance().format( x ), NumberFormat.getInstance().format( y ) ) );
      }

      sc.addPoint( x.doubleValue(), y.doubleValue() );

      if ( alpha != null ) {
        sc.setAlpha( alpha );
      }

      e = sc;
      e.setText( text );
      elements.add( e );
    }
  }

  @Override
  protected void setupStyles() {
    super.setupStyles();

    Node temp = chartNode.selectSingleNode( DOT_WIDTH_NODE_LOC );
    if ( getValue( temp ) != null ) {
      dotwidth = Integer.parseInt( getValue( temp ) );
    }
  }

}
