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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.engine.IPentahoRequestContext;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.engine.core.solution.ActionInfo;
import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.SolutionURIResolver;
import org.pentaho.platform.engine.services.actionsequence.ActionSequenceResource;
import org.pentaho.platform.uifoundation.component.xml.XmlComponent;
import org.pentaho.platform.uifoundation.messages.Messages;
import org.pentaho.platform.util.messages.LocaleHelper;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

/**
 * This class is a Pentaho user interface component.
 * <p/>
 * It generates dial images that can be embedded into JSPs, portals or other HTML supporting user interface.
 * <p/>
 * <ol>
 * <li>The creating object sets the width, height, the type of the dial, and the name of the dial.xml file that
 * contains the definition of the dial.</li>
 * <li>This class creates an instance of a DialWidgetDefinition using the specified XML definition file. The XML
 * files are located in the solution folders and have .dial.xml extenstions. The dial XML files define the
 * attributes that define how the dial looks.</li>
 * <li>It uses the JFreeChartEngine to create an image of the dial.</li>
 * <li>Once the image has been created this class creates an XML document describing the dial
 * <li>It uses an XSL to tranforms the XML description into HTML.
 * </ol>
 * This is an example image
 */
public class DashboardWidgetComponent extends XmlComponent {

  /**
   * 
   */
  private static final long serialVersionUID = 3060729271469984040L;

  public static final int TYPE_DIAL = 1;

  public static final int TYPE_THERMOMETER = 2;

  private static final Log logger = LogFactory.getLog( DashboardWidgetComponent.class );

  private int type;

  private double value = DashboardWidgetComponent.TYPE_DIAL;

  private String definitionPath;

  private String title = ""; //$NON-NLS-1$

  private String units = ""; //$NON-NLS-1$

  private int width;

  private int height;

