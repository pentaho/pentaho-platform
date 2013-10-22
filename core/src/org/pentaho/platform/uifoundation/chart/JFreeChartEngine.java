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

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Node;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberAxis3D;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.PieToolTipGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieToolTipGenerator;
import org.jfree.chart.labels.StandardXYSeriesLabelGenerator;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.StandardXYZToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.MeterInterval;
import org.jfree.chart.plot.MeterPlot;
import org.jfree.chart.plot.MultiplePiePlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.AreaRenderer;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.BarRenderer3D;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.LineRenderer3D;
import org.jfree.chart.renderer.category.StackedAreaRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer3D;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.renderer.xy.StackedXYAreaRenderer2;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLine3DRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYStepAreaRenderer;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.urls.PieURLGenerator;
import org.jfree.chart.urls.StandardCategoryURLGenerator;
import org.jfree.chart.urls.StandardPieURLGenerator;
import org.jfree.chart.urls.StandardXYURLGenerator;
import org.jfree.data.Range;
import org.jfree.data.general.Dataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;
import org.jfree.util.TableOrder;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.actionsequence.ActionSequenceResource;
import org.pentaho.platform.uifoundation.messages.Messages;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.w3c.dom.Document;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

public class JFreeChartEngine {

  protected static final Log log = LogFactory.getLog( JFreeChartEngine.class );

  public static final int OUTPUT_CHART = 3;

  public static final int OUTPUT_PNG = 1;

  public static final int OUTPUT_SVG = 2;

  public static final int OUTPUT_PNG_BYTES = 4;

  // Node names
  private static final String X1_NODE_NAME = "x1"; //$NON-NLS-1$

  private static final String Y1_NODE_NAME = "y1"; //$NON-NLS-1$

  private static final String X2_NODE_NAME = "x2"; //$NON-NLS-1$

  private static final String Y2_NODE_NAME = "y2"; //$NON-NLS-1$

  private static final String COLOR1_NODE_NAME = "color1"; //$NON-NLS-1$

  private static final String COLOR2_NODE_NAME = "color2"; //$NON-NLS-1$

  private static final String CYCLIC_NODE_NAME = "cyclic"; //$NON-NLS-1$

  private static final String TEXTURE_IMAGE_NODE_NAME = "texture-image"; //$NON-NLS-1$

  private static final String FONT_FAMILY_NODE_NAME = "font-family"; //$NON-NLS-1$

  private static final String SIZE_NODE_NAME = "size"; //$NON-NLS-1$

  private static final String IS_ITALIC_NODE_NAME = "is-italic"; //$NON-NLS-1$

  private static final String IS_BOLD_NODE_NAME = "is-bold"; //$NON-NLS-1$

  public static final int UNDEFINED_CHART_TYPE = -1;

  public static final int DIAL_CHART_TYPE = 0;

  public static final int THERMOMETER_CHART_TYPE = 1;

  public static final int PIE_CHART_TYPE = 2;

  public static final int PIE_GRID_CHART_TYPE = 3;

  public static final int BAR_CHART_TYPE = 4;

  public static final int LINE_CHART_TYPE = 5;

  public static final int AREA_CHART_TYPE = 6;

  public static final int STEP_CHART_TYPE = 7;

  public static final int STEP_AREA_CHART_TYPE = 8;

  public static final int DIFFERENCE_CHART_TYPE = 9;

  public static final int DOT_CHART_TYPE = 10;

  // new chart types
  public static final int BAR_LINE_CHART_TYPE = 11;

  public static final int BUBBLE_CHART_TYPE = 12;

  // end new chart type

  /**
   * Creates a JFreeChart object from a dial definition object. All of the settings for the dial image are defined
   * by the dial definition.
   * 
   * @param widget
   *          The dial definition object.
   * @param title
   *          The dial title.
   * @param units
   *          The dial units
   * @return A JFreeChart object for the dial
   */
  private static JFreeChart createDial( final DialWidgetDefinition widget, final String title, final String units ) {
    MeterPlot meter = new MeterPlot( widget );

    JFreeChartEngine.updatePlot( meter, widget );
    meter.setUnits( units );

    meter.setValuePaint( widget.getValuePaint() );
    meter.setTickPaint( widget.getTickPaint() );
    int tickSize = widget.getTickSize();
    if ( tickSize == 0 ) {
      meter.setTickLabelsVisible( false );
    } else {
      meter.setTickLabelsVisible( true );
      meter.setTickSize( tickSize );
    }
    if ( widget.getValueFont() != null ) {
      meter.setValueFont( widget.getValueFont() );
    } else {
      meter.setValuePaint( JFreeChartEngine.getTransparent() );
    }
    // TODO support title fonts in the definition
    Font titleFont = widget.getTitleFont();

    JFreeChart chart = new JFreeChart( title, titleFont, meter, false );

    // TODO support any other MeterPlot properties

    return chart;
  }

  /*
   * New char types based on the XYZSeriesCollectionChartDefinition
   */
  private static JFreeChart createXYZSeriesCollectionChart( final XYZSeriesCollectionChartDefinition chartDefinition ) {
    JFreeChart chart = null;
    // TODO Make the following accessible from the chartDefinition
    String domainAxisLabel = null;
    String rangeAxisLabel = null;
    boolean tooltips = true;
    boolean urls = true;
    // -----------------------------------------------------------

    String title = chartDefinition.getTitle();
    boolean legend = chartDefinition.isLegendIncluded();

    NumberAxis domainAxis =
        chartDefinition.isThreeD() ? new NumberAxis3D( domainAxisLabel ) : new NumberAxis( domainAxisLabel );
    domainAxis.setAutoRangeIncludesZero( chartDefinition.isDomainIncludesZero() );
    domainAxis.setAutoRangeStickyZero( chartDefinition.isDomainStickyZero() );

    NumberAxis rangeAxis = new NumberAxis( rangeAxisLabel );
    rangeAxis.setAutoRangeIncludesZero( chartDefinition.isRangeIncludesZero() );
    rangeAxis.setAutoRangeStickyZero( chartDefinition.isRangeStickyZero() );

    BubbleRenderer renderer = null;

    // So far only Bubble charts are supported
    switch ( chartDefinition.getChartType() ) {
      case BUBBLE_CHART_TYPE:
        renderer = new BubbleRenderer();
        break;
      default:
        // should log an error if invalid chart type passed in - at least return null for no renderer
        return null;
    }

    if ( tooltips ) {
      // creating the label definition

      renderer.setToolTipGenerator( new StandardXYZToolTipGenerator( chartDefinition.getBubbleLabelContent(),
          chartDefinition.getXFormat(), chartDefinition.getYFormat(), chartDefinition.getZFormat() ) );
    }
    if ( urls ) {
      renderer.setURLGenerator( new StandardBubbleURLGenerator() );
    }

    renderer.setMaxSize( chartDefinition.getMaxBubbleSize() );
    renderer.setMaxZ( chartDefinition.getMaxZValue() );

    XYPlot plot = new XYPlot( chartDefinition, domainAxis, rangeAxis, renderer );
    JFreeChartEngine.updatePlot( plot, chartDefinition );

    chart = new JFreeChart( title, JFreeChart.DEFAULT_TITLE_FONT, plot, legend );

    return chart;
  }

  private static JFreeChart createXYSeriesCollectionChart( final XYSeriesCollectionChartDefinition chartDefinition ) {
    JFreeChart chart = null;
    // TODO Make the following accessible from the chartDefinition
    String domainAxisLabel = null;
    String rangeAxisLabel = null;
    boolean tooltips = true;
    boolean urls = true;
    // -----------------------------------------------------------

    String title = chartDefinition.getTitle();
    boolean legend = chartDefinition.isLegendIncluded();

    NumberAxis domainAxis =
        chartDefinition.isThreeD() ? new NumberAxis3D( domainAxisLabel ) : new NumberAxis( domainAxisLabel );
    domainAxis.setAutoRangeIncludesZero( chartDefinition.isDomainIncludesZero() );
    domainAxis.setAutoRangeStickyZero( chartDefinition.isDomainStickyZero() );
    ValueAxis rangeAxis = new NumberAxis( rangeAxisLabel );

    AbstractXYItemRenderer renderer = null;
    switch ( chartDefinition.getChartType() ) {
      case LINE_CHART_TYPE:
        renderer = chartDefinition.isThreeD() ? new XYLine3DRenderer() : new XYLineAndShapeRenderer( true, false );
        ( (XYLineAndShapeRenderer) renderer ).setShapesVisible( chartDefinition.isMarkersVisible() );
        ( (XYLineAndShapeRenderer) renderer ).setBaseShapesFilled( chartDefinition.isMarkersVisible() );
        break;
      case AREA_CHART_TYPE:
        renderer = new XYAreaRenderer();
        break;
      case STEP_CHART_TYPE:
        renderer = new XYStepRenderer();
        break;
      case STEP_AREA_CHART_TYPE:
        renderer = new XYStepAreaRenderer();
        break;
      case DIFFERENCE_CHART_TYPE:
        renderer = new XYDifferenceRenderer();
        break;
      case DOT_CHART_TYPE:
        renderer = new XYDotRenderer();
        ( (XYDotRenderer) renderer ).setDotHeight( chartDefinition.getDotHeight() );
        ( (XYDotRenderer) renderer ).setDotWidth( chartDefinition.getDotWidth() );
        break;
      default:
        // should log an error if invalid chart type passed in - at least return null for no renderer
        return null;
    }

    if ( renderer != null && legend ) {
      renderer.setLegendItemURLGenerator( new StandardXYSeriesLabelGenerator() );
    }

    if ( tooltips ) {
      XYToolTipGenerator generator =
          new StandardXYToolTipGenerator( chartDefinition.getTooltipContent(), new DecimalFormat( chartDefinition
              .getTooltipXFormat() ), new DecimalFormat( chartDefinition.getTooltipYFormat() ) );
      renderer.setToolTipGenerator( generator );
    }

    if ( urls ) {
      renderer.setURLGenerator( new StandardXYURLGenerator() );
    }

    renderer.setStroke( JFreeChartEngine.getLineStyleStroke( chartDefinition.getLineStyle(), chartDefinition
        .getLineWidth() ) );

    XYPlot plot = new XYPlot( chartDefinition, domainAxis, rangeAxis, renderer );
    JFreeChartEngine.updatePlot( plot, chartDefinition );

    chart = new JFreeChart( title, JFreeChart.DEFAULT_TITLE_FONT, plot, legend );

    return chart;

  }

