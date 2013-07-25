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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IContentInfo;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginOperation;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.engine.PluginBeanException;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.repository2.unified.webservices.ExecutableFileTypeDto;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.WILDCARD;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

@Path("/repos")
public class RepositoryResource extends AbstractJaxRSResource {

  protected IPluginManager pluginManager = PentahoSystem.get(IPluginManager.class);

  private static final Log logger = LogFactory.getLog(RepositoryResource.class);
  public static final String GENERATED_CONTENT_PERSPECTIVE = "generatedContent"; //$NON-NLS-1$

  protected IUnifiedRepository repository = PentahoSystem.get(IUnifiedRepository.class);

  @GET
  @Path("{pathId : .+}/content")
  @Produces({WILDCARD})
  public Response doGetFileOrDir(@PathParam("pathId") String pathId) throws FileNotFoundException {
    FileResource fileResource = new FileResource(httpServletResponse);
    return fileResource.doGetFileOrDir(pathId);
  }


  @GET
  @Path("{pathId : .+}/default")
  @Produces({WILDCARD})
  public Response doExecuteDefault(@PathParam("pathId") String pathId) throws FileNotFoundException, MalformedURLException, URISyntaxException {
    String perspective = null;
    StringBuffer buffer = null;
    String url = null;
    String path = FileResource.idToPath(pathId);
    String extension = path.substring(path.lastIndexOf('.') + 1);
    IPluginManager pluginManager = PentahoSystem.get(IPluginManager.class, PentahoSessionHolder.getSession());
    IContentInfo info = pluginManager.getContentTypeInfo(extension);
    for (IPluginOperation operation : info.getOperations()) {
      if (operation.getId().equalsIgnoreCase("RUN")) { //$NON-NLS-1$
        perspective = operation.getPerspective();
        break;
      }
    }
    if (perspective == null) {
      perspective = GENERATED_CONTENT_PERSPECTIVE;
    }

    buffer = httpServletRequest.getRequestURL();
    String queryString = httpServletRequest.getQueryString();
    url = buffer.substring(0, buffer.lastIndexOf("/") + 1) + perspective + //$NON-NLS-1$
        ((queryString != null && queryString.length() > 0) ? "?" + httpServletRequest.getQueryString() : ""); //$NON-NLS-1$ //$NON-NLS-2$
    return Response.seeOther((new URL(url)).toURI()).build();
  }

  /**
   * Services a HTTP form POST request for a resource identified by the compound key
   * <code>contextId</code>, and <code>resourceId</code>
   * <p/>
   * <code>contextId</code> may be one of:<p>
   * A. repository file id (colon-delimited path), e.g. <code>:public:steel-wheels:sales.prpt</code></br>
   * B. repository file extension, e.g. <code>prpt</code><br>
   * C. plugin id</li>
   * <p/>
   * <code>resourceId</code> may be one of:<p>
   * 1. static file residing in a publicly visible plugin folder</br>
   * 2. repository file id (colon-delimited path), e.g. <code>:public:steel-wheels:sales.prpt</code></br>
   * 3. content generator id</br>
   * <p/>
   * The resolution order is as follows, the first to find the resource wins:
   * <ol>
   * <li>A1</li>
   * <li>A2</li>
   * <li>A3</li>
   * <li>B1</li>
   * <li>B3</li>
   * <li>C1</li>
   * <li>C3</li>
   * </ol>
   *
   * @param contextId  identifies the context in which the resource should be retrieved
   * @param resourceId identifies a resource to be retrieved
   * @param formParams any arguments needed to render the resource
   * @return a JAX-RS {@link Response}, in many cases, this will trigger a streaming operation
   *         <b>after</b> it it is returned to the caller
   */
  @Path("/{contextId}/{resourceId : .+}")
  @POST
  @Consumes(APPLICATION_FORM_URLENCODED)
  @Produces({WILDCARD})
  public Response doFormPost(@PathParam("contextId") String contextId, @PathParam("resourceId") String resourceId,
                             final MultivaluedMap<String, String> formParams) throws ObjectFactoryException, PluginBeanException, IOException, URISyntaxException {

    httpServletRequest = JerseyUtil.correctPostRequest(formParams, httpServletRequest);

    if (logger.isDebugEnabled()) {
      for (Object key : httpServletRequest.getParameterMap().keySet()) {
        logger.debug("param [" + key + "]"); //$NON-NLS-1$ //$NON-NLS-2$
      }
    }

    return doService(contextId, resourceId);
  }

