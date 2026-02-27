/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.repository;

import java.util.StringTokenizer;

public class RepositoryDownloadWhitelist {

  private String extensions = "gif,jpg,jpeg,png,bmp,tiff,csv,xls,xlsx,pdf,txt,css,html,js,xml,doc,ppt,eml,properties";

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
