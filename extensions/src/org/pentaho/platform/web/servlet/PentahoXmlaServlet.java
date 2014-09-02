/*
 * Copyright 2002 - 2014 Pentaho Corporation.  All rights reserved.
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
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import mondrian.olap.Connection;
import mondrian.olap.DriverManager;
import mondrian.olap.MondrianException;
import mondrian.olap.MondrianServer;
import mondrian.server.FileRepository;
import mondrian.server.RepositoryContentFinder;

import org.apache.commons.collections.Predicate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.tree.DefaultElement;
import org.olap4j.OlapConnection;
import org.pentaho.platform.api.engine.ICacheManager;
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
import mondrian.xmla.XmlaException;
import mondrian.xmla.XmlaHandler.ConnectionFactory;
import mondrian.xmla.impl.DynamicDatasourceXmlaServlet;

import static org.apache.commons.collections.CollectionUtils.*;

/**
 * Filters out <code>DataSource</code> elements that are not XMLA-related.
 * <p/>
 * Background: Pentaho re-used datasources.xml for non-XMLA purposes. But since <code>DefaultXmlaServlet</code> requires
 * actual XMLA datasources, this servlet extends <code>DefaultXmlaServlet</code> and removes the non-XMLA datasources
 * before continuing normal <code>DefaultXmlaServlet</code> behavior.
 * <p/>
 * The convention here is that any <code>DataSource</code> elements with
 * <code>&lt;ProviderType&gt;None&lt;/ProviderType&gt;</code> are considered non-XMLA and are filtered out.
 *
 * @author mlowery
 */
@SuppressWarnings ( "unchecked" )
public class PentahoXmlaServlet extends DynamicDatasourceXmlaServlet {

  // ~ Static fields/initializers ======================================================================================

  /**
   * A cache of {@link RepositoryContentFinder} implementations.
   * The key is the datasource URL.
   */
  final ICacheManager cacheMgr = PentahoSystem.getCacheManager( null );

  final String CACHE_REGION = "org.pentaho.platform.web.servlet.PentahoXmlaServlet";

  private static final long serialVersionUID = 5801343357261568600L;
  private static final Log logger = LogFactory.getLog( PentahoXmlaServlet.class );

  private final IUnifiedRepository repo;

  private final MondrianCatalogHelper mondrianCatalogService;

  // - Constructors ================================

  public PentahoXmlaServlet() {
    super();
    if ( !cacheMgr.cacheEnabled( CACHE_REGION ) ) {
      cacheMgr.addCacheRegion( CACHE_REGION );
    }
    repo = PentahoSystem.get( IUnifiedRepository.class );
    mondrianCatalogService = (MondrianCatalogHelper) PentahoSystem.get( IMondrianCatalogService.class );
    try {
      DefaultFileSystemManager dfsm = (DefaultFileSystemManager) VFS.getManager();
      if ( !dfsm.hasProvider( "mondrian" ) ) {
        dfsm.addProvider( "mondrian", new MondrianVfs() );
      }
    } catch ( FileSystemException e ) {
      logger.error( e.getMessage() );
    }
  }

  // ~ Methods =========================================================================================================

  @Override
  protected RepositoryContentFinder makeContentFinder( String dataSourcesUrl ) {
    // It is safe to cache these for now because their lambda doesn't
    // do anything with security.
    // BEWARE before making modifications that check security rights or all
    // other kind of stateful things.
    @SuppressWarnings ( "rawtypes" )
    Set keys = cacheMgr.getAllKeysFromRegionCache( CACHE_REGION );

    if ( !keys.contains( dataSourcesUrl ) ) {
      cacheMgr.putInRegionCache(
          CACHE_REGION,
          dataSourcesUrl,
          new DynamicContentFinder( dataSourcesUrl ) {
            @Override
            public String getContent() {
              try {
                String original = generateInMemoryDatasourcesXml();
                EntityResolver loader = new PentahoEntityResolver();
                Document originalDocument = XmlDom4JHelper.getDocFromString( original, loader );
                if ( PentahoXmlaServlet.logger.isDebugEnabled() ) {
                  PentahoXmlaServlet.logger
                      .debug( Messages.getInstance().getString( "PentahoXmlaServlet.DEBUG_ORIG_DOC", originalDocument.asXML() ) ); //$NON-NLS-1$
                }
                Document modifiedDocument = (Document) originalDocument.clone();
                List<Node> nodesToRemove = getNodesToRemove( modifiedDocument );
                if ( PentahoXmlaServlet.logger.isDebugEnabled() ) {
                  PentahoXmlaServlet.logger.debug( Messages.getInstance().getString(
                      "PentahoXmlaServlet.DEBUG_NODES_TO_REMOVE", String.valueOf( nodesToRemove.size() ) ) ); //$NON-NLS-1$
                }
                for ( Node node : nodesToRemove ) {
                  node.detach();
                }
                if ( PentahoXmlaServlet.logger.isDebugEnabled() ) {
                  PentahoXmlaServlet.logger.debug( Messages.getInstance().getString( "PentahoXmlaServlet.DEBUG_MOD_DOC", modifiedDocument.asXML() ) ); //$NON-NLS-1$
                }
                return modifiedDocument.asXML();
              } catch ( XmlParseException e ) {
                PentahoXmlaServlet.logger.error( Messages.getInstance().getString( "PentahoXmlaServlet.ERROR_0004_UNABLE_TO_GET_DOCUMENT_FROM_STRING" ), e ); //$NON-NLS-1$
                return null;
              }
            }

            private List<Node> getNodesToRemove( Document doc ) {
              List<Node> nodesToRemove = doc.selectNodes( "/DataSources/DataSource/Catalogs/Catalog" );
              filter( nodesToRemove,
                  new Predicate() {
                    @Override
                    public boolean evaluate( Object o ) {
                      Element el = ( (DefaultElement) o ).element( "DataSourceInfo" );

                      if ( el == null || el.getText() == null || el.getTextTrim().length() == 0 ) {
                        throw new XmlaException(
                            SERVER_FAULT_FC,
                            UNKNOWN_ERROR_CODE,
                            UNKNOWN_ERROR_FAULT_FS,
                            new MondrianException(
                                "DataSourceInfo not defined for " + ( (DefaultElement) o ).attribute( "name" ).getText() )
                        );
                      }
                      return el.getText().matches( "(?i).*EnableXmla=['\"]?false['\"]?" );
                    }
                  }
              );
              return nodesToRemove;
            }
          }
      );
    }
    return (RepositoryContentFinder)
        cacheMgr.getFromRegionCache( CACHE_REGION, dataSourcesUrl );
  }

