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
