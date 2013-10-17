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
