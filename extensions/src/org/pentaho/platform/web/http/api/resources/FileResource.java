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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileInputStream;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileOutputStream;
import org.pentaho.platform.repository2.unified.importexport.Exporter;
import org.pentaho.platform.repository2.unified.jcr.PentahoJcrConstants;
import org.pentaho.platform.repository2.unified.webservices.DefaultUnifiedRepositoryWebService;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclAceDto;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclDto;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileTreeDto;
import org.pentaho.platform.repository2.unified.webservices.StringKeyStringValueDto;
import org.pentaho.platform.web.http.messages.Messages;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.MediaType.WILDCARD;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

/**
 * Represents a file node in the repository.  This api provides methods for discovering information
 * about repository files as well as CRUD operations
 *
 * @author aaron
 */
@Path("/repo/files/")
public class FileResource extends AbstractJaxRSResource {
  private static final Integer MODE_OVERWRITE = 1;
  private static final Integer MODE_RENAME = 2;
  private static final Integer MODE_NO_OVERWRITE = 3;

  public static final String PATH_SEPERATOR = "/"; //$NON-NLS-1$

  private static final String ACTION_READ = "org.pentaho.repository.read"; //$NON-NLS-1$

  private static final String ACTION_CREATE = "org.pentaho.repository.create"; //$NON-NLS-1$

  private static final String ACTION_ADMINISTER_SECURITY = "org.pentaho.security.administerSecurity"; //$NON-NLS-1$

  public static final String APPLICATION_ZIP = "application/zip"; //$NON-NLS-1$

  private static final Log logger = LogFactory.getLog(FileResource.class);

  protected IUnifiedRepository repository;

  protected DefaultUnifiedRepositoryWebService repoWs;

  protected IAuthorizationPolicy policy;

  public FileResource() {
    repository = PentahoSystem.get(IUnifiedRepository.class);
    repoWs = new DefaultUnifiedRepositoryWebService();
    policy = PentahoSystem.get(IAuthorizationPolicy.class);
  }

  public FileResource(HttpServletResponse httpServletResponse) {
    this();
    this.httpServletResponse = httpServletResponse;
  }

  public static String idToPath(String pathId) {
    String path = null;
    //slashes in pathId are illegal.. we scrub them out so the file will not be found
    //if the pathId was given in slash separated format
    if (pathId.contains(PATH_SEPERATOR)) {
      logger.warn(Messages.getInstance().getString("FileResource.ILLEGAL_PATHID", pathId)); //$NON-NLS-1$
    }
    path = pathId.replaceAll(PATH_SEPERATOR, ""); //$NON-NLS-1$
    path = path.replace(':', '/');
    if (!path.startsWith(PATH_SEPERATOR)) {
      path = PATH_SEPERATOR + path;
    }
    return path;
  }

  /////////
  // DELETE
  @PUT
  @Path("/delete")
  @Consumes({WILDCARD})
  public Response doDeleteFiles(String params) {
    String[] sourceFileIds = params.split("[,]"); //$NON-NLS-1$
    try {
      for (int i = 0; i < sourceFileIds.length; i++) {
        repoWs.deleteFile(sourceFileIds[i], null);
      }
      return Response.ok().build();
    } catch (Throwable t) {
      t.printStackTrace();
      return Response.serverError().build();
    }
  }

  @PUT
  @Path("/deletepermanent")
  @Consumes({WILDCARD})
  public Response doDeleteFilesPermanent(String params) {
    String[] sourceFileIds = params.split("[,]"); //$NON-NLS-1$
    try {
      for (int i = 0; i < sourceFileIds.length; i++) {
        repoWs.deleteFileWithPermanentFlag(sourceFileIds[i], true, null);
      }
      return Response.ok().build();
    } catch (Throwable t) {
      t.printStackTrace();
      return Response.serverError().build();
    }
  }

  @PUT
  @Path("/restore")
  @Consumes({WILDCARD})
  public Response doRestore(String params) {
    String[] sourceFileIds = params.split("[,]"); //$NON-NLS-1$
    try {
      for (int i = 0; i < sourceFileIds.length; i++) {
        repoWs.undeleteFile(sourceFileIds[i], null);
      }
      return Response.ok().build();
    } catch (Throwable t) {
      t.printStackTrace();
      return Response.serverError().build();
    }
  }
  /////////
  // CREATE

