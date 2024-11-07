/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.plugin.action.openflashchart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Node;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.util.XmlParseException;
import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;
import org.pentaho.platform.engine.services.runtime.TemplateUtil;
import org.pentaho.platform.engine.services.solution.ComponentBase;
import org.pentaho.platform.engine.services.solution.PentahoEntityResolver;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.plugin.action.openflashchart.factory.PentahoOFC4JChartHelper;
import org.pentaho.platform.util.UUIDUtil;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

import java.util.Properties;

/**
 * This component is a contribution from Nick Goodman and Tom Qin, it conforms to the ChartComponent API. Please see the
 * wiki for details on this component.
 * 
 * The component uses open-flash-chart-full-embedded-font.swf, found at http://www.ofc2dz.com/, which is a patched
 * version of Open Flash Chart 2, found at http://teethgrinder.co.uk/open-flash-chart-2/
 * 
 * note: onclick events are only partially implemented, due to limitations of OFC. In future releases of OFC, this
 * should be revisited.
 * 
 * Backlog: - y2_legend - barline right axis title, would need to update OFC4J - horizontal stacked bars
 * http://www.ofc2dz.com/OFC2/examples/HorizontalStackedBars.html - styled stacked bars (would require impl in OFC) -
 * dial chart (would require impl in OFC) - XY Line Chart - XY Area Chart - onclick / link support (would require
 * enhancements to OFC) - allow override of dataFunction name - review colors, move into config file for both jfree and
 * ofc? - area stacked
 * 
 * @author Nick Goodman
 * @author Tom Qin
 * @author Will Gorman (wgorman@pentaho.com)
 */
public class OpenFlashChartComponent extends ComponentBase {

  private static final long serialVersionUID = 825147871232129168L;

  private static final Log log = LogFactory.getLog( OpenFlashChartComponent.class );

  private static final String DEFAULT_FLASH_LOC = "openflashchart"; //$NON-NLS-1$

  private static final String DEFAULT_FLASH_SWF = "open-flash-chart-full-embedded-font.swf"; //$NON-NLS-1$s

  private static final String CHART_DATA = "chart-data"; //$NON-NLS-1$

  private static final String CHART_WIDTH = "width"; //$NON-NLS-1$

  private static final String CHART_HEIGHT = "height"; //$NON-NLS-1$

  private static final String OFC_URL = "ofc_url"; //$NON-NLS-1$

  // Static declarations
  public static String CHART_NODE_LOC = "chart"; //$NON-NLS-1$

  private static final String CHART_ATTRIBUTES = "chart-attributes"; //$NON-NLS-1$

  private static final String OFC_LIB_NAME = "ofc_lib_name"; //$NON-NLS-1$

  private static final String BY_ROW_PROP = "by-row"; //$NON-NLS-1$

  private static final String DEFAULT_WIDTH = "100%"; //$NON-NLS-1$

  private static final String DEFAULT_HEIGHT = "100%"; //$NON-NLS-1$

  protected String template = null;

  protected IPentahoResultSet data = null;

  protected static String flashFragment =
      "<script>function {dataFunction}() { return \"{chartJson}\";}</script>" //$NON-NLS-1$
          + "<object classid=\"clsid:D27CDB6E-AE6D-11cf-96B8-444553540000\" " //$NON-NLS-1$
          + "codebase=\"http://fpdownload.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=8,0,0,0\" " //$NON-NLS-1$
          + "width=\"{chart-width}\" height=\"{chart-height}\"  id=\"ofco{chartId}\" align=\"middle\"> " //$NON-NLS-1$
          + "<param name=\"allowScriptAccess\" value=\"sameDomain\" /> " //$NON-NLS-1$
          + "<param name=\"wmode\" value=\"opaque\">" //$NON-NLS-1$
          + "<param name=\"movie\" value=\"{ofc-url}/{ofc-libname}?get-data={dataFunction}\" /> " //$NON-NLS-1$
          + "<param name=\"quality\" value=\"high\" /> " //$NON-NLS-1$
          + "<embed src=\"{ofc-url}/{ofc-libname}?get-data={dataFunction}\" wmode=\"opaque\" quality=\"high\" bgcolor=\"#FFFFFF\" " //$NON-NLS-1$
          + "width=\"{chart-width}\" height=\"{chart-height}\"  id=\"ofce{chartId}\" align=\"middle\" allowScriptAccess=\"sameDomain\" type=\"application/x-shockwave-flash\" " //$NON-NLS-1$
          + "pluginspage=\"http://www.macromedia.com/go/getflashplayer\" /></object>"; //$NON-NLS-1$

  public OpenFlashChartComponent() {
    // default constructor
  }

  protected boolean validateAction() {
    // See if we have the chart data
    if ( !isDefinedInput( CHART_DATA ) ) {
      inputMissingError( CHART_DATA );
      return false;
    }

    // See if we have chart attributes
    if ( !isDefinedInput( CHART_ATTRIBUTES ) ) {
      if ( !isDefinedResource( CHART_ATTRIBUTES ) ) {
        inputMissingError( CHART_ATTRIBUTES );
        return false;
      }
    }

    return true;
  }

