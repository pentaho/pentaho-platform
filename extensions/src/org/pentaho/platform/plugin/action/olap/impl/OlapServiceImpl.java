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
*/
package org.pentaho.platform.plugin.action.olap.impl;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;

import mondrian.olap.MondrianServer;
import mondrian.rolap.RolapConnection;
import mondrian.server.DynamicContentFinder;
import mondrian.server.MondrianServerRegistry;
import mondrian.spi.CatalogLocator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.olap4j.OlapConnection;
import org.olap4j.OlapException;
import org.pentaho.platform.api.engine.IConnectionUserRoleMapper;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.plugin.action.olap.IOlapConnectionFilter;
import org.pentaho.platform.plugin.action.olap.IOlapService;
import org.pentaho.platform.plugin.action.olap.IOlapServiceException;
import org.pentaho.platform.plugin.services.connections.mondrian.MDXConnection;
import org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper;
import org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper.HostedCatalogInfo;
import org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper.Olap4jServerInfo;

public class OlapServiceImpl implements IOlapService {

    public static final String MONDRIAN_DATASOURCE_FOLDER = "mondrian"; //$NON-NLS-1$
    public final static String DATASOURCE_NAME = "Pentaho";

    private static final Log LOG = getLogger();

    /*
     * Do not access these two fields directly. They need to be accessed through
     * getRepository and getHelper because we can't init them before spring is
     * done initializing the sub modules.
     */
    private IUnifiedRepository repository;
    private MondrianCatalogRepositoryHelper helper;

    private MondrianServer server = null;
    private final List<IOlapConnectionFilter> filters;

    protected static enum CatalogPermission {
        READ, WRITE
    }

    private static Log getLogger() {
        return LogFactory.getLog(IOlapService.class);
    }

    public OlapServiceImpl() {
        this(null);
    }

    public OlapServiceImpl(IUnifiedRepository repo) {
        this.repository = repo;
        this.filters = new CopyOnWriteArrayList<IOlapConnectionFilter>();
    }

    private synchronized IUnifiedRepository getRepository() {
        if (repository == null) {
            repository = PentahoSystem.get(IUnifiedRepository.class);
        }
        return repository;
    }

    private synchronized MondrianCatalogRepositoryHelper getHelper() {
        if (helper == null) {
            helper =
                new MondrianCatalogRepositoryHelper(
                    getRepository());
        }
        return helper;
    }

    public void addHostedCatalog(
        String name,
        String dataSourceInfo,
        InputStream inputStream,
        boolean overwrite,
        IPentahoSession session)
    {
        // Access
        if (!hasAccess(name, CatalogPermission.WRITE, session)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("user does not have access; throwing exception"); //$NON-NLS-1$
            }
            throw new IOlapServiceException(
                Messages.getInstance().getErrorString(
                    "OlapServiceImpl.ERROR_0003_INSUFFICIENT_PERMISSION"), //$NON-NLS-1$
                    IOlapServiceException.Reason.ACCESS_DENIED);
        }

        // check for existing vs. the overwrite flag.
        if (getCatalogs(session).contains(name) && !overwrite) {
          throw new IOlapServiceException(
              Messages.getInstance().getErrorString(
                  "OlapServiceImpl.ERROR_0004_ALREADY_EXISTS"), //$NON-NLS-1$
                  IOlapServiceException.Reason.ALREADY_EXISTS);
        }