  @PUT
  @Path("{pathId : .+}")
  @Consumes({WILDCARD})
  public Response createFile(@PathParam("pathId") String pathId, InputStream fileContents) throws IOException {
    RepositoryFileOutputStream rfos = new RepositoryFileOutputStream(idToPath(pathId));
    IOUtils.copy(fileContents, rfos);
    rfos.close();
    fileContents.close();
    return Response.ok().build();
  }

  @PUT
  @Path("{pathId : .+}/children")
  @Consumes({TEXT_PLAIN})
  public Response copyFiles(@PathParam("pathId") String pathId, @QueryParam("mode") Integer mode, String params) {
    if (mode == null) {
      mode = MODE_RENAME;
    }
    try {
      String path = idToPath(pathId);
      RepositoryFile destDir = repository.getFile(path);
      String[] sourceFileIds = params.split("[,]"); //$NON-NLS-1$
      if (mode == MODE_OVERWRITE || mode == MODE_NO_OVERWRITE) {
        for (String sourceFileId : sourceFileIds) {
          RepositoryFile sourceFile = repository.getFileById(sourceFileId);
          if (destDir != null && destDir.isFolder() && sourceFile != null && !sourceFile.isFolder()) {
            String fileName = sourceFile.getName();
            String sourcePath = sourceFile.getPath().substring(0, sourceFile.getPath().lastIndexOf(PATH_SEPERATOR));
            if (!sourcePath.equals(destDir.getPath())) { // We're saving to a different folder than we're copying from
              IRepositoryFileData data = repository.getDataForRead(sourceFileId, SimpleRepositoryFileData.class);
              RepositoryFileAcl acl = repository.getAcl(sourceFileId);
              RepositoryFile destFile = repository.getFile(destDir.getPath() + PATH_SEPERATOR + fileName);
              if (destFile == null) { // destFile doesn't exist so we'll create it.
                RepositoryFile duplicateFile = new RepositoryFile.Builder(fileName).build();
                repository.createFile(destDir.getId(), duplicateFile, data, acl, null);
              } else if (mode == MODE_OVERWRITE) { // destFile exists so check to see if we want to overwrite it.
                repository.updateFile(destFile, data, null);
                repository.updateAcl(acl);
              }
            }
          }
        }
      } else {
        for (String sourceFileId : sourceFileIds) {
          RepositoryFile sourceFile = repository.getFileById(sourceFileId);
          if (destDir != null && destDir.isFolder() && sourceFile != null && !sourceFile.isFolder()) {
            String fileName = sourceFile.getName();
            String nameNoExtension = fileName.substring(0, fileName.lastIndexOf('.'));
            String extension = fileName.substring(fileName.lastIndexOf('.'));
            String sourcePath = sourceFile.getPath().substring(0, sourceFile.getPath().lastIndexOf(PATH_SEPERATOR));
            RepositoryFileDto testFile = repoWs.getFile(path + PATH_SEPERATOR + nameNoExtension + Messages.getInstance().getString("FileResource.COPY_PREFIX") + extension); //$NON-NLS-1$
            if (sourcePath.equals(destDir.getPath()) && !nameNoExtension.endsWith(Messages.getInstance().getString("FileResource.COPY_PREFIX")) && testFile == null) { // We're trying to save to the same folder we copied from //$NON-NLS-1$
              fileName = nameNoExtension + Messages.getInstance().getString("FileResource.COPY_PREFIX") + extension;  //$NON-NLS-1$
            } else { // We're saving to a different folder than we're copying from or we've already copied here before
              if (testFile != null) {
                nameNoExtension = testFile.getName().substring(0, testFile.getName().lastIndexOf('.'));
              }
              testFile = repoWs.getFile(path + PATH_SEPERATOR + fileName);
              String testFileName = null;
              Integer nameCount = 1;
              while (testFile != null) {
                nameCount++;
                testFileName = nameNoExtension + Messages.getInstance().getString("FileResource.DUPLICATE_INDICATOR", nameCount) + extension;  //$NON-NLS-1$
                testFile = repoWs.getFile(path + PATH_SEPERATOR + testFileName);
              }
              if (nameCount > 1) {
                fileName = testFileName;
              }
            }
            IRepositoryFileData data = repository.getDataForRead(sourceFileId, SimpleRepositoryFileData.class);
            RepositoryFileAcl acl = repository.getAcl(sourceFileId);
            RepositoryFile duplicateFile = new RepositoryFile.Builder(fileName).build();
            repository.createFile(destDir.getId(), duplicateFile, data, acl, null);
          }
        }
      }
    } catch (Throwable t) {
      t.printStackTrace();
      return Response.serverError().build();
    }
    return Response.ok().build();
  }

