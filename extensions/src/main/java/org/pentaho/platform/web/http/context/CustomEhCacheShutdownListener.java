/*!
 *
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
 *
 * Copyright (c) 2024 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.context;

import org.ehcache.CacheManager;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * A ServletContextListener that shutsdown CacheManager. Use this when you want to shutdown
 * ehcache automatically when the web application is shutdown.
 * <p/>
 * To receive notification events, this class must be configured in the deployment
 * descriptor for the web application.
 *
 * To do so, add the following to web.xml in your web application:
 * <pre>
 * &lt;listener&gt;
 *      &lt;listener-class&gt;net.sf.ehcache.constructs.web.ShutdownListener&lt;/listener-class&gt;
 * &lt;/listener&gt;
 * <p/>
 * </pre>
 *
 * @author Daniel Wiell
 * @author Greg Luck
 * @version $Id: ShutdownListener.java 744 2008-08-16 20:10:49Z gregluck $
 */
public class CustomEhCacheShutdownListener implements ServletContextListener {

    private static final Logger LOG = LoggerFactory.getLogger( CustomEhCacheShutdownListener.class );
    private static final List<CacheManager> cacheManagers = new ArrayList<>();

    /**
     * Notification that the web application is ready to process requests.
     *
     * @param servletContextEvent
     */
    public void contextInitialized( ServletContextEvent servletContextEvent ) {
        // Initialize CacheManager and add to the list
        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build( true );
        cacheManagers.add( cacheManager );
    }

    /**
     * Notification that the servlet context is about to be shut down.
     * <p/>
     * Shuts down all cache managers known to {@link CacheManager}
     *
     * @param servletContextEvent
     */
    public void contextDestroyed( ServletContextEvent servletContextEvent ) {
        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "Shutting down " + cacheManagers.size() + " CacheManagers." );
        }
        for ( CacheManager cacheManager : cacheManagers ) {
            cacheManager.close();
        }
    }
}
