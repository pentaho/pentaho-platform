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

import ofc4j.model.Chart;
import ofc4j.model.Text;
import ofc4j.model.axis.Axis;
import ofc4j.model.axis.Label.Rotation;
import ofc4j.model.axis.XAxis;
import ofc4j.model.axis.YAxis;
import ofc4j.model.elements.Element;
import org.apache.commons.logging.Log;
import org.dom4j.Node;
import org.pentaho.commons.connection.IPentahoDataTypes;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.commons.connection.PentahoDataTransmuter;
import org.pentaho.platform.engine.services.runtime.TemplateUtil;
import org.pentaho.platform.plugin.action.messages.Messages;

import java.util.ArrayList;
import java.util.Properties;

public abstract class AbstractChartFactory implements IChartFactory {

  public static class MinMax {
    int min;
    int max;

    public MinMax( int min, int max ) {
      this.min = min;
      this.max = max;
    }
  }

  // general chart related elements
  protected static final String COLOR_NODE_LOC = "color"; //$NON-NLS-1$

  private static final String TITLE_NODE_LOC = "title"; //$NON-NLS-1$
  private static final String TITLE_FONT_NODE_LOC = "title-font"; //$NON-NLS-1$
  private static final String DATASET_TYPE_NODE_LOC = "dataset-type"; //$NON-NLS-1$
  private static final String COLOR_PALETTE_NODE_LOC = "color-palette"; //$NON-NLS-1$  
  private static final String PLOT_BACKGROUND_NODE_LOC = "plot-background"; //$NON-NLS-1$
  private static final String PLOT_BACKGROUND_COLOR_XPATH = "@type"; //attribute of plot-background  //$NON-NLS-1$
  private static final String CHART_BACKGROUND_NODE_LOC = "chart-background"; //$NON-NLS-1$
  private static final String CHART_BACKGROUND_COLOR_XPATH = "@type"; //attribute of chart-background  //$NON-NLS-1$
  private static final String URL_TEMPLATE_NODE_LOC = "url-template"; //$NON-NLS-1$
  private static final String TOOLTIP_NODE_LOC = "tooltip"; //$NON-NLS-1$
  private static final String ORIENTATION_NODE_LOC = "orientation"; //$NON-NLS-1$
  private static final String ALPHA_NODE_LOC = "alpha"; //$NON-NLS-1$

  // font related elements
  private static final String FONT_FAMILY_NODE_LOC = "font-family"; //$NON-NLS-1$
  private static final String FONT_SIZE_NODE_LOC = "size"; //$NON-NLS-1$
  private static final String FONT_BOLD_NODE_LOC = "is-bold"; //$NON-NLS-1$
  private static final String FONT_ITALIC_NODE_LOC = "is-italic"; //$NON-NLS-1$

  // domain axis related elements
  private static final String DOMAIN_STROKE_NODE_LOC = "domain-stroke"; //$NON-NLS-1$
  private static final String DOMAIN_GRID_COLOR_NODE_LOC = "domain-grid-color"; //$NON-NLS-1$
  private static final String DOMAIN_COLOR_NODE_LOC = "domain-color"; //$NON-NLS-1$
  private static final String DOMAIN_STEPS_NODE_LOC = "domain-steps"; //$NON-NLS-1$
  private static final String DOMAIN_TITLE_NODE_LOC = "domain-title"; //$NON-NLS-1$
  private static final String DOMAIN_TITLE_FONT_NODE_LOC = "domain-title-font"; //$NON-NLS-1$
  private static final String DOMAIN_MAXIMUM_NODE_LOC = "domain-maximum"; //$NON-NLS-1$
  private static final String DOMAIN_MINIMUM_NODE_LOC = "domain-minimum"; //$NON-NLS-1$
  private static final String DOMAIN_ROTATION_NODE_LOC = "domain-label-rotation"; //$NON-NLS-1$
  private static final String DOMAIN_ROTATION_DIR_NODE_LOC = "domain-label-rotation-dir"; //$NON-NLS-1$

