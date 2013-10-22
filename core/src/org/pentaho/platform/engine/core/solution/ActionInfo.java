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

package org.pentaho.platform.engine.core.solution;

import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.util.PentahoCheckedChainedException;

public class ActionInfo {

  /**
   * TODO the method PentahoSystem.parseActionString() should be converted into a ctor for this class, and then
   * removed. Callers should be adjusted. ActionResource.getLocationInSolution should be refactored into a method
   * in this class, and callers adjusted. In general, the solutionName, path and actionName should likely be
   * abstracted into an opaque class that can be passed around, instead of passing around 3 strings, and manually
   * combining and parsing them in many places.
   * 
   * @author Steven Barkdull
   * 
   */
  private String solutionName;

  private String path;

  private String actionName;

  public ActionInfo( final String solutionName, final String path, final String actionName ) {
    this.solutionName = solutionName;
    this.path = path;
    this.actionName = actionName;
  }

  public String getActionName() {
    return actionName;
  }

  public String getPath() {
    return path;
  }

  public String getSolutionName() {
    return solutionName;
  }

  public static ActionInfo parseActionString( final String actionString ) {
    return ActionInfo.parseActionString( actionString, true );
  }

  // TODO sbarkdull, instead of returning null on parse error, should throw ActionInfoParseException
  /**
   * Break an action string into it's 3 components: the solution Id, the path to action file, and the action name
   * 
   * @param actionString
   * @return
   */
  public static ActionInfo parseActionString( String actionString, boolean mustBeComplete ) {
    // parse a string in this format samples/reporting/JFR/report.xml into
    // the solution, path, and document name
    if ( StringUtils.isEmpty( actionString ) ) {
      return null;
    }

    // first normalize the path separators
    actionString = actionString.replace( '\\', RepositoryFile.SEPARATOR.charAt( 0 ) );

    // remove a leading '/'
    if ( actionString.charAt( 0 ) == RepositoryFile.SEPARATOR.charAt( 0 ) ) {
      actionString = actionString.substring( 1 );
    }

    String solution;
    String path = ""; //$NON-NLS-1$
    String name;
    int idx1 = actionString.indexOf( RepositoryFile.SEPARATOR.charAt( 0 ) );
    int idx2 = actionString.lastIndexOf( RepositoryFile.SEPARATOR.charAt( 0 ) );
    if ( idx1 == -1 ) {
      if ( mustBeComplete ) {
        // this is not a valid action String
        return null;
      } else {
        return new ActionInfo( actionString, null, null );
      }
    }
    solution = actionString.substring( 0, idx1 );
    name = actionString.substring( idx2 + 1 );
    if ( idx2 > idx1 ) {
      path = actionString.substring( idx1 + 1, idx2 );
    }
    // see if the name has an extension
    if ( !mustBeComplete && ( name.indexOf( '.' ) == -1 ) ) {
      // not really a filename
      if ( StringUtils.isEmpty( path ) ) {
        path = name;
      } else {
        path = path + RepositoryFile.SEPARATOR + name;
      }
      name = null;
    }
    return new ActionInfo( solution, path, name );
  }

  public String toString() {
    return ActionInfo.buildSolutionPath( this.solutionName, this.path, this.actionName );
  }

  public static String buildSolutionPath( final String solution, String path, String filename ) {
    StringBuffer buf = new StringBuffer();

    // if the solutionName is the same as the fileName and the path is empty, then we are
    // probably trying to load the solution root itself
    if ( solution != null && solution.equals( filename ) && "".equals( path ) ) { //$NON-NLS-1$
      filename = ""; //$NON-NLS-1$
    }
    if ( path != null && path.equalsIgnoreCase( "/" ) ) { //$NON-NLS-1$
      path = ""; //$NON-NLS-1$
    }
    if ( StringUtils.isEmpty( path ) ) {
      if ( !StringUtils.isEmpty( filename ) && filename.charAt( 0 ) == RepositoryFile.SEPARATOR.charAt( 0 ) ) {
        return buf.append( solution ).append( filename ).toString();
      } else {
        return buf.append( solution ).append( RepositoryFile.SEPARATOR.charAt( 0 ) ).append( filename ).toString();
      }
    } else if ( path.charAt( 0 ) == RepositoryFile.SEPARATOR.charAt( 0 ) ) {
      if ( !StringUtils.isEmpty( filename ) && filename.charAt( 0 ) == RepositoryFile.SEPARATOR.charAt( 0 ) ) {
        return buf.append( solution ).append( path ).append( filename ).toString();
      } else {
        return buf.append( solution ).append( path ).append( RepositoryFile.SEPARATOR.charAt( 0 ) ).append( filename )
            .toString();
      }
    } else {
      if ( !StringUtils.isEmpty( filename ) && filename.charAt( 0 ) == RepositoryFile.SEPARATOR.charAt( 0 ) ) {
        return buf.append( solution ).append( RepositoryFile.SEPARATOR.charAt( 0 ) ).append( path ).append( filename )
            .toString();
      } else {
        return buf.append( solution ).append( RepositoryFile.SEPARATOR.charAt( 0 ) ).append( path ).append(
            RepositoryFile.SEPARATOR.charAt( 0 ) ).append( filename ).toString();
      }
    }
  }

  public static class ActionInfoParseException extends PentahoCheckedChainedException {

    private static final long serialVersionUID = 420;

    public ActionInfoParseException( final String message, final Throwable reas ) {
      super( message, reas );
    }

    public ActionInfoParseException( final String message ) {
      super( message );
    }

    public ActionInfoParseException( final Throwable reas ) {
      super( reas );
    }

    public ActionInfoParseException() {
      super();
    }
  }
}