  /**
   * Services a HTTP GET request for a resource identified by the compound key <code>contextId</code>, and <code>resourceId</code>.
   * Any HTTP request params are pulled from the GET request and used in rendering the resource.
   * <p/>
   * <code>contextId</code> may be one of:<p>
   * A. repository file id (colon-delimited path), e.g. <code>:public:steel-wheels:sales.prpt</code></br>
   * B. repository file extension, e.g. <code>prpt</code><br>
   * C. plugin id</li>
   * <p/>
   * <code>resourceId</code> may be one of:<p>
   * 1. static file residing in a publicly visible plugin folder</br>
   * 2. repository file id (colon-delimited path), e.g. <code>:public:steel-wheels:sales.prpt</code></br>
   * 3. content generator id</br>
   * <p/>
   * The resolution order is as follows, the first to find the resource wins:
   * <ol>
   * <li>A1</li>
   * <li>A2</li>
   * <li>A3</li>
   * <li>B1</li>
   * <li>B3</li>
   * <li>C1</li>
   * <li>C3</li>
   * </ol>
   *
   * @param contextId  identifies the context in which the resource should be retrieved
   * @param resourceId identifies a resource to be retrieved
   * @return a JAX-RS {@link Response}, in many cases, this will trigger a streaming operation
   *         <b>after</b> it it is returned to the caller
   */
  @Path("/{contextId}/{resourceId : .+}")
  @GET
  @Produces({WILDCARD})
  public Response doGet(@PathParam("contextId") String contextId, @PathParam("resourceId") String resourceId)
      throws ObjectFactoryException, PluginBeanException, IOException, URISyntaxException {

    if (logger.isDebugEnabled()) {
      for (Object key : httpServletRequest.getParameterMap().keySet()) {
        logger.debug("param [" + key + "]"); //$NON-NLS-1$ //$NON-NLS-2$
      }
    }

    return doService(contextId, resourceId);
  }

  @Path("/executableTypes")
  @GET
  @Produces({APPLICATION_XML, APPLICATION_JSON})
  public Response getExecutableTypes() {
    ArrayList<ExecutableFileTypeDto> executableTypes = new ArrayList<ExecutableFileTypeDto>();
    for (String contentType : pluginManager.getContentTypes()) {
      IContentInfo contentInfo = pluginManager.getContentTypeInfo(contentType);
      ExecutableFileTypeDto executableFileType = new ExecutableFileTypeDto();
      executableFileType.setDescription(contentInfo.getDescription());
      executableFileType.setExtension(contentInfo.getExtension());
      executableFileType.setTitle(contentInfo.getTitle());
      executableFileType.setCanSchedule(hasOperationId(contentInfo.getOperations(), "SCHEDULE_NEW"));
      executableFileType.setCanEdit(hasOperationId(contentInfo.getOperations(), "EDIT"));
      executableTypes.add(executableFileType);
    }

    final GenericEntity<List<ExecutableFileTypeDto>> entity = new GenericEntity<List<ExecutableFileTypeDto>>(
        executableTypes) {
    };
    return Response.ok(entity).build();
  }