  private static JFreeChart createTimeSeriesCollectionChart( final
                                                             TimeSeriesCollectionChartDefinition chartDefinition ) {
    JFreeChart chart = null;
    // TODO Make the following accessible from the chartDefinition
    String domainAxisLabel = null;
    String rangeAxisLabel = null;
    boolean tooltips = true;
    boolean urls = true;
    // -----------------------------------------------------------

    String title = chartDefinition.getTitle();
    boolean legend = chartDefinition.isLegendIncluded();

    DateAxis domainAxis = new DateAxis( domainAxisLabel, TimeZone.getDefault() );
    ValueAxis rangeAxis = new NumberAxis( rangeAxisLabel );

    XYItemRenderer renderer = null;
    switch ( chartDefinition.getChartType() ) {
      case LINE_CHART_TYPE:
        renderer = chartDefinition.isThreeD() ? new XYLine3DRenderer() : new XYLineAndShapeRenderer( true, false );
        ( (XYLineAndShapeRenderer) renderer ).setShapesVisible( chartDefinition.isMarkersVisible() );
        ( (XYLineAndShapeRenderer) renderer ).setBaseShapesFilled( chartDefinition.isMarkersVisible() );
        break;
      case AREA_CHART_TYPE:
        renderer = new XYAreaRenderer();
        break;
      case STEP_CHART_TYPE:
        renderer = new XYStepRenderer();
        break;
      case STEP_AREA_CHART_TYPE:
        renderer = new XYStepAreaRenderer();
        break;
      case DIFFERENCE_CHART_TYPE:
        renderer = new XYDifferenceRenderer();
        break;
      case DOT_CHART_TYPE:
        renderer = new XYDotRenderer();
        ( (XYDotRenderer) renderer ).setDotHeight( chartDefinition.getDotHeight() );
        ( (XYDotRenderer) renderer ).setDotWidth( chartDefinition.getDotWidth() );
        break;
      default:
        // should log an error if invalid chart type passed in - at least return null for no renderer
        return null;
    }
    if ( tooltips ) {
      XYToolTipGenerator generator =
          new StandardXYToolTipGenerator( chartDefinition.getTooltipContent(), new SimpleDateFormat( chartDefinition
              .getTooltipXFormat() ), new DecimalFormat( chartDefinition.getTooltipYFormat() ) );
      renderer.setToolTipGenerator( generator );
    }
    if ( urls ) {
      renderer.setURLGenerator( new StandardXYURLGenerator() );
    }

    renderer.setStroke( JFreeChartEngine.getLineStyleStroke( chartDefinition.getLineStyle(), chartDefinition
        .getLineWidth() ) );

    XYPlot plot = new XYPlot( chartDefinition, domainAxis, rangeAxis, renderer );
    JFreeChartEngine.updatePlot( plot, chartDefinition );

    chart = new JFreeChart( title, JFreeChart.DEFAULT_TITLE_FONT, plot, legend );

    return chart;

  }

  /*
   * The only type of chart this method will produce is a stacked XY area chart with a time series dimension. This
   * is because the dataset that jFreeChart expects is different than the regular TimeSeriesCOllection. More
   * lipstick on this homely pig.
   */
  private static JFreeChart createStackedTimeSeriesChart( final TimeTableXYDatasetChartDefinition chartDefinition ) {
    JFreeChart chart = null;
    // TODO Make the following accessible from the chartDefinition
    String domainAxisLabel = null;
    String rangeAxisLabel = null;
    boolean tooltips = true;
    boolean urls = true;
    // -----------------------------------------------------------

    String title = chartDefinition.getTitle();
    boolean legend = chartDefinition.isLegendIncluded();

    DateAxis domainAxis = new DateAxis( domainAxisLabel, TimeZone.getDefault() );
    ValueAxis rangeAxis = new NumberAxis( rangeAxisLabel );

    XYItemRenderer renderer = null;
    switch ( chartDefinition.getChartType() ) {
      case AREA_CHART_TYPE:
        renderer = chartDefinition.isStacked() ? new StackedXYAreaRenderer2() : new XYAreaRenderer();
        break;
      default:
        // should log an error if invalid chart type passed in - at least return null for no renderer
        return null;
    }
    if ( tooltips ) {
      XYToolTipGenerator generator =
          new StandardXYToolTipGenerator( chartDefinition.getTooltipContent(), new SimpleDateFormat( chartDefinition
              .getTooltipXFormat() ), new DecimalFormat( chartDefinition.getTooltipYFormat() ) );
      renderer.setToolTipGenerator( generator );
    }
    if ( urls ) {
      renderer.setURLGenerator( new StandardXYURLGenerator() );
    }

    renderer.setStroke( JFreeChartEngine.getLineStyleStroke( chartDefinition.getLineStyle(), chartDefinition
        .getLineWidth() ) );

    XYPlot plot = new XYPlot( chartDefinition, domainAxis, rangeAxis, renderer );
    JFreeChartEngine.updatePlot( plot, chartDefinition );

    chart = new JFreeChart( title, JFreeChart.DEFAULT_TITLE_FONT, plot, legend );

    return chart;

  }

  private static JFreeChart createCategoryDatasetChart( final CategoryDatasetChartDefinition chartDefinition ) {
    JFreeChart chart = null;
    switch ( chartDefinition.getChartType() ) {
      case BAR_CHART_TYPE:
        chart = JFreeChartEngine.createBarChart( chartDefinition );
        break;
      case LINE_CHART_TYPE:
        chart = JFreeChartEngine.createLineChart( chartDefinition );
        break;
      case PIE_GRID_CHART_TYPE:
        chart = JFreeChartEngine.createMultiplePieChart( chartDefinition );
        break;
      case AREA_CHART_TYPE:
        chart = JFreeChartEngine.createAreaChart( chartDefinition );
        break;
      default:
        break;
    }
    return chart;
  }

  private static JFreeChart createPieDatasetChart( final PieDatasetChartDefinition chartDefinition ) {
    // TODO Make the following accessible from the chartDefinition
    boolean tooltips = true;
    boolean urls = true;
    // -----------------------------------------------------------

    String title = chartDefinition.getTitle();
    boolean legend = chartDefinition.isLegendIncluded();

    PiePlot plot = null;
    plot = chartDefinition.isThreeD() ? new PiePlot3D( chartDefinition ) : new PiePlot( chartDefinition );
    JFreeChartEngine.updatePlot( plot, chartDefinition );
    JFreeChart pieChart = new JFreeChart( title, chartDefinition.getTitleFont(), plot, legend );
    TextTitle seriesTitle = new TextTitle( "Series Title", new Font( "SansSerif", Font.BOLD, 12 ) ); //$NON-NLS-1$ //$NON-NLS-2$
    seriesTitle.setPosition( RectangleEdge.BOTTOM );
    pieChart.setTitle( title );
    pieChart.setBackgroundPaint( chartDefinition.getChartBackgroundPaint() );

    if ( tooltips ) {
      PieToolTipGenerator tooltipGenerator = new StandardPieToolTipGenerator();
      plot.setToolTipGenerator( tooltipGenerator );
    }

    if ( urls ) {
      PieURLGenerator urlGenerator = new StandardPieURLGenerator();
      plot.setURLGenerator( urlGenerator );
    }

    return pieChart;
  }

