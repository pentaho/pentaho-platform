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

package org.pentaho.platform.web.servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * Delegates Servlet requests to a Spring-managed bean. Based in <code>FilterToBeanProxy</code>.
 * 
 * @author mlowery
 * @see
 */
public class ServletToBeanProxy implements Servlet {

  // ~ Static fields/initializers ======================================================================================

  private static final long serialVersionUID = 2934977002901688130L;

  private static final Log logger = LogFactory.getLog( ServletToBeanProxy.class );

  // ~ Instance fields =================================================================================================

  protected Servlet delegate;

  private ServletConfig servletConfig;

  private boolean initialized = false;

  private boolean servletContainerManaged = false;

  // ~ Constructors ====================================================================================================

  public ServletToBeanProxy() {
    super();
  }

  // ~ Methods =========================================================================================================

  public void destroy() {
    if ( ServletToBeanProxy.logger.isDebugEnabled() ) {
      ServletToBeanProxy.logger.debug( "[destroy]" );
    }
    if ( ( delegate != null ) && servletContainerManaged ) {
      if ( ServletToBeanProxy.logger.isDebugEnabled() ) {
        ServletToBeanProxy.logger.debug( "servletContainerManaged=true so calling destroy" );
      }
      delegate.destroy();
    }
  }

  public void service( final ServletRequest req, final ServletResponse res ) throws ServletException, IOException {
    if ( !initialized ) {
      doInit();
    }
    if ( ServletToBeanProxy.logger.isDebugEnabled() ) {
      ServletToBeanProxy.logger.debug( "[service]" );
    }
    delegate.service( req, res );
  }

  private synchronized void doInit() throws ServletException {
    if ( initialized ) {
      // already initialized, so don't re-initialize
      return;
    }

    if ( ServletToBeanProxy.logger.isDebugEnabled() ) {
      ServletToBeanProxy.logger.debug( "[doInit]" );
    }

    String targetBean = servletConfig.getInitParameter( "targetBean" ); //$NON-NLS-1$

    if ( ServletToBeanProxy.logger.isDebugEnabled() ) {
      ServletToBeanProxy.logger.debug( "targetBean=" + targetBean );
    }

    if ( "".equals( targetBean ) ) { //$NON-NLS-1$
      targetBean = null;
    }

    String lifecycle = servletConfig.getInitParameter( "lifecycle" ); //$NON-NLS-1$

    if ( ServletToBeanProxy.logger.isDebugEnabled() ) {
      ServletToBeanProxy.logger.debug( "lifecycle=" + lifecycle );
    }

    if ( "servlet-container-managed".equals( lifecycle ) ) { //$NON-NLS-1$
      servletContainerManaged = true;
    }

    ApplicationContext ctx = this.getContext( servletConfig );

    String beanName = null;

    if ( ( targetBean != null ) && ctx.containsBean( targetBean ) ) {
      if ( ServletToBeanProxy.logger.isDebugEnabled() ) {
        ServletToBeanProxy.logger.debug( "using targetBean" );
      }
      beanName = targetBean;
    } else if ( targetBean != null ) {
      throw new ServletException( "targetBean '" + targetBean + "' not found in context" );
    } else {
      if ( ServletToBeanProxy.logger.isDebugEnabled() ) {
        ServletToBeanProxy.logger.debug( "using targetClass" );
      }

      String targetClassString = servletConfig.getInitParameter( "targetClass" ); //$NON-NLS-1$

      if ( ( targetClassString == null ) || "".equals( targetClassString ) ) { //$NON-NLS-1$
        throw new ServletException( "targetClass or targetBean must be specified" );
      }

      Class targetClass;

      try {
        targetClass = Thread.currentThread().getContextClassLoader().loadClass( targetClassString );
      } catch ( ClassNotFoundException ex ) {
        throw new ServletException( "Class of type " + targetClassString + " not found in classloader" );
      }

      Map beans = BeanFactoryUtils.beansOfTypeIncludingAncestors( ctx, targetClass, true, true );

      if ( beans.size() == 0 ) {
        throw new ServletException( "Bean context must contain at least one bean of type " + targetClassString );
      }

      beanName = (String) beans.keySet().iterator().next();
    }

    Object object = ctx.getBean( beanName );

    if ( ServletToBeanProxy.logger.isDebugEnabled() ) {
      ServletToBeanProxy.logger.debug( "got bean=" + object );
    }

    if ( !( object instanceof Servlet ) ) {
      throw new ServletException( "Bean '" + beanName + "' does not implement javax.servlet.Servlet" );
    }

    delegate = (Servlet) object;

    if ( servletContainerManaged ) {
      if ( ServletToBeanProxy.logger.isDebugEnabled() ) {
        ServletToBeanProxy.logger.debug( "servletContainerManaged=true so calling init" );
      }
      delegate.init( servletConfig );
    }

    // Set initialized to true at the end of the synchronized method, so
    // that invocations of service() before this method has completed will not
    // cause NullPointerException
    initialized = true;
  }

  public ServletConfig getServletConfig() {
    return delegate.getServletConfig();
  }

  public String getServletInfo() {
    return delegate.getServletInfo();
  }

  protected ApplicationContext getContext( final ServletConfig config ) {
    return WebApplicationContextUtils.getRequiredWebApplicationContext( config.getServletContext() );
  }

  public void init( final ServletConfig config ) throws ServletException {
    this.servletConfig = config;

    if ( ServletToBeanProxy.logger.isDebugEnabled() ) {
      ServletToBeanProxy.logger.debug( "[init]" );
    }

    String strategy = servletConfig.getInitParameter( "init" ); //$NON-NLS-1$

    if ( ServletToBeanProxy.logger.isDebugEnabled() ) {
      ServletToBeanProxy.logger.debug( "init (strategy)=" + strategy );
    }

    if ( ( strategy != null ) && strategy.toLowerCase().equals( "lazy" ) ) { //$NON-NLS-1$
      if ( ServletToBeanProxy.logger.isDebugEnabled() ) {
        ServletToBeanProxy.logger.debug( "strategy=lazy so deferring init" );
      }
      return;
    }

    doInit();
  }

}
