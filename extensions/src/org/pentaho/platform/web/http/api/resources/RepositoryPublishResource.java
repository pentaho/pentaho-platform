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

import java.io.File;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importer.IPlatformImportBundle;
import org.pentaho.platform.plugin.services.importer.IPlatformImporter;
import org.pentaho.platform.plugin.services.importer.PlatformImportException;
import org.pentaho.platform.plugin.services.importer.RepositoryFileImportBundle;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.policy.rolebased.actions.PublishAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryReadAction;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

/**
 * Publishes a content file to the repository.  Used for Analyzer and Report Writer publish
 *
 * @author tkafalas
 */
@Path("/repo/publish")
public class RepositoryPublishResource {
  
  private static final Log logger = LogFactory.getLog(FileResource.class);
  
  /////////
  // PUBLISH
  // Create or overwrite a file from stream
  @POST
  @Path("/publishfile")
  @Consumes({MediaType.MULTIPART_FORM_DATA})
  @Produces(MediaType.TEXT_PLAIN)
  public Response writeFile(@FormDataParam("importPath") String pathId,
      @FormDataParam("fileUpload") InputStream fileContents,
      @FormDataParam("overwriteFile") Boolean overwriteFile,
      @FormDataParam("fileUpload") FormDataContentDisposition fileInfo)
      throws PentahoAccessControlException {

    try {
      validateAccess();
    } catch (PentahoAccessControlException e) {
      return Response.status(UNAUTHORIZED).entity(Integer.toString(PlatformImportException.PUBLISH_USERNAME_PASSWORD_FAIL)).build();
    }
    File file = new File(pathId);
    RepositoryFileImportBundle.Builder bundleBuilder = new RepositoryFileImportBundle.Builder()
        .input(fileContents)
        .charSet("UTF-8")
        .hidden(false)
        .mime("text/xml")
        .path(file.getParent())
        .name(file.getName())
        .overwriteFile(overwriteFile);
    
    IPlatformImportBundle bundle = bundleBuilder.build();
    try{
      IPlatformImporter importer = PentahoSystem.get(IPlatformImporter.class);
      importer.importFile(bundle);
      return Response.ok("SUCCESS").type(MediaType.TEXT_PLAIN).build();
    } catch (PlatformImportException e) {
      logger.error(e);
      return Response.status(Status.PRECONDITION_FAILED).entity(Integer.toString(e.getErrorStatus())).build();
    } catch (Exception e) {
      logger.error(e);
      return Response.serverError().entity(Integer.toString(PlatformImportException.PUBLISH_GENERAL_ERROR)).build();
    }
  }
  
  /**
   * Check if user has the rights to publish or is administrator
   * 
   * @throws PentahoAccessControlException
   */
  private void validateAccess() throws PentahoAccessControlException {
    IAuthorizationPolicy policy = PentahoSystem
        .get(IAuthorizationPolicy.class);
    boolean isAdmin = policy
        .isAllowed(RepositoryReadAction.NAME)
        && policy.isAllowed(RepositoryCreateAction.NAME)
        && (policy.isAllowed(AdministerSecurityAction.NAME)
         || policy.isAllowed(PublishAction.NAME));
    if (!isAdmin) {
      throw new PentahoAccessControlException("Access Denied");
    }
  }
}