  private String generateInMemoryDatasourcesXml() {
    try {
      return SecurityHelper.getInstance().runAsSystem(
          new Callable<String>() {
            public String call() throws Exception {
              return mondrianCatalogService.generateInMemoryDatasourcesXml( repo );
            }
          }
      );
    } catch ( Exception e ) {
      PentahoXmlaServlet.logger.error( e );
      throw new RuntimeException( e );
    }
  }

  @Override
  protected CatalogLocator makeCatalogLocator( ServletConfig servletConfig ) {
    return new ServletContextCatalogLocator( servletConfig.getServletContext() ) {
      @Override
      public String locate( String catalogPath ) {
        if ( catalogPath.startsWith( "mondrian:" ) ) { //$NON-NLS-1$
          try {
            FileSystemManager fsManager = VFS.getManager();
            SolutionRepositoryVfsFileObject catalog = (SolutionRepositoryVfsFileObject)
                fsManager.resolveFile( catalogPath );
            catalogPath = "solution:" + catalog.getFileRef();
          } catch ( FileSystemException e ) {
            logger.error( e.getMessage() );
          }
        } else {
          catalogPath = super.locate( catalogPath );
        }
        return catalogPath;
      }
    };
  }

  @Override
  protected String makeDataSourcesUrl( ServletConfig config ) {
    return "";
  }

  @Override
  protected ConnectionFactory createConnectionFactory( final ServletConfig servletConfig ) throws ServletException {

    final ConnectionFactory delegate =
        super.createConnectionFactory( servletConfig );

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
          Properties props )
        throws SQLException {
        // What we do here is to filter the role names with the mapper.
        // First, get a user role mapper, if one is configured.
        final IPentahoSession session =
            PentahoSessionHolder.getSession();

        final IConnectionUserRoleMapper mondrianUserRoleMapper =
            PentahoSystem.get(
                IConnectionUserRoleMapper.class,
                MDXConnection.MDX_CONNECTION_MAPPER_KEY,
                null ); // Don't use the user session here yet.

        String[] effectiveRoles = new String[0];

        /*
         * If Catalog/Schema are null (this happens with high level metadata requests,
         * like DISCOVER_DATASOURCES) we can't use the role mapper, even if it
         * is present and configured.
         */
        if ( mondrianUserRoleMapper != null
            && catalogName != null ) {
          // Use the role mapper.
          try {
            effectiveRoles =
                mondrianUserRoleMapper
                    .mapConnectionRoles(
                        session,
                        catalogName );
            if ( effectiveRoles == null ) {
              effectiveRoles = new String[0];
            }
          } catch ( PentahoAccessControlException e ) {
            throw new SQLException( e );
          }
        }

        // Now we tokenize that list.
        boolean addComma = false;
        roleName = ""; //$NON-NLS-1$
        for ( String role : effectiveRoles ) {
          if ( addComma ) {
            roleName = roleName.concat( "," ); //$NON-NLS-1$
          }
          roleName = roleName.concat( role );
          addComma = true;
        }


        // Now let the delegate connection factory do its magic.
        if ( catalogName == null ) {

          return
              delegate.getConnection(
                  databaseName,
                  catalogName,
                  roleName.equals( "" )
                      ? null
                      : roleName,
                  props
          );

        } else {
          //We create a connection differently so we can ensure that
          //the XMLA servlet shares the same MondrianServer instance as the rest
          //of the platform
          IMondrianCatalogService mcs =
              PentahoSystem.get( IMondrianCatalogService.class );

          MondrianCatalog mc =
              mcs.getCatalog( catalogName, PentahoSessionHolder.getSession() );

          if ( mc == null ) {
            throw new XmlaException(
                CLIENT_FAULT_FC,
                HSB_BAD_RESTRICTION_LIST_CODE,
                HSB_BAD_RESTRICTION_LIST_FAULT_FS,
                new MondrianException(
                    "No such catalog: " + catalogName )
            );
          }

          Connection con =
              DriverManager.getConnection(
                  mc.getDataSourceInfo() + ";Catalog=" + mc.getDefinition(),
                  makeCatalogLocator( servletConfig ) );

          try {
            final MondrianServer server = MondrianServer.forConnection( con );

            FileRepository fr =
                new FileRepository(
                    makeContentFinder( makeDataSourcesUrl( servletConfig ) ),
                    makeCatalogLocator( servletConfig ) );

            return fr.getConnection(
                server, databaseName, catalogName, roleName, props );
          } finally {
            con.close();
          }
        }
      }
    };
  }
}
