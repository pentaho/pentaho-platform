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
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.runtime.TemplateUtil;
import org.pentaho.platform.uifoundation.component.xml.XmlComponent;
import org.pentaho.platform.uifoundation.messages.Messages;
import org.pentaho.platform.util.messages.LocaleHelper;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// ESCA-JAVA0100:
// ESCA-JAVA0136:
// ESCA-JAVA0054:
public abstract class AbstractChartComponent extends XmlComponent {

  private static final long serialVersionUID = -3700747149855352376L;

  /**
   * XML Node for the chart configuration
   */
  public static final String CHART_NODE_NAME = "chart"; //$NON-NLS-1$

  /**
   * XML node for the URL Template
   */
  public static final String URLTEMPLATE_NODE_NAME = "url-template"; //$NON-NLS-1$ 

  /**
   * XML node for the series name
   */
  public static final String PARAM2_NODE_NAME = "series-name"; //$NON-NLS-1$

  /**
   * Index into array of the filename
   */
  public static final int FILENAME_INDEX = 0;

  /**
   * Index into array of the filename without the extension
   */
  public static final int FILENAME_WITHOUT_EXTENSION_INDEX = 1;

  protected String definitionPath;

  protected int width = -1;

  protected int height = -1;

  protected String title;

  protected Object values;

  protected boolean byRow = true;

  protected String actionPath;

  protected String actionOutput;

  protected String instanceId = null;

  protected IRuntimeContext context;

  protected String urlTemplate = null;

  protected List<String> outerParamNames = new ArrayList<String>();

  protected String paramName; // Assumes 1 parameter subclasses may need more

  protected static int chartCount = 0;

  protected static Log logger = null;

  protected AbstractChartComponent( final String definitionPath, final int width, final int height,
      final IPentahoUrlFactory urlFactory, final List messages ) {
    this( urlFactory, messages );
    this.definitionPath = definitionPath;
    this.width = width;
    this.height = height;
    setSourcePath( definitionPath );
  }

  /**
   * @param definitionPath
   * @param urlFactory
   * @param messages
   */
  protected AbstractChartComponent( final String definitionPath, final IPentahoUrlFactory urlFactory,
      final ArrayList messages ) {
    this( urlFactory, messages );
    this.definitionPath = definitionPath;
    setSourcePath( definitionPath );
  }

  protected AbstractChartComponent( final IPentahoUrlFactory urlFactory, final List messages ) {
    super( urlFactory, messages, null );
    setXsl( "text/html", "Chart.xsl" ); //$NON-NLS-1$ //$NON-NLS-2$
    AbstractChartComponent.logger = LogFactory.getLog( this.getClass() );
  }

  /**
   * @param chartDefinition
   *          String that represents a file in the solution to create the chart from.
   * @return
   */
  public abstract boolean setDataAction( String chartDefinition );

  /**
   * Sets the action to be executed to get the data for the pies
   * 
   * @param solution
   * @param actionPath
   * @param actionName
   * @param actionOutput
   */
  public void setDataAction( final String actionPath, final String actionOutput ) {
    this.actionPath = actionPath;
    this.actionOutput = actionOutput;
  }

