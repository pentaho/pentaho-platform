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

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.PieSectionEntity;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.imagemap.ImageMapUtilities;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.Dataset;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.IPentahoRequestContext;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.runtime.TemplateUtil;
import org.pentaho.platform.uifoundation.messages.Messages;
import org.pentaho.platform.util.messages.LocaleHelper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CategoryDatasetChartComponent extends AbstractJFreeChartComponent {
  private static final long serialVersionUID = -6268840271596447555L;

  protected String seriesName = null;

  public CategoryDatasetChartComponent( final int chartType, final String definitionPath, final int width,
      final int height, final IPentahoUrlFactory urlFactory, final List messages ) {
    super( chartType, definitionPath, width, height, urlFactory, messages );
    // Set the XSL file to be used to generate the HTML
    setXsl( "text/html", "Chart.xsl" ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  public CategoryDatasetChartComponent( final String definitionPath, final IPentahoUrlFactory urlFactory,
      final ArrayList messages ) {
    super( definitionPath, urlFactory, messages );
    // Set the XSL file to be used to generate the HTML
    setXsl( "text/html", "Chart.xsl" ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  public CategoryDatasetChartComponent( final IPentahoUrlFactory urlFactory, final List messages ) {
    super( urlFactory, messages );
    // Set the XSL file to be used to generate the HTML
    setXsl( "text/html", "Chart.xsl" ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  public void setSeriesName( final String seriesName ) {
    this.seriesName = seriesName;
  }

  @Override
  public Dataset createChart( final Document doc ) {
    if ( actionPath != null ) { // if we have a solution then get the values
      values = getActionData();
    }

    if ( values == null ) {
      // we could not get any data
      return null;
    }
    // get the chart node from the document
    Node chartAttributes = doc.selectSingleNode( "//" + AbstractChartComponent.CHART_NODE_NAME ); //$NON-NLS-1$
    // create the definition

    String chType = chartAttributes.selectSingleNode( "chart-type" ).getText(); //$NON-NLS-1$
    CategoryDatasetChartDefinition chartDefinition = null;

    if ( ChartDefinition.BAR_LINE_CHART_STR.equalsIgnoreCase( chType ) ) {
      chartDefinition = new BarLineChartDefinition( (IPentahoResultSet) values, byRow, chartAttributes, getSession() );
    } else {
      chartDefinition =
          new CategoryDatasetChartDefinition( (IPentahoResultSet) values, byRow, chartAttributes, getSession() );
    }

    // set the misc values from chartDefinition
    setChartType( chartDefinition.getChartType() );
    setTitle( chartDefinition.getTitle() );

    // get the URL template
    Node urlTemplateNode = chartAttributes.selectSingleNode( AbstractChartComponent.URLTEMPLATE_NODE_NAME );
    if ( urlTemplateNode != null ) {
      setUrlTemplate( urlTemplateNode.getText() );
    }

    // get the additional parameter
    Node paramName2Node = chartAttributes.selectSingleNode( AbstractChartComponent.PARAM2_NODE_NAME );
    if ( paramName2Node != null ) {
      seriesName = paramName2Node.getText();
    }

    if ( ( chartDefinition.getWidth() != -1 ) && ( width == -1 ) ) {
      setWidth( chartDefinition.getWidth() );
    }
    if ( ( chartDefinition.getHeight() != -1 ) && ( height == -1 ) ) {
      setHeight( chartDefinition.getHeight() );
    }

    return chartDefinition;
  }

  @Override
  public Document getXmlContent() {

    // Create a document that describes the result
    Document result = DocumentHelper.createDocument();
    IPentahoRequestContext requestContext = PentahoRequestContextHolder.getRequestContext();
    setXslProperty( "baseUrl", requestContext.getContextPath() ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    setXslProperty( "fullyQualifiedServerUrl", PentahoSystem.getApplicationContext().getFullyQualifiedServerURL() ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    String mapName = "chart" + AbstractChartComponent.chartCount++; //$NON-NLS-1$
    Document chartDefinition = jcrHelper.getSolutionDocument( definitionPath, RepositoryFilePermission.READ );
    if ( chartDefinition == null ) {
      Element errorElement = result.addElement( "error" ); //$NON-NLS-1$
      errorElement
          .addElement( "title" ).setText( Messages.getInstance().getString( "ABSTRACTCHARTEXPRESSION.ERROR_0001_ERROR_GENERATING_CHART" ) ); //$NON-NLS-1$ //$NON-NLS-2$
      String message = Messages.getInstance().getString( "CHARTS.ERROR_0001_CHART_DEFINIION_MISSING", definitionPath ); //$NON-NLS-1$
      errorElement.addElement( "message" ).setText( message ); //$NON-NLS-1$
      error( message );
      return result;
    }
    // create a pie definition from the XML definition
    dataDefinition = createChart( chartDefinition );

    if ( dataDefinition == null ) {
      Element errorElement = result.addElement( "error" ); //$NON-NLS-1$
      errorElement
          .addElement( "title" ).setText( Messages.getInstance().getString( "ABSTRACTCHARTEXPRESSION.ERROR_0001_ERROR_GENERATING_CHART" ) ); //$NON-NLS-1$ //$NON-NLS-2$
      String message = Messages.getInstance().getString( "CHARTS.ERROR_0002_CHART_DATA_MISSING", actionPath ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      errorElement.addElement( "message" ).setText( message ); //$NON-NLS-1$
      // System .out.println( result.asXML() );
      return result;
    }

    // create an image for the dial using the JFreeChart engine
    PrintWriter printWriter = new PrintWriter( new StringWriter() );
    // we'll dispay the title in HTML so that the dial image does not have
    // to
    // accommodate it
    String chartTitle = ""; //$NON-NLS-1$
    try {
      if ( width == -1 ) {
        width = Integer.parseInt( chartDefinition.selectSingleNode( "/chart/width" ).getText() ); //$NON-NLS-1$
      }
    } catch ( NumberFormatException ignored ) {
      // go with the default
    }
    try {
      if ( height == -1 ) {
        height = Integer.parseInt( chartDefinition.selectSingleNode( "/chart/height" ).getText() ); //$NON-NLS-1$
      }
    } catch ( NumberFormatException ignored ) {
      // go with the default
    }
    if ( chartDefinition.selectSingleNode( "/chart/" + AbstractChartComponent.URLTEMPLATE_NODE_NAME ) != null ) { //$NON-NLS-1$
      urlTemplate =
          chartDefinition.selectSingleNode( "/chart/" + AbstractChartComponent.URLTEMPLATE_NODE_NAME ).getText(); //$NON-NLS-1$
    }

    if ( chartDefinition.selectSingleNode( "/chart/paramName" ) != null ) { //$NON-NLS-1$
      paramName = chartDefinition.selectSingleNode( "/chart/paramName" ).getText(); //$NON-NLS-1$
    }

    Element root = result.addElement( "charts" ); //$NON-NLS-1$
    DefaultCategoryDataset chartDataDefinition = (DefaultCategoryDataset) dataDefinition;
    if ( chartDataDefinition.getRowCount() > 0 ) {
      // create temporary file names
      String[] tempFileInfo = createTempFile();
      String fileName = tempFileInfo[AbstractChartComponent.FILENAME_INDEX];
      String filePathWithoutExtension = tempFileInfo[AbstractChartComponent.FILENAME_WITHOUT_EXTENSION_INDEX];

      ChartRenderingInfo info = new ChartRenderingInfo( new StandardEntityCollection() );
      JFreeChartEngine.saveChart( chartDataDefinition, chartTitle,
          "", filePathWithoutExtension, width, height, JFreeChartEngine.OUTPUT_PNG, printWriter, info, this ); //$NON-NLS-1$
      applyOuterURLTemplateParam();
      populateInfo( info );
      Element chartElement = root.addElement( "chart" ); //$NON-NLS-1$
      chartElement.addElement( "mapName" ).setText( mapName ); //$NON-NLS-1$
      chartElement.addElement( "width" ).setText( Integer.toString( width ) ); //$NON-NLS-1$
      chartElement.addElement( "height" ).setText( Integer.toString( height ) ); //$NON-NLS-1$
      for ( int row = 0; row < chartDataDefinition.getRowCount(); row++ ) {
        for ( int column = 0; column < chartDataDefinition.getColumnCount(); column++ ) {
          Number value = chartDataDefinition.getValue( row, column );
          Comparable rowKey = chartDataDefinition.getRowKey( row );
          Comparable columnKey = chartDataDefinition.getColumnKey( column );
          Element valueElement = chartElement.addElement( "value2D" ); //$NON-NLS-1$
          valueElement.addElement( "value" ).setText( value.toString() ); //$NON-NLS-1$
          valueElement.addElement( "row-key" ).setText( rowKey.toString() ); //$NON-NLS-1$
          valueElement.addElement( "column-key" ).setText( columnKey.toString() ); //$NON-NLS-1$
        }
      }
      String mapString = ImageMapUtilities.getImageMap( mapName, info );
      chartElement.addElement( "imageMap" ).setText( mapString ); //$NON-NLS-1$
      chartElement.addElement( "image" ).setText( fileName ); //$NON-NLS-1$
    }
    return result;
  }

  private void populatePieSectionEntity( PieSectionEntity pieSectionEntity ) {
    String value = pieSectionEntity.getSectionKey().toString();
    if ( paramName == null ) {
      pieSectionEntity.setURLText( value );
    } else {
      try {
        String encodedVal = URLEncoder.encode( value, LocaleHelper.getSystemEncoding() );
        String drillURL = TemplateUtil.applyTemplate( urlTemplate, paramName, encodedVal );
        pieSectionEntity.setURLText( drillURL );

      } catch ( UnsupportedEncodingException ignored ) {
        this.getLogger().debug( ignored );
      }
    }
  }

  private void populateCategoryItemEntity( CategoryItemEntity categoryItemEntity, List seriesListArray ) {
    if ( seriesListArray == null ) {
      seriesListArray = new ArrayList( categoryItemEntity.getDataset().getRowKeys() );
    }
    String category = categoryItemEntity.getCategory().toString();
    if ( paramName == null ) {
      categoryItemEntity.setURLText( category );
    } else {
      try {
        String encodedVal = URLEncoder.encode( category, LocaleHelper.getSystemEncoding() );
        String drillURL = TemplateUtil.applyTemplate( urlTemplate, paramName, encodedVal );

        /**********
         * PLATFORM-841: get series name from categoryItemEntity tooltip ********
         * 
         * The entity collection generated by JFreeChartEngine will not always be fully populated (i.e., the entity
         * collection WILL NOT contain items for values that are zero (0)), because items with zero value do not
         * need to be drawn! So, we cannot iterate through the seriseListArray and apply values from there to the
         * values in the entity collection. Instead, we must use the series index contained in the entity record to
         * access the appropriate series name.
         */

        // ... get the series index and use it to find the series name in
        // the seriesListArray ...
        //
        int seriesIndex = categoryItemEntity.getSeries();
        encodedVal = Messages.getInstance().getString( "CategoryDatasetChartComponent.UNKNOWN_SERIES" ); //$NON-NLS-1$
        if ( ( seriesIndex >= 0 ) && ( seriesIndex < seriesListArray.size() ) ) {
          encodedVal =
              URLEncoder.encode( seriesListArray.get( seriesIndex ).toString(), LocaleHelper.getSystemEncoding() );
        } else {
          error( Messages.getInstance().getErrorString(
              "CategoryDatasetChartComponent.ERROR_0002_INVALID_SERIES_INDEX", String.valueOf( seriesIndex ) ) ); //$NON-NLS-1$
        }

        /********** PLATFORM-841 change end **********/

        if ( seriesName == null ) {
          drillURL = TemplateUtil.applyTemplate( drillURL, "SERIES", encodedVal ); //$NON-NLS-1$
        } else {
          drillURL = TemplateUtil.applyTemplate( drillURL, seriesName, encodedVal );
        }
        categoryItemEntity.setURLText( drillURL );

      } catch ( UnsupportedEncodingException ignored ) {
        // TODO Auto-generated catch block
        this.getLogger().debug( ignored );
      }
    }
  }

  private void populateInfo( final ChartRenderingInfo info ) {
    if ( urlTemplate == null ) {
      return;
    }
    ArrayList seriesListArray = null;

    Iterator iter = info.getEntityCollection().iterator();
    while ( iter.hasNext() ) {
      ChartEntity entity = (ChartEntity) iter.next();
      if ( entity instanceof PieSectionEntity ) {
        populatePieSectionEntity( (PieSectionEntity) entity );
      } else if ( entity instanceof CategoryItemEntity ) {
        populateCategoryItemEntity( (CategoryItemEntity) entity, seriesListArray );
      }
    }
  }

  @Override
  public boolean validate() {
    // TODO Auto-generated method stub
    return true;
  }

}
