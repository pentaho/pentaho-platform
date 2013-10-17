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

package org.pentaho.platform.plugin.action.jfreereport.components;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.actionsequence.dom.IActionResource;
import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.engine.IPentahoRequestContext;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.jfreereport.AbstractJFreeReportComponent;
import org.pentaho.platform.plugin.action.jfreereport.helper.PentahoResourceLoader;
import org.pentaho.platform.plugin.action.jfreereport.helper.ReportUtils;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.modules.parser.base.ReportGenerator;
import org.pentaho.reporting.libraries.resourceloader.FactoryParameterKey;
import org.pentaho.reporting.libraries.resourceloader.ResourceException;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import org.pentaho.util.messages.LocaleHelper;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A JFreeReport run contains at least three steps. Step 1: Parse the report definition. Step 2: Grab some data. Step 3:
 * Spit out some content. Alternativly, show the print-preview.
 * <p/>
 * This class loads or parses the report definition.
 * 
 * 
 * @deprecated This code has known bugs and it is highly recommended that it not be used by any sane person
 * @author Thomas Morgner
 */
@Deprecated
public class JFreeReportLoadComponent extends AbstractJFreeReportComponent {
  private static final long serialVersionUID = -2240691437049710246L;

  public JFreeReportLoadComponent() {
  }

  @Override
  protected boolean validateAction() {
    if ( isDefinedResource( AbstractJFreeReportComponent.REPORTGENERATEDEFN_REPORTDEFN ) ) {
      return true;
    }

    if ( isDefinedInput( AbstractJFreeReportComponent.REPORTGENERATEDEFN_REPORTDEFN ) ) {
      IActionParameter o = getInputParameter( AbstractJFreeReportComponent.REPORTGENERATEDEFN_REPORTDEFN );
      if ( ( o != null ) && ( o.getValue() instanceof String ) ) {
        return true;
      }
      return false;
    }

    // Handle late-bind of report resource name
    if ( isDefinedInput( AbstractJFreeReportComponent.REPORTLOAD_RESOURCENAME ) ) {
      if ( isDefinedResource( getInputStringValue( AbstractJFreeReportComponent.REPORTLOAD_RESOURCENAME ) ) ) {
        return true;
      } else {
        error( Messages.getInstance().getErrorString( "JFreeReport.ERROR_0004_REPORT_DEFINITION_UNREADABLE" ) ); //$NON-NLS-1$
        return false;
      }
    }

    if ( isDefinedResource( AbstractJFreeReportComponent.DATACOMPONENT_JARINPUT ) ) {
      if ( !isDefinedInput( AbstractJFreeReportComponent.REPORTLOAD_REPORTLOC ) ) {
        error( Messages.getInstance().getErrorString( "JFreeReport.ERROR_0011_REPORT_LOCATION_MISSING" ) ); //$NON-NLS-1$
        return false;
      }

      final IActionSequenceResource resource = getResource( AbstractJFreeReportComponent.DATACOMPONENT_JARINPUT );
      final InputStream in;
      in = resource.getInputStream( RepositoryFilePermission.READ, LocaleHelper.getLocale() );

      try {
        // not being able to read a single char is definitly a big boo ..
        if ( in.read() == -1 ) {
          error( Messages.getInstance().getErrorString( "JFreeReport.ERROR_0009_REPORT_JAR_UNREADABLE" ) ); //$NON-NLS-1$
          return false;
        }
      } catch ( Exception e ) {
        error( Messages.getInstance().getErrorString( "JFreeReport.ERROR_0009_REPORT_JAR_UNREADABLE" ) ); //$NON-NLS-1$
        return false;
      }

      if ( !isDefinedInput( AbstractJFreeReportComponent.REPORTLOAD_REPORTLOC ) ) {
        error( Messages.getInstance().getErrorString( "JFreeReport.ERROR_0012_CLASS_LOCATION_MISSING" ) ); //$NON-NLS-1$
        return false;
      }
      return true;
    }
    return false;
  }

  @Override
  protected boolean validateSystemSettings() {
    return true;
  }

  @Override
  public void done() {

  }

