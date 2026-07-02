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

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;

public class BubbleChartFactory extends AbstractChartFactory {

  private static final String MAX_BUBBLE_SIZE_NODE_LOC = "max-bubble-size"; //$NON-NLS-1$
  private static final String BUBBLE_LABEL_Z_FORMAT_NODE_LOC = "bubble-label-z-format"; //$NON-NLS-1$
  private static final String BUBBLE_LABEL_CONTENT_NODE_LOC = "bubble-label-content"; //$NON-NLS-1$

  // scatter / bubble members
  private Number bubbleMaxX;
  private int maxBubbleSize = 100;

  @Override
  public void validateData() {
    super.validateData();
    if ( getColumnCount() < 3 ) {
      throw new RuntimeException( Messages.getInstance().getErrorString(
          "BubbleChartFactory.ERROR_0001_XYZ_COLUMN_COUNT" ) ); //$NON-NLS-1$
    }
  }

  @Override
  protected void createElements() {
    Node contentNode = chartNode.selectSingleNode( BUBBLE_LABEL_CONTENT_NODE_LOC );
    String content = getValue( contentNode );
    Node zFormatNode = chartNode.selectSingleNode( BUBBLE_LABEL_Z_FORMAT_NODE_LOC );
    String zFormat = getValue( zFormatNode );
    DecimalFormat df = null;
    if ( zFormat != null ) {
      df = new DecimalFormat( zFormat );
    }

    for ( int row = 0; row < getRowCount(); row++ ) {
      Element e = null;
      String text = getRowHeader( row );

      ScatterChart sc = new ScatterChart( "" ); //$NON-NLS-1$
      sc.setColour( getColor( row ) );
      Number x = (Number) getValueAt( row, 0 );
      Number y = (Number) getValueAt( row, 1 );
      Number z = (Number) getValueAt( row, 2 );

      setupDotSize( sc, z );

      sc.addPoint( x.doubleValue(), y.doubleValue() );

      if ( content != null ) {
        String zstr = null;
        if ( df != null ) {
          zstr = df.format( z );
        } else {
          if ( z != null ) {
            zstr = z.toString();
          }
        }
        sc.setTooltip( MessageFormat.format( content, text, NumberFormat.getInstance().format( x ), NumberFormat
            .getInstance().format( y ), zstr ) );
      }

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
    // max bubble x

    Number maxX = 0;
    int rowCount = getRowCount();
    for ( int row = 0; row < rowCount; row++ ) {
      Number currx = (Number) getValueAt( row, 2 );
      if ( maxX.doubleValue() < currx.doubleValue() ) {
        maxX = currx;
      }
    }
    bubbleMaxX = maxX;

    // max bubble size

    Node bubbleSizeNode = chartNode.selectSingleNode( MAX_BUBBLE_SIZE_NODE_LOC );
    if ( getValue( bubbleSizeNode ) != null ) {
      maxBubbleSize = Integer.parseInt( getValue( bubbleSizeNode ) );
    }
  }

  public void setupDotSize( ScatterChart se, Number x ) {
    se.setDotSize( Integer
        .valueOf( java.lang.Math.round( maxBubbleSize * ( x.floatValue() / bubbleMaxX.floatValue() ) ) ) );
  }

}
