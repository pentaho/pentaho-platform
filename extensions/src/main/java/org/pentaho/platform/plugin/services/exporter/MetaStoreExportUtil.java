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

package org.pentaho.platform.plugin.services.exporter;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.io.ByteArrayInputStream;

public class MetaStoreExportUtil {
  private static final String SINGLE_DI_SERVER_INSTANCE = "singleDiServerInstance"; //$NON-NLS-1$

  public static Repository connectToRepository( String repositoryName ) throws KettleException {
    RepositoriesMeta repositoriesMeta = new RepositoriesMeta();
    boolean singleDiServerInstance =
      "true".equals( PentahoSystem.getSystemSetting( SINGLE_DI_SERVER_INSTANCE, "true" ) ); //$NON-NLS-1$ //$NON-NLS-2$

    try {
      if ( singleDiServerInstance ) {

        // only load a default enterprise repository. If this option is set, then you cannot load
        // transformations or jobs from anywhere but the local server.

        String repositoriesXml =
          "<?xml version=\"1.0\" encoding=\"UTF-8\"?><repositories>" //$NON-NLS-1$
            + "<repository><id>PentahoEnterpriseRepository</id>" //$NON-NLS-1$
            + "<name>" + SINGLE_DI_SERVER_INSTANCE + "</name>" //$NON-NLS-1$ //$NON-NLS-2$
            + "<description>" + SINGLE_DI_SERVER_INSTANCE + "</description>" //$NON-NLS-1$ //$NON-NLS-2$
            + "<repository_location_url>" + PentahoSystem.getApplicationContext().getFullyQualifiedServerURL() + "</repository_location_url>" //$NON-NLS-1$ //$NON-NLS-2$
            + "<version_comment_mandatory>N</version_comment_mandatory>" //$NON-NLS-1$
            + "</repository>" //$NON-NLS-1$
            + "</repositories>"; //$NON-NLS-1$

        ByteArrayInputStream sbis = new ByteArrayInputStream( repositoriesXml.getBytes( "UTF8" ) );
        repositoriesMeta.readDataFromInputStream( sbis );
      } else {
        // TODO: add support for specified repositories.xml files...
        repositoriesMeta.readData(); // Read from the default $HOME/.kettle/repositories.xml file.
      }
    } catch ( Exception e ) {
      throw new KettleException( "Meta repository not populated", e ); //$NON-NLS-1$
    }

    // Find the specified repository.
    RepositoryMeta repositoryMeta = null;
    try {
      if ( singleDiServerInstance ) {
        repositoryMeta = repositoriesMeta.findRepository( SINGLE_DI_SERVER_INSTANCE );
      } else {
        repositoryMeta = repositoriesMeta.findRepository( repositoryName );
      }

    } catch ( Exception e ) {
      throw new KettleException( "Repository not found", e ); //$NON-NLS-1$
    }

    if ( repositoryMeta == null ) {
      throw new KettleException( "RepositoryMeta is null" ); //$NON-NLS-1$
    }

    Repository repository = null;
    try {
      repository =
        PluginRegistry.getInstance().loadClass( RepositoryPluginType.class,
          repositoryMeta.getId(), Repository.class );
      repository.init( repositoryMeta );

    } catch ( Exception e ) {
      throw new KettleException( "Could not get repository instance", e ); //$NON-NLS-1$
    }

    // Two scenarios here: internal to server or external to server. If internal, you are already authenticated. If
    // external, you must provide a username and additionally specify that the IP address of the machine running this
    // code is trusted.
    repository.connect( PentahoSessionHolder.getSession().getName(), "password" );

    return repository;
  }
}
