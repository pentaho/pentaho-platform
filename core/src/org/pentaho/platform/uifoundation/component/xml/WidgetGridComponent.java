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

package org.pentaho.platform.uifoundation.component.xml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.pentaho.commons.connection.IPentahoMetaData;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IPentahoRequestContext;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.ActionInfo;
import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.SolutionURIResolver;
import org.pentaho.platform.engine.services.actionsequence.ActionSequenceResource;
import org.pentaho.platform.engine.services.runtime.TemplateUtil;
import org.pentaho.platform.uifoundation.chart.DialWidgetDefinition;
import org.pentaho.platform.uifoundation.chart.JFreeChartEngine;
import org.pentaho.platform.uifoundation.chart.WidgetDefinition;
import org.pentaho.platform.uifoundation.messages.Messages;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class WidgetGridComponent extends XmlComponent {

  /**
   * 
   */
  private static final long serialVersionUID = -3952161695550067971L;

  private String definitionPath;

  private int widgetWidth;

  private int widgetHeight;

  private String solution = null;

  private String actionPath = null;

  private String actionName = null;

  private String valueItem = null;

  private String nameItem = null;

  private int columns = 0;

  private String instanceId = null;

  private String actionOutput = null;

  private String urlTemplate = null;

  private String style = null;

  private IRuntimeContext context;

  private static final Log logger = LogFactory.getLog( WidgetGridComponent.class );

  @Override
  public Log getLogger() {
    return WidgetGridComponent.logger;
  }

  /**
   * Creates a WidgetGrid
   * <p>
   * After creating an instance of this class <CODE>validate</CODE> should be called.
   * 
   * @param type
   *          The type of the widget, currently only TYPE_DIAL is supported
   * @param definitionPath
   *          The path and name of the XML definition of the dial
   * @param widgetWidth
   *          The width of the image to be created
   * @param widgetHeight
   *          The height of the image to be created
   * @param urlFactory
   *          The urlFactory for the content
   * @param messages
   *          The messages list for any logger messages
   */
  public WidgetGridComponent( final String definitionPath, final IPentahoUrlFactory urlFactory, final List messages ) {
    super( urlFactory, messages, null );
    this.definitionPath = definitionPath;
    ActionInfo info = ActionInfo.parseActionString( definitionPath );
    if ( info != null ) {
      setSourcePath( info.getSolutionName() + File.separator + info.getPath() );
    }
    setXsl( "text/html", "DialWidget.xsl" ); //$NON-NLS-1$ //$NON-NLS-2$    
  }

  /**
   * Sets the width (in pixels) of the widget images that will be created
   * 
   * @param widgetWidth
   */
  public void setWidgetWidth( final int widgetWidth ) {
    this.widgetWidth = widgetWidth;
  }

  /**
   * Sets the height (in pixels) of the widget images that will be created
   * 
   * @param widgetHeight
   */
  public void setWidgetHeight( final int widgetHeight ) {
    this.widgetHeight = widgetHeight;
  }

  /**
   * Sets the number of widgets that will be dispayed in a row before another row of widgets is created
   * 
   * @param instanceId
   */
  public void setColumns( final int columns ) {
    this.columns = columns;
  }

  /**
   * Sets the instance id for this execution
   * 
   * @param instanceId
   *          The instance id of the parent object or process
   */
  public void setInstanceId( final String instanceId ) {
    this.instanceId = instanceId;
  }

  public boolean setDataAction( final String widgetGridDataDefinition ) {
    try {
      Document dataActionDocument = null;
      try {
        org.dom4j.io.SAXReader reader = new org.dom4j.io.SAXReader();
        reader.setEntityResolver( new SolutionURIResolver() );
        dataActionDocument =
            reader.read( ActionSequenceResource.getInputStream( widgetGridDataDefinition, LocaleHelper.getLocale() ) );
      } catch ( Throwable t ) {
        return false;
      }
      Node dataNode = dataActionDocument.selectSingleNode( "widgetgrid/data" ); //$NON-NLS-1$
      solution = XmlDom4JHelper.getNodeText( "data-solution", dataNode ); //$NON-NLS-1$
      actionPath = XmlDom4JHelper.getNodeText( "data-path", dataNode ); //$NON-NLS-1$
      actionName = XmlDom4JHelper.getNodeText( "data-action", dataNode ); //$NON-NLS-1$
      actionOutput = XmlDom4JHelper.getNodeText( "data-output", dataNode ); //$NON-NLS-1$
      valueItem = XmlDom4JHelper.getNodeText( "data-value", dataNode ); //$NON-NLS-1$
      nameItem = XmlDom4JHelper.getNodeText( "data-name", dataNode ); //$NON-NLS-1$
      widgetWidth = (int) XmlDom4JHelper.getNodeText( "widgetgrid/width", dataActionDocument, 125 ); //$NON-NLS-1$
      widgetHeight = (int) XmlDom4JHelper.getNodeText( "widgetgrid/height", dataActionDocument, 125 ); //$NON-NLS-1$
      columns = (int) XmlDom4JHelper.getNodeText( "widgetgrid/columns", dataActionDocument, 2 ); //$NON-NLS-1$
      style = XmlDom4JHelper.getNodeText( "widgetgrid/style", dataActionDocument ); //$NON-NLS-1$
    } catch ( Exception e ) {
      error( Messages.getInstance().getErrorString(
          "WidgetGrid.ERROR_0003_DEFINITION_NOT_VALID", widgetGridDataDefinition ), e ); //$NON-NLS-1$
      return false;
    }
    return true;
  }

  /**
   * Sets the action to be executed to get the data for the widgets
   * 
   * @param solution
   * @param actionPath
   * @param actionName
   * @param actionOutput
   * @param nameItem
   * @param valueItem
   */
  public void setDataAction( final String solution, final String actionPath, final String actionName,
      final String actionOutput, final String nameItem, final String valueItem ) {
    this.solution = solution;
    this.actionPath = actionPath;
    this.actionName = actionName;
    this.actionOutput = actionOutput;
    this.valueItem = valueItem;
    this.nameItem = nameItem;
  }

  public void setDrillUrlTemplate( final String urlTemplate ) {
    this.urlTemplate = urlTemplate;
  }

  @Override
  public boolean validate() {
    return true;
  }

  @Override
  public Document getXmlContent() {

    // get the data to populate the widgets
    IPentahoResultSet resultSet = null;
    if ( solution != null ) {
      resultSet = getActionData();
    }

    // create the widget to use
    // load the XML document that defines the dial
    Document dialDefinition = null;
    try {
      org.dom4j.io.SAXReader reader = new org.dom4j.io.SAXReader();
      reader.setEntityResolver( new SolutionURIResolver() );
      dialDefinition = reader.read( ActionSequenceResource.getInputStream( definitionPath, LocaleHelper.getLocale() ) );
    } catch ( Throwable t ) {
      // XML document can't be read. We'll just return a null document.
    }

    // create a dial definition from the XML definition
    WidgetDefinition widgetDefinition =
        new DialWidgetDefinition( dialDefinition, 0, widgetWidth, widgetHeight, getSession() );

    return createDials( resultSet, widgetDefinition );
  }

  protected Document createDials( final IPentahoResultSet resultSet, final WidgetDefinition widgetDefinition ) {

    if ( resultSet == null ) {
      error( Messages.getInstance().getErrorString( "WidgetGrid.ERROR_0001_NO_RESULTS_FROM_ACTION" ) ); //$NON-NLS-1$
      return null;
    }

    if ( valueItem == null ) {
      error( Messages.getInstance().getErrorString( "WidgetGrid.ERROR_0002_NO_VALUE_ITEM" ) ); //$NON-NLS-1$
    }

    // Create a document that describes the result
    Document result = DocumentHelper.createDocument();
    IPentahoRequestContext requestContext = PentahoRequestContextHolder.getRequestContext();
    setXslProperty( "baseUrl", requestContext.getContextPath() ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    setXslProperty( "fullyQualifiedServerUrl", PentahoSystem.getApplicationContext().getFullyQualifiedServerURL() ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    Element root = result.addElement( "widgets" ); //$NON-NLS-1$

    IPentahoMetaData metaData = resultSet.getMetaData();
    // TODO support multiple column headers / row headers
    // TODO support an iteration across columns for a given row

    // find the column that we have been told to you
    Object[][] columnHeaders = metaData.getColumnHeaders();
    int nameColumnNo = -1;
    int valueColumnNo = -1;
    for ( int idx = 0; idx < columnHeaders[0].length; idx++ ) {
      if ( columnHeaders[0][idx].toString().equalsIgnoreCase( nameItem ) ) {
        nameColumnNo = idx;
      }
      if ( columnHeaders[0][idx].toString().equalsIgnoreCase( valueItem ) ) {
        valueColumnNo = idx;
      }
    }

    if ( nameColumnNo == -1 ) {
      // we did not find the specified name column
      error( Messages.getInstance().getErrorString( "WidgetGrid.ERROR_0004_NAME_COLUMN_MISSING", nameItem ) ); //$NON-NLS-1$
      return null;
    }

    if ( valueColumnNo == -1 ) {
      // we did not find the specified name column
      error( Messages.getInstance().getErrorString( "WidgetGrid.ERROR_0005_VALUE_COLUMN_MISSING", valueItem ) ); //$NON-NLS-1$
      return null;
    }

    double value;
    String name;
    Object[] row = resultSet.next();
    while ( row != null ) {
      name = row[nameColumnNo].toString();
      try {
        value = Double.parseDouble( row[valueColumnNo].toString() );
        createDial( value, name, root, widgetDefinition );
      } catch ( Exception e ) {
        //ignore
      }

      row = resultSet.next();
    }
    setXslProperty( "urlTarget", "pentaho_popup" ); //$NON-NLS-1$ //$NON-NLS-2$
    setXslProperty( "columns", Integer.toString( columns ) ); //$NON-NLS-1$
    if ( style != null ) {
      setXslProperty( "style", style ); //$NON-NLS-1$
    }
    return result;
  }

  protected void createDial( final double value, final String name, final Element root,
      final WidgetDefinition widgetDefinition ) {

    widgetDefinition.setValue( new Double( value ) );

    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter( stringWriter );

    // TODO get units from somewhere
    String units = ""; //$NON-NLS-1$
    String dialName = ""; //$NON-NLS-1$
    // create temporary file names
    String solutionDir = "system/tmp/"; //$NON-NLS-1$
    String fileNamePrefix = "tmp_pie_"; //$NON-NLS-1$
    String extension = ".png"; //$NON-NLS-1$
    String fileName = null;
    String filePathWithoutExtension = null;
    try {
      File file = PentahoSystem.getApplicationContext().createTempFile( getSession(), fileNamePrefix, extension, true );
      fileName = file.getName();
      filePathWithoutExtension = solutionDir + fileName.substring( 0, fileName.indexOf( '.' ) );
    } catch ( IOException e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    JFreeChartEngine.saveChart( widgetDefinition, dialName, units, filePathWithoutExtension, widgetWidth, widgetHeight,
        JFreeChartEngine.OUTPUT_PNG, printWriter, this );

    Element widgetNode = root.addElement( "widget" ); //$NON-NLS-1$

    widgetNode.addElement( "title" ).setText( name ); //$NON-NLS-1$
    widgetNode.addElement( "units" ).setText( units ); //$NON-NLS-1$
    widgetNode.addElement( "width" ).setText( Integer.toString( widgetWidth ) ); //$NON-NLS-1$
    widgetNode.addElement( "height" ).setText( Integer.toString( widgetHeight ) ); //$NON-NLS-1$
    Element valueNode = widgetNode.addElement( "value" ); //$NON-NLS-1$
    valueNode.setText( Double.toString( value ) );
    valueNode.addAttribute( "in-image", Boolean.toString( widgetDefinition.getValueFont() != null ) ); //$NON-NLS-1$
    root.addElement( "image" ).setText( fileName ); //$NON-NLS-1$
    widgetNode.addElement( "image" ).setText( fileName ); //$NON-NLS-1$

    // apply the current data item name to the URL template
    String drillUrl = TemplateUtil.applyTemplate( urlTemplate, nameItem, name );

    // now apply any parameters to the URL template
    drillUrl = TemplateUtil.applyTemplate( drillUrl, context );

    widgetNode.addElement( "urlDrill" ).setText( drillUrl ); //$NON-NLS-1$

  }

  public void dispose() {
    if ( context != null ) {
      context.dispose();
    }
  }

  protected IPentahoResultSet getActionData() {
    // create an instance of the solution engine to execute the specified
    // action

    ISolutionEngine solutionEngine = PentahoSystem.get( ISolutionEngine.class, getSession() );
    solutionEngine.setLoggingLevel( ILogger.DEBUG );
    solutionEngine.init( getSession() );

    HashMap parameterProviders = getParameterProviders();

    OutputStream outputStream = null;
    SimpleOutputHandler outputHandler = null;
    outputHandler = new SimpleOutputHandler( outputStream, false );

    ArrayList messages = new ArrayList();
    String processId = this.getClass().getName();

    String actionSeqPath = ActionInfo.buildSolutionPath( solution, actionPath, actionName );

    context =
        solutionEngine.execute( actionSeqPath, processId, false, true, instanceId, false, parameterProviders,
            outputHandler, null, urlFactory, messages );

    if ( actionOutput != null ) {
      if ( context.getOutputNames().contains( actionOutput ) ) {
        IActionParameter output = context.getOutputParameter( actionOutput );
        IPentahoResultSet results = output.getValueAsResultSet();
        if ( results != null ) {
          results = results.memoryCopy();
        }
        return results;
      } else {
        // this is an error
        return null;
      }
    } else {
      // return the first list that we find...
      Iterator it = context.getOutputNames().iterator();
      while ( it.hasNext() ) {
        IActionParameter output = (IActionParameter) it.next();
        if ( output.getType().equalsIgnoreCase( IActionParameter.TYPE_RESULT_SET ) ) {
          IPentahoResultSet results = output.getValueAsResultSet();
          if ( results != null ) {
            results = results.memoryCopy();
          }
          return results;
        }
      }
    }
    return null;
  }

}
