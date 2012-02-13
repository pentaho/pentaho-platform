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
 * Copyright 2008 - 2009 Pentaho Corporation.  All rights reserved.
 *
*/
package org.pentaho.platform.web.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.data.DBDatasourceServiceException;
import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogServiceException;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCube;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianDataSource;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianSchema;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.web.servlet.messages.Messages;

import mondrian.xmla.DataSourcesConfig.DataSource;

public class MondrianCatalogPublisher extends RepositoryFilePublisher {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(MondrianCatalogPublisher.class);

  private static final long serialVersionUID = -6052692173173633694L;

  private static final int FILE_ADD_DATASOURCE_PROBLEM = 6;
  
  private static final int FILE_ADD_XMLA_SCHEMA_EXISTS = 8;
  
  private static final int DATASOURCE_DRIVER_MISSING = 9;
 // ~ Instance fields =================================================================================================

  private IMondrianCatalogService mondrianCatalogService = PentahoSystem.get(IMondrianCatalogService.class, "IMondrianCatalogService", PentahoSessionHolder.getSession()); //$NON-NLS-1$

  private String fullyQualifiedServerURL;

  // ~ Constructors ====================================================================================================

  public MondrianCatalogPublisher() {
    super();
    fullyQualifiedServerURL = PentahoSystem.getApplicationContext().getFullyQualifiedServerURL();
  }

  // ~ Methods =========================================================================================================

