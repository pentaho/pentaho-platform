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
 * Copyright 2005 - 2009 Pentaho Corporation.  All rights reserved.
 *
 *
 * Created Dec 29, 2005 
 * @author wseyler
 */

package org.pentaho.platform.web.refactor;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.uifoundation.component.xml.XmlComponent;
import org.pentaho.platform.web.http.messages.Messages;
import org.pentaho.platform.web.http.request.HttpRequestParameterProvider;

public class SolutionManagerUIComponent extends XmlComponent {
  /**
   * 
   */
  private static final long serialVersionUID = 5322450732426274853L;

  private static final Log logger = LogFactory.getLog(SolutionManagerUIComponent.class);

  private static final String PATH_STR = "path"; //$NON-NLS-1$

  private static final String EMPTY_STR = ""; //$NON-NLS-1$

  private static final String BASE_URL_STR = "baseUrl"; //$NON-NLS-1$

  private IPentahoSession session = null;

  public SolutionManagerUIComponent(final IPentahoUrlFactory urlFactory, final List messages, final IPentahoSession session) {
    super(urlFactory, messages, null);
    this.session = session;
    setXsl("text/xml", "copy.xsl"); //$NON-NLS-1$ //$NON-NLS-2$
  }

  public Document doGetSolutionStructure() {
    ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, session);
    Document doc = repository.getSolutionStructure(ISolutionRepository.ACTION_ADMIN);
    return doc;
  }

  public Document doFileUpload() {
    String baseUrl = PentahoSystem.getApplicationContext().getSolutionPath(SolutionManagerUIComponent.EMPTY_STR);
    ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, session);
    String path = this.getParameter(SolutionManagerUIComponent.PATH_STR, null);
    IParameterProvider request = (IParameterProvider) getParameterProviders().get(
        IParameterProvider.SCOPE_REQUEST);
    HttpServletRequest httpRequest = ((HttpRequestParameterProvider)request).getRequest();
/*    
    String contentType = httpRequest.getContentType();
    if ((contentType == null)
        || ((contentType.indexOf("multipart/form-data") < 0) && (contentType.indexOf("multipart/mixed stream") < 0))) { //$NON-NLS-1$ //$NON-NLS-2$
      return doGetSolutionStructure();
    }
    DiskFileUpload uploader = new DiskFileUpload();
*/    
    
    if (!ServletFileUpload.isMultipartContent(httpRequest)) {
      return doGetSolutionStructure();
    }
    
    ServletFileUpload uploader = new ServletFileUpload(new DiskFileItemFactory());
    
    try {
      List fileList = uploader.parseRequest(httpRequest);
      Iterator iter = fileList.iterator();
      while (iter.hasNext()) {
        FileItem fi = (FileItem) iter.next();

        // Check if not form field so as to only handle the file inputs
        if (!fi.isFormField()) {
          File tempFileRef = new File(fi.getName());
          repository.addSolutionFile(baseUrl, path, tempFileRef.getName(), fi.get(), true);
          SolutionManagerUIComponent.logger
              .info(Messages.getInstance().getString("SolutionManagerUIComponent.INFO_0001_FILE_SAVED") + path + "/" + tempFileRef.getName()); //$NON-NLS-1$ //$NON-NLS-2$
        }
      }
    } catch (FileUploadException e) {
      SolutionManagerUIComponent.logger.error(e.toString());
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return doGetSolutionStructure();
  }

  @Override
  public Document getXmlContent() {
    setXslProperty(SolutionManagerUIComponent.BASE_URL_STR, urlFactory.getDisplayUrlBuilder().getUrl());
    return doFileUpload();
  }

  @Override
  public Log getLogger() {
    return SolutionManagerUIComponent.logger;
  }

  @Override
  public boolean validate() {
    return true;
  }
}
