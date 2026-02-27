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


package org.pentaho.platform.repository2.unified.webservices.jaxws;

import java.util.List;

import jakarta.jws.WebService;

import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryReadAction;


@WebService ( endpointInterface = "org.pentaho.platform.repository2.unified.webservices.jaxws.IUnifiedRepositoryJaxwsWebService",
  serviceName = "unifiedRepository", portName = "unifiedRepositoryPort", targetNamespace = "http://www.pentaho.org/ws/1.0" )
public class DiUnifiedRepositoryJaxwsWebService extends DefaultUnifiedRepositoryJaxwsWebService implements
  IUnifiedRepositoryJaxwsWebService {

  @Override
  protected void validateEtcReadAccess( String path ) {
    IAuthorizationPolicy policy = PentahoSystem.get( IAuthorizationPolicy.class );

    if ( !policy.isAllowed( RepositoryReadAction.NAME ) && path.startsWith( "/etc" ) ) {
      throw new RuntimeException( "This user is not allowed to access the ETC folder in JCR." );
    }
  }

  @Override
  protected void validateEtcWriteAccess( String parentFolderId ) {
    // Noop to allow write access in DI Server
  }

  @Override
  public List<RepositoryFileDto> getDeletedFiles() {
    return marshalFiles( repo.getAllDeletedFiles() );
  }

  @Override
  public void logout() {
    // no-op, handled in PentahoWSSpringServlet
  }
}