  /**
   * Gets a IPentahoResultSet from the action output
   * 
   * @return IPentahoResultSet
   */
  public IPentahoResultSet getActionData() {
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
    context = solutionEngine.execute( actionPath, processId, false, true, instanceId, false, //$NON-NLS-1$ //$NON-NLS-2$
        parameterProviders, outputHandler, null, urlFactory, messages );

    if ( context == null ) {
      // this went badly wrong
      return null;
    }

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
      for ( Object objAp : context.getOutputNames() ) {
        IActionParameter output = (IActionParameter) objAp;
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

  @Override
  public Log getLogger() {
    return AbstractChartComponent.logger;
  }

  /**
   * @return String that represents the file path to a temporary file
   */
  protected String[] createTempFile() {
    // create temporary file names
    String solutionDir = "system/tmp/"; //$NON-NLS-1$
    String fileNamePrefix = "tmp_chart_"; //$NON-NLS-1$
    String extension = ".png"; //$NON-NLS-1$
    String fileName = null;
    String filePathWithoutExtension = null;
    try {
      File file = PentahoSystem.getApplicationContext().createTempFile( getSession(), fileNamePrefix, extension, true );
      fileName = file.getName();
      filePathWithoutExtension = solutionDir + fileName.substring( 0, fileName.indexOf( '.' ) );
    } catch ( IOException e ) {
      getLogger().error(
          Messages.getInstance().getErrorString( "AbstractChartComponent.ERROR_0001_CANT_CREATE_TEMP_CHART" ), e ); //$NON-NLS-1$
    }
    String[] value = new String[2];
    value[AbstractChartComponent.FILENAME_INDEX] = fileName;
    value[AbstractChartComponent.FILENAME_WITHOUT_EXTENSION_INDEX] = filePathWithoutExtension;

    return value;
  }

  protected void applyOuterURLTemplateParam() {
    if ( outerParamNames == null ) {
      return;
    }
    for ( String outerParamName : outerParamNames ) {
      Object value = null;
      if ( ( context != null ) && context.getInputNames().contains( outerParamName ) ) {
        value = context.getInputParameterValue( outerParamName );
      }
      if ( value == null ) {
        return;
      }
      try {
        if ( value.getClass().isArray() ) {
          if ( Array.getLength( value ) > 0 ) {
            String[] encodedVals = new String[Array.getLength( value )];
            for ( int i = 0; i < Array.getLength( value ); ++i ) // ESCA-JAVA0049:
            {
              encodedVals[i] = URLEncoder.encode( Array.get( value, i ).toString(), LocaleHelper.getSystemEncoding() );
            }
            // TODO Sleeze Alert!!! This is a temporary hack for making the
            // URLs generated support multiple selections. A JIRA case PLATFORM-393
            // has been generated to address this issue.
            //
            // For now, applyTemplate looks for an "&" or "?" preceding and following the param, uses all
            // the characters between them as the template and repeats it once for each param value
            // separating them with '&'
            urlTemplate = TemplateUtil.applyTemplate( urlTemplate, outerParamName, encodedVals );
          }
        } else {
          String encodedVal = URLEncoder.encode( value.toString(), LocaleHelper.getSystemEncoding() );
          urlTemplate = TemplateUtil.applyTemplate( urlTemplate, outerParamName, encodedVal );
        }

        //encodedVal = URLEncoder.encode(stringVal, LocaleHelper.getSystemEncoding()); //$NON-NLS-1$
      } catch ( UnsupportedEncodingException e ) {
        getLogger().error(
            Messages.getInstance().getErrorString( "AbstractChartComponent.ERROR_0002_URL_ENCODE_FAILED" ), e ); //$NON-NLS-1$
      }
    }
  }

  /**
   * 
   */
  public void dispose() {
    if ( context != null ) {
      context.dispose();
    }
  }

  /**
   * @return Returns the actionOutput.
   */
  public String getActionOutput() {
    return actionOutput;
  }

  /**
   * @param actionOutput
   *          The actionOutput to set.
   */
  public void setActionOutput( final String actionOutput ) {
    this.actionOutput = actionOutput;
  }

  /**
   * @return Returns the actionPath.
   */
  public String getActionPath() {
    return actionPath;
  }

  /**
   * @param actionPath
   *          The actionPath to set.
   */
  public void setActionPath( final String actionPath ) {
    this.actionPath = actionPath;
  }

  /**
   * @return Returns the context.
   */
  public IRuntimeContext getContext() {
    return context;
  }

  /**
   * @param context
   *          The context to set.
   */
  public void setContext( final IRuntimeContext context ) {
    this.context = context;
  }

  /**
   * @return Returns the definitionPath.
   */
  public String getDefinitionPath() {
    return definitionPath;
  }

  /**
   * @param definitionPath
   *          The definitionPath to set.
   */
  public void setDefinitionPath( final String definitionPath ) {
    this.definitionPath = definitionPath;
  }

  /**
   * @return Returns the height.
   */
  public int getHeight() {
    return height;
  }

  /**
   * @param height
   *          The height to set.
   */
  public void setHeight( final int height ) {
    this.height = height;
  }

  /**
   * @return Returns the instanceId.
   */
  public String getInstanceId() {
    return instanceId;
  }

  /**
   * @param instanceId
   *          The instanceId to set.
   */
  public void setInstanceId( final String instanceId ) {
    this.instanceId = instanceId;
  }

  /**
   * @return Returns the title.
   */
  public String getTitle() {
    return title;
  }

  /**
   * @param title
   *          The title to set.
   */
  public void setTitle( final String title ) {
    this.title = title;
  }

  /**
   * @return Returns the urlTemplate.
   */
  public String getUrlTemplate() {
    return urlTemplate;
  }

  /**
   * @param urlTemplate
   *          The urlTemplate to set.
   */
  public void setUrlTemplate( final String urlTemplate ) {
    this.urlTemplate = urlTemplate;
  }

  /**
   * @return Returns the values.
   */
  public Object getValues() {
    return values;
  }

  /**
   * @param values
   *          The values to set.
   */
  public void setValues( final Object values ) {
    this.values = values;
  }

  /**
   * @return Returns the width.
   */
  public int getWidth() {
    return width;
  }

  /**
   * @param width
   *          The width to set.
   */
  public void setWidth( final int width ) {
    this.width = width;
  }

  /**
   * @param logger
   *          The logger to set.
   */
  public void setLogger( final Log logger ) {
    AbstractChartComponent.logger = logger;
  }

  /**
   * @return Returns the byRow.
   */
  public boolean isByRow() {
    return byRow;
  }

  /**
   * @param byRow
   *          The byRow to set.
   */
  public void setByRow( final boolean byRow ) {
    this.byRow = byRow;
  }

  /**
   * @return Returns the paramName.
   */
  public String getParamName() {
    return paramName;
  }

  /**
   * @param paramName
   *          The paramName to set.
   */
  public void setParamName( final String paramName ) {
    this.paramName = paramName;
  }

  /**
   * @return Returns the outerParamNames. private List getOuterParamNames() { return outerParamNames; }
   */

  /**
   * @param outerParamName
   *          The outerParamNames name to add to the outParamNames list.
   */
  public void addOuterParamName( final String outerParamName ) {
    outerParamNames.add( outerParamName );
  }

}