  private static void updatePlot( final Plot plot, final ChartDefinition chartDefinition ) {
    plot.setBackgroundPaint( chartDefinition.getPlotBackgroundPaint() );
    plot.setBackgroundImage( chartDefinition.getPlotBackgroundImage() );

    plot.setNoDataMessage( chartDefinition.getNoDataMessage() );

    // create a custom palette if it was defined
    if ( chartDefinition.getPaintSequence() != null ) {
      DefaultDrawingSupplier drawingSupplier =
          new DefaultDrawingSupplier( chartDefinition.getPaintSequence(),
              DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE, DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE,
              DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE, DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE );
      plot.setDrawingSupplier( drawingSupplier );
    }
    plot.setOutlineStroke( null ); // TODO define outline stroke

    if ( plot instanceof CategoryPlot ) {
      CategoryPlot categoryPlot = (CategoryPlot) plot;
      CategoryDatasetChartDefinition categoryDatasetChartDefintion = (CategoryDatasetChartDefinition) chartDefinition;
      categoryPlot.setOrientation( categoryDatasetChartDefintion.getOrientation() );
      CategoryAxis domainAxis = categoryPlot.getDomainAxis();
      if ( domainAxis != null ) {
        domainAxis.setLabel( categoryDatasetChartDefintion.getDomainTitle() );
        domainAxis.setLabelFont( categoryDatasetChartDefintion.getDomainTitleFont() );
        if ( categoryDatasetChartDefintion.getDomainTickFont() != null ) {
          domainAxis.setTickLabelFont( categoryDatasetChartDefintion.getDomainTickFont() );
        }
        domainAxis.setCategoryLabelPositions( categoryDatasetChartDefintion.getCategoryLabelPositions() );
      }
      NumberAxis numberAxis = (NumberAxis) categoryPlot.getRangeAxis();
      if ( numberAxis != null ) {
        numberAxis.setLabel( categoryDatasetChartDefintion.getRangeTitle() );
        numberAxis.setLabelFont( categoryDatasetChartDefintion.getRangeTitleFont() );
        if ( categoryDatasetChartDefintion.getRangeMinimum() != ValueAxis.DEFAULT_LOWER_BOUND ) {
          numberAxis.setLowerBound( categoryDatasetChartDefintion.getRangeMinimum() );
        }
        if ( categoryDatasetChartDefintion.getRangeMaximum() != ValueAxis.DEFAULT_UPPER_BOUND ) {
          numberAxis.setUpperBound( categoryDatasetChartDefintion.getRangeMaximum() );
        }

        if ( categoryDatasetChartDefintion.getRangeTickFormat() != null ) {
          numberAxis.setNumberFormatOverride( categoryDatasetChartDefintion.getRangeTickFormat() );
        }
        if ( categoryDatasetChartDefintion.getRangeTickFont() != null ) {
          numberAxis.setTickLabelFont( categoryDatasetChartDefintion.getRangeTickFont() );
        }
        if ( categoryDatasetChartDefintion.getRangeTickUnits() != null ) {
          numberAxis.setTickUnit( new NumberTickUnit( categoryDatasetChartDefintion.getRangeTickUnits() ) );
        }
      }

    }
    if ( plot instanceof PiePlot ) {
      PiePlot pie = (PiePlot) plot;
      PieDatasetChartDefinition pieDefinition = (PieDatasetChartDefinition) chartDefinition;
      pie.setInteriorGap( pieDefinition.getInteriorGap() );
      pie.setStartAngle( pieDefinition.getStartAngle() );
      pie.setLabelFont( pieDefinition.getLabelFont() );
      if ( pieDefinition.getLabelPaint() != null ) {
        pie.setLabelPaint( pieDefinition.getLabelPaint() );
      }
      pie.setLabelBackgroundPaint( pieDefinition.getLabelBackgroundPaint() );
      if ( pieDefinition.isLegendIncluded() ) {
        StandardPieSectionLabelGenerator labelGen = new StandardPieSectionLabelGenerator( "{0}" ); //$NON-NLS-1$
        pie.setLegendLabelGenerator( labelGen );
      }
      if ( pieDefinition.getExplodedSlices() != null ) {
        for ( Iterator iter = pieDefinition.getExplodedSlices().iterator(); iter.hasNext(); ) {
          pie.setExplodePercent( (Comparable) iter.next(), .30 );
        }
      }
      pie.setLabelGap( pieDefinition.getLabelGap() );
      if ( !pieDefinition.isDisplayLabels() ) {
        pie.setLabelGenerator( null );
      } else {
        if ( pieDefinition.isLegendIncluded() ) {
          StandardPieSectionLabelGenerator labelGen = new StandardPieSectionLabelGenerator( "{1} ({2})" ); //$NON-NLS-1$
          pie.setLabelGenerator( labelGen );
        } else {
          StandardPieSectionLabelGenerator labelGen = new StandardPieSectionLabelGenerator( "{0} = {1} ({2})" ); //$NON-NLS-1$
          pie.setLabelGenerator( labelGen );
        }
      }
    }
    if ( plot instanceof MultiplePiePlot ) {
      MultiplePiePlot pies = (MultiplePiePlot) plot;
      CategoryDatasetChartDefinition categoryDatasetChartDefintion = (CategoryDatasetChartDefinition) chartDefinition;
      pies.setDataset( categoryDatasetChartDefintion );
    }
    if ( plot instanceof MeterPlot ) {
      MeterPlot meter = (MeterPlot) plot;
      DialWidgetDefinition widget = (DialWidgetDefinition) chartDefinition;
      List intervals = widget.getIntervals();
      Iterator intervalIterator = intervals.iterator();
      while ( intervalIterator.hasNext() ) {
        MeterInterval interval = (MeterInterval) intervalIterator.next();
        meter.addInterval( interval );
      }

      meter.setNeedlePaint( widget.getNeedlePaint() );
      meter.setDialShape( widget.getDialShape() );
      meter.setDialBackgroundPaint( widget.getPlotBackgroundPaint() );
      meter.setRange( new Range( widget.getMinimum(), widget.getMaximum() ) );

    }
    if ( plot instanceof XYPlot ) {
      XYPlot xyPlot = (XYPlot) plot;
      if ( chartDefinition instanceof XYSeriesCollectionChartDefinition ) {
        XYSeriesCollectionChartDefinition xySeriesCollectionChartDefintion =
            (XYSeriesCollectionChartDefinition) chartDefinition;
        xyPlot.setOrientation( xySeriesCollectionChartDefintion.getOrientation() );
        ValueAxis domainAxis = xyPlot.getDomainAxis();
        if ( domainAxis != null ) {
          domainAxis.setLabel( xySeriesCollectionChartDefintion.getDomainTitle() );
          domainAxis.setLabelFont( xySeriesCollectionChartDefintion.getDomainTitleFont() );
          domainAxis.setVerticalTickLabels( xySeriesCollectionChartDefintion.isDomainVerticalTickLabels() );
          if ( xySeriesCollectionChartDefintion.getDomainTickFormat() != null ) {
            ( (NumberAxis) domainAxis )
                .setNumberFormatOverride( xySeriesCollectionChartDefintion.getDomainTickFormat() );
          }
          if ( xySeriesCollectionChartDefintion.getDomainTickFont() != null ) {
            domainAxis.setTickLabelFont( xySeriesCollectionChartDefintion.getDomainTickFont() );
          }
          if ( xySeriesCollectionChartDefintion.getDomainMinimum() != ValueAxis.DEFAULT_LOWER_BOUND ) {
            domainAxis.setLowerBound( xySeriesCollectionChartDefintion.getDomainMinimum() );
          }
          if ( xySeriesCollectionChartDefintion.getDomainMaximum() != ValueAxis.DEFAULT_UPPER_BOUND ) {
            domainAxis.setUpperBound( xySeriesCollectionChartDefintion.getDomainMaximum() );
          }
        }

        ValueAxis rangeAxis = xyPlot.getRangeAxis();
        if ( rangeAxis != null ) {
          rangeAxis.setLabel( xySeriesCollectionChartDefintion.getRangeTitle() );
          rangeAxis.setLabelFont( xySeriesCollectionChartDefintion.getRangeTitleFont() );
          if ( xySeriesCollectionChartDefintion.getRangeMinimum() != ValueAxis.DEFAULT_LOWER_BOUND ) {
            rangeAxis.setLowerBound( xySeriesCollectionChartDefintion.getRangeMinimum() );
          }
          if ( xySeriesCollectionChartDefintion.getRangeMaximum() != ValueAxis.DEFAULT_UPPER_BOUND ) {
            rangeAxis.setUpperBound( xySeriesCollectionChartDefintion.getRangeMaximum() );
          }
          if ( xySeriesCollectionChartDefintion.getRangeMinimum() != ValueAxis.DEFAULT_LOWER_BOUND ) {
            rangeAxis.setLowerBound( xySeriesCollectionChartDefintion.getRangeMinimum() );
          }
          if ( xySeriesCollectionChartDefintion.getRangeMaximum() != ValueAxis.DEFAULT_UPPER_BOUND ) {
            rangeAxis.setUpperBound( xySeriesCollectionChartDefintion.getRangeMaximum() );
          }
          if ( xySeriesCollectionChartDefintion.getRangeTickFormat() != null ) {
            ( (NumberAxis) rangeAxis ).setNumberFormatOverride( xySeriesCollectionChartDefintion.getRangeTickFormat() );
          }
          if ( xySeriesCollectionChartDefintion.getRangeTickFont() != null ) {
            rangeAxis.setTickLabelFont( xySeriesCollectionChartDefintion.getRangeTickFont() );
          }
        }

      } else if ( chartDefinition instanceof TimeSeriesCollectionChartDefinition ) {
        TimeSeriesCollectionChartDefinition timeSeriesCollectionChartDefintion =
            (TimeSeriesCollectionChartDefinition) chartDefinition;
        xyPlot.setOrientation( timeSeriesCollectionChartDefintion.getOrientation() );
        ValueAxis domainAxis = xyPlot.getDomainAxis();
        if ( domainAxis != null ) {
          domainAxis.setLabel( timeSeriesCollectionChartDefintion.getDomainTitle() );
          domainAxis.setLabelFont( timeSeriesCollectionChartDefintion.getDomainTitleFont() );
          domainAxis.setVerticalTickLabels( timeSeriesCollectionChartDefintion.isDomainVerticalTickLabels() );
          if ( domainAxis instanceof DateAxis ) {
            DateAxis da = (DateAxis) domainAxis;
            if ( timeSeriesCollectionChartDefintion.getDateMinimum() != null ) {
              da.setMinimumDate( timeSeriesCollectionChartDefintion.getDateMinimum() );
            }
            if ( timeSeriesCollectionChartDefintion.getDateMaximum() != null ) {
              da.setMaximumDate( timeSeriesCollectionChartDefintion.getDateMaximum() );
            }
          }
        }

        ValueAxis rangeAxis = xyPlot.getRangeAxis();
        if ( rangeAxis != null ) {
          rangeAxis.setLabel( timeSeriesCollectionChartDefintion.getRangeTitle() );
          rangeAxis.setLabelFont( timeSeriesCollectionChartDefintion.getRangeTitleFont() );
          if ( timeSeriesCollectionChartDefintion.getRangeMinimum() != ValueAxis.DEFAULT_LOWER_BOUND ) {
            rangeAxis.setLowerBound( timeSeriesCollectionChartDefintion.getRangeMinimum() );
          }
          if ( timeSeriesCollectionChartDefintion.getRangeMaximum() != ValueAxis.DEFAULT_UPPER_BOUND ) {
            rangeAxis.setUpperBound( timeSeriesCollectionChartDefintion.getRangeMaximum() );
          }
        }
      } else if ( chartDefinition instanceof XYZSeriesCollectionChartDefinition ) {
        XYZSeriesCollectionChartDefinition xyzSeriesCollectionChartDefintion =
            (XYZSeriesCollectionChartDefinition) chartDefinition;
        xyPlot.setOrientation( xyzSeriesCollectionChartDefintion.getOrientation() );
        ValueAxis domainAxis = xyPlot.getDomainAxis();
        if ( domainAxis != null ) {
          domainAxis.setLabel( xyzSeriesCollectionChartDefintion.getDomainTitle() );
          domainAxis.setLabelFont( xyzSeriesCollectionChartDefintion.getDomainTitleFont() );
          domainAxis.setVerticalTickLabels( xyzSeriesCollectionChartDefintion.isDomainVerticalTickLabels() );
          if ( xyzSeriesCollectionChartDefintion.getDomainMinimum() != ValueAxis.DEFAULT_LOWER_BOUND ) {
            domainAxis.setLowerBound( xyzSeriesCollectionChartDefintion.getDomainMinimum() );
          }
          if ( xyzSeriesCollectionChartDefintion.getDomainMaximum() != ValueAxis.DEFAULT_UPPER_BOUND ) {
            domainAxis.setUpperBound( xyzSeriesCollectionChartDefintion.getDomainMaximum() );
          }
          if ( xyzSeriesCollectionChartDefintion.getDomainTickFormat() != null ) {
            ( (NumberAxis) domainAxis ).setNumberFormatOverride( xyzSeriesCollectionChartDefintion
                .getDomainTickFormat() );
          }
          if ( xyzSeriesCollectionChartDefintion.getDomainTickFont() != null ) {
            domainAxis.setTickLabelFont( xyzSeriesCollectionChartDefintion.getDomainTickFont() );
          }
        }

        ValueAxis rangeAxis = xyPlot.getRangeAxis();
        if ( rangeAxis != null ) {
          rangeAxis.setLabel( xyzSeriesCollectionChartDefintion.getRangeTitle() );
          rangeAxis.setLabelFont( xyzSeriesCollectionChartDefintion.getRangeTitleFont() );
          rangeAxis.setLowerBound( xyzSeriesCollectionChartDefintion.getRangeMinimum() );
          if ( xyzSeriesCollectionChartDefintion.getRangeMinimum() != ValueAxis.DEFAULT_LOWER_BOUND ) {
            rangeAxis.setLowerBound( xyzSeriesCollectionChartDefintion.getRangeMinimum() );
          }
          if ( xyzSeriesCollectionChartDefintion.getRangeMaximum() != ValueAxis.DEFAULT_UPPER_BOUND ) {
            rangeAxis.setUpperBound( xyzSeriesCollectionChartDefintion.getRangeMaximum() );
          }
          if ( xyzSeriesCollectionChartDefintion.getRangeTickFormat() != null ) {
            ( (NumberAxis) rangeAxis ).setNumberFormatOverride(
              xyzSeriesCollectionChartDefintion.getRangeTickFormat() );
          }
          if ( xyzSeriesCollectionChartDefintion.getRangeTickFont() != null ) {
            rangeAxis.setTickLabelFont( xyzSeriesCollectionChartDefintion.getRangeTickFont() );
          }
        }

      }
    }
  }

