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
 * Copyright 2010 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.platform.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.spring.container.servlet.SpringServlet;

/**
 * This should only be used by a plugin in the plugin.spring.xml file to initialize a Jersey.  The
 * presence of this servlet in the spring file will make it possible to write JAX-RS POJOs in your
 * plugin.
 * @author Aaron Phillips
 */
public class JAXRSPluginServlet extends SpringServlet implements ApplicationContextAware {

  private static final long serialVersionUID = 457538570048660945L;

  private ApplicationContext applicationContext;

  private static final Log logger = LogFactory.getLog(JAXRSPluginServlet.class);

  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  @Override
  protected ConfigurableApplicationContext getContext() {
    return (ConfigurableApplicationContext) applicationContext;
  }

  @Override
  public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    logger.debug("servicing request for resource " + request.getPathInfo()); //$NON-NLS-1$
    super.service(request, response);
  }

  @Override
  public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
    super.service(req, res);
  }

  @Override
  protected void initiate(ResourceConfig rc, WebApplication wa) {
    if (logger.isDebugEnabled()) {
      rc.getFeatures().put(ResourceConfig.FEATURE_TRACE, true);
      rc.getFeatures().put(ResourceConfig.FEATURE_TRACE_PER_REQUEST, true);
    }
    super.initiate(rc, wa);
  }

}