  // range axis related elements
  private static final String RANGE_STROKE_NODE_LOC = "range-stroke"; //$NON-NLS-1$
  private static final String RANGE_GRID_COLOR_NODE_LOC = "range-grid-color"; //$NON-NLS-1$
  private static final String RANGE_COLOR_NODE_LOC = "range-color"; //$NON-NLS-1$

  protected static final String RANGE_STEPS_NODE_LOC = "range-steps"; //$NON-NLS-1$

  private static final String RANGE_TITLE_NODE_LOC = "range-title"; //$NON-NLS-1$
  private static final String RANGE_TITLE_FONT_NODE_LOC = "range-title-font"; //$NON-NLS-1$
  private static final String RANGE_MAXIMUM_NODE_LOC = "range-maximum"; //$NON-NLS-1$
  private static final String RANGE_MINIMUM_NODE_LOC = "range-minimum"; //$NON-NLS-1$

  // color types

  private static final String COLOR_TYPE = "color"; //$NON-NLS-1$

  private static final String CSS_FONT_STYLES = "font-family: {fontfamily}; font-size: {fontsize}px; " + //$NON-NLS-1$
      "font-weight: {fontweight}; font-style: {fontstyle};"; //$NON-NLS-1$

  // Orientation Type Values (ORIENTATION_NODE_LOC)
  protected static final String HORIZONTAL_ORIENTATION = "horizontal"; //$NON-NLS-1$
  protected static final String VERTICAL_ORIENTATION = "vertical"; //$NON-NLS-1$

  // Dataset Type Values
  protected static final String CATEGORY_TYPE = "CategoryDataset"; //$NON-NLS-1$
  protected static final String XY_TYPE = "XYSeriesCollection"; //$NON-NLS-1$
  protected static final String XYZ_TYPE = "XYZSeriesCollection"; //$NON-NLS-1$

  // defaults
  private static final String DATASET_TYPE_DEFAULT = "CategoryDataset"; //$NON-NLS-1$
  private static final String CSS_FONT_FAMILY_DEFAULT = "Arial"; //$NON-NLS-1$
  private static final String CSS_FONT_SIZE_DEFAULT = "14"; //$NON-NLS-1$
  private static final String CSS_FONT_WEIGHT_DEFAULT = "normal"; //$NON-NLS-1$
  private static final String CSS_FONT_STYLE_DEFAULT = "normal"; //$NON-NLS-1$

  protected static final String AXIS_GRID_COLOR_DEFAULT = "#aaaaaa"; //$NON-NLS-1$
  protected static final String AXIS_COLOR_DEFAULT = "#000000"; //$NON-NLS-1$

  protected static final String ORIENTATION_DEFAULT = "vertical"; //$NON-NLS-1$

  @SuppressWarnings( "nls" )
  protected static final String[] COLORS_DEFAULT = { "#006666", "#0066CC", "#009999", "#336699", "#339966", "#3399FF",
    "#663366", "#666666", "#666699", "#669999", "#6699CC", "#66CCCC", "#993300", "#999933", "#999966", "#999999",
    "#9999CC", "#9999FF", "#99CC33", "#99CCCC", "#99CCFF", "#CC6600", "#CC9933", "#CCCC33", "#CCCC66", "#CCCC99",
    "#CCCCCC", "#FF9900", "#FFCC00", "#FFCC66" };

  protected ArrayList<String> colors = new ArrayList<String>();
  protected Chart chart = new Chart();
  protected ArrayList<Element> elements = new ArrayList<Element>();
  protected Node chartNode;
  private Log log;

  // data related members
  public String[] rowHeaders;
  public String[] columnHeaders;
  public IPentahoResultSet data;
  public boolean hasRowHeaders = false;
  public boolean hasColumnHeaders = false;

  protected String datasetType;

  // general chart members
  protected String orientation;
  protected String baseURLTemplate;
  protected String tooltipText;
  protected Float alpha;

  protected abstract void createElements();

  protected void setupStyles() {
    Node temp = chartNode.selectSingleNode( TOOLTIP_NODE_LOC );
    if ( getValue( temp ) != null ) {
      tooltipText = getValue( temp );
    }

    temp = chartNode.selectSingleNode( ALPHA_NODE_LOC );
    if ( getValue( temp ) != null ) {
      alpha = Float.parseFloat( getValue( temp ) );
    }
  }

