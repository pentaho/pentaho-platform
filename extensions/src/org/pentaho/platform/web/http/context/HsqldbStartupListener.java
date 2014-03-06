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

package org.pentaho.platform.web.http.context;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.web.hsqldb.HsqlDatabaseStarterBean;
import org.pentaho.platform.web.hsqldb.messages.Messages;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class HsqldbStartupListener implements ServletContextListener {

  private static final Log logger = LogFactory.getLog( HsqldbStartupListener.class );

  public void contextDestroyed( ServletContextEvent sce ) {
    ServletContext ctx = sce.getServletContext();
    Object obj = ctx.getAttribute( "hsqldb-starter-bean" ); //$NON-NLS-1$
    if ( obj != null ) {
      logger.debug( "Context listener stopping Embedded HSQLDB" ); //$NON-NLS-1$
      HsqlDatabaseStarterBean starterBean = (HsqlDatabaseStarterBean) obj;
      starterBean.stop();
    }
  }

  private Map<String, String> getDatabases( ServletContext ctx ) {
    HashMap<String, String> map = new LinkedHashMap<String, String>();
    String dbs = ctx.getInitParameter( "hsqldb-databases" ); //$NON-NLS-1$
    String[] dbEntries = dbs.split( "," ); //$NON-NLS-1$
    for ( int i = 0; i < dbEntries.length; i++ ) {
      String[] entry = dbEntries[i].split( "@" ); //$NON-NLS-1$
      if ( ( entry.length != 2 ) || ( StringUtils.isEmpty( entry[0] ) ) || ( StringUtils.isEmpty( entry[1] ) ) ) {
        logger.equals( Messages.getErrorString( "HsqlDatabaseStartupListener.ERROR_0001_HSQLDB_ENTRY_MALFORMED" ) ); //$NON-NLS-1$
        continue;
      }
      map.put( entry[0], entry[1] );
    }
    return map;
  }

  public void contextInitialized( ServletContextEvent sce ) {
    ServletContext ctx = sce.getServletContext();

    logger.debug( "Starting HSQLDB Embedded Listener" ); //$NON-NLS-1$
    HsqlDatabaseStarterBean starterBean = new HsqlDatabaseStarterBean();
    String port = ctx.getInitParameter( "hsqldb-port" ); //$NON-NLS-1$
    int portNum = -1;
    if ( port != null ) {
      logger.debug( String.format( "Port override specified: %s", port ) ); //$NON-NLS-1$
      try {
        portNum = Integer.parseInt( port );
        starterBean.setPort( portNum );
      } catch ( NumberFormatException ex ) {
        logger.error( Messages.getErrorString( "HsqldbStartupListener.ERROR_0004_INVALID_PORT", "9001" ) ); //$NON-NLS-1$
        port = null; // force check default port
      }
    }

    starterBean.setDatabases( getDatabases( ctx ) );

    String sampleDataAllowPortFailover = ctx.getInitParameter( "hsqldb-allow-port-failover" ); //$NON-NLS-1$
    if ( ( sampleDataAllowPortFailover != null ) && ( sampleDataAllowPortFailover.equalsIgnoreCase( "true" ) ) ) { //$NON-NLS-1$
      logger.debug( String.format( "Allow Port Failover specified" ) ); //$NON-NLS-1$
      starterBean.setAllowPortFailover( true );
    }

    if ( starterBean.start() ) {
      ctx.setAttribute( "hsqldb-starter-bean", starterBean ); //$NON-NLS-1$
    }

  }

}