  @Override
  public Log getLogger() {
    return MondrianCatalogPublisher.logger;
  }

  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    PentahoSystem.systemEntryPoint();
    try {
    resp.setCharacterEncoding(LocaleHelper.getSystemEncoding());

    IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
    String publishPath = req.getParameter("publishPath"); //$NON-NLS-1$
    String publishKey = req.getParameter("publishKey");//$NON-NLS-1$
    String jndiName = req.getParameter("jndiName");//$NON-NLS-1$

    boolean overwrite = Boolean.valueOf(req.getParameter("overwrite")).booleanValue(); //$NON-NLS-1$
    boolean mkdirs = Boolean.valueOf(req.getParameter("mkdirs")).booleanValue(); //$NON-NLS-1$
    boolean enableXmla = Boolean.valueOf(req.getParameter("enableXmla")).booleanValue(); //$NON-NLS-1$

    List<FileItem> fileItems = Collections.emptyList();
    try {
      fileItems = getFileItems(req);
    } catch (FileUploadException e) {
      if (MondrianCatalogPublisher.logger.isErrorEnabled()) {
        MondrianCatalogPublisher.logger.error(Messages.getInstance().getErrorString("MondrianCatalogPublisher.ERROR_0002_EXCEPTION_OCCURRED"), e); //$NON-NLS-1$
      }
      resp.getWriter().println(ISolutionRepository.FILE_ADD_FAILED);
      return;
    }
    int status = ISolutionRepository.FILE_ADD_FAILED;
    try {
      status = doPublish(fileItems, publishPath, publishKey, null, null, null, null, null,
        overwrite, mkdirs, pentahoSession);
    } catch (Exception e) {
      MondrianCatalogPublisher.logger.error(Messages.getInstance().getErrorString("MondrianCatalogPublisher.ERROR_0005_PUBLISH_EXCEPTION"), e); //$NON-NLS-1$
    }

    if (status != ISolutionRepository.FILE_ADD_SUCCESSFUL) {
      resp.getWriter().println(status);
      return;
    }

    if (MondrianCatalogPublisher.logger.isDebugEnabled()) {
      MondrianCatalogPublisher.logger.debug("publishPath=" + publishPath); //$NON-NLS-1$
    }
    if ((publishPath != null) && (publishPath.endsWith("/") || publishPath.endsWith("\\"))) { //$NON-NLS-1$ //$NON-NLS-2$
      publishPath = publishPath.substring(0, publishPath.length() - 1);
    }

    if (MondrianCatalogPublisher.logger.isDebugEnabled()) {
      MondrianCatalogPublisher.logger.debug("jndiName=" + jndiName); //$NON-NLS-1$
    }
    if (StringUtils.isBlank(jndiName)) {
      throw new ServletException(Messages.getInstance().getErrorString("MondrianCatalogPublisher.ERROR_0003_JNDINAME_REQUIRED")); //$NON-NLS-1$
    }

    // expecting exactly one file
    if (fileItems.size() != 1) {
      // when this is appended, FILE_ADD_SUCCESSFUL has already been appended from super
      if (MondrianCatalogPublisher.logger.isErrorEnabled()) {
        MondrianCatalogPublisher.logger.error(Messages.getInstance().getErrorString("MondrianCatalogPublisher.ERROR_0004_FILE_COUNT", "" + fileItems.size())); //$NON-NLS-1$ //$NON-NLS-2$
      }
      resp.getWriter().println(ISolutionRepository.FILE_ADD_FAILED);
      return;
    }

    FileItem fi = fileItems.iterator().next();

    String catDef = "solution:" + publishPath + "/" + fi.getName(); //$NON-NLS-1$//$NON-NLS-2$

    MondrianSchema mondrianSchema = mondrianCatalogService.loadMondrianSchema(catDef, pentahoSession);
    
    String catName = mondrianSchema.getName();
    
    // verify JNDI
    // Note: we use the unbound JNDI name here, the PentahoXmlaServlet and PivotViewComponent resolve the JNDI name

    try {
      IDBDatasourceService datasourceService =  PentahoSystem.getObjectFactory().get(IDBDatasourceService.class ,null);    	
      datasourceService.getDataSource(jndiName);
    } catch (ObjectFactoryException objface) {
      	MondrianCatalogPublisher.logger.error(Messages.getInstance().getErrorString("MondrianCatalogPublisher.ERROR_0006_UNABLE_TO_FACTORY_OBJECT", jndiName), objface); //$NON-NLS-1$      	
    } catch (DBDatasourceServiceException dse) {
      MondrianCatalogPublisher.logger.error(Messages.getInstance().getErrorString("MondrianCatalogPublisher.ERROR_0001_JNDI_NAMING_ERROR", jndiName), dse); //$NON-NLS-1$
      resp.getWriter().println(FILE_ADD_DATASOURCE_PROBLEM);
      return;
    }

    String catConnectStr = "Provider=mondrian;DataSource=" + jndiName; //$NON-NLS-1$

    // If XMLA is disabled, set an additional connection parameter
    if (!enableXmla) {
      catConnectStr += ";EnableXmla=False"; //$NON-NLS-1$
        
    }

    // write this catalog to the default Pentaho DataSource
    
    String dsUrl = fullyQualifiedServerURL;
    if (!dsUrl.endsWith("/")) { //$NON-NLS-1$
      dsUrl += "/"; //$NON-NLS-1$
    }
    dsUrl += "Xmla"; //$NON-NLS-1$
    
    MondrianDataSource ds = new MondrianDataSource(
        "Provider=Mondrian;DataSource=Pentaho",
        "Pentaho BI Platform Datasources",
        dsUrl, 
        "Provider=Mondrian", // no default jndi datasource should be specified
        "PentahoXMLA", 
        DataSource.PROVIDER_TYPE_MDP, 
        DataSource.AUTH_MODE_UNAUTHENTICATED, 
        null
      );

    MondrianCatalog cat = new MondrianCatalog(
        catName, 
        catConnectStr, 
        catDef, 
        ds, 
        new MondrianSchema(catName, new ArrayList<MondrianCube>())
      );

    try {
      mondrianCatalogService.addCatalog(cat, overwrite, pentahoSession);
    } catch (MondrianCatalogServiceException e) {
      if (MondrianCatalogPublisher.logger.isErrorEnabled()) {
        MondrianCatalogPublisher.logger.error(Messages.getInstance().getErrorString("MondrianCatalogPublisher.ERROR_0002_EXCEPTION_OCCURRED"), e); //$NON-NLS-1$
      }
      if (e.getReason().equals(MondrianCatalogServiceException.Reason.XMLA_SCHEMA_NAME_EXISTS)) {
        // Special case for files that already exists.
        resp.getWriter().println(FILE_ADD_XMLA_SCHEMA_EXISTS);
      } else {
        // Default error code
        resp.getWriter().println(ISolutionRepository.FILE_ADD_FAILED);
      }
      return;
    }

    // flush all schemas
    mondrian.rolap.agg.AggregationManager.instance().getCacheControl(null, null).flushSchemaCache();
    
    resp.getWriter().println(ISolutionRepository.FILE_ADD_SUCCESSFUL);
    } finally {
      PentahoSystem.systemExitPoint();
    }
  }

  @Override
  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    doGet(req, resp);
  }

  public IMondrianCatalogService getMondrianCatalogService() {
    return mondrianCatalogService;
  }

  public void setMondrianCatalogService(final IMondrianCatalogService mondrianCatalogService) {
    this.mondrianCatalogService = mondrianCatalogService;
  }

  public String getFullyQualifiedServerURL() {
    return fullyQualifiedServerURL;
  }

  public void setFullyQualifiedServerURL(final String fullyQualifiedServerURL) {
    this.fullyQualifiedServerURL = fullyQualifiedServerURL;
  }
}
