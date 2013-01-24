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
 * Copyright 2013 Pentaho Corporation.  All rights reserved.
 *
 *
 * @created 1/14/2011
 * @author Aaron Phillips
 *
 */
package org.pentaho.platform.web.http.api.resources;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
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
import org.pentaho.platform.plugin.services.importexport.DefaultExportHandler;
import org.pentaho.platform.plugin.services.importexport.SimpleExportProcessor;
import org.pentaho.platform.plugin.services.importexport.ZipExportProcessor;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileInputStream;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileOutputStream;
import org.pentaho.platform.repository2.unified.jcr.PentahoJcrConstants;
import org.pentaho.platform.repository2.unified.webservices.*;
import org.pentaho.platform.web.http.messages.Messages;
import org.pentaho.reporting.libraries.libsparklines.util.StringUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.pentaho.platform.plugin.services.importexport.BaseExportProcessor;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.Response.Status.*;

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

  public static final String PATH_SEPARATOR = "/"; //$NON-NLS-1$

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
    if (pathId.contains(PATH_SEPARATOR)) {
      logger.warn(Messages.getInstance().getString("FileResource.ILLEGAL_PATHID", pathId)); //$NON-NLS-1$
    }
    path = pathId.replaceAll(PATH_SEPARATOR, ""); //$NON-NLS-1$
    path = path.replace(':', '/');
    if (!path.startsWith(PATH_SEPARATOR)) {
      path = PATH_SEPARATOR + path;
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
            String sourcePath = sourceFile.getPath().substring(0, sourceFile.getPath().lastIndexOf(PATH_SEPARATOR));
            if (!sourcePath.equals(destDir.getPath())) { // We're saving to a different folder than we're copying from
              IRepositoryFileData data = repository.getDataForRead(sourceFileId, SimpleRepositoryFileData.class);
              RepositoryFileAcl acl = repository.getAcl(sourceFileId);
              RepositoryFile destFile = repository.getFile(destDir.getPath() + PATH_SEPARATOR + fileName);
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
            String sourcePath = sourceFile.getPath().substring(0, sourceFile.getPath().lastIndexOf(PATH_SEPARATOR));
            RepositoryFileDto testFile = repoWs.getFile(path + PATH_SEPARATOR + nameNoExtension + Messages.getInstance().getString("FileResource.COPY_PREFIX") + extension); //$NON-NLS-1$
            if (sourcePath.equals(destDir.getPath()) && !nameNoExtension.endsWith(Messages.getInstance().getString("FileResource.COPY_PREFIX")) && testFile == null) { // We're trying to save to the same folder we copied from //$NON-NLS-1$
              fileName = nameNoExtension + Messages.getInstance().getString("FileResource.COPY_PREFIX") + extension;  //$NON-NLS-1$
            } else { // We're saving to a different folder than we're copying from or we've already copied here before
              if (testFile != null) {
                nameNoExtension = testFile.getName().substring(0, testFile.getName().lastIndexOf('.'));
              }
              testFile = repoWs.getFile(path + PATH_SEPARATOR + fileName);
              String testFileName = null;
              Integer nameCount = 1;
              while (testFile != null) {
                nameCount++;
                testFileName = nameNoExtension + Messages.getInstance().getString("FileResource.DUPLICATE_INDICATOR", nameCount) + extension;  //$NON-NLS-1$
                testFile = repoWs.getFile(path + PATH_SEPARATOR + testFileName);
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

  /**
   * Overloaded this method to try and reduce calls to the repository
   * @param pathId
   * @return
   * @throws FileNotFoundException
   */
  @GET
  @Path("{pathId : .+}")
  @Produces({WILDCARD})
  public Response doGetFileOrDir(@PathParam("pathId") String pathId) throws FileNotFoundException {
    String path = idToPath(pathId);

    if(!isPathValid(path)){
      return Response.status(FORBIDDEN).build();
    }

    RepositoryFile repoFile = repository.getFile(path);

    if (repoFile == null) {
      //file does not exist or is not readable but we can't tell at this point
      return Response.status(NOT_FOUND).build();
    }

    return doGetFileOrDir(repoFile);
  }

  /**
   * Overloaded this method to try and reduce calls to the repository
   * @param repoFile
   * @return
   * @throws FileNotFoundException
   */
  public Response doGetFileOrDir(RepositoryFile repoFile) throws FileNotFoundException{
    final RepositoryFileInputStream is = new RepositoryFileInputStream(repoFile);
    StreamingOutput streamingOutput = new StreamingOutput() {
      public void write(OutputStream output) throws IOException {
        IOUtils.copy(is, output);
      }
    };
    return Response.ok(streamingOutput, is.getMimeType()).build();
  }


  // Overloaded this method to try and minimize calls to the repo
  //Had to unmap this method since browsers ask for resources with Accepts="*/*" which will default to this method
//  @GET
//  @Path("{pathId : .+}")
//  @Produces({ APPLICATION_ZIP })
  public Response doGetDirAsZip(@PathParam("pathId") String pathId) {
    String path = idToPath(pathId);

    if(!isPathValid(path)){
      return Response.status(FORBIDDEN).build();
    }

    RepositoryFile repoFile = repository.getFile(path);

    if (repoFile == null) {
      //file does not exist or is not readable but we can't tell at this point
      return Response.status(NOT_FOUND).build();
    }

    return doGetDirAsZip(repoFile);
  }


  /**
   *
   * @param repositoryFile
   * @return
   */
  public Response doGetDirAsZip(RepositoryFile repositoryFile) {

    String path = repositoryFile.getPath();

    final InputStream is;
    StreamingOutput streamingOutput = null;

    try {
      org.pentaho.platform.plugin.services.importexport.Exporter exporter = new org.pentaho.platform.plugin.services.importexport.Exporter(repository);
      exporter.setRepoPath(path);
      exporter.setRepoWs(repoWs);

      File zipFile = exporter.doExportAsZip(repositoryFile);
      is = new FileInputStream(zipFile);
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

          // exclude all parameters that are of type "system", xactions set system params that have to be ignored.
          @SuppressWarnings("rawtypes")
          List nodes = document.selectNodes("parameters/parameter");
          for (int i = 0; i < nodes.size() && !hasParameters; i++) {
            Element elem = (Element)nodes.get(i);
            Element attrib = (Element)elem.selectSingleNode("attribute[@namespace='http://reporting.pentaho.org/namespaces/engine/parameter-attributes/core' and @name='role']");
            if (attrib == null || !"system".equals(attrib.attributeValue("value"))) {
              hasParameters = true;
            }
          }
        }
      }
    } catch (Exception e) {
      logger.error(Messages.getInstance().getString("FileResource.PARAM_FAILURE", e.getMessage()), e); //$NON-NLS-1$
    }
    return Boolean.toString(hasParameterUi && hasParameters);
  }

  /**
   * Compatibility endpoint for browsers since you can't specify Accepts headers in browsers
   * Added path param withManifest to indicate that manifest containing ACL and metadata
   * should be included
   *
   * @param pathId
   * @return
   * @throws FileNotFoundException
   */
  @GET
  @Path("{pathId : .+}/download")
  @Produces(WILDCARD)
  //have to accept anything for browsers to work
  public Response doGetFileOrDirAsDownload(@PathParam("pathId") String pathId, @QueryParam("withManifest") String strWithManifest) throws FileNotFoundException {
    String quotedFileName = null;

    Response origResponse = null;

    // send zip with manifest by default
    boolean withManifest = "false".equals(strWithManifest)?false:true;

    // change file id to path
    String path = idToPath(pathId);

    // if no path is sent, return bad request
    if(StringUtils.isEmpty(pathId)){
      return Response.status(BAD_REQUEST).build();
    }

    // check if path is valid
    if(!isPathValid(path)){
      return Response.status(FORBIDDEN).build();
    }

    // check if entity exists in repo
    RepositoryFile repositoryFile = repository.getFile(path);

    if (repositoryFile == null) {
      //file does not exist or is not readable but we can't tell at this point
      return Response.status(NOT_FOUND).build();
    }

    try{
      final InputStream is;
      StreamingOutput streamingOutput;
      Response response;
      BaseExportProcessor exportProcessor;

      // create processor
      if(repositoryFile.isFolder()||withManifest){
        exportProcessor = new ZipExportProcessor(path, this.repository, withManifest);
        quotedFileName = "\"" + repositoryFile.getName() + ".zip\""; //$NON-NLS-1$//$NON-NLS-2$
      }
      else{
        exportProcessor = new SimpleExportProcessor(path, this.repository, withManifest);
        quotedFileName = "\"" + repositoryFile.getName() + "\""; //$NON-NLS-1$//$NON-NLS-2$
      }

      // add export handlers for each expected file type
      exportProcessor.addExportHandler(new DefaultExportHandler(this.repository));

      File zipFile = exportProcessor.performExport(repositoryFile);
      is = new FileInputStream(zipFile);

      // copy streaming output
      streamingOutput = new StreamingOutput() {
        public void write(OutputStream output) throws IOException {
          IOUtils.copy(is, output);
        }
      };

      // create response

      response = Response.ok(streamingOutput, APPLICATION_ZIP).header("Content-Disposition", "attachment; filename=" + quotedFileName).build();

      return response;
    }
    catch(Exception e){
      logger.error(Messages.getInstance().getString("FileResource.EXPORT_FAILED", quotedFileName + " " + e.getMessage()), e); //$NON-NLS-1$
      return Response.status(INTERNAL_SERVER_ERROR).build();
    }

  }

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
    return repoWs.getFile(PATH_SEPARATOR);
  }

  @GET
  @Path("/canAdminister")
  @Produces(TEXT_PLAIN)
  public String doGetCanAdminister() {
    return policy.isAllowed(IAuthorizationPolicy.READ_REPOSITORY_CONTENT_ACTION) && policy.isAllowed(IAuthorizationPolicy.CREATE_REPOSITORY_CONTENT_ACTION)
        && policy.isAllowed(IAuthorizationPolicy.ADMINISTER_SECURITY_ACTION) ? "true" : "false"; //$NON-NLS-1$//$NON-NLS-2$
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
    return doGetChildren(PATH_SEPARATOR, depth, filter, showHidden);
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
    if (pathId == null || pathId.equals(PATH_SEPARATOR)) {
      path = PATH_SEPARATOR;
    } else {
      if (!pathId.startsWith(PATH_SEPARATOR)) {
        path = idToPath(pathId);
      }
    }
    if (showHidden == null) {
      showHidden = Boolean.FALSE;
    }
    
    List<RepositoryFileTreeDto> filteredChildren = new ArrayList();
    RepositoryFileTreeDto tree = repoWs.getTree(path, depth, filter, showHidden.booleanValue());
    for(RepositoryFileTreeDto child : tree.getChildren()) {
  	  RepositoryFileDto file = child.getFile();
  	  Map<String, Serializable> fileMeta = repository.getFileMetadata(file.getId());
  	  boolean isSystemFolder = fileMeta.containsKey(IUnifiedRepository.SYSTEM_FOLDER) ? (Boolean) fileMeta.get(IUnifiedRepository.SYSTEM_FOLDER) : false;
  	  if(!isSystemFolder) {
  		  filteredChildren.add(child);
  	  }
    }
    tree.setChildren(filteredChildren);
    return tree;
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
    if (pathId == null || pathId.equals(PATH_SEPARATOR)) {
      path = PATH_SEPARATOR;
    } else {
      if (!pathId.startsWith(PATH_SEPARATOR)) {
        path = idToPath(pathId);
      }
    }
    final RepositoryFileDto file = repoWs.getFile(path);
    if (file != null) {
      return repoWs.getFileMetadata(file.getId());
    }
    return null;
  }

  /**
   * Validate path and send appropriate response if necessary
   * TODO: Add validation to IUnifiedRepository interface
   * @param path
   * @return
   */
  private boolean isPathValid(String path){
    if(path.startsWith("/etc")||path.startsWith("/system")) {
      return false;
    }
    return true;
  }
}