  public void setChartNode( Node chartNode ) {
    this.chartNode = chartNode;
  }

  public void setLog( Log log ) {
    this.log = log;
  }

  public void validateData() {
    // make sure data is of a category dataset.
    if ( getRowCount() < 1 ) {
      throw new RuntimeException( Messages.getInstance().getErrorString( "AbstractChartFactory.ERROR_0001_ROW_COUNT" ) ); //$NON-NLS-1$
    }
    if ( getColumnCount() < 1 ) {
      throw new RuntimeException( Messages.getInstance()
          .getErrorString( "AbstractChartFactory.ERROR_0002_COLUMN_COUNT" ) ); //$NON-NLS-1$
    }

    // check first row and first column, making sure the values are numbers
    for ( int c = 0; c < getColumnCount(); c++ ) {
      Object data = getValueAt( 0, c );
      if ( !( data instanceof Number ) ) {
        throw new RuntimeException( Messages.getInstance().getErrorString(
            "AbstractChartFactory.ERROR_0003_INVALID_TYPE" ) ); //$NON-NLS-1$
      }
    }

    // check first row
    for ( int r = 1; r < getRowCount(); r++ ) {
      Object data = getValueAt( r, 0 );
      if ( !( data instanceof Number ) ) {
        throw new RuntimeException( Messages.getInstance().getErrorString(
            "AbstractChartFactory.ERROR_0003_INVALID_TYPE" ) ); //$NON-NLS-1$
      }
    }
  }

  public String convertToJson() {

    // first, determine the dataset type

    Node temp = chartNode.selectSingleNode( DATASET_TYPE_NODE_LOC );
    if ( getValue( temp ) != null ) {
      datasetType = getValue( temp );
    } else {
      // Default is CategoricalDataset
      datasetType = DATASET_TYPE_DEFAULT;
    }

    validateData();

    // These things apply to pretty much all charts
    setupColors();
    setupStyles();
    setupDomain();
    setupOnclick();

    // Build the elements (usually Chart Type specific)
    createElements();

    // Setup a few additional things after creating elements
    setupTitles();
    setupRange();

    chart.addElements( elements );
    return chart.toString();
  }

  //
  // Setup Methods
  //

  protected void setupTitles() {
    // in the Pentaho chart, range-title equals yLengend title
    Node rangeTitle = chartNode.selectSingleNode( RANGE_TITLE_NODE_LOC );
    Node rangeTitleFont = chartNode.selectSingleNode( RANGE_TITLE_FONT_NODE_LOC );
    Node title = chartNode.selectSingleNode( TITLE_NODE_LOC );

    // in the Pentaho chart, domain-title equals xLengend title
    Node domainTitle = chartNode.selectSingleNode( DOMAIN_TITLE_NODE_LOC );
    Node domainTitleFont = chartNode.selectSingleNode( DOMAIN_TITLE_FONT_NODE_LOC );
    Node titleFont = chartNode.selectSingleNode( TITLE_FONT_NODE_LOC );

    if ( getValue( title ) != null ) {
      Text titleText = new Text();
      titleText.setText( getValue( title ) );
      titleText.setStyle( buildCSSStringFromNode( titleFont ) );
      chart.setTitle( titleText );
    }

    Text domainText = new Text();
    if ( getValue( domainTitle ) != null ) {
      domainText.setText( getValue( domainTitle ) );
    } else {
      // TODO figure out what to do if the header isn't CategoryDataset
      domainText.setText( columnHeaders[0] );
    }
    domainText.setStyle( buildCSSStringFromNode( domainTitleFont ) );

    Text rangeText = new Text();
    if ( getValue( rangeTitle ) != null ) {
      rangeText.setText( getValue( rangeTitle ) );
      rangeText.setStyle( buildCSSStringFromNode( rangeTitleFont ) );
      chart.setYLegend( rangeText );
    }

    // TODO: need to support YRightLegend, exposed as y2_legend in open flash charts

    chart.setXLegend( domainText );
  }

