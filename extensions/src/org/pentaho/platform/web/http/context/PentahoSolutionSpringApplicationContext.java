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
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.context.support.XmlWebApplicationContext;

import java.io.File;

/**
 * Overrides <code>getResourceByPath</code> so that relative paths are relative to the Pentaho solution repository's
 * system directory instead of being relative to servlet context root.
 * 
 * @author mlowery
 */
public class PentahoSolutionSpringApplicationContext extends XmlWebApplicationContext {

  protected Resource getResourceByPath( String path ) {
    Resource resource = null;
    String solutionPath = PentahoHttpSessionHelper.getSolutionPath( getServletContext() );
    if ( solutionPath != null ) {
      File file = new File( solutionPath + File.separator + "system" + File.separator + path ); //$NON-NLS-1$
      resource = new FileSystemResource( file );
    } else {
      resource = super.getResourceByPath( path );
    }
    return resource;
  }

}
