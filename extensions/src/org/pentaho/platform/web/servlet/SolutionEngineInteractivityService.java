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

package org.pentaho.platform.web.servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.actionsequence.dom.IActionDefinition;
import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IActionSequence;
import org.pentaho.platform.api.engine.ICreateFeedbackParameterCallback;
import org.pentaho.platform.api.engine.IMimeTypeListener;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoRequestContext;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.engine.core.solution.ActionInfo;
import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.ActionSequenceJCRHelper;
import org.pentaho.platform.engine.services.actionsequence.ActionParameterSource;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.platform.web.http.HttpOutputHandler;
import org.pentaho.platform.web.http.request.HttpRequestParameterProvider;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SolutionEngineInteractivityService extends ServletBase {

  private static final Log logger = LogFactory.getLog( SolutionEngineInteractivityService.class );
  protected Map xformFields = new HashMap();

  public Log getLogger() {
    return SolutionEngineInteractivityService.logger;
  }

  public SolutionEngineInteractivityService() {
    super();
  }

  protected boolean doMessages( final HttpServletRequest request ) {
    return "true".equalsIgnoreCase( request.getParameter( "debug" ) ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  protected void handleActionRequest( final HttpServletRequest request, final HttpServletResponse response,
      final HttpOutputHandler outputHandler, final HttpServletRequestHandler requestHandler,
      IParameterProvider requestParameters, ByteArrayOutputStream outputStream, final IContentItem contentItem )
    throws ServletException, IOException {
    IRuntimeContext runtime = null;
    try {

      final org.w3c.dom.Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
      final org.w3c.dom.Element root = document.createElement( "action_sequence_info" );
      document.appendChild( root );

      requestHandler.setCreateFeedbackParameterCallback( new ICreateFeedbackParameterCallback() {
        public void createFeedbackParameter( IRuntimeContext runtimeContext, String fieldName, String displayName,
            String hint, Object defaultValues, List values, Map dispNames, String displayStyle, boolean optional,
            boolean visible ) {

          org.w3c.dom.Element parameterElement = document.createElement( "parameter" );
          parameterElement.setAttribute( "name", fieldName );
          parameterElement.setAttribute( "display-name", displayName );
          parameterElement.setAttribute( "display-style", displayStyle );
          parameterElement.setAttribute( "hint", "" + hint );
          parameterElement.setAttribute( "optional", "" + optional );
          parameterElement.setAttribute( "visible", "" + visible );

          try {
            IActionParameter actionParameter = runtimeContext.getInputParameter( fieldName );
            if ( actionParameter != null ) {
              List variables = actionParameter.getVariables();
              for ( int i = 0; variables != null && i < variables.size(); i++ ) {
                Object var = variables.get( i );
                if ( var instanceof ActionParameterSource ) {
                  String sourceName = ( (ActionParameterSource) var ).getSourceName();
                  String sourceValue = ( (ActionParameterSource) var ).getValue();
                  parameterElement.setAttribute( "source-name", "" + sourceName );
                  parameterElement.setAttribute( "source-value", "" + sourceValue );
                } else {
                  System.out.println( var );
                }
              }
            }
          } catch ( Exception npe ) {
            //ignore
          }

          root.appendChild( parameterElement );

          if ( values != null ) {
            org.w3c.dom.Element valuesElement = document.createElement( "values" );
            for ( Object value : values ) {
              org.w3c.dom.Element valueElement = document.createElement( "value" );
              valueElement.setAttribute( "value", "" + value );
              if ( dispNames != null && dispNames.containsKey( value ) ) {
                valueElement.setAttribute( "display-name", "" + dispNames.get( value ) );
              }
              valuesElement.appendChild( valueElement );
            }
            parameterElement.appendChild( valuesElement );
          }
          if ( defaultValues != null ) {
            org.w3c.dom.Element valuesElement = document.createElement( "selected-values" );
            if ( defaultValues instanceof List ) {
              for ( Object value : (List) defaultValues ) {
                org.w3c.dom.Element valueElement = document.createElement( "value" );
                valueElement.setAttribute( "value", "" + value );
                valuesElement.appendChild( valueElement );
              }
            } else {
              org.w3c.dom.Element valueElement = document.createElement( "value" );
              valueElement.setAttribute( "value", "" + defaultValues );
              valuesElement.appendChild( valueElement );
            }
            parameterElement.appendChild( valuesElement );
          }
        }
      } );
      runtime = requestHandler.handleActionRequest( 0, 0 );

      root.setAttribute( "is-prompt-pending", "" + runtime.isPromptPending() );

      DOMSource source = new DOMSource( document );
      StreamResult result = new StreamResult( new StringWriter() );
      TransformerFactory.newInstance().newTransformer().transform( source, result );
      String theXML = result.getWriter().toString();

      response.setContentType( "text/xml" );
      response.getOutputStream().write( theXML.getBytes() );
      response.getOutputStream().close();

    } catch ( ParserConfigurationException e ) {
      e.printStackTrace();
    } catch ( TransformerConfigurationException e ) {
      e.printStackTrace();
    } catch ( TransformerException e ) {
      e.printStackTrace();
    } catch ( TransformerFactoryConfigurationError e ) {
      e.printStackTrace();
    } finally {
      if ( runtime != null ) {
        runtime.dispose();
      }
    }
    if ( contentItem != null ) {
      contentItem.closeOutputStream();
    }
  }

  protected void setupRequestHandler( final HttpServletRequest request,
                                      final HttpServletRequestHandler requestHandler ) {
    String prompt = request.getParameter( "prompt" ); //$NON-NLS-1$
    String actionPath = request.getParameter( "path" ); //$NON-NLS-1$
    String processId = this.getClass().getName();
    String instanceId = request.getParameter( "instance-id" ); //$NON-NLS-1$
    requestHandler.setInstanceId( instanceId );
    requestHandler.setProcessId( processId );
    requestHandler.setActionPath( actionPath );
    requestHandler.setForcePrompt( ( prompt != null ) && prompt.equalsIgnoreCase( "yes" ) ); //$NON-NLS-1$
  }

  protected void setupOutputHandler( final HttpOutputHandler outputHandler,
                                     final IParameterProvider requestParameters ) {
    int outputPreference = IOutputHandler.OUTPUT_TYPE_DEFAULT;
    outputHandler.setOutputPreference( outputPreference );
  }

  protected HttpServletRequestHandler getRequestHandler( final HttpServletRequest request,
      final HttpServletResponse response, final IPentahoSession userSession,
      final IParameterProvider requestParameters, final OutputStream outputStream,
      final HttpOutputHandler outputHandler, final SimpleUrlFactory urlFactory ) throws ServletException, IOException {
    HttpServletRequestHandler requestHandler =
        new HttpServletRequestHandler( userSession, null, request, outputHandler, urlFactory );
    setupRequestHandler( request, requestHandler, requestParameters, userSession );
    return requestHandler;
  }

  protected HttpOutputHandler createOutputHandler( final HttpServletResponse response,
                                                   final OutputStream outputStream ) {
    return new HttpOutputHandler( response, outputStream, true );
  }

  @Override
  protected void doGet( final HttpServletRequest request, final HttpServletResponse response ) throws ServletException,
    IOException {
    PentahoSystem.systemEntryPoint();
    try {
      IPentahoSession userSession = getPentahoSession( request );
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      String solutionName = request.getParameter( "solution" ); //$NON-NLS-1$
      String actionPath = request.getParameter( "path" ); //$NON-NLS-1$
      String actionName = request.getParameter( "action" ); //$NON-NLS-1$
      IActionSequence actionSequence =
          new ActionSequenceJCRHelper().getActionSequence( ActionInfo.buildSolutionPath( solutionName, actionPath,
            actionName ), PentahoSystem.loggingLevel, RepositoryFilePermission.READ );
      String fileName = null;
      if ( actionSequence != null ) {
        String title = actionSequence.getTitle();
        if ( ( title != null ) && ( title.length() > 0 ) ) {
          fileName = title;
        } else {
          String sequenceName = actionSequence.getSequenceName();

          if ( ( sequenceName != null ) && ( sequenceName.length() > 0 ) ) {
            fileName = sequenceName;
          } else {
            List actionDefinitionsList = actionSequence.getActionDefinitionsAndSequences();
            int i = 0;
            boolean done = false;

            while ( ( actionDefinitionsList.size() > i ) && !done ) {
              IActionDefinition actionDefinition = (IActionDefinition) actionDefinitionsList.get( i );
              String componentName = actionDefinition.getComponentName();
              if ( ( componentName != null ) && ( componentName.length() > 0 ) ) {
                fileName = componentName;
                done = true;
              } else {
                i++;
              }
            }
          }
        }
      }
      IPentahoRequestContext requestContext = PentahoRequestContextHolder.getRequestContext();
      HttpOutputHandler outputHandler = createOutputHandler( response, outputStream );
      outputHandler.setSession( userSession );

      IMimeTypeListener listener = new HttpMimeTypeListener( request, response );
      listener.setName( fileName );
      outputHandler.setMimeTypeListener( listener );
      SimpleUrlFactory urlFactory =
          new SimpleUrlFactory( requestContext.getContextPath()
            + "SolutionEngineInteractivityService?" ); //$NON-NLS-1$
      IParameterProvider requestParameters = new HttpRequestParameterProvider( request );
      setupOutputHandler( outputHandler, requestParameters );
      HttpServletRequestHandler requestHandler =
          getRequestHandler( request, response, userSession, requestParameters, outputStream,
            outputHandler, urlFactory );
      handleActionRequest( request, response, outputHandler, requestHandler, requestParameters, outputStream, null );
    } finally {
      PentahoSystem.systemExitPoint();
    }
  }

  @Override
  protected void doPost( final HttpServletRequest request, final HttpServletResponse response )
    throws ServletException, IOException {
    doGet( request, response );
  }

  protected void setupRequestHandler( final HttpServletRequest request, final HttpServletRequestHandler requestHandler,
      final IParameterProvider requestParameters, final IPentahoSession userSession ) {
    this.setupRequestHandler( request, requestHandler );
    // first take a secondary action
    String actionName = requestParameters.getStringParameter( "action2", null ); //$NON-NLS-1$
    if ( actionName == null ) {
      // now look for a primary action
      actionName = requestParameters.getStringParameter( "action", null ); //$NON-NLS-1$
    }
    ISystemSettings systemSettings = PentahoSystem.getSystemSettings();
    String defaultParameterXsl = systemSettings.getSystemSetting( "default-parameter-xsl", "DefaultParameterForm.xsl"
    ); //$NON-NLS-1$ //$NON-NLS-2$
    requestHandler.setParameterXsl( defaultParameterXsl );
  }

}