  private boolean hasOperationId(final List<IPluginOperation> operations, final String operationId){
    if(operations != null && StringUtils.isNotBlank(operationId)){
      for(IPluginOperation operation : operations){
        if(operation != null && StringUtils.isNotBlank(operation.getId())){
          if(operation.getId().equals(operationId) && StringUtils.isNotBlank(operation.getPerspective())){
            return true;
          }
        }
      }
    }
    return false;
  }

  protected Response doService(String contextId, String resourceId)
      throws ObjectFactoryException, PluginBeanException, IOException, URISyntaxException {

    ctxt("Is [{0}] a repository file id?", contextId); //$NON-NLS-1$
    if (contextId.startsWith(":")) { //$NON-NLS-1$
      //
      // The context is a repository file (A)
      //

      final RepositoryFile file = repository.getFile(FileResource.idToPath(contextId));
      if (file == null) {
        logger.error(MessageFormat.format("Repository file [{0}] not found", contextId));
        return Response.serverError().build();
      }

      Response response = null;

      ctxt("Yep, [{0}] is a repository file id", contextId); //$NON-NLS-1$
      final String ext = RepositoryFilenameUtils.getExtension(file.getName());
      String pluginId = pluginManager.getPluginIdForType(ext);
      if (pluginId == null) {

        // A.3.a (faux content generator for .url files)
        response = getUrlResponse(file, resourceId);
        if (response != null) {
          return response;
        } else {
          logger.error(MessageFormat.format("No plugin was found to service content of type [{0}]", ext));
          return Response.serverError().build();
        }
      }

      // A.1.
      response = getPluginFileResponse(pluginId, resourceId);
      if (response != null) {
        return response;
      }

      // A.2.
      response = getRepositoryFileResponse(file.getPath(), resourceId);
      if (response != null) {
        return response;
      }

      // A.3.b (real content generator)
      CGFactory fac = new RepositoryFileCGFactory(resourceId, file);
      response = getContentGeneratorResponse(fac);
      if (response != null) {
        return response;
      }

    } else {
      ctxt("Nope, [{0}] is not a repository file id", contextId); //$NON-NLS-1$
      ctxt("Is [{0}] is a repository file extension?", contextId); //$NON-NLS-1$
      String pluginId = pluginManager.getPluginIdForType(contextId);
      if (pluginId != null) {
        //
        // The context is a file extension (B)
        //
        ctxt("Yep, [{0}] is a repository file extension", contextId); //$NON-NLS-1$

        // B.1.
        Response response = getPluginFileResponse(pluginId, resourceId);
        if (response != null) {
          return response;
        }

        // B.3.
        CGFactory fac = new ContentTypeCGFactory(resourceId, contextId);
        response = getContentGeneratorResponse(fac);
        if (response != null) {
          return response;
        }
      } else {
        ctxt("Nope, [{0}] is not a repository file extension", contextId); //$NON-NLS-1$
        ctxt("Is [{0}] is a plugin id?", contextId); //$NON-NLS-1$
        if (pluginManager.getRegisteredPlugins().contains(contextId)) {
          //
          // The context is a plugin id (C)
          //
          ctxt("Yep, [{0}] is a plugin id", contextId); //$NON-NLS-1$
          pluginId = contextId;

          // C.1.
          Response response = getPluginFileResponse(pluginId, resourceId);
          if (response != null) {
            return response;
          }

          // C.3.
          CGFactory fac = new DirectCGFactory(resourceId, contextId);
          response = getContentGeneratorResponse(fac);
          if (response != null) {
            return response;
          }
        } else {
          ctxt("Nope, [{0}] is not a plugin id", contextId); //$NON-NLS-1$
          logger.warn(MessageFormat.format("Failed to resolve context [{0}]", contextId)); //$NON-NLS-1$
        }
      }
    }

    logger.warn(MessageFormat.format("End of the resolution chain. No resource [{0}] found in context [{1}].",
        resourceId, contextId));
    return Response.status(NOT_FOUND).build();
  }

  abstract class CGFactory implements ContentGeneratorDescriptor {
    String contentGeneratorId;