  private MasterReport getReportFromResource() throws ResourceException, IOException {
    MasterReport report = null;
    if ( isDefinedResource( AbstractJFreeReportComponent.REPORTGENERATEDEFN_REPORTDEFN ) ) {
      final IActionSequenceResource resource = getResource(
        AbstractJFreeReportComponent.REPORTGENERATEDEFN_REPORTDEFN );

      if ( resource.getSourceType() == IActionResource.XML ) {
        String repDef = resource.getAddress();
        ReportGenerator generator = ReportGenerator.getInstance();

        report =
            generator.parseReport( new InputSource( new ByteArrayInputStream( repDef.getBytes() ) ),
                getDefinedResourceURL( null ) );
      }
      report = parseReport( resource );
    } else if ( isDefinedInput( AbstractJFreeReportComponent.REPORTLOAD_RESOURCENAME ) ) {
      final String resName = getInputStringValue( AbstractJFreeReportComponent.REPORTLOAD_RESOURCENAME );
      if ( isDefinedResource( resName ) ) {
        final IActionSequenceResource resource = getResource( resName );
        report = parseReport( resource );
      }
    }
    return report;
  }

  private MasterReport getReportFromInputParam() throws ResourceException, UnsupportedEncodingException, IOException {
    MasterReport report = null;

    if ( isDefinedInput( AbstractJFreeReportComponent.REPORTGENERATEDEFN_REPORTDEFN ) ) {
      IActionParameter o = getInputParameter( AbstractJFreeReportComponent.REPORTGENERATEDEFN_REPORTDEFN );
      if ( o != null ) {
        String repDef = o.getStringValue();
        ReportGenerator generator = ReportGenerator.getInstance();
        IPentahoRequestContext requestContext = PentahoRequestContextHolder.getRequestContext();
        URL url = null;
        try {
          url = new URL( requestContext.getContextPath() );
        } catch ( Exception e ) {
          // a null URL is ok
          warn( Messages.getInstance().getString( "JFreeReportLoadComponent.WARN_COULD_NOT_CREATE_URL" ) ); //$NON-NLS-1$
        }
        report =
            generator
                .parseReport(
                  new InputSource( new ByteArrayInputStream( repDef.getBytes( "UTF-8" ) ) ),
                  getDefinedResourceURL( url ) ); //$NON-NLS-1$
      }
    }

    return report;
  }

  private MasterReport getReportFromJar() throws Exception {
    MasterReport report;
    final IActionSequenceResource resource = getResource( AbstractJFreeReportComponent.DATACOMPONENT_JARINPUT );
    final ClassLoader loader = ReportUtils.createJarLoader( getSession(), resource );
    if ( loader == null ) {
      throw new Exception( Messages.getInstance().getString(
        "JFreeReportLoadComponent.ERROR_0035_COULD_NOT_CREATE_CLASSLOADER" ) ); //$NON-NLS-1$
    }

    String reportLocation = getInputStringValue( AbstractJFreeReportComponent.REPORTLOAD_REPORTLOC );
    URL resourceUrl = loader.getResource( reportLocation );
    if ( resourceUrl == null ) {
      throw new Exception( Messages.getInstance().getErrorString( "JFreeReport.ERROR_0016_REPORT_RESOURCE_INVALID", //$NON-NLS-1$
          reportLocation, resource.getAddress() ) );
    }

    try {
      ReportGenerator generator = ReportGenerator.getInstance();
      report = generator.parseReport( resourceUrl, getDefinedResourceURL( resourceUrl ) );
    } catch ( Exception ex ) {
      throw new Exception( Messages.getInstance().getErrorString(
          "JFreeReport.ERROR_0007_COULD_NOT_PARSE", reportLocation ), ex ); //$NON-NLS-1$
    }
    return report;
  }

  public MasterReport getReport() throws Exception {
    MasterReport report = getReportFromResource();
    if ( report == null ) {
      report = getReportFromInputParam();
      if ( report == null ) {
        report = getReportFromJar();
      }
    }
    return report;
  }

