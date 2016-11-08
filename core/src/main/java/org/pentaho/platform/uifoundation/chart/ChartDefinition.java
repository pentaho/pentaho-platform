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

import org.jfree.ui.RectangleEdge;

import java.awt.Font;
import java.awt.Image;
import java.awt.Paint;
import java.util.List;

public interface ChartDefinition {

  public static final String DIAL_CHART_STR = "DialChart"; //$NON-NLS-1$

  public static final String PIE_CHART_STR = "PieChart"; //$NON-NLS-1$

  public static final String PIE_GRID_CHART_STR = "PieGrid"; //$NON-NLS-1$

  public static final String BAR_CHART_STR = "BarChart"; //$NON-NLS-1$

  public static final String LINE_CHART_STR = "LineChart"; //$NON-NLS-1$

  public static final String AREA_CHART_STR = "AreaChart"; //$NON-NLS-1$

  public static final String STEP_CHART_STR = "StepChart"; //$NON-NLS-1$

  public static final String STEP_AREA_CHART_STR = "StepAreaChart"; //$NON-NLS-1$

  public static final String DIFFERENCE_CHART_STR = "DifferenceChart"; //$NON-NLS-1$

  public static final String DOT_CHART_STR = "DotChart"; //$NON-NLS-1$

  // new chart type
  public static final String BAR_LINE_CHART_STR = "BarLineChart"; //$NON-NLS-1$

  public static final String BUBBLE_CHART_STR = "BubbleChart"; //$NON-NLS-1$

  // end new chart types

  public static final String XY_SERIES_COLLECTION_STR = "XYSeriesCollection"; //$NON-NLS-1$

  public static final String XYZ_SERIES_COLLECTION_STR = "XYZSeriesCollection"; //$NON-NLS-1$

  public static final String TIME_SERIES_COLLECTION_STR = "TimeSeriesCollection"; //$NON-NLS-1$

  public static final String CATEGORY_DATASET_STR = "CategoryDataset"; //$NON-NLS-1$

  public static final String DAY_PERIOD_TYPE_STR = "Day"; //$NON-NLS-1$

  public static final String FIXEDMILLISECOND_PERIOD_TYPE_STR = "FixedMillisecond"; //$NON-NLS-1$

  public static final String HOUR_PERIOD_TYPE_STR = "Hour"; //$NON-NLS-1$

  public static final String MILLISECOND_PERIOD_TYPE_STR = "Millisecond"; //$NON-NLS-1$

  public static final String MINUTE_PERIOD_TYPE_STR = "Minute"; //$NON-NLS-1$

  public static final String MONTH_PERIOD_TYPE_STR = "Month"; //$NON-NLS-1$

  public static final String QUARTER_PERIOD_TYPE_STR = "Quarter"; //$NON-NLS-1$

  public static final String SECOND_PERIOD_TYPE_STR = "Second"; //$NON-NLS-1$

  public static final String WEEK_PERIOD_TYPE_STR = "Week"; //$NON-NLS-1$

  public static final String YEAR_PERIOD_TYPE_STR = "Year"; //$NON-NLS-1$

  public static final String VERTICAL_ORIENTATION = "Vertical"; //$NON-NLS-1$

  public static final String HORIZONTAL_ORIENTATION = "Horizontal"; //$NON-NLS-1$

  public static final String TYPE_NODE_NAME = "chart-type"; //$NON-NLS-1$

  public static final String DATASET_TYPE_NODE_NAME = "dataset-type"; //$NON-NLS-1$

  public static final String WIDTH_NODE_NAME = "width"; //$NON-NLS-1$

  public static final String HEIGHT_NODE_NAME = "height"; //$NON-NLS-1$

  public static final String CHART_BORDER_VISIBLE_NODE_NAME = "border-visible"; //$NON-NLS-1$

  public static final String CHART_BORDER_PAINT_NODE_NAME = "border-paint"; //$NON-NLS-1$

  public static final String TITLE_NODE_NAME = "title"; //$NON-NLS-1$

  public static final String TITLE_FONT_NODE_NAME = "title-font"; //$NON-NLS-1$

