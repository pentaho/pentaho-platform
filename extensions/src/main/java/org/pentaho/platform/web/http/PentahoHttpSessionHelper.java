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

package org.pentaho.platform.web.http;

import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;

public class PentahoHttpSessionHelper {

  // list of the default locations to search for the solution repository
  private static final String[] DEFAULT_LOCATIONS = { "/eclipse/workspace/pentaho-solutions", //$NON-NLS-1$  Solution samples from CVS in the default eclipse workspace
    "/eclipse/workspace/pentaho-samples/solutions/test-solution" //$NON-NLS-1$  All Samples from CVS in the default eclipse workspace
  };

  public static String getSolutionPath( final ServletContext context ) {
    File pentahoSolutions;

    // first try the web.xml setting
    String rootPath = context.getInitParameter( "solution-path" ); //$NON-NLS-1$
    if ( StringUtils.isNotBlank( rootPath ) ) {
      pentahoSolutions = new File( rootPath );
      if ( pentahoSolutions.exists() && pentahoSolutions.isDirectory() ) {
        return rootPath;
      }
    }

    for ( String element : DEFAULT_LOCATIONS ) {
      pentahoSolutions = new File( element );
      if ( pentahoSolutions.exists() && pentahoSolutions.isDirectory() ) {
        try {
          return pentahoSolutions.getCanonicalPath();
        } catch ( IOException e ) {
          return pentahoSolutions.getAbsolutePath();
        }
      }
    }

    // now try the path to the WEB-INF to see if we find
    File file = new File( context.getRealPath( "" ) ); //$NON-NLS-1$
    while ( file != null ) {
      if ( file.exists() && file.isDirectory() ) {
        pentahoSolutions = new File( file.getAbsolutePath() + File.separator + "pentaho-solutions" ); //$NON-NLS-1$
        if ( pentahoSolutions.exists() && pentahoSolutions.isDirectory() ) {
          try {
            return pentahoSolutions.getCanonicalPath();
          } catch ( IOException e ) {
            return pentahoSolutions.getAbsolutePath();
          }
        }
      }
      file = file.getParentFile();
    }
    return null;
  }
}
