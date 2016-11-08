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

package org.pentaho.platform.repository2.unified.fileio;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.engine.services.outputhandler.BaseOutputHandler;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.util.web.MimeHelper;

public class RepositoryContentOutputHandler extends BaseOutputHandler {

  public IContentItem getFileOutputContentItem() {
    String filePath = getSolutionPath();
    if ( StringUtils.isEmpty( filePath )) {
      filePath = getContentRef();
    }
    if ( filePath.startsWith( "~/" ) || filePath.startsWith( "~\\" ) || filePath.equals( "~" ) ) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      filePath = ClientRepositoryPaths.getUserHomeFolderPath( getSession().getName() ) + "/"; //$NON-NLS-1$
      filePath =
          filePath
              + ( getSolutionPath().length() > 1 ? getSolutionPath().substring( 2 )
            : getSolutionPath().substring( 1 ) );
    }
    
    filePath = replaceIllegalChars( filePath );
    
    IContentItem contentItem = null;
    String requestedFileExtension = MimeHelper.getExtension( getMimeType() );
    if ( requestedFileExtension == null ) {
      contentItem = new RepositoryFileContentItem( filePath );
    } else {
      String tempFilePath =
          FilenameUtils.getFullPathNoEndSeparator( filePath ) + "/" + FilenameUtils.getBaseName( filePath )
              + requestedFileExtension;
      contentItem = new RepositoryFileContentItem( tempFilePath );
    }
    return contentItem;
  }

  protected String replaceIllegalChars( String inStr ) {
    String outStr = inStr.replaceAll( "'", "" );
    return outStr;
  }
  
}
