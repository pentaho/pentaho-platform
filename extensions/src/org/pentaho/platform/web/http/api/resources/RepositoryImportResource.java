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
 * @created 1/14/2011
 * @author Aaron Phillips
 * 
 */
package org.pentaho.platform.web.http.api.resources;

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.services.importer.IPlatformImportBundle;
import org.pentaho.platform.plugin.services.importer.IPlatformImporter;
import org.pentaho.platform.plugin.services.importer.NameBaseMimeResolver;
import org.pentaho.platform.plugin.services.importer.RepositoryFileImportBundle;
import org.pentaho.platform.web.http.messages.Messages;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

@Path("/repo/files/import")
public class RepositoryImportResource {

	/**
	 * @param uploadDir
	 *            : JCR Directory to which the zip structure or single file will
	 *            be uploaded to.
	 * @param fileIS
	 *            : Input stream for the file.
	 * @param fileInfo
	 *            : Info about he file (
	 * @return http ok response of everything went well... some other error
	 *         otherwise
	 *         <p/>
	 *         This REST method takes multi-part form data and imports it to a
	 *         JCR repository.
	 *         --import --url=http://localhost:8080/pentaho --username=joe --password=password --source=file-system --type=files --charset=UTF-8 --path=/public  --file-path="C:/pentahotraining/BootCamp Labs/Pilot Project/SteelWheels.csv" --permission=true --overwrite=true --retainOwnership=true --rest=true
	 */
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_HTML)
	public Response doPostImport(
			@FormDataParam("importDir") String uploadDir,
			@FormDataParam("fileUpload") InputStream fileIS,
			@FormDataParam("overwrite") String overwrite,
			@FormDataParam("ignoreACLS") String ignoreACLS,
			@FormDataParam("retainOwnership") String retainOwnership,
			@FormDataParam("charSet") String charSet,
			@FormDataParam("fileUpload") FormDataContentDisposition fileInfo) {
		
			try {
				validateAccess();
				
				boolean overwriteFileFlag = ("true".equals(overwrite) ? true : false);
				boolean ignoreACLFlag = ("true".equals(ignoreACLS) ? true : false);
				boolean retainOwnershipFlag = ("true".equals(retainOwnership) ? true : false);
	
				RepositoryFileImportBundle.Builder bundleBuilder = new RepositoryFileImportBundle.Builder();
				bundleBuilder.input(fileIS);
				bundleBuilder.charSet(charSet == null?"UTF-8":charSet);
				bundleBuilder.hidden(false);
				bundleBuilder.path(uploadDir);
				bundleBuilder.overwrite(overwriteFileFlag);
				bundleBuilder.name(fileInfo.getFileName());
				IPlatformImportBundle bundle = bundleBuilder.build();
	
				NameBaseMimeResolver mimeResolver = PentahoSystem.get(NameBaseMimeResolver.class);
				bundleBuilder.mime(mimeResolver.resolveMimeForFileName(fileInfo.getFileName()));
	
				IPlatformImporter importer = PentahoSystem.get(IPlatformImporter.class);
				importer.importFile(bundle);
	
				// Flush the Mondrian cache to show imported datasources.
				IMondrianCatalogService mondrianCatalogService = PentahoSystem.get(
				IMondrianCatalogService.class, "IMondrianCatalogService",
				PentahoSessionHolder.getSession());
				mondrianCatalogService.reInit(PentahoSessionHolder.getSession());
			} catch (PentahoAccessControlException e) {
				return Response.serverError().entity(e.toString()).build();
			} catch (Exception e) {
				return Response.serverError().entity(e.toString()).build();
			}
			return Response.ok(Messages.getInstance().getString("FileResource.IMPORT_SUCCESS")).build();
	}

	private void validateAccess() throws PentahoAccessControlException {
		IAuthorizationPolicy policy = PentahoSystem
				.get(IAuthorizationPolicy.class);
		boolean isAdmin = policy
				.isAllowed(IAuthorizationPolicy.READ_REPOSITORY_CONTENT_ACTION)
				&& policy
						.isAllowed(IAuthorizationPolicy.CREATE_REPOSITORY_CONTENT_ACTION)
				&& policy
						.isAllowed(IAuthorizationPolicy.ADMINISTER_SECURITY_ACTION);
		if (!isAdmin) {
			throw new PentahoAccessControlException("Access Denied");
		}
	}
}
