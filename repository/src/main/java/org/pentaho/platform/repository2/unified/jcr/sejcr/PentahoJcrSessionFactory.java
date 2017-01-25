package org.pentaho.platform.repository2.unified.jcr.sejcr;

import javax.jcr.Credentials;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Factory used by the CredentialsStrategySessionFactory to create new JCR Session instances
 * Created by nbaker on 6/9/14.
 */
public interface PentahoJcrSessionFactory {
  Session getSession( Credentials credentials ) throws RepositoryException;
}