  /**
   * Setup colors for the series and also background
   */
  protected void setupColors() {

    Node temp = chartNode.selectSingleNode( COLOR_PALETTE_NODE_LOC );
    if ( temp != null ) {
      Object[] colorNodes = temp.selectNodes( COLOR_NODE_LOC ).toArray();
      for ( int j = 0; j < colorNodes.length; j++ ) {
        colors.add( getValue( (Node) colorNodes[j] ) );
      }
    } else {
      for ( int i = 0; i < COLORS_DEFAULT.length; i++ ) {
        colors.add( COLORS_DEFAULT[i] );
      }
    }

    // Use either chart-background or plot-background (chart takes precendence)
    temp = chartNode.selectSingleNode( PLOT_BACKGROUND_NODE_LOC );
    if ( getValue( temp ) != null ) {
      String type = temp.valueOf( PLOT_BACKGROUND_COLOR_XPATH );
      if ( type != null && COLOR_TYPE.equals( type ) ) {
        chart.setBackgroundColour( getValue( temp ) );
        chart.setInnerBackgroundColour( getValue( temp ) );
      }
    }
    temp = chartNode.selectSingleNode( CHART_BACKGROUND_NODE_LOC );
    if ( getValue( temp ) != null ) {
      String type = temp.valueOf( CHART_BACKGROUND_COLOR_XPATH );
      if ( type != null && COLOR_TYPE.equals( type ) ) {
        chart.setBackgroundColour( getValue( temp ) );
      }
    }
  }

