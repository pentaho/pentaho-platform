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


package org.pentaho.platform.repository2.unified.jcr.sejcr;

import javax.jcr.Credentials;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * JCR Session factory which creates a new session for every request.
 *
 * Created by nbaker on 6/9/14.
 */
public class NoCachePentahoJcrSessionFactory implements PentahoJcrSessionFactory {

  private Repository repository;
  private String workspaceName;

  public NoCachePentahoJcrSessionFactory( Repository repository, String workspaceName ) {
    this.repository = repository;
    this.workspaceName = workspaceName;
  }

  @Override public Session getSession( Credentials credentials ) throws RepositoryException {
    return repository.login( credentials, workspaceName );
  }
}
