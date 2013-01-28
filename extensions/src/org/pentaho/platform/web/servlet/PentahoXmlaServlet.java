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
*/
package org.pentaho.platform.web.servlet;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import mondrian.olap.Connection;
import mondrian.olap.DriverManager;
import mondrian.olap.MondrianServer;
import mondrian.server.FileRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.dom4j.Document;
import org.dom4j.Node;
import org.olap4j.OlapConnection;
import org.pentaho.platform.api.engine.IConnectionUserRoleMapper;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.util.XmlParseException;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.engine.services.solution.PentahoEntityResolver;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogHelper;
import org.pentaho.platform.plugin.services.connections.mondrian.MDXConnection;
import org.pentaho.platform.repository.solution.filebased.MondrianVfs;
import org.pentaho.platform.repository.solution.filebased.SolutionRepositoryVfsFileObject;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
import org.pentaho.platform.web.servlet.messages.Messages;
import org.xml.sax.EntityResolver;

import mondrian.server.DynamicContentFinder;
import mondrian.spi.CatalogLocator;
import mondrian.spi.impl.ServletContextCatalogLocator;
import mondrian.xmla.XmlaHandler.ConnectionFactory;
import mondrian.xmla.impl.DynamicDatasourceXmlaServlet;

/**
 * Filters out <code>DataSource</code> elements that are not XMLA-related.
 * <p />
 * Background: Pentaho re-used datasources.xml for non-XMLA purposes. But since <code>DefaultXmlaServlet</code> requires 
 * actual XMLA datasources, this servlet extends <code>DefaultXmlaServlet</code> and removes the non-XMLA datasources 
 * before continuing normal <code>DefaultXmlaServlet</code> behavior.
 * <p />
 * The convention here is that any <code>DataSource</code> elements with  
 * <code>&lt;ProviderType&gt;None&lt;/ProviderType&gt;</code> are considered non-XMLA and are filtered out.
 * 
 * @author mlowery
 */
@SuppressWarnings("unchecked")
public class PentahoXmlaServlet extends DynamicDatasourceXmlaServlet {

  // ~ Static fields/initializers ======================================================================================

  private static final long serialVersionUID = 5801343357261568600L;
  private static final Log logger = LogFactory.getLog(PentahoXmlaServlet.class);

  // - Constructors ================================

  public PentahoXmlaServlet() {
    super();
    
    try {
    	DefaultFileSystemManager dfsm = (DefaultFileSystemManager)VFS.getManager();
      if(dfsm.hasProvider("mondrian") == false){
        dfsm.addProvider("mondrian", new MondrianVfs());
      }
    } catch (FileSystemException e) {
    	logger.error(e.getMessage());
    }
  }

  // ~ Methods =========================================================================================================

  @Override
  protected DynamicContentFinder makeContentFinder(String dataSourcesUrl) {
    return new DynamicContentFinder(dataSourcesUrl) {
      @Override
      public String getContent() {
    	String content = null;
    	try {
	    	String original = generateInMemoryDatasourcesXml();
	        EntityResolver loader = new PentahoEntityResolver();
	        Document originalDocument = XmlDom4JHelper.getDocFromString(original, loader);
	        if (PentahoXmlaServlet.logger.isDebugEnabled()) {
	          PentahoXmlaServlet.logger
	              .debug(Messages.getInstance().getString("PentahoXmlaServlet.DEBUG_ORIG_DOC", originalDocument.asXML())); //$NON-NLS-1$
	        }
	        Document modifiedDocument = (Document) originalDocument.clone();
	        List<Node> nodesToRemove = modifiedDocument.selectNodes("/DataSources/DataSource/Catalogs/Catalog[contains(DataSourceInfo, 'EnableXmla=False')]"); //$NON-NLS-1$
	        if (PentahoXmlaServlet.logger.isDebugEnabled()) {
	          PentahoXmlaServlet.logger.debug(Messages.getInstance().getString(
	              "PentahoXmlaServlet.DEBUG_NODES_TO_REMOVE", String.valueOf(nodesToRemove.size()))); //$NON-NLS-1$
	        }
	        for (Node node : nodesToRemove) {
	          node.detach();
	        }
	        if (PentahoXmlaServlet.logger.isDebugEnabled()) {
	          PentahoXmlaServlet.logger.debug(Messages.getInstance().getString("PentahoXmlaServlet.DEBUG_MOD_DOC", modifiedDocument.asXML())); //$NON-NLS-1$
	        }
	        content = modifiedDocument.asXML();
    	} catch(XmlParseException e) {
             PentahoXmlaServlet.logger.error(Messages.getInstance().getString("PentahoXmlaServlet.ERROR_0004_UNABLE_TO_GET_DOCUMENT_FROM_STRING"), e); //$NON-NLS-1$
        }
    	return content;
      }
      
      private String generateInMemoryDatasourcesXml() {
    	  String datasourcesXml = null;
    	  try {
    		  datasourcesXml = SecurityHelper.getInstance().runAsSystem(new Callable<String>() {
    	        @Override
    	        public String call() throws Exception {
    	        	String result = null;
    	        	IUnifiedRepository repo = PentahoSystem.get(IUnifiedRepository.class);
    	    	    if(repo != null) {
        	    	    MondrianCatalogHelper mondrianCatalogService = (MondrianCatalogHelper) PentahoSystem.get(IMondrianCatalogService.class);
        	    	    result = mondrianCatalogService.generateInMemoryDatasourcesXml(repo);  
    	    	    }
    	    	    return result;
    	        }
    	      });
    	  } catch (Exception e) {
    	    throw new RuntimeException(e);
    	  }
    	  return datasourcesXml;
      }
    };
  }
  