  private static JFreeChart createMultiplePieChart( final CategoryDatasetChartDefinition chartDefinition ) {
    // TODO Make the following accessible from the chartDefinition
    TableOrder order = TableOrder.BY_ROW;
    // boolean tooltips = true;
    // boolean urls = true;
    // -----------------------------------------------------------

    String title = chartDefinition.getTitle();
    boolean legend = chartDefinition.isLegendIncluded();

    if ( order == null ) {
      throw new IllegalArgumentException( Messages.getInstance().getString(
          "JFreeChartEngine.ERROR_0001_NULL_ORDER_ARGUMENT" ) ); //$NON-NLS-1$
    }
    MultiplePiePlot plot = new MultiplePiePlot( chartDefinition );
    JFreeChartEngine.updatePlot( plot, chartDefinition );
    plot.setDataExtractOrder( order );

    JFreeChart pieCharts = new JFreeChart( title, chartDefinition.getTitleFont(), plot, legend );
    JFreeChart pieChart = plot.getPieChart();
    TextTitle seriesTitle = new TextTitle( "Series Title", new Font( "SansSerif", Font.BOLD, 12 ) ); //$NON-NLS-1$ //$NON-NLS-2$
    seriesTitle.setPosition( RectangleEdge.BOTTOM );
    pieChart.setTitle( seriesTitle );
    pieChart.setBackgroundPaint( chartDefinition.getChartBackgroundPaint() );
    plot.setPieChart( pieChart );

    // if (tooltips) {
    // PieToolTipGenerator tooltipGenerator = new
    // StandardPieToolTipGenerator();
    // PiePlot pp = (PiePlot) plot.getPieChart().getPlot();
    // pp.setToolTipGenerator(tooltipGenerator);
    // }

    // if (urls) {
    // PieURLGenerator urlGenerator = new StandardPieURLGenerator();
    // PiePlot pp = (PiePlot) plot.getPieChart().getPlot();
    // pp.setURLGenerator(urlGenerator);
    // }

    return pieCharts;
  }

  private static JFreeChart createLineChart( final CategoryDatasetChartDefinition chartDefinition ) {
    // TODO Make the following accessible from the chartDefinition
    String categoryAxisLabel = null;
    String valueAxisLabel = null;
    boolean tooltips = true;
    boolean urls = true;
    // -----------------------------------------------------------

    String title = chartDefinition.getTitle();
    boolean legend = chartDefinition.isLegendIncluded();

    CategoryAxis categoryAxis = new CategoryAxis( categoryAxisLabel );
    ValueAxis valueAxis = new NumberAxis( valueAxisLabel );
    LineAndShapeRenderer renderer =
        chartDefinition.isThreeD() ? new LineRenderer3D() : new LineAndShapeRenderer( true, false );
    if ( tooltips ) {
      renderer.setBaseToolTipGenerator( new StandardCategoryToolTipGenerator() );
    }
    if ( urls ) {
      renderer.setBaseItemURLGenerator( new StandardCategoryURLGenerator() );
    }

    renderer.setStroke( JFreeChartEngine.getLineStyleStroke( chartDefinition.getLineStyle(), chartDefinition
        .getLineWidth() ) );
    renderer.setShapesVisible( chartDefinition.isMarkersVisible() );
    renderer.setBaseShapesFilled( chartDefinition.isMarkersVisible() );

    CategoryPlot plot = new CategoryPlot( chartDefinition, categoryAxis, valueAxis, renderer );
    JFreeChartEngine.updatePlot( plot, chartDefinition );

    JFreeChart chart = new JFreeChart( title, JFreeChart.DEFAULT_TITLE_FONT, plot, legend );

    return chart;

  }

  private static JFreeChart createBarChart( final CategoryDatasetChartDefinition chartDefinition ) {
    // TODO Make the following accessible from the chartDefinition
    String categoryAxisLabel = null;
    String valueAxisLabel = null;
    boolean tooltips = true;
    boolean urls = true;
    // -----------------------------------------------------------

    String title = chartDefinition.getTitle();
    boolean legend = chartDefinition.isLegendIncluded();
    PlotOrientation orientation = chartDefinition.getOrientation();

    CategoryAxis categoryAxis = new CategoryAxis( categoryAxisLabel );
    ValueAxis valueAxis = new NumberAxis( valueAxisLabel );
    BarRenderer renderer = null;
    // Determine the type of renderer to use
    if ( chartDefinition.isStacked() || chartDefinition.isThreeD() ) {
      if ( chartDefinition.isStacked() && chartDefinition.isThreeD() ) {
        renderer = new StackedBarRenderer3D();
      } else if ( chartDefinition.isStacked() ) {
        renderer = new StackedBarRenderer();
      } else {
        renderer = new BarRenderer3D();
      }
    } else {
      renderer = new BarRenderer();
    }
    if ( orientation == PlotOrientation.HORIZONTAL ) {
      ItemLabelPosition position1 = new ItemLabelPosition( ItemLabelAnchor.OUTSIDE3, TextAnchor.CENTER_LEFT );
      renderer.setPositiveItemLabelPosition( position1 );
      ItemLabelPosition position2 = new ItemLabelPosition( ItemLabelAnchor.OUTSIDE9, TextAnchor.CENTER_RIGHT );
      renderer.setNegativeItemLabelPosition( position2 );
    } else if ( orientation == PlotOrientation.VERTICAL ) {
      ItemLabelPosition position1 = new ItemLabelPosition( ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER );
      renderer.setPositiveItemLabelPosition( position1 );
      ItemLabelPosition position2 = new ItemLabelPosition( ItemLabelAnchor.OUTSIDE6, TextAnchor.TOP_CENTER );
      renderer.setNegativeItemLabelPosition( position2 );
    }
    if ( tooltips ) {
      renderer.setBaseToolTipGenerator( new StandardCategoryToolTipGenerator() );
    }
    if ( urls ) {
      renderer.setBaseItemURLGenerator( new StandardCategoryURLGenerator() );
    }

    if ( chartDefinition.getMaxBarWidth() != null ) {
      renderer.setMaximumBarWidth( chartDefinition.getMaxBarWidth().doubleValue() );
    }

    CategoryPlot plot = new CategoryPlot( chartDefinition, categoryAxis, valueAxis, renderer );
    JFreeChartEngine.updatePlot( plot, chartDefinition );

    JFreeChart chart = new JFreeChart( title, JFreeChart.DEFAULT_TITLE_FONT, plot, legend );

    return chart;

  }