  /////////
  // READ

  @GET
  @Path("{pathId : .+}")
  @Produces({WILDCARD})
  public Response doGetFileOrDir(@PathParam("pathId") String pathId) throws FileNotFoundException {
    String path = idToPath(pathId);
    if(path.startsWith("/etc")) {
    	return Response.status(Status.FORBIDDEN).build();
    }
    RepositoryFile repoFile = repository.getFile(path);
    if (repoFile == null) {
      //file does not exist or is not readable but we can't tell at this point
      return Response.status(NOT_FOUND).build();
    }

    final RepositoryFileInputStream is = new RepositoryFileInputStream(repoFile);
    StreamingOutput streamingOutput = new StreamingOutput() {
      public void write(OutputStream output) throws IOException {
        IOUtils.copy(is, output);
      }
    };
    return Response.ok(streamingOutput, is.getMimeType()).build();
  }

  //Had to unmap this method since browsers ask for resources with Accepts="*/*" which will default to this method
//  @GET
//  @Path("{pathId : .+}")
//  @Produces({ APPLICATION_ZIP })
  public Response doGetDirAsZip(@PathParam("pathId") String pathId) {
    String path = idToPath(pathId);
    RepositoryFile repoFile = repository.getFile(path);

    final InputStream is;
    StreamingOutput streamingOutput = null;
    try {
      if (repoFile.isFolder()) {
        Exporter exporter = new Exporter(repository);
        exporter.setRepoPath(path);
        File zipFile = exporter.doExportAsZip();
        is = new FileInputStream(zipFile);
      } else {
        //we cannot service a request for a file as application/zip
        logger.info(MessageFormat.format("Getting file [{0}] as a zip archive is not supported", pathId)); //$NON-NLS-1$
        return Response.status(Status.NOT_ACCEPTABLE).build();
      }
    } catch (Exception e) {
      return Response.serverError().entity(e.toString()).build();
    }
    streamingOutput = new StreamingOutput() {
      public void write(OutputStream output) throws IOException {
        IOUtils.copy(is, output);
      }
    };
    Response response = null;
    response = Response.ok(streamingOutput, APPLICATION_ZIP).build();
    return response;
  }