  /**
   * Creates a DashboardWidgetComponent.
   * <p>
   * After creating an instance of this class <CODE>validate</CODE> should be called.
   * 
   * @param type
   *          The type of the widget, currently only TYPE_DIAL is supported
   * @param definitionPath
   *          The path and name of the XML definition of the dial
   * @param width
   *          The width of the image to be created
   * @param height
   *          The height of the image to be created
   * @param urlFactory
   *          The urlFactory for the content
   * @param messages
   *          The messages list for any logger messages
   */
  public DashboardWidgetComponent( final int type, final String definitionPath, final int width, final int height,
      final IPentahoUrlFactory urlFactory, final List messages ) {
    super( urlFactory, messages, null );
    this.type = type;
    this.definitionPath = definitionPath;
    this.width = width;
    this.height = height;
    ActionInfo info = ActionInfo.parseActionString( definitionPath );
    if ( info != null ) {
      setSourcePath( info.getSolutionName() + File.separator + info.getPath() );
    }
    // Set the XSL file to be used to generate the HTML
    setXsl( "text/html", "DialWidget.xsl" ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * Sets the value to be displayed by the dial.
   * 
   * @param value
   *          The dial value
   */
  public void setValue( final double value ) {
    this.value = value;
  }

  /**
   * Sets the title for the dial
   * 
   * @param title
   *          The title of the dial
   */
  public void setTitle( final String title ) {
    this.title = title;
  }

  /**
   * Sets the unit for the dial value
   * 
   * @param units
   *          The dial units
   */
  public void setUnits( final String units ) {
    this.units = units;
  }

  /**
   * Gets the logger for his component.
   * 
   * @return logger This component's logger
   */
  @Override
  public Log getLogger() {
    return DashboardWidgetComponent.logger;
  }

  /**
   * Validate that this component can generate the requested dial
   */
  @Override
  public boolean validate() {
    // TODO
    return true;
  }

  /**
   * Create a dial image.
   * <ul>
   * <li>Load the specified XML document describing the dial definition</li>
   * <li>Create a dial definition object from the XML definition</li>
   * <li>Use the JFreeChartEngine to create a dial image</li>
   * <li>Create an XML document describing the dial</li>
   * <li>Return the XML document</li>
   * </ul>
   * 
   * @return The XML document describing this dial
   */
  @Override
  public Document getXmlContent() {

    WidgetDefinition widget = null;
    if ( type == DashboardWidgetComponent.TYPE_DIAL ) {

      // load the XML document that defines the dial
      IActionSequenceResource resource =
          new ActionSequenceResource( title, IActionSequenceResource.SOLUTION_FILE_RESOURCE, "text/xml", //$NON-NLS-1$
              definitionPath );
      Document dialDefinition = null;
      try {
        org.dom4j.io.SAXReader reader = new org.dom4j.io.SAXReader();
        reader.setEntityResolver( new SolutionURIResolver() );
        dialDefinition =
            reader.read( resource.getInputStream( RepositoryFilePermission.READ, LocaleHelper.getLocale() ) );
      } catch ( Throwable t ) {
        // XML document can't be read. We'll just return a null document.
      }

      if ( dialDefinition == null ) {
        error( Messages.getInstance().getErrorString( "Widget.ERROR_0002_INVALID_RESOURCE", definitionPath ) ); //$NON-NLS-1$
        return null;
      }
      // create a dial definition from the XML definition
      widget = new DialWidgetDefinition( dialDefinition, 0, width, height, getSession() );

      if ( widget != null ) {
        // set the value to be displayed on the dial
        widget.setValue( new Double( value ) );

      }
    }
    /*
     * else if( type == TYPE_THERMOMETER ) { // load the XML document that defines the thermometer
     * 
     * ActionResource resource = new ActionResource( title, IActionResource.SOLUTION_FILE_RESOURCE, "text/xml",
     * //$NON-NLS-1$ PentahoSystem.getApplicationContext().getSolutionPath( definitionPath ) ); //$NON-NLS-1$
     * Document thermometerDefinition = null; try { thermometerDefinition = PentahoSystem.getResourceAsDocument(
     * resource ); } catch (IOException e) {} // create a dial definition from the XML definition widget =
     * createThermometer( thermometerDefinition );
     * 
     * if( widget != null ) { // set the value to be displayed on the dial widget.setValue( new Double(value) ); //
     * Set the XSL file to be used to generate the HTML setXsl( "text/html", "DialWidget.xsl" ); //$NON-NLS-1$
     * //$NON-NLS-2$ } else { error( Messages.getInstance().getString("Widget.ERROR_0001_COULD_NOT_CREATE") );
     * //$NON-NLS-1$ return null; } }
     */
    if ( widget == null ) {
      error( Messages.getInstance().getString( "Widget.ERROR_0001_COULD_NOT_CREATE" ) ); //$NON-NLS-1$
      return null;
    }
    // create an image for the dial using the JFreeChart engine
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter( stringWriter );
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
    String dialTitle = ""; //$NON-NLS-1$
    JFreeChartEngine.saveChart( widget, dialTitle, units, filePathWithoutExtension, width, height,
        JFreeChartEngine.OUTPUT_PNG, printWriter, this );

    // Create a document that describes the result
    Document result = DocumentHelper.createDocument();
    IPentahoRequestContext requestContext = PentahoRequestContextHolder.getRequestContext();
    setXslProperty( "baseUrl", requestContext.getContextPath() ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    setXslProperty( "fullyQualifiedServerUrl", PentahoSystem.getApplicationContext().getFullyQualifiedServerURL() ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    Element root = result.addElement( "widget" ); //$NON-NLS-1$
    root.addElement( "title" ).setText( title ); //$NON-NLS-1$
    root.addElement( "units" ).setText( units ); //$NON-NLS-1$
    root.addElement( "width" ).setText( Integer.toString( width ) ); //$NON-NLS-1$
    root.addElement( "height" ).setText( Integer.toString( height ) ); //$NON-NLS-1$
    Element valueNode = root.addElement( "value" ); //$NON-NLS-1$
    valueNode.setText( Double.toString( value ) );
    valueNode.addAttribute( "in-image", Boolean.toString( widget.getValueFont() != null ) ); //$NON-NLS-1$
    root.addElement( "image" ).setText( fileName ); //$NON-NLS-1$
    return result;

  }

  public void dispose() {

  }

}
