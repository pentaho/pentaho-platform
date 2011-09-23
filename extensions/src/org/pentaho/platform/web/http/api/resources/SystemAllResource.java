/*
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
 * Copyright 2011 Pentaho Corporation.  All rights reserved.
 *
 *
 * @created 3/11/2011
 * @author Ramaiz Mansoor
 *
 */
package org.pentaho.platform.web.http.api.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/system")
public class SystemAllResource extends AbstractJaxRSResource {
    @GET
    @Produces({MediaType.APPLICATION_XML})
    public Response getAll()  throws Exception {
        try {
            return Response.ok(SystemResourceUtil.getAll().asXML()).type(MediaType.APPLICATION_XML).build();
        } catch (Throwable t) {
           throw new WebApplicationException(t);
        }
    }


}