  public Axis setupDomain() {
    String[] labels = null;
    Number domainMin = null;
    Number domainMax = null;
    Integer stepforchart = null;
    String domainRotation = null;

    if ( CATEGORY_TYPE.equals( datasetType ) ) {
      int rowCount = getRowCount();
      labels = new String[rowCount];
      for ( int j = 0; j < rowCount; j++ ) {
        labels[j] = getRowHeader( j );
      }
    } else if ( XYZ_TYPE.equals( datasetType ) || XY_TYPE.equals( datasetType ) ) {
      domainMin = ( (Number) getValueAt( 0, 0 ) ).intValue();
      domainMax = domainMin;
      // Iterate over rows
      for ( int r = 1; r < getRowCount(); r++ ) {
        if ( domainMin.intValue() > ( (Number) getValueAt( r, 0 ) ).intValue() ) {
          domainMin = ( (Number) getValueAt( r, 0 ) ).intValue();
        }
        if ( domainMax.intValue() < ( (Number) getValueAt( r, 0 ) ).intValue() ) {
          domainMax = ( (Number) getValueAt( r, 0 ) ).intValue();
        }
      }

      if ( domainMin.equals( domainMax ) ) {
        if ( domainMin.intValue() == 0 ) {
          domainMax = new Integer( 100 );
        } else if ( domainMin.intValue() < 0 ) {
          domainMax = new Integer( 0 );
        } else {
          domainMin = new Integer( 0 );
        }
      }

      int steps = 9;
      int diff = domainMax.intValue() - domainMin.intValue();

      Node temp = chartNode.selectSingleNode( DOMAIN_STEPS_NODE_LOC );
      if ( getValue( temp ) != null ) {
        steps = new Integer( getValue( temp ) ).intValue();
      }

      int chunksize = diff / steps;

      if ( chunksize > 0 ) {
        stepforchart = new Integer( chunksize );

        // If actual min is positive, don't go below ZERO
        if ( domainMin.intValue() >= 0 && domainMin.intValue() - chunksize < 0 ) {
          domainMin = 0;
        } else {
          domainMin = domainMin.intValue() - chunksize;
        }
        domainMax = domainMin.intValue() + ( chunksize * ( steps + 2 ) );
      }
      temp = chartNode.selectSingleNode( DOMAIN_MINIMUM_NODE_LOC );
      if ( getValue( temp ) != null ) {
        domainMin = new Integer( getValue( temp ) ).intValue();
      }

      temp = chartNode.selectSingleNode( DOMAIN_MAXIMUM_NODE_LOC );
      if ( getValue( temp ) != null ) {
        domainMax = new Integer( getValue( temp ) ).intValue();
      }
    }

    String domainColor = AXIS_COLOR_DEFAULT;
    String domainGridColor = AXIS_GRID_COLOR_DEFAULT;
    int domainStroke = 1;

    Node temp = chartNode.selectSingleNode( DOMAIN_COLOR_NODE_LOC );
    if ( getValue( temp ) != null ) {
      domainColor = getValue( temp );
    }

    temp = chartNode.selectSingleNode( DOMAIN_GRID_COLOR_NODE_LOC );
    if ( getValue( temp ) != null ) {
      domainGridColor = getValue( temp );
    }

    temp = chartNode.selectSingleNode( DOMAIN_STROKE_NODE_LOC );
    if ( getValue( temp ) != null ) {
      domainStroke = Integer.parseInt( getValue( temp ) );
    }

    temp = chartNode.selectSingleNode( DOMAIN_ROTATION_NODE_LOC );
    if ( getValue( temp ) != null ) {
      domainRotation = getValue( temp );
    }

    // Orientation
    temp = chartNode.selectSingleNode( ORIENTATION_NODE_LOC );
    if ( getValue( temp ) != null ) {
      orientation = getValue( temp );
    } else {
      orientation = ORIENTATION_DEFAULT;
    }

    if ( HORIZONTAL_ORIENTATION.equals( orientation ) ) {
      YAxis yaxis = new YAxis();
      if ( labels != null ) {
        // BISERVER-3075: must reverse the category labels on hbar
        // charts due to bug in OFC2.
        String[] reversedLabels = new String[labels.length];
        int reversedLabelCount = 0;
        for ( int i = reversedLabels.length - 1; i >= 0; i-- ) {
          reversedLabels[reversedLabelCount++] = labels[i];
        }

        yaxis.setLabels( reversedLabels );
      }

      yaxis.setStroke( domainStroke );
      yaxis.setColour( domainColor );
      yaxis.setGridColour( domainGridColor );

      if ( domainMin != null && domainMax != null ) {
        yaxis.setRange( domainMin.intValue(), domainMax.intValue(), stepforchart );
      }

      chart.setYAxis( yaxis );
      return yaxis;
    } else {
      XAxis xaxis = new XAxis();
      if ( labels != null ) {
        xaxis.addLabels( labels );
      }
      xaxis.setStroke( domainStroke );
      xaxis.setColour( domainColor );
      xaxis.setGridColour( domainGridColor );
      if ( domainMin != null && domainMax != null ) {
        xaxis.setRange( domainMin.intValue(), domainMax.intValue(), stepforchart );
      }

      if ( domainRotation != null ) {

        Rotation rot = null;
        if ( domainRotation.equals( "vertical" ) ) {
          rot = Rotation.VERTICAL;
        } else if ( domainRotation.equals( "diagonal" ) ) {
          rot = Rotation.DIAGONAL;
        } else {
          rot = Rotation.HORIZONTAL;
        }

        xaxis.getLabels().setRotation( rot );
      }

      chart.setXAxis( xaxis );
      return xaxis;
    }
  }

  public MinMax getRangeMinMax() {
    int rangeMin = 0;
    int rangeMax = 100;
    if ( XYZ_TYPE.equals( datasetType ) || XY_TYPE.equals( datasetType ) ) {
      rangeMin = ( (Number) getValueAt( 0, 1 ) ).intValue();
      rangeMax = rangeMin;
      // Iterate over 2nd row
      for ( int r = 0; r < getRowCount(); r++ ) {
        if ( rangeMin > ( (Number) getValueAt( r, 1 ) ).intValue() ) {
          rangeMin = ( (Number) getValueAt( r, 1 ) ).intValue();
        }
        if ( rangeMax < ( (Number) getValueAt( r, 1 ) ).intValue() ) {
          rangeMax = ( (Number) getValueAt( r, 1 ) ).intValue();
        }
      }
    } else {
      rangeMin = ( (Number) getValueAt( 0, 0 ) ).intValue();
      rangeMax = rangeMin;
      // Iterate over columns 1+
      for ( int c = 0; c < getColumnCount(); c++ ) {
        for ( int r = 0; r < getRowCount(); r++ ) {
          if ( rangeMin > ( (Number) getValueAt( r, c ) ).intValue() ) {
            rangeMin = ( (Number) getValueAt( r, c ) ).intValue();
          }
          if ( rangeMax < ( (Number) getValueAt( r, c ) ).intValue() ) {
            rangeMax = ( (Number) getValueAt( r, c ) ).intValue();
          }
        }
      }
    }

    MinMax minMax = null;
    if ( rangeMin == rangeMax ) {
      if ( rangeMin == 0 ) {
        minMax = new MinMax( 0, 100 );
      } else if ( rangeMin < 0 ) {
        minMax = new MinMax( rangeMin, 0 );
      } else {
        minMax = new MinMax( 0, rangeMax );
      }
    } else {
      minMax = new MinMax( rangeMin, rangeMax );
    }
    return minMax;
  }