  private static JFreeChart createBarLineChart( final BarLineChartDefinition chartDefinition ) {
    // TODO Make the following accessible from the chartDefinition
    String categoryAxisLabel = null;
    String valueAxisLabel = null;
    String secondValueAxisLabel = null;
    boolean tooltips = true;
    boolean urls = true;
    // -----------------------------------------------------------
    String title = chartDefinition.getTitle();
    boolean legend = chartDefinition.isLegendIncluded();
    PlotOrientation orientation = chartDefinition.getOrientation();

    // split BarLineChartDefinition in two Definitions
    CategoryDatasetChartDefinition barsDataset =
        new CategoryDatasetChartDefinition( chartDefinition.getSession(), chartDefinition.getChartAttributes() );
    CategoryDatasetChartDefinition linesDataset =
        new CategoryDatasetChartDefinition( chartDefinition.getSession(), chartDefinition.getChartAttributes() );

    /*
     * try{ barsDataset = (CategoryDatasetChartDefinition)chartDefinition.clone(); linesDataset =
     * (CategoryDatasetChartDefinition)chartDefinition.clone(); }catch(Exception e){}
     */

    // get column and row count of the data set
    int iColumnCount = chartDefinition.getColumnCount();
    int iRowCount = chartDefinition.getRowCount();

    if ( iRowCount <= 0 ) {
      chartDefinition.setNoDataMessage( Messages.getInstance().getString( "CHART.USER_NO_DATA_AVAILABLE" ) ); //$NON-NLS-1$
    }

    /*
     * Copy data to the two new data sets
     */
    // Loop through columns
    for ( int r = 0; r < iRowCount; r++ ) {

      // check if measure should be include in bar or line dataset
      String strMeasureName = (String) chartDefinition.getRowKey( r );
      boolean bIsBarColumn = JFreeChartEngine.isBarColumn( chartDefinition.getBarColumns(), strMeasureName );
      boolean bIsLineColumn = JFreeChartEngine.isLineColumn( chartDefinition.getLineColumns(), strMeasureName );

      // getting all values
      for ( int c = 0; c < iColumnCount; c++ ) {
        Comparable compColumnName = chartDefinition.getColumnKey( c );
        Number nValue = chartDefinition.getValue( strMeasureName, compColumnName );
        if ( bIsBarColumn ) {
          barsDataset.addValue( nValue, strMeasureName, compColumnName );
        }
        if ( bIsLineColumn ) {
          linesDataset.addValue( nValue, strMeasureName, compColumnName );
        }
      }

    }

    if ( ( iRowCount > 0 ) && ( barsDataset.getRowCount() <= 0 ) && ( linesDataset.getRowCount() <= 0 ) ) {
      chartDefinition.setNoDataMessage( Messages.getInstance().getString( "CHART.USER_INCORRECT_DATA_FORMAT" ) ); //$NON-NLS-1$
    }

    // Create Axis Objects
    CategoryAxis catAxis = new CategoryAxis( categoryAxisLabel );
    NumberAxis barsAxis = new NumberAxis( valueAxisLabel );
    NumberAxis linesAxis = new NumberAxis( secondValueAxisLabel );

    // set title and font for lines Axis
    linesDataset.setRangeTitle( chartDefinition.getLinesRangeTitle() );
    linesDataset.setRangeTitleFont( chartDefinition.getLinesRangeTitleFont() );
    if ( chartDefinition.getLinesRangeTickFormat() != null ) {
      linesAxis.setNumberFormatOverride( chartDefinition.getLinesRangeTickFormat() );
    }

    // create renderer
    BarRenderer barRenderer = null;
    LineAndShapeRenderer lineRenderer = null;
    // Determine the type of renderer to use
    if ( chartDefinition.isStacked() || chartDefinition.isThreeD() ) {
      if ( chartDefinition.isStacked() && chartDefinition.isThreeD() ) {
        barRenderer = new StackedBarRenderer3D();
        lineRenderer = new LineRenderer3D();
      } else if ( chartDefinition.isStacked() ) {
        barRenderer = new StackedBarRenderer();
        lineRenderer = new LineAndShapeRenderer();
      } else {
        barRenderer = new BarRenderer3D();
        lineRenderer = new LineRenderer3D();
      }
    } else {
      barRenderer = new BarRenderer();
      lineRenderer = new LineAndShapeRenderer();
    }
    if ( orientation == PlotOrientation.HORIZONTAL ) {
      ItemLabelPosition position1 = new ItemLabelPosition( ItemLabelAnchor.OUTSIDE3, TextAnchor.CENTER_LEFT );
      barRenderer.setPositiveItemLabelPosition( position1 );
      lineRenderer.setPositiveItemLabelPosition( position1 );
      ItemLabelPosition position2 = new ItemLabelPosition( ItemLabelAnchor.OUTSIDE9, TextAnchor.CENTER_RIGHT );
      barRenderer.setNegativeItemLabelPosition( position2 );
      lineRenderer.setNegativeItemLabelPosition( position2 );
    } else if ( orientation == PlotOrientation.VERTICAL ) {
      ItemLabelPosition position1 = new ItemLabelPosition( ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER );
      barRenderer.setPositiveItemLabelPosition( position1 );
      lineRenderer.setPositiveItemLabelPosition( position1 );
      ItemLabelPosition position2 = new ItemLabelPosition( ItemLabelAnchor.OUTSIDE6, TextAnchor.TOP_CENTER );
      barRenderer.setNegativeItemLabelPosition( position2 );
      lineRenderer.setNegativeItemLabelPosition( position2 );
    }
    if ( tooltips ) {
      barRenderer.setBaseToolTipGenerator( new StandardCategoryToolTipGenerator() );
      lineRenderer.setBaseToolTipGenerator( new StandardCategoryToolTipGenerator() );
    }
    if ( urls ) {
      barRenderer.setBaseItemURLGenerator( new StandardCategoryURLGenerator() );
      lineRenderer.setBaseItemURLGenerator( new StandardCategoryURLGenerator() );
    }

    if ( chartDefinition.getMaxBarWidth() != null ) {
      barRenderer.setMaximumBarWidth( chartDefinition.getMaxBarWidth().doubleValue() );
    }

    // setting some line attributes
    lineRenderer.setStroke( JFreeChartEngine.getLineStyleStroke( chartDefinition.getLineStyle(), chartDefinition
        .getLineWidth() ) );
    lineRenderer.setShapesVisible( chartDefinition.isMarkersVisible() );
    lineRenderer.setBaseShapesFilled( chartDefinition.isMarkersVisible() );

    /*
     * Create plot and make necessary adjustments for overlaid chart
     */
    // create the plot with bar chart
    CategoryPlot plot = new CategoryPlot( barsDataset, catAxis, barsAxis, barRenderer );
    // add line renderer
    plot.setRenderer( 1, lineRenderer );
    // add lines dataset, renderer and axis to plot
    plot.setDataset( 1, linesDataset );
    plot.setRangeAxis( 1, linesAxis );
    // map lines to second axis
    plot.mapDatasetToRangeAxis( 1, 1 );
    // set rendering order
    plot.setDatasetRenderingOrder( DatasetRenderingOrder.FORWARD );
    // set location of second axis
    plot.setRangeAxisLocation( 1, AxisLocation.BOTTOM_OR_RIGHT );

    // standard settings for plots
    JFreeChartEngine.updatePlot( plot, barsDataset );
    // additional settings for second axis
    ValueAxis secondValueAxis = plot.getRangeAxis( 1 );

    if ( secondValueAxis != null ) {
      if ( chartDefinition.getLinesRangeTitle() != null ) {
        secondValueAxis.setLabel( chartDefinition.getLinesRangeTitle() );
      }
      if ( chartDefinition.getLinesRangeTitleFont() != null ) {
        secondValueAxis.setLabelFont( chartDefinition.getLinesRangeTitleFont() );
      }
      if ( chartDefinition.getLinesRangeTickFont() != null ) {
        secondValueAxis.setTickLabelFont( chartDefinition.getLinesRangeTickFont() );
      }
      if ( chartDefinition.getLinesRangeMinimum() != ValueAxis.DEFAULT_LOWER_BOUND ) {
        secondValueAxis.setLowerBound( chartDefinition.getLinesRangeMinimum() );
      }
      if ( chartDefinition.getLinesRangeMaximum() != ValueAxis.DEFAULT_UPPER_BOUND ) {
        secondValueAxis.setUpperBound( chartDefinition.getLinesRangeMaximum() );
      }
    }

    JFreeChart chart = new JFreeChart( title, JFreeChart.DEFAULT_TITLE_FONT, plot, legend );

    return chart;
  }

  private static JFreeChart createAreaChart( final CategoryDatasetChartDefinition chartDefinition ) {
    // TODO Make the following accessible from the chartDefinition
    String categoryAxisLabel = null;
    String valueAxisLabel = null;
    boolean tooltips = true;
    boolean urls = true;
    // -----------------------------------------------------------

    String title = chartDefinition.getTitle();
    boolean legend = chartDefinition.isLegendIncluded();

    CategoryAxis categoryAxis = new CategoryAxis( categoryAxisLabel );
    ValueAxis valueAxis = new NumberAxis( valueAxisLabel );

    AreaRenderer renderer = chartDefinition.isStacked() ? new StackedAreaRenderer() : new AreaRenderer();
    if ( tooltips ) {
      renderer.setBaseToolTipGenerator( new StandardCategoryToolTipGenerator() );
    }
    if ( urls ) {
      renderer.setBaseItemURLGenerator( new StandardCategoryURLGenerator() );
    }

    CategoryPlot plot = new CategoryPlot( chartDefinition, categoryAxis, valueAxis, renderer );
    JFreeChartEngine.updatePlot( plot, chartDefinition );

    JFreeChart chart = new JFreeChart( title, JFreeChart.DEFAULT_TITLE_FONT, plot, legend );

    return chart;
  }