  @GET
  @Path("{pathId : .+}/parameterizable")
  @Produces(TEXT_PLAIN)
  //have to accept anything for browsers to work
  public String doIsParameterizable(@PathParam("pathId") String pathId) throws FileNotFoundException {
    boolean hasParameterUi = false;
    RepositoryFile repositoryFile = repository.getFile(FileResource.idToPath(pathId));
    if (repositoryFile != null) {
      try {
        hasParameterUi = (PentahoSystem.get(IPluginManager.class).getContentGenerator(repositoryFile.getName().substring(repositoryFile.getName().indexOf('.') + 1), "parameterUi") != null);
      } catch (NoSuchBeanDefinitionException e) {
        // Do nothing.
      }
    }
    boolean hasParameters = false;
    try {
      IContentGenerator parameterContentGenerator = PentahoSystem.get(IPluginManager.class).getContentGenerator(
          repositoryFile.getName().substring(repositoryFile.getName().indexOf('.') + 1), "parameter");
      if (parameterContentGenerator != null) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        parameterContentGenerator.setOutputHandler(new SimpleOutputHandler(outputStream, false));
        parameterContentGenerator.setMessagesList(new ArrayList<String>());
        Map<String, IParameterProvider> parameterProviders = new HashMap<String, IParameterProvider>();
        SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
        parameterProvider.setParameter("path", repositoryFile.getPath());
        parameterProviders.put(IParameterProvider.SCOPE_REQUEST, parameterProvider);
        parameterContentGenerator.setParameterProviders(parameterProviders);
        parameterContentGenerator.setSession(PentahoSessionHolder.getSession());
        parameterContentGenerator.createContent();
        if (outputStream.size() > 0) {
          Document document = DocumentHelper.parseText(outputStream.toString());
          hasParameters = document.selectNodes("/parameters/parameter").size() > 0;
        }
      }
    } catch (Exception e) {
      // TODO: handle exception
    }
    return Boolean.toString(hasParameterUi && hasParameters);
  }

  /**
   * Compatibility endpoint for browsers since you can't specify Accepts headers in browsers
   *
   * @throws FileNotFoundException
   */
  @GET
  @Path("{pathId : .+}/download")
  @Produces(WILDCARD)
  //have to accept anything for browsers to work
  public Response doGetFileOrDirAsDownload(@PathParam("pathId") String pathId) throws FileNotFoundException {
    String path = idToPath(pathId);
    String quotedFileName = null;

    RepositoryFile repoFile = repository.getFile(path);
    if (repoFile == null) {
      //file does not exist or is not readable but we can't tell at this point
      return Response.status(NOT_FOUND).build();
    }

    if (repoFile.isFolder()) {
      quotedFileName = "\"" + repoFile.getName() + ".zip\""; //$NON-NLS-1$//$NON-NLS-2$
      Response origResponse = doGetDirAsZip(pathId);
      return Response.fromResponse(origResponse)
          .header("Content-Disposition", "attachment; filename=" + quotedFileName).build(); //$NON-NLS-1$ //$NON-NLS-2$
    }

    quotedFileName = "\"" + repoFile.getName() + "\""; //$NON-NLS-1$ //$NON-NLS-2$
    Response origResponse = doGetFileOrDir(pathId);
    return Response.fromResponse(origResponse)
        .header("Content-Disposition", "attachment; filename=" + quotedFileName).build(); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /////////
  // UPDATE

  @PUT
  @Path("{pathId : .+}/acl")
  @Consumes({APPLICATION_XML, APPLICATION_JSON})
  public Response setFileAcls(@PathParam("pathId") String pathId, RepositoryFileAclDto acl) {
    RepositoryFileDto file = repoWs.getFile(idToPath(pathId));
    acl.setId(file.getId());
    repoWs.updateAcl(acl);
    return Response.ok().build();
  }

  @PUT
  @Path("{pathId : .+}/creator")
  @Consumes({APPLICATION_XML, APPLICATION_JSON})
  public Response setContentCreator(@PathParam("pathId") String pathId, RepositoryFileDto contentCreator) {
    try {
      RepositoryFileDto file = repoWs.getFile(idToPath(pathId));
      Map<String, Serializable> fileMetadata = repository.getFileMetadata(file.getId());
      fileMetadata.put(PentahoJcrConstants.PHO_CONTENTCREATOR, contentCreator.getId());
      repository.setFileMetadata(file.getId(), fileMetadata);
      return Response.ok().build();
    } catch (Throwable t) {
      return Response.serverError().build();
    }
  }

  /////////
  // PROPERTIES

  @GET
  @Path("/properties")
  @Produces({APPLICATION_XML, APPLICATION_JSON})
  public RepositoryFileDto doGetRootProperties() {
    return repoWs.getFile(PATH_SEPERATOR);
  }

  @GET
  @Path("/canAdminister")
  @Produces(TEXT_PLAIN)
  public String doGetCanAdminister() {
    return policy.isAllowed(ACTION_READ) && policy.isAllowed(ACTION_CREATE)
        && policy.isAllowed(ACTION_ADMINISTER_SECURITY) ? "true" : "false"; //$NON-NLS-1$//$NON-NLS-2$
  }

  @GET
  @Path("{pathId : .+}/acl")
  @Produces({APPLICATION_XML, APPLICATION_JSON})
  public RepositoryFileAclDto doGetFileAcl(@PathParam("pathId") String pathId) {
    RepositoryFileDto file = repoWs.getFile(idToPath(pathId));
    RepositoryFileAclDto fileAcl = repoWs.getAcl(file.getId());
    if (fileAcl.isEntriesInheriting()) {
      List<RepositoryFileAclAceDto> aces = repoWs.getEffectiveAces(file.getId());
      fileAcl.setAces(aces, fileAcl.isEntriesInheriting());
    }
    return fileAcl;
  }

  @GET
  @Path("{pathId : .+}/properties")
  @Produces({APPLICATION_XML, APPLICATION_JSON})
  public RepositoryFileDto doGetProperties(@PathParam("pathId") String pathId) {
    return repoWs.getFile(idToPath(pathId));
  }

  @GET
  @Path("{pathId : .+}/creator")
  @Produces({APPLICATION_XML, APPLICATION_JSON})
  public RepositoryFileDto doGetContentCreator(@PathParam("pathId") String pathId) {
    try {
      RepositoryFileDto file = repoWs.getFile(idToPath(pathId));
      Map<String, Serializable> fileMetadata = repository.getFileMetadata(file.getId());
      String creatorId = (String) fileMetadata.get(PentahoJcrConstants.PHO_CONTENTCREATOR);
      if (creatorId != null && creatorId.length() > 0) {
        return repoWs.getFileById(creatorId);
      } else {
        return null;
      }
    } catch (Throwable t) {
      return null;
    }
  }

  @GET
  @Path("{pathId : .+}/generatedcontent")
  @Produces({APPLICATION_XML, APPLICATION_JSON})
  public List<RepositoryFileDto> doGetGeneratedContent(@PathParam("pathId") String pathId) {
    RepositoryFileDto targetFile = doGetProperties(pathId);
    List<RepositoryFileDto> content = new ArrayList<RepositoryFileDto>();
    if (targetFile != null) {
      String targetFileId = targetFile.getId();
      SessionResource sessionResource = new SessionResource();
      RepositoryFileDto workspaceFolder = repoWs.getFile(sessionResource.doGetCurrentUserDir());
      if (workspaceFolder != null) {
        List<RepositoryFileDto> children = repoWs.getChildren(workspaceFolder.getId());
        for (RepositoryFileDto child : children) {
          if (!child.isFolder()) {
            Map<String, Serializable> fileMetadata = repository.getFileMetadata(child.getId());
            String creatorId = (String) fileMetadata.get(PentahoJcrConstants.PHO_CONTENTCREATOR);
            if (creatorId != null && creatorId.equals(targetFileId)) {
              content.add(child);
            }
          }
        }
      }
    }
    return content;
  }


  /////////
  // BROWSE

  @GET
  @Path("/children")
  @Produces({APPLICATION_XML, APPLICATION_JSON})
  public RepositoryFileTreeDto doGetRootChildren(@QueryParam("depth") Integer depth, @QueryParam("filter") String filter, @QueryParam("showHidden") Boolean showHidden) {
    return doGetChildren(PATH_SEPERATOR, depth, filter, showHidden);
  }

  @GET
  @Path("{pathId : .+}/children")
  @Produces({APPLICATION_XML, APPLICATION_JSON})
  public RepositoryFileTreeDto doGetChildren(@PathParam("pathId") String pathId, @QueryParam("depth") Integer depth,
                                             @QueryParam("filter") String filter, @QueryParam("showHidden") Boolean showHidden) {

    String path = null;
    if (filter == null) {
      filter = "*"; //$NON-NLS-1$
    }
    if (depth == null) {
      depth = -1; //search all
    }
    if (pathId == null || pathId.equals(PATH_SEPERATOR)) {
      path = PATH_SEPERATOR;
    } else {
      if (!pathId.startsWith(PATH_SEPERATOR)) {
        path = idToPath(pathId);
      }
    }
    if (showHidden == null) {
      showHidden = Boolean.FALSE;
    }
    return repoWs.getTree(path, depth, filter, showHidden.booleanValue());
  }

  @GET
  @Path("/deleted")
  @Produces({APPLICATION_XML, APPLICATION_JSON})
  public List<RepositoryFileDto> doGetDeletedFiles() {
    return repoWs.getDeletedFiles();
  }

  @GET
  @Path("{pathId : .+}/metadata")
  @Produces({APPLICATION_XML, APPLICATION_JSON})
  public List<StringKeyStringValueDto> doGetMetadata(@PathParam("pathId") String pathId) {

    String path = null;
    if (pathId == null || pathId.equals(PATH_SEPERATOR)) {
      path = PATH_SEPERATOR;
    } else {
      if (!pathId.startsWith(PATH_SEPERATOR)) {
        path = idToPath(pathId);
      }
    }
    final RepositoryFileDto file = repoWs.getFile(path);
    if (file != null) {
      return repoWs.getFileMetadata(file.getId());
    }
    return null;
  }
}
