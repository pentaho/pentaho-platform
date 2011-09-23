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

import java.util.List;

import javax.servlet.ServletConfig;

import mondrian.server.DynamicContentFinder;
import mondrian.xmla.impl.DynamicDatasourceXmlaServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Node;
import org.pentaho.platform.api.util.XmlParseException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.solution.PentahoEntityResolver;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
import org.pentaho.platform.web.servlet.messages.Messages;
import org.xml.sax.EntityResolver;

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
public class PentahoXmlaServlet extends DynamicDatasourceXmlaServlet {

  // ~ Static fields/initializers ======================================================================================

  private static final long serialVersionUID = 5801343357261568600L;
  private static final Log logger = LogFactory.getLog(PentahoXmlaServlet.class);

  // - Constructors ================================

  public PentahoXmlaServlet() {
    super();
  }

  // ~ Methods =========================================================================================================

  
  protected DynamicContentFinder makeContentFinder(String dataSourcesUrl) {
    return new DynamicContentFinder(dataSourcesUrl) {
      @Override
      public String getContent() {
        String original = super.getContent();
        EntityResolver loader = new PentahoEntityResolver();
        Document originalDocument = null;
        try {
          originalDocument = XmlDom4JHelper.getDocFromString(original, loader);
        } catch(XmlParseException e) {
          PentahoXmlaServlet.logger.error(Messages.getInstance().getString("PentahoXmlaServlet.ERROR_0004_UNABLE_TO_GET_DOCUMENT_FROM_STRING"), e); //$NON-NLS-1$
          return null;
        }
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
        return modifiedDocument.asXML();
      }
    };
  }

  protected String makeDataSourcesUrl(ServletConfig config) {
    final String path =
      "file:" + //$NON-NLS-1$
      PentahoSystem
        .getApplicationContext()
        .getSolutionPath("system/olap/datasources.xml");  //$NON-NLS-1$
    if (logger.isDebugEnabled()) {
      logger.debug(
        Messages.getInstance()
          .getString(
            "PentahoXmlaServlet.DEBUG_SCHEMA_FILE_PATH",
            path));
    }
    return path;
  }
}