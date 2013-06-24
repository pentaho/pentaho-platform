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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Level;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.services.importer.IPlatformImportBundle;
import org.pentaho.platform.plugin.services.importer.IPlatformImporter;
import org.pentaho.platform.plugin.services.importer.NameBaseMimeResolver;
import org.pentaho.platform.plugin.services.importer.RepositoryFileImportBundle;
import org.pentaho.platform.plugin.services.importexport.IRepositoryImportLogger;
import org.pentaho.platform.plugin.services.importexport.ImportSession;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.policy.rolebased.actions.PublishAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryReadAction;

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
	 *         --import --url=http://localhost:8080/pentaho --username=admin --password=password --source=file-system --type=files --charset=UTF-8 --path=/public  --file-path="C:/pentahotraining/BootCamp Labs/Pilot Project/SteelWheels.csv" --permission=true --overwrite=true --retainOwnership=true --rest=true
	 */
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_HTML)
	public Response doPostImport(
			@FormDataParam("importDir") String uploadDir,
			@FormDataParam("fileUpload") InputStream fileIS,
			@FormDataParam("overwriteFile") String overwriteFile,
			@FormDataParam("overwriteAclPermissions") String overwriteAclPermissions,
			@FormDataParam("applyAclPermissions") String applyAclPermission,
			@FormDataParam("retainOwnership") String retainOwnership,
			@FormDataParam("charSet") String charSet,
			@FormDataParam("logLevel") String logLevel,
			@FormDataParam("fileUpload") FormDataContentDisposition fileInfo) {
	      IRepositoryImportLogger importLogger = null;
		    ByteArrayOutputStream importLoggerStream = new ByteArrayOutputStream();
			try {
				validateAccess();
				
				boolean overwriteFileFlag = ("false".equals(overwriteFile) ? false : true);
				boolean overwriteAclSettingsFlag = ("true".equals(overwriteAclPermissions) ? true : false);
				boolean applyAclSettingsFlag = ("true".equals(applyAclPermission) ? true : false);
				boolean retainOwnershipFlag = ("true".equals(retainOwnership) ? true : false);
				
				Level level = Level.toLevel(logLevel);
				ImportSession.getSession().setAclProperties(applyAclSettingsFlag, retainOwnershipFlag, overwriteAclSettingsFlag);
	
				RepositoryFileImportBundle.Builder bundleBuilder = new RepositoryFileImportBundle.Builder();
				bundleBuilder.input(fileIS);
				bundleBuilder.charSet(charSet == null?"UTF-8":charSet);
				bundleBuilder.hidden(false);
				bundleBuilder.path(uploadDir);
				bundleBuilder.overwriteFile(overwriteFileFlag);
				bundleBuilder.applyAclSettings(applyAclSettingsFlag);
				bundleBuilder.overwriteAclSettings(overwriteAclSettingsFlag);
				bundleBuilder.retainOwnership(retainOwnershipFlag);
				bundleBuilder.name(fileInfo.getFileName());
				IPlatformImportBundle bundle = bundleBuilder.build();
	
				NameBaseMimeResolver mimeResolver = PentahoSystem.get(NameBaseMimeResolver.class);
				bundleBuilder.mime(mimeResolver.resolveMimeForFileName(fileInfo.getFileName()));
	
				IPlatformImporter importer = PentahoSystem.get(IPlatformImporter.class);
				importLogger = importer.getRepositoryImportLogger();
				
				importLogger.startJob(importLoggerStream, uploadDir, level); 
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
			} finally {
				importLogger.endJob();
			}
			return Response.ok(importLoggerStream.toString(), MediaType.TEXT_HTML).build();
	}

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
