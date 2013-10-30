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

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.MediaType.WILDCARD;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
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
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importexport.BaseExportProcessor;
import org.pentaho.platform.plugin.services.importexport.DefaultExportHandler;
import org.pentaho.platform.plugin.services.importexport.SimpleExportProcessor;
import org.pentaho.platform.plugin.services.importexport.ZipExportProcessor;
import org.pentaho.platform.repository.RepositoryDownloadWhitelist;
import org.pentaho.platform.repository2.locale.PentahoLocale;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileInputStream;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileOutputStream;
import org.pentaho.platform.repository2.unified.jcr.PentahoJcrConstants;
import org.pentaho.platform.repository2.unified.webservices.DefaultUnifiedRepositoryWebService;
import org.pentaho.platform.repository2.unified.webservices.LocaleMapDto;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclAceDto;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclDto;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAdapter;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileTreeDto;
import org.pentaho.platform.repository2.unified.webservices.StringKeyStringValueDto;
import org.pentaho.platform.scheduler2.quartz.QuartzScheduler;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.policy.rolebased.actions.PublishAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryReadAction;
import org.pentaho.platform.web.http.messages.Messages;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

/**
 * Represents a file node in the getRepository(). This api provides methods for discovering information about repository files as well as CRUD operations
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

  protected RepositoryDownloadWhitelist whitelist;

  protected static IUnifiedRepository repository;

  protected static DefaultUnifiedRepositoryWebService repoWs;

  protected static IAuthorizationPolicy policy;

  public FileResource() {
  }

  public FileResource(HttpServletResponse httpServletResponse) {
    this();
    this.httpServletResponse = httpServletResponse;
  }

  public static String idToPath(String pathId) {
    String path = null;
    // slashes in pathId are illegal.. we scrub them out so the file will not be found
    // if the pathId was given in slash separated format
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

  // ///////
  // DELETE
  @PUT
  @Path("/delete")
  @Consumes({ WILDCARD })
  public Response doDeleteFiles(String params) {
    String[] sourceFileIds = params.split("[,]"); //$NON-NLS-1$
    try {
      for (int i = 0; i < sourceFileIds.length; i++) {
        getRepoWs().deleteFile(sourceFileIds[i], null);
      }
      return Response.ok().build();
    } catch (Throwable t) {
      t.printStackTrace();
      return Response.serverError().build();
    }
  }

  @PUT
  @Path("/deletepermanent")
  @Consumes({ WILDCARD })
  public Response doDeleteFilesPermanent(String params) {
    String[] sourceFileIds = params.split("[,]"); //$NON-NLS-1$
    try {
      for (int i = 0; i < sourceFileIds.length; i++) {
        getRepoWs().deleteFileWithPermanentFlag(sourceFileIds[i], true, null);
      }
      return Response.ok().build();
    } catch (Throwable t) {
      t.printStackTrace();
      return Response.serverError().build();
    }
  }

  @PUT
  @Path("/restore")
  @Consumes({ WILDCARD })
  public Response doRestore(String params) {
    String[] sourceFileIds = params.split("[,]"); //$NON-NLS-1$
    try {
      for (int i = 0; i < sourceFileIds.length; i++) {
        getRepoWs().undeleteFile(sourceFileIds[i], null);
      }
      return Response.ok().build();
    } catch (Throwable t) {
      t.printStackTrace();
      return Response.serverError().build();
    }
  }

  // ///////
  // CREATE

  @PUT
  @Path("{pathId : .+}")
  @Consumes({ WILDCARD })
  public Response createFile(@PathParam("pathId") String pathId, InputStream fileContents) throws IOException {
    RepositoryFileOutputStream rfos = new RepositoryFileOutputStream(idToPath(pathId));
    rfos.setCharsetName(httpServletRequest.getCharacterEncoding());
    IOUtils.copy(fileContents, rfos);
    rfos.close();
    fileContents.close();
    return Response.ok().build();
  }

  @PUT
  @Path("{pathId : .+}/children")
  @Consumes({ TEXT_PLAIN })
  public Response copyFiles(@PathParam("pathId") String pathId, @QueryParam("mode") Integer mode, String params) {
    if (mode == null) {
      mode = MODE_RENAME;
    }
    try {
      String path = idToPath(pathId);
      RepositoryFile destDir = getRepository().getFile(path);
      String[] sourceFileIds = params.split("[,]"); //$NON-NLS-1$
      if (mode == MODE_OVERWRITE || mode == MODE_NO_OVERWRITE) {
        for (String sourceFileId : sourceFileIds) {
          RepositoryFile sourceFile = getRepository().getFileById(sourceFileId);
          if (destDir != null && destDir.isFolder() && sourceFile != null && !sourceFile.isFolder()) {
            String fileName = sourceFile.getName();
            String sourcePath = sourceFile.getPath().substring(0, sourceFile.getPath().lastIndexOf(PATH_SEPARATOR));
            if (!sourcePath.equals(destDir.getPath())) { // We're saving to a different folder than we're copying from
              IRepositoryFileData data = getRepository().getDataForRead(sourceFileId, SimpleRepositoryFileData.class);
              RepositoryFileAcl acl = getRepository().getAcl(sourceFileId);
              RepositoryFile destFile = getRepository().getFile(destDir.getPath() + PATH_SEPARATOR + fileName);
              if (destFile == null) { // destFile doesn't exist so we'll create it.
                RepositoryFile duplicateFile = new RepositoryFile.Builder(fileName).build();
                getRepository().createFile(destDir.getId(), duplicateFile, data, acl, null);
              } else if (mode == MODE_OVERWRITE) { // destFile exists so check to see if we want to overwrite it.
                getRepository().updateFile(destFile, data, null);
                getRepository().updateAcl(acl);
              }
            }
          }
        }
      } else {
        for (String sourceFileId : sourceFileIds) {
          RepositoryFile sourceFile = getRepository().getFileById(sourceFileId);
          if (destDir != null && destDir.isFolder() && sourceFile != null && !sourceFile.isFolder()) {

            // First try to see if regular name is available
            String fileName = sourceFile.getName();
            String copyText = "";
            String rootCopyText = "";
            String nameNoExtension = fileName.substring(0, fileName.lastIndexOf('.'));
            String extension = fileName.substring(fileName.lastIndexOf('.'));

            RepositoryFileDto testFile = getRepoWs().getFile(path + PATH_SEPARATOR + nameNoExtension + extension); //$NON-NLS-1$
            if (testFile != null) {
              // Second try COPY_PREFIX, If the name already ends with a COPY_PREFIX don't append twice
              if (!nameNoExtension.endsWith(Messages.getInstance().getString("FileResource.COPY_PREFIX"))) { //$NON-NLS-1$
                copyText = rootCopyText = Messages.getInstance().getString("FileResource.COPY_PREFIX");
                fileName = nameNoExtension + copyText + extension;
                testFile = getRepoWs().getFile(path + PATH_SEPARATOR + fileName);
              }
            }

            // Third try COPY_PREFIX + DUPLICATE_INDICATOR
            Integer nameCount = 1;
            while (testFile != null) {
              nameCount++;
              copyText = rootCopyText + Messages.getInstance().getString("FileResource.DUPLICATE_INDICATOR", nameCount);
              fileName = nameNoExtension + copyText + extension;
              testFile = getRepoWs().getFile(path + PATH_SEPARATOR + fileName);
            }
            IRepositoryFileData data = getRepository().getDataForRead(sourceFileId, SimpleRepositoryFileData.class);
            RepositoryFileAcl acl = getRepository().getAcl(sourceFileId);
            RepositoryFile duplicateFile = null;

            // If the title is different than the source file, copy it separately
            if (!sourceFile.getName().equals(sourceFile.getTitle())) {
              duplicateFile = new RepositoryFile.Builder(fileName).title(RepositoryFile.DEFAULT_LOCALE, sourceFile.getTitle() + copyText).build();
            } else {
              duplicateFile = new RepositoryFile.Builder(fileName).build();
            }

            getRepository().createFile(destDir.getId(), duplicateFile, data, acl, null);
          }
        }
      }
    } catch (Throwable t) {
      t.printStackTrace();
      return Response.serverError().build();
    }
    return Response.ok().build();
  }

  // ///////
  // READ

  /**
   * Overloaded this method to try and reduce calls to the repository
   * 
   * @param pathId
   * @return
   * @throws FileNotFoundException
   */
  @GET
  @Path("{pathId : .+}")
  @Produces({ WILDCARD })
  public Response doGetFileOrDir(@PathParam("pathId") String pathId) throws FileNotFoundException {
    String path = idToPath(pathId);

    if (!isPathValid(path)) {
      return Response.status(FORBIDDEN).build();
    }

    RepositoryFile repoFile = getRepository().getFile(path);

    if (repoFile == null) {
      // file does not exist or is not readable but we can't tell at this point
      return Response.status(NOT_FOUND).build();
    }

    // check whitelist acceptance of file (based on extension)
    if (getWhitelist().accept(repoFile.getName()) == false) {
      // if whitelist check fails, we can still inline if you have PublishAction, otherwise we're FORBIDDEN
      if (getPolicy().isAllowed(PublishAction.NAME) == false) {
        return Response.status(FORBIDDEN).build();
      }
    }
    
    return doGetFileOrDir(repoFile);
  }

  /**
   * Overloaded this method to try and reduce calls to the repository
   * 
   * @param repoFile
   * @return
   * @throws FileNotFoundException
   */
  public Response doGetFileOrDir(RepositoryFile repoFile) throws FileNotFoundException {
    final RepositoryFileInputStream is = new RepositoryFileInputStream(repoFile);
    StreamingOutput streamingOutput = new StreamingOutput() {
      public void write(OutputStream output) throws IOException {
        IOUtils.copy(is, output);
      }
    };
    return Response.ok(streamingOutput, is.getMimeType()).header("Content-Disposition", "inline; filename=\"" + repoFile.getName() + "\"").build();
  }

  // Overloaded this method to try and minimize calls to the repo
  // Had to unmap this method since browsers ask for resources with Accepts="*/*" which will default to this method
  // @GET
  // @Path("{pathId : .+}")
  // @Produces({ APPLICATION_ZIP })
  public Response doGetDirAsZip(@PathParam("pathId") String pathId) {
    String path = idToPath(pathId);

    if (!isPathValid(path)) {
      return Response.status(FORBIDDEN).build();
    }

    // you have to have PublishAction in order to get dir as zip
    if (getPolicy().isAllowed(PublishAction.NAME) == false) {
      return Response.status(FORBIDDEN).build();
    }
    
    RepositoryFile repoFile = getRepository().getFile(path);

    if (repoFile == null) {
      // file does not exist or is not readable but we can't tell at this point
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
  // have to accept anything for browsers to work
  public String doIsParameterizable(@PathParam("pathId") String pathId) throws FileNotFoundException {
    boolean hasParameterUi = false;
    RepositoryFile repositoryFile = getRepository().getFile(FileResource.idToPath(pathId));
    if (repositoryFile != null) {
      try {
        hasParameterUi = (PentahoSystem.get(IPluginManager.class).getContentGenerator(
            repositoryFile.getName().substring(repositoryFile.getName().indexOf('.') + 1), "parameterUi") != null);
      } catch (NoSuchBeanDefinitionException e) {
        // Do nothing.
      }
    }
    boolean hasParameters = false;
    if (hasParameterUi) {
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
          parameterProvider.setParameter("renderMode", "PARAMETER");
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
              Element elem = (Element) nodes.get(i);
              if (elem.attributeValue("name").equalsIgnoreCase("output-target") && elem.attributeValue("is-mandatory").equalsIgnoreCase("true")) {
                hasParameters = true;
                continue;
              }
              Element attrib = (Element) elem
                  .selectSingleNode("attribute[@namespace='http://reporting.pentaho.org/namespaces/engine/parameter-attributes/core' and @name='role']");
              if (attrib == null || !"system".equals(attrib.attributeValue("value"))) {
                hasParameters = true;
              }
            }
          }
        }
      } catch (Exception e) {
        logger.error(Messages.getInstance().getString("FileResource.PARAM_FAILURE", e.getMessage()), e); //$NON-NLS-1$
      }
    }
    return Boolean.toString(hasParameters);
  }

  /**
   * Compatibility endpoint for browsers since you can't specify Accepts headers in browsers Added path param withManifest to indicate that manifest containing
   * ACL and metadata should be included
   * 
   * @param pathId
   * @return
   * @throws FileNotFoundException
   */
  @GET
  @Path("{pathId : .+}/download")
  @Produces(WILDCARD)
  // have to accept anything for browsers to work
  public Response doGetFileOrDirAsDownload(@PathParam("pathId") String pathId, @QueryParam("withManifest") String strWithManifest) throws FileNotFoundException {
    
    // you have to have PublishAction in order to download
    if (getPolicy().isAllowed(PublishAction.NAME) == false) {
      return Response.status(FORBIDDEN).build();
    }
    
    String quotedFileName = null;

    // send zip with manifest by default
    boolean withManifest = "false".equals(strWithManifest) ? false : true;

    // change file id to path
    String path = idToPath(pathId);

    // if no path is sent, return bad request
    if (StringUtils.isEmpty(pathId)) {
      return Response.status(BAD_REQUEST).build();
    }

    // check if path is valid
    if (!isPathValid(path)) {
      return Response.status(FORBIDDEN).build();
    }

    // check if entity exists in repo
    RepositoryFile repositoryFile = getRepository().getFile(path);

    if (repositoryFile == null) {
      // file does not exist or is not readable but we can't tell at this point
      return Response.status(NOT_FOUND).build();
    }

    try {
      final InputStream is;
      StreamingOutput streamingOutput;
      Response response;
      BaseExportProcessor exportProcessor;

      // create processor
      if (repositoryFile.isFolder() || withManifest) {
        exportProcessor = new ZipExportProcessor(path, FileResource.repository, withManifest);
        quotedFileName = "\"" + repositoryFile.getName() + ".zip\""; //$NON-NLS-1$//$NON-NLS-2$
      } else {
        exportProcessor = new SimpleExportProcessor(path, FileResource.repository, withManifest);
        quotedFileName = "\"" + repositoryFile.getName() + "\""; //$NON-NLS-1$//$NON-NLS-2$
      }

      // add export handlers for each expected file type
      exportProcessor.addExportHandler(PentahoSystem.get(DefaultExportHandler.class));

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
    } catch (Exception e) {
      logger.error(Messages.getInstance().getString("FileResource.EXPORT_FAILED", quotedFileName + " " + e.getMessage()), e); //$NON-NLS-1$
      return Response.status(INTERNAL_SERVER_ERROR).build();
    }

  }

  @GET
  @Path("{pathId : .+}/inline")
  @Produces(WILDCARD)
  // have to accept anything for browsers to work
  public Response doGetFileAsInline(@PathParam("pathId") String pathId) throws FileNotFoundException {
  	String path = null;
  	RepositoryFile repositoryFile = null;
  	// Check if the path is actually and ID
  	if(isPath(pathId)) {
  		path = idToPath(pathId);
      if (!isPathValid(path)) {
        return Response.status(FORBIDDEN).build();
      }
      repositoryFile = getRepository().getFile(path);
  	} else {
  		// Yes path provided is an ID
  		repositoryFile = getRepository().getFileById(pathId);
  	}

    if (repositoryFile == null) {
        // file does not exist or is not readable but we can't tell at this point
        return Response.status(NOT_FOUND).build();
    }
    
    // check whitelist acceptance of file (based on extension)
    if (getWhitelist().accept(repositoryFile.getName()) == false) {
      // if whitelist check fails, we can still inline if you have PublishAction, otherwise we're FORBIDDEN
      if (getPolicy().isAllowed(PublishAction.NAME) == false) {
        return Response.status(FORBIDDEN).build();
      }
    }
    
    try {
      SimpleRepositoryFileData fileData = getRepository().getDataForRead(repositoryFile.getId(), SimpleRepositoryFileData.class);
      final InputStream is = fileData.getInputStream();

      StreamingOutput streamingOutput;
      Response response;

      // copy streaming output
      streamingOutput = new StreamingOutput() {
        public void write(OutputStream output) throws IOException {
          IOUtils.copy(is, output);
        }
      };

      // create response
      response = Response.ok(streamingOutput).header("Content-Disposition", "inline; filename=" + repositoryFile.getName()).build();

      return response;
    } catch (Exception e) {
      logger.error(Messages.getInstance().getString("FileResource.EXPORT_FAILED", repositoryFile.getName() + " " + e.getMessage()), e); //$NON-NLS-1$
      return Response.status(INTERNAL_SERVER_ERROR).build();
    }

  }

  @PUT
  @Path("{pathId : .+}/acl")
  @Consumes({ APPLICATION_XML, APPLICATION_JSON })
  public Response setFileAcls(@PathParam("pathId") String pathId, RepositoryFileAclDto acl) {
    RepositoryFileDto file = getRepoWs().getFile(idToPath(pathId));
    acl.setId(file.getId());
    getRepoWs().updateAcl(acl);
    return Response.ok().build();
  }

  @PUT
  @Path("{pathId : .+}/creator")
  @Consumes({ APPLICATION_XML, APPLICATION_JSON })
  public Response setContentCreator(@PathParam("pathId") String pathId, RepositoryFileDto contentCreator) {
    try {
      RepositoryFileDto file = getRepoWs().getFile(idToPath(pathId));
      Map<String, Serializable> fileMetadata = getRepository().getFileMetadata(file.getId());
      fileMetadata.put(PentahoJcrConstants.PHO_CONTENTCREATOR, contentCreator.getId());
      getRepository().setFileMetadata(file.getId(), fileMetadata);
      return Response.ok().build();
    } catch (Throwable t) {
      return Response.serverError().build();
    }
  }

  // ///////
  // LOCALES

  @GET
  @Path("{pathId : .+}/locales")
  @Produces({ APPLICATION_XML, APPLICATION_JSON })
  public List<LocaleMapDto> doGetFileLocales(@PathParam("pathId") String pathId) {
    List<LocaleMapDto> availableLocales = new ArrayList<LocaleMapDto>();
    RepositoryFileDto file = getRepoWs().getFile(idToPath(pathId));
    List<PentahoLocale> locales = getRepoWs().getAvailableLocalesForFileById(file.getId());
    if (locales != null && !locales.isEmpty()) {
      for (PentahoLocale locale : locales) {
        availableLocales.add(new LocaleMapDto(locale.toString(), null));
      }
    }
    return availableLocales;
  }

  @GET
  @Path("{pathId : .+}/localeProperties")
  @Produces({ APPLICATION_XML, APPLICATION_JSON })
  public List<StringKeyStringValueDto> doGetLocaleProperties(@PathParam("pathId") String pathId, @QueryParam("locale") String locale) {
    RepositoryFileDto file = getRepoWs().getFile(idToPath(pathId));
    List<StringKeyStringValueDto> keyValueList = new ArrayList<StringKeyStringValueDto>();
    if (file != null) {
      Properties properties = getRepoWs().getLocalePropertiesForFileById(file.getId(), locale);
      if (properties != null && !properties.isEmpty()) {
        for (String key : properties.stringPropertyNames()) {
          keyValueList.add(new StringKeyStringValueDto(key, properties.getProperty(key)));
        }
      }
    }
    return keyValueList;
  }

  @PUT
  @Path("{pathId : .+}/localeProperties")
  @Produces({ APPLICATION_XML, APPLICATION_JSON })
  public Response doSetLocaleProperties(@PathParam("pathId") String pathId, @QueryParam("locale") String locale, List<StringKeyStringValueDto> properties) {
    try {
      RepositoryFileDto file = getRepoWs().getFile(idToPath(pathId));
      Properties fileProperties = new Properties();
      if (properties != null && !properties.isEmpty()) {
        for (StringKeyStringValueDto dto : properties) {
          fileProperties.put(dto.getKey(), dto.getValue());
        }
      }
      getRepoWs().setLocalePropertiesForFileByFileId(file.getId(), locale, fileProperties);

      return Response.ok().build();
    } catch (Throwable t) {
      return Response.serverError().build();
    }
  }

  @PUT
  @Path("{pathId : .+}/deleteLocale")
  @Produces({ APPLICATION_XML, APPLICATION_JSON })
  public Response doDeleteLocale(@PathParam("pathId") String pathId, @QueryParam("locale") String locale) {
    try {
      RepositoryFileDto file = getRepoWs().getFile(idToPath(pathId));
      getRepoWs().deleteLocalePropertiesForFile(file.getId(), locale);

      return Response.ok().build();
    } catch (Throwable t) {
      return Response.serverError().build();
    }
  }

  // ///////
  // PROPERTIES

  @GET
  @Path("/properties")
  @Produces({ APPLICATION_XML, APPLICATION_JSON })
  public RepositoryFileDto doGetRootProperties() {
    return getRepoWs().getFile(PATH_SEPARATOR);
  }

  @GET
  @Path("{pathId : .+}/canAccessMap")
  @Produces({ APPLICATION_XML, APPLICATION_JSON })
  public List<Setting> doGetCanAccessList(@PathParam("pathId") String pathId, @QueryParam("permissions") String permissions) {
    StringTokenizer tokenizer = new StringTokenizer(permissions, "|");
    ArrayList<Setting> permMap = new ArrayList<Setting>();
    while (tokenizer.hasMoreTokens()) {
      Integer perm = Integer.valueOf(tokenizer.nextToken());
      EnumSet<RepositoryFilePermission> permission = EnumSet.of(RepositoryFilePermission.values()[perm]);
      permMap.add(new Setting(perm.toString(), new Boolean(getRepository().hasAccess(idToPath(pathId), permission)).toString()));
    }
    return permMap;
  }

  @GET
  @Path("{pathId : .+}/canAccess")
  @Produces(TEXT_PLAIN)
  public String doGetCanAccess(@PathParam("pathId") String pathId, @QueryParam("permissions") String permissions) {
    StringTokenizer tokenizer = new StringTokenizer(permissions, "|");
    List<Integer> permissionList = new ArrayList<Integer>();
    while (tokenizer.hasMoreTokens()) {
      Integer perm = Integer.valueOf(tokenizer.nextToken());
      switch (perm) {
      case 0: {
        permissionList.add(RepositoryFilePermission.READ.ordinal());
        break;
      }
      case 1: {
        permissionList.add(RepositoryFilePermission.WRITE.ordinal());
        break;
      }
      case 2: {
        permissionList.add(RepositoryFilePermission.DELETE.ordinal());
        break;
      }
      case 3: {
        permissionList.add(RepositoryFilePermission.ACL_MANAGEMENT.ordinal());
        break;
      }
      case 4: {
        permissionList.add(RepositoryFilePermission.ALL.ordinal());
        break;
      }
      }
    }
    return getRepoWs().hasAccess(idToPath(pathId), permissionList) ? "true" : "false";
  }

  @GET
  @Path("/canAdminister")
  @Produces(TEXT_PLAIN)
  public String doGetCanAdminister() {
    return getPolicy().isAllowed(RepositoryReadAction.NAME) && getPolicy().isAllowed(RepositoryCreateAction.NAME) && getPolicy().isAllowed(AdministerSecurityAction.NAME) ? "true" : "false"; //$NON-NLS-1$//$NON-NLS-2$
  }


    @GET
    @Path("/canCreate")
    @Produces(TEXT_PLAIN)
    public String doGetCanCreate() {
        return getPolicy().isAllowed(RepositoryCreateAction.NAME)? "true" : "false"; //$NON-NLS-1$//$NON-NLS-2$
  }

  @GET
  @Path("{pathId : .+}/acl")
  @Produces({ APPLICATION_XML, APPLICATION_JSON })
  public RepositoryFileAclDto doGetFileAcl(@PathParam("pathId") String pathId) {
    RepositoryFileDto file = getRepoWs().getFile(idToPath(pathId));
    RepositoryFileAclDto fileAcl = getRepoWs().getAcl(file.getId());
    if (fileAcl.isEntriesInheriting()) {
      List<RepositoryFileAclAceDto> aces = getRepoWs().getEffectiveAces(file.getId());
      fileAcl.setAces(aces, fileAcl.isEntriesInheriting());
    }
    return fileAcl;
  }

  @GET
  @Path("{pathId : .+}/properties")
  @Produces({ APPLICATION_XML, APPLICATION_JSON })
  public RepositoryFileDto doGetProperties(@PathParam("pathId") String pathId) {
    return getRepoWs().getFile(idToPath(pathId));
  }

  @GET
  @Path("{pathId : .+}/creator")
  @Produces({ APPLICATION_XML, APPLICATION_JSON })
  public RepositoryFileDto doGetContentCreator(@PathParam("pathId") String pathId) {
    try {
      RepositoryFileDto file = getRepoWs().getFile(idToPath(pathId));
      Map<String, Serializable> fileMetadata = getRepository().getFileMetadata(file.getId());
      String creatorId = (String) fileMetadata.get(PentahoJcrConstants.PHO_CONTENTCREATOR);
      if (creatorId != null && creatorId.length() > 0) {
        return getRepoWs().getFileById(creatorId);
      } else {
        return null;
      }
    } catch (Throwable t) {
      return null;
    }
  }

  @GET
  @Path("{pathId : .+}/generatedContent")
  @Produces({ APPLICATION_XML, APPLICATION_JSON })
  public List<RepositoryFileDto> doGetGeneratedContent(@PathParam("pathId") String pathId) {
    RepositoryFileDto targetFile = doGetProperties(pathId);
    List<RepositoryFileDto> content = new ArrayList<RepositoryFileDto>();
    if (targetFile != null) {
      String targetFileId = targetFile.getId();
      SessionResource sessionResource = new SessionResource();

      RepositoryFile workspaceFolder = getRepository().getFile(sessionResource.doGetCurrentUserDir());
      if (workspaceFolder != null) {
        List<RepositoryFile> children = getRepository().getChildren(workspaceFolder.getId());
        for (RepositoryFile child : children) {
          if (!child.isFolder()) {
            Map<String, Serializable> fileMetadata = getRepository().getFileMetadata(child.getId());
            String creatorId = (String) fileMetadata.get(PentahoJcrConstants.PHO_CONTENTCREATOR);
            if (creatorId != null && creatorId.equals(targetFileId)) {
              content.add(RepositoryFileAdapter.toFileDto(child));
            }
          }
        }
      }
    }
    return content;
  }

  @GET
  @Path("{pathId : .+}/generatedContentForUser")
  @Produces({ APPLICATION_XML, APPLICATION_JSON })
  public List<RepositoryFileDto> doGetGeneratedContentForUser(@PathParam("pathId") String pathId, @QueryParam("user") String user) {
    RepositoryFileDto targetFile = doGetProperties(pathId);
    List<RepositoryFileDto> content = new ArrayList<RepositoryFileDto>();
    if (targetFile != null) {
      String targetFileId = targetFile.getId();
      SessionResource sessionResource = new SessionResource();

      RepositoryFile workspaceFolder = getRepository().getFile(sessionResource.doGetUserDir(user));
      if (workspaceFolder != null) {
        List<RepositoryFile> children = getRepository().getChildren(workspaceFolder.getId());
        for (RepositoryFile child : children) {
          if (!child.isFolder()) {
            Map<String, Serializable> fileMetadata = getRepository().getFileMetadata(child.getId());
            String creatorId = (String) fileMetadata.get(PentahoJcrConstants.PHO_CONTENTCREATOR);
            if (creatorId != null && creatorId.equals(targetFileId)) {
              content.add(RepositoryFileAdapter.toFileDto(child));
            }
          }
        }
      }
    }
    return content;
  }

  @GET
  @Path("/generatedContentForSchedule")
  @Produces({ APPLICATION_XML, APPLICATION_JSON })
  public List<RepositoryFileDto> doGetGeneratedContentForSchedule(@QueryParam("lineageId") String lineageId) {
    List<RepositoryFileDto> content = new ArrayList<RepositoryFileDto>();
    SessionResource sessionResource = new SessionResource();
    RepositoryFile workspaceFolder = getRepository().getFile(sessionResource.doGetCurrentUserDir());
    if (workspaceFolder != null) {
      List<RepositoryFile> children = getRepository().getChildren(workspaceFolder.getId());
      for (RepositoryFile child : children) {
        if (!child.isFolder()) {
          Map<String, Serializable> fileMetadata = getRepository().getFileMetadata(child.getId());
          String lineageIdMeta = (String) fileMetadata.get(QuartzScheduler.RESERVEDMAPKEY_LINEAGE_ID);
          if (lineageIdMeta != null && lineageIdMeta.equals(lineageId)) {
            content.add(RepositoryFileAdapter.toFileDto(child));
          }
        }
      }
    }
    return content;
  }

  // ///////
  // BROWSE

  @GET
  @Path("/children")
  @Produces({ APPLICATION_XML, APPLICATION_JSON })
  public RepositoryFileTreeDto doGetRootChildren(@QueryParam("depth") Integer depth, @QueryParam("filter") String filter,
      @QueryParam("showHidden") Boolean showHidden) {
    return doGetChildren(PATH_SEPARATOR, depth, filter, showHidden);
  }

  @GET
  @Path("{pathId : .+}/children")
  @Produces({ APPLICATION_XML, APPLICATION_JSON })
  public RepositoryFileTreeDto doGetChildren(@PathParam("pathId") String pathId, @QueryParam("depth") Integer depth, @QueryParam("filter") String filter,
      @QueryParam("showHidden") Boolean showHidden) {

    String path = null;
    if (filter == null) {
      filter = "*"; //$NON-NLS-1$
    }
    if (depth == null) {
      depth = -1; // search all
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

    List<RepositoryFileTreeDto> filteredChildren = new ArrayList<RepositoryFileTreeDto>();
    RepositoryFileTreeDto tree = getRepoWs().getTree(path, depth, filter, showHidden.booleanValue());

    // BISERVER-9599 - Use special sort order
    Collator collator = Collator.getInstance(PentahoSessionHolder.getSession().getLocale());
    collator.setStrength(Collator.PRIMARY); // ignore case
    sortByLocaleTitle(collator, tree);

    for (RepositoryFileTreeDto child : tree.getChildren()) {
      RepositoryFileDto file = child.getFile();
      Map<String, Serializable> fileMeta = getRepository().getFileMetadata(file.getId());
      boolean isSystemFolder = fileMeta.containsKey(IUnifiedRepository.SYSTEM_FOLDER) ? (Boolean) fileMeta.get(IUnifiedRepository.SYSTEM_FOLDER) : false;
      if (!isSystemFolder) {
        filteredChildren.add(child);
      }
    }
    tree.setChildren(filteredChildren);
    return tree;
  }

  @GET
  @Path("/deleted")
  @Produces({ APPLICATION_XML, APPLICATION_JSON })
  public List<RepositoryFileDto> doGetDeletedFiles() {
    return getRepoWs().getDeletedFiles();
  }

  @GET
  @Path("{pathId : .+}/metadata")
  @Produces({ APPLICATION_JSON })
  public List<StringKeyStringValueDto> doGetMetadata(@PathParam("pathId") String pathId) {
    List<StringKeyStringValueDto> list = null;
    String path = null;
    if (pathId == null || pathId.equals(PATH_SEPARATOR)) {
      path = PATH_SEPARATOR;
    } else {
      if (!pathId.startsWith(PATH_SEPARATOR)) {
        path = idToPath(pathId);
      }
    }
    final RepositoryFileDto file = getRepoWs().getFile(path);
    if (file != null) {
      list = getRepoWs().getFileMetadata(file.getId());
    }
    if (list != null) {
      boolean hasSchedulable = false;
      for (StringKeyStringValueDto value : list) {
        if (value.getKey().equals("_PERM_SCHEDULABLE")) {
          hasSchedulable = true;
          break;
        }
      }
      if (!hasSchedulable) {
        StringKeyStringValueDto schedPerm = new StringKeyStringValueDto("_PERM_SCHEDULABLE", "true");
        list.add(schedPerm);
      }

      // check file object for hidden value and add it to the list
      list.add(new StringKeyStringValueDto("_PERM_HIDDEN", String.valueOf(file.isHidden())));
    }

    return list;
  }

  
  @PUT
  @Path("{pathId : .+}/rename")
  @Consumes({ WILDCARD })
  @Produces({ WILDCARD })
  public Response doRename(@PathParam("pathId") String pathId, @QueryParam("newName") String newName) {
    try {
      RepositoryFileDto fileToBeRenamed = getRepoWs().getFile(idToPath(pathId));
      StringBuilder buf = new StringBuilder(fileToBeRenamed.getPath().length());
      buf.append(getParentPath(fileToBeRenamed.getPath()));
      buf.append(RepositoryFile.SEPARATOR);
      buf.append(newName);
      String extension = getExtension(fileToBeRenamed.getName());
      if(extension != null) {
    	  buf.append(extension);  
      }
      getRepoWs().moveFile(fileToBeRenamed.getId(), buf.toString(), "Renaming the file");
      return Response.ok().build();
    } catch (Throwable t) {
    	 return processErrorResponse(t.getLocalizedMessage());
    }
  }
  
  private Response processErrorResponse(String errMessage) {
	    return Response.ok(errMessage).build();
  }
  
  private  String getParentPath(final String path) {
	    if (path == null) {
	      throw new IllegalArgumentException();
	    } else if (RepositoryFile.SEPARATOR.equals(path)) {
	      return null;
	    }
	    int lastSlashIndex = path.lastIndexOf(RepositoryFile.SEPARATOR);
	    if (lastSlashIndex == 0) {
	      return RepositoryFile.SEPARATOR;
	    } else if (lastSlashIndex > 0) {
	      return path.substring(0, lastSlashIndex);
	    } else {
	      throw new IllegalArgumentException();
	    }
  }
  
  private String getExtension(final String name) {
	  int startIndex = name.lastIndexOf('.');
	  if(startIndex >= 0) {
		  return name.substring(startIndex, name.length());  
	  }
	  return null;
  }
  /**
   * Even though the hidden flag is a property of the file node itself, and not the metadata child, it is considered metadata from PUC and is included in the
   * setMetadata call
   * 
   * @param pathId
   * @param metadata
   * @return
   */
  @PUT
  @Path("{pathId : .+}/metadata")
  @Produces({ APPLICATION_XML, APPLICATION_JSON })
  public Response doSetMetadata(@PathParam("pathId") String pathId, List<StringKeyStringValueDto> metadata) {
    try {

      RepositoryFileDto file = getRepoWs().getFile(idToPath(pathId));
      RepositoryFileAclDto fileAcl = getRepoWs().getAcl(file.getId());

      boolean canManage = PentahoSessionHolder.getSession().getName().equals(fileAcl.getOwner())
          || (getPolicy().isAllowed(RepositoryReadAction.NAME) && getPolicy().isAllowed(RepositoryCreateAction.NAME) && getPolicy().isAllowed(AdministerSecurityAction.NAME));

      if (!canManage) {

        if (fileAcl.isEntriesInheriting()) {
          List<RepositoryFileAclAceDto> aces = getRepoWs().getEffectiveAces(file.getId());
          fileAcl.setAces(aces, fileAcl.isEntriesInheriting());
        }

        for (int i = 0; i < fileAcl.getAces().size(); i++) {
          RepositoryFileAclAceDto acl = fileAcl.getAces().get(i);
          if (acl.getRecipient().equals(PentahoSessionHolder.getSession().getName())) {
            if (acl.getPermissions().contains(RepositoryFilePermission.ACL_MANAGEMENT.ordinal())
                || acl.getPermissions().contains(RepositoryFilePermission.ALL.ordinal())) {
              canManage = true;
              break;
            }
          }
        }
      }

      if (canManage) {
        Map<String, Serializable> fileMetadata = getRepository().getFileMetadata(file.getId());
        boolean isHidden = false;

        for (StringKeyStringValueDto nv : metadata) {
          // don't add hidden to the list because it is not actually part of the metadata node
          if ((nv.getKey().contentEquals("_PERM_HIDDEN"))) {
            isHidden = Boolean.parseBoolean(nv.getValue());
          } else {
            fileMetadata.put(nv.getKey(), nv.getValue());
          }
        }

        // now update the rest of the metadata
        if (!file.isFolder()) {
          getRepository().setFileMetadata(file.getId(), fileMetadata);
        }

        // handle hidden flag if it is different
        if (file.isHidden() != isHidden) {
          file.setHidden(isHidden);

          /*
           * Since we cannot simply set the new value, use the RepositoryFileAdapter to create a new instance and then update the original.
           */
          RepositoryFile sourceFile = getRepository().getFileById(file.getId());
          RepositoryFileDto destFileDto = RepositoryFileAdapter.toFileDto(sourceFile);

          destFileDto.setHidden(isHidden);

          RepositoryFile destFile = RepositoryFileAdapter.toFile(destFileDto);

          // add the existing acls and file data
          RepositoryFileAcl acl = getRepository().getAcl(sourceFile.getId());
          if (!file.isFolder()) {
            IRepositoryFileData data = getRepository().getDataForRead(sourceFile.getId(), SimpleRepositoryFileData.class);
            getRepository().updateFile(destFile, data, null);
            getRepository().updateAcl(acl);
          } else {
            getRepository().updateFolder(destFile, null);
          }
        }
        return Response.ok().build();
      } else {
        return Response.status(Response.Status.UNAUTHORIZED).build();
      }
    } catch (Throwable t) {
      return Response.serverError().build();
    }
  }

  /**
   * Validate path and send appropriate response if necessary TODO: Add validation to IUnifiedRepository interface
   * 
   * @param path
   * @return
   */
  private boolean isPathValid(String path) {
    if (path.startsWith("/etc") || path.startsWith("/system")) {
      return false;
    }
    return true;
  }

  private boolean isPath(String pathId) {
	   return pathId != null && pathId.contains(":");
  }
  private void sortByLocaleTitle(final Collator collator, final RepositoryFileTreeDto tree) {

    if (tree == null || tree.getChildren() == null || tree.getChildren().size() <= 0) {
      return;
    }

    for (RepositoryFileTreeDto rft : tree.getChildren()) {
      sortByLocaleTitle(collator, rft);
      Collections.sort(tree.getChildren(), new Comparator<RepositoryFileTreeDto>() {
        @Override
        public int compare(RepositoryFileTreeDto repositoryFileTree, RepositoryFileTreeDto repositoryFileTree2) {
          String title1 = repositoryFileTree.getFile().getTitle();
          String title2 = repositoryFileTree2.getFile().getTitle();

          if (collator.compare(title1, title2) == 0) {
            return title1.compareTo(title2); // use lexical order if equals ignore case
          }

          return collator.compare(title1, title2);
        }
      });
    }
  }

  public RepositoryDownloadWhitelist getWhitelist() {
    if (whitelist == null) {
      whitelist = new RepositoryDownloadWhitelist();
    }
    return whitelist;
  }

  public void setWhitelist(RepositoryDownloadWhitelist whitelist) {
    this.whitelist = whitelist;
  }  
  
  public static IAuthorizationPolicy getPolicy() {
    if(policy == null){
      policy = PentahoSystem.get(IAuthorizationPolicy.class);
    }
    return policy;
  }

  public static IUnifiedRepository getRepository() {
    if(repository == null){
      repository = PentahoSystem.get(IUnifiedRepository.class);
    }
    return repository;
  }

  public static DefaultUnifiedRepositoryWebService getRepoWs() {
    if(repoWs == null){
      repoWs = new DefaultUnifiedRepositoryWebService();
    }
    return repoWs;
  }
}