  public Axis setupRange() {
    int rangeMin = 0;
    int rangeMax = 100;
    int steps = 9;

    String rangeColor = AXIS_COLOR_DEFAULT;
    String rangeGridColor = AXIS_GRID_COLOR_DEFAULT;
    int rangeStroke = 1;

    MinMax rangeMinMax = getRangeMinMax();
    rangeMin = rangeMinMax.min;
    rangeMax = rangeMinMax.max;

    boolean minDefined = false;
    boolean maxDefined = false;

    Node temp = chartNode.selectSingleNode( RANGE_MINIMUM_NODE_LOC );
    if ( getValue( temp ) != null ) {
      rangeMin = new Integer( getValue( temp ) ).intValue();
      minDefined = true;
    }

    temp = chartNode.selectSingleNode( RANGE_MAXIMUM_NODE_LOC );
    if ( getValue( temp ) != null ) {
      rangeMax = new Integer( getValue( temp ) ).intValue();
      maxDefined = true;
    }

    temp = chartNode.selectSingleNode( RANGE_STEPS_NODE_LOC );
    if ( getValue( temp ) != null ) {
      steps = new Integer( getValue( temp ) ).intValue();
    }

    temp = chartNode.selectSingleNode( RANGE_COLOR_NODE_LOC );
    if ( getValue( temp ) != null ) {
      rangeColor = getValue( temp );
    }

    temp = chartNode.selectSingleNode( RANGE_GRID_COLOR_NODE_LOC );
    if ( getValue( temp ) != null ) {
      rangeGridColor = getValue( temp );
    }

    temp = chartNode.selectSingleNode( RANGE_STROKE_NODE_LOC );
    if ( getValue( temp ) != null ) {
      rangeStroke = Integer.parseInt( getValue( temp ) );
    }

    int diff = rangeMax - rangeMin;

    int chunksize = diff / steps;

    Integer stepforchart = null;
    if ( chunksize > 0 ) {
      stepforchart = new Integer( chunksize );

      // Read just mins/maxs only if they weren't specified
      if ( !minDefined ) {
        // If actual min is positive, don't go below ZERO
        if ( rangeMin >= 0 && rangeMin - chunksize < 0 ) {
          rangeMin = 0;
        } else {
          rangeMin = rangeMin - chunksize;
        }
      }
      if ( !maxDefined ) {
        rangeMax = rangeMin + ( chunksize * ( steps + 2 ) );
      }
    }

    if ( HORIZONTAL_ORIENTATION.equals( orientation ) ) {
      XAxis xaxis = new XAxis();
      xaxis.setRange( rangeMin, rangeMax, stepforchart );
      xaxis.setStroke( rangeStroke );
      xaxis.setColour( rangeColor );
      xaxis.setGridColour( rangeGridColor );
      chart.setXAxis( xaxis );
      return xaxis;
    } else {
      YAxis yaxis = new YAxis();
      yaxis.setRange( rangeMin, rangeMax, stepforchart );
      yaxis.setStroke( rangeStroke );
      yaxis.setColour( rangeColor );
      yaxis.setGridColour( rangeGridColor );
      chart.setYAxis( yaxis );
      return yaxis;
    }
  }