  /**
   * Create a PNG image file from a JFreeChart object
   * 
   * @param chart
   *          The chart object to create an image from
   * @param path
   *          The path and name of the image file to create
   * @param width
   *          The width of the image in pixels
   * @param height
   *          The height of the image in pixels
   * @param writer
   *          A writer for the iamge map to be written to
   * @throws IOException
   */
  private static void saveChartAsPNG( final JFreeChart chart, final String path, final int width, final int height,
      final PrintWriter writer, final ChartRenderingInfo info ) throws IOException {
    File file = new File( path + ".png" ); //$NON-NLS-1$
    ChartUtilities.saveChartAsPNG( file, chart, width, height, info );

    // TODO support tool tip generators

    // TODO support URL fragment generators

    // TODO: make the image map name unique on the page
    if ( writer != null ) {
      ChartUtilities.writeImageMap( writer, "map-name", info, true ); //$NON-NLS-1$
    }
  }

  /**
   * Create an SVG image file from a JFreeChart object
   * 
   * @param chart
   *          The chart object to create an image from
   * @param path
   *          The path and name of the image file to create
   * @param width
   *          The width of the image in pixels
   * @param height
   *          The height of the image in pixels
   * @throws IOException
   */
  private static void saveChartAsSVG( final JFreeChart chart, final String path, final int width, final int height,
      final ChartRenderingInfo info ) throws IOException {
    // THE FOLLOWING CODE BASED ON THE EXAMPLE IN THE BATIK DOCUMENTATION...
    // Get a DOMImplementation
    org.w3c.dom.DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
    // Create an instance of org.w3c.dom.Document
    Document document = domImpl.createDocument( null, "svg", null ); //$NON-NLS-1$
    // Create an instance of the SVG Generator
    SVGGraphics2D svgGenerator = new SVGGraphics2D( document );
    // set the precision to avoid a null pointer exception in Batik 1.5
    svgGenerator.getGeneratorContext().setPrecision( 6 );
    // Ask the chart to render into the SVG Graphics2D implementation
    chart.draw( svgGenerator, new Rectangle2D.Double( 0, 0, width, height ), info );
    // Finally, stream out SVG to a file using UTF-8 character to byte
    // encoding
    boolean useCSS = true;
    Writer out =
        new OutputStreamWriter( new FileOutputStream( new File( path + ".svg" ) ), LocaleHelper.getSystemEncoding() ); //$NON-NLS-1$
    svgGenerator.stream( out, useCSS );
  }

  /**
   * Create an image file from a JFreeChart object
   * 
   * @param chart
   *          The chart object to create an image from
   * @param path
   *          The path and name of the image file to create
   * @param width
   *          The width of the image in pixels
   * @param height
   *          The height of the image in pixels
   * @param outputType
   *          The type of the image to create, OUPUT_PNG or OUTPUT_SVG
   * @param writer
   * @throws IOException
   */
  private static void saveChart( final JFreeChart chart, final String path, final int width, final int height,
      final int outputType, final PrintWriter writer, final ChartRenderingInfo info ) throws IOException {
    if ( outputType == JFreeChartEngine.OUTPUT_PNG ) {
      JFreeChartEngine.saveChartAsPNG( chart, path, width, height, writer, info );
    } else if ( outputType == JFreeChartEngine.OUTPUT_SVG ) {
      JFreeChartEngine.saveChartAsSVG( chart, path, width, height, info );
    }
    // TODO support other image types, e.g. JPG, GIF
  }

  /**
   * Create an image file using dataset object. This method takes a dataset object, e.g. a DialWidgetDefinition,
   * creates a JFreeChart object from it, and then creates an image file.
   * 
   * @param dataset
   *          The
   * @param title
   *          The title of the chart
   * @param units
   *          The units of the chart value
   * @param fileName
   *          The path and file of the image to create
   * @param width
   *          The width of the image to create
   * @param height
   *          The height of the image to create
   * @param outputType
   *          The type of the image to create
   * @param writer
   *          A writer to writer the image map into
   * @param logger
   *          The logger to log any messages to
   */
  public static void saveChart( final Dataset dataset, final String title, final String units, final String fileName,
      final int width, final int height, final int outputType, final PrintWriter writer, final ILogger logger ) {
    ChartRenderingInfo info = new ChartRenderingInfo();
    JFreeChartEngine.saveChart( dataset, title, units, fileName, width, height, outputType, writer, info, logger );
  }

  /**
   * Create an image file using dataset object. This method takes a dataset object, e.g. a DialWidgetDefinition,
   * creates a JFreeChart object from it, and then creates an image file.
   * 
   * @param dataset
   *          The
   * @param title
   *          The title of the chart
   * @param units
   *          The units of the chart value
   * @param fileName
   *          The path and file of the image to create
   * @param width
   *          The width of the image to create
   * @param height
   *          The height of the image to create
   * @param outputType
   *          The type of the image to create
   * @param writer
   *          A writer to writer the image map into
   * @param logger
   *          The logger to log any messages to
   */
  public static void saveChart( final Dataset dataset, final String title, final String units, final String fileName,
      final int width, final int height, final int outputType, final PrintWriter writer, final ChartRenderingInfo info,
      final ILogger logger ) {

    JFreeChart chart = JFreeChartEngine.getChart( dataset, title, units, width, height, logger );

    try {
      String filePath = PentahoSystem.getApplicationContext().getFileOutputPath( fileName );
      JFreeChartEngine.saveChart( chart, filePath, width, height, outputType, writer, info );
    } catch ( IOException e ) {
      logger.error( Messages.getInstance().getString( "ChartEngine.ERROR_0001_COULD_NOT_CREATE_CHART" ), e ); //$NON-NLS-1$
    }
  }

  /**
   * Create a chart-object using dataset object. This method takes a dataset object, e.g. a DialWidgetDefinition,
   * and creates and returns a JFreeChart object from it.
   * 
   * @param dataset
   *          The dataset
   * @param title
   *          The title of the chart
   * @param units
   *          The units of the chart value
   * @param width
   *          The width of the image to create
   * @param height
   *          The height of the image to create
   * @param logger
   *          The logger to log any messages to
   * 
   * @return JFreeChart the generated chart object
   */
  public static JFreeChart getChart( final Dataset dataset, final String title, final String units, final int width,
      final int height, final ILogger logger ) {

    JFreeChart chart = null;
    if ( dataset instanceof DialWidgetDefinition ) {
      chart = JFreeChartEngine.createDial( (DialWidgetDefinition) dataset, title, units );
    } else if ( dataset instanceof TimeSeriesCollectionChartDefinition ) {
      chart = JFreeChartEngine.createTimeSeriesCollectionChart( (TimeSeriesCollectionChartDefinition) dataset );
    } else if ( dataset instanceof TimeTableXYDatasetChartDefinition ) {
      chart = JFreeChartEngine.createStackedTimeSeriesChart( (TimeTableXYDatasetChartDefinition) dataset );
    } else if ( dataset instanceof XYSeriesCollectionChartDefinition ) {
      chart = JFreeChartEngine.createXYSeriesCollectionChart( (XYSeriesCollectionChartDefinition) dataset );
    } else if ( dataset instanceof BarLineChartDefinition ) {
      chart = JFreeChartEngine.createBarLineChart( (BarLineChartDefinition) dataset );
    } else if ( dataset instanceof CategoryDatasetChartDefinition ) {
      chart = JFreeChartEngine.createCategoryDatasetChart( (CategoryDatasetChartDefinition) dataset );
    } else if ( dataset instanceof PieDatasetChartDefinition ) {
      chart = JFreeChartEngine.createPieDatasetChart( (PieDatasetChartDefinition) dataset );
    } else if ( dataset instanceof XYZSeriesCollectionChartDefinition ) {
      chart = JFreeChartEngine.createXYZSeriesCollectionChart( (XYZSeriesCollectionChartDefinition) dataset );
    }
    if ( chart == null ) {
      logger.error( Messages.getInstance().getString( "ChartEngine.ERROR_0002_COULD_NOT_CREATE_CHART" ) ); //$NON-NLS-1$
    } else {
      // TODO implement the ability to have "ImageTitle"s for subtitles
      ChartDefinition chartDefinition = (ChartDefinition) dataset;
      Iterator iter = chartDefinition.getSubtitles().iterator();
      while ( iter.hasNext() ) {
        chart.addSubtitle( new TextTitle( iter.next().toString() ) );
      }
      chart.setBackgroundPaint( chartDefinition.getChartBackgroundPaint() );
      chart.setBackgroundImage( chartDefinition.getChartBackgroundImage() );
      chart.setBorderVisible( chartDefinition.isBorderVisible() );
      chart.setBorderPaint( chartDefinition.getBorderPaint() );
      if ( chart.getTitle() != null ) {
        chart.getTitle().setPosition( chartDefinition.getTitlePosition() );
        chart.getTitle().setFont( chartDefinition.getTitleFont() );
      }

      if ( chartDefinition.getLegendFont() != null && chart.getLegend() != null ) {
        chart.getLegend().setItemFont( chartDefinition.getLegendFont() );
      }
      if ( !chartDefinition.isLegendBorderVisible() && chart.getLegend() != null ) {
        chart.getLegend().setBorder( BlockBorder.NONE );
      }
      if ( chartDefinition.getLegendPosition() != null && chart.getLegend() != null ) {
        chart.getLegend().setPosition( chartDefinition.getLegendPosition() );
      }
    }
    return ( chart );
  }

