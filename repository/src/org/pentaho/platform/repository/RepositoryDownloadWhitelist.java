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

package org.pentaho.platform.repository;

import java.util.StringTokenizer;

public class RepositoryDownloadWhitelist {

  private String extensions = "gif,jpg,jpeg,png,bmp,tiff,csv,xls,xlsx,pdf,txt,css,html,js,xml,doc,ppt";

  public RepositoryDownloadWhitelist() {
  }

  public boolean accept( String filename ) {
    String extension = filename;
    if ( filename.lastIndexOf( '.' ) != -1 ) {
      extension = filename.substring( filename.lastIndexOf( '.' ) + 1 );
    }
    StringTokenizer extTok = new StringTokenizer( extensions, "," );
    while ( extTok.hasMoreTokens() ) {
      if ( extension.equalsIgnoreCase( extTok.nextToken() ) ) {
        return true;
      }
    }
    return false;
  }

  public String getExtensions() {
    return extensions;
  }

  public void setExtensions( String extensions ) {
    this.extensions = extensions;
  }

}
