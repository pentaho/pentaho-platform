/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.repository2.unified.webservices.jaxws;

import java.util.List;

import javax.jws.WebService;

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
}