  /**
   * 
   * @param typeStr
   *          String name for chart
   * @return int representing the chart string passed in; -1 if invalid chart name
   * @see ChartDefinition for valid chart name strings and integers
   */
  public static int getChartType( final String typeStr ) {
    if ( typeStr != null ) {
      if ( ChartDefinition.PIE_CHART_STR.equalsIgnoreCase( typeStr ) ) {
        return JFreeChartEngine.PIE_CHART_TYPE;
      } else if ( ChartDefinition.PIE_GRID_CHART_STR.equalsIgnoreCase( typeStr ) ) {
        return JFreeChartEngine.PIE_GRID_CHART_TYPE;
      } else if ( ChartDefinition.BAR_CHART_STR.equalsIgnoreCase( typeStr ) ) {
        return JFreeChartEngine.BAR_CHART_TYPE;
      } else if ( ChartDefinition.LINE_CHART_STR.equalsIgnoreCase( typeStr ) ) {
        return JFreeChartEngine.LINE_CHART_TYPE;
      } else if ( ChartDefinition.AREA_CHART_STR.equalsIgnoreCase( typeStr ) ) {
        return JFreeChartEngine.AREA_CHART_TYPE;
      } else if ( ChartDefinition.BAR_LINE_CHART_STR.equalsIgnoreCase( typeStr ) ) {
        return JFreeChartEngine.BAR_LINE_CHART_TYPE;
      } else if ( ChartDefinition.BUBBLE_CHART_STR.equalsIgnoreCase( typeStr ) ) {
        return JFreeChartEngine.BUBBLE_CHART_TYPE;
      } else if ( ChartDefinition.DIAL_CHART_STR.equalsIgnoreCase( typeStr ) ) {
        return JFreeChartEngine.DIAL_CHART_TYPE;
      } else if ( ChartDefinition.DIFFERENCE_CHART_STR.equalsIgnoreCase( typeStr ) ) {
        return JFreeChartEngine.DIFFERENCE_CHART_TYPE;
      } else if ( ChartDefinition.DOT_CHART_STR.equalsIgnoreCase( typeStr ) ) {
        return JFreeChartEngine.DOT_CHART_TYPE;
      } else if ( ChartDefinition.STEP_AREA_CHART_STR.equalsIgnoreCase( typeStr ) ) {
        return JFreeChartEngine.STEP_AREA_CHART_TYPE;
      } else if ( ChartDefinition.STEP_CHART_STR.equalsIgnoreCase( typeStr ) ) {
        return JFreeChartEngine.STEP_CHART_TYPE;
      }
    }
    return JFreeChartEngine.UNDEFINED_CHART_TYPE;
  }

  /**
   * 
   * @param type
   *          int type for chart
   * @return String representing the chart
   * @see ChartDefinition for valid chart name strings
   */
  public static String getChartTypeName( final int type ) {

    String rtn = ""; //$NON-NLS-1$

    switch ( type ) {
      case JFreeChartEngine.PIE_CHART_TYPE:
        rtn = ChartDefinition.PIE_CHART_STR;
        break;
      case JFreeChartEngine.PIE_GRID_CHART_TYPE:
        rtn = ChartDefinition.PIE_GRID_CHART_STR;
        break;
      case JFreeChartEngine.BAR_CHART_TYPE:
        rtn = ChartDefinition.BAR_CHART_STR;
        break;
      case JFreeChartEngine.LINE_CHART_TYPE:
        rtn = ChartDefinition.LINE_CHART_STR;
        break;
      case JFreeChartEngine.BAR_LINE_CHART_TYPE:
        rtn = ChartDefinition.BAR_LINE_CHART_STR;
        break;
      case JFreeChartEngine.BUBBLE_CHART_TYPE:
        rtn = ChartDefinition.BUBBLE_CHART_STR;
        break;
      case JFreeChartEngine.DIAL_CHART_TYPE:
        rtn = ChartDefinition.DIAL_CHART_STR;
        break;
      case JFreeChartEngine.DIFFERENCE_CHART_TYPE:
        rtn = ChartDefinition.DIFFERENCE_CHART_STR;
        break;
      case JFreeChartEngine.DOT_CHART_TYPE:
        rtn = ChartDefinition.DOT_CHART_STR;
        break;
      case JFreeChartEngine.STEP_AREA_CHART_TYPE:
        rtn = ChartDefinition.STEP_AREA_CHART_STR;
        break;
      case JFreeChartEngine.STEP_CHART_TYPE:
        rtn = ChartDefinition.STEP_CHART_STR;
        break;
      case JFreeChartEngine.AREA_CHART_TYPE:
        rtn = ChartDefinition.AREA_CHART_STR;
        break;
      default:
    }

    return rtn;
  }

  /**
   * Creates a java.awt.Color object from an HTML color definition, eg #80dd3f
   * 
   * @param htmlColor
   *          The color string
   * @return A Color object
   */
  public static Color getColor( final String htmlColor ) {

    return new Color( Integer.parseInt( htmlColor.substring( 1, 3 ), 16 ), Integer.parseInt(
        htmlColor.substring( 3, 5 ), 16 ), Integer.parseInt( htmlColor.substring( 5 ), 16 ) );
  }

  public static Color getTransparent() {
    return new Color( 0, 0, 0, 0 );
  }

  /**
   * Creates a java.awt.Paint object from an XML node from the dial definition document
   * 
   * @param width
   *          of the chart
   * @param height
   *          of the chart
   * @param node
   *          XML Node from the dial definition
   * @return Paint object defined by the node
   */
  public static Paint getPaint( final Node node, final int width, final int height, final IPentahoSession session ) {
    if ( node == null ) {
      return null;
    }
    // TODO support gradient and texture paints
    if ( null != node.selectSingleNode( "gradient" ) ) { //$NON-NLS-1$
      return JFreeChartEngine.getGradientPaint( ( node.selectSingleNode( "gradient" ) ), width, height ); //$NON-NLS-1$
    } else if ( null != node.selectSingleNode( "texture" ) ) { //$NON-NLS-1$
      return JFreeChartEngine.getTexturePaint( ( node.selectSingleNode( "texture" ) ), width, height, session ); //$NON-NLS-1$
    } else {
      String htmlColor = node.getText();
      return JFreeChartEngine.getPaint( htmlColor );
    }

  }

  /**
   * Creates a java.awt.GradientPaint object from an XML node from the dial definition document
   * 
   * @param XML
   *          Node from the dial definition
   * @param width
   *          of the chart
   * @param height
   *          of the chart
   * @return Paint object defined by the node / public static Paint getGradientPaint( Node node,int width, int
   *         height ) {
   * 
   *         if( node == null ) { return null; } Color
   *         firstColor=getColor((node.selectSingleNode("first-color")).getText().toString()); //$NON-NLS-1$ Color
   *         secondColor=getColor((node.selectSingleNode("second-color")).getText().toString()); //$NON-NLS-1$
   *         boolean cyclic=true; if((node.selectSingleNode("cyclic")).getText().trim().equals("false")) {
   *         //$NON-NLS-1$ //$NON-NLS-2$ cyclic=false; } return new
   *         GradientPaint(0,0,firstColor,width-(width/2),height,secondColor,cyclic); }
   */
  /**
   * Creates a java.awt.TexturePaint object from an XML node from the dial definition document
   * 
   * @param XML
   *          Node from the dial definition
   * @param width
   *          of the chart
   * @param height
   *          of the chart
   * @return Paint object defined by the node / public static Paint getTexturePaint( Node node, int width, int
   *         height ) {
   * 
   *         if( node == null ) { return null; } int rectWidth=width; int rectHeight=height; int x=0; int y=0; //
   *         Get Image try{ Node imageNode = node.selectSingleNode( "background-image" ); //$NON-NLS-1$ if(
   *         imageNode != null ) { String imageName = imageNode.getText().toString(); String fileName =
   *         PentahoSystem.getApplicationContext().getSolutionPath( imageName ); int offset = fileName.lastIndexOf(
   *         "." ); //$NON-NLS-1$ String type = offset == -1 ? "jpg" : fileName.substring(offset + 1);
   *         //$NON-NLS-1$
   * 
   *         BufferedImage image = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB);
   * 
   *         ImageIO.write(image, type, new File( fileName ));
   * 
   *         Node rectangle= node.selectSingleNode("rectangle"); //$NON-NLS-1$ if( rectangle != null ) { Node
   *         tmpNode = rectangle.selectSingleNode("width"); //$NON-NLS-1$ if( tmpNode != null ) {
   *         rectWidth=Integer.parseInt(tmpNode.getText().toString().trim()); //$NON-NLS-1$ } tmpNode =
   *         rectangle.selectSingleNode("height"); //$NON-NLS-1$ if( tmpNode != null ) {
   *         rectHeight=Integer.parseInt(tmpNode.getText().toString().trim()); //$NON-NLS-1$ } tmpNode =
   *         rectangle.selectSingleNode("x"); //$NON-NLS-1$ if( tmpNode != null ) {
   *         x=Integer.parseInt(tmpNode.getText().toString().trim()); //$NON-NLS-1$ } tmpNode =
   *         rectangle.selectSingleNode("y"); //$NON-NLS-1$ if( tmpNode != null ) {
   *         y=Integer.parseInt(tmpNode.getText().toString().trim()); //$NON-NLS-1$ } }
   * 
   *         Rectangle2D rect = new Rectangle2D.Double(x,y,rectWidth,rectHeight);
   * 
   *         return new TexturePaint(image,rect); }
   * 
   *         }catch(Exception e){e.printStackTrace();} return null; }
   */
  public static Font getFont( final Node fontNode ) {
    Font font = null;
    if ( fontNode != null ) {
      String fontFamily = TextTitle.DEFAULT_FONT.getFamily();
      int fontStyle = Font.PLAIN;
      int fontSize = TextTitle.DEFAULT_FONT.getSize();

      Node fontFamilyNode = fontNode.selectSingleNode( JFreeChartEngine.FONT_FAMILY_NODE_NAME );
      if ( ( fontFamilyNode != null ) && ( fontFamilyNode.getText().length() > 0 ) ) {
        fontFamily = fontFamilyNode.getText();
      }
      Node isBoldNode = fontNode.selectSingleNode( JFreeChartEngine.IS_BOLD_NODE_NAME );
      if ( isBoldNode != null ) {
        boolean bold = Boolean.valueOf( isBoldNode.getText() ).booleanValue();
        if ( bold ) {
          fontStyle += Font.BOLD;
        }
      }
      Node isItalicNode = fontNode.selectSingleNode( JFreeChartEngine.IS_ITALIC_NODE_NAME );
      if ( isItalicNode != null ) {
        boolean italic = Boolean.valueOf( isItalicNode.getText() ).booleanValue();
        if ( italic ) {
          fontStyle += Font.ITALIC;
        }
      }
      Node sizeNode = fontNode.selectSingleNode( JFreeChartEngine.SIZE_NODE_NAME );
      if ( ( sizeNode != null ) && ( sizeNode.getText().length() > 0 ) ) {
        fontSize = Integer.parseInt( sizeNode.getText() );
      }
      font = new Font( fontFamily, fontStyle, fontSize );
    }
    return font;
  }

