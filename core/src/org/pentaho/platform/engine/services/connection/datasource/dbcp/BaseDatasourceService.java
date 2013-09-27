/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.services.connection.datasource.dbcp;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.pentaho.platform.api.data.DBDatasourceServiceException;
import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public abstract class BaseDatasourceService implements IDBDatasourceService {
  ICacheManager cacheManager;

  public BaseDatasourceService() {
    cacheManager = PentahoSystem.getCacheManager( null );
    // if no cache manager implementation is available we'll use the simple one
  }

  /**
   * This method clears the JNDI DS cache. The need exists because after a JNDI connection edit the old DS must be
   * removed from the cache.
   * 
   */
  public void clearCache() {
    cacheManager.removeRegionCache( IDBDatasourceService.JDBC_DATASOURCE );
  }

  /**
   * This method clears the JNDI DS cache. The need exists because after a JNDI connection edit the old DS must be
   * removed from the cache.
   * 
   */
  public void clearDataSource( String dsName ) {
    cacheManager.removeFromRegionCache( IDBDatasourceService.JDBC_DATASOURCE, dsName );
  }

  protected DataSource getJndiDataSource( final String dsName ) throws DBDatasourceServiceException {
    Object foundDs = null;
    if ( !cacheManager.cacheEnabled( IDBDatasourceService.JDBC_DATASOURCE ) ) {
      cacheManager.addCacheRegion( IDBDatasourceService.JDBC_DATASOURCE );
    }
    foundDs = cacheManager.getFromRegionCache( IDBDatasourceService.JDBC_DATASOURCE, dsName );
    if ( foundDs != null ) {
      return (DataSource) foundDs;
    }
    try {
      InitialContext ctx = new InitialContext();
      Object lkup = null;
      DataSource rtn = null;
      NamingException firstNe = null;
      // First, try what they ask for...
      try {
        lkup = ctx.lookup( dsName );
        if ( lkup != null ) {
          rtn = (DataSource) lkup;
          cacheManager.putInRegionCache( IDBDatasourceService.JDBC_DATASOURCE, dsName, rtn );
          return rtn;
        }
      } catch ( NamingException ignored ) {
        firstNe = ignored;
      }
      try {
        // Needed this for Jboss
        lkup = ctx.lookup( "java:" + dsName ); //$NON-NLS-1$
        if ( lkup != null ) {
          rtn = (DataSource) lkup;
          cacheManager.putInRegionCache( IDBDatasourceService.JDBC_DATASOURCE, dsName, rtn );
          return rtn;
        }
      } catch ( NamingException ignored ) {
      }
      try {
        // Tomcat
        lkup = ctx.lookup( "java:comp/env/jdbc/" + dsName ); //$NON-NLS-1$
        if ( lkup != null ) {
          rtn = (DataSource) lkup;
          cacheManager.putInRegionCache( IDBDatasourceService.JDBC_DATASOURCE, dsName, rtn );
          return rtn;
        }
      } catch ( NamingException ignored ) {
      }
      try {
        // Others?
        lkup = ctx.lookup( "jdbc/" + dsName ); //$NON-NLS-1$
        if ( lkup != null ) {
          rtn = (DataSource) lkup;
          cacheManager.putInRegionCache( IDBDatasourceService.JDBC_DATASOURCE, dsName, rtn );
          return rtn;
        }
      } catch ( NamingException ignored ) {
      }
      if ( firstNe != null ) {
        throw new DBDatasourceServiceException( firstNe );
      }
      throw new DBDatasourceServiceException( dsName );
    } catch ( NamingException ne ) {
      throw new DBDatasourceServiceException( ne );
    }
  }

  /**
   * Since JNDI is supported different ways in different app servers, it's nearly impossible to have a ubiquitous way to
   * look up a datasource. This method is intended to hide all the lookups that may be required to find a jndi name, and
   * return the actual bound name.
   * 
   * @param dsName
   *          The Datasource name (like SampleData)
   * @return The bound DS name if it is bound in JNDI (like "jdbc/SampleData")
   * @throws DBDatasourceServiceException
   */
  public String getDSBoundName( final String dsName ) throws DBDatasourceServiceException {
    try {
      InitialContext ctx = new InitialContext();
      Object lkup = null;
      NamingException firstNe = null;
      String rtn = dsName;
      // First, try what they ask for...
      try {
        lkup = ctx.lookup( rtn );
        if ( lkup != null ) {
          return rtn;
        }
      } catch ( NamingException ignored ) {
        firstNe = ignored;
      }
      try {
        // Needed this for Jboss
        rtn = "java:" + dsName; //$NON-NLS-1$
        lkup = ctx.lookup( rtn );
        if ( lkup != null ) {
          return rtn;
        }
      } catch ( NamingException ignored ) {
      }
      try {
        // Tomcat
        rtn = "java:comp/env/jdbc/" + dsName; //$NON-NLS-1$
        lkup = ctx.lookup( rtn );
        if ( lkup != null ) {
          return rtn;
        }
      } catch ( NamingException ignored ) {
      }
      try {
        // Others?
        rtn = "jdbc/" + dsName; //$NON-NLS-1$
        lkup = ctx.lookup( rtn );
        if ( lkup != null ) {
          return rtn;
        }
      } catch ( NamingException ignored ) {
      }
      if ( firstNe != null ) {
        throw new DBDatasourceServiceException( firstNe );
      }
      throw new DBDatasourceServiceException( dsName );
    } catch ( NamingException ne ) {
      throw new DBDatasourceServiceException( ne );
    }
  }

  /**
   * Since JNDI is supported different ways in different app servers, it's nearly impossible to have a ubiquitous way to
   * look up a datasource. This method is intended to extract just the regular name of a specified JNDI source.
   * 
   * @param dsName
   *          The Datasource name (like "jdbc/SampleData")
   * @return The unbound DS name (like "SampleData")
   */
  public String getDSUnboundName( final String dsName ) {
    if ( null == dsName ) {
      return null;
    }
    final String PREFIX_TOMCAT = "java:comp/env/jdbc/"; //$NON-NLS-1$
    final String PREFIX_JBOSS = "java:"; //$NON-NLS-1$
    final String PREFIX_OTHER = "jdbc/"; //$NON-NLS-1$

    // order is important here since jboss is a substring of tomcat
    if ( dsName.startsWith( PREFIX_TOMCAT ) ) {
      return dsName.substring( PREFIX_TOMCAT.length() );
    } else if ( dsName.startsWith( PREFIX_JBOSS ) ) {
      return dsName.substring( PREFIX_JBOSS.length() );
    } else if ( dsName.startsWith( PREFIX_OTHER ) ) {
      return dsName.substring( PREFIX_OTHER.length() );
    } else {
      // select that last token from the string
      int last = dsName.lastIndexOf( "/" ); //$NON-NLS-1$
      if ( last < dsName.lastIndexOf( ":" ) ) { //$NON-NLS-1$
        last = dsName.lastIndexOf( ":" ); //$NON-NLS-1$
      }
      if ( last != -1 ) {
        return dsName.substring( last + 1 );
      } else {
        return dsName;
      }
    }
  }
}
