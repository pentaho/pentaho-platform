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

package org.pentaho.platform.util;

/**
 * Bean that holds the results of the version information
 * 
 * @author dkincade
 */
public class VersionInfo {
  /**
   * The product id of the product
   */
  private String productID;

  /**
   * The text title of the product
   */
  private String title;

  /**
   * The major portion of the version number
   */
  private String versionMajor;

  /**
   * The minor portion of the version number
   */
  private String versionMinor;

  /**
   * The release portion of the version number
   */
  private String versionRelease;

  /**
   * The milestone portion of the version number
   */
  private String versionMilestone;

  /**
   * THe build number portion of the version number
   */
  private String versionBuild;

  /**
   * Boolean indicating if this was retrieved from the manifest (indicating that it is running from a build) or
   * from class files (indicating it is running from compiled class files).
   */
  private boolean fromManifest;

  public boolean isFromManifest() {
    return fromManifest;
  }

  public String getProductID() {
    return productID;
  }

  public String getTitle() {
    return title;
  }

  public String getVersionMajor() {
    return versionMajor;
  }

  public String getVersionMinor() {
    return versionMinor;
  }

  public String getVersionRelease() {
    return versionRelease;
  }

  public String getVersionMilestone() {
    return versionMilestone;
  }

  public String getVersionBuild() {
    return versionBuild;
  }

  public String getVersionNumber() {
    StringBuffer sb = new StringBuffer();
    if ( versionMajor != null ) {
      sb.append( versionMajor );
      if ( versionMinor != null ) {
        sb.append( '.' ).append( versionMinor );
        if ( versionRelease != null ) {
          sb.append( '.' ).append( versionRelease );
          if ( versionMilestone != null ) {
            sb.append( '.' ).append( versionMilestone );
            if ( versionBuild != null ) {
              sb.append( '.' ).append( versionBuild );
            }
          }
        }
      }
    }
    return sb.toString();
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append( "fromManifest       = [" ).append( fromManifest ).append( "]\n" ); //$NON-NLS-1$  //$NON-NLS-2$
    sb.append( "productID          = [" ).append( productID ).append( "]\n" ); //$NON-NLS-1$ //$NON-NLS-2$
    sb.append( "title              = [" ).append( title ).append( "]\n" ); //$NON-NLS-1$ //$NON-NLS-2$
    sb.append( "versionMajor       = [" ).append( versionMajor ).append( "]\n" ); //$NON-NLS-1$ //$NON-NLS-2$
    sb.append( "versionMinor       = [" ).append( versionMinor ).append( "]\n" ); //$NON-NLS-1$ //$NON-NLS-2$
    sb.append( "versionRelease     = [" ).append( versionRelease ).append( "]\n" ); //$NON-NLS-1$ //$NON-NLS-2$
    sb.append( "versionMilestone   = [" ).append( versionMilestone ).append( "]\n" ); //$NON-NLS-1$ //$NON-NLS-2$
    sb.append( "versionBuild       = [" ).append( versionBuild ).append( "]\n" ); //$NON-NLS-1$ //$NON-NLS-2$
    sb.append( "getVersionNumber() = [" ).append( getVersionNumber() ).append( "]\n" ); //$NON-NLS-1$ //$NON-NLS-2$
    return sb.toString();
  }

  void setFromManifest( final boolean fromManifest ) {
    this.fromManifest = fromManifest;
  }

  void setProductID( final String productID ) {
    this.productID = productID;
  }

  void setTitle( final String title ) {
    this.title = title;
  }

  void setVersionMajor( final String versionMajor ) {
    this.versionMajor = versionMajor;
  }

  void setVersionMinor( final String versionMinor ) {
    this.versionMinor = versionMinor;
  }

  void setVersionRelease( final String versionRelease ) {
    this.versionRelease = versionRelease;
  }

  void setVersionMilestone( final String versionMilestone ) {
    this.versionMilestone = versionMilestone;
  }

  void setVersionBuild( final String versionBuild ) {
    this.versionBuild = versionBuild;
  }

  /**
   * Sets the version fields by passing the whole string and parsing it. <br/>
   * NOTE: spaces will be changed to dots before parsing
   * 
   * @param version
   *          the version number (1.6.0.GA.500 or 1.6.0-RC2.400)
   */
  @SuppressWarnings( { "fallthrough" } )
  void setVersion( final String version ) {
    // Parse the version
    if ( version != null ) {
      String[] pieces = version.replace( '-', '.' ).split( "\\." ); //$NON-NLS-1$
      switch ( pieces.length ) {
        case 9: // just in case
        case 8: // just in case
        case 7: // just in case
        case 6: // just in case
        case 5:
          setVersionBuild( pieces[4] ); // intentional fall through
        case 4:
          setVersionMilestone( pieces[3] ); // intentional fall through
        case 3:
          setVersionRelease( pieces[2] ); // intentional fall through
        case 2:
          setVersionMinor( pieces[1] ); // intentional fall through
        case 1:
          setVersionMajor( pieces[0] ); // intentional fall through
        default: // do nothing
      }
    }
  }
}