    String command;

    public CGFactory(String contentGeneratorPath) {
      if (contentGeneratorPath.contains("/")) { //$NON-NLS-1$
        contentGeneratorId = contentGeneratorPath.substring(0, contentGeneratorPath.indexOf('/'));
        command = contentGeneratorPath.substring(contentGeneratorPath.indexOf('/') + 1);
        debug("decomposing path [{0}] into content generator id [{1}] and command [{2}]", contentGeneratorPath, //$NON-NLS-1$
            contentGeneratorId, command);
      } else {
        contentGeneratorId = contentGeneratorPath;
      }
    }

    public String getContentGeneratorId() {
      return contentGeneratorId;
    }

    public String getCommand() {
      return command;
    }

    abstract IContentGenerator create();

    abstract GeneratorStreamingOutput getStreamingOutput(IContentGenerator cg);
  }

  class RepositoryFileCGFactory extends ContentTypeCGFactory {
    RepositoryFile file;

    public RepositoryFileCGFactory(String contentGeneratorPath, RepositoryFile file) {
      super(contentGeneratorPath, file.getName().substring(file.getName().indexOf('.') + 1));
      this.file = file;
    }

    @Override
    GeneratorStreamingOutput getStreamingOutput(IContentGenerator cg) {
      return new GeneratorStreamingOutput(cg, this, httpServletRequest, httpServletResponse, acceptableMediaTypes, file,
          command);
    }
  }

  class ContentTypeCGFactory extends CGFactory {
    String repoFileExt;

    public ContentTypeCGFactory(String contentGeneratorPath, String repoFileExt) {
      super(contentGeneratorPath);
      this.repoFileExt = repoFileExt;
    }

    @Override
    public IContentGenerator create() {
      return pluginManager.getContentGenerator(repoFileExt, contentGeneratorId);
    }

    @Override
    GeneratorStreamingOutput getStreamingOutput(IContentGenerator cg) {
      return new GeneratorStreamingOutput(cg, this, httpServletRequest, httpServletResponse, acceptableMediaTypes, null,
          command);
    }

    @Override
    public String getServicingFileType() {
      return repoFileExt;
    }

    @Override
    public String getPluginId() {
      return PentahoSystem.get(IPluginManager.class).getPluginIdForType(repoFileExt);
    }
  }

  class DirectCGFactory extends CGFactory {
    String pluginId;

    public DirectCGFactory(String contentGeneratorPath, String pluginId) {
      super(contentGeneratorPath);
      this.pluginId = pluginId;
    }

    @Override
    IContentGenerator create() {
      return pluginManager.getContentGenerator(null, contentGeneratorId);
    }

    @Override
    GeneratorStreamingOutput getStreamingOutput(IContentGenerator cg) {
      return new GeneratorStreamingOutput(cg, this, httpServletRequest, httpServletResponse, acceptableMediaTypes, null,
          command);
    }

    @Override
    public String getServicingFileType() {
      return null;
    }

    @Override
    public String getPluginId() {
      return pluginId;
    }

  }

  protected Response getUrlResponse(RepositoryFile file, String resourceId) throws MalformedURLException, URISyntaxException {
    String ext = file.getName().substring(file.getName().indexOf('.') + 1);
    if (!(ext.equals("url") && resourceId.equals("generatedContent"))) return null; //$NON-NLS-1$ //$NON-NLS-2$

    String url = extractUrl(file);
    if (!url.trim().startsWith("http")) { //$NON-NLS-1$
      //if path is relative, prepend FQSURL
      url = PentahoSystem.getApplicationContext().getFullyQualifiedServerURL() + url;
    }
    return Response.seeOther((new URL(url)).toURI()).build();
  }

