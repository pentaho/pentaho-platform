/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.uifoundation.chart;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBubbleRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.RectangleEdge;

import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

/**
 * @author klosei
 * 
 */
public class BubbleRenderer extends XYBubbleRenderer {
  /**
   *
   */
  private static final long serialVersionUID = -216587271628618807L;

  private double maxSize = 0;

  private double maxZ = 0;

  public BubbleRenderer() {
    super();
  }

  /**
   * Draws the visual representation of a single data item.
   * 
   * @param g2
   *          the graphics device.
   * @param state
   *          the renderer state.
   * @param dataArea
   *          the area within which the data is being drawn.
   * @param info
   *          collects information about the drawing.
   * @param plot
   *          the plot (can be used to obtain standard color information etc).
   * @param domainAxis
   *          the domain (horizontal) axis.
   * @param rangeAxis
   *          the range (vertical) axis.
   * @param dataset
   *          the dataset (an {@link XYZDataset} is expected).
   * @param series
   *          the series index (zero-based).
   * @param item
   *          the item index (zero-based).
   * @param crosshairState
   *          crosshair information for the plot (<code>null</code> permitted).
   * @param pass
   *          the pass index.
   */
  @Override
  public void drawItem( final Graphics2D g2, final XYItemRendererState state, final Rectangle2D dataArea,
      final PlotRenderingInfo info, final XYPlot plot, final ValueAxis domainAxis, final ValueAxis rangeAxis,
      final XYDataset dataset, final int series, final int item, final CrosshairState crosshairState, final int pass ) {

    PlotOrientation orientation = plot.getOrientation();

    // get the data point...
    double x = dataset.getXValue( series, item );
    double y = dataset.getYValue( series, item );
    double z = Double.NaN;
    if ( dataset instanceof XYZDataset ) {
      XYZDataset xyzData = (XYZDataset) dataset;
      z = xyzData.getZValue( series, item );
    }
    if ( !Double.isNaN( z ) ) {
      RectangleEdge domainAxisLocation = plot.getDomainAxisEdge();
      RectangleEdge rangeAxisLocation = plot.getRangeAxisEdge();
      double transX = domainAxis.valueToJava2D( x, dataArea, domainAxisLocation );
      double transY = rangeAxis.valueToJava2D( y, dataArea, rangeAxisLocation );

      double circleSize;

      circleSize = maxSize * ( z / maxZ );

      circleSize = Math.abs( circleSize );

      Ellipse2D circle = null;
      if ( orientation == PlotOrientation.VERTICAL ) {
        circle = new Ellipse2D.Double( transX - circleSize / 2.0, transY - circleSize / 2.0, circleSize, circleSize );
      } else if ( orientation == PlotOrientation.HORIZONTAL ) {
        circle = new Ellipse2D.Double( transY - circleSize / 2.0, transX - circleSize / 2.0, circleSize, circleSize );
      }
      g2.setPaint( getItemPaint( series, item ) );
      g2.fill( circle );
      g2.setStroke( getItemOutlineStroke( series, item ) );
      g2.setPaint( getItemOutlinePaint( series, item ) );
      g2.draw( circle );

      if ( isItemLabelVisible( series, item ) ) {
        if ( orientation == PlotOrientation.VERTICAL ) {
          drawItemLabel( g2, orientation, dataset, series, item, transX, transY, false );
        } else if ( orientation == PlotOrientation.HORIZONTAL ) {
          drawItemLabel( g2, orientation, dataset, series, item, transY, transX, false );
        }
      }

      // setup for collecting optional entity info...
      EntityCollection entities = null;
      if ( info != null ) {
        entities = info.getOwner().getEntityCollection();
      }

      // add an entity for the item...
      if ( entities != null ) {
        String tip = null;
        XYToolTipGenerator generator = getToolTipGenerator( series, item );
        if ( generator != null ) {
          tip = generator.generateToolTip( dataset, series, item );
        }
        String url = null;
        if ( getURLGenerator() != null ) {
          url = getURLGenerator().generateURL( dataset, series, item );
        }
        XYItemEntity entity = new XYItemEntity( circle, dataset, series, item, tip, url );
        entities.add( entity );
      }

      int domainAxisIndex = plot.getDomainAxisIndex( domainAxis );
      int rangeAxisIndex = plot.getRangeAxisIndex( rangeAxis );
      updateCrosshairValues( crosshairState, x, y, domainAxisIndex, rangeAxisIndex, transX, transY, orientation );
    }

  }

  /**
   * @return
   */
  public double getMaxZ() {
    return maxZ;
  }

  /**
   * @param maxZ
   */
  public void setMaxZ( final double maxZ ) {
    this.maxZ = maxZ;
  }

  /**
   * @return
   */
  public double getMaxSize() {
    return maxSize;
  }

  /**
   * @param size
   */
  public void setMaxSize( final double size ) {
    this.maxSize = size;
  }

}
