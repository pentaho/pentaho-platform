/*
 * Copyright 2002 - 2013 Pentaho Corporation.  All rights reserved.
 *
 * This software was developed by Pentaho Corporation and is provided under the terms
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. TThe Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
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
        throws ServletException
    {
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
// The code below will be needed when we move to olap4j-xmlaserver
//
//            public void endRequest(Request arg0) {
//                // no op.
//            }
//            public XmlaExtra getExtra() {
//                OlapConnection conn = olapService
//                    .getConnection(null, PentahoSessionHolder.getSession());
//                try {
//                    return conn.unwrap(XmlaHandler.XmlaExtra.class);
//                } catch (SQLException e) {
//                    throw new IOlapServiceException(
//                        "Failed to obtain XmlaExtra form olap connection.",
//                        e);
//                } finally {
//                    try {
//                        conn.close();
//                    } catch (SQLException e) {
//                        // ignore.
//                        LOGGER.warn("Failed to close olap connection.", e);
//                    }
//                }
//            }
//            public Request startRequest(XmlaRequest request, OlapConnection conn) {
//                return null;
//            }
        };
    }
}