  @Override
  protected boolean executeAction() throws Throwable {
    boolean result = false;
    try {
      MasterReport report = getReport();
      if ( report != null ) {
        addTempParameterObject( AbstractJFreeReportComponent.DATACOMPONENT_REPORTTEMP_OBJINPUT, report );
        result = true;
      }
    } catch ( Exception ex ) {
      error( ex.getMessage() );
    }
    return result;
  }

  private URL getDefinedResourceURL( final URL defaultValue ) {
    if ( isDefinedInput( AbstractJFreeReportComponent.REPORTLOAD_RESURL ) == false ) {
      return defaultValue;
    }

    try {
      final String inputStringValue =
          getInputStringValue( Messages.getInstance().getString( AbstractJFreeReportComponent.REPORTLOAD_RESURL ) );
      return new URL( inputStringValue );
    } catch ( Exception e ) {
      return defaultValue;
    }
  }

  private String getBaseServerURL( final String pentahoBaseURL ) {
    try {
      URL url = new URL( pentahoBaseURL );
      return url.getProtocol() + "://" + url.getHost() + ":" + url.getPort(); //$NON-NLS-1$ //$NON-NLS-2$
    } catch ( Exception e ) {
      //ignore
    }
    return pentahoBaseURL;
  }

  private String getHostColonPort( final String pentahoBaseURL ) {
    try {
      URL url = new URL( pentahoBaseURL );
      return url.getHost() + ":" + url.getPort(); //$NON-NLS-1$
    } catch ( Exception e ) {
      //ignore
    }
    return pentahoBaseURL;
  }

  /**
   * Parses the report, using the given ActionResource as initial report definition.
   * 
   * @param resource
   * @return
   */
  private MasterReport parseReport( final IActionSequenceResource resource ) {
    try {
      // define the resource url so that PentahoResourceLoader recognizes the path.
      String resourceUrl =
          PentahoResourceLoader.SOLUTION_SCHEMA_NAME + PentahoResourceLoader.SCHEMA_SEPARATOR + resource.getAddress();

      String fullyQualifiedServerUrl = PentahoSystem.getApplicationContext().getFullyQualifiedServerURL();

      HashMap helperObjects = new HashMap();

      helperObjects.put( new FactoryParameterKey( "pentahoBaseURL" ), fullyQualifiedServerUrl ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 

      // trim out the server and port
      helperObjects.put( new FactoryParameterKey( "serverBaseURL" ), getBaseServerURL( fullyQualifiedServerUrl ) ); //$NON-NLS-1$

      helperObjects.put(
          new FactoryParameterKey( "solutionRoot" ), PentahoSystem.getApplicationContext().getSolutionPath( "" ) ); //$NON-NLS-1$ //$NON-NLS-2$

      // get the host:port portion only
      helperObjects.put( new FactoryParameterKey( "hostColonPort" ), getHostColonPort( fullyQualifiedServerUrl ) ); //$NON-NLS-1$

      // get the requestContextPath
      helperObjects
          .put(
            new FactoryParameterKey( "requestContextPath" ),
            PentahoRequestContextHolder.getRequestContext().getContextPath() ); //$NON-NLS-1$

      Iterator it = getInputNames().iterator();
      while ( it.hasNext() ) {
        try {
          String inputName = (String) it.next();
          String inputValue = getInputStringValue( inputName );
          helperObjects.put( new FactoryParameterKey( inputName ), inputValue );
        } catch ( Exception e ) {
          //ignore
        }
      }

      ResourceManager resourceManager = new ResourceManager();
      resourceManager.registerDefaults();

      ResourceKey contextKey = resourceManager.createKey( resourceUrl, helperObjects );
      ResourceKey key = resourceManager.createKey( resourceUrl, helperObjects );

      return ReportGenerator.getInstance().parseReport( resourceManager, key, contextKey );

    } catch ( Exception ex ) {
      error(
          Messages.getInstance().getErrorString( "JFreeReport.ERROR_0007_COULD_NOT_PARSE", resource.getAddress() ), ex ); //$NON-NLS-1$
      return null;
    }
  }

  @Override
  public boolean init() {
    return true;
  }

  @Override
  public Log getLogger() {
    return LogFactory.getLog( JFreeReportLoadComponent.class );
  }
}
