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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipInputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.web.http.messages.Messages;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

@Path("/repo/files/import")
public class RepositoryImportResource {

	private IUnifiedRepository repository;

	public RepositoryImportResource() {
		repository = PentahoSystem.get(IUnifiedRepository.class);
	}

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
	 */
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_HTML)
	public Response doPostImport(@FormDataParam("importDir") String uploadDir,
			@FormDataParam("fileUpload") InputStream fileIS,
			@FormDataParam("fileUpload") FormDataContentDisposition fileInfo) {
		try {
			validateAccess();
			final org.pentaho.platform.plugin.services.importexport.ImportProcessor importProcessor = new org.pentaho.platform.plugin.services.importexport.SimpleImportProcessor(
					uploadDir, null);
			// TODO - create a SolutionRepositoryImportHandler which delegates
			// to these three automatically
			importProcessor
					.addImportHandler(new org.pentaho.platform.plugin.services.importexport.MondrianImportHandler(
							repository));
			importProcessor
					.addImportHandler(new org.pentaho.platform.plugin.services.importexport.MetadataImportHandler(
							repository));
			importProcessor
					.addImportHandler(new org.pentaho.platform.plugin.services.importexport.DefaultImportHandler(
							repository));
			if (fileInfo.getFileName().toLowerCase().endsWith(".zip")) {
				importProcessor
						.setImportSource(new org.pentaho.platform.plugin.services.importexport.legacy.ZipSolutionRepositoryImportSource(
								new ZipInputStream(fileIS), "UTF-8"));
			} else {
				final File outFile = File.createTempFile("import", null);
				convertInputStreamToFile(outFile, fileIS);
				outFile.deleteOnExit();
				importProcessor
						.setImportSource(new org.pentaho.platform.plugin.services.importexport.legacy.FileSolutionRepositoryImportSource(
								outFile, fileInfo.getFileName(), "UTF-8"));
			}
			importProcessor.performImport();

			// Flush the Mondrian cache to show imported datasources.
			IMondrianCatalogService mondrianCatalogService = PentahoSystem.get(
					IMondrianCatalogService.class, "IMondrianCatalogService",
					PentahoSessionHolder.getSession());
			mondrianCatalogService.reInit(PentahoSessionHolder.getSession());
		} catch (org.pentaho.platform.plugin.services.importexport.ImportException e) {
			return Response.serverError().entity(e.toString()).build();
		} catch (org.pentaho.platform.plugin.services.importexport.InitializationException e) {
			return Response.serverError().entity(e.toString()).build();
		} catch (IOException e) {
			return Response.serverError().entity(e.toString()).build();
		} catch (PentahoAccessControlException e) {
			return Response.serverError().entity(e.toString()).build();
		}

		return Response
				.ok(Messages.getInstance().getString(
						"FileResource.IMPORT_SUCCESS")).build();
	}

	/**
	 * Take the InputStream and convert to Output Stream
	 * @param outFile
	 * @param fileIS
	 * @throws IOException
	 */
	private void convertInputStreamToFile(File outFile, InputStream fileIS)
			throws IOException {		
		OutputStream out = new FileOutputStream(outFile);

		int read = 0;
		byte[] bytes = new byte[1024];

		while ((read = fileIS.read(bytes)) != -1) {
			out.write(bytes, 0, read);
		}
		fileIS.close();
		out.flush();
		out.close();	
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