  public static final String TITLE_POSITION_NODE_NAME = "title-position"; //$NON-NLS-1$

  public static final String SUBTITLE_NODE_NAME = "subtitle"; //$NON-NLS-1$

  public static final String SUBTITLES_NODE_NAME = "subtitles"; //$NON-NLS-1$

  public static final String CHART_BACKGROUND_NODE_NAME = "chart-background"; //$NON-NLS-1$

  public static final String PLOT_BACKGROUND_NODE_NAME = "plot-background"; //$NON-NLS-1$

  public static final String INCLUDE_LEGEND_NODE_NAME = "include-legend"; //$NON-NLS-1$

  public static final String LEGEND_FONT_NODE_NAME = "legend-font"; //$NON-NLS-1$

  public static final String DISPLAY_LEGEND_BORDER_NODE_NAME = "legend-border-visible"; //$NON-NLS-1$

  public static final String LEGEND_POSITION_NODE_NAME = "legend-position";

  public static final String DISPLAY_LABELS_NODE_NAME = "display-labels"; //$NON-NLS-1$

  public static final String PALETTE_NODE_NAME = "color-palette"; //$NON-NLS-1$

  public static final String COLOR_NODE_NAME = "color"; //$NON-NLS-1$

  public static final String THREED_NODE_NAME = "is-3D"; //$NON-NLS-1$

  public static final String TEXTURE_TYPE_NAME = "texture"; //$NON-NLS-1$

  public static final String GRADIENT_TYPE_NAME = "gradient"; //$NON-NLS-1$

  public static final String COLOR_TYPE_NAME = "color"; //$NON-NLS-1$

  public static final String IMAGE_TYPE_NAME = "image"; //$NON-NLS-1$

  public static final String BACKGROUND_TYPE_ATTRIBUTE_NAME = "@type"; //$NON-NLS-1$

  public static final String DOT_HEIGHT_NODE_NAME = "dot-height"; //$NON-NLS-1$

  public static final String DOT_WIDTH_NODE_NAME = "dot-width"; //$NON-NLS-1$

  public static final String LINE_STYLE_NODE_NAME = "line-style"; //$NON-NLS-1$

  public static final String LINE_WIDTH_NODE_NAME = "line-width"; //$NON-NLS-1$

  public static final String MARKER_VISIBLE_NODE_NAME = "markers-visible"; //$NON-NLS-1$

  public static final String FOREGROUND_ALPHA_NODE_NAME = "foreground-alpha"; //$NON-NLS-1$

  public static final String BACKGROUND_ALPHA_NODE_NAME = "background-alpha"; //$NON-NLS-1$

  public static final String STACKED_NODE_NAME = "is-stacked"; //$NON-NLS-1$

  public static final String LINE_STYLE_SOLID_STR = "solid"; //$NON-NLS-1$

  public static final String LINE_STYLE_DASH_STR = "dash"; //$NON-NLS-1$

  public static final String LINE_STYLE_DOT_STR = "dot"; //$NON-NLS-1$

  public static final String LINE_STYLE_DASHDOT_STR = "dashdot"; //$NON-NLS-1$

  public static final String LINE_STYLE_DASHDOTDOT_STR = "dashdotdot"; //$NON-NLS-1$

  public int getHeight();

  public int getWidth();

  public String getTitle();

  // Chart Methods
  public Font getTitleFont();

  public List getSubtitles();

  public Paint getChartBackgroundPaint();

  public Image getChartBackgroundImage();

  public boolean isBorderVisible();

  public Paint getBorderPaint();

  public Font getLegendFont();

  public boolean isLegendBorderVisible();

  public RectangleEdge getLegendPosition();

  // Plot methods
  public RectangleEdge getTitlePosition();

  public Paint[] getPaintSequence();

  public Paint getPlotBackgroundPaint();

  public Image getPlotBackgroundImage();

  public boolean isLegendIncluded();

  public boolean isThreeD();

  public boolean isDisplayLabels();

  public String getNoDataMessage();

  public Float getForegroundAlpha();

  public Float getBackgroundAlpha();

}
