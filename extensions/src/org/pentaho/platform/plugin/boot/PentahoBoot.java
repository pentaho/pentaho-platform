/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.plugin.boot;

import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory.Scope;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginProvider;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.api.engine.IServiceManager;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.engine.core.system.boot.PentahoSystemBoot;
import org.pentaho.platform.engine.services.connection.datasource.dbcp.PooledDatasourceSystemListener;
import org.pentaho.platform.engine.services.connection.datasource.dbcp.PooledOrJndiDatasourceService;
import org.pentaho.platform.engine.services.solution.SolutionEngine;
import org.pentaho.platform.plugin.action.jfreereport.JFreeReportSystemListener;
import org.pentaho.platform.plugin.action.kettle.KettleSystemListener;
import org.pentaho.platform.plugin.action.mondrian.MondrianSystemListener;
import org.pentaho.platform.plugin.outputs.FileOutputHandler;
import org.pentaho.platform.plugin.services.connections.mondrian.MDXConnection;
import org.pentaho.platform.plugin.services.connections.sql.SQLConnection;
import org.pentaho.platform.plugin.services.connections.xquery.XQConnection;
import org.pentaho.platform.plugin.services.pluginmgr.DefaultPluginManager;
import org.pentaho.platform.plugin.services.pluginmgr.PluginAdapter;
import org.pentaho.platform.plugin.services.pluginmgr.PluginResourceLoader;
import org.pentaho.platform.plugin.services.pluginmgr.SystemPathXmlPluginProvider;
import org.pentaho.platform.plugin.services.pluginmgr.servicemgr.DefaultServiceManager;
import org.pentaho.platform.repository2.unified.fs.FileSystemBackedUnifiedRepository;

/**
 * This class is designed to help embedded deployments start the Pentaho system
 * 
 * @author jamesdixon
 */
public class PentahoBoot extends PentahoSystemBoot {

  public PentahoBoot() {
    super();
  }

  /**
   * Sets up the defaults: - File-based repository - SQL datasource connections - MXL datasources - File outputs
   */
  @Override
  protected void configure( String solutionPath, String baseUrl, IPentahoDefinableObjectFactory factory ) {
    super.configure( null, null, null );
    IPentahoObjectFactory objectFactory = getFactory();
    if ( objectFactory instanceof IPentahoDefinableObjectFactory ) {
      define( ISolutionEngine.class, SolutionEngine.class, Scope.LOCAL );
      define( IUnifiedRepository.class, FileSystemBackedUnifiedRepository.class, Scope.SESSION );
      define( "connection-XML", XQConnection.class, Scope.LOCAL ); //$NON-NLS-1$
      define( "connection-SQL", SQLConnection.class, Scope.LOCAL ); //$NON-NLS-1$
      define( "file", FileOutputHandler.class, Scope.LOCAL ); //$NON-NLS-1$
    }
  }

  /**
   * Enables the components necessary to create reports
   */
  public void enableReporting() {
    addLifecycleListener( new JFreeReportSystemListener() );
  }

  /**
   * Enables the components necessary to create reports
   */
  public void enableOlap() {
    IPentahoObjectFactory objectFactory = getFactory();
    if ( objectFactory instanceof IPentahoDefinableObjectFactory ) {
      define( "connection-MDX", MDXConnection.class.getName(), Scope.LOCAL ); //$NON-NLS-1$
    }
    addLifecycleListener( new MondrianSystemListener() );
  }

  /**
   * Enables the plugin manager
   */
  public void enablePluginManager() {
    if ( getFactory() instanceof IPentahoDefinableObjectFactory ) {
      define( IPluginProvider.class, SystemPathXmlPluginProvider.class, Scope.GLOBAL );
      define( IPluginManager.class, DefaultPluginManager.class, Scope.GLOBAL );
      define( IServiceManager.class, DefaultServiceManager.class, Scope.GLOBAL );
      define( IPluginResourceLoader.class, PluginResourceLoader.class, Scope.GLOBAL );
    }
    addLifecycleListener( new PluginAdapter() );

  }

  /**
   * Enables the pooled datasources
   */
  public void enablePooledDatasources() {
    IPentahoObjectFactory objectFactory = getFactory();
    if ( objectFactory instanceof IPentahoDefinableObjectFactory ) {
      define( IDBDatasourceService.class, PooledOrJndiDatasourceService.class, Scope.LOCAL );
    }
    addLifecycleListener( new PooledDatasourceSystemListener() );
  }

  /**
   * Enables the metadata services
   */
  public void enableMetadata() {
    IPentahoObjectFactory objectFactory = getFactory();
    if ( objectFactory instanceof IPentahoDefinableObjectFactory ) {
      define( IMetadataDomainRepository.class,
          org.pentaho.platform.plugin.services.metadata.CachingPentahoMetadataDomainRepository.class, Scope.GLOBAL );
    }
  }

  /**
   * Enables the components necessary to create reports
   */
  public void enableDataIntegration() {
    addLifecycleListener( new KettleSystemListener() );
  }

}
