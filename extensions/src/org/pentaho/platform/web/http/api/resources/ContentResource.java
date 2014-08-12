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

package org.pentaho.platform.web.http.api.resources;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.web.http.api.resources.utils.FileUtils;
import org.pentaho.platform.web.http.messages.Messages;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

/**
 * This api provides methods for discovering content in the platform
 */
@Path( "/content" )
public class ContentResource {

  private static final Log logger = LogFactory.getLog( ContentResource.class );
  protected static IUnifiedRepository repository;

  /**
   * Determines whether a selected file supports parameters or not
   *
   * @param pathId (colon separated path for the repository file)
   * @return ("true" or "false")
   * @throws java.io.FileNotFoundException
   */
  @GET
  @Path( "{pathId : .+}/parameterizable" )
  @Produces( TEXT_PLAIN )
  // have to accept anything for browsers to work
  public String doIsParameterizable( @PathParam( "pathId" ) String pathId ) throws FileNotFoundException {
    boolean hasParameterUi = false;
    RepositoryFile repositoryFile = getRepository().getFile( FileUtils.idToPath( pathId ) );
    if ( repositoryFile != null ) {
      try {
        hasParameterUi =
          ( PentahoSystem.get( IPluginManager.class ).getContentGenerator(
            repositoryFile.getName().substring( repositoryFile.getName().lastIndexOf( '.' ) + 1 ), "parameterUi" )
            != null );
      } catch ( NoSuchBeanDefinitionException e ) {
        // Do nothing.
      }
    }
    boolean hasParameters = false;
    if ( hasParameterUi ) {
      try {
        IContentGenerator parameterContentGenerator =
          PentahoSystem.get( IPluginManager.class ).getContentGenerator(
            repositoryFile.getName().substring( repositoryFile.getName().lastIndexOf( '.' ) + 1 ), "parameter" );
        if ( parameterContentGenerator != null ) {
          ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
          parameterContentGenerator.setOutputHandler( new SimpleOutputHandler( outputStream, false ) );
          parameterContentGenerator.setMessagesList( new ArrayList<String>() );
          Map<String, IParameterProvider> parameterProviders = new HashMap<String, IParameterProvider>();
          SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
          parameterProvider.setParameter( "path", URLEncoder.encode( repositoryFile.getPath(), "UTF-8" ) );
          parameterProvider.setParameter( "renderMode", "PARAMETER" );
          parameterProviders.put( IParameterProvider.SCOPE_REQUEST, parameterProvider );
          parameterContentGenerator.setParameterProviders( parameterProviders );
          parameterContentGenerator.setSession( PentahoSessionHolder.getSession() );
          parameterContentGenerator.createContent();
          if ( outputStream.size() > 0 ) {
            Document document = DocumentHelper.parseText( outputStream.toString() );

            // exclude all parameters that are of type "system", xactions set system params that have to be ignored.
            @SuppressWarnings( "rawtypes" )
            List nodes = document.selectNodes( "parameters/parameter" );
            for ( int i = 0; i < nodes.size() && !hasParameters; i++ ) {
              Element elem = (Element) nodes.get( i );
              if ( elem.attributeValue( "name" ).equalsIgnoreCase( "output-target" )
                && elem.attributeValue( "is-mandatory" ).equalsIgnoreCase( "true" ) ) {
                hasParameters = true;
                continue;
              }
              Element attrib =
                (Element) elem.selectSingleNode( "attribute[@namespace='http://reporting.pentaho"
                  + ".org/namespaces/engine/parameter-attributes/core' and @name='role']" );
              if ( attrib == null || !"system".equals( attrib.attributeValue( "value" ) ) ) {
                hasParameters = true;
              }
            }
          }
        }
      } catch ( Exception e ) {
        logger
          .error( Messages.getInstance().getString( "FileResource.PARAM_FAILURE", e.getMessage() ), e ); //$NON-NLS-1$
      }
    }
    return Boolean.toString( hasParameters );
  }

  public static IUnifiedRepository getRepository() {
    if ( repository == null ) {
      repository = PentahoSystem.get( IUnifiedRepository.class );
    }
    return repository;
  }

}
