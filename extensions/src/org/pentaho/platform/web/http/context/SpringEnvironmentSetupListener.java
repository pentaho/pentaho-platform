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

import org.pentaho.platform.web.http.PentahoHttpSessionHelper;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;

/**
 * Spring needs to know the location of the pentaho "system" folder before startup. This class computes and stores this
 * path as a System property.
 * 
 * User: nbaker Date: 4/8/13
 */
public class SpringEnvironmentSetupListener implements ServletContextListener {

  @Override
  public void contextInitialized( ServletContextEvent servletContextEvent ) {

    ServletContext context = servletContextEvent.getServletContext();

    String solutionPath = PentahoHttpSessionHelper.getSolutionPath( context );
    if ( solutionPath != null ) {
      String systemPath = solutionPath + File.separator + "system";
      System.setProperty( "PentahoSystemPath", systemPath );
    }

  }

  @Override
  public void contextDestroyed( ServletContextEvent servletContextEvent ) {
  }

}
