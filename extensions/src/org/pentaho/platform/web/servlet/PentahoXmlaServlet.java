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
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import mondrian.xmla.XmlaHandler.ConnectionFactory;
import mondrian.xmla.impl.DefaultXmlaServlet;

import org.olap4j.OlapConnection;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.olap.IOlapService;

public class PentahoXmlaServlet extends DefaultXmlaServlet {

    private static final long serialVersionUID = 1L;
    private final IOlapService olapService;

    public PentahoXmlaServlet() {
        super();
        olapService = PentahoSystem.get(IOlapService.class);
    }

    @Override
    protected ConnectionFactory createConnectionFactory(ServletConfig arg0)
            throws ServletException {
        return new ConnectionFactory() {
            public Map<String, Object> getPreConfiguredDiscoverDatasourcesResponse() {
                return null;
            }
            public OlapConnection getConnection(
                String databaseName,
                String catalogName,
                String roleNames,
                Properties props)
            throws SQLException
            {
                // We ignore databaseName because there can only be 1.
                // We ignore the roleNames because we get them from the session.
                return olapService
                    .getConnection(
                        catalogName,
                        PentahoSessionHolder.getSession());
            }
        };
    }
}