  public void setupOnclick() {
    Node urlTemplateNode = chartNode.selectSingleNode( URL_TEMPLATE_NODE_LOC );
    if ( getValue( urlTemplateNode ) != null ) {
      baseURLTemplate = getValue( urlTemplateNode );
    }
  }

  //
  // Data Related Methods
  //

  @SuppressWarnings( "deprecation" )
  public void setData( IPentahoResultSet data ) {
    hasRowHeaders = data.getMetaData().getRowHeaders() != null;
    hasColumnHeaders = data.getMetaData().getColumnHeaders() != null;
    if ( !hasRowHeaders || !hasColumnHeaders ) {
      // this populates the data's row header and col headers if not already populated
      data = PentahoDataTransmuter.transmute( data, false );
    }
    try {
      rowHeaders = PentahoDataTransmuter.getCollapsedHeaders( IPentahoDataTypes.AXIS_ROW, data, '|' );
      columnHeaders = PentahoDataTransmuter.getCollapsedHeaders( IPentahoDataTypes.AXIS_COLUMN, data, '|' );
    } catch ( Exception e ) {
      // should really NEVER get here
      if ( log != null ) {
        log.error( null, e );
      }
    }

    this.data = data;
  }

  protected int getColumnCount() {
    if ( !hasRowHeaders ) {
      return data.getColumnCount() - 1;
    } else {
      return data.getColumnCount();
    }
  }

  protected int getRowCount() {
    if ( !hasColumnHeaders ) {
      return data.getRowCount() - 1;
    } else {
      return data.getRowCount();
    }
  }

  protected String getRowHeader( int r ) {
    if ( !hasColumnHeaders ) {
      r = r + 1;
    }
    return rowHeaders[r];
  }

  protected String getColumnHeader( int c ) {
    if ( !hasRowHeaders ) {
      c = c + 1;
    }
    return columnHeaders[c];
  }

  protected Object getValueAt( int r, int c ) {
    if ( !hasRowHeaders ) {
      c = c + 1;
    }
    if ( !hasColumnHeaders ) {
      r = r + 1;
    }
    return data.getValueAt( r, c );
  }

  //
  // Utility Methods
  //

  protected String buildCSSStringFromNode( Node n ) {
    String fontFamily = getNodeValue( n, FONT_FAMILY_NODE_LOC );
    String fontSize = getNodeValue( n, FONT_SIZE_NODE_LOC );
    String fontWeight = null;
    if ( "true".equals( getNodeValue( n, FONT_BOLD_NODE_LOC ) ) ) { //$NON-NLS-1$
      fontWeight = "bold"; //$NON-NLS-1$
    }
    String fontStyle = null;
    if ( "true".equals( getNodeValue( n, FONT_ITALIC_NODE_LOC ) ) ) { //$NON-NLS-1$
      fontStyle = "italic"; //$NON-NLS-1$
    }
    return buildCSSString( fontFamily, fontSize, fontWeight, fontStyle );
  }

  protected String buildCSSString( String fontfamily, String fontsize, String fontweight, String fontstyle ) {
    Properties props = new Properties();
    props.put( "fontfamily", fontfamily != null ? fontfamily : CSS_FONT_FAMILY_DEFAULT ); //$NON-NLS-1$
    props.put( "fontsize", fontsize != null ? fontsize : CSS_FONT_SIZE_DEFAULT ); //$NON-NLS-1$
    props.put( "fontweight", fontweight != null ? fontweight : CSS_FONT_WEIGHT_DEFAULT ); //$NON-NLS-1$
    props.put( "fontstyle", fontstyle != null ? fontstyle : CSS_FONT_STYLE_DEFAULT ); //$NON-NLS-1$

    return TemplateUtil.applyTemplate( CSS_FONT_STYLES, props, null );
  }

  protected String getColor( int i ) {
    return colors.get( i % colors.size() );
  }

  public static String getNodeValue( Node parent, String node ) {
    if ( parent == null ) {
      return null;
    }
    Node textNode = parent.selectSingleNode( node );
    return getValue( textNode );
  }

  public static String getValue( Node n ) {
    if ( n != null && n.getText() != null && n.getText().length() > 0 ) {
      return n.getText().trim();
    } else {
      return null;
    }
  }
}
