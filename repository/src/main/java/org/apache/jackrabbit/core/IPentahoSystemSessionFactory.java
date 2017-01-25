package org.apache.jackrabbit.core;

import org.apache.jackrabbit.core.config.WorkspaceConfig;
import org.pentaho.platform.api.engine.IPentahoRegistrableObjectFactory;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Factory for Jackrabbit System Sessions. This should only be used to perform low-level JCR internal operations.
 *
 * Created by nbaker on 10/6/15.
 */
public interface IPentahoSystemSessionFactory {
  Session create(RepositoryImpl repository) throws RepositoryException;

  class DefaultImpl implements IPentahoSystemSessionFactory {
    public Session create(RepositoryImpl repository) throws
        RepositoryException {
      return SystemSession.create( repository.getRepositoryContext(), repository.getWorkspaceInfo( "default" ).getConfig() );
    }
  }


}
