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

package org.pentaho.platform.plugin.action.kettle;

import com.google.common.base.Throwables;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.www.SlaveServerConfig;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.Collections;

/**
 * Slave Server Config for Carte Servlet running within the DI Server.
 * Overrides {@link #getRepository()} and {@link #getMetaStore()} to use an in-process PurRepository connection
 * with active user credentials if not otherwise configured via slaveserverconfig.xml
 *
 * @author nhudak
 */
public class DIServerConfig extends SlaveServerConfig {
  public static final String SINGLE_DI_SERVER_INSTANCE = "singleDiServerInstance";
  public static final String PUR_REPOSITORY_PLUGIN_ID = "PentahoEnterpriseRepository";
  private RepositoryMeta repositoryMeta;
  private final PluginRegistry pluginRegistry;

  public DIServerConfig( LogChannel logChannel, Node configNode ) throws KettleXMLException {
    this( logChannel, configNode, PluginRegistry.getInstance() );
  }

  public DIServerConfig( LogChannel logChannel, Node configNode, PluginRegistry pluginRegistry ) throws KettleXMLException {
    super( logChannel, configNode );
    this.pluginRegistry = pluginRegistry;
  }

  @Override
  public Repository getRepository() throws KettleException {
    Repository repository = super.getRepository();

    if ( repository == null && repositoryInProcess() ) {
      try {
        repository = connectInProcessRepository();
      } catch ( Exception e ) {
        // Something failed, give up and return null
        Logger.warn( this, e.getMessage(), e ); //$NON-NLS-1$
        repository = null;
      }
    }
    return repository;
  }

  @Override
  public IMetaStore getMetaStore() {
    IMetaStore metaStore = super.getMetaStore();

    try {
      Repository configuredRepository = super.getRepository();
      if ( configuredRepository == null && repositoryInProcess() ) {
        Repository inProcessRepository = connectInProcessRepository();
        if ( inProcessRepository != null ) {
          metaStore = inProcessRepository.getRepositoryMetaStore();
        }
      }
    } catch ( Exception e ) {
      // Something failed, give up and use default
      Logger.warn( this, e.getMessage(), e );
      metaStore = super.getMetaStore();
    }
    return metaStore;
  }

  private boolean repositoryInProcess() {
    return PentahoSystem.getApplicationContext() != null
      && "true".equals( PentahoSystem.getSystemSetting( SINGLE_DI_SERVER_INSTANCE, "true" ) );
  }


  private Repository connectInProcessRepository() throws KettleException {
    Repository repository = null;

    // Get active user
    IPentahoSession user = PentahoSessionHolder.getSession();

    if ( user != null ) {
      // Connect to repository
      repository = pluginRegistry.loadClass( RepositoryPluginType.class, PUR_REPOSITORY_PLUGIN_ID, Repository.class );
      try {
        repository.init( getRepositoryMeta() );
      } catch ( Exception e ) {
        Throwables.propagateIfPossible( e, KettleException.class );
        throw new KettleException( e );
      }

      // Connect as current user, password will not actually be used
      repository.connect( user.getName(), "" );
    }

    return repository;
  }

  private RepositoryMeta getRepositoryMeta() throws KettleException, ParserConfigurationException {
    if ( repositoryMeta == null ) {
      // Load Repository Meta
      RepositoryMeta repositoryMeta = pluginRegistry.loadClass( RepositoryPluginType.class, PUR_REPOSITORY_PLUGIN_ID, RepositoryMeta.class );

      DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document document = docBuilder.newDocument();
      Element repnode = document.createElement( RepositoryMeta.XML_TAG );

      Element id = document.createElement( "id" );
      id.setTextContent( PUR_REPOSITORY_PLUGIN_ID );
      repnode.appendChild( id );

      Element name = document.createElement( "name" );
      name.setTextContent( SINGLE_DI_SERVER_INSTANCE );
      repnode.appendChild( name );

      Element description = document.createElement( "description" );
      description.setTextContent( SINGLE_DI_SERVER_INSTANCE );
      repnode.appendChild( description );

      Element location = document.createElement( "repository_location_url" );
      location.setTextContent( PentahoSystem.getApplicationContext().getFullyQualifiedServerURL() );
      repnode.appendChild( location );

      repositoryMeta.loadXML( repnode, Collections.<DatabaseMeta>emptyList() );
      this.repositoryMeta = repositoryMeta;
    }
    return repositoryMeta;
  }
}