  protected Response getContentGeneratorResponse(CGFactory fac) {
    rsc("Is [{0}] a content generator ID?", fac.getContentGeneratorId()); //$NON-NLS-1$
    final IContentGenerator contentGenerator;
    try {
      contentGenerator = fac.create();
    } catch (NoSuchBeanDefinitionException e) {
      rsc("Nope, [{0}] is not a content generator ID.", fac.getContentGeneratorId()); //$NON-NLS-1$
      return null;
    }
    rsc(
        "Yep, [{0}] is a content generator ID. Executing (where command path is {1})..", fac.getContentGeneratorId(), fac.getCommand()); //$NON-NLS-1$
    GeneratorStreamingOutput gso = fac.getStreamingOutput(contentGenerator);
    return Response.ok(gso).build();
  }

  protected Response getPluginFileResponse(String pluginId, String filePath) throws IOException {
    rsc("Is [{0}] a path to a plugin file?", filePath); //$NON-NLS-1$
    if(pluginManager.isPublic(pluginId, filePath)) {
      PluginResource pluginResource = new PluginResource(httpServletResponse);
      Response readFileResponse = pluginResource.readFile(pluginId, filePath);
      //TODO: should we assume forbidden means move on in the resolution chain, or abort??
      if (readFileResponse.getStatus() != Status.NOT_FOUND.getStatusCode()) {
        rsc(MessageFormat.format("Yep, [{0}] is a path to a static plugin file", filePath)); //$NON-NLS-1$
        return readFileResponse;
      }
    }
    rsc(MessageFormat.format("Nope, [{0}] is not a path to a static plugin file", filePath)); //$NON-NLS-1$
    return null;
  }

  protected Response getRepositoryFileResponse(String filePath, String relPath) throws IOException {
    rsc("Is [{0}] a relative path to a repository file, relative to [{1}]?", relPath, filePath); //$NON-NLS-1$

    FileResource fileResource = new FileResource(httpServletResponse);
    String path = RepositoryFilenameUtils.separatorsToRepository(RepositoryFilenameUtils.concat(filePath, "../" + relPath)); //$NON-NLS-1$
    Response response = fileResource.doGetFileOrDir(path.replace('/', ':').substring(1));
    if (response.getStatus() != Status.NOT_FOUND.getStatusCode()) {
      rsc(MessageFormat.format("Yep, [{0}] is a repository file", path)); //$NON-NLS-1$
      return response;
    }
    rsc(MessageFormat.format("Nope, [{0}] is not a repository file", path)); //$NON-NLS-1$
    return null;
  }

  private void ctxt(String msg, Object... args) {
    debug("[RESOLVING CONTEXT ID] ==> " + msg, args); //$NON-NLS-1$
  }

  private void rsc(String msg, Object... args) {
    debug("[RESOLVING RESOURCE ID] ==> " + msg, args); //$NON-NLS-1$
  }

  private void debug(String msg, Object... args) {
    logger.debug(MessageFormat.format(msg, args));
  }

  protected String extractUrl(RepositoryFile file) {

    SimpleRepositoryFileData data = null;

    data = repository.getDataForRead(file.getId(), SimpleRepositoryFileData.class);
    StringWriter writer = new StringWriter();
    try {
      IOUtils.copy(data.getStream(), writer);
    } catch (IOException e) {
      return ""; //$NON-NLS-1$
    }

    String props = writer.toString();
    StringTokenizer tokenizer = new StringTokenizer(props, "\n"); //$NON-NLS-1$
    while (tokenizer.hasMoreTokens()) {
      String line = tokenizer.nextToken();
      int pos = line.indexOf('=');
      if (pos > 0) {
        String propname = line.substring(0, pos);
        String value = line.substring(pos + 1);
        if ((value != null) && (value.length() > 0) && (value.charAt(value.length() - 1) == '\r')) {
          value = value.substring(0, value.length() - 1);
        }
        if ("URL".equalsIgnoreCase(propname)) { //$NON-NLS-1$
          return value;
        }

      }
    }
    // No URL found
    return "";//$NON-NLS-1$

  }
}