  @Override
  protected CatalogLocator makeCatalogLocator(ServletConfig servletConfig) {
    return new ServletContextCatalogLocator(servletConfig.getServletContext()) {
      @Override
      public String locate(String catalogPath) {
    	if(catalogPath.startsWith("mondrian:")) { //$NON-NLS-1$
    		try {
    			FileSystemManager fsManager = VFS.getManager();
    			SolutionRepositoryVfsFileObject catalog = (SolutionRepositoryVfsFileObject) fsManager.resolveFile(catalogPath);
    			catalogPath = "solution:" + catalog.getFileRef();
    		} catch (FileSystemException e) {
    			logger.error(e.getMessage());
    		}
    	} else {
    		catalogPath = super.locate(catalogPath);
    	}
    	return catalogPath;  
      }
    };
  }

  @Override
  protected String makeDataSourcesUrl(ServletConfig config) {
	return null;
  }
  
  @Override
  protected ConnectionFactory createConnectionFactory(final ServletConfig servletConfig) throws ServletException {
      
    final ConnectionFactory delegate =
      super.createConnectionFactory(servletConfig);
    
    /*
     * This wrapper for the connection factory allows us to
     * override the list of roles with the ones defined in
     * the IPentahoSession and filter it through the
     * IConnectionUserRoleMapper.
     */
    return new ConnectionFactory() {

      public Map<String, Object> getPreConfiguredDiscoverDatasourcesResponse() {
        return delegate.getPreConfiguredDiscoverDatasourcesResponse();
      }
      
      
      public OlapConnection getConnection(
        String databaseName,
        String catalogName,
        String roleName,
        Properties props)
        throws SQLException
      {
        // What we do here is to filter the role names with the mapper.
        // First, get a user role mapper, if one is configured.
        final IPentahoSession session =
          PentahoSessionHolder.getSession();
        
        final IConnectionUserRoleMapper mondrianUserRoleMapper =
          PentahoSystem.get(
              IConnectionUserRoleMapper.class,
              MDXConnection.MDX_CONNECTION_MAPPER_KEY,
              null); // Don't use the user session here yet.
        
        String[] effectiveRoles = new String[0];
        
        /*
         * If Catalog/Schema are null (this happens with high level metadata requests,
         * like DISCOVER_DATASOURCES) we can't use the role mapper, even if it
         * is present and configured. 
         */
        if (mondrianUserRoleMapper != null
            && catalogName != null) 
        {
          // Use the role mapper.
          try {
            effectiveRoles =
              mondrianUserRoleMapper
                .mapConnectionRoles(
                  session,
                  catalogName);
            if (effectiveRoles == null) {
              effectiveRoles = new String[0];
            }
          } catch (PentahoAccessControlException e) {
            throw new SQLException(e);
          }
        }
        
        // Now we tokenize that list.
        boolean addComma = false;
        roleName = ""; //$NON-NLS-1$
        for (String role : effectiveRoles) {
          if (addComma) {
            roleName = roleName.concat(","); //$NON-NLS-1$
          }
          roleName = roleName.concat(role);
          addComma = true;
        }
        

        // Now let the delegate connection factory do its magic.
        if (catalogName == null)
        return
          delegate.getConnection(
            databaseName,
            catalogName,
            roleName.equals("")
              ? null
              : roleName,
            props);
        else {
         //We create a connection differently so we can ensure that
          //the XMLA servlet shares the same MondrianServer instance as the rest
          //of the platform
          MondrianCatalog mc = org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogHelper.getInstance().getCatalog(catalogName,
                  PentahoSessionHolder.getSession());


          Connection con = DriverManager.getConnection(mc.getDataSourceInfo() + ";Catalog=" + mc.getDefinition(), makeCatalogLocator(servletConfig));


          MondrianServer server = MondrianServer.forConnection(con);
          FileRepository fr = new FileRepository(makeContentFinder(makeDataSourcesUrl(servletConfig)), makeCatalogLocator(servletConfig));
          return fr.getConnection(server, databaseName, catalogName, roleName, props);
        }
      }
    };
  }
}