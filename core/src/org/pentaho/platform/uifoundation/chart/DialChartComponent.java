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
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.imagemap.ImageMapUtilities;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DefaultValueDataset;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.commons.connection.memory.MemoryResultSet;
import org.pentaho.platform.api.engine.IPentahoRequestContext;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.uifoundation.messages.Messages;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class DialChartComponent extends AbstractJFreeChartComponent {
  private static final long serialVersionUID = -6268840271596447555L;

  public static final String VALUE_NODE_NAME = "dialValue"; //$NON-NLS-1$

  public static final String MAXVALUE_NODE_NAME = "dialMaximum"; //$NON-NLS-1$ 

  public static final String MINVALUE_NODE_NAME = "dialMinimum"; //$NON-NLS-1$

  public static final int TYPE_DIAL = 1;

  public static final int TYPE_THERMOMETER = 2;

  private Double value = null;

  public DialChartComponent( final int chartType, final String definitionPath, final int width, final int height,
      final IPentahoUrlFactory urlFactory, final List messages ) {
    super( chartType, definitionPath, width, height, urlFactory, messages );
  }

  public DialChartComponent( final String definitionPath, final IPentahoUrlFactory urlFactory,
                             final ArrayList messages ) {
    super( definitionPath, urlFactory, messages );
  }

  public DialChartComponent( final IPentahoUrlFactory urlFactory, final List messages ) {
    super( urlFactory, messages );
  }

  @Override
  public Dataset createChart( final Document doc ) {

    // get the chart node from the document
    Node chartAttributes = doc.selectSingleNode( "//" + AbstractChartComponent.CHART_NODE_NAME ); //$NON-NLS-1$

    // Try to retrieve the data values from the <data> XML block
    if ( actionPath != null ) {
      values = getActionData();
    }

    // No <data> node; check for <dialvalue>, <dialmaximum>, <dialminimum> nodes
    if ( values == null ) {

      Node valueNode = chartAttributes.selectSingleNode( DialChartComponent.VALUE_NODE_NAME );
      Node maxValueNode = chartAttributes.selectSingleNode( DialChartComponent.MAXVALUE_NODE_NAME );
      Node minValueNode = chartAttributes.selectSingleNode( DialChartComponent.MINVALUE_NODE_NAME );

      double val = 0;
      double min = -1;
      double max = -1;

      if ( valueNode != null ) {

        try {
          val = Double.parseDouble( valueNode.getText() );
        } catch ( Exception e ) {
          AbstractChartComponent.logger.error( Messages.getInstance().getErrorString(
              "DIALCHARTCOMPONENT.ERROR_0001_ERROR_PARSING_VALUE", valueNode.getText() ), e ); //$NON-NLS-1$
          val = 0;
        }

        if ( ( minValueNode != null ) && ( maxValueNode != null ) ) {

          try {
            min = Double.parseDouble( minValueNode.getText() );
            max = Double.parseDouble( maxValueNode.getText() );
          } catch ( Exception e ) {
            AbstractChartComponent.logger.error( Messages.getInstance().getErrorString(
                "DIALCHARTCOMPONENT.ERROR_0001_ERROR_PARSING_VALUE", //$NON-NLS-1$
                minValueNode.getText(), maxValueNode.getText() ), e );
            min = -1;
            max = -1;
          }
        }

        // Its OK if there is no min and max - we will derive it later if absent.
        MemoryResultSet set = new MemoryResultSet();
        if ( ( min == -1 ) && ( max == -1 ) ) {
          set.addRow( new Object[] { new Double( val ) } );
        } else {
          set.addRow( new Object[] { new Double( val ), new Double( min ), new Double( max ) } );
        }
        values = set;
      }
    }

    // If, at this point, the values object is null, we will continue to execute, and assume
    // the user is has set the dial value using the API.

    // create the definition
    DialWidgetDefinition chartDefinition =
        new DialWidgetDefinition( (IPentahoResultSet) values, byRow, chartAttributes, width, height, getSession() );

    // This local variable "value" is here for parity with the way the DashboardWidgetComponent functions...
    // If value gets set, all other data methods executed previously are overridden.
    if ( value != null ) {
      chartDefinition.setValue( value.doubleValue() );
      chartDefinition.deriveMinMax( value.doubleValue() );
    }

    // set the misc values from chartDefinition
    setChartType( JFreeChartEngine.DIAL_CHART_TYPE );
    setTitle( chartDefinition.getTitle() );

    // get the URL template
    Node urlTemplateNode = chartAttributes.selectSingleNode( AbstractChartComponent.URLTEMPLATE_NODE_NAME );
    if ( urlTemplateNode != null ) {
      setUrlTemplate( urlTemplateNode.getText() );
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
      if ( height == -1 ) {
        height = Integer.parseInt( chartDefinition.selectSingleNode( "/chart/height" ).getText() ); //$NON-NLS-1$
      }
    } catch ( Exception e ) {
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
    DefaultValueDataset chartDataDefinition = (DefaultValueDataset) dataDefinition;

    // if (dataDefinition.getRowCount() > 0) {
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
    // for (int row = 0; row < chartDataDefinition.getRowCount(); row++) {
    // for (int column = 0; column < chartDataDefinition.getColumnCount(); column++) {
    // Number value = chartDataDefinition.getValue(row, column);
    // Comparable rowKey = chartDataDefinition.getRowKey(row);
    // Comparable columnKey = chartDataDefinition.getColumnKey(column);
    //                    Element valueElement = chartElement.addElement("value2D"); //$NON-NLS-1$
    //                    valueElement.addElement("value").setText(value.toString()); //$NON-NLS-1$
    //                    valueElement.addElement("row-key").setText(rowKey.toString()); //$NON-NLS-1$
    //                    valueElement.addElement("column-key").setText(columnKey.toString()); //$NON-NLS-1$
    // }
    // }
    String mapString = ImageMapUtilities.getImageMap( mapName, info );
    chartElement.addElement( "imageMap" ).setText( mapString ); //$NON-NLS-1$
    chartElement.addElement( "image" ).setText( fileName ); //$NON-NLS-1$
    // }
    return result;
  }

  private void populateInfo( final ChartRenderingInfo info ) {
  }

  @Override
  public boolean validate() {
    return true;
  }

  /**
   * Sets the value to be displayed by the dial.
   * 
   * @param value
   *          The dial value
   */
  public void setValue( final double value ) {
    this.value = new Double( value );
  }

}
