package org.pentaho.platform.plugin.action.mondrian.catalog;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;

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
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.plugin.services.connections.mondrian.MDXConnection;
import org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper;
import org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper.OlapServerInfo;
import org.pentaho.platform.repository2.ClientRepositoryPaths;

public class OlapServiceImpl implements IOlapService {

    public static final String MONDRIAN_DATASOURCE_FOLDER = "mondrian"; //$NON-NLS-1$
    public final static String DATASOURCE_NAME = "Pentaho";

    private static final Log LOG = getLogger();

    private MondrianServer server = null;

    protected static enum CatalogPermission {
        READ, WRITE
    }

    private static Log getLogger() {
        return LogFactory.getLog(IOlapService.class);
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
                new MondrianCatalogRepositoryHelper(
                PentahoSystem.get(IUnifiedRepository.class));
            helper.addSchema(inputStream, name, dataSourceInfo);
          } catch (Exception e) {
            throw new IOlapServiceException(
                Messages.getInstance().getErrorString(
                    "OlapServiceImpl.ERROR_0008_ERROR_OCCURRED"), //$NON-NLS-1$
                    IOlapServiceException.Reason.valueOf(e.getMessage()));
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

    public void addRemoteCatalog(
        String name,
        String className,
        String URL,
        String SSO,
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
            new MondrianCatalogRepositoryHelper(
            PentahoSystem.get(IUnifiedRepository.class));

        helper.addOlapServer(name, className, URL, SSO, user, password, props);
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

        final IUnifiedRepository solutionRepository =
            PentahoSystem.get(IUnifiedRepository.class);
        final MondrianCatalogRepositoryHelper helper =
            new MondrianCatalogRepositoryHelper(solutionRepository);
        final RepositoryFile deletingFile =
            solutionRepository.getFile(RepositoryFile.SEPARATOR + "etc" //$NON-NLS-1$
            + RepositoryFile.SEPARATOR + "mondrian" + RepositoryFile.SEPARATOR + name); //$NON-NLS-1$

        if (deletingFile != null) {
            // We are dealing with a local datasource here. We can delete it.
            solutionRepository.deleteFile(
                deletingFile.getId(),
                "Deleting Mondrian Schema because of a request from "
                + session.getName());
        } else {
            // This could be a remote connection
            helper.deleteOlapServer(name);
        }
    }

    public void flushAll(IPentahoSession pentahoSession) {
        try {
            server.getConnection(null, null, null)
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
        OlapConnection conn = null;
        try {
            conn =
                // We get a non-authenticated session here.
                // We want all catalogs for now.
                getServer().getConnection(
                    DATASOURCE_NAME, null, null);

            // First, add the local ones
            for (String name : conn.getOlapCatalogs().asMap().keySet()) {
                // Check for access rights.
                if (hasAccess(name, CatalogPermission.READ, session)) {
                    names.add(name);
                }
            }

            // Now add the remote ones
            MondrianCatalogRepositoryHelper helper =
                new MondrianCatalogRepositoryHelper(
                PentahoSystem.get(IUnifiedRepository.class));
            for (String name : helper.getOlapServers()) {
                // Check for access rights.
                if (hasAccess(name, CatalogPermission.READ, session)) {
                    names.add(name);
                }
            }

            // Done.
            return names;
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e1) {
                    // Don't care.
                    LOG.debug(
                        "Failed to cleanup the connection.",
                        e1);
                }
            }
            throw new IOlapServiceException(e);
        }
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
        MondrianCatalogRepositoryHelper helper =
            new MondrianCatalogRepositoryHelper(
            PentahoSystem.get(IUnifiedRepository.class));
        if (catalogName != null
            && helper.getOlapServers().contains(catalogName))
        {
            return makeRemoteConnection(catalogName, session);
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

    private OlapConnection makeRemoteConnection(
        String name,
        IPentahoSession session)
    {
        MondrianCatalogRepositoryHelper helper =
            new MondrianCatalogRepositoryHelper(
            PentahoSystem.get(IUnifiedRepository.class));
        OlapServerInfo olapServerInfo = helper.getOlapServer(name);

        // Make sure the driver is present
        try {
            Class.forName(olapServerInfo.className);
        } catch (ClassNotFoundException e) {
            throw new IOlapServiceException(e);
        }

        Properties newProps =
                new Properties(olapServerInfo.properties);
        if (olapServerInfo.SSO.equalsIgnoreCase("true")) {
            newProps.put(
                "user", session.getName());
            // TODO pass the password here.
            newProps.put(
                "password", "NOT_IMPLEMENTED");
        } else {
            newProps.put(
                "user", olapServerInfo.user);
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
        IUnifiedRepository unifiedRepository =
            PentahoSystem.get(IUnifiedRepository.class);
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

        //Creates <Catalogs> from the "/etc/mondrian/<catalog>/metadata" nodes.
        /*IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
        String tenantEtcFolder = null;
        if(pentahoSession != null) {
          String tenantId = (String) pentahoSession.getAttribute(IPentahoSession.TENANT_ID_KEY);
          tenantEtcFolder = ServerRepositoryPaths.getTenantEtcFolderPath(tenantId);
        } else {
          tenantEtcFolder = ServerRepositoryPaths.getTenantEtcFolderPath();
        }*/

        String etcMondrian =
            ClientRepositoryPaths.getEtcFolderPath()
            + RepositoryFile.SEPARATOR
            + MONDRIAN_DATASOURCE_FOLDER;

        RepositoryFile etcMondrianFolder = unifiedRepository.getFile(etcMondrian);
        if (etcMondrianFolder != null) {
          List<RepositoryFile> mondrianCatalogs = unifiedRepository.getChildren(etcMondrianFolder.getId());

          for (RepositoryFile catalog : mondrianCatalogs) {

            String catalogName = catalog.getName();
            RepositoryFile metadata = unifiedRepository.getFile(etcMondrian + RepositoryFile.SEPARATOR + catalogName
                + RepositoryFile.SEPARATOR + "metadata"); //$NON-NLS-1$

            if (metadata != null) {
              DataNode metadataNode = unifiedRepository.getDataForRead(metadata.getId(), NodeRepositoryFileData.class)
                  .getNode();
              String datasourceInfo = metadataNode.getProperty("datasourceInfo").getString(); //$NON-NLS-1$
              String definition = metadataNode.getProperty("definition").getString(); //$NON-NLS-1$

              datasourcesXML.append("<Catalog name=\"" + catalogName + "\">\n"); //$NON-NLS-1$ //$NON-NLS-2$
              datasourcesXML.append("<DataSourceInfo>" + datasourceInfo + "</DataSourceInfo>\n"); //$NON-NLS-1$ //$NON-NLS-2$
              datasourcesXML.append("<Definition>" + definition + "</Definition>\n"); //$NON-NLS-1$ //$NON-NLS-2$
              datasourcesXML.append("</Catalog>\n"); //$NON-NLS-1$
            } else {
              LOG.warn(Messages.getInstance().getString("MondrianCatalogHelper.WARN_META_DATA_IS_NULL")); //$NON-NLS-1$
            }
          }

          datasourcesXML.append("</Catalogs>\n"); //$NON-NLS-1$
          datasourcesXML.append("</DataSource>\n"); //$NON-NLS-1$
          datasourcesXML.append("</DataSources>\n"); //$NON-NLS-1$
          return datasourcesXML.toString();
        } else {
          return null;
        }
      }
}