        try {
            MondrianCatalogRepositoryHelper helper =
                new MondrianCatalogRepositoryHelper(getRepository());
            helper.addSchema(inputStream, name, dataSourceInfo);
          } catch (Exception e) {
            throw new IOlapServiceException(
                Messages.getInstance().getErrorString(
                    "OlapServiceImpl.ERROR_0008_ERROR_OCCURRED"), //$NON-NLS-1$
                    IOlapServiceException.Reason.convert(e));
          }
    }

    private boolean hasAccess(
        String name,
        CatalogPermission perm,
        IPentahoSession session)
    {
        // TODO Implement this. Keep it mind it could be a remote or a local catalog.
        // we also need to check for ABS vs. pure access.
        return true;
    }

    public void addOlap4jCatalog(
        String name,
        String className,
        String URL,
        String user,
        String password,
        Properties props,
        boolean overwrite,
        IPentahoSession session)
    {
        // Access
        if (!hasAccess(name, CatalogPermission.WRITE, session)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("user does not have access; throwing exception"); //$NON-NLS-1$
            }
            throw new IOlapServiceException(
                Messages.getInstance().getErrorString(
                    "OlapServiceImpl.ERROR_0003_INSUFFICIENT_PERMISSION"), //$NON-NLS-1$
                    IOlapServiceException.Reason.ACCESS_DENIED);
        }

        // check for existing vs. the overwrite flag.
        if (getCatalogs(session).contains(name) && !overwrite) {
          throw new IOlapServiceException(
              Messages.getInstance().getErrorString(
                  "OlapServiceImpl.ERROR_0004_ALREADY_EXISTS"), //$NON-NLS-1$
                  IOlapServiceException.Reason.ALREADY_EXISTS);
        }

        MondrianCatalogRepositoryHelper helper =
            new MondrianCatalogRepositoryHelper(getRepository());

        helper.addOlap4jServer(name, className, URL, user, password, props);
    }

    public void removeCatalog(String name, IPentahoSession session) {
        try {
            if (!getConnection(name, session)
                .getOlapCatalogs().asMap().containsKey(name))
            {
                throw new IOlapServiceException(
                    Messages.getInstance().getErrorString(
                        "MondrianCatalogHelper.ERROR_0015_CATALOG_NOT_FOUND",
                        name));
            }
        } catch (OlapException e) {
            throw new IOlapServiceException(e);
        }

        // Check Access
        if (!hasAccess(name, CatalogPermission.WRITE, session)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("user does not have access; throwing exception"); //$NON-NLS-1$
            }
            throw new IOlapServiceException(
                Messages.getInstance().getErrorString(
                    "OlapServiceImpl.ERROR_0003_INSUFFICIENT_PERMISSION"), //$NON-NLS-1$
                    IOlapServiceException.Reason.ACCESS_DENIED);
        }

        final RepositoryFile deletingFile =
            getRepository().getFile(RepositoryFile.SEPARATOR + "etc" //$NON-NLS-1$
            + RepositoryFile.SEPARATOR + "mondrian" + RepositoryFile.SEPARATOR + name); //$NON-NLS-1$

        if (deletingFile != null) {
            // We are dealing with a local datasource here. We can delete it.
            getRepository().deleteFile(
                deletingFile.getId(),
                "Deleting Mondrian Schema because of a request from "
                + session.getName());
        } else {
            // This could be a remote connection
            getHelper().deleteOlap4jServer(name);
        }
    }

    public void flushAll(IPentahoSession pentahoSession) {
        try {
            getServer().getConnection(null, null, null)
                .unwrap(RolapConnection.class).getCacheControl(null)
                    .flushSchemaCache();
        } catch (Exception e) {
            throw new IOlapServiceException(e);
        }
    }

    public List<String> getCatalogs(
        IPentahoSession session)
    throws IOlapServiceException
    {
        List<String> names = new ArrayList<String>();
        names.addAll(getHelper().getHostedCatalogs());
        names.addAll(getHelper().getOlap4jServers());
        return names;
    }

    public OlapConnection getConnection(
        String catalogName,
        IPentahoSession session)
    throws IOlapServiceException
    {
        // Check Access
        if (catalogName != null
            && !hasAccess(catalogName, CatalogPermission.READ, session))
        {
            if (LOG.isDebugEnabled()) {
                LOG.debug("user does not have access; throwing exception"); //$NON-NLS-1$
            }
            throw new IOlapServiceException(
                Messages.getInstance().getErrorString(
                    "OlapServiceImpl.ERROR_0003_INSUFFICIENT_PERMISSION"), //$NON-NLS-1$
                    IOlapServiceException.Reason.ACCESS_DENIED);
        }

        // Check if it is a remote server
        if (catalogName != null
            && getHelper().getOlap4jServers().contains(catalogName))
        {
            return makeOlap4jConnection(catalogName, session);
        }

        // Check its existence.
        if (catalogName != null
            && !getCatalogs(session).contains(catalogName))
        {
            throw new IOlapServiceException(
                Messages.getInstance().getErrorString(
                    "MondrianCatalogHelper.ERROR_0015_CATALOG_NOT_FOUND",
                    catalogName));
        }

        final IConnectionUserRoleMapper mapper =
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
        if (mapper != null
            && catalogName != null)
        {
          // Use the role mapper.
          try {
            effectiveRoles =
              mapper
                .mapConnectionRoles(
                  session,
                  catalogName);
            if (effectiveRoles == null) {
              effectiveRoles = new String[0];
            }
          } catch (PentahoAccessControlException e) {
            throw new IOlapServiceException(e);
          }
        }

        // Now we tokenize that list.
        boolean addComma = false;
        StringBuilder roleName = new StringBuilder();
        for (String role : effectiveRoles) {
          if (addComma) {
            roleName.append(","); //$NON-NLS-1$
          }
          roleName.append(role);
          addComma = true;
        }

        // Return a connection
        try {
            return getServer().getConnection(
                    DATASOURCE_NAME,
                    catalogName,
                    roleName.toString(),
                    new Properties());
        } catch (Exception e) {
            throw new IOlapServiceException(e);
        }
    }

    private OlapConnection makeOlap4jConnection(
        String name,
        IPentahoSession session)
    {
        final Olap4jServerInfo olapServerInfo =
            getHelper().getOlap4jServerInfo(name);
        assert olapServerInfo != null;

        // Make sure the driver is present
        try {
            Class.forName(olapServerInfo.className);
        } catch (ClassNotFoundException e) {
            throw new IOlapServiceException(e);
        }

        // As per the JDBC specs, we can set the user/pass into
        // connection properties called 'user' and 'password'.
        Properties newProps =
                new Properties(olapServerInfo.properties);

        // First, apply the filters.
        for (IOlapConnectionFilter filter : this.filters) {
            filter.filterProperties(newProps);
        }

        // Then override the user and password. We do this after the filters
        // so as not to expose this.
        if (olapServerInfo.user != null) {
            newProps.put(
                "user", olapServerInfo.user);
        }
        if (olapServerInfo.password != null) {
            newProps.put(
                "password", olapServerInfo.password);
        }

        try {
            Connection conn =
                DriverManager.getConnection(
                    olapServerInfo.URL, newProps);
            return conn.unwrap(OlapConnection.class);
        } catch (SQLException e) {
            throw new IOlapServiceException(e);
        }
    }

    private synchronized MondrianServer getServer() {
        if (server == null) {
            server =
                MondrianServerRegistry.INSTANCE.createWithRepository(
                    new DynamicContentFinder("http://not-needed.com") {
                        @Override
                        public String getContent() {
                            return getDatasourcesXml();
                        }
                    },
                    new CatalogLocator() {
                        public String locate(String URL) {
                            return URL;
                        }
                    });
        }
        return server;
    }

    private String getDatasourcesXml() {
        try {
            return
                SecurityHelper.getInstance().runAsSystem(
                    new Callable<String>() {
                        public String call() throws Exception {
                            return generateInMemoryDatasourcesXml();
                        }
                    });
        } catch (Exception e) {
            throw new IOlapServiceException(e);
        }
    }

    private String generateInMemoryDatasourcesXml() {
        StringBuffer datasourcesXML = new StringBuffer();
        datasourcesXML.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //$NON-NLS-1$
        datasourcesXML.append("<DataSources>\n"); //$NON-NLS-1$

        datasourcesXML.append("<DataSource>\n"); //$NON-NLS-1$
        datasourcesXML.append("<DataSourceName>" + DATASOURCE_NAME + "</DataSourceName>\n"); //$NON-NLS-1$
        datasourcesXML.append("<DataSourceDescription>Pentaho BI Platform Datasources</DataSourceDescription>\n"); //$NON-NLS-1$
        datasourcesXML.append("<URL>" + PentahoRequestContextHolder.getRequestContext().getContextPath() + "Xmla</URL>\n"); //$NON-NLS-1$
        datasourcesXML.append("<DataSourceInfo>Provider=mondrian</DataSourceInfo>\n"); //$NON-NLS-1$
        datasourcesXML.append("<ProviderName>PentahoXMLA</ProviderName>\n"); //$NON-NLS-1$
        datasourcesXML.append("<ProviderType>MDP</ProviderType>\n"); //$NON-NLS-1$
        datasourcesXML.append("<AuthenticationMode>Unauthenticated</AuthenticationMode>\n"); //$NON-NLS-1$
        datasourcesXML.append("<Catalogs>\n"); //$NON-NLS-1$

        // Start with local catalogs.
        for (String name : getHelper().getHostedCatalogs()) {
            final HostedCatalogInfo hostedServerInfo =
                getHelper().getHostedCatalogInfo(name);
            addCatalogXml(
                datasourcesXML,
                hostedServerInfo.name,
                hostedServerInfo.dataSourceInfo,
                hostedServerInfo.definition);
        }

        // Don't add the olap4j catalogs. This doesn't work for now.

        datasourcesXML.append("</Catalogs>\n"); //$NON-NLS-1$
        datasourcesXML.append("</DataSource>\n"); //$NON-NLS-1$
        datasourcesXML.append("</DataSources>\n"); //$NON-NLS-1$
        return datasourcesXML.toString();
    }

    private void addCatalogXml(StringBuffer str, String catalogName, String dsInfo, String definition) {
        assert definition != null;
        str.append("<Catalog name=\"" + catalogName + "\">\n"); //$NON-NLS-1$ //$NON-NLS-2$
        if (dsInfo != null) {
            str.append("<DataSourceInfo>" + dsInfo + "</DataSourceInfo>\n"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        str.append("<Definition>" + definition + "</Definition>\n"); //$NON-NLS-1$ //$NON-NLS-2$
        str.append("</Catalog>\n"); //$NON-NLS-1$
    }

    public void setConnectionFilters(Collection<IOlapConnectionFilter> filters) {
        this.filters.addAll(filters);
    }
}
