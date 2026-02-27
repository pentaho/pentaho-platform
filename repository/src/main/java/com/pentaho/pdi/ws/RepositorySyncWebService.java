/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package com.pentaho.pdi.ws;

import com.pentaho.pdi.messages.Messages;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.core.xml.XMLParserFactoryProducer;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import jakarta.jws.WebService;
import java.io.Serializable;
import java.io.StringBufferInputStream;

@WebService( endpointInterface = "com.pentaho.pdi.ws.IRepositorySyncWebService", serviceName = "repositorySync",
    portName = "repositorySyncPort", targetNamespace = "http://www.pentaho.org/ws/1.0" )
public class RepositorySyncWebService implements IRepositorySyncWebService, Serializable {

  private static final long serialVersionUID = 743647084187858081L; /* EESOURCE: UPDATE SERIALVERUID */

  private static Log log = LogFactory.getLog( RepositorySyncWebService.class );
  private static final String SINGLE_DI_SERVER_INSTANCE = "singleDiServerInstance";

  public RepositorySyncStatus sync( String repositoryId, String repositoryUrl ) throws RepositorySyncException {
    boolean singleDiServerInstance =
        "true".equals( PentahoSystem.getSystemSetting( SINGLE_DI_SERVER_INSTANCE, "true" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    if ( singleDiServerInstance ) {
      return RepositorySyncStatus.SINGLE_DI_SERVER_INSTANCE;
    }

    RepositoriesMeta repositoriesMeta = new RepositoriesMeta();
    try {
      repositoriesMeta.readData();
    } catch ( Exception e ) {
      log.error( Messages.getInstance().getString( "RepositorySyncWebService.UNABLE_TO_READ_DATA" ), e ); //$NON-NLS-1$
      throw new RepositorySyncException( Messages.getInstance().getString(
          "RepositorySyncWebService.UNABLE_TO_READ_DATA" ), e ); //$NON-NLS-1$
    }
    RepositoryMeta repositoryMeta = repositoriesMeta.findRepository( repositoryId );
    if ( repositoryMeta == null ) {
      try {
        repositoryMeta = getRepositoryMeta( repositoryId, repositoryUrl );
        if ( repositoryMeta == null ) {
          log.error( Messages.getInstance().getString( "RepositorySyncWebService.UNABLE_TO_LOAD_PLUGIN" ) ); //$NON-NLS-1$
          throw new RepositorySyncException( Messages.getInstance().getString(
              "RepositorySyncWebService.UNABLE_TO_LOAD_PLUGIN" ) ); //$NON-NLS-1$
        }
        repositoriesMeta.addRepository( repositoryMeta );
        repositoriesMeta.writeData();
        return RepositorySyncStatus.REGISTERED;
      } catch ( KettleException e ) {
        log.error( Messages.getInstance().getString(
            "RepositorySyncWebService.UNABLE_TO_REGISTER_REPOSITORY", repositoryId ), e ); //$NON-NLS-1$
        throw new RepositorySyncException( Messages.getInstance().getString(
            "RepositorySyncWebService.UNABLE_TO_REGISTER_REPOSITORY", repositoryId ), e ); //$NON-NLS-1$
      }
    } else {
      String xml = repositoryMeta.getXML();
      Element node;
      try {
        node =
          XMLParserFactoryProducer.createSecureDocBuilderFactory().newDocumentBuilder().parse( new StringBufferInputStream( xml ) )
                .getDocumentElement();
      } catch ( Exception e ) {
        node = null;
      }
      if ( node != null ) {
        NodeList list = node.getElementsByTagName( "repository_location_url" ); //$NON-NLS-1$
        if ( list != null && list.getLength() == 1 ) {
          String url = list.item( 0 ).getTextContent();
          if ( url.equals( repositoryUrl ) ) {

            // now test base URL
            String fullyQualifiedServerUrl = null;
            if ( PentahoSystem.getApplicationContext().getFullyQualifiedServerURL() != null ) {
              fullyQualifiedServerUrl = PentahoSystem.getApplicationContext().getFullyQualifiedServerURL();
              if ( url.endsWith( "/" ) ) { //$NON-NLS-1$
                url = url.substring( 0, url.length() - 2 );
              }
              if ( fullyQualifiedServerUrl.endsWith( "/" ) ) { //$NON-NLS-1$
                fullyQualifiedServerUrl = fullyQualifiedServerUrl.substring( 0, fullyQualifiedServerUrl.length() - 2 );
              }
              if ( url.startsWith( fullyQualifiedServerUrl ) ) {
                return RepositorySyncStatus.ALREADY_REGISTERED;
              }
            }
            log.error( Messages.getInstance().getString(
                "RepositorySyncWebService.FULLY_QUALIFIED_SERVER_URL_SYNC_PROBLEM", fullyQualifiedServerUrl, url ) ); //$NON-NLS-1$
            throw new RepositorySyncException( Messages.getInstance().getString(
                "RepositorySyncWebService.FULLY_QUALIFIED_SERVER_URL_SYNC_PROBLEM", fullyQualifiedServerUrl, url ) ); //$NON-NLS-1$
          } else {
            log.error( Messages.getInstance().getString(
                "RepositorySyncWebService.REPOSITORY_URL_SYNC_PROBLEM", repositoryId, url, repositoryUrl ) ); //$NON-NLS-1$
            throw new RepositorySyncException( Messages.getInstance().getString(
                "RepositorySyncWebService.REPOSITORY_URL_SYNC_PROBLEM", repositoryId, url, repositoryUrl ) ); //$NON-NLS-1$
          }
        }
      }
      log.error( Messages.getInstance().getString(
          "RepositorySyncWebService.REPOSITORY_URL_XML_PARSING_PROBLEM", repositoryId, xml ) ); //$NON-NLS-1$
      throw new RepositorySyncException( Messages.getInstance().getString(
          "RepositorySyncWebService.REPOSITORY_URL_XML_PARSING_PROBLEM_CLIENT_MESSAGE", repositoryId ) ); //$NON-NLS-1$
    }
  }

  private static RepositoryMeta getRepositoryMeta( String repositoryId, String repositoryUrl ) throws KettleException {
    RepositoryMeta repMeta =
        PluginRegistry.getInstance().loadClass( RepositoryPluginType.class,
            "PentahoEnterpriseRepository", RepositoryMeta.class ); //$NON-NLS-1$
    // this repository is not available
    if ( repMeta == null ) {
      return null;
    }

    String xml = "<repo>" + //$NON-NLS-1$
        "<id>PentahoEnterpriseRepository</id>" + //$NON-NLS-1$
        "<name>" + repositoryId + "</name>" + //$NON-NLS-1$ //$NON-NLS-2$
        "<description>" + repositoryId + "</description>" + //$NON-NLS-1$ //$NON-NLS-2$
        "<repository_location_url>" + repositoryUrl + "</repository_location_url> </repo>"; //$NON-NLS-1$ //$NON-NLS-2$

    Element node;
    try {
      node =
        XMLParserFactoryProducer.createSecureDocBuilderFactory().newDocumentBuilder().parse( new StringBufferInputStream( xml ) )
          .getDocumentElement();
    } catch ( Exception e ) {
      node = null;
    }
    repMeta.loadXML( node, null );
    return repMeta;
  }

  @Override
  public void logout() {
    // no-op, handled in PentahoWSSpringServlet
  }
}