  /**
   * Creates a java.awt.Paint object from an XML node from the dial definition document
   * 
   * @param node
   *          XML Node from the chart definition
   * @return Paint object defined by the node
   */
  public static Paint getPaint( final Node node ) {
    if ( node == null ) {
      return null;
    }
    // TODO support gradient and texture paints
    String htmlColor = node.getText();
    return JFreeChartEngine.getPaint( htmlColor );
  }

  /**
   * Creates a java.awt.Paint object from an HTML color definition, eg #80ff3f
   * 
   * @param htmlColor
   *          The color string
   * @return Paint The Paint object for the requested color
   */
  public static Paint getPaint( final String htmlColor ) {

    Color color = JFreeChartEngine.getColor( htmlColor );

    return color;
  }

  /**
   * @param gradientNode
   *          root node that hold gradient information
   * @return a gradientPaint implementation of Paint
   */
  public static Paint getGradientPaint( final Node gradientNode, final int width, final int height ) {

    if ( gradientNode == null ) {
      return null;
    }
    float x1 = 0;
    float y1 = 0;
    float x2 = width;
    float y2 = height;

    if ( gradientNode.selectSingleNode( JFreeChartEngine.X1_NODE_NAME ) != null ) {
      x1 = Float.parseFloat( gradientNode.selectSingleNode( JFreeChartEngine.X1_NODE_NAME ).getText() );
    }
    if ( gradientNode.selectSingleNode( JFreeChartEngine.Y1_NODE_NAME ) != null ) {
      y1 = Float.parseFloat( gradientNode.selectSingleNode( JFreeChartEngine.Y1_NODE_NAME ).getText() );
    }
    if ( gradientNode.selectSingleNode( JFreeChartEngine.X2_NODE_NAME ) != null ) {
      x2 = Float.parseFloat( gradientNode.selectSingleNode( JFreeChartEngine.X2_NODE_NAME ).getText() );
    }
    if ( gradientNode.selectSingleNode( JFreeChartEngine.Y2_NODE_NAME ) != null ) {
      y2 = Float.parseFloat( gradientNode.selectSingleNode( JFreeChartEngine.Y2_NODE_NAME ).getText() );
    }
    Color color1 =
        JFreeChartEngine.getColor( gradientNode.selectSingleNode( JFreeChartEngine.COLOR1_NODE_NAME ).getText() );
    Color color2 =
        JFreeChartEngine.getColor( gradientNode.selectSingleNode( JFreeChartEngine.COLOR2_NODE_NAME ).getText() );
    boolean cyclic = false;
    if ( gradientNode.selectSingleNode( JFreeChartEngine.CYCLIC_NODE_NAME ) != null ) {
      cyclic =
          Boolean.valueOf( gradientNode.selectSingleNode( JFreeChartEngine.CYCLIC_NODE_NAME ).getText() )
              .booleanValue();
    }

    Paint paint = new GradientPaint( x1, y1, color1, x2, y2, color2, cyclic );
    return paint;
  }

  /**
   * @param textureNode
   *          root node that holds texture information
   * @return
   */
  public static Paint getTexturePaint( final Node textureNode, final int width, final int height,
      final IPentahoSession session ) {

    if ( textureNode == null ) {
      return null;
    }
    double x1 = 0.0;
    double y1 = 0.0;
    double x2 = width;
    double y2 = height;
    BufferedImage texture =
        (BufferedImage) JFreeChartEngine.getImage( textureNode
            .selectSingleNode( JFreeChartEngine.TEXTURE_IMAGE_NODE_NAME ), session );
    if ( textureNode.selectSingleNode( JFreeChartEngine.X1_NODE_NAME ) != null ) {
      x1 = Double.parseDouble( textureNode.selectSingleNode( JFreeChartEngine.X1_NODE_NAME ).getText() );
    }
    if ( textureNode.selectSingleNode( JFreeChartEngine.Y1_NODE_NAME ) != null ) {
      y1 = Double.parseDouble( textureNode.selectSingleNode( JFreeChartEngine.Y1_NODE_NAME ).getText() );
    }
    if ( textureNode.selectSingleNode( JFreeChartEngine.X2_NODE_NAME ) != null ) {
      x2 = Double.parseDouble( textureNode.selectSingleNode( JFreeChartEngine.X2_NODE_NAME ).getText() );
    }
    if ( textureNode.selectSingleNode( JFreeChartEngine.Y2_NODE_NAME ) != null ) {
      y2 = Double.parseDouble( textureNode.selectSingleNode( JFreeChartEngine.Y2_NODE_NAME ).getText() );
    }
    Rectangle2D anchor = new Rectangle2D.Double( x1, y1, x2, y2 );

    Paint paint = new TexturePaint( texture, anchor );
    return paint;
  }

  public static Stroke getLineStyleStroke( final String lineStyle, float lineWidth ) {

    BasicStroke stroke = null;
    float[] strokeSteps = null;

    // Negative linewidths not allowed; reset to default;
    if ( lineWidth < 0 ) {
      lineWidth = 1.0f;
    }

    if ( lineStyle != null ) {
      if ( lineStyle.equals( ChartDefinition.LINE_STYLE_DASH_STR ) ) {
        strokeSteps = new float[] { 6.0f, 6.0f };
      } else if ( lineStyle.equals( ChartDefinition.LINE_STYLE_DOT_STR ) ) {
        strokeSteps = new float[] { 2.0f, 6.0f };
      } else if ( lineStyle.equals( ChartDefinition.LINE_STYLE_DASHDOT_STR ) ) {
        strokeSteps = new float[] { 10.0f, 6.0f, 2.0f, 6.0f };
      } else if ( lineStyle.equals( ChartDefinition.LINE_STYLE_DASHDOTDOT_STR ) ) {
        strokeSteps = new float[] { 10.0f, 6.0f, 2.0f, 6.0f, 2.0f, 6.0f };
      }
    }

    if ( strokeSteps != null ) {
      stroke = new BasicStroke( lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, strokeSteps, 0.0f );
    } else {
      stroke = new BasicStroke( lineWidth );
    }
    return stroke;
  }

  /**
   * @param chartBackgroundImageNode
   *          - Node that contains a path to the images in its text element
   * @return the image
   */
  public static Image getImage( final Node chartBackgroundImageNode, final IPentahoSession session ) {
    Image image = null;
    if ( ( chartBackgroundImageNode != null ) && ( chartBackgroundImageNode.getText().length() > 0 ) ) {
      return JFreeChartEngine.getImage( chartBackgroundImageNode.getText(), session );
    }
    return image;
  }

  /**
   * @param imageName
   *          String that contains a path to the image
   * @return the image
   */
  public static Image getImage( final String imageName, final IPentahoSession session ) {
    Image image = null;
    try {
      IActionSequenceResource resource =
          new ActionSequenceResource( "", IActionSequenceResource.SOLUTION_FILE_RESOURCE, "", //$NON-NLS-1$ //$NON-NLS-2$
              imageName );
      InputStream is = resource.getInputStream( RepositoryFilePermission.READ, LocaleHelper.getLocale() );
      image = ImageIO.read( is );
    } catch ( IOException e ) {
      JFreeChartEngine.log.error( null, e );
    }
    return image;
  }

  /*
   * Two methods for the BarLineChart - Determine if a column of the data set is part of the defined bar or line
   * char columns
   */
  public static boolean isBarColumn( final String[] barColumns, final String columnName ) {
    boolean bIsBarColumn = false;

    for ( String element : barColumns ) {
      bIsBarColumn = columnName.equalsIgnoreCase( element );
      if ( bIsBarColumn ) {
        break;
      }
    }
    return bIsBarColumn;
  }

  public static boolean isLineColumn( final String[] lineColumns, final String columnName ) {
    boolean bIsLineColumn = false;

    for ( String element : lineColumns ) {
      bIsLineColumn = columnName.equalsIgnoreCase( element );
      if ( bIsLineColumn ) {
        break;
      }
    }
    return bIsLineColumn;
  }

}
