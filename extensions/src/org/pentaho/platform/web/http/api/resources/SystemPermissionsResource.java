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


import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryReadAction;

@Path("/legacy/permissions")
public class SystemPermissionsResource extends AbstractJaxRSResource {

    @GET
    @Produces({MediaType.APPLICATION_XML})
    public Response getLegacyPermissions()  throws Exception {
        try {
            if(canAdminister()) {
              return Response.ok(SystemResourceUtil.getPermissions().asXML()).type(MediaType.APPLICATION_XML).build();  
            } else {
              return Response.status(UNAUTHORIZED).build();
            }
            
        } catch (Throwable t) {
          throw new WebApplicationException(t);
        }
    }
    
    private boolean canAdminister() {
      IAuthorizationPolicy policy = PentahoSystem
          .get(IAuthorizationPolicy.class);
      return policy
          .isAllowed(RepositoryReadAction.NAME) && policy.isAllowed(RepositoryCreateAction.NAME)
          && (policy.isAllowed(AdministerSecurityAction.NAME));
    }
}