  protected boolean executeAction() {
    log.debug( "start to run open flash chart component......" ); //$NON-NLS-1$

    // input data

    IPentahoResultSet data = (IPentahoResultSet) getInputValue( CHART_DATA );
    if ( !data.isScrollable() ) {
      getLogger().debug( "ResultSet is not scrollable. Copying into memory" ); //$NON-NLS-1$
      IPentahoResultSet memSet = data.memoryCopy();
      data.close();
      data = memSet;
    }
    // chart width

    String chartWidth = null;
    String inputWidth = getInputStringValue( CHART_WIDTH );

    if ( inputWidth == null ) {
      chartWidth = DEFAULT_WIDTH;
    } else {
      chartWidth = inputWidth;
    }

    // chart height

    String chartHeight = null;
    String inputHeight = getInputStringValue( CHART_HEIGHT );

    if ( null == inputHeight ) {
      chartHeight = DEFAULT_HEIGHT;
    } else {
      chartHeight = inputHeight;
    }

    // swf file location

    String ofcURL = getInputStringValue( OFC_URL );
    if ( ofcURL == null || "".equals( ofcURL ) ) { //$NON-NLS-1$
      ofcURL = PentahoRequestContextHolder.getRequestContext().getContextPath() + DEFAULT_FLASH_LOC;
    }

    // swf file name

    String ofclibname = getInputStringValue( OFC_LIB_NAME );
    if ( ofclibname == null || "".equals( ofclibname ) ) { //$NON-NLS-1$
      ofclibname = DEFAULT_FLASH_SWF;
    }

    // chart definition

    String chartAttributeString = null;
    if ( getInputNames().contains( CHART_ATTRIBUTES ) ) {
      chartAttributeString = getInputStringValue( CHART_ATTRIBUTES );
    } else if ( isDefinedResource( CHART_ATTRIBUTES ) ) {
      IActionSequenceResource resource = getResource( CHART_ATTRIBUTES );
      chartAttributeString = getResourceAsString( resource );
    }

    Node chartNode = null;

    if ( chartAttributeString != null ) {

      // apply any additional inputs to the chart definition

      chartAttributeString = applyInputsToFormat( chartAttributeString );

      // parse the xml

      try {
        Document chartDocument = XmlDom4JHelper.getDocFromString( chartAttributeString, new PentahoEntityResolver() );

        chartNode = chartDocument.selectSingleNode( CHART_NODE_LOC );
        if ( chartNode == null ) {
          chartNode = chartDocument.selectSingleNode( CHART_ATTRIBUTES );
        }

      } catch ( XmlParseException e ) {
        getLogger().error(
            Messages.getInstance().getErrorString( "OpenFlashChartComponent.ERROR_0001_CANT_DOCUMENT_FROM_STRING" ), e ); //$NON-NLS-1$
        return false;
      }
    } else {

      // see if the chart-attributes node is available in the component definition

      chartNode = getComponentDefinition( true ).selectSingleNode( CHART_ATTRIBUTES );
    }

    // if the chart def isn't available, exit

    if ( chartNode == null ) {
      getLogger().error(
          Messages.getInstance().getErrorString( "OpenFlashChartComponent.ERROR_0002_CHART_DEFINITION_NOT_FOUND" ) ); //$NON-NLS-1$
      return false;
    }

    // Determine if we are going to read the chart data set by row or by column
    boolean byRow = false;
    if ( getInputStringValue( BY_ROW_PROP ) != null ) {
      byRow = Boolean.valueOf( getInputStringValue( BY_ROW_PROP ) ).booleanValue();
    }

    String chartJson = generateChartJson( data, chartNode, byRow );

    // generate a unique name for the function

    String chartId = UUIDUtil.getUUIDAsString().replaceAll( "[^\\w]", "" ); //$NON-NLS-1$ //$NON-NLS-2$

    // populate the flash html template

    Properties props = new Properties();
    props.setProperty( "chartId", chartId ); //$NON-NLS-1$
    props.setProperty( "dataFunction", "getData" + chartId ); //$NON-NLS-1$ //$NON-NLS-2$
    props.setProperty( "chart-width", chartWidth ); //$NON-NLS-1$
    props.setProperty( "chart-height", chartHeight ); //$NON-NLS-1$
    props.setProperty( "ofc-url", ofcURL ); //$NON-NLS-1$
    props.setProperty( "ofc-libname", ofclibname ); //$NON-NLS-1$
    props.setProperty( "chartJson", chartJson.replaceAll( "\"", "\\\\\"" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    String flashContent = TemplateUtil.applyTemplate( getFlashFragment(), props, null );

    // set output value

    log.debug( "html_fragment=" + flashContent ); //$NON-NLS-1$

    if ( this.isDefinedOutput( "html_fragment" ) ) { //$NON-NLS-1$
      this.setOutputValue( "html_fragment", flashContent ); //$NON-NLS-1$
    }
    if ( this.isDefinedOutput( "image-tag" ) ) { //$NON-NLS-1$
      this.setOutputValue( "image-tag", flashContent ); //$NON-NLS-1$
    }

    return true;
  }

  protected String generateChartJson( IPentahoResultSet data, Node chartNode, boolean byRow ) {
    return PentahoOFC4JChartHelper.generateChartJson( chartNode, data, byRow, log );
  }

  protected String getFlashFragment() {
    return flashFragment;
  }

  public void done() {
    // nothing to do here
  }

  public boolean init() {
    // nothing to do here
    return true;
  }

  public boolean validateSystemSettings() {
    return true;
  }

  public Log getLogger() {
    return log;
  }

  ///////////////////////////////////////////////////////////////////////////////////////
  // overrides for testability
  @Override
  protected Object getInputValue( String inputName ) {
    return super.getInputValue( inputName );
  }

  @Override
  protected String getInputStringValue( String inputName ) {
    return super.getInputStringValue( inputName );
  }

  @Override
  protected boolean isDefinedInput( String inputName ) {
    return super.isDefinedInput( inputName );
  }

  @Override
  protected boolean isDefinedResource( String resourceName ) {
    return super.isDefinedResource( resourceName );
  }

  @Override
  public void inputMissingError( String paramName ) {
    super.inputMissingError( paramName );
  }
  ///////////////////////////////////////////////////////////////////////////////////////

}
